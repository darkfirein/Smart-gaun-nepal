package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.model.GaunLanguage

@Composable
fun EmergencyDialerDialog(
    phoneNumber: String,
    title: String,
    language: GaunLanguage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call",
                tint = MaterialTheme.colorScheme.secondary
            )
        },
        title = {
            Text(
                text = if (language == GaunLanguage.NEPALI) "फोन कल पुष्टिकरण" else "Call Confirmation",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = if (language == GaunLanguage.NEPALI)
                    "$title ($phoneNumber) मा सिधै फोन कल गर्न चाहनुहुन्छ?"
                else
                    "Do you want to dial $title ($phoneNumber)?"
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.testTag("confirm_dial_button")
            ) {
                Text(
                    text = if (language == GaunLanguage.NEPALI) "कल गर्नुहोस्" else "Call Now",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_dial_button")
            ) {
                Text(text = if (language == GaunLanguage.NEPALI) "रद्द गर्नुहोस्" else "Cancel")
            }
        },
        modifier = modifier.testTag("emergency_dialer_dialog")
    )
}
