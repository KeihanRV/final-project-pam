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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(UnscrollPrimary, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("U", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unscroll", color = UnscrollBlack, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = UnscrollBlack)
                    }
                },
                actions = {
                    if (uiState is AppSelectUiState.Success) {
                        IconButton(onClick = onNavigateToPicker) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = "Tambah Aplikasi",
                                tint = UnscrollPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UnscrollBackground)
            )
        },
        bottomBar = {
            UnscrollBottomNavigation(
                currentRoute = "app_select",
                onDashboardClick = onNavigateBack,
                onAppSelectClick = { },
                onProfileClick = {
                    // TODO: navigate to profile
                }
            )
        },
        containerColor = UnscrollBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is AppSelectUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = UnscrollPrimary)
                    }
                }
                is AppSelectUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        isRetrying = state.isRetrying,
                        onRetry = { viewModel.loadData() }
                    )
                }
                is AppSelectUiState.Success -> {
                    if (state.selectedApps.isEmpty()) {
                        EmptyContent(onAddClick = onNavigateToPicker)
                    } else {
                        SelectedAppsContent(
                            selectedApps = state.selectedApps,
                            isSaving = state.isSaving,
                            pendingActionPackage = state.pendingActionPackage,
                            onUpdateMinutes = { pkg, mins -> viewModel.updateMinutes(pkg, mins) },
                            onDeleteApp = { pkg -> viewModel.deleteApp(pkg) },
                            onLaunchApp = { app -> viewModel.launchApp(context, app.packageName, app.unscrollMinutes) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    isRetrying: Boolean,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = UnscrollSecondary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = UnscrollBlack.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            enabled = !isRetrying,
            colors = ButtonDefaults.buttonColors(containerColor = UnscrollPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isRetrying) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Coba Lagi", color = Color.White)
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
            Icons.Outlined.Apps,
            contentDescription = null,
            tint = UnscrollBlack.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Belum ada aplikasi dipilih",
            color = UnscrollBlack.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tambahkan aplikasi untuk memulai Unscroll",
            color = UnscrollBlack.copy(alpha = 0.4f),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = UnscrollPrimary),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tambah Aplikasi", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SelectedAppsContent(
    selectedApps: List<SelectedApp>,
    isSaving: Boolean,
    pendingActionPackage: String?,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "⚠️ Klik di sini untuk mengaktifkan izin Unscroll Monitor di Accessibility Settings!",
                        modifier = Modifier.padding(12.dp),
                        color = UnscrollSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            Column {
                Text(
                    text = "Aplikasi Pilihan",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = UnscrollBlack
                    )
                )
                Text(
                    text = "${selectedApps.size} aplikasi terpilih",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = UnscrollBlack.copy(alpha = 0.6f)
                    )
                )
            }
        }

        items(selectedApps, key = { it.packageName }) { app ->
            AppCardFull(
                app = app,
                context = context,
                isPending = pendingActionPackage == app.packageName && isSaving,
                onMinutesChange = { mins -> onUpdateMinutes(app.packageName, mins) },
                onDelete = { onDeleteApp(app.packageName) },
                onLaunch = { onLaunchApp(app) }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun AppCardFull(
    app: SelectedApp,
    context: Context,
    isPending: Boolean,
    onMinutesChange: (Int) -> Unit,
    onDelete: () -> Unit,
    onLaunch: () -> Unit
) {
    var minutesText by remember(app.unscrollMinutes) {
        mutableStateOf(app.unscrollMinutes.toString())
    }
    val hasChanged = minutesText != app.unscrollMinutes.toString()
    val icon = rememberAppIcon(context, app.packageName)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconBox(icon = icon, context = context, packageName = app.packageName)

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appLabel,
                        fontWeight = FontWeight.Bold,
                        color = UnscrollBlack,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        color = UnscrollBlack.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp),
                    enabled = !isPending
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = UnscrollSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val current = minutesText.toIntOrNull() ?: 0
                        if (current > 1) minutesText = (current - 1).toString()
                    },
                    modifier = Modifier.size(24.dp),
                    enabled = !isPending
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = UnscrollPrimary, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(UnscrollBlack.copy(alpha = 0.03f))
                        .border(1.dp, UnscrollBlack.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = minutesText,
                        onValueChange = { v ->
                            if (v.length <= 4) minutesText = v.filter { it.isDigit() }
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center,
                            color = UnscrollBlack,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        val current = minutesText.toIntOrNull() ?: 0
                        if (current < 1440) minutesText = (current + 1).toString()
                    },
                    modifier = Modifier.size(24.dp),
                    enabled = !isPending
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = UnscrollPrimary, modifier = Modifier.size(18.dp))
                }

                Text(
                    text = " mnt",
                    color = UnscrollBlack.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 2.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                if (hasChanged) {
                    TextButton(
                        onClick = {
                            val mins = minutesText.toIntOrNull()
                            if (mins != null && mins in 1..1440) {
                                onMinutesChange(mins)
                            }
                        },
                        enabled = !isPending,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text("Simpan", color = UnscrollPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = onLaunch,
                    modifier = Modifier.fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(containerColor = UnscrollPrimary),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isPending && !hasChanged,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    if (isPending && !hasChanged) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mulai", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppIconBox(icon: ImageBitmap?, context: Context, packageName: String) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, UnscrollBlack.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = context.getAppLabel(packageName).firstOrNull()?.uppercase() ?: "?",
                fontWeight = FontWeight.Bold,
                color = UnscrollPrimary,
                fontSize = 14.sp
            )
        }
    }
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedComponentName = ComponentName(context, AppMonitorService::class.java)
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.contains(expectedComponentName.flattenToString())
}