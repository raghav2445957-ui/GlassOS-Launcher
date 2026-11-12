package com.glassos.launcher

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class LauncherApp(val name: String, val packageName: String, val icon: android.graphics.drawable.Drawable)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.rgb(13, 16, 27)
        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setContent { GlassOS() }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GlassOS() {
    val context = LocalContext.current
    val pm = context.packageManager
    var apps by remember { mutableStateOf(emptyList<LauncherApp>()) }
    var drawerOpen by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var now by remember { mutableStateOf(Date()) }
    var page by remember { mutableIntStateOf(0) }
    var dragAmount by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            delay(1000)
        }
    }
    LaunchedEffect(Unit) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        val resolved = pm.queryIntentActivities(intent, 0)
        apps = resolved.mapNotNull { item ->
            runCatching {
                LauncherApp(item.loadLabel(pm).toString(), item.activityInfo.packageName,
                    pm.getApplicationIcon(item.activityInfo.packageName))
            }.getOrNull()
        }.distinctBy { it.packageName }.sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    BackHandler(drawerOpen) { drawerOpen = false; search = "" }
    val filtered = apps.filter { it.name.contains(search, true) }
    val favorites = remember(apps) {
        val preferred = listOf("phone", "messages", "chrome", "camera", "settings", "youtube")
        preferred.mapNotNull { key -> apps.firstOrNull { it.name.contains(key, true) } }
            .distinctBy { it.packageName }.take(5)
    }
    val clock = remember(now) { SimpleDateFormat("h:mm", Locale.getDefault()).format(now) }
    val date = remember(now) { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(now) }

    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF25213D), Color(0xFF101522))))
    ) {
        // Soft ambient light, deliberately subtle to keep icons readable.
        Box(Modifier.fillMaxWidth().height(360.dp).align(Alignment.TopCenter)
            .background(Brush.radialGradient(listOf(Color(0x553F70C9), Color.Transparent), radius = 700f)))
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 22.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(SimpleDateFormat("EEEE", Locale.getDefault()).format(now).uppercase(Locale.getDefault()),
                    color = Color.White.copy(.62f), fontSize = 10.sp, letterSpacing = 2.4.sp, fontWeight = FontWeight.Bold)
                    Text(date, color = Color.White.copy(.9f), fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Wifi, null, tint = Color.White.copy(.8f), modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Icon(Icons.Rounded.BatteryFull, null, tint = Color.White.copy(.8f), modifier = Modifier.size(19.dp))
                }
            }

            AnimatedVisibility(visible = !drawerOpen, modifier = Modifier.weight(1f).pointerInput(drawerOpen) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, amount -> dragAmount += amount },
                    onDragEnd = {
                        if (dragAmount < -45f) drawerOpen = true
                        dragAmount = 0f
                    },
                    onDragCancel = { dragAmount = 0f }
                )
            }) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(38.dp))
                    Text(clock, color = Color.White, fontSize = 76.sp, lineHeight = 82.sp,
                        fontWeight = FontWeight.Light, letterSpacing = (-3).sp)
                    Text(date, color = Color.White.copy(.65f), fontSize = 14.sp)
                    Spacer(Modifier.height(30.dp))
                    GlassCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("YOUR SPACE", color = Color(0xFFB8C8FF), fontSize = 10.sp,
                                    letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                Text("Everything in\none place.", color = Color.White, fontSize = 23.sp,
                                    fontWeight = FontWeight.SemiBold, lineHeight = 27.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("Swipe up to open your apps", color = Color.White.copy(.62f), fontSize = 12.sp)
                            }
                            Box(Modifier.size(66.dp).clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFF8A9EFF), Color(0xFFB7A0FF)))),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.AutoAwesome, null, tint = Color(0xFF171A31), modifier = Modifier.size(31.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Favourites", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text("EDIT", color = Color(0xFFB9C5FF), fontSize = 10.sp, letterSpacing = 1.5.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        favorites.take(4).forEach { app ->
                            Box(Modifier.weight(1f)) { AppIcon(app, true) { launch(context, app.packageName) } }
                        }
                        if (favorites.isEmpty()) {
                            repeat(4) { Box(Modifier.weight(1f)) { EmptyIcon() } }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    GlassCard(Modifier.fillMaxWidth().clickable { drawerOpen = true }) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Search, null, tint = Color.White.copy(.85f))
                            Spacer(Modifier.width(12.dp))
                            Text("Search apps", color = Color.White.copy(.65f), fontSize = 14.sp)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Rounded.KeyboardArrowUp, null, tint = Color.White.copy(.65f))
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    GlassCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            val dockApps = favorites.take(4)
                            dockApps.forEach { app -> DockIcon(app) { launch(context, app.packageName) } }
                            Box(Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(.13f))
                                .clickable { drawerOpen = true }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Apps, null, tint = Color.White, modifier = Modifier.size(23.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        repeat(2) { index ->
                            Box(Modifier.size(if (page == index) 16.dp else 5.dp, 5.dp).clip(CircleShape)
                                .background(if (page == index) Color.White else Color.White.copy(.35f))
                                .clickable { page = index })
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            AnimatedVisibility(visible = drawerOpen, modifier = Modifier.weight(1f)) {
                Column(Modifier.fillMaxSize().padding(top = 24.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("App library", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                            Text("${apps.size} apps installed", color = Color.White.copy(.55f), fontSize = 12.sp)
                        }
                        IconButton(onClick = { drawerOpen = false; search = "" }) {
                            Icon(Icons.Rounded.Close, "Close app drawer", tint = Color.White)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(.12f)).padding(horizontal = 15.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Search, null, tint = Color.White.copy(.7f))
                        Spacer(Modifier.width(10.dp))
                        BasicTextField(value = search, onValueChange = { search = it },
                            modifier = Modifier.weight(1f), singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                            cursorBrush = SolidColor(Color.White),
                            decorationBox = { inner ->
                                if (search.isEmpty()) Text("Find an app", color = Color.White.copy(.48f), fontSize = 15.sp)
                                inner()
                            })
                        if (search.isNotEmpty()) Icon(Icons.Rounded.Cancel, "Clear search",
                            tint = Color.White.copy(.6f), modifier = Modifier.clickable { search = "" })
                    }
                    Spacer(Modifier.height(14.dp))
                    LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 76.dp),
                        modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtered, key = { it.packageName }) { app ->
                            AppIcon(app, false) { launch(context, app.packageName) }
                        }
                    }
                }
            }
        }
        if (!drawerOpen) {
            Text("GLASSOS", modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp).zIndex(2f),
                color = Color.White.copy(.22f), fontSize = 8.sp, letterSpacing = 3.sp)
        }
    }
}

