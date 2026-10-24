package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.model.BikeColorOption
import com.example.game.model.EnvironmentTheme
import com.example.game.model.TrafficVehicle
import com.example.game.model.VehicleType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun CockpitRenderer(
  engine: GameEngine,
  bikeColor: BikeColorOption,
  modifier: Modifier = Modifier
) {
  val textMeasurer = rememberTextMeasurer()

  Canvas(modifier = modifier.fillMaxSize()) {
    // Read engine state to force 60 FPS redraw on every frame
    val _frame = engine.frameTick
    val width = size.width
    val height = size.height

    // Horizon line at ~40% of screen height
    val horizonY = height * 0.40f
    // Vanishing point shifted dynamically by road curvature and player steering
    val vanishX = width * 0.5f + (engine.roadCurvature * width * 0.18f) - (engine.playerX * width * 0.08f)

    // Camera shake calculation for high speed / nitro
    var shakeX = 0f
    var shakeY = 0f
    if (engine.speedKmh > 140f || engine.isNitroActive) {
      val intensity = if (engine.isNitroActive) 3.5f else ((engine.speedKmh - 140f) / 100f) * 2.0f
      shakeX = (Random.nextFloat() * 2f - 1f) * intensity
      shakeY = (Random.nextFloat() * 2f - 1f) * intensity
    }

    withTransform({
      translate(shakeX, shakeY)
    }) {
      // 1. Draw Sky, Sun & Horizon Scenery
      drawEnvironmentSky(engine.level.environment, horizonY, width, height)

      // 2. Draw Roadside Scenery (Trees, mountains, guardrails)
      drawRoadsideScenery(engine.level.environment, vanishX, horizonY, width, height, engine.distanceMeters)

      // 3. Draw Perspective Highway (Asphalt, lane markings, curbs)
      drawPerspectiveHighway(engine, vanishX, horizonY, width, height)

      // 4. Draw Traffic Vehicles in 3D Perspective
      drawTrafficVehicles(engine, vanishX, horizonY, width, height)

      // 5. Speed Lines / Nitro Warp
      if (engine.isNitroActive || engine.speedKmh > 210f) {
        drawSpeedStreaks(vanishX, horizonY, width, height, engine.isNitroActive)
      }

      // 6. Draw First-Person Motorcycle Cockpit Overlay (Windshield, Handlebars, Gauges, Mirrors, Hands)
      drawMotorcycleCockpit(engine, bikeColor, textMeasurer, width, height)
    }

    // 7. Crash impact flash overlay if crashed
    if (engine.isCrashed) {
      drawRect(
        color = Color(0x77D50000),
        topLeft = Offset.Zero,
        size = size
      )
    }
  }
}

// ----------------------------------------------------
// 1. SKY & HORIZON DRAWING
// ----------------------------------------------------
private fun DrawScope.drawEnvironmentSky(
  theme: EnvironmentTheme,
  horizonY: Float,
  width: Float,
  height: Float
) {
  val skyBrush = when (theme) {
    EnvironmentTheme.SUNSET -> Brush.verticalGradient(
      colors = listOf(
        Color(0xFF5D4037), // Dusk brown
        Color(0xFFD84315), // Deep orange
        Color(0xFFFF8A65), // Warm peach
        Color(0xFFFFCC80)  // Golden horizon
      ),
      startY = 0f,
      endY = horizonY
    )
    EnvironmentTheme.DAY -> Brush.verticalGradient(
      colors = listOf(
        Color(0xFF0288D1),
        Color(0xFF29B6F6),
        Color(0xFF81D4FA),
        Color(0xFFE1F5FE)
      ),
      startY = 0f,
      endY = horizonY
    )
    EnvironmentTheme.NIGHT -> Brush.verticalGradient(
      colors = listOf(
        Color(0xFF0A0E17),
        Color(0xFF111827),
        Color(0xFF1E293B),
        Color(0xFF334155)
      ),
      startY = 0f,
      endY = horizonY
    )
    EnvironmentTheme.DESERT -> Brush.verticalGradient(
      colors = listOf(
        Color(0xFF795548),
        Color(0xFFBCAAA4),
        Color(0xFFFFB74D),
        Color(0xFFFFE0B2)
      ),
      startY = 0f,
      endY = horizonY
    )
  }

  drawRect(
    brush = skyBrush,
    topLeft = Offset.Zero,
    size = Size(width, horizonY + 2f)
  )

  // Sun / Moon on horizon
  if (theme == EnvironmentTheme.SUNSET) {
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFB74D), Color(0x00FFB74D)),
        center = Offset(width * 0.48f, horizonY * 0.85f),
        radius = horizonY * 0.4f
      ),
      radius = horizonY * 0.38f,
      center = Offset(width * 0.48f, horizonY * 0.85f)
    )
  }
}

