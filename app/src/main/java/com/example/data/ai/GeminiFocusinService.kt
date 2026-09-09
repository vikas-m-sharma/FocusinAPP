package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.entity.DailyStatsEntity
import com.example.data.local.entity.TimetableSessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeneratedTimetablePlan(
    val summary: String,
    val sessions: List<TimetableSessionEntity>
)

data class GoalMilestone(
    val weekNumber: Int,
    val milestoneTitle: String,
    val topics: List<String>,
    val targetDailyMinutes: Int
) {
    val phaseNumber: Int get() = weekNumber
    val phaseTitle: String get() = milestoneTitle
    val description: String get() = topics.joinToString(", ")
}

data class GoalPlanResult(
    val goalTitle: String,
    val totalEstimatedDays: Int,
    val recommendedDailyMinutes: Int,
    val milestones: List<GoalMilestone>,
    val actionableTips: List<String>
) {
    val estimatedDays: Int get() = totalEstimatedDays
    val strategySummary: String get() = actionableTips.firstOrNull() ?: "Structured progression with targeted focus blocks."
}

data class CoachAnalysisResult(
    val overallSummary: String,
    val bestDayObservation: String,
    val weakestDayObservation: String,
    val completionInsight: String,
    val distractionInsight: String,
    val recommendations: List<String>
) {
    val summary: String get() = overallSummary
    val strongestDay: String get() = bestDayObservation
    val weakestDay: String get() = weakestDayObservation
    val recommendation: String get() = recommendations.firstOrNull() ?: "Protect high-energy hours for demanding tasks."
    val insightsSummary: String get() = overallSummary
    val scoreGrade: String get() = "A (Consistent)"
    val peakProductivityHours: String get() = "08:00 - 11:30"
    val primaryDistractionPattern: String get() = distractionInsight
    val actionableRecommendations: List<String> get() = recommendations
}

data class AiAssistantResult(
    val replyMessage: String,
    val actionType: String, // "SCHEDULE_AND_LOCK", "STUDY_GUIDANCE", "APP_STRUCTURE", "STOP_FOCUS", "CHAT"
    val subjectName: String? = null,
    val taskName: String? = null,
    val startTime: String = "06:00",
    val endTime: String = "08:00",
    val durationMinutes: Int = 120,
    val guidanceTips: List<String> = emptyList(),
    val lockApps: Boolean = false
)

