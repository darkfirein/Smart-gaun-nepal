package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.model.LocalNotice
import com.example.model.NoticeCategory
import com.example.model.NoticePriority
import java.time.LocalDate

@Composable
fun AdminNoticeDialog(
    initialNotice: LocalNotice?,
    language: GaunLanguage,
    onSave: (LocalNotice) -> Unit,
    onDelete: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(initialNotice?.title ?: "") }
    var titleEn by remember { mutableStateOf(initialNotice?.titleEn ?: "") }
    var description by remember { mutableStateOf(initialNotice?.description ?: "") }
    var descriptionEn by remember { mutableStateOf(initialNotice?.descriptionEn ?: "") }
    var publisher by remember { mutableStateOf(initialNotice?.publisher ?: "गाउँपालिका कार्यालय") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialNotice == null) "नयाँ सूचना थप्नुहोस्" else "सूचना सम्पादन गर्नुहोस्",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (initialNotice != null && onDelete != null) {
                    IconButton(
                        onClick = { onDelete(initialNotice.id); onDismiss() },
                        modifier = Modifier.testTag("admin_delete_notice_button")
                    ) {
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
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("शीर्षक (नेपाली)*") },
                    modifier = Modifier.fillMaxWidth().testTag("notice_title_ne_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = titleEn,
                    onValueChange = { titleEn = it },
                    label = { Text("Title (English)") },
                    modifier = Modifier.fillMaxWidth().testTag("notice_title_en_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("विस्तृत विवरण (नेपाली)*") },
                    modifier = Modifier.fillMaxWidth().testTag("notice_desc_ne_input"),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = publisher,
                    onValueChange = { publisher = it },
                    label = { Text("जारीकर्ता निकाय (Publisher)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        val notice = LocalNotice(
                            id = initialNotice?.id ?: "notice_${System.currentTimeMillis()}",
                            title = title,
                            titleEn = titleEn,
                            description = description,
                            descriptionEn = descriptionEn,
                            dateIso = initialNotice?.dateIso ?: LocalDate.now().toString(),
                            category = initialNotice?.category ?: NoticeCategory.CIVIC,
                            priority = initialNotice?.priority ?: NoticePriority.NORMAL,
                            publisher = publisher
                        )
                        onSave(notice)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank() && description.isNotBlank(),
                modifier = Modifier.testTag("admin_save_notice_button")
            ) {
                Text(text = "सुरक्षित गर्नुहोस् (Save)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "रद्द गर्नुहोस्")
            }
        },
        modifier = modifier.testTag("admin_notice_dialog")
    )
}
