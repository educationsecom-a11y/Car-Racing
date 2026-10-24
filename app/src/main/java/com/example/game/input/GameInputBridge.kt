package com.example.game.input

import android.view.KeyEvent

object GameInputBridge {
  @Volatile var isSteerLeftPressed = false
  @Volatile var isSteerRightPressed = false
  @Volatile var isThrottlePressed = false
  @Volatile var isBrakePressed = false
  @Volatile var isNitroPressed = false
  @Volatile var isHornPressed = false

  var onPauseToggle: (() -> Unit)? = null
  var onRestart: (() -> Unit)? = null

  fun handleKeyEvent(event: KeyEvent): Boolean {
    val isDown = event.action == KeyEvent.ACTION_DOWN
    val isUp = event.action == KeyEvent.ACTION_UP

    when (event.keyCode) {
      // Throttle (Gas)
      KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_BUTTON_R2 -> {
        if (isDown) isThrottlePressed = true
        if (isUp) isThrottlePressed = false
        return true
      }
      // Brake
      KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_BUTTON_L2 -> {
        if (isDown) isBrakePressed = true
        if (isUp) isBrakePressed = false
        return true
      }
      // Steer Left
      KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_DPAD_LEFT -> {
        if (isDown) isSteerLeftPressed = true
        if (isUp) isSteerLeftPressed = false
        return true
      }
      // Steer Right
      KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_DPAD_RIGHT -> {
        if (isDown) isSteerRightPressed = true
        if (isUp) isSteerRightPressed = false
        return true
      }
      // Nitro Boost
      KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT, KeyEvent.KEYCODE_BUTTON_A -> {
        if (isDown) isNitroPressed = true
        if (isUp) isNitroPressed = false
        return true
      }
      // Horn
      KeyEvent.KEYCODE_H, KeyEvent.KEYCODE_E, KeyEvent.KEYCODE_BUTTON_X -> {
        if (isDown) isHornPressed = true
        if (isUp) isHornPressed = false
        return true
      }
      // Pause
      KeyEvent.KEYCODE_P, KeyEvent.KEYCODE_ESCAPE -> {
        if (isDown) onPauseToggle?.invoke()
        return true
      }
      // Restart
      KeyEvent.KEYCODE_R -> {
        if (isDown) onRestart?.invoke()
        return true
      }
    }
    return false
  }

  fun reset() {
    isSteerLeftPressed = false
    isSteerRightPressed = false
    isThrottlePressed = false
    isBrakePressed = false
    isNitroPressed = false
    isHornPressed = false
  }
}
