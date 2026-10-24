package com.example.game.data

import android.content.Context
import android.content.SharedPreferences

class GamePreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("smk_game_prefs", Context.MODE_PRIVATE)

  var coins: Int
    get() = prefs.getInt(KEY_COINS, 99999)
    set(value) = prefs.edit().putInt(KEY_COINS, value).apply()

  var selectedBikeId: String
    get() = prefs.getString(KEY_SELECTED_BIKE, "smk_inferno_600") ?: "smk_inferno_600"
    set(value) = prefs.edit().putString(KEY_SELECTED_BIKE, value).apply()

  var selectedColorId: String
    get() = prefs.getString(KEY_SELECTED_COLOR, "crimson_red") ?: "crimson_red"
    set(value) = prefs.edit().putString(KEY_SELECTED_COLOR, value).apply()

  fun isLevelUnlocked(levelId: Int): Boolean {
    return true // All levels 100% free and unlocked!
  }

  fun unlockLevel(levelId: Int) {
    prefs.edit().putBoolean(KEY_LEVEL_UNLOCKED_PREFIX + levelId, true).apply()
  }

  fun getLevelStars(levelId: Int): Int {
    return prefs.getInt(KEY_LEVEL_STARS_PREFIX + levelId, 0)
  }

  fun saveLevelStars(levelId: Int, stars: Int) {
    val current = getLevelStars(levelId)
    if (stars > current) {
      prefs.edit().putInt(KEY_LEVEL_STARS_PREFIX + levelId, stars).apply()
    }
  }

  fun getTotalStars(): Int {
    var total = 0
    for (i in 1..4) {
      total += getLevelStars(i)
    }
    return total
  }

  fun getLevelHighScore(levelId: Int): Int {
    return prefs.getInt(KEY_LEVEL_SCORE_PREFIX + levelId, 0)
  }

  fun saveLevelScore(levelId: Int, score: Int) {
    val current = getLevelHighScore(levelId)
    if (score > current) {
      prefs.edit().putInt(KEY_LEVEL_SCORE_PREFIX + levelId, score).apply()
    }
  }

  fun isBikeUnlocked(bikeId: String): Boolean {
    return true // All bikes 100% free and unlocked!
  }

  fun unlockBike(bikeId: String) {
    prefs.edit().putBoolean(KEY_BIKE_UNLOCKED_PREFIX + bikeId, true).apply()
  }

  fun getUpgradeLevel(bikeId: String, stat: String): Int {
    return prefs.getInt("upgrade_${bikeId}_$stat", 0)
  }

  fun incrementUpgrade(bikeId: String, stat: String) {
    val current = getUpgradeLevel(bikeId, stat)
    prefs.edit().putInt("upgrade_${bikeId}_$stat", current + 1).apply()
  }

  var isSoundEnabled: Boolean
    get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
    set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

  var isVibrationEnabled: Boolean
    get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
    set(value) = prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()

  companion object {
    private const val KEY_COINS = "player_coins"
    private const val KEY_SELECTED_BIKE = "selected_bike"
    private const val KEY_SELECTED_COLOR = "selected_color"
    private const val KEY_LEVEL_UNLOCKED_PREFIX = "level_unlocked_"
    private const val KEY_LEVEL_STARS_PREFIX = "level_stars_"
    private const val KEY_LEVEL_SCORE_PREFIX = "level_score_"
    private const val KEY_BIKE_UNLOCKED_PREFIX = "bike_unlocked_"
    private const val KEY_SOUND_ENABLED = "setting_sound"
    private const val KEY_VIBRATION_ENABLED = "setting_vibration"
  }
}
