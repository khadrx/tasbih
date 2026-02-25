package com.khadr.tasbih.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val bg           : Color,
    val surface      : Color,
    val textPrimary  : Color,
    val textSecondary: Color,
    val line         : Color,
    val btn          : Color,
    val btnText      : Color,
    val danger       : Color,
    val success      : Color,
    val isDark       : Boolean
)

val LightColors = AppColors(
    bg            = Color(0xFFF5F0E8),
    surface       = Color(0xFFEDE8DF),
    textPrimary   = Color(0xFF2C2416),
    textSecondary = Color(0xFF8C7A60),
    line          = Color(0xFFC9BBA0),
    btn           = Color(0xFF2C2416),
    btnText       = Color(0xFFF5F0E8),
    danger        = Color(0xFF8B3A2A),
    success       = Color(0xFF3A6B40),
    isDark        = false
)

val DarkColors = AppColors(
    bg            = Color(0xFF18140F),
    surface       = Color(0xFF242018),
    textPrimary   = Color(0xFFF0E8D8),
    textSecondary = Color(0xFF8C7A60),
    line          = Color(0xFF3A3028),
    btn           = Color(0xFFC9BBA0),
    btnText       = Color(0xFF18140F),
    danger        = Color(0xFFB05040),
    success       = Color(0xFF5A9B62),
    isDark        = true
)

val LocalColors = staticCompositionLocalOf { LightColors }