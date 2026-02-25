package com.khadr.tasbih.utils

import android.content.Context
import android.content.SharedPreferences

private const val PREFS_NAME        = "tasbih_settings"
private const val KEY_CITY          = "city"
private const val KEY_COUNTRY       = "country"
private const val KEY_ENABLE_BEFORE = "enable_before"
private const val KEY_ENABLE_AFTER  = "enable_after"
private const val KEY_BEFORE_MIN    = "before_minutes"
private const val KEY_AFTER_MIN     = "after_minutes"
private const val KEY_PRAYER_FAJR   = "prayer_fajr"
private const val KEY_PRAYER_DHUHR  = "prayer_dhuhr"
private const val KEY_PRAYER_ASR    = "prayer_asr"
private const val KEY_PRAYER_MAGHRIB= "prayer_maghrib"
private const val KEY_PRAYER_ISHA   = "prayer_isha"
private const val KEY_NOTIF_ACTIVE  = "notifications_active"
private const val KEY_HAPTIC_ENABLED= "haptic_enabled"
private const val KEY_HAPTIC_TICK   = "haptic_tick"     // vibrate on every count?

data class NotifSettings(
    val city          : String       = "Cairo",
    val country       : String       = "Egypt",
    val enableBefore  : Boolean      = false,
    val enableAfter   : Boolean      = false,
    val beforeMinutes : Int          = 15,
    val afterMinutes  : Int          = 10,
    val prayerTimes   : PrayerTimes? = null,
    val notifsActive  : Boolean      = false,
    val hapticEnabled : Boolean      = true,   // vibrate on step complete
    val hapticTick    : Boolean      = false   // vibrate on every single count
)

private fun Context.prefs(): SharedPreferences =
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

fun Context.saveNotifSettings(settings: NotifSettings) {
    prefs().edit().apply {
        putString(KEY_CITY,           settings.city)
        putString(KEY_COUNTRY,        settings.country)
        putBoolean(KEY_ENABLE_BEFORE, settings.enableBefore)
        putBoolean(KEY_ENABLE_AFTER,  settings.enableAfter)
        putInt(KEY_BEFORE_MIN,        settings.beforeMinutes)
        putInt(KEY_AFTER_MIN,         settings.afterMinutes)
        putBoolean(KEY_NOTIF_ACTIVE,  settings.notifsActive)
        putBoolean(KEY_HAPTIC_ENABLED,settings.hapticEnabled)
        putBoolean(KEY_HAPTIC_TICK,   settings.hapticTick)
        settings.prayerTimes?.let { pt ->
            putString(KEY_PRAYER_FAJR,    pt.fajr)
            putString(KEY_PRAYER_DHUHR,   pt.dhuhr)
            putString(KEY_PRAYER_ASR,     pt.asr)
            putString(KEY_PRAYER_MAGHRIB, pt.maghrib)
            putString(KEY_PRAYER_ISHA,    pt.isha)
        }
        apply()
    }
}

fun Context.loadNotifSettings(): NotifSettings {
    val p = prefs()
    val hasPrayerTimes = p.contains(KEY_PRAYER_FAJR)
    val prayerTimes = if (hasPrayerTimes) PrayerTimes(
        fajr    = p.getString(KEY_PRAYER_FAJR,    "00:00") ?: "00:00",
        dhuhr   = p.getString(KEY_PRAYER_DHUHR,   "00:00") ?: "00:00",
        asr     = p.getString(KEY_PRAYER_ASR,     "00:00") ?: "00:00",
        maghrib = p.getString(KEY_PRAYER_MAGHRIB, "00:00") ?: "00:00",
        isha    = p.getString(KEY_PRAYER_ISHA,    "00:00") ?: "00:00"
    ) else null

    return NotifSettings(
        city          = p.getString(KEY_CITY,    "Cairo") ?: "Cairo",
        country       = p.getString(KEY_COUNTRY, "Egypt") ?: "Egypt",
        enableBefore  = p.getBoolean(KEY_ENABLE_BEFORE, false),
        enableAfter   = p.getBoolean(KEY_ENABLE_AFTER,  false),
        beforeMinutes = p.getInt(KEY_BEFORE_MIN, 15),
        afterMinutes  = p.getInt(KEY_AFTER_MIN,  10),
        prayerTimes   = prayerTimes,
        notifsActive  = p.getBoolean(KEY_NOTIF_ACTIVE,   false),
        hapticEnabled = p.getBoolean(KEY_HAPTIC_ENABLED, true),
        hapticTick    = p.getBoolean(KEY_HAPTIC_TICK,    false)
    )
}

// ══════════════════════════════════════════════
//  Dhikr list persistence
// ══════════════════════════════════════════════
private const val KEY_DHIKR_COUNT = "dhikr_count"
private const val KEY_DHIKR_NAME  = "dhikr_name_"   // + index
private const val KEY_DHIKR_TARGET= "dhikr_target_" // + index

