package com.khadr.tasbih.utils

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission

object HapticManager {

    // ── Patterns ──
    // Format: [delay, vibrate, pause, vibrate, ...] in ms

    // كل تسبيحة: نبضة خفيفة قصيرة
    private val TICK_PATTERN     = longArrayOf(0, 30)

    // اكتمال خطوة: 3 نبضات واضحة — يعرف إنه انتقل
    private val STEP_DONE_PATTERN = longArrayOf(0, 60, 80, 60, 80, 60)

    // اكتمال كل الأذكار: نبضة طويلة ثم قصيرتين — احتفالية
    private val ALL_DONE_PATTERN  = longArrayOf(0, 200, 100, 80, 60, 80)

    // خطأ/نقصان: نبضة واحدة ناعمة مختلفة
    private val UNDO_PATTERN      = longArrayOf(0, 20)

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrate(context: Context, pattern: LongArray, amplitude: Int = -1) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = if (amplitude == -1) {
                VibrationEffect.createWaveform(pattern, -1)
            } else {
                // Clamp amplitude to valid range
                val amps = pattern.mapIndexed { i, _ ->
                    if (i % 2 == 0) 0 else amplitude.coerceIn(1, 255)
                }.toIntArray()
                VibrationEffect.createWaveform(pattern, amps, -1)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    /** نبضة لكل تسبيحة — خفيفة جداً */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun tick(context: Context) = vibrate(context, TICK_PATTERN, 60)

    /** اكتمال خطوة — 3 نبضات متوسطة */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun stepDone(context: Context) = vibrate(context, STEP_DONE_PATTERN, 180)

    /** اكتمال كل الأذكار — نبضة احتفالية */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun allDone(context: Context) = vibrate(context, ALL_DONE_PATTERN, 220)

    /** نقصان / تراجع — نبضة ناعمة مختلفة */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun undo(context: Context) = vibrate(context, UNDO_PATTERN, 40)
}