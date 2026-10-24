package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.audio.SoundManager
import com.example.game.data.GamePreferences
import com.example.game.engine.GameEngine
import com.example.game.model.GameDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SMK", appName)
  }

  @Test
  fun `verify game engine initialization and physics`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val preferences = GamePreferences(context)
    val soundManager = SoundManager(context)
    val level = GameDefaults.LEVELS.first()
    val bike = GameDefaults.DEFAULT_BIKES.first()

    val engine = GameEngine(level, bike, soundManager, preferences)

    // Initial state checks
    assertEquals(95f, engine.speedKmh, 0.01f)
    assertEquals(0f, engine.distanceMeters, 0.01f)
    assertFalse(engine.isCrashed)
    assertFalse(engine.isVictory)

    // Throttle acceleration test
    engine.isThrottleActive = true
    engine.update(0.5f)
    assertTrue("Bike should accelerate when throttle is active", engine.speedKmh > 0f)

    // Steering test
    val initialX = engine.playerX
    engine.isSteeringLeft = true
    engine.update(0.5f)
    assertTrue("Bike playerX should move left when steering left", engine.playerX < initialX)

    engine.destroy()
  }

  @Test
  fun `verify game preferences unlock and upgrade logic`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val preferences = GamePreferences(context)

    assertTrue("Level 1 should be unlocked by default", preferences.isLevelUnlocked(1))

    preferences.unlockLevel(2)
    assertTrue("Level 2 should be unlocked after calling unlockLevel", preferences.isLevelUnlocked(2))

    preferences.saveLevelStars(1, 3)
    assertEquals(3, preferences.getLevelStars(1))

    val currentTopSpeed = preferences.getUpgradeLevel("smk_pulsar_250", "topSpeed")
    preferences.incrementUpgrade("smk_pulsar_250", "topSpeed")
    assertEquals(currentTopSpeed + 1, preferences.getUpgradeLevel("smk_pulsar_250", "topSpeed"))
  }

  @Test
  fun `verify PC keyboard input bridge acceleration`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val preferences = GamePreferences(context)
    val soundManager = SoundManager(context)
    val level = GameDefaults.LEVELS.first()
    val bike = GameDefaults.DEFAULT_BIKES.first()

    val engine = GameEngine(level, bike, soundManager, preferences)

    com.example.game.input.GameInputBridge.isThrottlePressed = true
    engine.update(0.5f)
    assertTrue("Bike should accelerate with keyboard throttle", engine.speedKmh > 0f)

    com.example.game.input.GameInputBridge.reset()
    engine.destroy()
  }
}