// ----------------------------------------------------
// 2. ROADSIDE SCENERY (Trees & Terrain)
// ----------------------------------------------------
private fun DrawScope.drawRoadsideScenery(
  theme: EnvironmentTheme,
  vanishX: Float,
  horizonY: Float,
  width: Float,
  height: Float,
  distanceMeters: Float
) {
  // Ground fill (grass or desert or urban)
  val groundColor = when (theme) {
    EnvironmentTheme.SUNSET -> Color(0xFF334228)
    EnvironmentTheme.DAY -> Color(0xFF2E5B1D)
    EnvironmentTheme.NIGHT -> Color(0xFF0B1209)
    EnvironmentTheme.DESERT -> Color(0xFFC79248)
  }
  drawRect(
    color = groundColor,
    topLeft = Offset(0f, horizonY),
    size = Size(width, height - horizonY)
  )

  // Mountain silhouettes on horizon
  val mountainColor = when (theme) {
    EnvironmentTheme.SUNSET -> Color(0xFF4E342E)
    EnvironmentTheme.DAY -> Color(0xFF455A64)
    EnvironmentTheme.NIGHT -> Color(0xFF0F172A)
    EnvironmentTheme.DESERT -> Color(0xFF8D6E63)
  }

  val mountainPath = Path().apply {
    moveTo(0f, horizonY)
    lineTo(width * 0.15f, horizonY - 45f)
    lineTo(width * 0.32f, horizonY - 20f)
    lineTo(width * 0.52f, horizonY - 60f)
    lineTo(width * 0.75f, horizonY - 30f)
    lineTo(width * 0.90f, horizonY - 50f)
    lineTo(width, horizonY)
    close()
  }
  drawPath(mountainPath, color = mountainColor)

  // Distant tree lines on road edges
  val treeBaseColor = if (theme == EnvironmentTheme.DESERT) Color(0xFFA1887F) else Color(0xFF1B3815)
  val treeHighlight = if (theme == EnvironmentTheme.SUNSET) Color(0xFF8D6E63) else Color(0xFF2E6924)

  // Left roadside trees
  for (i in 0..6) {
    val treeZ = ((distanceMeters * 0.6f + i * 28f) % 180f)
    if (treeZ > 2f) {
      val scale = 1f / (treeZ * 0.05f + 1f)
      val x = vanishX - (width * 0.55f * scale) - (80f * scale)
      val y = horizonY + (height - horizonY) * scale * 0.7f
      val treeRadius = 60f * scale
      drawCircle(
        color = treeBaseColor,
        radius = treeRadius,
        center = Offset(x, y - treeRadius)
      )
      drawCircle(
        color = treeHighlight,
        radius = treeRadius * 0.65f,
        center = Offset(x + 5f, y - treeRadius * 1.1f)
      )
    }
  }

  // Right roadside trees
  for (i in 0..6) {
    val treeZ = ((distanceMeters * 0.6f + i * 28f + 14f) % 180f)
    if (treeZ > 2f) {
      val scale = 1f / (treeZ * 0.05f + 1f)
      val x = vanishX + (width * 0.55f * scale) + (80f * scale)
      val y = horizonY + (height - horizonY) * scale * 0.7f
      val treeRadius = 60f * scale
      drawCircle(
        color = treeBaseColor,
        radius = treeRadius,
        center = Offset(x, y - treeRadius)
      )
      drawCircle(
        color = treeHighlight,
        radius = treeRadius * 0.65f,
        center = Offset(x - 5f, y - treeRadius * 1.1f)
      )
    }
  }
}

// ----------------------------------------------------
// 3. PERSPECTIVE HIGHWAY DRAWING
// ----------------------------------------------------
private fun DrawScope.drawPerspectiveHighway(
  engine: GameEngine,
  vanishX: Float,
  horizonY: Float,
  width: Float,
  height: Float
) {
  val roadTopWidth = width * 0.08f
  val roadBottomWidth = width * 1.45f

  val roadTopLeft = vanishX - roadTopWidth * 0.5f
  val roadTopRight = vanishX + roadTopWidth * 0.5f
  val roadBottomLeft = (width * 0.5f) - (roadBottomWidth * 0.5f) - (engine.playerX * width * 0.35f)
  val roadBottomRight = (width * 0.5f) + (roadBottomWidth * 0.5f) - (engine.playerX * width * 0.35f)

  // Road surface (Dark Asphalt)
  val roadPath = Path().apply {
    moveTo(roadTopLeft, horizonY)
    lineTo(roadTopRight, horizonY)
    lineTo(roadBottomRight, height)
    lineTo(roadBottomLeft, height)
    close()
  }

  val roadBrush = Brush.verticalGradient(
    colors = listOf(
      Color(0xFF5A6065),
      Color(0xFF373B3E),
      Color(0xFF222528),
      Color(0xFF181A1C)
    ),
    startY = horizonY,
    endY = height
  )
  drawPath(roadPath, brush = roadBrush)

  // Guardrails on both sides (metallic reflections)
  val leftGuardrailPath = Path().apply {
    moveTo(roadTopLeft - 10f, horizonY)
    lineTo(roadTopLeft, horizonY)
    lineTo(roadBottomLeft, height)
    lineTo(roadBottomLeft - 45f, height)
    close()
  }
  drawPath(leftGuardrailPath, color = Color(0xFF78909C))

  val rightGuardrailPath = Path().apply {
    moveTo(roadTopRight, horizonY)
    lineTo(roadTopRight + 10f, horizonY)
    lineTo(roadBottomRight + 45f, height)
    lineTo(roadBottomRight, height)
    close()
  }
  drawPath(rightGuardrailPath, color = Color(0xFF78909C))

  // Lane dividers (4 lanes = 3 divider lines between them)
  // lane 0: -1.0 to -0.5
  // lane 1: -0.5 to 0.0
  // lane 2: 0.0 to 0.5
  // lane 3: 0.5 to 1.0
  val dividerFractions = listOf(0.25f, 0.50f, 0.75f)

  for (fraction in dividerFractions) {
    val isCenterDivider = fraction == 0.50f
    val isTwoWayCenter = isCenterDivider && engine.level.isTwoWay

    // Draw dashed lines projecting in perspective
    val numSegments = 16
    for (seg in 0 until numSegments) {
      val zNear = ((engine.distanceMeters * 0.45f + seg * 8.0f) % 120.0f).toFloat()
      val zFar = zNear + 4.5f

      if (zNear > 0.5f) {
        val tNear = 1f / (zNear * 0.035f + 1f)
        val tFar = 1f / (zFar * 0.035f + 1f)

        val yNear = horizonY + (height - horizonY) * (tNear * tNear)
        val yFar = horizonY + (height - horizonY) * (tFar * tFar)

        val leftXNear = roadTopLeft + (roadBottomLeft - roadTopLeft) * (tNear * tNear)
        val rightXNear = roadTopRight + (roadBottomRight - roadTopRight) * (tNear * tNear)
        val xNear = leftXNear + (rightXNear - leftXNear) * fraction

        val leftXFar = roadTopLeft + (roadBottomLeft - roadTopLeft) * (tFar * tFar)
        val rightXFar = roadTopRight + (roadBottomRight - roadTopRight) * (tFar * tFar)
        val xFar = leftXFar + (rightXFar - leftXFar) * fraction

        val strokeWidth = (4f + 14f * (tNear * tNear)).coerceIn(1.5f, 18f)

        if (isTwoWayCenter) {
          // Double yellow center divider line
          val gap = strokeWidth * 0.9f
          drawLine(
            color = Color(0xFFFFD54F),
            start = Offset(xNear - gap, yNear),
            end = Offset(xFar - gap, yFar),
            strokeWidth = strokeWidth * 0.6f,
            cap = StrokeCap.Round
          )
          drawLine(
            color = Color(0xFFFFD54F),
            start = Offset(xNear + gap, yNear),
            end = Offset(xFar + gap, yFar),
            strokeWidth = strokeWidth * 0.6f,
            cap = StrokeCap.Round
          )
        } else {
          // White dashed line (or solid white if center one-way)
          val color = if (isCenterDivider) Color(0xFFFFFFFF) else Color(0xCCFFFFFF)
          drawLine(
            color = color,
            start = Offset(xNear, yNear),
            end = Offset(xFar, yFar),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
          )
        }
      }
    }
  }
}

