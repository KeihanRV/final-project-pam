package com.example.final_project_pam.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.final_project_pam.data.model.SelectedApp
import com.example.final_project_pam.service.AppMonitorService
import com.example.final_project_pam.ui.theme.*
import com.example.final_project_pam.viewmodel.AppSelectUiState
import com.example.final_project_pam.viewmodel.AppSelectViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectScreen(
    viewModel: AppSelectViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPicker: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lockedPackages by viewModel.lockedPackages.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showConfirmDialog by remember { mutableStateOf(false) }
    var appToLaunch by remember { mutableStateOf<SelectedApp?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LaunchedEffect(uiState) {
        val msg = (uiState as? AppSelectUiState.Success)?.snackbarMessage ?: return@LaunchedEffect
        scope.launch {
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            viewModel.clearSnackbar()
        }
    }

    if (showConfirmDialog && appToLaunch != null) {
        val app = appToLaunch!!
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                appToLaunch = null
            },
            title = { Text("Mulai Sesi", fontWeight = FontWeight.Bold) },
            text = {
                Text("Buka ${app.appLabel} selama ${app.unscrollMinutes} menit?\n\nSetelah itu akan terkunci otomatis.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.launchApp(context, app.packageName, app.unscrollMinutes)
                        appToLaunch = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UnscrollPrimary)
                ) {
                    Text("Mulai", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).background(UnscrollPrimary, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                            Text("U", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unscroll", color = UnscrollBlack, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = onNavigateToPicker) { Icon(Icons.Outlined.Add, contentDescription = "Tambah", tint = UnscrollPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UnscrollBackground)
            )
        },
        bottomBar = {
            UnscrollBottomNavigation(currentRoute = "app_select", onDashboardClick = onNavigateBack, onAppSelectClick = { }, onProfileClick = { })
        },
        containerColor = UnscrollBackground
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is AppSelectUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = UnscrollPrimary)
                is AppSelectUiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center))
                is AppSelectUiState.Success -> {
                    if (state.selectedApps.isEmpty()) {
                        EmptyContent(onAddClick = onNavigateToPicker)
                    } else {
                        SelectedAppsContent(
                            selectedApps = state.selectedApps,
                            isSaving = state.isSaving,
                            pendingActionPackage = state.pendingActionPackage,
                            lockedPackages = lockedPackages,
                            onUpdateMinutes = { pkg, mins -> viewModel.updateMinutes(pkg, mins) },
                            onDeleteApp = { pkg -> viewModel.deleteApp(pkg) },
                            onLaunchApp = { app -> appToLaunch = app; showConfirmDialog = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedAppsContent(
    selectedApps: List<SelectedApp>,
    isSaving: Boolean,
    pendingActionPackage: String?,
    lockedPackages: Set<String>,
    onUpdateMinutes: (String, Int) -> Unit,
    onDeleteApp: (String) -> Unit,
    onLaunchApp: (SelectedApp) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            if (!isAccessibilityServiceEnabled(context)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = UnscrollSecondary.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth().clickable { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
                ) {
                    Text("⚠️ Aktifkan Izin Accessibility untuk fitur Auto-Redirect!", modifier = Modifier.padding(12.dp), color = UnscrollSecondary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        }

        item {
            Text("Aplikasi Pilihan", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = UnscrollBlack))
        }

        items(selectedApps, key = { it.packageName }) { app ->
            AppCardFull(
                app = app,
                context = context,
                isPending = pendingActionPackage == app.packageName && isSaving,
                isLocked = lockedPackages.contains(app.packageName),
                onMinutesChange = { mins -> onUpdateMinutes(app.packageName, mins) },
                onDelete = { onDeleteApp(app.packageName) },
                onLaunch = { onLaunchApp(app) }
            )
        }
    }
}

@Composable
private fun AppCardFull(
    app: SelectedApp,
    context: Context,
    isPending: Boolean,
    isLocked: Boolean,
    onMinutesChange: (Int) -> Unit,
    onDelete: () -> Unit,
    onLaunch: () -> Unit
) {
    var minutesText by remember(app.unscrollMinutes) { mutableStateOf(app.unscrollMinutes.toString()) }
    val hasChanged = minutesText != app.unscrollMinutes.toString()
    val icon = rememberAppIcon(context, app.packageName)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isLocked) Color(0xFFF5F5F5) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AppIconBox(icon = icon, context = context, packageName = app.packageName)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(app.appLabel, fontWeight = FontWeight.Bold, color = UnscrollBlack, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(app.packageName, color = UnscrollBlack.copy(alpha = 0.4f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp), enabled = !isPending) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = UnscrollSecondary, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth().height(36.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    val current = minutesText.toIntOrNull() ?: 0
                    if (current > 1) minutesText = (current - 1).toString()
                }, modifier = Modifier.size(24.dp), enabled = !isPending && !isLocked) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = UnscrollPrimary)
                }

                Box(modifier = Modifier.width(48.dp).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(UnscrollBlack.copy(alpha = 0.03f)).border(1.dp, UnscrollBlack.copy(alpha = 0.12f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    BasicTextField(
                        value = minutesText,
                        onValueChange = { v -> if (v.length <= 4) minutesText = v.filter { it.isDigit() } },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center, color = UnscrollBlack),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLocked
                    )
                }

                IconButton(onClick = {
                    val current = minutesText.toIntOrNull() ?: 0
                    if (current < 1440) minutesText = (current + 1).toString()
                }, modifier = Modifier.size(24.dp), enabled = !isPending && !isLocked) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = UnscrollPrimary)
                }

                Text(" mnt", color = UnscrollBlack.copy(alpha = 0.6f), fontSize = 12.sp)
                Spacer(modifier = Modifier.weight(1f))

                if (hasChanged && !isLocked) {
                    TextButton(onClick = { minutesText.toIntOrNull()?.let { onMinutesChange(it) } }, enabled = !isPending) {
                        Text("Simpan", color = UnscrollPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onLaunch,
                    modifier = Modifier.fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isLocked) Color.Gray else UnscrollPrimary),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isPending && !hasChanged && !isLocked
                ) {
                    if (isPending) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White)
                    } else if (isLocked) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Locked", color = Color.White, fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mulai", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppIconBox(icon: ImageBitmap?, context: Context, packageName: String) {
    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color.White).border(1.dp, UnscrollBlack.copy(alpha = 0.08f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
        if (icon != null) {
            Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Fit)
        } else {
            Text(text = packageName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = UnscrollPrimary)
        }
    }
}

@Composable
private fun EmptyContent(onAddClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.Apps, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
        Text("Belum ada aplikasi dipilih")
        Button(onClick = onAddClick) { Text("Tambah Aplikasi") }
    }
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedComponentName = ComponentName(context, AppMonitorService::class.java)
    val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
    return enabledServices.contains(expectedComponentName.flattenToString())
}
