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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.game.data.GamePreferences
import com.example.game.model.GameDefaults
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NitroAmber
import com.example.ui.theme.RacingRed

@Composable
fun MainMenuScreen(
  preferences: GamePreferences,
  onPlayClick: () -> Unit,
  onLevelSelectClick: () -> Unit,
  onGarageClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showHowToPlay by remember { mutableStateOf(false) }

  val selectedBike = GameDefaults.DEFAULT_BIKES.find { it.id == preferences.selectedBikeId }
    ?: GameDefaults.DEFAULT_BIKES.first()
  val selectedColor = GameDefaults.BIKE_COLORS.find { it.id == preferences.selectedColorId }
    ?: GameDefaults.BIKE_COLORS.first()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            DarkCarbon,
            DarkSurface,
            Color(0xFF1A1118),
            DarkCarbon
          )
        )
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top status badges (Coins & Stars)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Coins
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.MonetizationOn,
            contentDescription = "Coins",
            tint = NitroAmber,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${preferences.coins}",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 14.sp
          )
        }

        // Stars
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Total Stars",
            tint = NitroAmber,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${preferences.getTotalStars()} / 12",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 14.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // LOGO & TITLE
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Glowing brand badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(RacingRed)
            .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
          Text(
            text = "TRAFFIC RIDER",
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            color = Color.White,
            letterSpacing = 3.sp
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "SMK",
          fontWeight = FontWeight.Black,
          fontSize = 72.sp,
          letterSpacing = 4.sp,
          color = Color.White
        )

        Text(
          text = "FIRST-PERSON HIGHWAY RACER",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          letterSpacing = 2.sp,
          color = NeonCyan
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Current Selected Bike Card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, DarkBorder),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("bike_preview_card")
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = selectedBike.name,
              fontWeight = FontWeight.Black,
              fontSize = 18.sp,
              color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Top Speed: ${selectedBike.maxSpeedKmh.toInt()} KM/H",
              fontSize = 13.sp,
              color = NeonCyan,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "Color: ${selectedColor.name}",
              fontSize = 12.sp,
              color = Color.Gray
            )
          }

          // Visual bike color orb
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(selectedColor.primaryColor)
              .border(3.dp, Color.White, CircleShape)
          )
        }
      }

      Spacer(modifier = Modifier.height(30.dp))

      // MAIN ACTION BUTTONS
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Big "RIDE NOW" Primary Button
        Button(
          onClick = onPlayClick,
          colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .testTag("main_ride_button")
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Ride Now",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "RIDE NOW",
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            letterSpacing = 1.sp,
            color = Color.White
          )
        }

        // Secondary Row: Level Select & Garage
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Button(
            onClick = onLevelSelectClick,
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .height(52.dp)
              .testTag("levels_menu_button")
          ) {
            Icon(
              imageVector = Icons.Default.Map,
              contentDescription = "Levels",
              tint = NeonCyan,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "LEVELS",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color.White
            )
          }

          Button(
            onClick = onGarageClick,
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, NitroAmber),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .height(52.dp)
              .testTag("garage_menu_button")
          ) {
            Icon(
              imageVector = Icons.Default.Build,
              contentDescription = "Garage",
              tint = NitroAmber,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "GARAGE",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color.White
            )
          }
        }

        // How to play button
        OutlinedButton(
          onClick = { showHowToPlay = true },
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("how_to_play_button")
        ) {
          Icon(
            imageVector = Icons.Default.HelpOutline,
            contentDescription = "Instructions",
            tint = Color(0xFFB0BEC5),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "HOW TO PLAY & CONTROLS",
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = Color(0xFFB0BEC5)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
    }

    // How To Play Dialog
    if (showHowToPlay) {
      Dialog(onDismissRequest = { showHowToPlay = false }) {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, DarkBorder),
          modifier = Modifier.padding(16.dp)
        ) {
          Column(
            modifier = Modifier
              .padding(20.dp)
              .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "HOW TO RIDE",
              fontWeight = FontWeight.Black,
              fontSize = 20.sp,
              color = NeonCyan
            )

            Spacer(modifier = Modifier.height(14.dp))

            InstructionItem("GAS (Bottom-Right)", "Hold the green lever to rev the engine and accelerate.")
            InstructionItem("BRAKE (Bottom-Right)", "Hold the red pedal to decelerate quickly before collisions.")
            InstructionItem("STEERING (Bottom-Left)", "Hold Left / Right arrows to carve smoothly between lanes.")
            InstructionItem("NITRO (⚡)", "Unleash supercharged speed when you have nitro fuel!")
            InstructionItem("HORN (📢)", "Honk at traffic ahead to urge cars into changing lanes.")
            InstructionItem("🔥 NEAR MISS BONUS", "Pass cars at > 100 KM/H closely to earn +100 PTS and +2s extra time!")

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = { showHowToPlay = false },
              colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("GOT IT! LET'S RIDE", fontWeight = FontWeight.Bold, color = Color.White)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun InstructionItem(title: String, desc: String) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp)
  ) {
    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NitroAmber)
    Text(text = desc, fontSize = 12.sp, color = Color(0xFFCFD8DC))
  }
}