// ----------------------------------------------------
// 4. TRAFFIC VEHICLES DRAWING (3D PERSPECTIVE)
// ----------------------------------------------------
private fun DrawScope.drawTrafficVehicles(
  engine: GameEngine,
  vanishX: Float,
  horizonY: Float,
  width: Float,
  height: Float
) {
  // Sort vehicles by distance (farthest first) for painter's algorithm
  val sortedVehicles = engine.trafficVehicles.sortedByDescending { it.worldZ }

  val roadBottomWidth = width * 1.45f
  val roadTopWidth = width * 0.08f

  for (v in sortedVehicles) {
    if (v.worldZ <= 0.8f || v.worldZ > 240f) continue

    // 3D perspective projection formula
    val scale = 1f / (v.worldZ * 0.022f + 1f)
    val depthFactor = scale * scale

    val y = horizonY + (height - horizonY) * depthFactor
    // Road boundaries at this Y
    val roadLeftAtY = (vanishX - roadTopWidth * 0.5f) + (((width * 0.5f) - (roadBottomWidth * 0.5f) - (engine.playerX * width * 0.35f)) - (vanishX - roadTopWidth * 0.5f)) * depthFactor
    val roadRightAtY = (vanishX + roadTopWidth * 0.5f) + (((width * 0.5f) + (roadBottomWidth * 0.5f) - (engine.playerX * width * 0.35f)) - (vanishX + roadTopWidth * 0.5f)) * depthFactor

    val roadWidthAtY = roadRightAtY - roadLeftAtY
    // Map currentX from [-1, 1] to screen X
    val screenX = roadLeftAtY + roadWidthAtY * ((v.currentX + 1f) * 0.5f)

    val vehiclePixelWidth = when (v.type) {
      VehicleType.TRUCK -> 110f * scale
      VehicleType.SUV -> 85f * scale
      VehicleType.SEDAN -> 78f * scale
      VehicleType.SPORTS_CAR -> 80f * scale
      VehicleType.BIKE -> 40f * scale
    }
    val vehiclePixelHeight = when (v.type) {
      VehicleType.TRUCK -> 125f * scale
      VehicleType.SUV -> 80f * scale
      VehicleType.SEDAN -> 65f * scale
      VehicleType.SPORTS_CAR -> 55f * scale
      VehicleType.BIKE -> 65f * scale
    }

    // Drop shadow
    drawOval(
      color = Color(0x77000000),
      topLeft = Offset(screenX - vehiclePixelWidth * 0.6f, y - 8f * scale),
      size = Size(vehiclePixelWidth * 1.2f, 16f * scale)
    )

    if (v.isOncoming) {
      // Draw ONCOMING vehicle (Front View with glowing headlights!)
      drawOncomingVehicle(screenX, y, vehiclePixelWidth, vehiclePixelHeight, v, scale)
    } else {
      // Draw FORWARD vehicle (Rear View with glowing taillights & bumper)
      drawForwardVehicle(screenX, y, vehiclePixelWidth, vehiclePixelHeight, v, scale)
    }
  }
}

