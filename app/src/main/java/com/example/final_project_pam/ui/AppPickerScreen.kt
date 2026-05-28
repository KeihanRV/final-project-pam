package com.example.final_project_pam.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.final_project_pam.data.model.InstalledApp
import com.example.final_project_pam.ui.theme.*
import com.example.final_project_pam.viewmodel.AppSelectUiState
import com.example.final_project_pam.viewmodel.AppSelectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerScreen(
    viewModel: AppSelectViewModel,
    onNavigateBack: () -> Unit
) {
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val pendingSelections by viewModel.pendingSelections.collectAsStateWithLifecycle()
    val hasChanges by viewModel.hasChanges.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isSaving = (uiState as? AppSelectUiState.Success)?.isSaving == true

    // Optimasi: Gunakan derivedStateOf agar tidak re-render seluruh list saat jumlah pilihan berubah
    val selectionCount by remember {
        derivedStateOf { pendingSelections.size }
    }

    LaunchedEffect(Unit) {
        viewModel.initPickerSelections()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = UnscrollBackground,
        topBar = {
            TopAppBar(
                title = { Text("Pilih Aplikasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UnscrollBackground)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Tampilan List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "$selectionCount aplikasi dipilih",
                        style = MaterialTheme.typography.bodyMedium,
                        color = UnscrollBlack.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Gunakan key agar scrolling lebih mulus
                items(installedApps, key = { it.packageName }) { app ->
                    val isSelected = app.packageName in pendingSelections

                    AppPickerListRow(
                        app = app,
                        isSelected = isSelected,
                        onClick = { viewModel.toggleSelection(app.packageName) }
                    )
                }
            }

            // Tombol Simpan
            Surface(shadowElevation = 8.dp, color = UnscrollBackground) {
                Button(
                    onClick = { viewModel.saveSelectedApps(onComplete = onNavigateBack) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    enabled = hasChanges && !isSaving,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UnscrollPrimary)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppPickerListRow(
    app: InstalledApp,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val icon = rememberAppIcon(context, app.packageName)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) UnscrollPrimary.copy(alpha = 0.05f) else Color.White
        ),
        border = if (isSelected) BorderStroke(2.dp, UnscrollPrimary) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon App
            Box(
                modifier = Modifier.size(45.dp).clip(RoundedCornerShape(10.dp)).background(UnscrollBackground),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Image(bitmap = icon, contentDescription = null, modifier = Modifier.fillMaxSize(0.8f))
                } else {
                    Text(app.label.take(1).uppercase(), fontWeight = FontWeight.Bold, color = UnscrollPrimary)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Nama & Package
            Column(modifier = Modifier.weight(1f)) {
                Text(app.label, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text(app.packageName, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            // Checkbox bulat
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) UnscrollPrimary else Color.Transparent)
                    .border(1.5.dp, if (isSelected) UnscrollPrimary else Color.LightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}