private fun launch(context: android.content.Context, packageName: String) {
    runCatching { context.packageManager.getLaunchIntentForPackage(packageName)?.let { context.startActivity(it) } }
}

@Composable
private fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.clip(RoundedCornerShape(25.dp))
        .background(Brush.linearGradient(listOf(Color.White.copy(.15f), Color.White.copy(.07f))))
        .padding(17.dp), content = content)
}

@Composable
private fun AppIcon(app: LauncherApp, large: Boolean, onClick: () -> Unit) {
    val iconSize = if (large) 54.dp else 48.dp
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        androidx.compose.foundation.Image(bitmap = remember(app.packageName) { app.icon.toBitmap(128, 128).asImageBitmap() },
            contentDescription = app.name, modifier = Modifier.size(iconSize))
        Spacer(Modifier.height(7.dp))
        Text(app.name, color = Color.White.copy(.92f), fontSize = if (large) 10.sp else 11.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

@Composable
private fun DockIcon(app: LauncherApp, onClick: () -> Unit) {
    androidx.compose.foundation.Image(bitmap = remember(app.packageName) { app.icon.toBitmap(128, 128).asImageBitmap() },
        contentDescription = app.name, modifier = Modifier.size(42.dp).clickable(onClick = onClick))
}

@Composable
private fun EmptyIcon() {
    Box(Modifier.size(54.dp).clip(RoundedCornerShape(17.dp)).background(Color.White.copy(.10f)))
}
