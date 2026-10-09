package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.CommunityService
import com.example.model.GaunLanguage

@Composable
fun ServiceDetailDialog(
    service: CommunityService,
    language: GaunLanguage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (language == GaunLanguage.NEPALI) service.titleNe else service.titleEn,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (language == GaunLanguage.NEPALI) service.descriptionNe else service.descriptionEn,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (language == GaunLanguage.NEPALI) "आवश्यक कागजातहरू:" else "Required Documents:",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )

                val docs = if (language == GaunLanguage.NEPALI) service.requiredDocumentsNe else service.requiredDocumentsEn
                docs.forEach { doc ->
                    Text(
                        text = doc,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (language == GaunLanguage.NEPALI) "दस्तुर / शुल्क: ${service.feeNe}" else "Fee: ${service.feeEn}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (language == GaunLanguage.NEPALI) "लाग्ने समय: ${service.processingTimeNe}" else "Processing Time: ${service.processingTimeEn}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (language == GaunLanguage.NEPALI) "जिम्मेवार अधिकारी: ${service.responsiblePersonNe}" else "Responsible Officer: ${service.responsiblePersonEn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_service_detail_button")
            ) {
                Text(text = if (language == GaunLanguage.NEPALI) "बुझें (Close)" else "Got it")
            }
        },
        modifier = modifier.testTag("service_detail_dialog")
    )
}
