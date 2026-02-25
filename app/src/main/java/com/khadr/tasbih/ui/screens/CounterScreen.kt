package com.khadr.tasbih.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.*
import com.khadr.tasbih.data.Dhikr
import com.khadr.tasbih.ui.components.*
import com.khadr.tasbih.ui.theme.LocalColors
import com.khadr.tasbih.utils.*import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun CounterScreen(
    dhikrItems     : List<Dhikr>,
    isDark         : Boolean,
    onToggleDark   : () -> Unit,
    onAbout        : () -> Unit,
    onManage       : () -> Unit,
    onNotifSettings: () -> Unit
) {
    val c       = LocalColors.current
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val toast   = rememberToastState()

    var hapticEnabled by remember { mutableStateOf(context.loadNotifSettings().hapticEnabled) }
    var hapticTick    by remember { mutableStateOf(context.loadNotifSettings().hapticTick) }
    LaunchedEffect(Unit) {
        val s = context.loadNotifSettings()
        hapticEnabled = s.hapticEnabled
        hapticTick    = s.hapticTick
    }

    var dhikrIndex      by remember(dhikrItems) {
        val saved = context.loadCounterSession(dhikrItems.size)
        mutableIntStateOf(saved?.dhikrIndex ?: 0)
    }
    var finished        by remember(dhikrItems) {
        val saved = context.loadCounterSession(dhikrItems.size)
        mutableStateOf(saved?.finished ?: false)
    }
    var countDirection  by remember { mutableIntStateOf(1) }
    val stepCountsState  = remember(dhikrItems) {
        val saved = context.loadCounterSession(dhikrItems.size)
        mutableStateOf(saved?.stepCounts ?: IntArray(dhikrItems.size) { 0 })
    }
    val lock             = remember { AtomicBoolean(false) }
    var showRestartDialog by remember { mutableStateOf(false) }

    // ── Auto-save session on every meaningful state change ──
    LaunchedEffect(dhikrIndex, finished, stepCountsState.value.toList()) {
        context.saveCounterSession(
            CounterSession(
                dhikrIndex = dhikrIndex,
                finished   = finished,
                stepCounts = stepCountsState.value
            )
        )
    }

    fun currentIndex() = dhikrIndex.coerceIn(0, dhikrItems.lastIndex)
    fun currentCount() = stepCountsState.value.getOrElse(currentIndex()) { 0 }
    fun setCount(idx: Int, v: Int) {
        if (idx !in dhikrItems.indices) return
        stepCountsState.value = stepCountsState.value.copyOf().also { it[idx] = v.coerceAtLeast(0) }
    }

    fun goToIndex(idx: Int, dir: Int) {
        if (idx !in dhikrItems.indices) return
        countDirection = dir
        scope.launch { delay(260); dhikrIndex = idx; lock.set(false) }
    }

    fun increment() {
        if (finished) return
        if (!lock.compareAndSet(false, true)) return
        val idx      = currentIndex()
        val newCount = currentCount() + 1
        countDirection = 1
        setCount(idx, newCount)
        if (hapticTick && hapticEnabled) HapticManager.tick(context)
        if (newCount >= dhikrItems[idx].target) {
            scope.launch { toast.show(getEncouragementMessage(dhikrItems[idx].name)) }
            if (idx < dhikrItems.lastIndex) {
                if (hapticEnabled) HapticManager.stepDone(context)
                goToIndex(idx + 1, 1)
            } else {
                if (hapticEnabled) HapticManager.allDone(context)
                finished = true; lock.set(false)
            }
        } else lock.set(false)
    }

    fun decrement() {
        if (finished) return
        if (!lock.compareAndSet(false, true)) return
        val idx = currentIndex(); val cur = currentCount()
        if (cur > 0) {
            countDirection = -1; setCount(idx, cur - 1)
            if (hapticEnabled) HapticManager.undo(context); lock.set(false)
        } else if (idx > 0) {
            // Go back AND subtract 1 from the previous step
            val prevIdx      = idx - 1
            val prevCount    = stepCountsState.value.getOrElse(prevIdx) { 0 }
            val newPrevCount = (prevCount - 1).coerceAtLeast(0)
            stepCountsState.value = stepCountsState.value.copyOf().also { it[prevIdx] = newPrevCount }
            if (hapticEnabled) HapticManager.undo(context)
            goToIndex(prevIdx, -1)
        } else lock.set(false)
    }

    fun resetCurrentStep() {
        if (!lock.compareAndSet(false, true)) return
        countDirection = -1; setCount(currentIndex(), 0)
        if (hapticEnabled) HapticManager.undo(context); lock.set(false)
    }

    fun restartAll() {
        if (!lock.compareAndSet(false, true)) return
        stepCountsState.value = IntArray(dhikrItems.size) { 0 }
        dhikrIndex = 0; finished = false; countDirection = 1
        context.clearCounterSession()
        lock.set(false)
    }

    val displayIndex = dhikrIndex.coerceIn(0, dhikrItems.lastIndex)
    val displayCount = stepCountsState.value.getOrElse(displayIndex) { 0 }
    val displayDhikr = dhikrItems[displayIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {

        // ── Top bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 52.dp, start = 20.dp, end = 20.dp)
                .pointerInput(Unit) {
                    awaitPointerEventScope { while (true) awaitPointerEvent() }
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconCircle(Lucide.Info, c) { onAbout() }
                IconCircle(Lucide.ListChecks, c) { onManage() }
                IconCircle(Lucide.Bell, c) { onNotifSettings() }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconCircle(if (isDark) Lucide.Sun else Lucide.Moon, c) { onToggleDark() }
            }
        }

        // ── Progress dots — centered below top bar ──
        if (!finished) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 104.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                dhikrItems.forEachIndexed { i, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (i == displayIndex) 8.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    i < displayIndex  -> c.line
                                    i == displayIndex -> c.btn
                                    else              -> c.line.copy(alpha = 0.4f)
                                }
                            )
                    )
                }
            }
        }

        // ── Divider ──
        Box(
            Modifier.align(Alignment.TopCenter).padding(top = 124.dp)
                .fillMaxWidth(0.82f).height(1.dp).background(c.line)
        )

        // ── Interaction zone: tap = increment, swipe up/down = inc/dec ──
        // Positioned exactly from divider to above bottom buttons
        if (!finished && !showRestartDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top    = 127.dp,
                        bottom = 116.dp
                    )
                    .pointerInput(dhikrItems) {
                        var counted = false
                        detectDragGestures(
                            onDragStart  = { counted = false },
                            onDrag       = { ch, drag ->
                                ch.consume()
                                if (!counted) when {
                                    drag.y < -20f -> { counted = true; increment() }
                                    drag.y >  20f -> { counted = true; decrement() }
                                }
                            },
                            onDragEnd    = { counted = false },
                            onDragCancel = { counted = false }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { increment() }
                    }
            )
        }

        // ── Main content ──
        if (finished) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center).padding(32.dp)
            ) {
                Text("بارك الله فيك", fontSize = 38.sp, fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Light, color = c.textPrimary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Lucide.CircleCheck, contentDescription = null,
                        tint = c.success, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("أتممت جميع الأذكار", fontSize = 18.sp,
                        fontFamily = FontFamily.Serif, color = c.textSecondary)
                }
                Spacer(Modifier.height(48.dp))
                PillButton("إعادة من البداية", 220) { showRestartDialog = true }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center).offset(y = (-40).dp)
            ) {
                AnimatedContent(
                    targetState    = displayCount,
                    transitionSpec = {
                        val dir   = countDirection
                        val enter = slideInVertically(tween(180, easing = EaseOutCubic)) {
                            if (dir > 0) it else -it
                        } + fadeIn(tween(140))
                        val exit  = slideOutVertically(tween(180, easing = EaseInCubic)) {
                            if (dir > 0) -it else it
                        } + fadeOut(tween(140))
                        enter togetherWith exit using SizeTransform(clip = false)
                    },
                    label = "count"
                ) { n ->
                    Text(n.toString(), fontSize = 128.sp, fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Light, color = c.textPrimary, lineHeight = 128.sp)
                }
                Spacer(Modifier.height(2.dp))
                Text("/ ${displayDhikr.target}", fontSize = 22.sp,
                    fontFamily = FontFamily.Serif, color = c.textSecondary)
                Spacer(Modifier.height(18.dp))
                Text(displayDhikr.name, fontSize = 26.sp, fontFamily = FontFamily.Serif,
                    color = c.textPrimary, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp))
            }
        }

        // ── Bottom buttons: [ − ]  [ ↺ ] ──
        if (!finished) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 52.dp)
                    // ── Consume all pointer events so taps on buttons don't reach the Box ──
                    .pointerInput(Unit) {
                        awaitPointerEventScope { while (true) awaitPointerEvent() }
                    },
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Decrement pill
                PillButton("−", 90, 62, c.danger, 28) { decrement() }

                // Reset step — icon pill
                ResetPill(onClick = { showRestartDialog = true })
            }
        }

        // ── Toast ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 132.dp)
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        ) {
            ToastOverlay(message = toast.message, visible = toast.visible)
        }

        // ── Restart confirmation ──
        if (showRestartDialog) {
            RestartConfirmDialog(
                onConfirmAll   = { showRestartDialog = false; restartAll() },
                onConfirmStep  = { showRestartDialog = false; resetCurrentStep() },
                onDismiss      = { showRestartDialog = false }
            )
        }
    }
}

