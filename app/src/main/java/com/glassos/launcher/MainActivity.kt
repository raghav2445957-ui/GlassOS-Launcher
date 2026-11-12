package com.glassos.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LaunchableApp(val label: String, val packageName: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GlassHome() }
    }
}

@Composable
private fun GlassHome() {
    val context = LocalContext.current
    val pm = context.packageManager
    var apps by remember { mutableStateOf(emptyList<LaunchableApp>()) }
    var drawer by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        apps = pm.queryIntentActivities(intent, 0)
            .map { LaunchableApp(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    val shown = apps.filter { it.label.contains(query, ignoreCase = true) }
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(Color(0xFF101522), Color(0xFF25213A), Color(0xFF101923)))
        )
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 28.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("GLASSOS", color = Color.White.copy(alpha = .72f), fontSize = 13.sp, letterSpacing = 3.sp)
                    Text("Good evening", color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.SemiBold)
                }
                Text("◉", color = Color.White, fontSize = 28.sp)
            }
            Spacer(Modifier.height(24.dp))
            if (drawer) {
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search apps") },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFB9B8FF), unfocusedBorderColor = Color.White.copy(.25f),
                        focusedLabelColor = Color.White, unfocusedLabelColor = Color.White
                    )
                )
                Spacer(Modifier.height(14.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(4), contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(shown, key = { it.packageName }) { app ->
                        AppTile(app) {
                            pm.getLaunchIntentForPackage(app.packageName)?.let(context::startActivity)
                        }
                    }
                }
            } else {
                Spacer(Modifier.weight(1f))
                Text("Your space.\nYour flow.", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Medium, lineHeight = 42.sp)
                Spacer(Modifier.height(22.dp))
                GlassPanel(Modifier.fillMaxWidth()) {
                    Text("Swipe up or tap the button to explore", color = Color.White.copy(.8f), fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        apps.take(4).forEach { app ->
                            Box(Modifier.weight(1f)) { AppTile(app) {
                                pm.getLaunchIntentForPackage(app.packageName)?.let(context::startActivity)
                            } }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                GlassPanel(Modifier.fillMaxWidth().clickable { drawer = true }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.Search, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("All apps", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
        if (drawer) {
            TextButton(
                onClick = { drawer = false; query = "" },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
            ) { Text("Close app drawer", color = Color.White) }
        }
    }
}

@Composable
private fun GlassPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = .10f))
            .padding(18.dp),
        content = content
    )
}

@Composable
private fun AppTile(app: LaunchableApp, onClick: () -> Unit) {
    val context = LocalContext.current
    val pm = context.packageManager
    val icon = remember(app.packageName) {
        runCatching { pm.getApplicationIcon(app.packageName) }.getOrNull()
    }
    Column(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            androidx.compose.foundation.Image(
                bitmap = androidx.compose.ui.graphics.asImageBitmap(icon.toBitmap()),
                contentDescription = app.label,
                modifier = Modifier.size(48.dp)
            )
        } else {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(.18f)), contentAlignment = Alignment.Center) {
                Text(app.label.take(1), color = Color.White, fontSize = 20.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(app.label, color = Color.White, fontSize = 11.sp, maxLines = 1)
    }
}
