package com.khadr.tasbih.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.*
import com.khadr.tasbih.data.Dhikr
import com.khadr.tasbih.data.allPresets
import com.composables.icons.lucide.*
import com.khadr.tasbih.ui.components.*
import com.khadr.tasbih.utils.CustomPreset
import com.khadr.tasbih.utils.loadCustomPresets
import com.khadr.tasbih.utils.saveCustomPresets
import kotlinx.coroutines.delay
import com.khadr.tasbih.ui.theme.LocalColors

@Composable
fun ManageScreen(
    dhikrItems: List<Dhikr>,
    onSave    : (List<Dhikr>) -> Unit,
    onBack    : () -> Unit
) {
    val c       = LocalColors.current
    val context = LocalContext.current

    val  originalItems    = remember { dhikrItems.toList() }
    var items             by remember { mutableStateOf(dhikrItems.toMutableList()) }
    var newName           by remember { mutableStateOf("") }
    var newTarget         by remember { mutableStateOf("") }
    var pendingPreset       by remember { mutableStateOf<com.khadr.tasbih.data.DhikrPreset?>(null) }
    var pendingDeleteIndex  by remember { mutableIntStateOf(-1) }
    var customPresets       by remember { mutableStateOf(context.loadCustomPresets()) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var pendingDeletePreset  by remember { mutableIntStateOf(-1) }
    var showAddDialog        by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var addToastMessage   by remember { mutableStateOf("") }
    var addToastVisible   by remember { mutableStateOf(false) }

    val hasChanges = items.toList() != originalItems

    fun tryBack() { if (hasChanges) showUnsavedDialog = true else onBack() }

    BackHandler { tryBack() }

    LaunchedEffect(addToastVisible) {
        if (addToastVisible) {
            kotlinx.coroutines.delay(2000)
            addToastVisible = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(c.bg)
                .padding(top = 52.dp)
        ) {
            // ── Header ──
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconCircle(Lucide.ArrowRight, c) { tryBack() }
                Text("إدارة الأذكار", fontSize = 20.sp, fontFamily = FontFamily.Serif, color = c.textPrimary)
                PillButton("حفظ", 80, 40, fontSize = 15) { onSave(items) }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = c.line, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(16.dp))

            // ── Preset collections row ──
            Text(
                "قوائم جاهزة",
                fontSize = 13.sp, fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold, color = c.textSecondary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allPresets) { preset ->
                    PresetChip(
                        title    = preset.title,
                        count    = preset.items.size,
                        onClick  = { pendingPreset = preset }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = c.line, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(16.dp))

            // ── Custom presets row ──
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("قوائمي", fontSize = 13.sp, fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold, color = c.textSecondary)
                SmallCircleBtn(Lucide.BookmarkPlus, c, color = c.btn, tint = c.btnText) {
                    showSavePresetDialog = true
                }
            }
            Spacer(Modifier.height(10.dp))

            if (customPresets.isEmpty()) {
                Text("لا توجد قوائم محفوظة بعد — اضغط + لحفظ القائمة الحالية",
                    fontSize = 12.sp, fontFamily = FontFamily.Serif,
                    color = c.textSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 20.dp))
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(customPresets) { idx, preset ->
                        CustomPresetChip(
                            title   = preset.title,
                            count   = preset.items.size,
                            onClick = {
                                pendingPreset = com.khadr.tasbih.data.DhikrPreset(
                                    id    = "custom_$idx",
                                    title = preset.title,
                                    items = preset.items
                                )
                            },
                            onDelete = { pendingDeletePreset = idx }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = c.line, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(12.dp))

            // ── Current list ──
            Text(
                "القائمة الحالية",
                fontSize = 13.sp, fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold, color = c.textSecondary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(10.dp))

            val listState    = rememberLazyListState()
            val reorderState = rememberReorderableLazyListState(listState) { from, to ->
                // shift indices by 1 because item[0] is the "add" button
                val fromIdx = from.index - 1
                val toIdx   = to.index   - 1
                if (fromIdx >= 0 && toIdx >= 0) {
                    val m = items.toMutableList()
                    m.add(toIdx, m.removeAt(fromIdx))
                    items = m
                }
            }

            LazyColumn(
                state   = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── Add button as first item (key="add" so it doesn't participate in reorder) ──
                item(key = "add_btn") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(c.surface)
                            .pointerInput(Unit) { detectTapGestures { showAddDialog = true } }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Lucide.Plus, contentDescription = null,
                                tint = c.textSecondary, modifier = Modifier.size(16.dp))
                            Text("إضافة ذكر", fontSize = 15.sp,
                                fontFamily = FontFamily.Serif, color = c.textSecondary,
                                fontWeight = FontWeight.Light)
                        }
                    }
                }

                itemsIndexed(items, key = { _, dhikr -> dhikr.name + dhikr.target }) { idx, dhikr ->
                    ReorderableItem(reorderState, key = dhikr.name + dhikr.target) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 6.dp else 0.dp, label = "elev")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(c.surface)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Drag handle — long press to drag
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .longPressDraggableHandle(
                                            onDragStarted = {},
                                            onDragStopped = {}
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Lucide.GripVertical, contentDescription = null,
                                        tint = c.textSecondary, modifier = Modifier.size(18.dp))
                                }

                                // Name + target
                                Column(
                                    Modifier.weight(1f).padding(horizontal = 10.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(dhikr.name, fontSize = 16.sp, fontFamily = FontFamily.Serif,
                                        color = c.textPrimary, textAlign = TextAlign.End)
                                    Text("${dhikr.target} مرة", fontSize = 13.sp,
                                        fontFamily = FontFamily.Serif, color = c.textSecondary)
                                }

                                // Delete
                                SmallCircleBtn(Lucide.X, c, color = c.danger, tint = c.btnText) {
                                    pendingDeleteIndex = idx
                                }
                            }
                        }
                    }
                }


            }
        }

        // ── Preset load confirmation dialog ──
        pendingPreset?.let { preset ->
            PresetConfirmDialog(
                preset    = preset,
                onReplace = {
                    items         = preset.items.toMutableList()
                    pendingPreset = null
                },
                onAppend  = {
                    val m = items.toMutableList()
                    m.addAll(preset.items)
                    items         = m
                    pendingPreset = null
                },
                onDismiss = { pendingPreset = null }
            )
        }

        // ── Delete confirmation dialog ──
        if (pendingDeleteIndex >= 0) {
            DeleteConfirmDialog(
                dhikrName = items.getOrNull(pendingDeleteIndex)?.name ?: "",
                onConfirm = {
                    val m = items.toMutableList()
                    m.removeAt(pendingDeleteIndex)
                    items = m
                    pendingDeleteIndex = -1
                },
                onDismiss = { pendingDeleteIndex = -1 }
            )
        }

        // ── Add validation toast ──
        if (addToastVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                com.khadr.tasbih.ui.components.ToastOverlay(
                    message = addToastMessage,
                    visible = addToastVisible
                )
            }
        }

        // ── Unsaved changes dialog — rendered last so it's on top ──
        if (showUnsavedDialog) {
            UnsavedChangesDialog(
                onSave    = { onSave(items) },
                onDiscard = { onBack() },
                onDismiss = { showUnsavedDialog = false }
            )
        }

        // ── Add dhikr dialog ──
        if (showAddDialog) {
            AddDhikrDialog(
                onAdd = { name, target ->
                    val m = items.toMutableList()
                    m.add(0, Dhikr(name, target))   // add at top of list
                    items = m
                    showAddDialog = false
                },
                onDismiss = { showAddDialog = false }
            )
        }

        // ── Save as custom preset dialog ──
        if (showSavePresetDialog) {
            SavePresetDialog(
                onSave = { title ->
                    val newPreset = CustomPreset(title, items.toList())
                    val updated   = customPresets + newPreset
                    customPresets = updated
                    context.saveCustomPresets(updated)
                    showSavePresetDialog = false
                },
                onDismiss = { showSavePresetDialog = false }
            )
        }

        // ── Delete custom preset confirmation ──
        if (pendingDeletePreset >= 0) {
            DeleteCustomPresetDialog(
                title     = customPresets.getOrNull(pendingDeletePreset)?.title ?: "",
                onConfirm = {
                    val updated = customPresets.toMutableList().also { it.removeAt(pendingDeletePreset) }
                    customPresets      = updated
                    context.saveCustomPresets(updated)
                    pendingDeletePreset = -1
                },
                onDismiss = { pendingDeletePreset = -1 }
            )
        }

    } // end Box
}

