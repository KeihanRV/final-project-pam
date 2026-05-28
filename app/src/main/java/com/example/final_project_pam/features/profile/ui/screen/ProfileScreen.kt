package com.example.final_project_pam.features.profile.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.final_project_pam.features.profile.ui.components.DeleteAccountDialog
import com.example.final_project_pam.features.profile.ui.components.ProfileHeader
import com.example.final_project_pam.features.profile.ui.components.ProfileInfoCard
import com.example.final_project_pam.features.profile.ui.state.ProfileUiState
import com.example.final_project_pam.features.profile.ui.viewmodel.ProfileViewModel
import com.example.final_project_pam.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val phoneNumber by viewModel.phoneNumber.collectAsStateWithLifecycle()
    val birthDate by viewModel.birthDate.collectAsStateWithLifecycle()
    val avatarUrl by viewModel.avatarUrl.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Image Picker Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    viewModel.uploadAvatar(bytes)
                }
                inputStream?.close()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Deleted) {
            onLogout()
        } else if (uiState is ProfileUiState.Error) {
            snackbarHostState.showSnackbar((uiState as ProfileUiState.Error).message)
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val formattedDate = formatter.format(Date(it))
                        viewModel.onBirthDateChange(formattedDate)
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = UnscrollPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = UnscrollPrimary,
                    todayContentColor = UnscrollPrimary,
                    todayDateBorderColor = UnscrollPrimary
                )
            )
        }
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            onConfirm = {
                viewModel.deleteProfile()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UnscrollBackground)
    ) {
        when (uiState) {
            is ProfileUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = UnscrollPrimary
                )
            }
            is ProfileUiState.Success, is ProfileUiState.Error, is ProfileUiState.Idle -> {
                val profile = (uiState as? ProfileUiState.Success)?.profile
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Profile",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = UnscrollBlack
                        )
                        if (!isEditMode && uiState is ProfileUiState.Success) {
                            TextButton(
                                onClick = { viewModel.toggleEditMode() },
                                colors = ButtonDefaults.textButtonColors(contentColor = UnscrollPrimary)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    ProfileHeader(
                        avatarUrl = avatarUrl,
                        name = name.ifEmpty { profile?.name },
                        email = profile?.userEmail,
                        isEditMode = isEditMode,
                        onAvatarClick = { launcher.launch("image/*") }
                    )

                    ProfileInfoCard(
                        isEditMode = isEditMode,
                        name = name,
                        onNameChange = viewModel::onNameChange,
                        email = profile?.userEmail,
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = viewModel::onPhoneNumberChange,
                        birthDate = birthDate,
                        onBirthDateClick = { showDatePicker = true }
                    )

                    if (isEditMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.toggleEditMode() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, UnscrollPrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = UnscrollPrimary)
                            ) {
                                Text("Cancel", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.updateProfile() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = UnscrollPrimary)
                            ) {
                                Text("Save Changes", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = UnscrollSecondary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                "Delete Profile / Account", 
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            else -> {}
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
