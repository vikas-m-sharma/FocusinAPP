package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.FocusinViewModel
import org.json.JSONObject

data class CuratedPlaylist(
    val id: String,
    val title: String,
    val instructor: String,
    val category: String, // "BTECH", "NEET", "JEE", "JOB"
    val totalVideos: Int,
    val youtubeUrl: String,
    val tags: List<String>,
    val thumbnailColorHex: String = "#38BDF8"
)

val samplePlaylists = listOf(
    // B.Tech / IT
    CuratedPlaylist(
        id = "btech_dsa",
        title = "Data Structures & Algorithms A-Z",
        instructor = "striver (takeUforward)",
        category = "BTECH",
        totalVideos = 45,
        youtubeUrl = "https://www.youtube.com/results?search_query=striver+takeUforward+data+structures+and+algorithms+playlist",
        tags = listOf("Arrays", "Trees", "Graphs", "DP"),
        thumbnailColorHex = "#38BDF8"
    ),
    CuratedPlaylist(
        id = "btech_fullstack",
        title = "Complete Full Stack Web Dev (React & Node)",
        instructor = "CodeWithHarry",
        category = "BTECH",
        totalVideos = 60,
        youtubeUrl = "https://www.youtube.com/results?search_query=codewithharry+complete+web+development+course+playlist",
        tags = listOf("React", "Node.js", "MongoDB", "Express"),
        thumbnailColorHex = "#FBBF24"
    ),
    CuratedPlaylist(
        id = "btech_sysdesign",
        title = "System Design & Architecture Essentials",
        instructor = "Gate Smashers",
        category = "BTECH",
        totalVideos = 30,
        youtubeUrl = "https://www.youtube.com/results?search_query=gate+smashers+system+design+playlist",
        tags = listOf("LLD", "HLD", "Database Sharding", "Caching"),
        thumbnailColorHex = "#A78BFA"
    ),

    // NEET Medical
    CuratedPlaylist(
        id = "neet_bio",
        title = "Complete NEET Biology NCERT One-Shot",
        instructor = "Physics Wallah (PW)",
        category = "NEET",
        totalVideos = 50,
        youtubeUrl = "https://www.youtube.com/results?search_query=physics+wallah+neet+biology+one+shot+playlist",
        tags = listOf("Botany", "Zoology", "Genetics", "Human Physiology"),
        thumbnailColorHex = "#34D399"
    ),
    CuratedPlaylist(
        id = "neet_chem",
        title = "Organic Chemistry Reaction Mechanisms",
        instructor = "Unacademy NEET",
        category = "NEET",
        totalVideos = 35,
        youtubeUrl = "https://www.youtube.com/results?search_query=unacademy+neet+organic+chemistry+reaction+mechanisms",
        tags = listOf("Hydrocarbons", "Named Reactions", "Polymers"),
        thumbnailColorHex = "#F43F5E"
    ),

    // JEE IIT
    CuratedPlaylist(
        id = "jee_math",
        title = "IIT JEE Calculus & Coordinate Geometry",
        instructor = "Mohit Tyagi",
        category = "JEE",
        totalVideos = 55,
        youtubeUrl = "https://www.youtube.com/results?search_query=mohit+tyagi+jee+calculus+playlist",
        tags = listOf("Limits", "Derivatives", "Integrals", "Vectors"),
        thumbnailColorHex = "#38BDF8"
    ),
    CuratedPlaylist(
        id = "jee_physics",
        title = "JEE Advanced Physics Mechanics & Electricity",
        instructor = "Physics Galaxy (Ashish Arora)",
        category = "JEE",
        totalVideos = 50,
        youtubeUrl = "https://www.youtube.com/results?search_query=physics+galaxy+ashish+arora+jee+advanced+physics",
        tags = listOf("Kinematics", "Rotation", "Electroromagnetism"),
        thumbnailColorHex = "#38BDF8"
    ),

    // Job Placement
    CuratedPlaylist(
        id = "job_neetcode",
        title = "Top 75 LeetCode Coding Interview Patterns",
        instructor = "NeetCode",
        category = "JOB",
        totalVideos = 75,
        youtubeUrl = "https://www.youtube.com/results?search_query=neetcode+leetcode+75+blind+playlist",
        tags = listOf("Two Pointers", "Sliding Window", "Dynamic Programming"),
        thumbnailColorHex = "#FBBF24"
    ),
    CuratedPlaylist(
        id = "job_aptitude",
        title = "Campus Placement Quantitative Aptitude & Logic",
        instructor = "PrepInsta",
        category = "JOB",
        totalVideos = 30,
        youtubeUrl = "https://www.youtube.com/results?search_query=prepinsta+placement+quantitative+aptitude+playlist",
        tags = listOf("Probability", "Puzzles", "Data Interpretation"),
        thumbnailColorHex = "#A78BFA"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreparationScreen(
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val userSettings by viewModel.userSettings.collectAsState()

    var selectedCategory by remember { mutableStateOf("BTECH") }

    val categories = listOf(
        Pair("BTECH", "💻 IT & B.Tech"),
        Pair("NEET", "🩺 NEET Medical"),
        Pair("JEE", "📐 JEE / IIT Eng"),
        Pair("JOB", "💼 Job Placement")
    )

    val progressJson = userSettings?.playlistProgressJson ?: "{}"
    val progressMap = remember(progressJson) {
        try {
            val obj = JSONObject(progressJson)
            val map = mutableMapOf<String, Int>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = obj.optInt(k, 0)
            }
            map
        } catch (_: Exception) {
            mutableMapOf()
        }
    }

    val filteredPlaylists = samplePlaylists.filter { it.category == selectedCategory }

    Scaffold(
        containerColor = Slate950,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950,
                    titleContentColor = Color.White
                ),
                title = {
                    Column {
                        Text(
                            text = "PREPARATION & ROADMAPS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Curated top-tier courses & YouTube playlists",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
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
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Category Tabs Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { (catKey, catLabel) ->
                        item {
                            val isSelected = selectedCategory == catKey
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) CyanPrimary else Slate900)
                                    .clickable { selectedCategory = catKey }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = catLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Slate950 else Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Playlist Cards
            filteredPlaylists.forEach { playlist ->
                item(key = playlist.id) {
                    val watchedCount = progressMap[playlist.id] ?: 0
                    val progressFraction = (watchedCount.toFloat() / playlist.totalVideos.coerceAtLeast(1)).coerceIn(0f, 1f)

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
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(CyanPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = playlist.instructor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanPrimary
                                    )
                                    Text(
                                        text = playlist.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Tags
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            playlist.tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Slate800)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(tag, fontSize = 10.sp, color = Color(0xFF94A3B8))
                                }
                            }
                        }

                        // Progress Indicator
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Learning Progress",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "$watchedCount / ${playlist.totalVideos} Videos Watched",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanBright
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = CyanPrimary,
                                trackColor = Slate800
                            )
                        }

                        // Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playlist.youtubeUrl))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Watch on YouTube", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = {
                                    val newCount = (watchedCount + 1).coerceAtMost(playlist.totalVideos)
                                    val newMap = progressMap.toMutableMap()
                                    newMap[playlist.id] = newCount
                                    val newJson = JSONObject(newMap as Map<*, *>).toString()

                                    userSettings?.let { s ->
                                        viewModel.updateSettings(s.copy(playlistProgressJson = newJson))
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+1 Video Watched", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
}
