package com.khadr.tasbih.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class PrayerTimes(
    val fajr   : String,
    val dhuhr  : String,
    val asr    : String,
    val maghrib: String,
    val isha   : String
)

data class PrayerTimeEntry(
    val nameAr  : String,
    val nameKey : String,   // matches API key
    val time    : String
)

fun PrayerTimes.toList() = listOf(
    PrayerTimeEntry("الفجر",   "Fajr",    fajr),
    PrayerTimeEntry("الظهر",   "Dhuhr",   dhuhr),
    PrayerTimeEntry("العصر",   "Asr",     asr),
    PrayerTimeEntry("المغرب",  "Maghrib", maghrib),
    PrayerTimeEntry("العشاء",  "Isha",    isha)
)

// Parse "HH:mm" → Pair(hour, minute)
fun String.toHourMinute(): Pair<Int, Int> {
    val parts = split(":")
    return Pair(parts[0].trim().toInt(), parts[1].trim().take(2).toInt())
}

// ── Aladhan API: free, no key needed ──
// Docs: https://aladhan.com/prayer-times-api
suspend fun fetchPrayerTimes(city: String, country: String, method: Int = 5): Result<PrayerTimes> =
    withContext(Dispatchers.IO) {
        runCatching {
            val url = "https://api.aladhan.com/v1/timingsByCity" +
                    "?city=${city.trim().replace(" ", "%20")}" +
                    "&country=${country.trim().replace(" ", "%20")}" +
                    "&method=$method"

            val response = URL(url).readText()
            val json     = JSONObject(response)

            check(json.getString("code") == "200") { "API error: ${json.getString("status")}" }

            val timings = json.getJSONObject("data").getJSONObject("timings")

            PrayerTimes(
                fajr    = timings.getString("Fajr"),
                dhuhr   = timings.getString("Dhuhr"),
                asr     = timings.getString("Asr"),
                maghrib = timings.getString("Maghrib"),
                isha    = timings.getString("Isha")
            )
        }
    }