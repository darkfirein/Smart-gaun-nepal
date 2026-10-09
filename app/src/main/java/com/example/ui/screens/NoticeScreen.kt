package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.GaunLanguage
import com.example.model.GaunLocation
import com.example.model.LocalNotice
import com.example.model.NoticeCategory
import com.example.ui.components.NoticeCard

@Composable
fun NoticeScreen(
    location: GaunLocation,
    notices: List<LocalNotice>,
    selectedCategory: NoticeCategory,
    searchQuery: String,
    language: GaunLanguage,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onSelectCategory: (NoticeCategory) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectNotice: (LocalNotice) -> Unit,
    onOpenCreateNotice: () -> Unit,
    onOpenEditNotice: (LocalNotice) -> Unit,
    modifier: Modifier = Modifier
) {
    val filtered = notices.filter { notice ->
        val matchesCategory = selectedCategory == NoticeCategory.ALL || notice.category == selectedCategory
        val matchesQuery = searchQuery.isBlank() ||
                notice.title.contains(searchQuery, ignoreCase = true) ||
                notice.titleEn.contains(searchQuery, ignoreCase = true) ||
                notice.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Box(modifier = modifier.fillMaxSize().testTag("notice_screen")) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("notice_screen_back_button")) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = if (language == GaunLanguage.NEPALI) "स्थानीय वडा सूचनाहरू" else "Official Ward Notices",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(text = if (language == GaunLanguage.NEPALI) "सूचना खोज्नुहोस्..." else "Search notices...")
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("notice_search_field"),
                singleLine = true
            )

            // Category Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(NoticeCategory.entries) { cat ->
                    val isSelected = selectedCategory == cat
                    AssistChip(
                        onClick = { onSelectCategory(cat) },
                        label = {
                            Text(
                                text = if (language == GaunLanguage.NEPALI) cat.titleNe else cat.titleEn,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("notice_category_chip_${cat.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Notice List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filtered.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (language == GaunLanguage.NEPALI) "कुनै सूचना भेटिएन" else "No notices found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filtered) { notice ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NoticeCard(
                                notice = notice,
                                language = language,
                                onClick = { onSelectNotice(notice) },
                                modifier = Modifier.weight(1f)
                            )
                            if (isAdmin) {
                                IconButton(onClick = { onOpenEditNotice(notice) }) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Notice")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isAdmin) {
            FloatingActionButton(
                onClick = onOpenCreateNotice,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("create_notice_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Notice")
            }
        }
    }
}
