package com.khadr.tasbih.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.*
import com.khadr.tasbih.ui.components.IconCircle
import com.khadr.tasbih.ui.theme.LocalColors

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val c       = LocalColors.current
    val context = LocalContext.current

    BackHandler { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        // Back button
        IconCircle(
            icon     = Lucide.ArrowRight,
            c        = c,
            modifier = Modifier.padding(top = 52.dp, start = 20.dp).align(Alignment.TopStart)
        ) { onBack() }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App title
            Text("تسابيح", fontSize = 44.sp, fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light, color = c.textPrimary)
            Spacer(Modifier.height(6.dp))
            Text("تطبيق للذكر والتسبيح", fontSize = 14.sp,
                fontFamily = FontFamily.Serif, color = c.textSecondary)

            Spacer(Modifier.height(32.dp))
            HorizontalDivider(color = c.line)
            Spacer(Modifier.height(24.dp))

            // ── Dedication ──
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(c.surface).padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = com.composables.icons.lucide.Lucide.Heart,
                            contentDescription = null,
                            tint = c.textSecondary,
                            modifier = androidx.compose.ui.Modifier.size(14.dp)
                        )
                        Spacer(androidx.compose.ui.Modifier.width(6.dp))
                        Text("إهداء", fontSize = 13.sp, fontFamily = FontFamily.Serif, color = c.textSecondary)
                        Spacer(androidx.compose.ui.Modifier.width(6.dp))
                        androidx.compose.material3.Icon(
                            imageVector = com.composables.icons.lucide.Lucide.Heart,
                            contentDescription = null,
                            tint = c.textSecondary,
                            modifier = androidx.compose.ui.Modifier.size(14.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "هذا التطبيق إهداء\nلمسجد الحاج محمود\nالزاوية الحمراء",
                        fontSize = 18.sp, fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Light, color = c.textPrimary,
                        textAlign = TextAlign.Center, lineHeight = 30.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "رحم الله كل من صلى وذكر الله فيه",
                        fontSize = 13.sp, fontFamily = FontFamily.Serif,
                        color = c.textSecondary, textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = c.line)
            Spacer(Modifier.height(24.dp))

            // ── Developer ──
            Text("المطوّر", fontSize = 13.sp, fontFamily = FontFamily.Serif, color = c.textSecondary)
            Spacer(Modifier.height(10.dp))
            Text("عبد الرحمن خضر", fontSize = 20.sp,
                fontFamily = FontFamily.Serif, color = c.textPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))

            // GitHub link
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(c.surface)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val ev = awaitPointerEvent()
                                if (ev.changes.none { it.pressed }) {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/khadrx"))
                                    )
                                }
                            }
                        }
                    }
            ) {
                Text("github.com/khadrx", fontSize = 15.sp, fontFamily = FontFamily.Serif,
                    color = c.textPrimary, textDecoration = TextDecoration.Underline)
            }
        }

        Text("v1.0", fontSize = 12.sp, fontFamily = FontFamily.Serif, color = c.line,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp))
    }
}