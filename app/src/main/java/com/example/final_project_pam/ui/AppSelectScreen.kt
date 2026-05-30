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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun AppSelectScreen(
    viewModel: AppSelectViewModel,
    onNavigateToPicker: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lockedPackages by viewModel.lockedPackages.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showConfirmDialog by remember { mutableStateOf(false) }
    var appToLaunch by remember { mutableStateOf<SelectedApp?>(null) }
    var showAccessibilityWarning by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LaunchedEffect(uiState) {
        val msg = (uiState as? AppSelectUiState.Success)?.snackbarMessage ?: return@LaunchedEffect
        if (msg.isNotEmpty()) {
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

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is AppSelectUiState.Loading -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = UnscrollPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Memuat data...", color = UnscrollBlack.copy(alpha = 0.6f))
                }
            }
            is AppSelectUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.loadData() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UnscrollPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Coba Lagi", color = Color.White)
                    }
                }
            }
            is AppSelectUiState.Success -> {
                if (state.selectedApps.isEmpty()) {
                    EmptyContent(onAddClick = onNavigateToPicker)
                } else {
                    SelectedAppsContent(
                        selectedApps = state.selectedApps,
                        isSaving = state.isSaving,
                        pendingActionPackage = state.pendingActionPackage,
                        lockedPackages = lockedPackages,
                        showAccessibilityWarning = showAccessibilityWarning && !isAccessibilityServiceEnabled(context),
                        onDismissAccessibilityWarning = { showAccessibilityWarning = false },
                        onOpenAccessibilitySettings = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                        onUpdateMinutes = { pkg, mins -> viewModel.updateMinutes(pkg, mins) },
                        onDeleteApp = { pkg -> viewModel.deleteApp(pkg) },
                        onLaunchApp = { app -> appToLaunch = app; showConfirmDialog = true }
                    )

                    FloatingActionButton(
                        onClick = onNavigateToPicker,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        containerColor = UnscrollPrimary,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Aplikasi")
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        )
    }
}

@Composable
private fun SelectedAppsContent(
    selectedApps: List<SelectedApp>,
    isSaving: Boolean,
    pendingActionPackage: String?,
    lockedPackages: Set<String>,
    showAccessibilityWarning: Boolean,
    onDismissAccessibilityWarning: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onUpdateMinutes: (String, Int) -> Unit,
    onDeleteApp: (String) -> Unit,
    onLaunchApp: (SelectedApp) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showAccessibilityWarning) {
            item {
                Surface(
                    color = UnscrollSecondary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = UnscrollSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Aksesibilitas diperlukan",
                                color = UnscrollSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        TextButton(
                            onClick = onOpenAccessibilitySettings,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text("Aktifkan", color = UnscrollSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        IconButton(
                            onClick = onDismissAccessibilityWarning,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Aplikasi Pilihan",
                modifier = Modifier.padding(bottom = 4.dp),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = UnscrollBlack
                )
            )
        }

        items(selectedApps, key = { it.packageName }) { app ->
            AppCardFull(
                app = app,
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
    isPending: Boolean,
    isLocked: Boolean,
    onMinutesChange: (Int) -> Unit,
    onDelete: () -> Unit,
    onLaunch: () -> Unit
) {
    var minutesText by remember { mutableStateOf(app.unscrollMinutes.toString()) }
    LaunchedEffect(app.unscrollMinutes) {
        minutesText = app.unscrollMinutes.toString()
    }
    val hasChanged = minutesText != app.unscrollMinutes.toString()
    val parsedMinutes = minutesText.toIntOrNull() ?: 0
    val isValidRange = parsedMinutes in 1..1440

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isLocked) Color(0xFFF5F5F5) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AppIconBox(packageName = app.packageName)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        app.appLabel,
                        fontWeight = FontWeight.Bold,
                        color = UnscrollBlack,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (isLocked) "Terkunci" else "${app.unscrollMinutes} menit",
                        color = if (isLocked) UnscrollSecondary else UnscrollBlack.copy(alpha = 0.65f),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp), enabled = !isPending && !isLocked) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = UnscrollSecondary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val current = minutesText.toIntOrNull() ?: 0
                        if (current > 1) minutesText = (current - 1).toString()
                    },
                    modifier = Modifier.size(40.dp),
                    enabled = !isPending && !isLocked
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = UnscrollPrimary, modifier = Modifier.size(20.dp))
                }

                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(UnscrollBlack.copy(alpha = 0.03f))
                        .border(1.dp, UnscrollBlack.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = minutesText,
                        onValueChange = { v ->
                            if (v.length <= 4) {
                                val filtered = v.filter { it.isDigit() }
                                val num = filtered.toIntOrNull()
                                if (num == null || num <= 1440) {
                                    minutesText = filtered
                                }
                            }
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center, color = UnscrollBlack),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLocked
                    )
                }

                IconButton(
                    onClick = {
                        val current = minutesText.toIntOrNull() ?: 0
                        if (current < 1440) minutesText = (current + 1).toString()
                    },
                    modifier = Modifier.size(40.dp),
                    enabled = !isPending && !isLocked
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = UnscrollPrimary, modifier = Modifier.size(20.dp))
                }

                Text(" menit", color = UnscrollBlack.copy(alpha = 0.75f), fontSize = 13.sp)
                Spacer(modifier = Modifier.weight(1f))

                if (hasChanged && !isLocked && isValidRange) {
                    TextButton(onClick = { onMinutesChange(parsedMinutes) }, enabled = !isPending) {
                        Text("Simpan", color = UnscrollPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onLaunch,
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UnscrollPrimary,
                        disabledContainerColor = if (isLocked) Color(0xFF757575) else UnscrollPrimary.copy(alpha = 0.4f),
                        disabledContentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isPending && !hasChanged && !isLocked,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    if (isPending) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else if (isLocked) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Terkunci", color = Color.White, fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mulai", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppIconBox(packageName: String) {
    val context = LocalContext.current
    val icon = rememberAppIcon(context, packageName)

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, UnscrollBlack.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = packageName.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                color = UnscrollPrimary,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun EmptyContent(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Apps,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = UnscrollBlack.copy(alpha = 0.35f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Belum ada aplikasi dipilih",
            color = UnscrollBlack.copy(alpha = 0.7f),
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Pilih aplikasi yang ingin Anda batasi penggunaannya",
            color = UnscrollBlack.copy(alpha = 0.6f),
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = UnscrollPrimary,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tambah Aplikasi", color = Color.White)
        }
    }
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    return try {
        val expectedComponentName = ComponentName(context, AppMonitorService::class.java)
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        enabledServices.contains(expectedComponentName.flattenToString())
    } catch (e: Exception) {
        false
    }
}
