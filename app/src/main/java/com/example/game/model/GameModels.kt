package com.example.game.model

import androidx.compose.ui.graphics.Color

enum class EnvironmentTheme {
  SUNSET,
  DAY,
  NIGHT,
  DESERT
}

data class GameLevel(
  val id: Int,
  val name: String,
  val subtitle: String,
  val targetDistanceMeters: Float,
  val timeLimitSeconds: Float,
  val trafficDensity: Float,
  val isTwoWay: Boolean,
  val environment: EnvironmentTheme,
  val requiredStarsToUnlock: Int,
  val rewardCoins: Int
)

enum class VehicleType(
  val widthMeters: Float,
  val lengthMeters: Float,
  val defaultMinSpeed: Float,
  val defaultMaxSpeed: Float
) {
  SEDAN(1.8f, 4.2f, 85f, 110f),
  SUV(2.0f, 4.8f, 90f, 115f),
  TRUCK(2.5f, 8.5f, 65f, 80f),
  SPORTS_CAR(1.9f, 4.4f, 120f, 145f),
  BIKE(0.9f, 2.1f, 100f, 125f)
}

data class TrafficVehicle(
  val id: Long,
  var lane: Int,
  var currentX: Float,
  var worldZ: Float,
  var speedKmh: Float,
  val isOncoming: Boolean,
  val type: VehicleType,
  val color: Color,
  var targetLane: Int = lane,
  var isChangingLane: Boolean = false,
  var blinkerState: Int = 0, // -1: left, 0: off, 1: right
  var blinkerTimer: Float = 0f,
  var overtaken: Boolean = false,
  var nearMissAwarded: Boolean = false,
  var isBraking: Boolean = false
)

data class BikeModel(
  val id: String,
  val name: String,
  val tagLine: String,
  val maxSpeedKmh: Float,
  val accelerationFactor: Float,
  val handlingFactor: Float,
  val brakeFactor: Float,
  val priceCoins: Int,
  val unlockedByDefault: Boolean = false
)

data class BikeColorOption(
  val id: String,
  val name: String,
  val primaryColor: Color,
  val secondaryColor: Color
)

object GameDefaults {
  val DEFAULT_BIKES = listOf(
    BikeModel(
      id = "smk_pulsar_250",
      name = "SMK Street 250",
      tagLine = "Agile urban commuter with nimble cornering",
      maxSpeedKmh = 195f,
      accelerationFactor = 8.5f,
      handlingFactor = 1.0f,
      brakeFactor = 12f,
      priceCoins = 0,
      unlockedByDefault = true
    ),
    BikeModel(
      id = "smk_inferno_600",
      name = "SMK Inferno 600",
      tagLine = "High-revving supersport with blistering top-end",
      maxSpeedKmh = 245f,
      accelerationFactor = 12.0f,
      handlingFactor = 1.25f,
      brakeFactor = 15f,
      priceCoins = 0,
      unlockedByDefault = true
    ),
    BikeModel(
      id = "smk_phantom_1000",
      name = "SMK Phantom 1000",
      tagLine = "Liter-class hyperbike engineered for unmatched highway dominance",
      maxSpeedKmh = 295f,
      accelerationFactor = 16.5f,
      handlingFactor = 1.45f,
      brakeFactor = 18f,
      priceCoins = 0,
      unlockedByDefault = true
    )
  )

  val BIKE_COLORS = listOf(
    BikeColorOption("crimson_red", "Crimson Fire", Color(0xFFE53935), Color(0xFFB71C1C)),
    BikeColorOption("matte_black", "Stealth Shadow", Color(0xFF263238), Color(0xFF101416)),
    BikeColorOption("electric_cyan", "Cyber Cyan", Color(0xFF00E5FF), Color(0xFF0097A7)),
    BikeColorOption("racing_yellow", "Solar Flare", Color(0xFFFFD600), Color(0xFFFF8F00)),
    BikeColorOption("toxic_green", "Viper Green", Color(0xFF00E676), Color(0xFF00A040))
  )

  val LEVELS = listOf(
    GameLevel(
      id = 1,
      name = "Level 1: Sunset Highway",
      subtitle = "One-way highway, gentle flow. Reach 2,000m.",
      targetDistanceMeters = 2000f,
      timeLimitSeconds = 90f,
      trafficDensity = 1.0f,
      isTwoWay = false,
      environment = EnvironmentTheme.SUNSET,
      requiredStarsToUnlock = 0,
      rewardCoins = 300
    ),
    GameLevel(
      id = 2,
      name = "Level 2: Coastal Expressway",
      subtitle = "Two-way traffic! Beware of high-speed oncoming vehicles. Reach 3,500m.",
      targetDistanceMeters = 3500f,
      timeLimitSeconds = 110f,
      trafficDensity = 1.5f,
      isTwoWay = true,
      environment = EnvironmentTheme.DAY,
      requiredStarsToUnlock = 0,
      rewardCoins = 600
    ),
    GameLevel(
      id = 3,
      name = "Level 3: Midnight Tokyo",
      subtitle = "Heavy nocturnal traffic, illuminated skylines. Reach 4,500m.",
      targetDistanceMeters = 4500f,
      timeLimitSeconds = 120f,
      trafficDensity = 2.0f,
      isTwoWay = false,
      environment = EnvironmentTheme.NIGHT,
      requiredStarsToUnlock = 0,
      rewardCoins = 1000
    ),
    GameLevel(
      id = 4,
      name = "Level 4: Desert Outrun",
      subtitle = "Endless canyon straightaway, maximum speed overtakes. Reach 5,500m.",
      targetDistanceMeters = 5500f,
      timeLimitSeconds = 135f,
      trafficDensity = 2.5f,
      isTwoWay = true,
      environment = EnvironmentTheme.DESERT,
      requiredStarsToUnlock = 0,
      rewardCoins = 1500
    )
  )
}