// ── Reset pill button (icon + text) ──
@Composable
private fun ResetPill(onClick: () -> Unit) {
    val c = LocalColors.current
    var pressed by remember { mutableStateOf(false) }
    val bg    by animateColorAsState(
        if (pressed) c.surface else c.bg, tween(70), label = "bg"
    )
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, tween(70), label = "sc")

    Row(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(50.dp))
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
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Lucide.RotateCcw, contentDescription = null,
            tint = c.textSecondary, modifier = Modifier.size(18.dp))
        Text("إعادة", fontSize = 16.sp, fontFamily = FontFamily.Serif,
            color = c.textSecondary, fontWeight = FontWeight.Light)
    }
}

// ── Restart confirmation ── now offers: reset step OR reset all ──
@Composable
private fun RestartConfirmDialog(
    onConfirmAll : () -> Unit,
    onConfirmStep: () -> Unit,
    onDismiss    : () -> Unit
) {
    val c = LocalColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val ev = awaitPointerEvent()
                        if (ev.changes.none { it.pressed }) onDismiss()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 36.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(c.bg)
                .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } }
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Lucide.RotateCcw, contentDescription = null,
                tint = c.danger, modifier = Modifier.size(30.dp))
            Spacer(Modifier.height(14.dp))
            Text("إعادة العداد", fontSize = 20.sp, fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light, color = c.textPrimary)
            Spacer(Modifier.height(8.dp))
            Text("اختر ما تريد إعادته",
                fontSize = 14.sp, fontFamily = FontFamily.Serif,
                color = c.textSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Reset current step only
                PillButton(
                    label   = "إعادة هذا الذكر فقط",
                    width   = 260, height = 50,
                    color   = c.btn, fontSize = 15,
                    onClick = onConfirmStep
                )
                // Reset all
                PillButton(
                    label   = "إعادة كل الأذكار",
                    width   = 260, height = 50,
                    color   = c.danger, fontSize = 15,
                    onClick = onConfirmAll
                )
                // Cancel
                PillButton(
                    label   = "إلغاء",
                    width   = 260, height = 46,
                    color   = c.line, fontSize = 15,
                    onClick = onDismiss
                )
            }
        }
    }
}