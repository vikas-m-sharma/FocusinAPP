package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.AchievementEntity
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.ChapterProgressEntity
import com.example.data.local.entity.DailyStatsEntity
import com.example.data.local.entity.FocusSessionRecordEntity
import com.example.data.local.entity.LearningResourceEntity
import com.example.data.local.entity.QuestionAttemptEntity
import com.example.data.local.entity.QuestionAttemptRecordEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.QuizAttemptEntity
import com.example.data.local.entity.QuizAttemptRecordEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TimetableSessionEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.local.entity.VoiceRecordingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    private val learningDao = database.learningDao()
    private val mistakeDao = database.mistakeDao()
    private val importedTestDao = database.importedTestDao()
    private val ncertDao = database.ncertDao()

    val ncertRepository: NcertRepository = NcertRepository(ncertDao)

    // Flows
    val allSubjects: Flow<List<SubjectEntity>> = subjectDao.getAllSubjects()
    val allTimetableSessions: Flow<List<TimetableSessionEntity>> = timetableDao.getAllSessions()
    val allRecords: Flow<List<FocusSessionRecordEntity>> = focusSessionDao.getAllRecords()
    val recentWeekStats: Flow<List<DailyStatsEntity>> = dailyStatsDao.getRecentWeekStats()
    val recentMonthStats: Flow<List<DailyStatsEntity>> = dailyStatsDao.getRecentMonthStats()
    val recentYearStats: Flow<List<DailyStatsEntity>> = dailyStatsDao.getRecentYearStats()
    val allVoiceRecordings: Flow<List<VoiceRecordingEntity>> = voiceRecordingDao.getAllRecordings()
    val allAchievements: Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()
    val userSettings: Flow<UserSettingsEntity?> = userSettingsDao.getUserSettings()

    // Imported Tests Room Flows
    val allImportedTests: Flow<List<com.example.data.local.entity.ImportedTestEntity>> = importedTestDao.getAllImportedTests()

    // Mistake Diary Room Flows
    val allMistakes: Flow<List<com.example.data.local.entity.MistakeEntity>> = mistakeDao.getAllMistakes()
    val unresolvedMistakes: Flow<List<com.example.data.local.entity.MistakeEntity>> = mistakeDao.getUnresolvedMistakes()

    // Learning Analytics Flows
    val allChapterProgress: Flow<List<ChapterProgressEntity>> = learningDao.getAllChapterProgress()
    val allQuestionAttempts: Flow<List<QuestionAttemptRecordEntity>> = learningDao.getAllQuestionAttempts()
    val allQuizAttempts: Flow<List<QuizAttemptRecordEntity>> = learningDao.getAllQuizAttempts()

    val allChapters: Flow<List<ChapterEntity>> = learningDao.getAllChapters()
    val allQuestions: Flow<List<QuestionEntity>> = learningDao.getAllQuestions()
    val bookmarkedQuestions: Flow<List<QuestionEntity>> = learningDao.bookmarkedQuestions()
    val featuredResources: Flow<List<LearningResourceEntity>> = learningDao.getFeaturedResources()

    fun getChapterFlow(chapterId: String) = learningDao.getChapterFlow(chapterId)
    fun getChaptersBySubject(subjectId: String) = learningDao.getChaptersBySubject(subjectId)
    fun getChaptersBySubject(examId: String, subjectId: String) = learningDao.getChaptersBySubject(subjectId)
    fun getTopicsForChapter(chapterId: String) = learningDao.getTopicsForChapter(chapterId)
    fun getResourcesForChapter(chapterId: String) = learningDao.getResourcesForChapter(chapterId)
    fun getQuestionsForChapter(chapterId: String) = learningDao.getQuestionsForChapter(chapterId)
    suspend fun getQuestionsForChapterSync(chapterId: String) = learningDao.getQuestionsForChapterSync(chapterId)

    fun getQuestionsForChapterWithFilters(
        chapterId: String,
        sourceExam: String? = null,
        startYear: Int? = null,
        endYear: Int? = null,
        difficulty: String? = null,
        isOfficialOnly: Boolean = false
    ) = learningDao.getQuestionsForChapterWithFilters(chapterId, sourceExam, startYear, endYear, difficulty, isOfficialOnly)

    suspend fun getQuestionsForChapterWithFiltersSync(
        chapterId: String,
        sourceExam: String? = null,
        startYear: Int? = null,
        endYear: Int? = null,
        difficulty: String? = null,
        isOfficialOnly: Boolean = false
    ) = learningDao.getQuestionsForChapterWithFiltersSync(chapterId, sourceExam, startYear, endYear, difficulty, isOfficialOnly)

    fun getQuestionsForChapterAndExam(chapterId: String, sourceExam: String) =
        learningDao.getQuestionsForChapterAndExam(chapterId, sourceExam)

    fun getQuestionsForChapterAndYear(chapterId: String, year: Int) =
        learningDao.getQuestionsForChapterAndYear(chapterId, year)

    fun getQuestionsForChapterAndYears(chapterId: String, startYear: Int, endYear: Int) =
        learningDao.getQuestionsForChapterAndYears(chapterId, startYear, endYear)

    fun getUnansweredQuestionsForChapter(chapterId: String) =
        learningDao.getUnansweredQuestionsForChapter(chapterId)

    fun getMistakeQuestionsForChapter(chapterId: String) =
        learningDao.getMistakeQuestionsForChapter(chapterId)

    fun getQuestionCountForChapter(chapterId: String) =
        learningDao.getQuestionCountForChapter(chapterId)

    suspend fun getQuestionCountForChapterSync(chapterId: String) =
        learningDao.getQuestionCountForChapterSync(chapterId)

    fun getQuestionCountByYear(chapterId: String) =
        learningDao.getQuestionCountByYear(chapterId)

    fun getVerifiedQuestionCountByYear(chapterId: String) =
        learningDao.getVerifiedQuestionCountByYear(chapterId)

    fun getUnverifiedQuestionCountByYear(chapterId: String) =
        learningDao.getUnverifiedQuestionCountByYear(chapterId)

    suspend fun toggleQuestionBookmark(questionId: String, isBookmarked: Boolean) =
        learningDao.toggleQuestionBookmark(questionId, isBookmarked)

    fun getQuestionCountByExam(chapterId: String) =
        learningDao.getQuestionCountByExam(chapterId)

    fun getQuestionCountByTopic(chapterId: String) =
        learningDao.getQuestionCountByTopic(chapterId)

    suspend fun getQuestionById(id: String) =
        learningDao.getQuestionById(id)

    fun getTotalQuestionCount(): Flow<Int> = learningDao.getTotalQuestionCount()
    fun getDistinctExamYears(): Flow<List<Int>> = learningDao.getDistinctExamYears()
    fun getTotalOfficialPyqCount(): Flow<Int> = learningDao.getTotalOfficialPyqCount()
    fun getTotalUnverifiedCount(): Flow<Int> = learningDao.getTotalUnverifiedCount()
    fun getTotalSampleCount(): Flow<Int> = learningDao.getTotalSampleCount()
    fun getTotalGeneratedCount(): Flow<Int> = learningDao.getTotalGeneratedCount()
    fun getGlobalYearStats(): Flow<List<com.example.data.local.dao.GlobalYearStats>> = learningDao.getGlobalYearStats()

    fun getTotalImportedPapersCount(): Flow<Int> = learningDao.getTotalImportedPapersCount()

    fun getHistoricalCoverageReports(): Flow<List<com.example.data.catalog.YearCoverageReport>> {
        return learningDao.getGlobalYearStats().map { statsList: List<com.example.data.local.dao.GlobalYearStats> ->
            val statsMap = statsList.associateBy { it.examYear }
            com.example.data.catalog.HistoricalYearCatalog.ALL_YEARS.map { yearInfo ->
                val stats = statsMap[yearInfo.year]
                val actualImported = stats?.totalCount ?: 0
                val actualVerified = stats?.verifiedCount ?: 0
                val actualUnverified = stats?.unverifiedCount ?: 0
                val actualSample = stats?.sampleCount ?: 0
                val actualPapers = if (actualImported > 0) maxOf(1, stats?.paperCount ?: 1) else 0
                val actualPhy = stats?.physicsCount ?: 0
                val actualChem = stats?.chemistryCount ?: 0
                val actualBio = stats?.biologyCount ?: 0

                val rawExam = stats?.detectedExam ?: yearInfo.exam
                val detectedExam = when (rawExam) {
                    "NEET_UG" -> "NEET"
                    else -> rawExam
                }

                val status = when {
                    actualImported == 0 -> com.example.data.catalog.DatasetImportStatus.NOT_IMPORTED
                    actualImported < yearInfo.expectedTotal -> com.example.data.catalog.DatasetImportStatus.PARTIAL
                    actualVerified >= yearInfo.expectedTotal && actualImported >= yearInfo.expectedTotal -> com.example.data.catalog.DatasetImportStatus.VERIFIED
                    else -> com.example.data.catalog.DatasetImportStatus.UNVERIFIED
                }

                com.example.data.catalog.YearCoverageReport(
                    info = yearInfo,
                    actualImported = actualImported,
                    actualVerified = actualVerified,
                    actualUnverified = actualUnverified,
                    actualSample = actualSample,
                    actualPapers = actualPapers,
                    actualPhysics = actualPhy,
                    actualChemistry = actualChem,
                    actualBiology = actualBio,
                    detectedExam = detectedExam,
                    status = status
                )
            }
        }
    }

    suspend fun importPyqAssets(context: android.content.Context): com.example.data.importer.PyqImportReport {
        val importer = com.example.data.importer.PyqAssetImporter(context, learningDao)
        return importer.importAllPyqAssets()
    }

    val pyqReviewQueueManager = com.example.data.importer.PyqReviewQueueManager(learningDao)

    fun getPyqReviewQueue(): Flow<List<com.example.data.importer.PyqNormalizedQuestion>> =
        pyqReviewQueueManager.queue

    suspend fun convertPyqSourceDocument(
        document: com.example.data.importer.PyqSourceDocument
    ): Pair<List<com.example.data.importer.PyqNormalizedQuestion>, com.example.data.importer.PyqConversionReport> {
        val existingNaturalKeys = learningDao.getExistingNaturalKeys().toSet()
        val existingTexts = learningDao.getAllQuestionTexts()
        val textFingerprints = existingTexts.map { com.example.data.importer.PyqConversionPipeline.generateFingerprint(it) }.toSet()

        val (normalizedQuestions, report) = com.example.data.importer.PyqConversionPipeline.convert(
            document = document,
            existingQuestionSignatures = existingNaturalKeys,
            existingQuestionTextFingerprints = textFingerprints
        )

        val needsReview = normalizedQuestions.filter { it.needsHumanReview || it.verificationStatus == "UNVERIFIED" }
        if (needsReview.isNotEmpty()) {
            pyqReviewQueueManager.addToQueue(needsReview)
        }

        return Pair(normalizedQuestions, report)
    }

    suspend fun commitReviewedQuestions(): Int {
        return pyqReviewQueueManager.commitReviewedToDatabase(learningDao)
    }

    fun approveReviewItem(questionId: String, citation: String): Boolean =
        pyqReviewQueueManager.approveAsVerified(questionId, citation)

    fun keepReviewItemUnverified(questionId: String): Boolean =
        pyqReviewQueueManager.keepUnverified(questionId)

    fun rejectReviewItem(questionId: String): Boolean =
        pyqReviewQueueManager.rejectQuestion(questionId)

    fun updateReviewItemChapter(questionId: String, subject: String, chapter: String, topic: String): Boolean =
        pyqReviewQueueManager.updateChapter(questionId, subject, chapter, topic)

    fun updateReviewItemAnswer(questionId: String, answerIdx: Int): Boolean =
        pyqReviewQueueManager.updateAnswer(questionId, answerIdx)

    fun clearReviewQueue() {
        pyqReviewQueueManager.clearQueue()
    }

    fun getAttemptsForChapter(chapterId: String) = learningDao.getAttemptsForChapter(chapterId)

    suspend fun getChapterProgressById(chapterId: String) = learningDao.getChapterProgressById(chapterId)
    suspend fun updateChapterProgress(progress: ChapterProgressEntity) = learningDao.insertOrUpdateChapterProgress(progress)
    suspend fun recordQuestionAttempt(attempt: QuestionAttemptRecordEntity) = learningDao.insertQuestionAttempt(attempt)
    suspend fun recordQuestionAttempt(attempt: QuestionAttemptEntity) = learningDao.recordQuestionAttempt(attempt)
    suspend fun recordQuizAttempt(attempt: QuizAttemptRecordEntity) = learningDao.insertQuizAttempt(attempt)
    suspend fun recordQuizAttempt(attempt: QuizAttemptEntity) = learningDao.recordQuizAttempt(attempt)
    suspend fun updateTopicStatus(topicId: String, status: String) = learningDao.updateTopicStatus(topicId, status)
    suspend fun getQuizAttemptById(id: Long) = learningDao.getQuizAttemptById(id)
    fun getQuizAttemptFlowById(id: Long) = learningDao.getQuizAttemptFlowById(id)
    fun getSessionsForDay(dayOfWeek: Int): Flow<List<TimetableSessionEntity>> =
        timetableDao.getSessionsForDay(dayOfWeek)

    fun getTodayStats(dateString: String = getTodayDateString()): Flow<DailyStatsEntity?> =
        dailyStatsDao.getStatsForDate(dateString)

    // Subjects
    suspend fun insertSubject(
        name: String,
        description: String = "",
        colorHex: String = "#38BDF8",
        iconName: String = "School",
        weeklyHours: Float = 10f
    ): Long {
        return subjectDao.insertSubject(
            SubjectEntity(
                name = name,
                description = description,
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

    // Initialize Default Achievements, Starter Subjects, and Handle First-Time User Blank State
    suspend fun initializeDefaultDataIfNeeded(isFirstTimeUser: Boolean = false) {
        val defaultAchievements = listOf(
            AchievementEntity("first_session", "First Focus Session", "Completed your very first focused study block.", "Flag"),
            AchievementEntity("focus_10h", "10 Hours Focused", "Accumulated 10 full hours of deep, distraction-free focus.", "Timer"),
            AchievementEntity("streak_7", "7 Day Streak", "Maintained an unbroken daily focus streak for one week.", "Whatshot"),
            AchievementEntity("sessions_50", "50 Sessions Completed", "Finished 50 structured focus sessions.", "DoneAll"),
            AchievementEntity("focus_100h", "100 Hours Focused", "Milestone: Centurion of focus with 100 focused hours.", "EmojiEvents"),
            AchievementEntity("perfect_day", "Perfect Day", "Achieved full 6+ hours of planned focus in a single day.", "Star")
        )
        achievementDao.insertAll(defaultAchievements)

        // Seed initial starter subjects if none exist yet
        val existingSubjects = subjectDao.getAllSubjectsList()
        val bioId: Long
        val chemId: Long
        val physId: Long
        val mockId: Long
        val revId: Long
        val readId: Long

        if (existingSubjects.isEmpty()) {
            bioId = subjectDao.insertSubject(
                SubjectEntity(
                    name = "Biology",
                    description = "Human Physiology, Genetics & Ecology",
                    colorHex = "#22C55E",
                    iconName = "Eco",
                    targetWeeklyHours = 12f
                )
            )
            chemId = subjectDao.insertSubject(
                SubjectEntity(
                    name = "Chemistry",
                    description = "Organic Chemistry, Inorganic & Physical Chemistry",
                    colorHex = "#06B6D4",
                    iconName = "Science",
                    targetWeeklyHours = 10f
                )
            )
            physId = subjectDao.insertSubject(
                SubjectEntity(
                    name = "Physics",
                    description = "Current Electricity, Mechanics & Optics",
                    colorHex = "#A855F7",
                    iconName = "AutoAwesome",
                    targetWeeklyHours = 10f
                )
            )
            mockId = subjectDao.insertSubject(
                SubjectEntity(
                    name = "Mock Test",
                    description = "NEET Full Syllabus & Chapter Tests",
                    colorHex = "#EF4444",
                    iconName = "Description",
                    targetWeeklyHours = 6f
                )
            )
            revId = subjectDao.insertSubject(
                SubjectEntity(
                    name = "Revision",
                    description = "Daily Notes & High Yield Formulas",
                    colorHex = "#F59E0B",
                    iconName = "Edit",
                    targetWeeklyHours = 6f
                )
            )
            readId = subjectDao.insertSubject(
                SubjectEntity(
                    name = "Reading",
                    description = "NCERT Biology & Theory Concept Review",
                    colorHex = "#8B5CF6",
                    iconName = "MenuBook",
                    targetWeeklyHours = 6f
                )
            )
        } else {
            bioId = existingSubjects.firstOrNull { it.name.contains("Bio", ignoreCase = true) }?.id
                ?: existingSubjects.first().id
            chemId = existingSubjects.firstOrNull { it.name.contains("Chem", ignoreCase = true) }?.id
                ?: existingSubjects.first().id
            physId = existingSubjects.firstOrNull { it.name.contains("Phys", ignoreCase = true) }?.id
                ?: existingSubjects.first().id
            mockId = existingSubjects.firstOrNull { it.name.contains("Mock", ignoreCase = true) }?.id
                ?: existingSubjects.first().id
            revId = existingSubjects.firstOrNull { it.name.contains("Rev", ignoreCase = true) }?.id
                ?: existingSubjects.first().id
            readId = existingSubjects.firstOrNull { it.name.contains("Read", ignoreCase = true) }?.id
                ?: existingSubjects.first().id
        }

        if (isFirstTimeUser) {
            // First-time login: clear all sessions so user starts with a 100% BLANK timetable!
            timetableDao.deleteAllSessions()
        } else {
            // Seed initial NEET schedule timetable sessions if none exist yet
            val existingSessions = timetableDao.getAllSessionsList()
            if (existingSessions.isEmpty()) {
                val defaultSessions = mutableListOf<TimetableSessionEntity>()
                for (day in 1..7) {
                    defaultSessions.add(
                        TimetableSessionEntity(
                            dayOfWeek = day,
                            subjectId = bioId,
                            subjectName = "Biology",
                            taskName = "Human Physiology",
                            startTime = "06:00",
                            endTime = "07:00",
                            durationMinutes = 60,
                            colorHex = "#22C55E",
                            isCompleted = true
                        )
                    )
                    defaultSessions.add(
                        TimetableSessionEntity(
                            dayOfWeek = day,
                            subjectId = chemId,
                            subjectName = "Chemistry",
                            taskName = "Organic Chemistry",
                            startTime = "07:15",
                            endTime = "08:15",
                            durationMinutes = 60,
                            colorHex = "#06B6D4",
                            isCompleted = true
                        )
                    )
                    defaultSessions.add(
                        TimetableSessionEntity(
                            dayOfWeek = day,
                            subjectId = physId,
                            subjectName = "Physics",
                            taskName = "Current Electricity",
                            startTime = "10:00",
                            endTime = "11:30",
                            durationMinutes = 90,
                            colorHex = "#A855F7",
                            isCompleted = false
                        )
                    )
                    defaultSessions.add(
                        TimetableSessionEntity(
                            dayOfWeek = day,
                            subjectId = mockId,
                            subjectName = "Mock Test",
                            taskName = "Physics Full Syllabus",
                            startTime = "13:00",
                            endTime = "14:00",
                            durationMinutes = 60,
                            colorHex = "#EF4444",
                            isCompleted = false
                        )
                    )
                    defaultSessions.add(
                        TimetableSessionEntity(
                            dayOfWeek = day,
                            subjectId = revId,
                            subjectName = "Revision",
                            taskName = "Today's Notes",
                            startTime = "16:00",
                            endTime = "17:00",
                            durationMinutes = 60,
                            colorHex = "#F59E0B",
                            isCompleted = false
                        )
                    )
                    defaultSessions.add(
                        TimetableSessionEntity(
                            dayOfWeek = day,
                            subjectId = readId,
                            subjectName = "Reading",
                            taskName = "NCERT Biology",
                            startTime = "19:00",
                            endTime = "20:00",
                            durationMinutes = 60,
                            colorHex = "#8B5CF6",
                            isCompleted = false
                        )
                    )
                }
                timetableDao.insertAll(defaultSessions)
            }
        }

        // Ensure today has a clean 0-metrics DailyStats entry if not present
        val todayStr = getTodayDateString()
        val todayStats = dailyStatsDao.getStatsForDateSync(todayStr)
        if (todayStats == null) {
            dailyStatsDao.insertOrUpdate(
                DailyStatsEntity(
                    dateString = todayStr,
                    totalPlannedMinutes = 0,
                    totalFocusedMinutes = 0,
                    sessionsCompleted = 0,
                    sessionsTotal = 0,
                    focusScore = 0,
                    distractionCount = 0,
                    goalMinutes = 360
                )
            )
        }

        val settings = getUserSettingsSync()
        if (settings.lastActiveDate.isEmpty()) {
            userSettingsDao.insertOrUpdate(
                settings.copy(
                    currentStreak = 0,
                    lastActiveDate = ""
                )
            )
        }
    }

    // Optional Demo Data
    suspend fun seedDemoData() {
        // 1. Subjects
        val mathId = subjectDao.insertSubject(
            SubjectEntity(
                name = "Mathematics",
                description = "Calculus, Linear Algebra & Problem Solving",
                colorHex = "#38BDF8",
                iconName = "Calculate",
                targetWeeklyHours = 12f
            )
        )
        val physicsId = subjectDao.insertSubject(
            SubjectEntity(
                name = "Physics",
                description = "Classical Mechanics, Electromagnetism & Optics",
                colorHex = "#A78BFA",
                iconName = "Science",
                targetWeeklyHours = 10f
            )
        )
        val mlId = subjectDao.insertSubject(
            SubjectEntity(
                name = "Machine Learning",
                description = "Deep Learning, PyTorch models & Math",
                colorHex = "#34D399",
                iconName = "Psychology",
                targetWeeklyHours = 14f
            )
        )
        val progId = subjectDao.insertSubject(
            SubjectEntity(
                name = "Programming",
                description = "Kotlin, Jetpack Compose & Full Stack",
                colorHex = "#FBBF24",
                iconName = "Code",
                targetWeeklyHours = 12f
            )
        )

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
        learningDao.deleteAllChapterProgress()
        learningDao.deleteAllQuestionAttempts()
        learningDao.deleteAllQuizAttempts()

        val todayStr = getTodayDateString()
        dailyStatsDao.insertOrUpdate(
            DailyStatsEntity(
                dateString = todayStr,
                totalPlannedMinutes = 0,
                totalFocusedMinutes = 0,
                sessionsCompleted = 0,
                sessionsTotal = 0,
                focusScore = 0,
                distractionCount = 0,
                goalMinutes = 360
            )
        )

        val settings = getUserSettingsSync()
        userSettingsDao.insertOrUpdate(settings.copy(currentStreak = 0, lastActiveDate = ""))
    }

    // Mistake Diary Operations
    suspend fun insertMistake(mistake: com.example.data.local.entity.MistakeEntity) = mistakeDao.insertMistake(mistake)
    suspend fun insertMistakes(mistakes: List<com.example.data.local.entity.MistakeEntity>) = mistakeDao.insertMistakes(mistakes)
    suspend fun updateMistakeReason(id: String, reason: String, notes: String?) = mistakeDao.updateMistakeReason(id, reason, notes)
    suspend fun markMistakeResolved(id: String, isResolved: Boolean) = mistakeDao.markResolved(id, isResolved)
    suspend fun recordMistakeReattempt(id: String, isResolved: Boolean) = mistakeDao.recordReattempt(id, isResolved)
    suspend fun deleteMistakeById(id: String) = mistakeDao.deleteMistakeById(id)
    suspend fun deleteAllMistakes() = mistakeDao.deleteAllMistakes()

    // Imported Tests Operations
    suspend fun getImportedTest(testId: String): com.example.data.local.entity.ImportedTestEntity? = importedTestDao.getImportedTestById(testId)
    fun getQuestionsForImportedTest(testId: String): Flow<List<com.example.data.local.entity.ImportedQuestionEntity>> = importedTestDao.getQuestionsForTest(testId)
    suspend fun getQuestionsForImportedTestSync(testId: String): List<com.example.data.local.entity.ImportedQuestionEntity> = importedTestDao.getQuestionsForTestSync(testId)
    suspend fun insertImportedTest(test: com.example.data.local.entity.ImportedTestEntity) = importedTestDao.insertImportedTest(test)
    suspend fun insertImportedQuestions(questions: List<com.example.data.local.entity.ImportedQuestionEntity>) = importedTestDao.insertImportedQuestions(questions)
    suspend fun updateImportedTest(test: com.example.data.local.entity.ImportedTestEntity) = importedTestDao.updateImportedTest(test)
    suspend fun updateTestProgress(testId: String, currentQuestionIndex: Int, remainingSeconds: Long) =
        importedTestDao.updateTestProgress(testId, currentQuestionIndex, remainingSeconds)

    suspend fun updateImportedQuestionAnswerState(
        questionId: String,
        userAnswer: String?,
        userAnswerIndex: Int?,
        isAttempted: Boolean,
        isMarkedForReview: Boolean,
        answerState: String,
        timeSpentSeconds: Int,
        isCorrect: Boolean?
    ) = importedTestDao.updateUserAnswerState(
        questionId = questionId,
        userAnswer = userAnswer,
        userAnswerIndex = userAnswerIndex,
        isAttempted = isAttempted,
        isMarkedForReview = isMarkedForReview,
        answerState = answerState,
        timeSpentSeconds = timeSpentSeconds,
        isCorrect = isCorrect
    )

    suspend fun updateImportedQuestionMarkedForReview(questionId: String, isMarked: Boolean) =
        importedTestDao.updateQuestionMarkedForReview(questionId, isMarked)

    suspend fun resetImportedTestAnswers(testId: String) =
        importedTestDao.resetTestAnswers(testId)

    suspend fun recordImportedTestResult(
        testId: String,
        status: String,
        score: Int,
        accuracy: Int,
        correctCount: Int,
        wrongCount: Int,
        unattemptedCount: Int,
        timeTakenSeconds: Long
    ) = importedTestDao.recordTestResult(testId, status, score, accuracy, correctCount, wrongCount, unattemptedCount, timeTakenSeconds)
    suspend fun deleteImportedTest(testId: String) {
        importedTestDao.deleteQuestionsForTest(testId)
        importedTestDao.deleteImportedTest(testId)
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
