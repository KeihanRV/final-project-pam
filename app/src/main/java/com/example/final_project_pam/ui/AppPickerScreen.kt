package com.example.final_project_pam.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    val isSaving = (uiState as? AppSelectUiState.Success)?.isSaving == true

    var searchQuery by remember { mutableStateOf("") }
    var showSaveConfirm by remember { mutableStateOf(false) }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val selectionCount by remember {
        derivedStateOf { pendingSelections.size }
    }

    LaunchedEffect(Unit) {
        viewModel.initPickerSelections()
    }

    if (showSaveConfirm) {
        AlertDialog(
            onDismissRequest = { showSaveConfirm = false },
            title = { Text("Simpan Perubahan", fontWeight = FontWeight.Bold) },
            text = { Text("Simpan $selectionCount aplikasi yang dipilih?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveConfirm = false
                        viewModel.saveSelectedApps(onComplete = onNavigateBack)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UnscrollPrimary)
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirm = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(UnscrollBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {

            TopAppBar(
                title = {
                    Text(
                        "Pilih Aplikasi",
                        fontWeight = FontWeight.Bold,
                        color = UnscrollBlack
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UnscrollBackground),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text("Cari aplikasi...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus pencarian")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Text(
                text = "$selectionCount dari ${installedApps.size} aplikasi dipilih",
                style = MaterialTheme.typography.bodySmall,
                color = UnscrollBlack.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )

            if (filteredApps.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Tidak ditemukan",
                        color = UnscrollBlack.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredApps,
                        key = { it.packageName }
                    ) { app ->
                        val isSelected = app.packageName in pendingSelections
                        AppPickerGridItem(
                            app = app,
                            isSelected = isSelected,
                            onClick = { viewModel.toggleSelection(app.packageName) }
                        )
                    }
                }
            }

            Surface(
                shadowElevation = 4.dp,
                color = UnscrollBackground
            ) {
                Button(
                    onClick = { showSaveConfirm = true },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    enabled = hasChanges && !isSaving,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UnscrollPrimary,
                        contentColor = Color.White,
                        disabledContainerColor = UnscrollPrimary.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Perubahan", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun AppPickerGridItem(
    app: InstalledApp,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val icon = rememberAppIcon(context, app.packageName)

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) UnscrollPrimary.copy(alpha = 0.08f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) UnscrollPrimary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(UnscrollBackground),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Image(
                    bitmap = icon,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(0.75f),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    app.label.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = UnscrollPrimary,
                    fontSize = 18.sp
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(1.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(UnscrollPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = app.label,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = UnscrollBlack
        )
    }
}
