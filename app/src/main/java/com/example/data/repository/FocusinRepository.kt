package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.AchievementEntity
import com.example.data.local.entity.DailyStatsEntity
import com.example.data.local.entity.FocusSessionRecordEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TimetableSessionEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.local.entity.VoiceRecordingEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min

class FocusinRepository(private val database: AppDatabase) {

    private val subjectDao = database.subjectDao()
    private val timetableDao = database.timetableDao()
    private val focusSessionDao = database.focusSessionDao()
    private val dailyStatsDao = database.dailyStatsDao()
    private val voiceRecordingDao = database.voiceRecordingDao()
    private val achievementDao = database.achievementDao()
    private val userSettingsDao = database.userSettingsDao()

    // Flows
    val allSubjects: Flow<List<SubjectEntity>> = subjectDao.getAllSubjects()
    val allTimetableSessions: Flow<List<TimetableSessionEntity>> = timetableDao.getAllSessions()
    val allRecords: Flow<List<FocusSessionRecordEntity>> = focusSessionDao.getAllRecords()
    val recentWeekStats: Flow<List<DailyStatsEntity>> = dailyStatsDao.getRecentWeekStats()
    val allVoiceRecordings: Flow<List<VoiceRecordingEntity>> = voiceRecordingDao.getAllRecordings()
    val allAchievements: Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()
    val userSettings: Flow<UserSettingsEntity?> = userSettingsDao.getUserSettings()

    fun getSessionsForDay(dayOfWeek: Int): Flow<List<TimetableSessionEntity>> =
        timetableDao.getSessionsForDay(dayOfWeek)

    fun getTodayStats(dateString: String = getTodayDateString()): Flow<DailyStatsEntity?> =
        dailyStatsDao.getStatsForDate(dateString)

    // Subjects
    suspend fun insertSubject(name: String, colorHex: String, iconName: String, weeklyHours: Float = 10f): Long {
        return subjectDao.insertSubject(
            SubjectEntity(
                name = name,
                colorHex = colorHex,
                iconName = iconName,
                targetWeeklyHours = weeklyHours
            )
        )
    }

