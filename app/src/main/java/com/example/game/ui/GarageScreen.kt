package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.GamePreferences
import com.example.game.model.BikeModel
import com.example.game.model.GameDefaults
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NitroAmber
import com.example.ui.theme.RacingRed

@Composable
fun GarageScreen(
  preferences: GamePreferences,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedBikeId by remember { mutableStateOf(preferences.selectedBikeId) }
  var selectedColorId by remember { mutableStateOf(preferences.selectedColorId) }
  var currentCoins by remember { mutableStateOf(preferences.coins) }

  val bike = GameDefaults.DEFAULT_BIKES.find { it.id == selectedBikeId }
    ?: GameDefaults.DEFAULT_BIKES.first()
  val isBikeOwned = preferences.isBikeUnlocked(bike.id)
  val isEquipped = preferences.selectedBikeId == bike.id

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkCarbon)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
      // Top Bar: Back & Coins
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
              .testTag("garage_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = "GARAGE",
              fontWeight = FontWeight.Black,
              fontSize = 20.sp,
              color = Color.White
            )
            Text(
              text = "Customize and tune your ride",
              fontSize = 12.sp,
              color = Color(0xFF90A4AE)
            )
          }
        }

        // Coins balance + test add button
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.MonetizationOn,
            contentDescription = "Coins",
            tint = NitroAmber,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "$currentCoins",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 13.sp
          )
          IconButton(
            onClick = {
              preferences.coins += 1000
              currentCoins = preferences.coins
            },
            modifier = Modifier.size(28.dp).testTag("add_test_coins_button")
          ) {
            Icon(Icons.Default.Add, contentDescription = "Add Coins", tint = NeonCyan, modifier = Modifier.size(16.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Bike Selection Tabs
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        GameDefaults.DEFAULT_BIKES.forEach { b ->
          val isSelected = b.id == selectedBikeId
          val isOwned = preferences.isBikeUnlocked(b.id)

          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) RacingRed else DarkSurfaceElevated
            ),
            modifier = Modifier
              .weight(1f)
              .clickable { selectedBikeId = b.id }
              .testTag("bike_tab_${b.id}")
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = b.name.substringAfter(" "),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = Color.White
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = if (isOwned) "Owned" else "${b.priceCoins} ¢",
                fontSize = 11.sp,
                color = if (isSelected) Color.White else if (isOwned) NeonCyan else NitroAmber
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Main Bike Showcase Card
      val activeColor = GameDefaults.BIKE_COLORS.find { it.id == selectedColorId }
        ?: GameDefaults.BIKE_COLORS.first()

      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = bike.name,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = Color.White
              )
              Text(
                text = bike.tagLine,
                fontSize = 12.sp,
                color = Color(0xFFB0BEC5)
              )
            }

            // Big color badge
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(activeColor.primaryColor)
                .border(2.5.dp, Color.White, CircleShape)
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Color Customizer Palette
          Text(
            text = "CUSTOM PAINT COLOR",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color.Gray,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            GameDefaults.BIKE_COLORS.forEach { colorOpt ->
              val isColorActive = colorOpt.id == selectedColorId
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(colorOpt.primaryColor)
                  .border(
                    width = if (isColorActive) 3.dp else 1.dp,
                    color = if (isColorActive) Color.White else Color(0x55FFFFFF),
                    shape = CircleShape
                  )
                  .clickable {
                    selectedColorId = colorOpt.id
                    preferences.selectedColorId = colorOpt.id
                  }
                  .testTag("color_${colorOpt.id}"),
                contentAlignment = Alignment.Center
              ) {
                if (isColorActive) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Equip or Buy Button
          if (isBikeOwned) {
            Button(
              onClick = {
                preferences.selectedBikeId = bike.id
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isEquipped) Color(0xFF2E7D32) else NeonCyan
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("equip_bike_button")
            ) {
              Text(
                text = if (isEquipped) "EQUIPPED" else "EQUIP BIKE",
                fontWeight = FontWeight.Black,
                color = if (isEquipped) Color.White else Color.Black
              )
            }
          } else {
            val canAfford = currentCoins >= bike.priceCoins
            Button(
              onClick = {
                if (canAfford) {
                  preferences.coins -= bike.priceCoins
                  currentCoins = preferences.coins
                  preferences.unlockBike(bike.id)
                  preferences.selectedBikeId = bike.id
                }
              },
              enabled = canAfford,
              colors = ButtonDefaults.buttonColors(containerColor = NitroAmber),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("buy_bike_button")
            ) {
              Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Black)
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "UNLOCK FOR ${bike.priceCoins} COINS",
                fontWeight = FontWeight.Black,
                color = Color.Black
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Performance Tuning & Upgrades
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "PERFORMANCE TUNING",
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            color = Color.White
          )
          Spacer(modifier = Modifier.height(14.dp))

          // Stat 1: Top Speed
          UpgradeItem(
            name = "Top Speed",
            statKey = "topSpeed",
            bikeId = bike.id,
            currentValue = "${(bike.maxSpeedKmh + preferences.getUpgradeLevel(bike.id, "topSpeed") * 10f).toInt()} KM/H",
            level = preferences.getUpgradeLevel(bike.id, "topSpeed"),
            maxLevel = 5,
            cost = 350,
            coins = currentCoins,
            onUpgrade = {
              if (currentCoins >= 350) {
                preferences.coins -= 350
                currentCoins = preferences.coins
                preferences.incrementUpgrade(bike.id, "topSpeed")
              }
            }
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Stat 2: Acceleration
          UpgradeItem(
            name = "Acceleration",
            statKey = "accel",
            bikeId = bike.id,
            currentValue = "Stage ${preferences.getUpgradeLevel(bike.id, "accel") + 1}",
            level = preferences.getUpgradeLevel(bike.id, "accel"),
            maxLevel = 5,
            cost = 300,
            coins = currentCoins,
            onUpgrade = {
              if (currentCoins >= 300) {
                preferences.coins -= 300
                currentCoins = preferences.coins
                preferences.incrementUpgrade(bike.id, "accel")
              }
            }
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Stat 3: Handling
          UpgradeItem(
            name = "Handling & Lean",
            statKey = "handling",
            bikeId = bike.id,
            currentValue = "Stage ${preferences.getUpgradeLevel(bike.id, "handling") + 1}",
            level = preferences.getUpgradeLevel(bike.id, "handling"),
            maxLevel = 5,
            cost = 250,
            coins = currentCoins,
            onUpgrade = {
              if (currentCoins >= 250) {
                preferences.coins -= 250
                currentCoins = preferences.coins
                preferences.incrementUpgrade(bike.id, "handling")
              }
            }
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Stat 4: Brakes
          UpgradeItem(
            name = "Brakes & Decel",
            statKey = "brakes",
            bikeId = bike.id,
            currentValue = "Stage ${preferences.getUpgradeLevel(bike.id, "brakes") + 1}",
            level = preferences.getUpgradeLevel(bike.id, "brakes"),
            maxLevel = 5,
            cost = 200,
            coins = currentCoins,
            onUpgrade = {
              if (currentCoins >= 200) {
                preferences.coins -= 200
                currentCoins = preferences.coins
                preferences.incrementUpgrade(bike.id, "brakes")
              }
            }
          )
        }
      }
    }
  }
}

