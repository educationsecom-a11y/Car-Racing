package com.example.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class SoundManager(context: Context) {
  private val appContext = context.applicationContext

  private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
    vibratorManager?.defaultVibrator
  } else {
    @Suppress("DEPRECATION")
    appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  }

  var isSoundEnabled: Boolean = true
  var isVibrationEnabled: Boolean = true

  private val sampleRate = 22050
  private var engineTrack: AudioTrack? = null
  private var engineJob: Job? = null
  private val audioScope = CoroutineScope(Dispatchers.Default)

  @Volatile
  private var currentEngineRpmRatio = 0.1f // 0.0 (idle) to 1.0 (redline)

  @Volatile
  private var isEngineRunning = false

  fun startEngineSound() {
    if (!isSoundEnabled || isEngineRunning) return
    isEngineRunning = true

    val bufferSize = AudioTrack.getMinBufferSize(
      sampleRate,
      AudioFormat.CHANNEL_OUT_MONO,
      AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast( sampleRate / 10 )

    try {
      engineTrack = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(bufferSize)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

      engineTrack?.play()

      engineJob = audioScope.launch {
        val chunk = ShortArray(1024)
        var phase = 0.0
        var phase2 = 0.0

        while (isActive && isEngineRunning) {
          if (!isSoundEnabled) {
            kotlinx.coroutines.delay(100)
            continue
          }
          val ratio = currentEngineRpmRatio.coerceIn(0f, 1f)
          // Fundamental frequency: from 65Hz (idle) to 320Hz (revving)
          val freq = 65.0 + ratio * 255.0
          val subFreq = freq * 0.5

          for (i in chunk.indices) {
            phase += 2.0 * Math.PI * freq / sampleRate
            if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI

            phase2 += 2.0 * Math.PI * subFreq / sampleRate
            if (phase2 > 2.0 * Math.PI) phase2 -= 2.0 * Math.PI

            // Rich multi-harmonic motorcycle exhaust pulse
            val sample1 = sin(phase)
            val sample2 = sin(phase * 2.0) * 0.45
            val sample3 = sin(phase * 3.0) * 0.25
            val subSample = sin(phase2) * 0.35
            val grit = (Random.nextFloat() * 2f - 1f) * 0.08f

            val combined = (sample1 + sample2 + sample3 + subSample + grit) / 2.15
            val amplitude = (0.28 + ratio * 0.42) * Short.MAX_VALUE
            chunk[i] = (combined * amplitude).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
          }

          engineTrack?.write(chunk, 0, chunk.size)
        }
      }
    } catch (_: Exception) {
      // AudioTrack init guard
    }
  }

  fun updateEngineRpm(rpmRatio: Float) {
    currentEngineRpmRatio = rpmRatio
  }

  fun stopEngineSound() {
    isEngineRunning = false
    engineJob?.cancel()
    engineJob = null
    try {
      engineTrack?.stop()
      engineTrack?.release()
    } catch (_: Exception) {}
    engineTrack = null
  }

  fun playHorn() {
    if (!isSoundEnabled) return
    audioScope.launch {
      val durationMs = 300
      val numSamples = (sampleRate * durationMs / 1000)
      val buffer = ShortArray(numSamples)
      val freq1 = 440.0
      val freq2 = 554.0

      for (i in 0 until numSamples) {
        val t = i.toDouble() / sampleRate
        val env = (1.0 - (i.toDouble() / numSamples)).coerceIn(0.0, 1.0)
        val sample = (sin(2.0 * Math.PI * freq1 * t) * 0.5 + sin(2.0 * Math.PI * freq2 * t) * 0.5) * env
        buffer[i] = (sample * Short.MAX_VALUE * 0.7).toInt().toShort()
      }
      playOneShot(buffer)
    }
  }

  fun playNearMiss() {
    vibrateNearMiss()
    if (!isSoundEnabled) return
    audioScope.launch {
      val durationMs = 280
      val numSamples = (sampleRate * durationMs / 1000)
      val buffer = ShortArray(numSamples)

      for (i in 0 until numSamples) {
        val progress = i.toDouble() / numSamples
        // Doppler swoosh: starts at 750Hz and drops sharply to 250Hz
        val freq = 750.0 - progress * 500.0
        val t = i.toDouble() / sampleRate
        val env = sin(progress * Math.PI) // smooth fade in and out
        val noise = (Random.nextFloat() * 2f - 1f) * 0.25f
        val sample = (sin(2.0 * Math.PI * freq * t) * 0.75 + noise) * env
        buffer[i] = (sample * Short.MAX_VALUE * 0.65).toInt().toShort()
      }
      playOneShot(buffer)
    }
  }

  fun playCrash() {
    vibrateCrash()
    if (!isSoundEnabled) return
    audioScope.launch {
      val durationMs = 600
      val numSamples = (sampleRate * durationMs / 1000)
      val buffer = ShortArray(numSamples)

      for (i in 0 until numSamples) {
        val progress = i.toDouble() / numSamples
        val decay = kotlin.math.exp(-progress * 5.0)
        val noise = (Random.nextFloat() * 2f - 1f)
        val lowBoom = sin(2.0 * Math.PI * (80.0 - progress * 40.0) * (i.toDouble() / sampleRate)) * 0.5
        val sample = (noise * 0.65 + lowBoom) * decay
        buffer[i] = (sample * Short.MAX_VALUE * 0.85).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
      }
      playOneShot(buffer)
    }
  }

  fun playVictory() {
    if (!isSoundEnabled) return
    audioScope.launch {
      // Arpeggio C4, E4, G4, C5
      val notes = listOf(261.63, 329.63, 392.00, 523.25)
      val noteDurationMs = 150
      val totalSamples = (sampleRate * noteDurationMs * notes.size / 1000)
      val buffer = ShortArray(totalSamples)

      for ((index, freq) in notes.withIndex()) {
        val startSample = (index * sampleRate * noteDurationMs) / 1000
        val endSample = ((index + 1) * sampleRate * noteDurationMs) / 1000
        for (i in startSample until endSample) {
          val t = (i - startSample).toDouble() / sampleRate
          val progress = (i - startSample).toDouble() / (endSample - startSample)
          val env = (1.0 - progress * 0.3)
          val sample = (sin(2.0 * Math.PI * freq * t) + sin(4.0 * Math.PI * freq * t) * 0.3) * env
          buffer[i] = (sample * Short.MAX_VALUE * 0.6).toInt().toShort()
        }
      }
      playOneShot(buffer)
    }
  }

  private fun playOneShot(samples: ShortArray) {
    try {
      val track = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(samples.size * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

      track.write(samples, 0, samples.size)
      track.play()
    } catch (_: Exception) {}
  }

  fun vibrateNearMiss() {
    if (!isVibrationEnabled || vibrator == null || !vibrator.hasVibrator()) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(80)
      }
    } catch (_: Exception) {}
  }

  fun vibrateCrash() {
    if (!isVibrationEnabled || vibrator == null || !vibrator.hasVibrator()) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 150, 80, 250), intArrayOf(0, 255, 0, 200), -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(400)
      }
    } catch (_: Exception) {}
  }

  fun release() {
    stopEngineSound()
  }
}
