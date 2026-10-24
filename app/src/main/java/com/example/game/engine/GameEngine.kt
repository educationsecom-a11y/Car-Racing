package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.audio.SoundManager
import com.example.game.data.GamePreferences
import com.example.game.model.BikeModel
import com.example.game.model.GameLevel
import com.example.game.model.TrafficVehicle
import com.example.game.model.VehicleType
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class GameEngine(
  val level: GameLevel,
  val bike: BikeModel,
  private val soundManager: SoundManager,
  private val gamePreferences: GamePreferences
) {
  // Render tick that triggers 60 FPS recomposition and canvas redraw
  var frameTick by mutableLongStateOf(0L)

  // Player state
  var playerX by mutableFloatStateOf(0.25f)
  var speedKmh by mutableFloatStateOf(95f) // Actively racing down the highway immediately!
  var targetSpeedKmh by mutableFloatStateOf(95f)
  var distanceMeters by mutableFloatStateOf(0f)
  var timeRemaining by mutableFloatStateOf(level.timeLimitSeconds)

  var nitroFuel by mutableFloatStateOf(100f)
  var isNitroActive by mutableStateOf(false)

  var score by mutableIntStateOf(0)
  var comboMultiplier by mutableIntStateOf(1)
  var comboTimer by mutableFloatStateOf(0f)
  var nearMissCount by mutableIntStateOf(0)

  var currentGear by mutableIntStateOf(2)
  var engineRpm by mutableFloatStateOf(3500f)
  var leanAngle by mutableFloatStateOf(0f)

  var isCrashed by mutableStateOf(false)
  var isVictory by mutableStateOf(false)
  var crashReason by mutableStateOf("")
  var starsEarned by mutableIntStateOf(0)
  var earnedCoins by mutableIntStateOf(0)

  var recentNearMissText by mutableStateOf<String?>(null)
  var recentNearMissTimer by mutableFloatStateOf(0f)

  // Touch controls state
  var isSteeringLeft by mutableStateOf(false)
  var isSteeringRight by mutableStateOf(false)
  var isThrottleActive by mutableStateOf(false)
  var isBrakeActive by mutableStateOf(false)
  var isHornActive by mutableStateOf(false)

  // Road dynamics
  var roadCurvature by mutableFloatStateOf(0f)
  private var targetRoadCurvature = 0f
  private var curveChangeTimer = 4.0f

  // Upgrades applied
  private val topSpeedUpgrade = gamePreferences.getUpgradeLevel(bike.id, "topSpeed")
  private val accelUpgrade = gamePreferences.getUpgradeLevel(bike.id, "accel")
  private val handlingUpgrade = gamePreferences.getUpgradeLevel(bike.id, "handling")
  private val brakeUpgrade = gamePreferences.getUpgradeLevel(bike.id, "brakes")

  val maxSpeedKmh: Float = bike.maxSpeedKmh + topSpeedUpgrade * 10f
  val accelerationFactor: Float = bike.accelerationFactor + accelUpgrade * 1.5f
  val handlingFactor: Float = bike.handlingFactor + handlingUpgrade * 0.15f
  val brakeFactor: Float = bike.brakeFactor + brakeUpgrade * 2.0f

  // Traffic list
  val trafficVehicles = mutableListOf<TrafficVehicle>()
  private var nextVehicleId = 1L
  private var spawnTimer = 0f

  init {
    // Initial procedural spawn
    spawnInitialTraffic()
    soundManager.startEngineSound()
  }

  private fun getLaneCenter(lane: Int): Float {
    return when (lane) {
      0 -> -0.75f
      1 -> -0.25f
      2 -> 0.25f
      3 -> 0.75f
      else -> 0f
    }
  }

  private fun spawnInitialTraffic() {
    trafficVehicles.clear()
    val initialCounts = (4 * level.trafficDensity).toInt().coerceIn(3, 8)
    var z = 45f
    for (i in 0 until initialCounts) {
      z += Random.nextFloat() * 25f + 25f
      val lane = if (level.isTwoWay) {
        if (Random.nextFloat() < 0.45f) Random.nextInt(0, 2) else Random.nextInt(2, 4)
      } else {
        Random.nextInt(0, 4)
      }
      spawnVehicleAt(lane, z)
    }
  }

  private fun spawnVehicleAt(lane: Int, worldZ: Float) {
    val isOncoming = level.isTwoWay && (lane == 0 || lane == 1)
    val typeRoll = Random.nextFloat()
    val type = when {
      typeRoll < 0.45f -> VehicleType.SEDAN
      typeRoll < 0.70f -> VehicleType.SUV
      typeRoll < 0.88f -> VehicleType.TRUCK
      typeRoll < 0.95f -> VehicleType.SPORTS_CAR
      else -> VehicleType.BIKE
    }

    val carColors = listOf(
      Color(0xFFE53935), // Red
      Color(0xFFFDD835), // Yellow (like reference image)
      Color(0xFFFB8C00), // Orange
      Color(0xFFECEFF1), // White
      Color(0xFF1E88E5), // Blue
      Color(0xFF37474F), // Charcoal
      Color(0xFF43A047)  // Green
    )
    val color = carColors[Random.nextInt(carColors.size)]

    val baseSpeed = Random.nextFloat() * (type.defaultMaxSpeed - type.defaultMinSpeed) + type.defaultMinSpeed

    val vehicle = TrafficVehicle(
      id = nextVehicleId++,
      lane = lane,
      currentX = getLaneCenter(lane),
      worldZ = worldZ,
      speedKmh = baseSpeed,
      isOncoming = isOncoming,
      type = type,
      color = color
    )
    trafficVehicles.add(vehicle)
  }

  fun update(dt: Float) {
    if (isCrashed || isVictory) return

    // Trigger state observation for Compose redrawing
    frameTick++

    val deltaSeconds = dt.coerceIn(0.001f, 0.05f)

    // 1. Resolve combined touch & keyboard/gamepad inputs
    val effectiveThrottle = isThrottleActive || com.example.game.input.GameInputBridge.isThrottlePressed
    val effectiveBrake = isBrakeActive || com.example.game.input.GameInputBridge.isBrakePressed
    val effectiveSteerLeft = isSteeringLeft || com.example.game.input.GameInputBridge.isSteerLeftPressed
    val effectiveSteerRight = isSteeringRight || com.example.game.input.GameInputBridge.isSteerRightPressed
    val effectiveHorn = isHornActive || com.example.game.input.GameInputBridge.isHornPressed
    val effectiveNitro = isNitroActive || com.example.game.input.GameInputBridge.isNitroPressed

    // Horn action
    if (effectiveHorn) {
      soundManager.playHorn()
      // Make cars ahead in current lane switch lane if possible
      for (v in trafficVehicles) {
        if (v.worldZ in 5f..45f && abs(v.currentX - playerX) < 0.35f && !v.isChangingLane) {
          tryVehicleLaneChange(v)
        }
      }
    }

    // 2. Throttle, Brake, Nitro Dynamics
    val effectiveMaxSpeed = if (effectiveNitro && nitroFuel > 0f) maxSpeedKmh * 1.22f else maxSpeedKmh

    if (effectiveNitro && nitroFuel > 0f) {
      nitroFuel = (nitroFuel - deltaSeconds * 22f).coerceAtLeast(0f)
      if (nitroFuel <= 0f) isNitroActive = false
    } else {
      // Slow passive nitro fuel recovery
      nitroFuel = (nitroFuel + deltaSeconds * 2.5f).coerceAtMost(100f)
    }

    if (effectiveBrake) {
      targetSpeedKmh = 0f
      speedKmh = (speedKmh - brakeFactor * 6.5f * deltaSeconds).coerceAtLeast(0f)
    } else if (effectiveThrottle) {
      targetSpeedKmh = effectiveMaxSpeed
      val accelMultiplier = if (effectiveNitro) 1.8f else 1.0f
      val currentAccel = accelerationFactor * (1.1f - (speedKmh / effectiveMaxSpeed) * 0.45f) * accelMultiplier
      speedKmh = (speedKmh + currentAccel * deltaSeconds * 4.5f).coerceAtMost(effectiveMaxSpeed)
    } else {
      // Highway auto-cruise speed: naturally cruises at 95 KM/H so the game is always racing in full motion!
      val cruiseSpeed = 95f
      if (speedKmh < cruiseSpeed) {
        speedKmh = (speedKmh + accelerationFactor * deltaSeconds * 3.5f).coerceAtMost(cruiseSpeed)
      } else {
        speedKmh = (speedKmh - deltaSeconds * 6.0f).coerceAtLeast(cruiseSpeed)
      }
    }

    // 3. Engine RPM & Gear calculation
    val speedRatio = (speedKmh / maxSpeedKmh).coerceIn(0f, 1f)
    val totalGears = 6
    val gearIndex = (speedRatio * (totalGears - 0.1f)).toInt().coerceIn(0, totalGears - 1)
    currentGear = gearIndex + 1
    val gearRatioStart = gearIndex.toFloat() / totalGears
    val gearRatioEnd = (gearIndex + 1).toFloat() / totalGears
    val inGearProgress = ((speedRatio - gearRatioStart) / (gearRatioEnd - gearRatioStart)).coerceIn(0f, 1f)
    engineRpm = 2500f + inGearProgress * 10500f
    soundManager.updateEngineRpm(engineRpm / 14000f)

    // 4. Steering Physics & Lean
    var steerDirection = 0f
    if (effectiveSteerLeft) steerDirection -= 1f
    if (effectiveSteerRight) steerDirection += 1f

    val lateralSpeed = 1.35f * handlingFactor * (0.35f + speedRatio * 0.65f)
    playerX = (playerX + steerDirection * lateralSpeed * deltaSeconds).coerceIn(-1.12f, 1.12f)

    // Lean angle animation (counter-steering tilt)
    val targetLean = -steerDirection * (16f + speedRatio * 8f)
    leanAngle += (targetLean - leanAngle) * (deltaSeconds * 9f)

    // Guardrail boundary check
    if (abs(playerX) >= 1.08f && speedKmh > 30f) {
      triggerCrash("Crashed into highway guardrail at ${speedKmh.toInt()} KM/H")
      return
    }

    // 5. Distance and Timer Progression
    val distanceDeltaMeters = (speedKmh / 3.6f) * deltaSeconds
    distanceMeters += distanceDeltaMeters
    timeRemaining = (timeRemaining - deltaSeconds).coerceAtLeast(0f)

    // Score accumulation from high speed
    if (speedKmh > 70f) {
      score += ((speedKmh * 0.08f) * comboMultiplier * deltaSeconds).toInt()
    }

    // Combo timer decay
    if (comboMultiplier > 1) {
      comboTimer -= deltaSeconds
      if (comboTimer <= 0f) {
        comboMultiplier = 1
      }
    }

    // Near miss text banner decay
    if (recentNearMissText != null) {
      recentNearMissTimer -= deltaSeconds
      if (recentNearMissTimer <= 0f) {
        recentNearMissText = null
      }
    }

    // 6. Road curvature undulation
    curveChangeTimer -= deltaSeconds
    if (curveChangeTimer <= 0f) {
      curveChangeTimer = Random.nextFloat() * 5f + 4f
      targetRoadCurvature = (Random.nextFloat() * 2f - 1f) * 0.45f
    }
    roadCurvature += (targetRoadCurvature - roadCurvature) * (deltaSeconds * 1.5f)

    // 7. Traffic update & spawning
    updateTraffic(deltaSeconds, distanceDeltaMeters)

    // 8. Win / Loss Check
    if (distanceMeters >= level.targetDistanceMeters) {
      triggerVictory()
    } else if (timeRemaining <= 0f) {
      triggerCrash("Out of time! Target distance not reached.")
    }
  }

  private fun updateTraffic(dt: Float, playerDistanceDelta: Float) {
    // Relative speed movement
    val iter = trafficVehicles.iterator()
    while (iter.hasNext()) {
      val v = iter.next()

      // Vehicle speed in m/s
      val vehicleMps = v.speedKmh / 3.6f
      val relativeMps = if (v.isOncoming) {
        // Closing in from both speeds
        -( (speedKmh / 3.6f) + vehicleMps )
      } else {
        // Moving ahead or player catching up
        vehicleMps - (speedKmh / 3.6f)
      }

      v.worldZ += relativeMps * dt

      // Smooth lane changes
      if (v.isChangingLane) {
        val targetX = getLaneCenter(v.targetLane)
        val dir = if (targetX > v.currentX) 1f else -1f
        v.currentX += dir * 0.8f * dt
        v.blinkerTimer += dt
        if (abs(v.currentX - targetX) < 0.04f) {
          v.currentX = targetX
          v.lane = v.targetLane
          v.isChangingLane = false
          v.blinkerState = 0
        }
      } else if (Random.nextFloat() < 0.002f * level.trafficDensity) {
        tryVehicleLaneChange(v)
      }

      // Check Collision (AABB in road space)
      // Player is at Z = 0, road X = playerX, width ~ 0.22 (bike)
      val playerZStart = -0.5f
      val playerZEnd = 2.2f
      val vehicleZStart = v.worldZ - (v.type.lengthMeters * 0.5f)
      val vehicleZEnd = v.worldZ + (v.type.lengthMeters * 0.5f)

      val zOverlap = (playerZStart <= vehicleZEnd) && (playerZEnd >= vehicleZStart)
      val xThreshold = (v.type.widthMeters * 0.09f) + 0.12f
      val xDist = abs(playerX - v.currentX)

      if (zOverlap && xDist < xThreshold) {
        val hitSpeed = if (v.isOncoming) speedKmh + v.speedKmh else speedKmh
        val vehicleName = when (v.type) {
          VehicleType.TRUCK -> "Heavy Semi-Truck"
          VehicleType.SUV -> "SUV"
          VehicleType.SPORTS_CAR -> "Sports Car"
          VehicleType.SEDAN -> "Sedan"
          VehicleType.BIKE -> "Motorcycle"
        }
        val directionDesc = if (v.isOncoming) "Head-on collision with $vehicleName" else "Rear-ended $vehicleName"
        triggerCrash("$directionDesc at ${hitSpeed.toInt()} KM/H")
        return
      }

      // Near-Miss check
      // Passed the vehicle at high speed (> 90 KM/H) with very close lateral distance
      if (!v.nearMissAwarded && speedKmh > 90f) {
        val isPassing = if (v.isOncoming) (v.worldZ in -3.0f..1.5f) else (v.worldZ in -3.0f..1.0f)
        if (isPassing && xDist < (xThreshold + 0.18f)) {
          v.nearMissAwarded = true
          handleNearMiss()
        }
      }

      // Remove vehicles that are too far behind or ahead
      if (v.worldZ < -35f || v.worldZ > 320f) {
        iter.remove()
      }
    }

    // Procedural Spawner
    spawnTimer += dt
    val maxVehicles = (6 * level.trafficDensity).toInt().coerceIn(4, 14)
    if (spawnTimer > (2.5f / level.trafficDensity) && trafficVehicles.size < maxVehicles) {
      spawnTimer = 0f
      val spawnZ = Random.nextFloat() * 40f + 160f
      val lane = if (level.isTwoWay) {
        if (Random.nextFloat() < 0.45f) Random.nextInt(0, 2) else Random.nextInt(2, 4)
      } else {
        Random.nextInt(0, 4)
      }
      // Check if lane is not blocked at spawnZ
      val isBlocked = trafficVehicles.any { it.lane == lane && abs(it.worldZ - spawnZ) < 25f }
      if (!isBlocked) {
        spawnVehicleAt(lane, spawnZ)
      }
    }
  }

  private fun tryVehicleLaneChange(v: TrafficVehicle) {
    if (level.isTwoWay) {
      // In two way, lane 0 & 1 only switch between 0 & 1, 2 & 3 between 2 & 3
      val newLane = if (v.lane == 0) 1 else if (v.lane == 1) 0 else if (v.lane == 2) 3 else 2
      v.targetLane = newLane
      v.isChangingLane = true
      v.blinkerState = if (newLane > v.lane) 1 else -1
    } else {
      val delta = if (Random.nextBoolean()) 1 else -1
      val newLane = (v.lane + delta).coerceIn(0, 3)
      if (newLane != v.lane) {
        v.targetLane = newLane
        v.isChangingLane = true
        v.blinkerState = if (newLane > v.lane) 1 else -1
      }
    }
  }

  private fun handleNearMiss() {
    nearMissCount++
    comboMultiplier = (comboMultiplier + 1).coerceAtMost(6)
    comboTimer = 4.0f

    val bonusPoints = 120 * comboMultiplier
    score += bonusPoints

    // Add extra time!
    timeRemaining += 2.0f

    // Recharge nitro
    nitroFuel = (nitroFuel + 30f).coerceAtMost(100f)

    recentNearMissText = "🔥 NEAR MISS! +$bonusPoints PTS  +2s TIME"
    recentNearMissTimer = 1.8f

    soundManager.playNearMiss()
  }

  private fun triggerCrash(reason: String) {
    isCrashed = true
    crashReason = reason
    soundManager.stopEngineSound()
    soundManager.playCrash()
  }

  private fun triggerVictory() {
    isVictory = true
    soundManager.stopEngineSound()
    soundManager.playVictory()

    // Calculate stars
    var stars = 1 // completion star
    if (timeRemaining >= 10f) stars++
    if (nearMissCount >= 4) stars++
    starsEarned = stars

    earnedCoins = level.rewardCoins + (nearMissCount * 35) + (timeRemaining * 10).toInt()

    gamePreferences.saveLevelStars(level.id, stars)
    gamePreferences.saveLevelScore(level.id, score)
    gamePreferences.coins += earnedCoins

    // Unlock next level if available
    val nextLevelId = level.id + 1
    if (nextLevelId <= 4) {
      gamePreferences.unlockLevel(nextLevelId)
    }
  }

  fun toggleNitro() {
    if (nitroFuel > 15f) {
      isNitroActive = !isNitroActive
      if (isNitroActive) soundManager.vibrateNearMiss()
    }
  }

  fun restart() {
    playerX = 0.25f
    speedKmh = 95f
    targetSpeedKmh = 95f
    currentGear = 3
    distanceMeters = 0f
    timeRemaining = level.timeLimitSeconds
    nitroFuel = 100f
    isNitroActive = false
    score = 0
    comboMultiplier = 1
    comboTimer = 0f
    nearMissCount = 0
    isCrashed = false
    isVictory = false
    crashReason = ""
    starsEarned = 0
    recentNearMissText = null
    spawnInitialTraffic()
    soundManager.startEngineSound()
  }

  fun destroy() {
    soundManager.stopEngineSound()
  }
}