// ── Preset chip ──
@Composable
private fun PresetChip(title: String, count: Int, onClick: () -> Unit) {
    val c = LocalColors.current
    var pressed by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (pressed) c.btn else c.surface)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                title,
                fontSize   = 14.sp, fontFamily = FontFamily.Serif,
                color      = if (pressed) c.btnText else c.textPrimary,
                fontWeight = FontWeight.Light
            )
            Text(
                "$count أذكار",
                fontSize = 11.sp, fontFamily = FontFamily.Serif,
                color    = if (pressed) c.btnText.copy(alpha = 0.7f) else c.textSecondary
            )
        }
    }
}

// ── Preset confirmation dialog ──
@Composable
private fun PresetConfirmDialog(
    preset   : com.khadr.tasbih.data.DhikrPreset,
    onReplace: () -> Unit,
    onAppend : () -> Unit,
    onDismiss: () -> Unit
) {
    val c = LocalColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f))
            .pointerInput(Unit) {
                detectTapGestures { onDismiss() }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(c.bg)
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                preset.title,
                fontSize   = 20.sp, fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light, color = c.textPrimary
            )
            Spacer(Modifier.height(8.dp))

            // Preview items
            preset.items.forEach { dhikr ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${dhikr.target}×", fontSize = 13.sp,
                        fontFamily = FontFamily.Serif, color = c.textSecondary)
                    Text(dhikr.name, fontSize = 14.sp,
                        fontFamily = FontFamily.Serif, color = c.textPrimary,
                        textAlign = TextAlign.End, modifier = Modifier.weight(1f).padding(start = 8.dp))
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = c.line)
            Spacer(Modifier.height(16.dp))

            Text(
                "اختر طريقة التحميل",
                fontSize = 13.sp, fontFamily = FontFamily.Serif, color = c.textSecondary
            )
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Replace
                PillButton(
                    label    = "استبدال القائمة الحالية",
                    width    = 260, height = 50,
                    color    = c.danger, fontSize = 15,
                    onClick  = onReplace
                )
                // Append
                PillButton(
                    label    = "إضافة للقائمة الحالية",
                    width    = 260, height = 50,
                    color    = c.btn, fontSize = 15,
                    onClick  = onAppend
                )
                // Cancel
                PillButton(
                    label    = "إلغاء",
                    width    = 260, height = 46,
                    color    = c.line, fontSize = 15,
                    onClick  = onDismiss
                )
            }
        }
    }
}

