package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.QuestionEntity
import com.example.data.model.toNeetQuestion
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.FocusinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit
) {
    val bookmarkedEntities by viewModel.getBookmarkedQuestions().collectAsState(initial = emptyList())
    val bookmarkedQuestions = remember(bookmarkedEntities) {
        bookmarkedEntities.map { it.toNeetQuestion() }
    }
    var selectedSubjectFilter by remember { mutableStateOf("ALL") }
    val expandedState = remember { mutableStateMapOf<String, Boolean>() }

    val filteredQuestions = when (selectedSubjectFilter) {
        "PHYSICS" -> bookmarkedQuestions.filter { it.subjectName.uppercase().contains("PHYSIC") }
        "CHEMISTRY" -> bookmarkedQuestions.filter { it.subjectName.uppercase().contains("CHEM") }
        "BIOLOGY" -> bookmarkedQuestions.filter { it.subjectName.uppercase().contains("BIO") }
        else -> bookmarkedQuestions
    }

    Scaffold(
        containerColor = Slate950,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "Bookmarks",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${bookmarkedQuestions.size} Saved Questions",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Filter Pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to "All",
                        "PHYSICS" to "Physics",
                        "CHEMISTRY" to "Chemistry",
                        "BIOLOGY" to "Biology"
                    ).forEach { (key, label) ->
                        val isSel = selectedSubjectFilter == key
                        Surface(
                            modifier = Modifier
                                .clickable { selectedSubjectFilter = key }
                                .border(1.dp, if (isSel) CyanPrimary else Slate800, RoundedCornerShape(20.dp)),
                            color = if (isSel) CyanPrimary.copy(alpha = 0.15f) else Slate900,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) CyanPrimary else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            if (filteredQuestions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("No Bookmarked Questions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bookmark tricky questions during practice to review them here anytime.",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            } else {
                items(filteredQuestions) { question ->
                    val isExpanded = expandedState[question.id] == true

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate800, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        color = CyanPrimary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = question.subjectName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    val badgeLabel = when (question.sourceVerificationStatus) {
                                        "VERIFIED" -> if (question.sourceExam != null && question.pyqYear != null) "${question.sourceExam} ${question.pyqYear}" else "VERIFIED"
                                        "UNVERIFIED" -> if (question.sourceExam != null && question.pyqYear != null) "${question.sourceExam} ${question.pyqYear} (Unverified)" else "UNVERIFIED"
                                        "SAMPLE" -> "SAMPLE"
                                        else -> "PRACTICE"
                                    }
                                    Surface(
                                        color = if (question.sourceVerificationStatus == "VERIFIED") EmeraldSuccess.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = badgeLabel,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (question.sourceVerificationStatus == "VERIFIED") EmeraldSuccess else Color(0xFF94A3B8),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.toggleQuestionBookmark(question.id, false)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = "Remove bookmark",
                                        tint = Color(0xFFFBBF24)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = question.questionText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Options Preview
                            question.options.forEachIndexed { optIndex, optText ->
                                val optKey = listOf("A", "B", "C", "D").getOrElse(optIndex) { "A" }
                                val isCorrect = isExpanded && optIndex == question.correctOptionIndex
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .border(if (isCorrect) 1.dp else 0.dp, if (isCorrect) EmeraldSuccess else Color.Transparent, RoundedCornerShape(8.dp)),
                                    color = if (isCorrect) EmeraldSuccess.copy(alpha = 0.1f) else Slate850,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$optKey. $optText",
                                            fontSize = 13.sp,
                                            color = if (isCorrect) EmeraldSuccess else Color(0xFFCBD5E1),
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isCorrect) {
                                            Text("✓ Answer", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Toggle Explanation
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedState[question.id] = !isExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isExpanded) "Hide Explanation" else "Show Answer & Explanation",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyanPrimary
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    Surface(
                                        color = Slate800.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Explanation:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(question.explanation, fontSize = 12.sp, color = Color(0xFFCBD5E1), lineHeight = 17.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
