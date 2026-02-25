package com.khadr.tasbih.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khadr.tasbih.ui.theme.AppColors
import com.khadr.tasbih.ui.theme.LocalColors
import kotlinx.coroutines.delay

// ══════════════════════════════════════════════
//  Pill Button
// ══════════════════════════════════════════════
@Composable
fun PillButton(
    label   : String,
    width   : Int,
    height  : Int    = 62,
    color   : Color  = LocalColors.current.btn,
    fontSize: Int    = 20,
    modifier: Modifier = Modifier,
    onClick : () -> Unit
) {
    val c = LocalColors.current
    var pressed by remember { mutableStateOf(false) }
    val bg    by animateColorAsState(if (pressed) color.copy(alpha = 0.75f) else color, tween(70), label = "bg")
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, tween(70), label = "sc")

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .scale(scale)
            .width(width.dp)
            .height(height.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val ev   = awaitPointerEvent()
                        val down = ev.changes.any { it.pressed }
                        if (down && !pressed) pressed = true
                        else if (!down && pressed) { pressed = false; onClick() }
                    }
                }
            }
    ) {
        Text(
            label, color = c.btnText, fontSize = fontSize.sp,
            fontFamily = FontFamily.Serif, fontWeight = FontWeight.Light
        )
    }
}

// ══════════════════════════════════════════════
//  Icon Circle Button (uses Lucide ImageVector)
// ══════════════════════════════════════════════
@Composable
fun IconCircle(
    icon     : ImageVector,
    c        : AppColors,
    modifier : Modifier = Modifier,
    tint     : Color = c.textPrimary,
    onClick  : () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val bg by animateColorAsState(
        if (pressed) c.line else c.surface, tween(70), label = "icbg"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bg)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val ev = awaitPointerEvent()
                        val down = ev.changes.any { it.pressed }
                        if (down && !pressed) pressed = true
                        else if (!down && pressed) { pressed = false; onClick() }
                    }
                }
            }
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

// ══════════════════════════════════════════════
//  Small Circle Button (uses Lucide ImageVector)
// ══════════════════════════════════════════════
@Composable
fun SmallCircleBtn(
    icon     : ImageVector,
    c        : AppColors,
    size     : Dp      = 32.dp,
    color    : Color   = c.surface,
    tint     : Color   = c.textPrimary,
    enabled  : Boolean = true,
    onClick  : () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (enabled) color else c.line.copy(alpha = 0.3f))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        val ev = awaitPointerEvent()
                        if (ev.changes.none { it.pressed }) onClick()
                    }
                }
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) tint else c.textSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ══════════════════════════════════════════════
//  Custom Toast Overlay
// ══════════════════════════════════════════════
@Composable
fun ToastOverlay(message: String, visible: Boolean) {
    val c = LocalColors.current
    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 },
        exit    = fadeOut(tween(400)) + slideOutVertically(tween(400)) { it / 2 },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .wrapContentWidth(Alignment.CenterHorizontally)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(c.btn.copy(alpha = 0.92f))
                .padding(horizontal = 28.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = message,
                color      = c.btnText,
                fontSize   = 16.sp,
                fontFamily = FontFamily.Serif,
                textAlign  = TextAlign.Center
            )
        }
    }
}

// ══════════════════════════════════════════════
//  Toast state helper
// ══════════════════════════════════════════════
class ToastState {
    var message by mutableStateOf("")
    var visible by mutableStateOf(false)

    suspend fun show(msg: String) {
        message = msg
        visible = true
        delay(2200)
        visible = false
    }
}

@Composable
fun rememberToastState() = remember { ToastState() }