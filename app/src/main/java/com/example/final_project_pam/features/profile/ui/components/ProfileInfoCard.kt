package com.example.final_project_pam.features.profile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.final_project_pam.ui.theme.*

@Composable
fun ProfileInfoCard(
    isEditMode: Boolean,
    name: String,
    onNameChange: (String) -> Unit,
    email: String?,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    birthDate: String,
    onBirthDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            InfoItem(
                label = "Full Name",
                value = name,
                icon = Icons.Default.Person,
                isEditable = isEditMode,
                onValueChange = onNameChange
            )

            InfoItem(
                label = "Email Address",
                value = email ?: "Not set",
                icon = Icons.Default.Email,
                isEditable = false
            )

            InfoItem(
                label = "Phone Number",
                value = phoneNumber,
                icon = Icons.Default.Phone,
                isEditable = isEditMode,
                onValueChange = onPhoneNumberChange
            )

            if (isEditMode) {
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Birth Date", color = UnscrollPrimary) },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = UnscrollPrimary) },
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UnscrollPrimary,
                        unfocusedBorderColor = UnscrollBlack.copy(alpha = 0.12f),
                        cursorColor = UnscrollPrimary,
                        focusedLabelColor = UnscrollPrimary,
                        unfocusedLabelColor = UnscrollBlack.copy(alpha = 0.5f)
                    ),
                    trailingIcon = {
                        IconButton(onClick = onBirthDateClick) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Select Date", tint = UnscrollPrimary)
                        }
                    }
                )
            } else {
                InfoItem(
                    label = "Birth Date",
                    value = if (birthDate.isEmpty()) "Not set" else birthDate,
                    icon = Icons.Default.DateRange,
                    isEditable = false
                )
            }
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    icon: ImageVector,
    isEditable: Boolean,
    onValueChange: (String) -> Unit = {}
) {
    if (isEditable) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label, color = UnscrollPrimary) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = UnscrollPrimary) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = UnscrollPrimary,
                unfocusedBorderColor = UnscrollBlack.copy(alpha = 0.12f),
                cursorColor = UnscrollPrimary,
                focusedLabelColor = UnscrollPrimary,
                unfocusedLabelColor = UnscrollBlack.copy(alpha = 0.5f)
            )
        )
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(UnscrollTertiary, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = UnscrollPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = UnscrollBlack.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = UnscrollBlack,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