fun Context.saveDhikrList(items: List<com.khadr.tasbih.data.Dhikr>) {
    prefs().edit().apply {
        putInt(KEY_DHIKR_COUNT, items.size)
        items.forEachIndexed { i, dhikr ->
            putString("$KEY_DHIKR_NAME$i",  dhikr.name)
            putInt("$KEY_DHIKR_TARGET$i",   dhikr.target)
        }
        apply()
    }
}

fun Context.loadDhikrList(): List<com.khadr.tasbih.data.Dhikr>? {
    val p     = prefs()
    val count = p.getInt(KEY_DHIKR_COUNT, -1)
    if (count == -1) return null   // never saved → use default
    return (0 until count).map { i ->
        com.khadr.tasbih.data.Dhikr(
            name   = p.getString("$KEY_DHIKR_NAME$i",  "") ?: "",
            target = p.getInt("$KEY_DHIKR_TARGET$i",  33)
        )
    }.filter { it.name.isNotBlank() }
}

// ══════════════════════════════════════════════
//  Counter session persistence
// ══════════════════════════════════════════════
private const val KEY_SESSION_INDEX    = "session_index"
private const val KEY_SESSION_FINISHED = "session_finished"
private const val KEY_SESSION_COUNTS   = "session_counts_"   // + step index

data class CounterSession(
    val dhikrIndex : Int,
    val finished   : Boolean,
    val stepCounts : IntArray
)

fun Context.saveCounterSession(session: CounterSession) {
    prefs().edit().apply {
        putInt(KEY_SESSION_INDEX,    session.dhikrIndex)
        putBoolean(KEY_SESSION_FINISHED, session.finished)
        session.stepCounts.forEachIndexed { i, v ->
            putInt("$KEY_SESSION_COUNTS$i", v)
        }
        putInt("${KEY_SESSION_COUNTS}size", session.stepCounts.size)
        apply()
    }
}

fun Context.loadCounterSession(expectedSize: Int): CounterSession? {
    val p    = prefs()
    val size = p.getInt("${KEY_SESSION_COUNTS}size", -1)
    if (size != expectedSize) return null   // dhikr list changed → discard
    return CounterSession(
        dhikrIndex = p.getInt(KEY_SESSION_INDEX, 0),
        finished   = p.getBoolean(KEY_SESSION_FINISHED, false),
        stepCounts = IntArray(size) { i -> p.getInt("$KEY_SESSION_COUNTS$i", 0) }
    )
}

fun Context.clearCounterSession() {
    val p = prefs()
    val size = p.getInt("${KEY_SESSION_COUNTS}size", 0)
    prefs().edit().apply {
        remove(KEY_SESSION_INDEX)
        remove(KEY_SESSION_FINISHED)
        remove("${KEY_SESSION_COUNTS}size")
        repeat(size) { i -> remove("$KEY_SESSION_COUNTS$i") }
        apply()
    }
}

// ══════════════════════════════════════════════
//  Custom presets persistence
// ══════════════════════════════════════════════
private const val KEY_CUSTOM_PRESET_COUNT  = "custom_preset_count"
private const val KEY_CUSTOM_PRESET_TITLE  = "custom_preset_title_"
private const val KEY_CUSTOM_PRESET_SIZE   = "custom_preset_size_"
private const val KEY_CUSTOM_PRESET_NAME   = "custom_preset_name_"   // _presetIdx_itemIdx
private const val KEY_CUSTOM_PRESET_TARGET = "custom_preset_target_" // _presetIdx_itemIdx

data class CustomPreset(
    val title: String,
    val items: List<com.khadr.tasbih.data.Dhikr>
)

fun Context.saveCustomPresets(presets: List<CustomPreset>) {
    prefs().edit().apply {
        putInt(KEY_CUSTOM_PRESET_COUNT, presets.size)
        presets.forEachIndexed { pi, preset ->
            putString("$KEY_CUSTOM_PRESET_TITLE$pi",  preset.title)
            putInt("$KEY_CUSTOM_PRESET_SIZE$pi",      preset.items.size)
            preset.items.forEachIndexed { ii, dhikr ->
                putString("$KEY_CUSTOM_PRESET_NAME${pi}_$ii",   dhikr.name)
                putInt("$KEY_CUSTOM_PRESET_TARGET${pi}_$ii",    dhikr.target)
            }
        }
        apply()
    }
}

fun Context.loadCustomPresets(): List<CustomPreset> {
    val p     = prefs()
    val count = p.getInt(KEY_CUSTOM_PRESET_COUNT, 0)
    return (0 until count).map { pi ->
        val title = p.getString("$KEY_CUSTOM_PRESET_TITLE$pi", "") ?: ""
        val size  = p.getInt("$KEY_CUSTOM_PRESET_SIZE$pi", 0)
        val items = (0 until size).map { ii ->
            com.khadr.tasbih.data.Dhikr(
                name   = p.getString("$KEY_CUSTOM_PRESET_NAME${pi}_$ii", "") ?: "",
                target = p.getInt("$KEY_CUSTOM_PRESET_TARGET${pi}_$ii", 33)
            )
        }.filter { it.name.isNotBlank() }
        CustomPreset(title, items)
    }.filter { it.title.isNotBlank() }
}