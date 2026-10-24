package com.example.game.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import com.example.game.audio.SoundManager
import com.example.game.data.GamePreferences
import com.example.game.engine.GameEngine
import com.example.game.model.GameDefaults
import com.example.game.model.GameLevel

@Composable
fun GamePlayScreen(
  level: GameLevel,
  preferences: GamePreferences,
  soundManager: SoundManager,
  onNavigateToLevelSelect: () -> Unit,
  onNavigateToGarage: () -> Unit,
  onNextLevel: (GameLevel) -> Unit,
  modifier: Modifier = Modifier
) {
  val selectedBike = remember(preferences.selectedBikeId) {
    GameDefaults.DEFAULT_BIKES.find { it.id == preferences.selectedBikeId }
      ?: GameDefaults.DEFAULT_BIKES.first()
  }

  val selectedColor = remember(preferences.selectedColorId) {
    GameDefaults.BIKE_COLORS.find { it.id == preferences.selectedColorId }
      ?: GameDefaults.BIKE_COLORS.first()
  }

  val engine = remember(level, selectedBike) {
    GameEngine(
      level = level,
      bike = selectedBike,
      soundManager = soundManager,
      gamePreferences = preferences
    )
  }

  var isPaused by remember { mutableStateOf(false) }

  // Game update loop
  LaunchedEffect(isPaused, engine.isCrashed, engine.isVictory) {
    var lastFrameTimeNanos = 0L
    while (!isPaused && !engine.isCrashed && !engine.isVictory) {
      withFrameNanos { frameTimeNanos ->
        if (lastFrameTimeNanos != 0L) {
          val dt = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
          engine.update(dt)
        }
        lastFrameTimeNanos = frameTimeNanos
      }
    }
  }

  // Handle hardware back button
  BackHandler {
    if (!isPaused && !engine.isCrashed && !engine.isVictory) {
      isPaused = true
      soundManager.stopEngineSound()
    } else {
      onNavigateToLevelSelect()
    }
  }

  DisposableEffect(engine) {
    com.example.game.input.GameInputBridge.onPauseToggle = {
      isPaused = !isPaused
      if (isPaused) {
        soundManager.stopEngineSound()
      } else {
        soundManager.startEngineSound()
      }
    }
    com.example.game.input.GameInputBridge.onRestart = {
      isPaused = false
      engine.restart()
    }
    onDispose {
      com.example.game.input.GameInputBridge.onPauseToggle = null
      com.example.game.input.GameInputBridge.onRestart = null
      com.example.game.input.GameInputBridge.reset()
      engine.destroy()
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    // 1. Perspective Highway & Cockpit Renderer
    CockpitRenderer(
      engine = engine,
      bikeColor = selectedColor,
      modifier = Modifier.fillMaxSize()
    )

    // 2. HUD Controls & Overlay
    HudOverlay(
      engine = engine,
      onPauseClicked = {
        isPaused = true
        soundManager.stopEngineSound()
      },
      modifier = Modifier.fillMaxSize()
    )

    // 3. Victory Dialog
    if (engine.isVictory) {
      VictoryDialog(
        engine = engine,
        onNextLevel = {
          val nextLevel = GameDefaults.LEVELS.find { it.id == level.id + 1 }
          if (nextLevel != null) {
            onNextLevel(nextLevel)
          } else {
            onNavigateToLevelSelect()
          }
        },
        onReplay = {
          engine.restart()
        },
        onLevelSelect = onNavigateToLevelSelect
      )
    }

    // 4. Game Over Dialog (Crash or Time out)
    if (engine.isCrashed) {
      GameOverDialog(
        engine = engine,
        onRetry = {
          engine.restart()
        },
        onGarage = onNavigateToGarage,
        onLevelSelect = onNavigateToLevelSelect
      )
    }

    // 5. Pause Dialog
    if (isPaused) {
      PauseDialog(
        onResume = {
          isPaused = false
          soundManager.startEngineSound()
        },
        onRestart = {
          isPaused = false
          engine.restart()
        },
        onMainMenu = onNavigateToLevelSelect,
        isSoundEnabled = preferences.isSoundEnabled,
        onToggleSound = { enabled ->
          preferences.isSoundEnabled = enabled
          soundManager.isSoundEnabled = enabled
        },
        isVibrationEnabled = preferences.isVibrationEnabled,
        onToggleVibration = { enabled ->
          preferences.isVibrationEnabled = enabled
          soundManager.isVibrationEnabled = enabled
        }
      )
    }
  }
}
