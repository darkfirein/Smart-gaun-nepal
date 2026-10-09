package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.GaunLanguage
import com.example.model.HealthFacility

@Composable
fun AdminHealthFacilityDialog(
    initialFacility: HealthFacility?,
    language: GaunLanguage,
    onSave: (HealthFacility) -> Unit,
    onDelete: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nameNe by remember { mutableStateOf(initialFacility?.nameNe ?: "") }
    var nameEn by remember { mutableStateOf(initialFacility?.nameEn ?: "") }
    var typeNe by remember { mutableStateOf(initialFacility?.typeNe ?: "स्वास्थ्य चौकी") }
    var contactNumber by remember { mutableStateOf(initialFacility?.contactNumber ?: "") }
    var is24Hours by remember { mutableStateOf(initialFacility?.is24Hours ?: false) }
    var doctorAvailable by remember { mutableStateOf(initialFacility?.doctorAvailable ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialFacility == null) "स्वास्थ्य संस्था थप्नुहोस्" else "स्वास्थ्य संस्था सम्पादन",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (initialFacility != null && onDelete != null) {
                    IconButton(onClick = { onDelete(initialFacility.id); onDismiss() }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = nameNe,
                    onValueChange = { nameNe = it },
                    label = { Text("संस्थाको नाम (नेपाली)*") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("Facility Name (English)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { contactNumber = it },
                    label = { Text("सम्पर्क नम्बर (Phone)*") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = is24Hours, onCheckedChange = { is24Hours = it })
                    Text(text = "२४/७ आकस्मिक सेवा उपलब्ध")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = doctorAvailable, onCheckedChange = { doctorAvailable = it })
                    Text(text = "एमबीबीएस डाक्टर उपलब्ध")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameNe.isNotBlank() && contactNumber.isNotBlank()) {
                        val facility = HealthFacility(
                            id = initialFacility?.id ?: "facility_${System.currentTimeMillis()}",
                            nameNe = nameNe,
                            nameEn = if (nameEn.isBlank()) nameNe else nameEn,
                            typeNe = typeNe,
                            typeEn = if (typeNe.contains("अस्पताल")) "Hospital" else "Health Post",
                            locationName = initialFacility?.locationName ?: "हेलम्बु",
                            contactNumber = contactNumber,
                            is24Hours = is24Hours,
                            doctorAvailable = doctorAvailable
                        )
                        onSave(facility)
                        onDismiss()
                    }
                },
                enabled = nameNe.isNotBlank() && contactNumber.isNotBlank(),
                modifier = Modifier.testTag("admin_save_facility_button")
            ) {
                Text(text = "सुरक्षित गर्नुहोस्")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("रद्द") }
        },
        modifier = modifier.testTag("admin_health_facility_dialog")
    )
}
