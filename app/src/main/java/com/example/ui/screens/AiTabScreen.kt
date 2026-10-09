package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AiMessage
import com.example.data.InformationSourceCategory
import com.example.model.GaunLanguage
import com.example.model.GaunLocation

@Composable
fun AiTabScreen(
    location: GaunLocation,
    language: GaunLanguage,
    messages: List<AiMessage>,
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    isThinking: Boolean,
    isListening: Boolean,
    onStartVoice: () -> Unit,
    onSpeakMessage: (AiMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickTopics = if (language == GaunLanguage.NEPALI) {
        listOf(
            "नागरिकता सिफारिस लिन के के चाहिन्छ?",
            "जन्म दर्ता कति दिनभित्र गर्नुपर्छ?",
            "कृषि अनुदान कसरी पाइन्छ?",
            "स्वास्थ्य चौकीमा के के औषधि निशुल्क छ?",
            "आपतकालीन नम्बरहरू के के हुन्?"
        )
    } else {
        listOf(
            "Citizenship recommendation requirements?",
            "Birth registration timeline & fee?",
            "How to apply for agriculture subsidy?",
            "Free medicines at health post?",
            "Emergency SOS contact numbers?"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_tab_screen")
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (language == GaunLanguage.NEPALI) "गाउँ डिजिटल साथी (Rural Assistant)" else "Gaun Digital Civic Assistant",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${location.nameNe} • वडा सेवा तथा नागरिक सहायता",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Suggested Topics
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickTopics) { topic ->
                AssistChip(
                    onClick = { onSendMessage(topic) },
                    label = { Text(text = topic, fontSize = 12.sp) },
                    modifier = Modifier.testTag("quick_topic_chip")
                )
            }
        }

        // Chat Message Stream
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (language == GaunLanguage.NEPALI)
                                    "नमस्ते! मलाई सरकारी कागजात, स्वास्थ्य वा कृषि बारे सोध्नुहोस्।"
                                else
                                    "Namaste! Ask me anything about civic documents, health, or agriculture.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(messages) { msg ->
                    AiTabBubble(
                        message = msg,
                        language = language,
                        onSpeak = { onSpeakMessage(msg) }
                    )
                }
            }

            if (isThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == GaunLanguage.NEPALI) "सहायक सोच्दै हुनुहुन्छ..." else "Assistant is thinking...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                placeholder = {
                    Text(
                        text = if (language == GaunLanguage.NEPALI) "सोध्नुहोस् वा बोल्नुहोस्..." else "Type or use mic...",
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_tab_text_input"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onStartVoice,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer)
                    .testTag("ai_tab_voice_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice",
                    tint = if (isListening) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                    }
                },
                enabled = inputText.isNotBlank(),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .testTag("ai_tab_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun AiTabBubble(
    message: AiMessage,
    language: GaunLanguage,
    onSpeak: () -> Unit
) {
    val isUser = message.sender == AiMessage.MessageSender.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    val meta = message.metadata
                    val badgeLabel = meta?.category?.getBadgeLabel(language)
                        ?: InformationSourceCategory.AI_GENERAL.getBadgeLabel(language)

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (meta?.category) {
                            InformationSourceCategory.VERIFIED_LOCAL -> MaterialTheme.colorScheme.primaryContainer
                            InformationSourceCategory.LIVE_EXTERNAL -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .testTag("source_badge")
                    ) {
                        Text(
                            text = badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = when (meta?.category) {
                                InformationSourceCategory.VERIFIED_LOCAL -> MaterialTheme.colorScheme.onPrimaryContainer
                                InformationSourceCategory.LIVE_EXTERNAL -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )

                if (!isUser) {
                    val meta = message.metadata
                    if (meta != null && (meta.authority != null || meta.lastUpdated != null || !meta.isAvailable)) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val status = if (!meta.isAvailable) "⚠️ [अनुपलब्ध / Unavailable]" else "✓ प्रमाणित"
                        val authorityPart = meta.authority?.let { " • $it" } ?: ""
                        Text(
                            text = "$status${meta.sourceName?.let { " • $it" } ?: ""}$authorityPart",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onSpeak, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
