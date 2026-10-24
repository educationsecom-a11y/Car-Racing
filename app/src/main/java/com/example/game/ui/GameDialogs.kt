package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.engine.GameEngine
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NitroAmber
import com.example.ui.theme.RacingRed

@Composable
fun VictoryDialog(
  engine: GameEngine,
  onNextLevel: () -> Unit,
  onReplay: () -> Unit,
  onLevelSelect: () -> Unit
) {
  Dialog(onDismissRequest = {}) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      border = androidx.compose.foundation.BorderStroke(2.dp, NitroAmber),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("victory_dialog")
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Banner header
        Text(
          text = "VICTORY!",
          fontWeight = FontWeight.Black,
          fontSize = 28.sp,
          color = NitroAmber,
          letterSpacing = 2.sp
        )
        Text(
          text = "LEVEL COMPLETED",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = Color.White
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3 Stars Display
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          for (i in 1..3) {
            val isEarned = i <= engine.starsEarned
            Icon(
              imageVector = if (isEarned) Icons.Default.Star else Icons.Default.StarBorder,
              contentDescription = "Star $i",
              tint = if (isEarned) NitroAmber else Color(0xFF455A64),
              modifier = Modifier.size(36.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats card
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          StatRow("Distance", "${engine.distanceMeters.toInt()}m / ${engine.level.targetDistanceMeters.toInt()}m")
          StatRow("Time Remaining", "${engine.timeRemaining.toInt()}s")
          StatRow("Near Misses", "${engine.nearMissCount}")
          StatRow("Score", "${engine.score} PTS", highlightColor = NeonCyan)
          StatRow("Reward", "+${engine.earnedCoins} Coins", highlightColor = NitroAmber)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action buttons
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          if (engine.level.id < 4) {
            Button(
              onClick = onNextLevel,
              colors = ButtonDefaults.buttonColors(containerColor = NitroAmber),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("next_level_button")
            ) {
              Text("NEXT LEVEL", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 15.sp)
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = onReplay,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("replay_button")
            ) {
              Icon(Icons.Default.Refresh, contentDescription = "Replay", tint = Color.White, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("REPLAY", color = Color.White, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = onLevelSelect,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("level_select_button")
            ) {
              Text("LEVELS", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
fun GameOverDialog(
  engine: GameEngine,
  onRetry: () -> Unit,
  onGarage: () -> Unit,
  onLevelSelect: () -> Unit
) {
  Dialog(onDismissRequest = {}) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      border = androidx.compose.foundation.BorderStroke(2.dp, RacingRed),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("game_over_dialog")
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Crash header
        Text(
          text = "CRASHED!",
          fontWeight = FontWeight.Black,
          fontSize = 28.sp,
          color = RacingRed,
          letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = engine.crashReason.ifEmpty { "Motorcycle crash" },
          fontWeight = FontWeight.Medium,
          fontSize = 13.sp,
          color = Color(0xFFCFD8DC),
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Stats
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val percent = ((engine.distanceMeters / engine.level.targetDistanceMeters) * 100).toInt().coerceIn(0, 100)
          StatRow("Distance Completed", "$percent% (${engine.distanceMeters.toInt()}m)")
          StatRow("Top Speed", "${engine.speedKmh.toInt()} KM/H")
          StatRow("Near Misses", "${engine.nearMissCount}")
          StatRow("Score", "${engine.score} PTS", highlightColor = NeonCyan)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action buttons
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("retry_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("TRY AGAIN", fontWeight = FontWeight.Black, color = Color.White, fontSize = 15.sp)
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = onGarage,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("garage_button")
            ) {
              Text("UPGRADE BIKE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            OutlinedButton(
              onClick = onLevelSelect,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("levels_button")
            ) {
              Text("LEVELS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
fun PauseDialog(
  onResume: () -> Unit,
  onRestart: () -> Unit,
  onMainMenu: () -> Unit,
  isSoundEnabled: Boolean,
  onToggleSound: (Boolean) -> Unit,
  isVibrationEnabled: Boolean,
  onToggleVibration: (Boolean) -> Unit
) {
  Dialog(onDismissRequest = onResume) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      border = androidx.compose.foundation.BorderStroke(1.5.dp, DarkBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("pause_dialog")
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "PAUSED",
          fontWeight = FontWeight.Black,
          fontSize = 24.sp,
          color = Color.White,
          letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Toggles
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                contentDescription = "Sound",
                tint = NeonCyan
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Sound Effects", color = Color.White, fontWeight = FontWeight.Medium)
            }
            Switch(
              checked = isSoundEnabled,
              onCheckedChange = onToggleSound,
              colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Vibration,
                contentDescription = "Vibration",
                tint = NitroAmber
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Haptic Feedback", color = Color.White, fontWeight = FontWeight.Medium)
            }
            Switch(
              checked = isVibrationEnabled,
              onCheckedChange = onToggleVibration,
              colors = SwitchDefaults.colors(checkedThumbColor = NitroAmber)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Actions
        Button(
          onClick = onResume,
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("resume_button")
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = Color.Black)
          Spacer(modifier = Modifier.width(8.dp))
          Text("RESUME", fontWeight = FontWeight.Black, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onRestart,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f).height(46.dp)
          ) {
            Text("RESTART", color = Color.White, fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = onMainMenu,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f).height(46.dp)
          ) {
            Text("MENU", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun StatRow(label: String, value: String, highlightColor: Color = Color.White) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, fontSize = 13.sp, color = Color(0xFF90A4AE))
    Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = highlightColor)
  }
}
