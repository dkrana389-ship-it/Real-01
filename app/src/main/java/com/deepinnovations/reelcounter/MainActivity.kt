package com.deepinnovations.reelcounter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.TextStyle as DayStyle
import java.util.Locale

private val Bg = Color(0xFF0E0B1A)
private val PanelBg = Color(0xFF1B1630)
private val Grad = Brush.linearGradient(
    listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = darkColorScheme()) { Home() } }
    }
}

fun isServiceEnabled(c: Context): Boolean {
    val enabled = Settings.Secure.getString(
        c.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabled.split(':').any {
        it.startsWith(c.packageName) && it.contains("ReelAccessibilityService")
    }
}

private fun fmt(ms: Long): String {
    val s = ms / 1000
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) "${h}h ${m}m" else if (m > 0) "${m}m ${sec}s" else "${sec}s"
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(PanelBg)
            .padding(18.dp),
        content = content
    )
}

@Composable
private fun Home() {
    val ctx = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            tick++
        }
    }
    val reels = remember(tick) { Store.reels(ctx) }
    val ms = remember(tick) { Store.millis(ctx) }
    val enabled = remember(tick) { isServiceEnabled(ctx) }
    val week = remember(tick) {
        (6 downTo 0).map {
            val d = LocalDate.now().minusDays(it.toLong())
            Triple(d.dayOfWeek.getDisplayName(DayStyle.SHORT, Locale.ENGLISH), Store.reels(ctx, d.toString()), it == 0)
        }
    }
    var name by remember { mutableStateOf(Store.username(ctx)) }
    var strict by remember { mutableStateOf(Store.strict(ctx)) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero card
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(Grad)
                .padding(24.dp)
        ) {
            Column {
                Text("REEL COUNTER", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (name.isBlank()) "Hello 👋" else "Hello @${name.trim().removePrefix("@")}",
                    color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold
                )
                Text("Aaj Instagram pe tumne", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$reels", color = Color.White, fontSize = 72.sp, fontWeight = FontWeight.ExtraBold)
                    Text("  reels dekhi", color = Color.White, fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp))
                }
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("⏱  ${fmt(ms)} time bitaya", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Last 7 days
        Panel {
            Text("Last 7 din", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            val max = (week.maxOf { it.second }).coerceAtLeast(1)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                week.forEach { (label, count, isToday) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(38.dp)) {
                        Text("$count", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier
                                .width(20.dp)
                                .height((6 + 90f * count / max).dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isToday) Grad else Brush.linearGradient(listOf(Color(0xFF3A3358), Color(0xFF3A3358))))
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(label, color = if (isToday) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                }
            }
        }

        // Setup / status
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (enabled) Color(0xFF2ECC71) else Color(0xFFFF5252))
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    if (enabled) "Counter ON hai ✅" else "Counter OFF hai — permission chahiye",
                    color = Color.White, fontWeight = FontWeight.Bold
                )
            }
            if (!enabled) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "1. Neeche 'Enable karo' dabao → Installed apps → Reel Counter → ON karo.\n" +
                        "2. Agar 'Restricted setting' aaye: Phone Settings → Apps → Reel Counter → ⋮ (3 dot) → Allow restricted settings, phir step 1 dobara.\n" +
                        "3. Instagram kholo, counter khud dikhega.",
                    color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp, lineHeight = 19.sp
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFD1D5E)),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Enable karo") }
            }
        }

        // Settings
        Panel {
            Text("Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; Store.setUsername(ctx, it) },
                label = { Text("Instagram username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Sirf Reels count karo", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("OFF karoge to Instagram ka har scroll ginega (agar reels count na ho rahi ho to try karo)",
                        color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
                Switch(checked = strict, onCheckedChange = { strict = it; Store.setStrict(ctx, it) })
            }
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = { Store.resetToday(ctx) }) {
                Text("Aaj ki counting reset karo", color = Color(0xFFFF8A80))
            }
        }

        Text(
            "Privacy: yeh app sirf Instagram khula hai ya nahi aur reel scroll hui ya nahi, yahi dekhta hai. Koi data phone se bahar nahi jaata.",
            color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp, lineHeight = 16.sp
        )
    }
}
