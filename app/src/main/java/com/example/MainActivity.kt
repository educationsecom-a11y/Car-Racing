package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.game.audio.SoundManager
import com.example.game.data.GamePreferences
import com.example.game.model.GameDefaults
import com.example.game.model.GameLevel
import com.example.game.ui.GamePlayScreen
import com.example.game.ui.GarageScreen
import com.example.game.ui.LevelSelectScreen
import com.example.game.ui.MainMenuScreen
import com.example.ui.theme.MyApplicationTheme

enum class Screen {
  MAIN_MENU,
  LEVEL_SELECT,
  GARAGE,
  GAME_PLAY
}

class MainActivity : ComponentActivity() {
  private lateinit var soundManager: SoundManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    soundManager = SoundManager(this)

    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          SmkApp(
            soundManager = soundManager,
            modifier = Modifier.fillMaxSize()
          )
        }
      }
    }
  }

  override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
    if (com.example.game.input.GameInputBridge.handleKeyEvent(event)) {
      return true
    }
    return super.dispatchKeyEvent(event)
  }

  override fun onPause() {
    super.onPause()
    soundManager.stopEngineSound()
    com.example.game.input.GameInputBridge.reset()
  }

  override fun onDestroy() {
    super.onDestroy()
    soundManager.release()
    com.example.game.input.GameInputBridge.reset()
  }
}

@Composable
fun SmkApp(
  soundManager: SoundManager,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val preferences = remember { GamePreferences(context) }

  // Sync settings
  soundManager.isSoundEnabled = preferences.isSoundEnabled
  soundManager.isVibrationEnabled = preferences.isVibrationEnabled

  // Launch straight into the active highway race so the game plays immediately!
  var currentScreen by remember { mutableStateOf(Screen.GAME_PLAY) }
  var currentLevel by remember { mutableStateOf(GameDefaults.LEVELS.first()) }

  // BackHandler for sub-screens
  BackHandler(enabled = currentScreen != Screen.MAIN_MENU) {
    when (currentScreen) {
      Screen.LEVEL_SELECT, Screen.GARAGE -> currentScreen = Screen.MAIN_MENU
      Screen.GAME_PLAY -> currentScreen = Screen.LEVEL_SELECT
      Screen.MAIN_MENU -> { /* default exit */ }
    }
  }

  AnimatedContent(
    targetState = currentScreen,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
    label = "ScreenTransition",
    modifier = modifier
  ) { screen ->
    when (screen) {
      Screen.MAIN_MENU -> {
        MainMenuScreen(
          preferences = preferences,
          onPlayClick = {
            // Find highest unlocked level
            val highestUnlocked = GameDefaults.LEVELS.lastOrNull { preferences.isLevelUnlocked(it.id) }
              ?: GameDefaults.LEVELS.first()
            currentLevel = highestUnlocked
            currentScreen = Screen.GAME_PLAY
          },
          onLevelSelectClick = {
            currentScreen = Screen.LEVEL_SELECT
          },
          onGarageClick = {
            currentScreen = Screen.GARAGE
          }
        )
      }

      Screen.LEVEL_SELECT -> {
        LevelSelectScreen(
          preferences = preferences,
          onLevelSelected = { level ->
            currentLevel = level
            currentScreen = Screen.GAME_PLAY
          },
          onBack = {
            currentScreen = Screen.MAIN_MENU
          }
        )
      }

      Screen.GARAGE -> {
        GarageScreen(
          preferences = preferences,
          onBack = {
            currentScreen = Screen.MAIN_MENU
          }
        )
      }

      Screen.GAME_PLAY -> {
        GamePlayScreen(
          level = currentLevel,
          preferences = preferences,
          soundManager = soundManager,
          onNavigateToLevelSelect = {
            currentScreen = Screen.LEVEL_SELECT
          },
          onNavigateToGarage = {
            currentScreen = Screen.GARAGE
          },
          onNextLevel = { nextLvl ->
            currentLevel = nextLvl
          }
        )
      }
    }
  }
}
