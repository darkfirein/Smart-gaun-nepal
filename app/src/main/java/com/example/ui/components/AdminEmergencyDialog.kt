package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.example.model.EmergencyContact
import com.example.model.GaunLanguage

@Composable
fun AdminEmergencyDialog(
    initialContact: EmergencyContact?,
    language: GaunLanguage,
    onSave: (EmergencyContact) -> Unit,
    onDelete: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nameNe by remember { mutableStateOf(initialContact?.nameNe ?: "") }
    var designationNe by remember { mutableStateOf(initialContact?.designationNe ?: "") }
    var contactNumber by remember { mutableStateOf(initialContact?.contactNumber ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialContact == null) "सम्पर्क व्यक्ति थप्नुहोस्" else "सम्पर्क सम्पादन",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (initialContact != null && onDelete != null) {
                    IconButton(onClick = { onDelete(initialContact.id); onDismiss() }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = nameNe,
                    onValueChange = { nameNe = it },
                    label = { Text("नाम (Name)*") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = designationNe,
                    onValueChange = { designationNe = it },
                    label = { Text("पद / जिम्मेवारी (Designation)*") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { contactNumber = it },
                    label = { Text("फोन नम्बर (Phone)*") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameNe.isNotBlank() && contactNumber.isNotBlank()) {
                        val contact = EmergencyContact(
                            id = initialContact?.id ?: "contact_${System.currentTimeMillis()}",
                            nameNe = nameNe,
                            nameEn = nameNe,
                            designationNe = designationNe,
                            designationEn = designationNe,
                            contactNumber = contactNumber,
                            isPrimaryHelpline = false
                        )
                        onSave(contact)
                        onDismiss()
                    }
                },
                enabled = nameNe.isNotBlank() && contactNumber.isNotBlank(),
                modifier = Modifier.testTag("admin_save_emergency_button")
            ) {
                Text(text = "सुरक्षित गर्नुहोस्")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("रद्द") }
        },
        modifier = modifier.testTag("admin_emergency_dialog")
    )
}