@Composable
private fun UpgradeItem(
  name: String,
  statKey: String,
  bikeId: String,
  currentValue: String,
  level: Int,
  maxLevel: Int,
  cost: Int,
  coins: Int,
  onUpgrade: () -> Unit
) {
  val isMaxed = level >= maxLevel
  val canAfford = coins >= cost && !isMaxed

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
        Text(text = currentValue, fontSize = 12.sp, color = NeonCyan, fontWeight = FontWeight.SemiBold)
      }

      Spacer(modifier = Modifier.height(4.dp))

      LinearProgressIndicator(
        progress = { (level.toFloat() / maxLevel).coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = NeonCyan,
        trackColor = DarkSurfaceElevated
      )
    }

    Spacer(modifier = Modifier.width(14.dp))

    OutlinedButton(
      onClick = onUpgrade,
      enabled = canAfford,
      shape = RoundedCornerShape(10.dp),
      modifier = Modifier.height(38.dp).testTag("upgrade_${statKey}_button")
    ) {
      if (isMaxed) {
        Text("MAX", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 11.sp)
      } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Upgrade, contentDescription = null, tint = if (canAfford) NitroAmber else Color.Gray, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("$cost ¢", fontWeight = FontWeight.Bold, color = if (canAfford) NitroAmber else Color.Gray, fontSize = 11.sp)
        }
      }
    }
  }
}
