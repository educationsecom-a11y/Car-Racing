package com.example.game.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.GamePreferences
import com.example.game.model.GameDefaults
import com.example.game.model.GameLevel
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NitroAmber
import com.example.ui.theme.RacingRed

@Composable
fun LevelSelectScreen(
  preferences: GamePreferences,
  onLevelSelected: (GameLevel) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkCarbon)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      // Top Bar with Back Button & Title
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onBack,
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(DarkSurfaceElevated)
              .testTag("level_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to Main Menu",
              tint = Color.White
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = "SELECT LEVEL",
              fontWeight = FontWeight.Black,
              fontSize = 20.sp,
              color = Color.White
            )
            Text(
              text = "Prove your highway mastery",
              fontSize = 12.sp,
              color = Color(0xFF90A4AE)
            )
          }
        }

        // Stars & Coins summary
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(DarkSurfaceElevated)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(Icons.Default.Star, contentDescription = "Stars", tint = NitroAmber, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${preferences.getTotalStars()} ★",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = Color.White
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Levels list
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth().weight(1f)
      ) {
        items(GameDefaults.LEVELS) { level ->
          val isUnlocked = preferences.isLevelUnlocked(level.id)
          val stars = preferences.getLevelStars(level.id)
          val highScore = preferences.getLevelHighScore(level.id)

          LevelCard(
            level = level,
            isUnlocked = isUnlocked,
            starsEarned = stars,
            highScore = highScore,
            onSelect = {
              if (isUnlocked) onLevelSelected(level)
            }
          )
        }
      }
    }
  }
}

@Composable
private fun LevelCard(
  level: GameLevel,
  isUnlocked: Boolean,
  starsEarned: Int,
  highScore: Int,
  onSelect: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isUnlocked) DarkSurface else Color(0xFF101218)
    ),
    border = androidx.compose.foundation.BorderStroke(
      1.5.dp,
      if (isUnlocked) DarkBorder else Color(0xFF1E2129)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("level_card_${level.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = level.name,
            fontWeight = FontWeight.Black,
            fontSize = 17.sp,
            color = if (isUnlocked) Color.White else Color.Gray
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = level.subtitle,
            fontSize = 12.sp,
            color = if (isUnlocked) Color(0xFFB0BEC5) else Color(0xFF546E7A)
          )
        }

        if (isUnlocked) {
          // Stars indicator
          Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (i in 1..3) {
              val earned = i <= starsEarned
              Icon(
                imageVector = if (earned) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = if (earned) NitroAmber else Color(0xFF455A64),
                modifier = Modifier.size(20.dp)
              )
            }
          }
        } else {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked",
            tint = Color(0xFF78909C),
            modifier = Modifier.size(24.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Specifications chip row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        LevelBadge("Goal: ${level.targetDistanceMeters.toInt()}m", NeonCyan)
        LevelBadge("Time: ${level.timeLimitSeconds.toInt()}s", NitroAmber)
        if (level.isTwoWay) {
          LevelBadge("2-Way Traffic", RacingRed)
        } else {
          LevelBadge("One-Way", Color(0xFF00E676))
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action row: High score & Race button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (isUnlocked && highScore > 0) {
          Text(
            text = "Best: $highScore PTS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
          )
        } else if (!isUnlocked) {
          Text(
            text = "Requires ${level.requiredStarsToUnlock} ★ to Unlock",
            fontSize = 12.sp,
            color = Color(0xFFFF7043)
          )
        } else {
          Text(
            text = "Reward: +${level.rewardCoins} Coins",
            fontSize = 12.sp,
            color = NitroAmber
          )
        }

        Button(
          onClick = onSelect,
          enabled = isUnlocked,
          colors = ButtonDefaults.buttonColors(
            containerColor = RacingRed,
            disabledContainerColor = Color(0xFF1E222A)
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.height(40.dp)
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = if (isUnlocked) Color.White else Color.Gray,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isUnlocked) "RACE" else "LOCKED",
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            color = if (isUnlocked) Color.White else Color.Gray
          )
        }
      }
    }
  }
}

@Composable
private fun LevelBadge(text: String, accentColor: Color) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(DarkSurfaceElevated)
      .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 3.dp)
  ) {
    Text(
      text = text,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      color = accentColor
    )
  }
}