private fun DrawScope.drawForwardVehicle(
  screenX: Float,
  groundY: Float,
  w: Float,
  h: Float,
  v: TrafficVehicle,
  scale: Float
) {
  val topY = groundY - h
  val left = screenX - w * 0.5f

  // Main vehicle body
  val bodyRect = Rect(left, topY + h * 0.25f, left + w, groundY)
  drawRoundRect(
    color = v.color,
    topLeft = Offset(bodyRect.left, bodyRect.top),
    size = Size(bodyRect.width, bodyRect.height),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale, 6f * scale)
  )

  // Cabin / Roof (tapered top)
  val roofWidth = w * 0.78f
  val roofLeft = screenX - roofWidth * 0.5f
  val roofRect = Rect(roofLeft, topY, roofLeft + roofWidth, topY + h * 0.55f)
  drawRoundRect(
    color = v.color,
    topLeft = Offset(roofRect.left, roofRect.top),
    size = Size(roofRect.width, roofRect.height),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f * scale, 8f * scale)
  )

  // Rear Windshield (dark tinted glass with reflection)
  val glassW = roofWidth * 0.82f
  val glassH = h * 0.28f
  drawRoundRect(
    color = Color(0xFF1A2733),
    topLeft = Offset(screenX - glassW * 0.5f, topY + 4f * scale),
    size = Size(glassW, glassH),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale, 4f * scale)
  )

  // Wheels / Tires under chassis
  val tireW = w * 0.22f
  val tireH = h * 0.28f
  drawRoundRect(
    color = Color(0xFF111111),
    topLeft = Offset(left - tireW * 0.15f, groundY - tireH * 0.7f),
    size = Size(tireW, tireH),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * scale, 3f * scale)
  )
  drawRoundRect(
    color = Color(0xFF111111),
    topLeft = Offset(left + w - tireW * 0.85f, groundY - tireH * 0.7f),
    size = Size(tireW, tireH),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * scale, 3f * scale)
  )

  // Bumper & License plate
  val bumperH = h * 0.15f
  drawRect(
    color = Color(0xFF263238),
    topLeft = Offset(left, groundY - bumperH),
    size = Size(w, bumperH)
  )
  val plateW = w * 0.28f
  val plateH = bumperH * 0.7f
  drawRect(
    color = Color(0xFFFFF9C4),
    topLeft = Offset(screenX - plateW * 0.5f, groundY - bumperH + 2f * scale),
    size = Size(plateW, plateH)
  )

  // Red Taillights (with brake / normal intensity)
  val tailLightW = w * 0.20f
  val tailLightH = h * 0.14f
  val tailY = topY + h * 0.48f

  // Left taillight
  drawRoundRect(
    color = Color(0xFFFF1744),
    topLeft = Offset(left + 2f * scale, tailY),
    size = Size(tailLightW, tailLightH),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * scale, 2f * scale)
  )
  // Right taillight
  drawRoundRect(
    color = Color(0xFFFF1744),
    topLeft = Offset(left + w - tailLightW - 2f * scale, tailY),
    size = Size(tailLightW, tailLightH),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * scale, 2f * scale)
  )

  // Blinker / Turn Signal indicator if changing lanes
  if (v.blinkerState != 0 && (v.blinkerTimer % 0.4f) < 0.2f) {
    val blinkerX = if (v.blinkerState == -1) left else left + w - tailLightW
    drawRoundRect(
      color = Color(0xFFFFAB00),
      topLeft = Offset(blinkerX, tailY),
      size = Size(tailLightW, tailLightH),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * scale, 2f * scale)
    )
  }
}

private fun DrawScope.drawOncomingVehicle(
  screenX: Float,
  groundY: Float,
  w: Float,
  h: Float,
  v: TrafficVehicle,
  scale: Float
) {
  val topY = groundY - h
  val left = screenX - w * 0.5f

  // Oncoming car body
  drawRoundRect(
    color = v.color,
    topLeft = Offset(left, topY + h * 0.25f),
    size = Size(w, h * 0.75f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale, 6f * scale)
  )

  // Front Windshield
  val glassW = w * 0.75f
  drawRoundRect(
    color = Color(0xFF263238),
    topLeft = Offset(screenX - glassW * 0.5f, topY + 4f * scale),
    size = Size(glassW, h * 0.32f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale, 4f * scale)
  )

  // Front Radiator Grille
  val grilleW = w * 0.45f
  val grilleH = h * 0.18f
  drawRect(
    color = Color(0xFF111111),
    topLeft = Offset(screenX - grilleW * 0.5f, topY + h * 0.55f),
    size = Size(grilleW, grilleH)
  )

  // Bright Headlights with Lens Glow
  val lightRadius = (8f * scale).coerceAtLeast(3f)
  val lightY = topY + h * 0.58f

  // Left headlight
  drawCircle(
    color = Color(0xFFFFFDE7),
    radius = lightRadius,
    center = Offset(left + lightRadius * 1.5f, lightY)
  )
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(Color(0x99FFFFFF), Color(0x00FFFFFF)),
      center = Offset(left + lightRadius * 1.5f, lightY),
      radius = lightRadius * 3.5f
    ),
    radius = lightRadius * 3.5f,
    center = Offset(left + lightRadius * 1.5f, lightY)
  )

  // Right headlight
  drawCircle(
    color = Color(0xFFFFFDE7),
    radius = lightRadius,
    center = Offset(left + w - lightRadius * 1.5f, lightY)
  )
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(Color(0x99FFFFFF), Color(0x00FFFFFF)),
      center = Offset(left + w - lightRadius * 1.5f, lightY),
      radius = lightRadius * 3.5f
    ),
    radius = lightRadius * 3.5f,
    center = Offset(left + w - lightRadius * 1.5f, lightY)
  )
}