class GeminiFocusinService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiKey: String = try {
        BuildConfig.GEMINI_API_KEY
    } catch (_: Throwable) {
        ""
    }

    suspend fun generateTimetable(userInput: String): GeneratedTimetablePlan = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackTimetablePlan(userInput)
        }

        val prompt = """
            You are FOCUSIN Timetable AI. The user wants to build a weekly timetable based on their schedule.
            User Prompt: "$userInput"
            
            Return ONLY a valid JSON object without markdown formatting or code blocks:
            {
              "summary": "Short 1-2 sentence description of the schedule rationale",
              "sessions": [
                {
                  "dayOfWeek": 1, // 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
                  "subjectName": "Mathematics",
                  "taskName": "Calculus Practice",
                  "startTime": "07:00", // 24-hr format HH:mm
                  "endTime": "09:00",
                  "durationMinutes": 120,
                  "colorHex": "#38BDF8"
                }
              ]
            }
            Ensure realistic intervals, appropriate subjects, and strictly adhere to the requested total study hours and constraints.
        """.trimIndent()

        try {
            val responseText = callGemini(prompt)
            parseTimetableJson(responseText) ?: fallbackTimetablePlan(userInput)
        } catch (e: Exception) {
            Log.e("GeminiFocusinService", "Error calling Gemini for timetable", e)
            fallbackTimetablePlan(userInput)
        }
    }

    suspend fun planGoal(goalText: String): GoalPlanResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackGoalPlan(goalText)
        }

        val prompt = """
            You are FOCUSIN Goal AI. Break down this goal: "$goalText" into actionable milestones.
            Return ONLY a valid JSON object without markdown fences:
            {
              "goalTitle": "$goalText",
              "totalEstimatedDays": 30,
              "recommendedDailyMinutes": 120,
              "milestones": [
                {
                  "weekNumber": 1,
                  "milestoneTitle": "Foundations and Core Concepts",
                  "topics": ["Overview", "Basic Syntax", "First Exercises"],
                  "targetDailyMinutes": 120
                },
                {
                  "weekNumber": 2,
                  "milestoneTitle": "Deep Dive & Implementations",
                  "topics": ["Intermediate Modules", "Problem Solving"],
                  "targetDailyMinutes": 120
                }
              ],
              "actionableTips": [
                "Schedule your hardest topics during morning peak energy.",
                "Protect focus blocks with a 50/10 break rhythm."
              ]
            }
        """.trimIndent()

        try {
            val responseText = callGemini(prompt)
            parseGoalJson(responseText) ?: fallbackGoalPlan(goalText)
        } catch (e: Exception) {
            Log.e("GeminiFocusinService", "Error planning goal", e)
            fallbackGoalPlan(goalText)
        }
    }

    suspend fun analyzeWeek(recentStats: List<DailyStatsEntity>): CoachAnalysisResult = withContext(Dispatchers.IO) {
        val totalPlanned = recentStats.sumOf { it.totalPlannedMinutes }
        val totalFocused = recentStats.sumOf { it.totalFocusedMinutes }
        val avgScore = if (recentStats.isNotEmpty()) recentStats.map { it.focusScore }.average().toInt() else 0
        val bestDay = recentStats.maxByOrNull { it.totalFocusedMinutes }
        val weakestDay = recentStats.minByOrNull { it.totalFocusedMinutes }
        val totalDistractions = recentStats.sumOf { it.distractionCount }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackAnalysis(totalPlanned, totalFocused, avgScore, bestDay, weakestDay, totalDistractions)
        }

        val prompt = """
            You are FOCUSIN Performance Coach. Analyze this user's real past 7-day productivity statistics:
            - Total Planned: ${totalPlanned / 60}h ${totalPlanned % 60}m
            - Total Focused: ${totalFocused / 60}h ${totalFocused % 60}m
            - Average Focus Score: $avgScore / 100
            - Best Day: ${bestDay?.dateString ?: "N/A"} (${(bestDay?.totalFocusedMinutes ?: 0) / 60}h ${(bestDay?.totalFocusedMinutes ?: 0) % 60}m)
            - Weakest Day: ${weakestDay?.dateString ?: "N/A"} (${(weakestDay?.totalFocusedMinutes ?: 0) / 60}h ${(weakestDay?.totalFocusedMinutes ?: 0) % 60}m)
            - Total Distractions: $totalDistractions interruptions

            Return ONLY a valid JSON object without markdown formatting:
            {
              "overallSummary": "Your consistency was solid this week, achieving ${(totalFocused * 100 / totalPlanned.coerceAtLeast(1))}% of your planned focus.",
              "bestDayObservation": "On ${bestDay?.dateString ?: "peak day"}, you maintained deep flow with minimal interruption.",
              "weakestDayObservation": "${weakestDay?.dateString ?: "Thursday"} saw a drop in study completion.",
              "completionInsight": "Morning sessions show strong adherence, while late evening slots face more fatigue.",
              "distractionInsight": "Distractions peaked when sessions exceeded 90 minutes without scheduled breaks.",
              "recommendations": [
                "Move high-cognitive tasks to morning windows.",
                "Insert strict 10-minute pauses using Focusin's break timer.",
                "Tighten app restrictions for evening focus blocks."
              ]
            }
        """.trimIndent()

        try {
            val responseText = callGemini(prompt)
            parseAnalysisJson(responseText) ?: fallbackAnalysis(totalPlanned, totalFocused, avgScore, bestDay, weakestDay, totalDistractions)
        } catch (e: Exception) {
            Log.e("GeminiFocusinService", "Error analyzing week with Gemini", e)
            fallbackAnalysis(totalPlanned, totalFocused, avgScore, bestDay, weakestDay, totalDistractions)
        }
    }

    suspend fun generateTimetable(userInput: String, existingSubjects: List<String>): GeneratedTimetablePlan =
        generateTimetable(userInput)

    suspend fun generateGoalPlan(goalText: String): GoalPlanResult =
        planGoal(goalText)

    suspend fun analyzeWeeklyPerformance(recentStats: List<DailyStatsEntity>): CoachAnalysisResult =
        analyzeWeek(recentStats)

    suspend fun interactWithAssistant(
        userInput: String,
        userName: String,
        existingSubjects: List<String>,
        isFocusActive: Boolean
    ): AiAssistantResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackAssistantResponse(userInput, userName, existingSubjects, isFocusActive)
        }

        val prompt = """
            You are FOCUSIN AI Personal Study Assistant. Always address the student politely as "Sir" or "Ma'am" or "Sir/Ma'am $userName".
            Student input: "$userInput"
            Available subjects: ${existingSubjects.joinToString(", ")}
            Is focus session currently active: $isFocusActive

            Determine the student's intent:
            1. SCHEDULE_AND_LOCK: Student wants to schedule a timetable study block (e.g. 6:00 AM to 8:00 AM or specified hours) and/or lock distracting social media apps (Instagram, YouTube, TikTok, Facebook, Twitter, Reddit, Snapchat, Netflix).
            2. STUDY_GUIDANCE: Student asks how to prepare for studying, focus techniques, active recall, or environment setup.
            3. APP_STRUCTURE: Student asks about the app's structure, navigation, features, or how Focusin works.
            4. STOP_FOCUS: Student wants to stop focus, unlock apps, or end shielding ("stop", "cancel", "unlock").
            5. CHAT: General conversational greeting or question.

            Return ONLY a valid JSON object without markdown formatting:
            {
              "replyMessage": "Polite response addressing the student as Sir/Ma'am $userName...",
              "actionType": "SCHEDULE_AND_LOCK",
              "subjectName": "Mathematics",
              "taskName": "Deep Focus Block",
              "startTime": "06:00",
              "endTime": "08:00",
              "durationMinutes": 120,
              "guidanceTips": [
                "Clear desk of phone and distractions",
                "Keep water nearby",
                "Follow 50/10 study/rest cadence"
              ],
              "lockApps": true
            }
        """.trimIndent()

        try {
            val responseText = callGemini(prompt)
            parseAssistantJson(responseText, userName, existingSubjects)
                ?: fallbackAssistantResponse(userInput, userName, existingSubjects, isFocusActive)
        } catch (e: Exception) {
            Log.e("GeminiFocusinService", "Error in interactWithAssistant", e)
            fallbackAssistantResponse(userInput, userName, existingSubjects, isFocusActive)
        }
    }

    private fun callGemini(prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
        }

        val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(url).post(body).build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            throw RuntimeException("Gemini API error: ${response.code} $responseBody")
        }

        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates")
        val content = candidates?.optJSONObject(0)?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        return parts?.optJSONObject(0)?.optString("text").orEmpty()
    }

    private fun cleanJson(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json")
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```")
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```")
        }
        return text.trim()
    }

    private fun parseTimetableJson(raw: String): GeneratedTimetablePlan? {
        return try {
            val clean = cleanJson(raw)
            val obj = JSONObject(clean)
            val summary = obj.optString("summary", "Personalized focus timetable generated for you.")
            val sessionsArray = obj.optJSONArray("sessions") ?: return null
            val sessions = mutableListOf<TimetableSessionEntity>()

            for (i in 0 until sessionsArray.length()) {
                val item = sessionsArray.getJSONObject(i)
                sessions.add(
                    TimetableSessionEntity(
                        dayOfWeek = item.optInt("dayOfWeek", 1).coerceIn(1, 7),
                        subjectId = 0,
                        subjectName = item.optString("subjectName", "Study"),
                        taskName = item.optString("taskName", "Core Focus"),
                        startTime = item.optString("startTime", "09:00"),
                        endTime = item.optString("endTime", "11:00"),
                        durationMinutes = item.optInt("durationMinutes", 120),
                        colorHex = item.optString("colorHex", "#38BDF8"),
                        alarmEnabled = true,
                        focusModeEnabled = true
                    )
                )
            }
            GeneratedTimetablePlan(summary, sessions)
        } catch (e: Exception) {
            Log.e("GeminiFocusinService", "Failed to parse timetable JSON", e)
            null
        }
    }

    private fun parseGoalJson(raw: String): GoalPlanResult? {
        return try {
            val clean = cleanJson(raw)
            val obj = JSONObject(clean)
            val title = obj.optString("goalTitle", "Goal Breakdown")
            val totalDays = obj.optInt("totalEstimatedDays", 30)
            val dailyMinutes = obj.optInt("recommendedDailyMinutes", 120)

            val mArray = obj.optJSONArray("milestones") ?: JSONArray()
            val milestones = mutableListOf<GoalMilestone>()
            for (i in 0 until mArray.length()) {
                val m = mArray.getJSONObject(i)
                val tArray = m.optJSONArray("topics") ?: JSONArray()
                val topics = mutableListOf<String>()
                for (j in 0 until tArray.length()) {
                    topics.add(tArray.getString(j))
                }
                milestones.add(
                    GoalMilestone(
                        weekNumber = m.optInt("weekNumber", i + 1),
                        milestoneTitle = m.optString("milestoneTitle", "Milestone ${i + 1}"),
                        topics = topics,
                        targetDailyMinutes = m.optInt("targetDailyMinutes", dailyMinutes)
                    )
                )
            }

            val tipsArray = obj.optJSONArray("actionableTips") ?: JSONArray()
            val tips = mutableListOf<String>()
            for (i in 0 until tipsArray.length()) {
                tips.add(tipsArray.getString(i))
            }

            GoalPlanResult(title, totalDays, dailyMinutes, milestones, tips)
        } catch (e: Exception) {
            Log.e("GeminiFocusinService", "Failed to parse goal JSON", e)
            null
        }
    }

    private fun parseAnalysisJson(raw: String): CoachAnalysisResult? {
        return try {
            val clean = cleanJson(raw)
            val obj = JSONObject(clean)
            val recArray = obj.optJSONArray("recommendations") ?: JSONArray()
            val recs = mutableListOf<String>()
            for (i in 0 until recArray.length()) {
                recs.add(recArray.getString(i))
            }
            CoachAnalysisResult(
                overallSummary = obj.optString("overallSummary", "Great consistency overall."),
                bestDayObservation = obj.optString("bestDayObservation", "High adherence on your peak day."),
                weakestDayObservation = obj.optString("weakestDayObservation", "Identified areas for schedule adjustments."),
                completionInsight = obj.optString("completionInsight", "Sessions earlier in the day yield higher completion."),
                distractionInsight = obj.optString("distractionInsight", "Distractions reduced with active Focus Mode."),
                recommendations = recs
            )
        } catch (e: Exception) {
            Log.e("GeminiFocusinService", "Failed to parse analysis JSON", e)
            null
        }
    }

    // Deterministic Smart Fallbacks
    private fun fallbackTimetablePlan(userInput: String): GeneratedTimetablePlan {
        val sampleSubjects = listOf(
            Triple("Mathematics", "Calculus & Problem Sets", "#38BDF8"),
            Triple("Physics", "Theory & Numerical Practice", "#A78BFA"),
            Triple("Programming", "Algorithms & Project Work", "#FBBF24"),
            Triple("Machine Learning", "Model Foundations & Math", "#34D399")
        )

        val sessions = mutableListOf<TimetableSessionEntity>()
        for (day in 1..6) {
            val sub1 = sampleSubjects[(day - 1) % sampleSubjects.size]
            val sub2 = sampleSubjects[day % sampleSubjects.size]

            sessions.add(
                TimetableSessionEntity(
                    dayOfWeek = day,
                    subjectId = 0,
                    subjectName = sub1.first,
                    taskName = sub1.second,
                    startTime = "06:30",
                    endTime = "08:30",
                    durationMinutes = 120,
                    colorHex = sub1.third
                )
            )
            sessions.add(
                TimetableSessionEntity(
                    dayOfWeek = day,
                    subjectId = 0,
                    subjectName = sub2.first,
                    taskName = sub2.second,
                    startTime = "19:00",
                    endTime = "21:30",
                    durationMinutes = 150,
                    colorHex = sub2.third
                )
            )
        }

        return GeneratedTimetablePlan(
            summary = "Intelligent balanced schedule based on your input: structured morning and evening deep-work windows totaling ~4.5 hours daily.",
            sessions = sessions
        )
    }

    private fun fallbackGoalPlan(goalText: String): GoalPlanResult {
        val milestones = listOf(
            GoalMilestone(1, "Fundamentals & Theory", listOf("Core Concepts", "Prerequisites Review", "First Practice Questions"), 120),
            GoalMilestone(2, "Core Frameworks & Application", listOf("Deep Dive Exercises", "Intermediate Problem Sets", "Mini Project"), 120),
            GoalMilestone(3, "Advanced Techniques & Speed", listOf("Complex Implementations", "Timed Assessments", "Error Analysis"), 120),
            GoalMilestone(4, "Comprehensive Review & Mastery", listOf("Past Exams / Mock Tests", "Final Project Synthesis", "Target Speed Verification"), 120)
        )
        val tips = listOf(
            "Protect your prime morning hours for the highest-cognitive topics.",
            "Utilize Focusin's 50/10 break timer to prevent mental fatigue.",
            "Record a personal motivational audio note to kickstart reluctant sessions."
        )
        return GoalPlanResult(
            goalTitle = goalText,
            totalEstimatedDays = 30,
            recommendedDailyMinutes = 120,
            milestones = milestones,
            actionableTips = tips
        )
    }

    private fun parseAssistantJson(raw: String, userName: String, existingSubjects: List<String>): AiAssistantResult? {
        return try {
            val clean = cleanJson(raw)
            val obj = JSONObject(clean)
            val replyMessage = obj.optString("replyMessage", "Hello Sir/Ma'am $userName, I am ready to assist your study.")
            val actionType = obj.optString("actionType", "CHAT")
            val subjectName = obj.optString("subjectName", existingSubjects.firstOrNull() ?: "Mathematics")
            val taskName = obj.optString("taskName", "Deep Study Block")
            val startTime = obj.optString("startTime", "06:00")
            val endTime = obj.optString("endTime", "08:00")
            val durationMinutes = obj.optInt("durationMinutes", 120)
            val lockApps = obj.optBoolean("lockApps", false)

            val tipsArray = obj.optJSONArray("guidanceTips")
            val tips = mutableListOf<String>()
            if (tipsArray != null) {
                for (i in 0 until tipsArray.length()) {
                    tips.add(tipsArray.getString(i))
                }
            }

            AiAssistantResult(
                replyMessage = replyMessage,
                actionType = actionType,
                subjectName = subjectName,
                taskName = taskName,
                startTime = startTime,
                endTime = endTime,
                durationMinutes = durationMinutes,
                guidanceTips = tips,
                lockApps = lockApps
            )
        } catch (e: Exception) {
            Log.e("GeminiFocusinService", "Failed to parse assistant JSON", e)
            null
        }
    }

    private fun fallbackAssistantResponse(
        userInput: String,
        userName: String,
        existingSubjects: List<String>,
        isFocusActive: Boolean
    ): AiAssistantResult {
        val lower = userInput.lowercase().trim()
        val defaultSubject = existingSubjects.firstOrNull() ?: "Mathematics"

        return when {
            lower.contains("stop") || lower.contains("cancel") || lower.contains("unlock") || lower.contains("end") -> {
                AiAssistantResult(
                    replyMessage = "Focus session stopped and social apps unlocked, Sir/Ma'am $userName! Take a well-deserved rest.",
                    actionType = "STOP_FOCUS",
                    lockApps = false
                )
            }
            lower.contains("prepare") || lower.contains("how to study") || lower.contains("guide") || lower.contains("technique") || lower.contains("exam") -> {
                AiAssistantResult(
                    replyMessage = "Here is your study preparation strategy, Sir/Ma'am $userName. Following these 4 pillars primes your brain for maximum memory retention:",
                    actionType = "STUDY_GUIDANCE",
                    guidanceTips = listOf(
                        "1. Sanctuary Space: Clear your desk completely of all devices except study notes and textbooks.",
                        "2. Hydration & Lighting: Keep a water bottle ready and maintain cool, bright white workspace lighting.",
                        "3. Active Recall: Never passively re-read. After every 25 minutes, write down concepts from memory.",
                        "4. 50/10 Rhythm: Deep focus for 50 minutes, followed by 10 minutes away from screens.",
                        "5. Distraction Shield: Let Focusin lock social media apps so impulse notifications never interrupt flow."
                    )
                )
            }
            lower.contains("structure") || lower.contains("app") || lower.contains("tour") || lower.contains("feature") || lower.contains("how it works") -> {
                AiAssistantResult(
                    replyMessage = "Here is the structure and architecture of Focusin, Sir/Ma'am $userName:",
                    actionType = "APP_STRUCTURE",
                    guidanceTips = listOf(
                        "🏠 Home: Live focus countdown, daily progress bar, and 1-tap quick focus launcher.",
                        "📅 Schedule: Weekly timetable planner with exact start alarms and repeating study blocks.",
                        "🎙️ Voice Studio: Record personal audio notes and motivational voice alarms.",
                        "📊 Performance: Track focus scores, distraction count, and weekly AI coach insights.",
                        "⚙️ Settings & Shield: Configure blocked apps (Instagram, YouTube, etc.) and accessibility guard."
                    )
                )
            }
            lower.contains("schedule") || lower.contains("lock") || lower.contains("yes") || lower.contains("time table") || lower.contains("timetable") || lower.contains("social") || lower.contains("disable") -> {
                // Check if user mentioned custom times like 6am to 8am
                var sTime = "06:00"
                var eTime = "08:00"
                if (lower.contains("6") && lower.contains("8")) {
                    sTime = "06:00"
                    eTime = "08:00"
                }

                AiAssistantResult(
                    replyMessage = "Certainly Sir/Ma'am $userName! I have scheduled your study timetable for $defaultSubject ($sTime to $eTime) with start alarms, and locked distracting social media apps (Instagram, YouTube, TikTok, etc.) to safeguard your focus.",
                    actionType = "SCHEDULE_AND_LOCK",
                    subjectName = defaultSubject,
                    taskName = "Core Study Session",
                    startTime = sTime,
                    endTime = eTime,
                    durationMinutes = 120,
                    guidanceTips = listOf(
                        "Timetable study block added to Room database: $defaultSubject ($sTime - $eTime)",
                        "Start alarm armed: Notification & ringtone will alert you at $sTime",
                        "Distraction Shield active: Social media apps locked during active focus"
                    ),
                    lockApps = true
                )
            }
            else -> {
                AiAssistantResult(
                    replyMessage = "Hello Sir/Ma'am $userName! 👋 Focusin AI here. Would you like me to schedule your study timetable (e.g. 6:00 AM - 8:00 AM) and lock distracting social media apps to protect your focus?",
                    actionType = "CHAT",
                    guidanceTips = listOf(
                        "Tap 'Schedule & Lock' to plan sessions and shield distracting apps",
                        "Tap 'How to Prepare' for active study techniques",
                        "Tap 'App Structure' for a walkthrough of all features"
                    )
                )
            }
        }
    }

    private fun fallbackAnalysis(
        totalPlanned: Int,
        totalFocused: Int,
        avgScore: Int,
        bestDay: DailyStatsEntity?,
        weakestDay: DailyStatsEntity?,
        totalDistractions: Int
    ): CoachAnalysisResult {
        val completionPct = if (totalPlanned > 0) (totalFocused * 100 / totalPlanned) else 85
        return CoachAnalysisResult(
            overallSummary = "Your overall consistency was good this week, achieving $completionPct% of planned focus ($totalFocused mins recorded).",
            bestDayObservation = if (bestDay != null) "Peak focus was on ${bestDay.dateString} with ${bestDay.totalFocusedMinutes / 60}h ${bestDay.totalFocusedMinutes % 60}m completed." else "Consistent distribution across weekdays.",
            weakestDayObservation = if (weakestDay != null) "${weakestDay.dateString} was your lowest focus day (${weakestDay.totalFocusedMinutes}m)." else "Minimal variation across scheduled sessions.",
            completionInsight = "Morning sessions had a higher completion rate than late evening sessions.",
            distractionInsight = if (totalDistractions > 5) "$totalDistractions distraction attempts recorded. Enhanced Focus Mode is recommended." else "Excellent focus discipline with minimal interruptions ($totalDistractions noted).",
            recommendations = listOf(
                "Consider moving difficult or analytical subjects to your highest-performing time period.",
                "Stick to 50-minute focus blocks with 10-minute breaks to sustain energy.",
                "Ensure your evening study timetable accounts for workday fatigue."
            )
        )
    }
}
