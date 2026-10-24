package com.example.game.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NitroAmber
import com.example.ui.theme.RacingRed

@Composable
fun HudOverlay(
  engine: GameEngine,
  onPauseClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val configuration = LocalConfiguration.current
  val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

  Box(modifier = modifier.fillMaxSize().padding(10.dp)) {
    // ---------------------------------------------
    // TOP STATS BAR
    // ---------------------------------------------
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopCenter),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top
    ) {
      // Top Left: Pause, Screen Rotation & Level
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        IconButton(
          onClick = onPauseClicked,
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xAA000000))
            .border(1.dp, Color(0x66FFFFFF), CircleShape)
            .testTag("pause_button")
        ) {
          Icon(
            imageVector = Icons.Default.Pause,
            contentDescription = "Pause Game",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        }

        // Screen Orientation Toggle (especially useful on PC & tablets)
        IconButton(
          onClick = {
            val act = context as? Activity
            act?.requestedOrientation = if (isLandscape) {
              ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
              ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
          },
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xAA000000))
            .border(1.dp, Color(0x66FFFFFF), CircleShape)
            .testTag("rotate_screen_button")
        ) {
          Icon(
            imageVector = Icons.Default.ScreenRotation,
            contentDescription = "Toggle Screen Orientation",
            tint = NeonCyan,
            modifier = Modifier.size(20.dp)
          )
        }

        Column(modifier = Modifier.padding(start = 2.dp)) {
          Text(
            text = engine.level.name.substringBefore(":"),
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            color = Color.White
          )
          Text(
            text = "${engine.nearMissCount} Near Misses",
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = Color(0xFFB0BEC5)
          )
        }
      }

      // Top Center: Distance Progress & Countdown Timer
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xCC0A0D14))
          .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(12.dp))
          .padding(horizontal = 12.dp, vertical = 5.dp)
      ) {
        val progress = (engine.distanceMeters / engine.level.targetDistanceMeters).coerceIn(0f, 1f)
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "${engine.distanceMeters.toInt()}m",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = NeonCyan
          )
          Text(
            text = " / ${engine.level.targetDistanceMeters.toInt()}m",
            fontSize = 10.sp,
            color = Color.Gray
          )
        }

        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .width(100.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp)),
          color = NeonCyan,
          trackColor = Color(0xFF263238),
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Countdown Timer in MM:SS format
        val totalSeconds = engine.timeRemaining.toInt().coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val timeColor = if (totalSeconds <= 10) RacingRed else NitroAmber

        Text(
          text = String.format("%02d:%02d", minutes, seconds),
          fontWeight = FontWeight.Black,
          fontSize = 16.sp,
          color = timeColor
        )
      }

      // Top Right: Score & Multiplier
      Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xCC0A0D14))
          .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(12.dp))
          .padding(horizontal = 10.dp, vertical = 5.dp)
      ) {
        Text(
          text = "${engine.score} PTS",
          fontWeight = FontWeight.Black,
          fontSize = 14.sp,
          color = Color.White
        )
        if (engine.comboMultiplier > 1) {
          Text(
            text = "${engine.comboMultiplier}x COMBO 🔥",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 10.sp,
            color = NitroAmber
          )
        }
      }
    }

    // ---------------------------------------------
    // PC KEYBOARD HELPER HINT BANNER
    // ---------------------------------------------
    Box(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 58.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(Color(0x88000000))
        .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
        .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
      Text(
        text = "⌨️ PC Keys: [W] Gas • [S] Brake • [A][D] Steer • [SPACE] Nitro • [H] Horn",
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFFB0BEC5)
      )
    }

    // ---------------------------------------------
    // FLOATING NEAR-MISS BANNER
    // ---------------------------------------------
    AnimatedVisibility(
      visible = engine.recentNearMissText != null,
      enter = scaleIn() + fadeIn(),
      exit = scaleOut() + fadeOut(),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 84.dp)
    ) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(
            Brush.horizontalGradient(
              listOf(Color(0xDDE53935), Color(0xDDFF8F00))
            )
          )
          .border(1.5.dp, Color.White, RoundedCornerShape(20.dp))
          .padding(horizontal = 18.dp, vertical = 7.dp)
      ) {
        Text(
          text = engine.recentNearMissText ?: "",
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = 14.sp
        )
      }
    }

    // ---------------------------------------------
    // BOTTOM CONTROLS (STEERING, BRAKE, GAS, NITRO, HORN)
    // ---------------------------------------------
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .padding(bottom = 12.dp, start = 4.dp, end = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      // LEFT GROUP: Steer Left, Steer Right, Horn
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
      ) {
        // Steer Left Button
        TouchHoldButton(
          onPressChange = { isPressed -> engine.isSteeringLeft = isPressed },
          testTag = "steer_left_button",
          modifier = Modifier.size(62.dp)
        ) { isPressed ->
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
              .background(if (isPressed) Color(0xEE00E5FF) else Color(0x88141824))
              .border(2.dp, if (isPressed) Color.White else NeonCyan, CircleShape)
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Steer Left",
                tint = if (isPressed) Color.Black else Color.White,
                modifier = Modifier.size(30.dp)
              )
              Text(
                text = "[A]",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = if (isPressed) Color.Black else NeonCyan
              )
            }
          }
        }

        // Steer Right Button
        TouchHoldButton(
          onPressChange = { isPressed -> engine.isSteeringRight = isPressed },
          testTag = "steer_right_button",
          modifier = Modifier.size(62.dp)
        ) { isPressed ->
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
              .background(if (isPressed) Color(0xEE00E5FF) else Color(0x88141824))
              .border(2.dp, if (isPressed) Color.White else NeonCyan, CircleShape)
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Steer Right",
                tint = if (isPressed) Color.Black else Color.White,
                modifier = Modifier.size(30.dp)
              )
              Text(
                text = "[D]",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = if (isPressed) Color.Black else NeonCyan
              )
            }
          }
        }

        // Horn Button
        TouchHoldButton(
          onPressChange = { isPressed -> engine.isHornActive = isPressed },
          testTag = "horn_button",
          modifier = Modifier.size(48.dp)
        ) { isPressed ->
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
              .background(if (isPressed) Color(0xEEFFD600) else Color(0x88141824))
              .border(1.5.dp, NitroAmber, CircleShape)
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Horn",
                tint = if (isPressed) Color.Black else NitroAmber,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = "[H]",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPressed) Color.Black else Color.White
              )
            }
          }
        }
      }

      // RIGHT GROUP: Nitro, Brake, Gas
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
      ) {
        // Nitro Boost Button
        TouchHoldButton(
          onPressChange = { isPressed ->
            if (isPressed) engine.toggleNitro()
          },
          testTag = "nitro_button",
          modifier = Modifier.size(52.dp)
        ) { isPressed ->
          val hasFuel = engine.nitroFuel > 15f
          val nitroColor = if (engine.isNitroActive) NeonCyan else NitroAmber
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
              .background(if (engine.isNitroActive) Color(0xEE00E5FF) else Color(0x88141824))
              .border(2.dp, if (hasFuel) nitroColor else Color.Gray, CircleShape)
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = "Nitro Boost",
                tint = if (engine.isNitroActive) Color.Black else nitroColor,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = "${engine.nitroFuel.toInt()}%",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (engine.isNitroActive) Color.Black else Color.White
              )
              Text(
                text = "[SPACE]",
                fontSize = 7.sp,
                fontWeight = FontWeight.Black,
                color = if (engine.isNitroActive) Color.Black else nitroColor
              )
            }
          }
        }

        // Brake Button
        TouchHoldButton(
          onPressChange = { isPressed -> engine.isBrakeActive = isPressed },
          testTag = "brake_button",
          modifier = Modifier.size(62.dp)
        ) { isPressed ->
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
              .background(if (isPressed) Color(0xDDE53935) else Color(0x882A0F12))
              .border(2.dp, RacingRed, CircleShape)
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "BRAKE",
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = Color.White
              )
              Text(
                text = "[S]",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF8A80)
              )
            }
          }
        }

        // Throttle (GAS) Button
        TouchHoldButton(
          onPressChange = { isPressed -> engine.isThrottleActive = isPressed },
          testTag = "gas_button",
          modifier = Modifier.size(72.dp)
        ) { isPressed ->
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
              .background(if (isPressed) Color(0xEE00E676) else Color(0x880C2915))
              .border(2.5.dp, if (isPressed) Color.White else Color(0xFF00E676), CircleShape)
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = "Throttle Gas",
                tint = if (isPressed) Color.Black else Color(0xFF00E676),
                modifier = Modifier.size(26.dp)
              )
              Text(
                text = "GAS",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = if (isPressed) Color.Black else Color.White
              )
              Text(
                text = "[W]",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPressed) Color.Black else Color(0xFFB9F6CA)
              )
            }
          }
        }
      }
    }
  }
}

/**
 * High-performance pointer tracking that works seamlessly on Touch, Mouse, Trackpad, and Stylus!
 * Does NOT suffer from drag slop cancellation that breaks mouse clicks on PC emulators.
 */
@Composable
fun TouchHoldButton(
  onPressChange: (Boolean) -> Unit,
  testTag: String,
  modifier: Modifier = Modifier,
  content: @Composable (isPressed: Boolean) -> Unit
) {
  val isPressed = remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .testTag(testTag)
      .pointerInput(Unit) {
        awaitPointerEventScope {
          while (true) {
            val event = awaitPointerEvent(PointerEventPass.Main)
            val anyPressed = event.changes.any { it.pressed }
            val isInside = event.changes.any { change ->
              val pos = change.position
              pos.x >= 0f && pos.x <= size.width && pos.y >= 0f && pos.y <= size.height
            }
            val active = anyPressed && isInside

            if (active != isPressed.value) {
              isPressed.value = active
              onPressChange(active)
            }

            if (!anyPressed && isPressed.value) {
              isPressed.value = false
              onPressChange(false)
            }
          }
        }
      }
  ) {
    content(isPressed.value)
  }
}