// ----------------------------------------------------
// 5. SPEED STREAKS & NITRO WARP
// ----------------------------------------------------
private fun DrawScope.drawSpeedStreaks(
  vanishX: Float,
  horizonY: Float,
  width: Float,
  height: Float,
  isNitro: Boolean
) {
  val streakCount = if (isNitro) 24 else 12
  val streakColor = if (isNitro) Color(0x8800E5FF) else Color(0x44FFFFFF)

  for (i in 0 until streakCount) {
    val angle = (i.toFloat() / streakCount) * 2f * PI.toFloat()
    val r1 = height * 0.25f + Random.nextFloat() * 80f
    val r2 = r1 + 80f + Random.nextFloat() * 120f

    val startX = vanishX + cos(angle) * r1
    val startY = horizonY + sin(angle) * r1 * 0.6f
    val endX = vanishX + cos(angle) * r2
    val endY = horizonY + sin(angle) * r2 * 0.6f

    drawLine(
      color = streakColor,
      start = Offset(startX, startY),
      end = Offset(endX, endY),
      strokeWidth = if (isNitro) 3.5f else 2.0f,
      cap = StrokeCap.Round
    )
  }
}

// ----------------------------------------------------
// 6. MOTORCYCLE COCKPIT OVERLAY (AUTHENTIC FIRST PERSON)
// ----------------------------------------------------
private fun DrawScope.drawMotorcycleCockpit(
  engine: GameEngine,
  bikeColor: BikeColorOption,
  textMeasurer: TextMeasurer,
  width: Float,
  height: Float
) {
  // Apply rider lean rotation centered near the bottom center of the screen
  rotate(
    degrees = engine.leanAngle * 0.65f,
    pivot = Offset(width * 0.5f, height * 0.95f)
  ) {
    val cockpitCenter = width * 0.5f
    val cockpitBaseY = height

    // --- A. Red Sports Bike Fairing / Nose Cowl (matches reference image) ---
    val isPortrait = height > width
    val fairingTopY = if (isPortrait) height * 0.53f else height * 0.56f
    val fairingWidth = if (isPortrait) width * 0.74f else width * 0.60f

    val fairingPath = Path().apply {
      moveTo(cockpitCenter - fairingWidth * 0.5f, cockpitBaseY)
      // Left shoulder curve
      cubicTo(
        cockpitCenter - fairingWidth * 0.42f, height * 0.72f,
        cockpitCenter - fairingWidth * 0.32f, fairingTopY + 30f,
        cockpitCenter - fairingWidth * 0.18f, fairingTopY
      )
      // Nose cone tip
      lineTo(cockpitCenter + fairingWidth * 0.18f, fairingTopY)
      // Right shoulder curve
      cubicTo(
        cockpitCenter + fairingWidth * 0.32f, fairingTopY + 30f,
        cockpitCenter + fairingWidth * 0.42f, height * 0.72f,
        cockpitCenter + fairingWidth * 0.5f, cockpitBaseY
      )
      close()
    }

    // Gradient on fairing for realistic 3D sports bodywork
    drawPath(
      path = fairingPath,
      brush = Brush.radialGradient(
        colors = listOf(
          bikeColor.primaryColor,
          bikeColor.secondaryColor,
          Color(0xFF8B0000)
        ),
        center = Offset(cockpitCenter, fairingTopY + 80f),
        radius = fairingWidth * 0.55f
      )
    )

    // Aerodynamic Windshield Bubble (Dark Smoked Polycarbonate)
    val screenTopY = fairingTopY - height * 0.08f
    val screenPath = Path().apply {
      moveTo(cockpitCenter - fairingWidth * 0.18f, fairingTopY)
      cubicTo(
        cockpitCenter - fairingWidth * 0.12f, screenTopY + 15f,
        cockpitCenter - fairingWidth * 0.06f, screenTopY,
        cockpitCenter, screenTopY
      )
      cubicTo(
        cockpitCenter + fairingWidth * 0.06f, screenTopY,
        cockpitCenter + fairingWidth * 0.12f, screenTopY + 15f,
        cockpitCenter + fairingWidth * 0.18f, fairingTopY
      )
      close()
    }
    drawPath(
      path = screenPath,
      brush = Brush.verticalGradient(
        colors = listOf(
          Color(0x33FFFFFF),
          Color(0xAA10141A),
          Color(0xEE0B0D11)
        ),
        startY = screenTopY,
        endY = fairingTopY
      )
    )
    drawPath(screenPath, color = Color(0x55FFFFFF), style = Stroke(width = 2.5f))

    // --- B. Dual Rearview Mirrors (Angled stalks with reflective glass) ---
    drawRearviewMirrors(engine, cockpitCenter, fairingTopY, fairingWidth, height)

    // --- C. Triple-Tree Clamp, Handlebar Fork Crowns & Key Switch ---
    val stemY = if (isPortrait) height * 0.81f else height * 0.81f
    drawRect(
      color = Color(0xFF1E222B),
      topLeft = Offset(cockpitCenter - fairingWidth * 0.32f, stemY - 18f),
      size = Size(fairingWidth * 0.64f, 36f)
    )
    // Metallic fork cap adjusters
    val forkDist = fairingWidth * 0.22f
    drawCircle(color = Color(0xFF78909C), radius = 18f, center = Offset(cockpitCenter - forkDist, stemY))
    drawCircle(color = Color(0xFFFF5252), radius = 10f, center = Offset(cockpitCenter - forkDist, stemY))
    drawCircle(color = Color(0xFF78909C), radius = 18f, center = Offset(cockpitCenter + forkDist, stemY))
    drawCircle(color = Color(0xFFFF5252), radius = 10f, center = Offset(cockpitCenter + forkDist, stemY))
    // Center steering nut
    drawCircle(color = Color(0xFFECEFF1), radius = 16f, center = Offset(cockpitCenter, stemY))
    drawCircle(color = Color(0xFF37474F), radius = 9f, center = Offset(cockpitCenter, stemY))

    // --- D. Central Instrument Cluster (Analog Tachometer & Digital Speedometer) ---
    val dashCenterY = if (isPortrait) height * 0.67f else height * 0.68f
    val dashRadius = if (isPortrait) (width * 0.22f).coerceIn(85f, 150f) else (height * 0.12f).coerceIn(90f, 160f)
    drawInstrumentCluster(engine, textMeasurer, cockpitCenter, dashCenterY, dashRadius)

    // --- E. Clip-on Handlebars, Grips & Controls ---
    val barY = if (isPortrait) height * 0.86f else height * 0.86f
    val gripWidth = if (isPortrait) width * 0.17f else width * 0.16f

    // Left Clip-on & Clutch Grip
    drawRoundRect(
      color = Color(0xFF15181E),
      topLeft = Offset(cockpitCenter - fairingWidth * 0.44f - gripWidth, barY - 14f),
      size = Size(gripWidth, 34f),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
    // Left Grip textured ribs
    for (r in 0..5) {
      val rx = cockpitCenter - fairingWidth * 0.44f - gripWidth + (r * gripWidth / 6f)
      drawLine(
        color = Color(0xFF262C38),
        start = Offset(rx, barY - 14f),
        end = Offset(rx, barY + 20f),
        strokeWidth = 2f
      )
    }

    // Right Clip-on & Throttle Grip
    val throttleTurnOffset = if (engine.isThrottleActive) 4f else 0f
    drawRoundRect(
      color = Color(0xFF15181E),
      topLeft = Offset(cockpitCenter + fairingWidth * 0.44f, barY - 14f + throttleTurnOffset),
      size = Size(gripWidth, 34f),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
    // Right Grip textured ribs
    for (r in 0..5) {
      val rx = cockpitCenter + fairingWidth * 0.44f + (r * gripWidth / 6f)
      drawLine(
        color = Color(0xFF262C38),
        start = Offset(rx, barY - 14f + throttleTurnOffset),
        end = Offset(rx, barY + 20f + throttleTurnOffset),
        strokeWidth = 2f
      )
    }

    // Brake Lever on right & Clutch Lever on left (anodized aluminum)
    drawLine(
      color = Color(0xFFCFD8DC),
      start = Offset(cockpitCenter + fairingWidth * 0.42f, barY - 16f),
      end = Offset(cockpitCenter + fairingWidth * 0.42f + gripWidth * 0.85f, barY - 28f),
      strokeWidth = 5f,
      cap = StrokeCap.Round
    )
    drawLine(
      color = Color(0xFFCFD8DC),
      start = Offset(cockpitCenter - fairingWidth * 0.42f, barY - 16f),
      end = Offset(cockpitCenter - fairingWidth * 0.42f - gripWidth * 0.85f, barY - 28f),
      strokeWidth = 5f,
      cap = StrokeCap.Round
    )

    // --- F. Animated Rider Hands / Arms (holding grips, tilting with lean) ---
    drawRiderHands(cockpitCenter, fairingWidth, gripWidth, barY, height, engine.isThrottleActive)
  }
}

// ----------------------------------------------------
// DUAL REARVIEW MIRRORS
// ----------------------------------------------------
private fun DrawScope.drawRearviewMirrors(
  engine: GameEngine,
  cockpitCenter: Float,
  fairingTopY: Float,
  fairingWidth: Float,
  height: Float
) {
  val stalkLength = fairingWidth * 0.35f
  val mirrorW = (fairingWidth * 0.28f).coerceIn(65f, 150f)
  val mirrorH = mirrorW * 0.52f // Sleek horizontal aerodynamic mirror!

  // Left Mirror Stalk
  val leftMirrorCenterX = cockpitCenter - fairingWidth * 0.24f - stalkLength * 0.7f
  val leftMirrorCenterY = fairingTopY - mirrorH * 0.4f
  drawLine(
    color = Color(0xFF1E222B),
    start = Offset(cockpitCenter - fairingWidth * 0.16f, fairingTopY + 12f),
    end = Offset(leftMirrorCenterX + mirrorW * 0.2f, leftMirrorCenterY),
    strokeWidth = 8f,
    cap = StrokeCap.Round
  )

  // Left Mirror Housing & Glass
  drawRoundRect(
    color = Color(0xFF12151B),
    topLeft = Offset(leftMirrorCenterX - mirrorW * 0.5f, leftMirrorCenterY - mirrorH * 0.5f),
    size = Size(mirrorW, mirrorH),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
  )
  // Mirror Reflective Glass (reflects receding highway & cars behind!)
  drawRoundRect(
    brush = Brush.verticalGradient(
      colors = listOf(
        Color(0xFFFFB74D), // Sunset sky reflection
        Color(0xFF546E7A), // Horizon
        Color(0xFF263238)  // Receding asphalt
      ),
      startY = leftMirrorCenterY - mirrorH * 0.4f,
      endY = leftMirrorCenterY + mirrorH * 0.4f
    ),
    topLeft = Offset(leftMirrorCenterX - mirrorW * 0.45f, leftMirrorCenterY - mirrorH * 0.4f),
    size = Size(mirrorW * 0.9f, mirrorH * 0.8f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
  )
  // Simulated car in left mirror
  drawRect(
    color = Color(0xFFE53935),
    topLeft = Offset(leftMirrorCenterX - 10f, leftMirrorCenterY - 4f),
    size = Size(16f, 10f)
  )

  // Right Mirror Stalk
  val rightMirrorCenterX = cockpitCenter + fairingWidth * 0.24f + stalkLength * 0.7f
  val rightMirrorCenterY = fairingTopY - mirrorH * 0.4f
  drawLine(
    color = Color(0xFF1E222B),
    start = Offset(cockpitCenter + fairingWidth * 0.16f, fairingTopY + 12f),
    end = Offset(rightMirrorCenterX - mirrorW * 0.2f, rightMirrorCenterY),
    strokeWidth = 8f,
    cap = StrokeCap.Round
  )

  // Right Mirror Housing & Glass
  drawRoundRect(
    color = Color(0xFF12151B),
    topLeft = Offset(rightMirrorCenterX - mirrorW * 0.5f, rightMirrorCenterY - mirrorH * 0.5f),
    size = Size(mirrorW, mirrorH),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
  )
  drawRoundRect(
    brush = Brush.verticalGradient(
      colors = listOf(
        Color(0xFFFFB74D),
        Color(0xFF546E7A),
        Color(0xFF263238)
      ),
      startY = rightMirrorCenterY - mirrorH * 0.4f,
      endY = rightMirrorCenterY + mirrorH * 0.4f
    ),
    topLeft = Offset(rightMirrorCenterX - mirrorW * 0.45f, rightMirrorCenterY - mirrorH * 0.4f),
    size = Size(mirrorW * 0.9f, mirrorH * 0.8f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
  )
  // Simulated car in right mirror
  drawRect(
    color = Color(0xFFFDD835),
    topLeft = Offset(rightMirrorCenterX + 4f, rightMirrorCenterY - 6f),
    size = Size(16f, 10f)
  )
}

// ----------------------------------------------------
// CENTRAL INSTRUMENT CLUSTER
// ----------------------------------------------------
private fun DrawScope.drawInstrumentCluster(
  engine: GameEngine,
  textMeasurer: TextMeasurer,
  centerX: Float,
  centerY: Float,
  radius: Float
) {
  // Bezel surround (Brushed Dark Aluminum)
  drawCircle(
    color = Color(0xFF181B22),
    radius = radius + 8f,
    center = Offset(centerX, centerY)
  )
  drawCircle(
    color = Color(0xFF282D38),
    radius = radius + 3f,
    center = Offset(centerX, centerY),
    style = Stroke(width = 4f)
  )

  // Dial face (Black with subtle tachometer glow)
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(Color(0xFF101319), Color(0xFF07080B)),
      center = Offset(centerX, centerY),
      radius = radius
    ),
    radius = radius,
    center = Offset(centerX, centerY)
  )

  // Tachometer Dial Ticks (from 0 to 14,000 RPM across 240 degrees)
  val startAngleDeg = 145f
  val sweepAngleDeg = 250f
  val maxRpm = 14000f

  for (rpm in 0..14) {
    val progress = rpm / 14f
    val tickAngleDeg = startAngleDeg + progress * sweepAngleDeg
    val tickAngleRad = (tickAngleDeg * PI / 180f).toFloat()

    val isRedline = rpm >= 11
    val tickColor = if (isRedline) Color(0xFFFF1744) else Color(0xFFE0E0E0)
    val tickLen = if (rpm % 2 == 0) radius * 0.18f else radius * 0.10f

    val innerR = radius - tickLen - 5f
    val outerR = radius - 5f

    val startX = centerX + cos(tickAngleRad) * innerR
    val startY = centerY + sin(tickAngleRad) * innerR
    val endX = centerX + cos(tickAngleRad) * outerR
    val endY = centerY + sin(tickAngleRad) * outerR

    drawLine(
      color = tickColor,
      start = Offset(startX, startY),
      end = Offset(endX, endY),
      strokeWidth = if (rpm % 2 == 0) 3.0f else 1.8f,
      cap = StrokeCap.Round
    )
  }

  // Redline zone arc
  drawArc(
    color = Color(0x99FF1744),
    startAngle = startAngleDeg + (11f / 14f) * sweepAngleDeg,
    sweepAngle = (3f / 14f) * sweepAngleDeg,
    useCenter = false,
    topLeft = Offset(centerX - radius + 8f, centerY - radius + 8f),
    size = Size((radius - 8f) * 2f, (radius - 8f) * 2f),
    style = Stroke(width = 4f)
  )

  // Sweeping Tachometer Needle (Red needle)
  val rpmRatio = (engine.engineRpm / maxRpm).coerceIn(0f, 1f)
  val needleAngleDeg = startAngleDeg + rpmRatio * sweepAngleDeg
  val needleAngleRad = (needleAngleDeg * PI / 180f).toFloat()
  val needleLength = radius * 0.74f

  val needleTipX = centerX + cos(needleAngleRad) * needleLength
  val needleTipY = centerY + sin(needleAngleRad) * needleLength

  drawLine(
    color = Color(0xFFFF1744),
    start = Offset(centerX, centerY),
    end = Offset(needleTipX, needleTipY),
    strokeWidth = 3.5f,
    cap = StrokeCap.Round
  )
  // Needle center hub
  drawCircle(color = Color(0xFFCFD8DC), radius = 8f, center = Offset(centerX, centerY))
  drawCircle(color = Color(0xFF263238), radius = 4f, center = Offset(centerX, centerY))

  // Gear Indicator Badge (Upper area: N, 1, 2, 3...)
  val gearText = if (engine.speedKmh < 1f) "N" else engine.currentGear.toString()
  val gearColor = if (gearText == "N") Color(0xFF00E676) else Color(0xFFFFD600)
  val gearMeasure = textMeasurer.measure(
    text = "GEAR $gearText",
    style = TextStyle(
      fontSize = (radius * 0.14f).sp,
      fontWeight = FontWeight.ExtraBold,
      color = gearColor
    )
  )
  drawText(
    textLayoutResult = gearMeasure,
    topLeft = Offset(centerX - gearMeasure.size.width * 0.5f, centerY - radius * 0.52f)
  )

  // Digital Speed Display in center cluster (Cleanly centered above KM/H without overlap!)
  val speedText = engine.speedKmh.toInt().toString()
  val speedMeasure = textMeasurer.measure(
    text = speedText,
    style = TextStyle(
      fontSize = (radius * 0.32f).sp,
      fontWeight = FontWeight.Black,
      fontFamily = FontFamily.SansSerif,
      color = Color(0xFF00E5FF)
    )
  )
  drawText(
    textLayoutResult = speedMeasure,
    topLeft = Offset(centerX - speedMeasure.size.width * 0.5f, centerY - radius * 0.08f)
  )

  // "KM/H" Unit label (Cleanly placed below the speed digits)
  val kmhMeasure = textMeasurer.measure(
    text = "KM/H",
    style = TextStyle(
      fontSize = (radius * 0.12f).sp,
      fontWeight = FontWeight.Bold,
      color = Color(0xFF90A4AE)
    )
  )
  drawText(
    textLayoutResult = kmhMeasure,
    topLeft = Offset(centerX - kmhMeasure.size.width * 0.5f, centerY + radius * 0.30f)
  )
}

// ----------------------------------------------------
// ANIMATED RIDER HANDS
// ----------------------------------------------------
private fun DrawScope.drawRiderHands(
  cockpitCenter: Float,
  fairingWidth: Float,
  gripWidth: Float,
  barY: Float,
  height: Float,
  isThrottle: Boolean
) {
  val handWidth = gripWidth * 0.95f
  val handHeight = height * 0.14f

  // Left Hand & Forearm (gripping left handlebar)
  val leftHandX = cockpitCenter - fairingWidth * 0.44f - gripWidth * 0.65f
  val leftHandY = barY + 6f

  // Arm sleeve / forearm
  val leftArmPath = Path().apply {
    moveTo(leftHandX - 25f, height)
    lineTo(leftHandX - 10f, leftHandY + 20f)
    lineTo(leftHandX + handWidth * 0.4f, leftHandY + 20f)
    lineTo(leftHandX + 10f, height)
    close()
  }
  drawPath(
    path = leftArmPath,
    color = Color(0xFF212121) // Black leather rider jacket
  )
  // Left Hand / Glove
  drawRoundRect(
    color = Color(0xFFFFCCBC), // Natural skin / racing glove
    topLeft = Offset(leftHandX - handWidth * 0.5f, leftHandY - handHeight * 0.4f),
    size = Size(handWidth, handHeight * 0.7f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
  )
  // Glove knuckles
  drawRoundRect(
    color = Color(0xFFD84315),
    topLeft = Offset(leftHandX - handWidth * 0.4f, leftHandY - handHeight * 0.28f),
    size = Size(handWidth * 0.8f, 16f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
  )

  // Right Hand & Forearm (gripping throttle on right handlebar)
  val throttleShift = if (isThrottle) 5f else 0f
  val rightHandX = cockpitCenter + fairingWidth * 0.44f + gripWidth * 0.65f
  val rightHandY = barY + 6f + throttleShift

  // Right Arm sleeve
  val rightArmPath = Path().apply {
    moveTo(rightHandX - 10f, height)
    lineTo(rightHandX - handWidth * 0.4f, rightHandY + 20f)
    lineTo(rightHandX + 10f, rightHandY + 20f)
    lineTo(rightHandX + 25f, height)
    close()
  }
  drawPath(
    path = rightArmPath,
    color = Color(0xFF212121)
  )
  // Right Hand / Glove
  drawRoundRect(
    color = Color(0xFFFFCCBC),
    topLeft = Offset(rightHandX - handWidth * 0.5f, rightHandY - handHeight * 0.4f),
    size = Size(handWidth, handHeight * 0.7f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
  )
  // Glove knuckles
  drawRoundRect(
    color = Color(0xFFD84315),
    topLeft = Offset(rightHandX - handWidth * 0.4f, rightHandY - handHeight * 0.28f),
    size = Size(handWidth * 0.8f, 16f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
  )
}
