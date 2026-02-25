package com.khadr.tasbih

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.khadr.tasbih.data.Dhikr
import com.khadr.tasbih.data.defaultDhikrList
import com.khadr.tasbih.ui.screens.*
import com.khadr.tasbih.ui.theme.*
import com.khadr.tasbih.utils.*

enum class Screen { COUNTER, ABOUT, MANAGE, NOTIFICATIONS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        enableEdgeToEdge()
        setContent { TasbihApp() }
    }
}

@Composable
fun TasbihApp() {
    val context    = LocalContext.current
    var isDark     by remember { mutableStateOf(false) }
    var screen     by remember { mutableStateOf(Screen.COUNTER) }

    // ── Load saved dhikr list on startup, fall back to default ──
    var dhikrItems by remember {
        mutableStateOf(context.loadDhikrList() ?: defaultDhikrList)
    }

    val colors = if (isDark) DarkColors else LightColors

    CompositionLocalProvider(LocalColors provides colors) {
        AnimatedContent(
            targetState  = screen,
            transitionSpec = {
                val toCounter = targetState == Screen.COUNTER
                val enter = if (toCounter) slideInHorizontally(tween(280)) { -it }
                else           slideInHorizontally(tween(280)) {  it }
                val exit  = if (toCounter) slideOutHorizontally(tween(280)) {  it }
                else           slideOutHorizontally(tween(280)) { -it }
                (enter + fadeIn(tween(280))).togetherWith(exit + fadeOut(tween(280)))
            },
            label = "screen"
        ) { s ->
            when (s) {
                Screen.COUNTER -> CounterScreen(
                    dhikrItems      = dhikrItems,
                    isDark          = isDark,
                    onToggleDark    = { isDark = !isDark },
                    onAbout         = { screen = Screen.ABOUT },
                    onManage        = { screen = Screen.MANAGE },
                    onNotifSettings = { screen = Screen.NOTIFICATIONS }
                )
                Screen.ABOUT   -> AboutScreen(onBack = { screen = Screen.COUNTER })
                Screen.MANAGE  -> ManageScreen(
                    dhikrItems = dhikrItems,
                    onSave     = { newList ->
                        dhikrItems = newList
                        context.saveDhikrList(newList)   // ← persist on save
                        screen = Screen.COUNTER
                    },
                    onBack     = { screen = Screen.COUNTER }
                )
                Screen.NOTIFICATIONS -> NotificationSettingsScreen(onBack = { screen = Screen.COUNTER })
            }
        }
    }
}