    suspend fun updateSubject(subject: SubjectEntity) = subjectDao.updateSubject(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = subjectDao.deleteSubject(subject)

    // Timetable
    suspend fun insertTimetableSession(session: TimetableSessionEntity): Long =
        timetableDao.insertSession(session)

    suspend fun updateTimetableSession(session: TimetableSessionEntity) =
        timetableDao.updateSession(session)

    suspend fun deleteTimetableSession(session: TimetableSessionEntity) =
        timetableDao.deleteSession(session)

    suspend fun deleteTimetableSessionById(id: Long) =
        timetableDao.deleteSessionById(id)

    suspend fun duplicateTimetableSession(sessionId: Long, targetDay: Int) {
        val original = timetableDao.getSessionById(sessionId) ?: return
        timetableDao.insertSession(original.copy(id = 0, dayOfWeek = targetDay))
    }

    // Voice Recordings
    suspend fun insertVoiceRecording(title: String, filePath: String, durationSec: Int): Long {
        return voiceRecordingDao.insertRecording(
            VoiceRecordingEntity(
                title = title,
                filePath = filePath,
                durationSeconds = durationSec
            )
        )
    }

    suspend fun updateVoiceRecording(recording: VoiceRecordingEntity) =
        voiceRecordingDao.updateRecording(recording)

    suspend fun deleteVoiceRecording(recording: VoiceRecordingEntity) =
        voiceRecordingDao.deleteRecording(recording)

    // User Settings
    suspend fun getUserSettingsSync(): UserSettingsEntity {
        var current = userSettingsDao.getUserSettingsSync()
        if (current == null) {
            current = UserSettingsEntity()
            userSettingsDao.insertOrUpdate(current)
        }
        return current
    }

    suspend fun updateUserSettings(settings: UserSettingsEntity) =
        userSettingsDao.insertOrUpdate(settings)

    // Record Completed Focus Session
    suspend fun recordFocusSession(
        subjectId: Long,
        subjectName: String,
        taskName: String,
        plannedDurationMinutes: Int,
        actualDurationSeconds: Long,
        startTimeMillis: Long,
        endTimeMillis: Long,
        isCompleted: Boolean,
        earlyEndReason: String?,
        distractionCount: Int,
        mode: String
    ): Long {
        val todayStr = getTodayDateString()
        val record = FocusSessionRecordEntity(
            subjectId = subjectId,
            subjectName = subjectName,
            taskName = taskName,
            plannedDurationMinutes = plannedDurationMinutes,
            actualDurationSeconds = actualDurationSeconds,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            isCompleted = isCompleted,
            earlyEndReason = earlyEndReason,
            distractionCount = distractionCount,
            mode = mode,
            dateString = todayStr
        )
        val recordId = focusSessionDao.insertRecord(record)

        val actualMinutes = (actualDurationSeconds / 60).toInt()

        // 1. Update Subject stats
        val subject = subjectDao.getSubjectById(subjectId)
        if (subject != null) {
            subjectDao.updateSubject(
                subject.copy(
                    actualFocusedMinutes = subject.actualFocusedMinutes + actualMinutes,
                    sessionsCompleted = subject.sessionsCompleted + (if (isCompleted) 1 else 0)
                )
            )
        }

        // 2. Update Daily Stats
        val existingDaily = dailyStatsDao.getStatsForDateSync(todayStr)
        val settings = getUserSettingsSync()
        val goalMinutes = settings.dailyGoalMinutes

        val updatedFocused = (existingDaily?.totalFocusedMinutes ?: 0) + actualMinutes
        val updatedPlanned = (existingDaily?.totalPlannedMinutes ?: 0) + plannedDurationMinutes
        val updatedCompleted = (existingDaily?.sessionsCompleted ?: 0) + (if (isCompleted) 1 else 0)
        val updatedTotal = (existingDaily?.sessionsTotal ?: 0) + 1
        val updatedDistractions = (existingDaily?.distractionCount ?: 0) + distractionCount

        val calculatedScore = calculateFocusScore(
            actualMinutes = updatedFocused,
            goalMinutes = goalMinutes,
            completedSessions = updatedCompleted,
            totalSessions = updatedTotal,
            distractionCount = updatedDistractions
        )

        val newDaily = DailyStatsEntity(
            dateString = todayStr,
            totalPlannedMinutes = updatedPlanned,
            totalFocusedMinutes = updatedFocused,
            sessionsCompleted = updatedCompleted,
            sessionsTotal = updatedTotal,
            focusScore = calculatedScore,
            distractionCount = updatedDistractions,
            goalMinutes = goalMinutes
        )
        dailyStatsDao.insertOrUpdate(newDaily)

        // 3. Update Streak
        updateStreak(todayStr, updatedFocused, goalMinutes)

        // 4. Evaluate Achievements
        evaluateAchievements(updatedFocused, updatedCompleted)

        return recordId
    }

    // Deterministic Focus Score Formula (0-100)
    fun calculateFocusScore(
        actualMinutes: Int,
        goalMinutes: Int,
        completedSessions: Int,
        totalSessions: Int,
        distractionCount: Int
    ): Int {
        if (goalMinutes <= 0 && totalSessions <= 0) return 0

        // 1. Goal Completion: Up to 40 pts
        val goalRatio = if (goalMinutes > 0) min(1.0, actualMinutes.toDouble() / goalMinutes.toDouble()) else 0.0
        val goalPts = (goalRatio * 40).toInt()

        // 2. Session Completion: Up to 25 pts
        val sessionRatio = if (totalSessions > 0) min(1.0, completedSessions.toDouble() / totalSessions.toDouble()) else 0.0
        val sessionPts = (sessionRatio * 25).toInt()

        // 3. Consistency Base: Up to 20 pts (sessions done + minimum focus)
        val consistencyPts = when {
            actualMinutes >= 180 -> 20
            actualMinutes >= 120 -> 16
            actualMinutes >= 60 -> 12
            actualMinutes >= 25 -> 8
            else -> 4
        }

        // 4. Distraction Control: Up to 15 pts (decreases with distractions)
        val distractionPenalty = min(15, distractionCount * 3)
        val distractionPts = 15 - distractionPenalty

        val total = goalPts + sessionPts + consistencyPts + distractionPts
        return total.coerceIn(0, 100)
    }

    private suspend fun updateStreak(todayStr: String, focusedToday: Int, goalMinutes: Int) {
        val settings = getUserSettingsSync()
        // Streak increases if meaningful progress is made (e.g. >= 50% of goal or >= 60 min)
        val meaningfulProgress = focusedToday >= (goalMinutes * 0.5) || focusedToday >= 60

        if (meaningfulProgress) {
            if (settings.lastActiveDate != todayStr) {
                val isConsecutive = isYesterday(settings.lastActiveDate)
                val newStreak = if (isConsecutive || settings.lastActiveDate.isEmpty()) {
                    settings.currentStreak + 1
                } else {
                    1
                }
                userSettingsDao.insertOrUpdate(
                    settings.copy(
                        currentStreak = newStreak,
                        lastActiveDate = todayStr
                    )
                )
                if (newStreak >= 7) {
                    achievementDao.unlockAchievement("streak_7")
                }
            }
        }
    }

    private suspend fun evaluateAchievements(todayFocusedMinutes: Int, completedSessionsCount: Int) {
        achievementDao.unlockAchievement("first_session")
        if (completedSessionsCount >= 50) {
            achievementDao.unlockAchievement("sessions_50")
        }
        val allMonthStats = dailyStatsDao.getRecentMonthStatsSync()
        val totalMinutesAllTime = allMonthStats.sumOf { it.totalFocusedMinutes } + todayFocusedMinutes
        if (totalMinutesAllTime >= 600) { // 10 hours
            achievementDao.unlockAchievement("focus_10h")
        }
        if (totalMinutesAllTime >= 6000) { // 100 hours
            achievementDao.unlockAchievement("focus_100h")
        }
        if (todayFocusedMinutes >= 360) {
            achievementDao.unlockAchievement("perfect_day")
        }
    }

    // Initialize Default Achievements
    suspend fun initializeDefaultDataIfNeeded() {
        val defaultAchievements = listOf(
            AchievementEntity("first_session", "First Focus Session", "Completed your very first focused study block.", "Flag"),
            AchievementEntity("focus_10h", "10 Hours Focused", "Accumulated 10 full hours of deep, distraction-free focus.", "Timer"),
            AchievementEntity("streak_7", "7 Day Streak", "Maintained an unbroken daily focus streak for one week.", "Whatshot"),
            AchievementEntity("sessions_50", "50 Sessions Completed", "Finished 50 structured focus sessions.", "DoneAll"),
            AchievementEntity("focus_100h", "100 Hours Focused", "Milestone: Centurion of focus with 100 focused hours.", "EmojiEvents"),
            AchievementEntity("perfect_day", "Perfect Day", "Achieved full 6+ hours of planned focus in a single day.", "Star")
        )
        achievementDao.insertAll(defaultAchievements)
        getUserSettingsSync()
    }

    // Optional Demo Data
    suspend fun seedDemoData() {
        // 1. Subjects
        val mathId = subjectDao.insertSubject(SubjectEntity(name = "Mathematics", colorHex = "#38BDF8", iconName = "Calculate", targetWeeklyHours = 12f))
        val physicsId = subjectDao.insertSubject(SubjectEntity(name = "Physics", colorHex = "#A78BFA", iconName = "Science", targetWeeklyHours = 10f))
        val mlId = subjectDao.insertSubject(SubjectEntity(name = "Machine Learning", colorHex = "#34D399", iconName = "Psychology", targetWeeklyHours = 14f))
        val progId = subjectDao.insertSubject(SubjectEntity(name = "Programming", colorHex = "#FBBF24", iconName = "Code", targetWeeklyHours = 12f))

        // 2. Timetable Sessions (Mon - Sun)
        val sessions = listOf(
            TimetableSessionEntity(dayOfWeek = 1, subjectId = mathId, subjectName = "Mathematics", taskName = "Calculus & Linear Algebra", startTime = "06:00", endTime = "08:00", durationMinutes = 120, colorHex = "#38BDF8"),
            TimetableSessionEntity(dayOfWeek = 1, subjectId = physicsId, subjectName = "Physics", taskName = "Electromagnetism Problem Sets", startTime = "10:00", endTime = "12:00", durationMinutes = 120, colorHex = "#A78BFA"),
            TimetableSessionEntity(dayOfWeek = 1, subjectId = progId, subjectName = "Programming", taskName = "Algorithms & Data Structures", startTime = "19:00", endTime = "21:00", durationMinutes = 120, colorHex = "#FBBF24"),

            TimetableSessionEntity(dayOfWeek = 2, subjectId = mlId, subjectName = "Machine Learning", taskName = "Neural Networks & Backprop", startTime = "07:00", endTime = "09:00", durationMinutes = 120, colorHex = "#34D399"),
            TimetableSessionEntity(dayOfWeek = 2, subjectId = progId, subjectName = "Programming", taskName = "Kotlin Coroutines & Flow", startTime = "14:00", endTime = "16:00", durationMinutes = 120, colorHex = "#FBBF24"),

            TimetableSessionEntity(dayOfWeek = 3, subjectId = mathId, subjectName = "Mathematics", taskName = "Differential Equations", startTime = "06:00", endTime = "08:00", durationMinutes = 120, colorHex = "#38BDF8"),
            TimetableSessionEntity(dayOfWeek = 3, subjectId = physicsId, subjectName = "Physics", taskName = "Optics & Thermodynamics", startTime = "10:00", endTime = "12:00", durationMinutes = 120, colorHex = "#A78BFA"),
            TimetableSessionEntity(dayOfWeek = 3, subjectId = mlId, subjectName = "Machine Learning", taskName = "KNN & SVM Implementation", startTime = "18:00", endTime = "20:00", durationMinutes = 120, colorHex = "#34D399"),

            TimetableSessionEntity(dayOfWeek = 4, subjectId = progId, subjectName = "Programming", taskName = "Android Jetpack Compose", startTime = "08:00", endTime = "10:00", durationMinutes = 120, colorHex = "#FBBF24"),
            TimetableSessionEntity(dayOfWeek = 4, subjectId = mathId, subjectName = "Mathematics", taskName = "Probability & Statistics", startTime = "15:00", endTime = "17:00", durationMinutes = 120, colorHex = "#38BDF8"),

            TimetableSessionEntity(dayOfWeek = 5, subjectId = mlId, subjectName = "Machine Learning", taskName = "PyTorch Model Training", startTime = "07:00", endTime = "09:00", durationMinutes = 120, colorHex = "#34D399"),
            TimetableSessionEntity(dayOfWeek = 5, subjectId = physicsId, subjectName = "Physics", taskName = "Quantum Mechanics Basics", startTime = "11:00", endTime = "13:00", durationMinutes = 120, colorHex = "#A78BFA"),
            TimetableSessionEntity(dayOfWeek = 5, subjectId = progId, subjectName = "Programming", taskName = "Open Source Projects", startTime = "18:00", endTime = "20:00", durationMinutes = 120, colorHex = "#FBBF24"),

            TimetableSessionEntity(dayOfWeek = 6, subjectId = mathId, subjectName = "Mathematics", taskName = "Weekly Problem Review", startTime = "09:00", endTime = "11:30", durationMinutes = 150, colorHex = "#38BDF8"),
            TimetableSessionEntity(dayOfWeek = 6, subjectId = mlId, subjectName = "Machine Learning", taskName = "Research Paper Reading", startTime = "14:00", endTime = "16:30", durationMinutes = 150, colorHex = "#34D399"),

            TimetableSessionEntity(dayOfWeek = 7, subjectId = progId, subjectName = "Programming", taskName = "Full Stack Build Practice", startTime = "10:00", endTime = "12:00", durationMinutes = 120, colorHex = "#FBBF24")
        )
        timetableDao.insertAll(sessions)

        // 3. Past 7 days realistic history
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        val dailyList = mutableListOf<DailyStatsEntity>()
        val demoRecords = mutableListOf<FocusSessionRecordEntity>()

        val daysBack = listOf(
            Triple(6, 420, 390), // Mon: 7h planned, 6h 30m focused
            Triple(5, 360, 310), // Tue: 6h planned, 5h 10m focused
            Triple(4, 480, 440), // Wed: 8h planned, 7h 20m focused
            Triple(3, 300, 240), // Thu: 5h planned, 4h focused
            Triple(2, 420, 400), // Fri: 7h planned, 6h 40m focused
            Triple(1, 360, 350), // Sat: 6h planned, 5h 50m focused
            Triple(0, 360, 222)  // Sun / Today: 6h planned, 3h 42m focused (matches spec!)
        )

        for ((offset, plannedMins, focusedMins) in daysBack) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -offset)
            val dStr = sdf.format(c.time)
            val completed = if (offset == 0) 3 else 4
            val total = if (offset == 0) 4 else 4
            val distractions = if (offset == 3) 4 else 1

            val score = calculateFocusScore(focusedMins, 360, completed, total, distractions)
            dailyList.add(
                DailyStatsEntity(
                    dateString = dStr,
                    totalPlannedMinutes = plannedMins,
                    totalFocusedMinutes = focusedMins,
                    sessionsCompleted = completed,
                    sessionsTotal = total,
                    focusScore = score,
                    distractionCount = distractions,
                    goalMinutes = 360
                )
            )

            // Add sample session record for today
            if (offset == 0) {
                demoRecords.add(
                    FocusSessionRecordEntity(
                        subjectId = mathId,
                        subjectName = "Mathematics",
                        taskName = "Calculus Problem Set 3",
                        plannedDurationMinutes = 120,
                        actualDurationSeconds = 120 * 60L,
                        startTimeMillis = System.currentTimeMillis() - 4 * 3600 * 1000L,
                        endTimeMillis = System.currentTimeMillis() - 2 * 3600 * 1000L,
                        isCompleted = true,
                        earlyEndReason = null,
                        distractionCount = 0,
                        mode = "COUNTDOWN",
                        dateString = dStr
                    )
                )
                demoRecords.add(
                    FocusSessionRecordEntity(
                        subjectId = physicsId,
                        subjectName = "Physics",
                        taskName = "Electromagnetism Flashcards",
                        plannedDurationMinutes = 120,
                        actualDurationSeconds = 102 * 60L,
                        startTimeMillis = System.currentTimeMillis() - 2 * 3600 * 1000L,
                        endTimeMillis = System.currentTimeMillis() - 18 * 60 * 1000L,
                        isCompleted = true,
                        earlyEndReason = null,
                        distractionCount = 1,
                        mode = "COUNTDOWN",
                        dateString = dStr
                    )
                )
            }
        }

        dailyStatsDao.insertAll(dailyList)
        focusSessionDao.insertAll(demoRecords)

        // 4. Set current streak in settings
        val settings = getUserSettingsSync()
        userSettingsDao.insertOrUpdate(
            settings.copy(
                isOnboardingComplete = true,
                currentStreak = 12,
                lastActiveDate = getTodayDateString(),
                dailyGoalMinutes = 360
            )
        )

        // Unlock achievements
        achievementDao.unlockAchievement("first_session")
        achievementDao.unlockAchievement("focus_10h")
        achievementDao.unlockAchievement("streak_7")
    }

    suspend fun clearAllData() {
        timetableDao.deleteAllSessions()
        focusSessionDao.deleteAll()
        dailyStatsDao.deleteAll()
        val settings = getUserSettingsSync()
        userSettingsDao.insertOrUpdate(settings.copy(currentStreak = 0, lastActiveDate = ""))
    }

    private fun isYesterday(dateStr: String): Boolean {
        if (dateStr.isEmpty()) return false
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            val date = sdf.parse(dateStr) ?: return false
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            sdf.format(cal.time) == sdf.format(date)
        } catch (_: Exception) {
            false
        }
    }

    companion object {
        fun getTodayDateString(): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }
}
