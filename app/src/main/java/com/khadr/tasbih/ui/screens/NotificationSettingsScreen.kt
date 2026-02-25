package com.khadr.tasbih.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.composables.icons.lucide.*
import com.khadr.tasbih.ui.components.IconCircle
import com.khadr.tasbih.ui.components.PillButton
import com.khadr.tasbih.ui.theme.AppColors
import com.khadr.tasbih.ui.theme.LocalColors
import com.khadr.tasbih.utils.*
import kotlinx.coroutines.launch

@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val c       = LocalColors.current
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    BackHandler { onBack() }

    // ── Load saved settings on first open ──
    val saved = remember { context.loadNotifSettings() }

    var city          by remember { mutableStateOf(saved.city) }
    var country       by remember { mutableStateOf(saved.country) }
    var enableBefore  by remember { mutableStateOf(saved.enableBefore) }
    var enableAfter   by remember { mutableStateOf(saved.enableAfter) }
    var beforeMinutes by remember { mutableStateOf(saved.beforeMinutes.toString()) }
    var afterMinutes  by remember { mutableStateOf(saved.afterMinutes.toString()) }
    var prayerTimes   by remember { mutableStateOf(saved.prayerTimes) }
    var notifsActive  by remember { mutableStateOf(saved.notifsActive) }
    var hapticEnabled by remember { mutableStateOf(saved.hapticEnabled) }
    var hapticTick    by remember { mutableStateOf(saved.hapticTick) }

    var isLoading  by remember { mutableStateOf(false) }
    var errorMsg   by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf(
        if (saved.notifsActive) "الإشعارات مفعّلة ✓" else ""
    )}

    // ── Notification permission ──
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED
            else true
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    fun fetchAndSchedule() {
        if (!hasPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        if (city.isBlank() || country.isBlank()) { errorMsg = "أدخل المدينة والدولة"; return }

        isLoading  = true
        errorMsg   = ""
        successMsg = ""

        scope.launch {
            fetchPrayerTimes(city, country).fold(
                onSuccess = { times ->
                    prayerTimes = times
                    val bMin = if (enableBefore) (beforeMinutes.toIntOrNull() ?: 15) else 0
                    val aMin = if (enableAfter)  (afterMinutes.toIntOrNull()  ?: 10) else 0

                    scheduleAllPrayerAlarms(context, times, bMin, aMin)

                    notifsActive = true
                    successMsg   = "تم جدولة الإشعارات ✓"

                    // ── Save everything to prefs ──
                    context.saveNotifSettings(
                        NotifSettings(
                            city          = city,
                            country       = country,
                            enableBefore  = enableBefore,
                            enableAfter   = enableAfter,
                            beforeMinutes = beforeMinutes.toIntOrNull() ?: 15,
                            afterMinutes  = afterMinutes.toIntOrNull()  ?: 10,
                            prayerTimes   = times,
                            notifsActive  = true,
                            hapticEnabled = hapticEnabled,
                            hapticTick    = hapticTick
                        )
                    )
                },
                onFailure = { e ->
                    errorMsg = "فشل الجلب: ${e.message?.take(60)}"
                }
            )
            isLoading = false
        }
    }

    fun cancelAll() {
        cancelAllPrayerAlarms(context)
        notifsActive = false
        successMsg   = ""
        errorMsg     = ""
        // ── Save cancelled state ──
        context.saveNotifSettings(
            context.loadNotifSettings().copy(notifsActive = false)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .verticalScroll(rememberScrollState())
            .padding(top = 52.dp, start = 20.dp, end = 20.dp, bottom = 40.dp)
    ) {
        // ── Header ──
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconCircle(Lucide.ArrowRight, c) { onBack() }
            Text("إشعارات الصلاة", fontSize = 20.sp, fontFamily = FontFamily.Serif, color = c.textPrimary)
            Spacer(Modifier.width(40.dp))
        }

        Spacer(Modifier.height(28.dp))
        HorizontalDivider(color = c.line)
        Spacer(Modifier.height(20.dp))

        // ── Active badge ──
        if (notifsActive) {
            InfoBox("الإشعارات مفعّلة حالياً", c.success.copy(alpha = 0.12f), c.success, Lucide.BellRing)
            Spacer(Modifier.height(16.dp))
        }

        // ── Location ──
        SectionTitle("الموقع", c.textSecondary)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InputField(value = city,    placeholder = "المدينة", modifier = Modifier.weight(1f)) { city    = it }
            InputField(value = country, placeholder = "الدولة",  modifier = Modifier.weight(1f)) { country = it }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = c.line)
        Spacer(Modifier.height(20.dp))

        // ── Before prayer ──
        SectionTitle("قبل الصلاة", c.textSecondary)
        Spacer(Modifier.height(10.dp))
        ToggleRow("تذكير قبل الصلاة", enableBefore, c) { enableBefore = it }
        AnimatedVisibility(visible = enableBefore) {
            Column {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("قبل الصلاة بـ", fontSize = 14.sp, fontFamily = FontFamily.Serif, color = c.textPrimary)
                    InputField(value = beforeMinutes, placeholder = "دقيقة", modifier = Modifier.width(72.dp), isNumber = true) { beforeMinutes = it }
                    Text("دقيقة", fontSize = 14.sp, fontFamily = FontFamily.Serif, color = c.textPrimary)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = c.line)
        Spacer(Modifier.height(20.dp))

        // ── After prayer ──
        SectionTitle("بعد الصلاة", c.textSecondary)
        Spacer(Modifier.height(10.dp))
        ToggleRow("تذكير بالتسبيح بعد الصلاة", enableAfter, c) { enableAfter = it }
        AnimatedVisibility(visible = enableAfter) {
            Column {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("بعد الصلاة بـ", fontSize = 14.sp, fontFamily = FontFamily.Serif, color = c.textPrimary)
                    InputField(value = afterMinutes, placeholder = "دقيقة", modifier = Modifier.width(72.dp), isNumber = true) { afterMinutes = it }
                    Text("دقيقة", fontSize = 14.sp, fontFamily = FontFamily.Serif, color = c.textPrimary)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = c.line)
        Spacer(Modifier.height(20.dp))

        // ── Haptic / Vibration ──
        SectionTitle("الاهتزاز", c.textSecondary)
        Spacer(Modifier.height(10.dp))
        ToggleRow("اهتزاز عند اكتمال كل ذكر", hapticEnabled, c) {
            hapticEnabled = it
            context.saveNotifSettings(context.loadNotifSettings().copy(hapticEnabled = it, hapticTick = hapticTick))
        }
        AnimatedVisibility(visible = hapticEnabled) {
            Column {
                Spacer(Modifier.height(10.dp))
                ToggleRow("اهتزاز خفيف مع كل تسبيحة", hapticTick, c) {
                    hapticTick = it
                    context.saveNotifSettings(context.loadNotifSettings().copy(hapticTick = it, hapticEnabled = hapticEnabled))
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InfoBox("يحتاج التطبيق إذن الإشعارات", c.danger.copy(alpha = 0.12f), c.danger)
            Spacer(Modifier.height(12.dp))
        }

        // ── Actions ──
        PillButton(
            label    = if (isLoading) "جاري الجلب..." else "جلب المواقيت وتفعيل الإشعارات",
            width    = 340, height = 56, fontSize = 15,
            onClick  = { if (!isLoading) fetchAndSchedule() }
        )
        Spacer(Modifier.height(10.dp))
        PillButton(
            label    = "إلغاء جميع الإشعارات",
            width    = 240, height = 48, color = c.danger, fontSize = 15,
            onClick  = { cancelAll() }
        )

        // ── Status messages ──
        if (errorMsg.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            InfoBox(errorMsg, c.danger.copy(alpha = 0.1f), c.danger)
        }
        if (successMsg.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            InfoBox(successMsg, c.success.copy(alpha = 0.1f), c.success)
        }

        // ── Prayer times display ──
        prayerTimes?.let { times ->
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = c.line)
            Spacer(Modifier.height(16.dp))
            SectionTitle("أوقات الصلاة اليوم", c.textSecondary)
            Spacer(Modifier.height(12.dp))
            times.toList().forEach { entry ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(entry.time,   fontSize = 18.sp, fontFamily = FontFamily.Serif, color = c.textPrimary)
                    Text(entry.nameAr, fontSize = 18.sp, fontFamily = FontFamily.Serif, color = c.textSecondary)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(c.line.copy(alpha = 0.4f)))
            }
        }
    }
}

// ── Helpers ──

@Composable
private fun SectionTitle(text: String, color: Color) {
    Text(text, fontSize = 13.sp, fontFamily = FontFamily.Serif,
        color = color, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, c: AppColors, onToggle: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp, fontFamily = FontFamily.Serif,
            color = c.textPrimary, modifier = Modifier.weight(1f))
        Switch(
            checked         = checked,
            onCheckedChange = onToggle,
            colors          = SwitchDefaults.colors(
                checkedThumbColor   = c.bg,
                checkedTrackColor   = c.btn,
                uncheckedThumbColor = c.textSecondary,
                uncheckedTrackColor = c.surface
            )
        )
    }
}

@Composable
private fun InputField(
    value        : String,
    placeholder  : String,
    modifier     : Modifier = Modifier,
    isNumber     : Boolean  = false,
    onValueChange: (String) -> Unit
) {
    val c = LocalColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(c.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        BasicTextField(
            value         = value,
            onValueChange = { onValueChange(if (isNumber) it.filter(Char::isDigit) else it) },
            singleLine    = true,
            textStyle     = TextStyle(color = c.textPrimary, fontSize = 15.sp,
                fontFamily = FontFamily.Serif, textAlign = TextAlign.End),
            cursorBrush   = SolidColor(c.textPrimary),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(placeholder, color = c.textSecondary,
                    fontSize = 14.sp, fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                inner()
            }
        )
    }
}

@Composable
private fun InfoBox(
    msg      : String,
    bg       : Color,
    textColor: Color,
    icon     : androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)).background(bg).padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(msg, fontSize = 14.sp, fontFamily = FontFamily.Serif,
                color = textColor, textAlign = TextAlign.Center)
        }
    }
}