// ── Input field ──
@Composable
private fun ManageInputField(
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
            textStyle     = TextStyle(
                color = c.textPrimary, fontSize = 15.sp,
                fontFamily = FontFamily.Serif, textAlign = TextAlign.End
            ),
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

// ── Unsaved changes dialog ──
@Composable
private fun UnsavedChangesDialog(
    onSave   : () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    val c = LocalColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f))
            .pointerInput(Unit) {
                detectTapGestures { onDismiss() }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 36.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                .background(c.bg)
                .pointerInput(Unit) {
                    // consume taps so they don't reach the scrim
                    detectTapGestures { }
                }
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = com.composables.icons.lucide.Lucide.TriangleAlert,
                contentDescription = null,
                tint = c.danger,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "تعديلات غير محفوظة",
                fontSize = 20.sp, fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light, color = c.textPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "لديك تعديلات لم تُحفظ بعد.\nماذا تريد أن تفعل؟",
                fontSize = 14.sp, fontFamily = FontFamily.Serif,
                color = c.textSecondary, textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PillButton(
                    label   = "حفظ والخروج",
                    width   = 260, height = 50,
                    color   = c.btn, fontSize = 15,
                    onClick = onSave
                )
                PillButton(
                    label   = "خروج بدون حفظ",
                    width   = 260, height = 50,
                    color   = c.danger, fontSize = 15,
                    onClick = onDiscard
                )
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

// ── Quick count options ──
private val quickCounts = listOf(3, 7, 10, 11, 33, 100)

@Composable
private fun AddDhikrSection(
    newName         : String,
    newTarget       : String,
    onNameChange    : (String) -> Unit,
    onTargetChange  : (String) -> Unit,
    onAdd           : (String, Int) -> Unit,
    onValidationError: (String) -> Unit
) {
    val c = LocalColors.current

    Spacer(Modifier.height(4.dp))
    HorizontalDivider(color = c.line)
    Spacer(Modifier.height(14.dp))

    Text("إضافة ذكر جديد", fontSize = 13.sp, fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold, color = c.textSecondary)
    Spacer(Modifier.height(10.dp))

    // Row: name field + add button
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ManageInputField(
            value       = newName,
            placeholder = "اسم الذكر",
            modifier    = Modifier.weight(1f)
        ) { onNameChange(it) }

        SmallCircleBtn(
            icon  = Lucide.Plus,
            c     = c,
            size  = 48.dp,
            color = c.btn,
            tint  = c.btnText
        ) {
            val t = newTarget.toIntOrNull() ?: 0
            when {
                newName.isBlank() && t == 0 -> onValidationError("أدخل اسم الذكر والعدد")
                newName.isBlank()            -> onValidationError("أدخل اسم الذكر")
                t == 0                       -> onValidationError("اختر عدداً للتكرار")
                else -> onAdd(newName.trim(), t)
            }
        }
    }

    Spacer(Modifier.height(10.dp))

    // Row: manual input + quick count chips
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ManageInputField(
            value       = newTarget,
            placeholder = "يدوي",
            modifier    = Modifier.width(72.dp),
            isNumber    = true
        ) { onTargetChange(it) }

        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(quickCounts) { n ->
                val selected = newTarget == n.toString()
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(50.dp))
                        .background(if (selected) c.btn else c.surface)
                        .pointerInput(n) {
                            detectTapGestures { onTargetChange(n.toString()) }
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(n.toString(), fontSize = 14.sp, fontFamily = FontFamily.Serif,
                        color = if (selected) c.btnText else c.textPrimary)
                }
            }
        }
    }

    Spacer(Modifier.height(32.dp))
}

