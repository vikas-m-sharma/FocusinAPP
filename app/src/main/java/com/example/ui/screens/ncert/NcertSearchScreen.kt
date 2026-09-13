package com.example.ui.screens.ncert

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NcertSearchResult
import com.example.data.repository.NcertRepository
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

/**
 * NCERT Catalog Search Screen.
 * Fast, live search across Classes 9-12, subjects, books, and chapter titles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NcertSearchScreen(
    ncertRepository: NcertRepository,
    onNavigateBack: () -> Unit,
    onSelectChapter: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedClassFilter by remember { mutableStateOf<Int?>(null) }

    val rawResults = remember(query) {
        if (query.isNotBlank()) ncertRepository.searchCatalog(query) else emptyList()
    }

    val filteredResults = remember(rawResults, selectedClassFilter) {
        if (selectedClassFilter == null) rawResults
        else rawResults.filter { it.chapter.classNumber == selectedClassFilter }
    }

    Scaffold(
        containerColor = Slate950,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Search NCERT Library",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("ncert_search_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ncert_search_text_input"),
                placeholder = {
                    Text("Search chapters, topics, formulas...", color = Color(0xFF64748B), fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = Slate800,
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Class Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedClassFilter == null,
                        onClick = { selectedClassFilter = null },
                        label = { Text("All Classes", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = CyanPrimary,
                            containerColor = Slate900,
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedClassFilter == null) CyanPrimary else Slate800,
                            enabled = true,
                            selected = selectedClassFilter == null
                        )
                    )
                }

                items(listOf(9, 10, 11, 12)) { classNum ->
                    val isSelected = selectedClassFilter == classNum
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedClassFilter = if (isSelected) null else classNum },
                        label = { Text("Class $classNum", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = CyanPrimary,
                            containerColor = Slate900,
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) CyanPrimary else Slate800,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Results List or Empty State
            if (query.isBlank()) {
                // Popular Suggestions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "POPULAR NCERT TOPICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF64748B)
                    )

                    val popularTopics = listOf(
                        "Thermodynamics",
                        "Motion in a Straight Line",
                        "Organic Chemistry",
                        "Units and Measurements",
                        "Cell The Unit of Life",
                        "Genetics and Evolution",
                        "Calculus & Integrals",
                        "Electrostatics"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        popularTopics.forEach { topic ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Slate900)
                                    .clickable { query = topic }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = topic,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            } else if (filteredResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "No NCERT chapters found for \"$query\"",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Text(
                            text = "${filteredResults.size} MATCHES FOUND",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(filteredResults, key = { it.chapter.id }) { result ->
                        SearchResultItemCard(
                            result = result,
                            onClick = { onSelectChapter(result.chapter.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItemCard(
    result: NcertSearchResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("search_result_${result.chapter.id}"),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyanPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = result.chapter.chapterNumberFormatted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${result.classDisplayName} • ${result.subjectName}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyanPrimary
                )
                Text(
                    text = result.chapter.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = result.bookTitle,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