// ── Delete confirmation dialog ──
@Composable
private fun DeleteConfirmDialog(
    dhikrName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val c = LocalColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f))
            .pointerInput(Unit) { detectTapGestures { onDismiss() } },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 36.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(c.bg)
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Lucide.Trash2, contentDescription = null,
                tint = c.danger, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))
            Text("حذف الذكر", fontSize = 20.sp, fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light, color = c.textPrimary)
            Spacer(Modifier.height(8.dp))
            Text(dhikrName, fontSize = 16.sp, fontFamily = FontFamily.Serif,
                color = c.textSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text("هل أنت متأكد من الحذف؟", fontSize = 13.sp,
                fontFamily = FontFamily.Serif, color = c.textSecondary,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PillButton("حذف", 260, 50, c.danger, 15, onClick = onConfirm)
                PillButton("إلغاء", 260, 46, c.line, 15, onClick = onDismiss)
            }
        }
    }
}

// ── Custom preset chip (with delete X) ──
@Composable
private fun CustomPresetChip(
    title   : String,
    count   : Int,
    onClick : () -> Unit,
    onDelete: () -> Unit
) {
    val c = LocalColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(c.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .pointerInput(Unit) { detectTapGestures { onClick() } }
                .padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 8.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, fontSize = 14.sp, fontFamily = FontFamily.Serif,
                    color = c.textPrimary, fontWeight = FontWeight.Light)
                Text("$count أذكار", fontSize = 11.sp,
                    fontFamily = FontFamily.Serif, color = c.textSecondary)
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .pointerInput(Unit) { detectTapGestures { onDelete() } }
                .padding(end = 8.dp)
        ) {
            Icon(Lucide.X, contentDescription = null,
                tint = c.textSecondary, modifier = Modifier.size(12.dp))
        }
        Spacer(Modifier.width(4.dp))
    }
}

// ── Save current list as preset dialog ──
@Composable
private fun SavePresetDialog(
    onSave   : (String) -> Unit,
    onDismiss: () -> Unit
) {
    val c     = LocalColors.current
    var title by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f))
            .pointerInput(Unit) { detectTapGestures { onDismiss() } },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 36.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(c.bg)
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Lucide.BookmarkPlus, contentDescription = null,
                tint = c.btn, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))
            Text("حفظ القائمة الحالية", fontSize = 20.sp,
                fontFamily = FontFamily.Serif, fontWeight = FontWeight.Light,
                color = c.textPrimary)
            Spacer(Modifier.height(6.dp))
            Text("أدخل اسماً للقائمة", fontSize = 13.sp,
                fontFamily = FontFamily.Serif, color = c.textSecondary)
            Spacer(Modifier.height(18.dp))

            // Title input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))
                    .background(c.surface)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value         = title,
                    onValueChange = { title = it },
                    singleLine    = true,
                    textStyle     = androidx.compose.ui.text.TextStyle(
                        color      = c.textPrimary,
                        fontSize   = 16.sp,
                        fontFamily = FontFamily.Serif,
                        textAlign  = TextAlign.End
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(c.textPrimary),
                    decorationBox = { inner ->
                        if (title.isEmpty()) Text("مثال: أذكاري اليومية",
                            color = c.textSecondary, fontSize = 14.sp,
                            fontFamily = FontFamily.Serif, textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth())
                        inner()
                    }
                )
            }

            Spacer(Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PillButton(
                    label   = "حفظ",
                    width   = 260, height = 50,
                    color   = if (title.isBlank()) c.line else c.btn,
                    fontSize = 15,
                    onClick = { if (title.isNotBlank()) onSave(title.trim()) }
                )
                PillButton("إلغاء", 260, 46, c.line, 15, onClick = onDismiss)
            }
        }
    }
}

// ── Delete custom preset confirmation ──
@Composable
private fun DeleteCustomPresetDialog(
    title    : String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val c = LocalColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f))
            .pointerInput(Unit) { detectTapGestures { onDismiss() } },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 36.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(c.bg)
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Lucide.Trash2, contentDescription = null,
                tint = c.danger, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))
            Text("حذف القائمة", fontSize = 20.sp, fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light, color = c.textPrimary)
            Spacer(Modifier.height(6.dp))
            Text(title, fontSize = 16.sp, fontFamily = FontFamily.Serif,
                color = c.textSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text("هل أنت متأكد من الحذف؟", fontSize = 13.sp,
                fontFamily = FontFamily.Serif, color = c.textSecondary)
            Spacer(Modifier.height(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PillButton("حذف", 260, 50, c.danger, 15, onClick = onConfirm)
                PillButton("إلغاء", 260, 46, c.line, 15, onClick = onDismiss)
            }
        }
    }
}

// ── Add dhikr dialog ──
@Composable
private fun AddDhikrDialog(
    onAdd    : (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val c         = LocalColors.current
    var name      by remember { mutableStateOf("") }
    var target    by remember { mutableStateOf("") }
    var errorMsg  by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f))
            .pointerInput(Unit) { detectTapGestures { onDismiss() } },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(c.bg)
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Lucide.Plus, contentDescription = null,
                tint = c.btn, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))
            Text("إضافة ذكر جديد", fontSize = 20.sp, fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light, color = c.textPrimary)
            Spacer(Modifier.height(18.dp))

            // Name field
            ManageInputField(
                value       = name,
                placeholder = "اسم الذكر",
                modifier    = Modifier.fillMaxWidth()
            ) { name = it; errorMsg = "" }

            Spacer(Modifier.height(12.dp))

            // Count row: manual + quick chips
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ManageInputField(
                    value       = target,
                    placeholder = "يدوي",
                    modifier    = Modifier.width(68.dp),
                    isNumber    = true
                ) { target = it; errorMsg = "" }

                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(listOf(3, 7, 10, 11, 33, 100)) { n ->
                        val selected = target == n.toString()
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (selected) c.btn else c.surface)
                                .pointerInput(n) {
                                    detectTapGestures { target = n.toString(); errorMsg = "" }
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(n.toString(), fontSize = 14.sp, fontFamily = FontFamily.Serif,
                                color = if (selected) c.btnText else c.textPrimary)
                        }
                    }
                }
            }

            // Error message
            if (errorMsg.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(errorMsg, fontSize = 12.sp, fontFamily = FontFamily.Serif,
                    color = c.danger, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PillButton(
                    label   = "إضافة",
                    width   = 260, height = 50,
                    color   = c.btn, fontSize = 15,
                    onClick = {
                        val t = target.toIntOrNull() ?: 0
                        when {
                            name.isBlank() && t == 0 -> errorMsg = "أدخل اسم الذكر والعدد"
                            name.isBlank()            -> errorMsg = "أدخل اسم الذكر"
                            t == 0                    -> errorMsg = "اختر عدداً للتكرار"
                            else                      -> onAdd(name.trim(), t)
                        }
                    }
                )
                PillButton("إلغاء", 260, 46, c.line, 15, onClick = onDismiss)
            }
        }
    }
}