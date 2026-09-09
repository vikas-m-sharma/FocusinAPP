package com.example.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.VoiceRecorderManager
import com.example.data.ai.CoachAnalysisResult
import com.example.data.ai.GeminiFocusinService
import com.example.data.ai.GeneratedTimetablePlan
import com.example.data.ai.GoalPlanResult
import com.example.data.auth.AuthManager
import com.example.data.auth.AuthState
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AchievementEntity
import com.example.data.local.entity.DailyStatsEntity
import com.example.data.local.entity.FocusSessionRecordEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TimetableSessionEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.local.entity.VoiceRecordingEntity
import com.example.data.repository.FocusinRepository
import com.example.receiver.AlarmRingingManager
import com.example.receiver.RingingSessionInfo
import com.example.receiver.SessionAlarmReceiver
import com.example.data.backend.RedisDatabaseManager
import com.example.service.ActiveSessionState
import com.example.service.FocusSessionService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AiAssistantMessage(
    val id: Long = System.currentTimeMillis(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String? = null,
    val tips: List<String> = emptyList()
)

class FocusinViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = FocusinRepository(db)
    val voiceRecorderManager = VoiceRecorderManager(application)
    private val geminiService = GeminiFocusinService()
    val authManager = AuthManager(application)
    val authState: StateFlow<AuthState> = authManager.authState

    // Redis Backend & Database Cache Manager
    val redisDatabaseManager = RedisDatabaseManager.getInstance(application)
    val redisState = redisDatabaseManager.connectionState

    // Active Alarm Ringing State
    val isAlarmRinging: StateFlow<Boolean> = AlarmRingingManager.isRinging
    val currentRingingSession: StateFlow<RingingSessionInfo?> = AlarmRingingManager.currentRingingSession

    // Live clock and greeting state
    private val _currentTimeString = MutableStateFlow(getCurrentFormattedTime())
    val currentTimeString: StateFlow<String> = _currentTimeString.asStateFlow()

    private val _currentGreeting = MutableStateFlow(getGreetingForCurrentHour())
    val currentGreeting: StateFlow<String> = _currentGreeting.asStateFlow()

    // Database Flows
    val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<TimetableSessionEntity>> = repository.allTimetableSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyRecords: StateFlow<List<FocusSessionRecordEntity>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentWeekStats: StateFlow<List<DailyStatsEntity>> = repository.recentWeekStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentMonthStats: StateFlow<List<DailyStatsEntity>> = repository.recentMonthStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentYearStats: StateFlow<List<DailyStatsEntity>> = repository.recentYearStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDailyStats: StateFlow<List<DailyStatsEntity>> = repository.allDailyStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voiceRecordings: StateFlow<List<VoiceRecordingEntity>> = repository.allVoiceRecordings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementEntity>> = repository.allAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserSettingsEntity?> = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayStats: StateFlow<DailyStatsEntity?> = repository.getTodayStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeSessionState: StateFlow<ActiveSessionState> = FocusSessionService.sessionState

    // Timetable selected day (1=Mon..7=Sun)
    private val _selectedDay = MutableStateFlow(getCurrentDayOfWeek())
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    // Next upcoming or current session for Today
    val todaySessions: StateFlow<List<TimetableSessionEntity>> = combine(allSessions, _selectedDay) { sessions, _ ->
        val currentDay = getCurrentDayOfWeek()
        sessions.filter { it.dayOfWeek == currentDay && it.isEnabled }
            .sortedBy { it.startTime }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nextSession: StateFlow<TimetableSessionEntity?> = todaySessions.combine(activeSessionState) { sessions, active ->
        if (active.isActive) {
            null // Active session is shown instead
        } else {
            val nowTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            sessions.firstOrNull { it.endTime > nowTime } ?: sessions.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Learning & Prepare Flows
    val neetChapters: StateFlow<List<com.example.data.local.entity.ChapterEntity>> = repository.neetChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastActiveChapter: StateFlow<com.example.data.local.entity.ChapterEntity?> = repository.lastActiveChapter
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allQuestionAttempts: StateFlow<List<com.example.data.local.entity.QuestionAttemptEntity>> = repository.allQuestionAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuizAttempts: StateFlow<List<com.example.data.local.entity.QuizAttemptEntity>> = repository.allQuizAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalQuestionsAttempted: StateFlow<Int> = repository.totalQuestionsAttempted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalQuestionsCorrect: StateFlow<Int> = repository.totalQuestionsCorrect
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val featuredResources: StateFlow<List<com.example.data.local.entity.LearningResourceEntity>> = repository.featuredResources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedQuestions: StateFlow<List<com.example.data.local.entity.QuestionEntity>> = repository.bookmarkedQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mockTestAttempts: StateFlow<List<com.example.data.local.entity.QuizAttemptEntity>> = repository.mockTestAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMockTestsCount: StateFlow<Int> = repository.totalMockTestsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Derived Weak Topics based on actual question attempts (<65% accuracy)
    val calculatedWeakTopics: StateFlow<List<WeakTopicInfo>> = allQuestionAttempts.map { attempts ->
        attempts.groupBy { it.topicName }
            .mapNotNull { (topic, topicAttempts) ->
                if (topicAttempts.isNotEmpty()) {
                    val correct = topicAttempts.count { it.isCorrect }
                    val acc = ((correct.toFloat() / topicAttempts.size) * 100).toInt()
                    if (acc < 65) {
                        val sample = topicAttempts.first()
                        WeakTopicInfo(
                            topicName = topic,
                            subjectId = sample.subjectId,
                            chapterId = sample.chapterId,
                            totalAttempts = topicAttempts.size,
                            correctAttempts = correct,
                            accuracy = acc
                        )
                    } else null
                } else null
            }
            .sortedBy { it.accuracy }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getQuizAttemptById(id: Long): Flow<com.example.data.local.entity.QuizAttemptEntity?> {
        return repository.getQuizAttemptById(id)
    }

    val allQuestions: StateFlow<List<com.example.data.local.entity.QuestionEntity>> = repository.learningDao.getAllQuestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getAttemptsForChapter(chapterId: String): Flow<List<com.example.data.local.entity.QuestionAttemptEntity>> {
        return repository.learningDao.getAttemptsForChapter(chapterId)
    }

    fun toggleQuestionBookmark(questionId: String, current: Boolean) {
        viewModelScope.launch {
            repository.setQuestionBookmarked(questionId, !current)
        }
    }

    fun recordQuestionAttempt(
        questionId: String,
        chapterId: String,
        subjectId: String,
        topicName: String,
        selectedOption: String,
        isCorrect: Boolean,
        timeTakenSeconds: Int
    ) {
        viewModelScope.launch {
            repository.recordQuestionAttempt(
                com.example.data.local.entity.QuestionAttemptEntity(
                    questionId = questionId,
                    chapterId = chapterId,
                    subjectId = subjectId,
                    topicName = topicName,
                    selectedOption = selectedOption,
                    isCorrect = isCorrect,
                    timeTakenSeconds = timeTakenSeconds
                )
            )
        }
    }

    fun recordQuizAttempt(
        examId: String,
        subjectId: String,
        chapterId: String,
        chapterName: String,
        totalQuestions: Int,
        correctAnswers: Int,
        timeTakenSeconds: Int,
        mode: String,
        strongTopics: List<String>,
        weakTopics: List<String>
    ) {
        viewModelScope.launch {
            val strongJson = strongTopics.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")
            val weakJson = weakTopics.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")
            repository.recordQuizAttempt(
                com.example.data.local.entity.QuizAttemptEntity(
                    examId = examId,
                    subjectId = subjectId,
                    chapterId = chapterId,
                    chapterName = chapterName,
                    totalQuestions = totalQuestions,
                    correctAnswers = correctAnswers,
                    timeTakenSeconds = timeTakenSeconds,
                    mode = mode,
                    strongTopicsJson = strongJson,
                    weakTopicsJson = weakJson
                )
            )
        }
    }

    fun getQuestionsForSubject(subjectId: String): kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.QuestionEntity>> {
        return repository.getQuestionsForSubject(subjectId)
    }

    fun getQuestionsForTopic(chapterId: String, topicName: String): kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.QuestionEntity>> {
        return repository.getQuestionsForTopic(chapterId, topicName)
    }

    fun getAvailablePYQYears(): kotlinx.coroutines.flow.Flow<List<String>> {
        return repository.getAvailablePYQYears()
    }

    fun getTopicsForChapter(chapterId: String): kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.TopicEntity>> {
        return repository.learningDao.getTopicsForChapter(chapterId)
    }

    fun getChapterFlow(chapterId: String): kotlinx.coroutines.flow.Flow<com.example.data.local.entity.ChapterEntity?> {
        return repository.learningDao.getChapterById(chapterId)
    }

    fun getChaptersBySubject(examId: String, subjectId: String): kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.ChapterEntity>> {
        return repository.getChaptersBySubject(examId, subjectId)
    }

    fun getResourcesForChapter(chapterId: String): kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.LearningResourceEntity>> {
        return repository.getResourcesForChapter(chapterId)
    }

    fun getResourcesForTopic(topicId: String): kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.LearningResourceEntity>> {
        return repository.getResourcesForTopic(topicId)
    }

    fun getQuestionsForChapter(chapterId: String): kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.QuestionEntity>> {
        return repository.learningDao.getQuestionsForChapter(chapterId)
    }

    fun getQuestionsForPYQ(year: String): kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.QuestionEntity>> {
        return repository.learningDao.getQuestionsForPYQ(year)
    }

    fun updateTopicStatus(topicId: String, chapterId: String, status: String) {
        viewModelScope.launch {
            repository.updateTopicStatusAndSyncChapter(topicId, chapterId, status)
        }
    }

    fun updateResourceCompletion(resourceId: String, completed: Boolean) {
        viewModelScope.launch {
            repository.updateResourceCompletion(resourceId, completed)
        }
    }

    fun updateResourceBookmark(resourceId: String, bookmarked: Boolean) {
        viewModelScope.launch {
            repository.updateResourceBookmark(resourceId, bookmarked)
        }
    }

    fun scheduleLearningSession(
        subjectName: String,
        topicName: String,
        dayOfWeek: Int = getCurrentDayOfWeek(),
        startTime: String = "18:00",
        endTime: String = "19:00",
        durationMinutes: Int = 60,
        focusModeEnabled: Boolean = true,
        alarmEnabled: Boolean = true,
        protectionLevel: String = "STANDARD"
    ) {
        viewModelScope.launch {
            val newSession = TimetableSessionEntity(
                dayOfWeek = dayOfWeek,
                subjectId = 0,
                subjectName = subjectName,
                taskName = "Study: $topicName",
                startTime = startTime,
                endTime = endTime,
                durationMinutes = durationMinutes,
                focusModeEnabled = focusModeEnabled,
                alarmEnabled = alarmEnabled,
                note = protectionLevel
            )
            repository.insertTimetableSession(newSession)
        }
    }

    fun recordQuizAttempt(
        subjectId: String,
        chapterId: String,
        chapterName: String,
        totalQuestions: Int,
        correctAnswers: Int,
        timeTakenSeconds: Int,
        strongTopics: List<String>,
        weakTopics: List<String>
    ) {
        viewModelScope.launch {
            val strongJson = "[${strongTopics.joinToString(",") { "\"$it\"" }}]"
            val weakJson = "[${weakTopics.joinToString(",") { "\"$it\"" }}]"
            repository.learningDao.recordQuizAttempt(
                com.example.data.local.entity.QuizAttemptEntity(
                    subjectId = subjectId,
                    chapterId = chapterId,
                    chapterName = chapterName,
                    totalQuestions = totalQuestions,
                    correctAnswers = correctAnswers,
                    timeTakenSeconds = timeTakenSeconds,
                    strongTopicsJson = strongJson,
                    weakTopicsJson = weakJson
                )
            )
        }
    }

    fun addRevisionToSchedule(subjectName: String, topicName: String) {
        viewModelScope.launch {
            val currentDay = getCurrentDayOfWeek()
            val startTime = "18:00"
            val endTime = "19:00"
            val newSession = TimetableSessionEntity(
                dayOfWeek = currentDay,
                subjectId = 0,
                subjectName = subjectName,
                taskName = "Revise: $topicName",
                startTime = startTime,
                endTime = endTime,
                durationMinutes = 60,
                focusModeEnabled = true,
                alarmEnabled = true
            )
            repository.insertTimetableSession(newSession)
        }
    }

    // AI States
    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _aiGeneratedTimetable = MutableStateFlow<GeneratedTimetablePlan?>(null)
    val aiGeneratedTimetable: StateFlow<GeneratedTimetablePlan?> = _aiGeneratedTimetable.asStateFlow()

    private val _aiGoalPlan = MutableStateFlow<GoalPlanResult?>(null)
    val aiGoalPlan: StateFlow<GoalPlanResult?> = _aiGoalPlan.asStateFlow()

    private val _aiCoachAnalysis = MutableStateFlow<CoachAnalysisResult?>(null)
    val aiCoachAnalysis: StateFlow<CoachAnalysisResult?> = _aiCoachAnalysis.asStateFlow()

    // AI Study Assistant Conversation
    private val _aiAssistantMessages = MutableStateFlow<List<AiAssistantMessage>>(emptyList())
    val aiAssistantMessages: StateFlow<List<AiAssistantMessage>> = _aiAssistantMessages.asStateFlow()

    private val _isAiAssistantLoading = MutableStateFlow(false)
    val isAiAssistantLoading: StateFlow<Boolean> = _isAiAssistantLoading.asStateFlow()

    // Audio recording & playback state
    private val _isAudioRecording = MutableStateFlow(false)
    val isAudioRecording: StateFlow<Boolean> = _isAudioRecording.asStateFlow()

    private val _currentlyPlayingId = MutableStateFlow<Long?>(null)
    val currentlyPlayingId: StateFlow<Long?> = _currentlyPlayingId.asStateFlow()

    // Early End & Session Complete Event
    private val _showEarlyEndDialog = MutableStateFlow(false)
    val showEarlyEndDialog: StateFlow<Boolean> = _showEarlyEndDialog.asStateFlow()

    private val _latestCompletedRecord = MutableStateFlow<FocusSessionRecordEntity?>(null)
    val latestCompletedRecord: StateFlow<FocusSessionRecordEntity?> = _latestCompletedRecord.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
            authManager.continueAsLocalUser("Scholar")
        }

        // Sync auth state with database user settings
        viewModelScope.launch {
            authManager.authState.collect { state ->
                if (state is AuthState.Authenticated) {
                    val user = state.user
                    val current = repository.getUserSettingsSync()
                    repository.updateUserSettings(
                        current.copy(
                            userName = user.displayName,
                            userEmail = user.email,
                            isGoogleSignedIn = user.isGoogleUser,
                            cloudSyncEnabled = user.isGoogleUser,
                            lastSyncTimestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        // Live clock ticker
        viewModelScope.launch {
            while (isActive) {
                _currentTimeString.value = getCurrentFormattedTime()
                _currentGreeting.value = getGreetingForCurrentHour()
                delay(15000L) // updates every 15s
            }
        }

        // Initialize AI Study Assistant Greeting
        viewModelScope.launch {
            com.example.data.local.LearningInitialData.populateInitialDataIfEmpty(repository.learningDao)
            userSettings.collect { settings ->
                if (_aiAssistantMessages.value.isEmpty()) {
                    val name = settings?.userName ?: "Scholar"
                    val initial = AiAssistantMessage(
                        isUser = false,
                        text = "Hello Sir/Ma'am $name! 👋 Focusin AI here. Can I schedule your study timetable (e.g. 6:00 AM - 8:00 AM) and lock distracting social media apps to protect your focus today?",
                        actionType = "GREETING",
                        tips = listOf(
                            "⚡ Schedule & Lock Social Apps",
                            "📖 How to Prepare for Study",
                            "🗺️ App Structure Guidance"
                        )
                    )
                    _aiAssistantMessages.value = listOf(initial)
                }
            }
        }
    }

    fun selectDay(day: Int) {
        _selectedDay.value = day
    }

    // --- Active Session Management ---
    fun startFocusSession(
        subjectId: Long,
        subjectName: String,
        taskName: String,
        durationMinutes: Int,
        mode: String = "COUNTDOWN",
        protection: Boolean = true
    ) {
        val context = getApplication<Application>()
        val intent = Intent(context, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_START
            putExtra(FocusSessionService.EXTRA_SUBJECT_ID, subjectId)
            putExtra(FocusSessionService.EXTRA_SUBJECT_NAME, subjectName)
            putExtra(FocusSessionService.EXTRA_TASK_NAME, taskName)
            putExtra(FocusSessionService.EXTRA_DURATION_MINUTES, durationMinutes)
            putExtra(FocusSessionService.EXTRA_MODE, mode)
            putExtra("EXTRA_PROTECTION", protection)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun pauseSession() {
        val context = getApplication<Application>()
        val intent = Intent(context, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    fun resumeSession() {
        val context = getApplication<Application>()
        val intent = Intent(context, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_RESUME
        }
        context.startService(intent)
    }

    fun requestEndSession() {
        val current = activeSessionState.value
        if (current.mode == "COUNTDOWN" && current.remainingSeconds > 60) {
            _showEarlyEndDialog.value = true
        } else {
            confirmEndSession(isCompleted = true, reason = null)
        }
    }

    fun dismissEarlyEndDialog() {
        _showEarlyEndDialog.value = false
    }

    fun confirmEndSession(isCompleted: Boolean, reason: String?) {
        _showEarlyEndDialog.value = false
        val current = activeSessionState.value
        val actualSeconds = current.elapsedSeconds

        viewModelScope.launch {
            if (current.isActive && actualSeconds > 0) {
                val recordId = repository.recordFocusSession(
                    subjectId = current.subjectId,
                    subjectName = current.subjectName,
                    taskName = current.taskName,
                    plannedDurationMinutes = current.plannedDurationMinutes,
                    actualDurationSeconds = actualSeconds,
                    startTimeMillis = System.currentTimeMillis() - (actualSeconds * 1000L),
                    endTimeMillis = System.currentTimeMillis(),
                    isCompleted = isCompleted,
                    earlyEndReason = reason,
                    distractionCount = current.distractionCount,
                    mode = current.mode
                )

                if (isCompleted) {
                    _latestCompletedRecord.value = FocusSessionRecordEntity(
                        id = recordId,
                        subjectId = current.subjectId,
                        subjectName = current.subjectName,
                        taskName = current.taskName,
                        plannedDurationMinutes = current.plannedDurationMinutes,
                        actualDurationSeconds = actualSeconds,
                        startTimeMillis = System.currentTimeMillis() - (actualSeconds * 1000L),
                        endTimeMillis = System.currentTimeMillis(),
                        isCompleted = true,
                        earlyEndReason = null,
                        distractionCount = current.distractionCount,
                        mode = current.mode,
                        dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    )
                }
            }

            // Stop service
            val context = getApplication<Application>()
            val intent = Intent(context, FocusSessionService::class.java).apply {
                action = FocusSessionService.ACTION_STOP
            }
            context.startService(intent)
        }
    }

    fun dismissSessionComplete() {
        _latestCompletedRecord.value = null
    }

    fun startBreak(minutes: Int = 10) {
        FocusSessionService.startBreak(minutes)
    }

    fun endBreak() {
        FocusSessionService.endBreak()
    }

    fun addManualDistraction() {
        FocusSessionService.recordDistractionGlobal()
    }

    // --- Timetable Management ---
    fun addSession(session: TimetableSessionEntity) {
        viewModelScope.launch {
            val id = repository.insertTimetableSession(session)
            if (session.alarmEnabled && session.isEnabled) {
                scheduleSessionAlarm(session.copy(id = id))
            }
        }
    }

    fun updateSession(session: TimetableSessionEntity) {
        viewModelScope.launch {
            repository.updateTimetableSession(session)
            if (session.alarmEnabled && session.isEnabled) {
                scheduleSessionAlarm(session)
            } else {
                cancelSessionAlarm(session)
            }
        }
    }

    fun deleteSession(session: TimetableSessionEntity) {
        viewModelScope.launch {
            cancelSessionAlarm(session)
            repository.deleteTimetableSession(session)
        }
    }

    fun duplicateSession(sessionId: Long, targetDay: Int) {
        viewModelScope.launch {
            repository.duplicateTimetableSession(sessionId, targetDay)
        }
    }

    fun toggleSessionEnabled(session: TimetableSessionEntity) {
        viewModelScope.launch {
            val updated = session.copy(isEnabled = !session.isEnabled)
            repository.updateTimetableSession(updated)
            if (updated.isEnabled && updated.alarmEnabled) {
                scheduleSessionAlarm(updated)
            } else {
                cancelSessionAlarm(updated)
            }
        }
    }

    private fun scheduleSessionAlarm(session: TimetableSessionEntity) {
        try {
            val context = getApplication<Application>()
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            val intent = Intent(context, SessionAlarmReceiver::class.java).apply {
                putExtra("EXTRA_SESSION_ID", session.id)
                putExtra("EXTRA_SUBJECT_ID", session.subjectId)
                putExtra("EXTRA_SUBJECT_NAME", session.subjectName)
                putExtra("EXTRA_TASK_NAME", session.taskName)
                putExtra("EXTRA_START_TIME", session.startTime)
                putExtra("EXTRA_END_TIME", session.endTime)
                putExtra("EXTRA_DURATION_MINUTES", session.durationMinutes)
                putExtra("EXTRA_VOICE_NOTE_ID", session.voiceNoteId ?: -1L)
                putExtra("EXTRA_SOUND_URI", session.soundUri)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                session.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val timeParts = session.startTime.split(":")
            if (timeParts.size == 2) {
                val hour = timeParts[0].toIntOrNull() ?: 6
                val minute = timeParts[1].toIntOrNull() ?: 0

                val currentDayOfWeek = getCurrentDayOfWeek() // 1=Mon .. 7=Sun
                var daysDiff = session.dayOfWeek - currentDayOfWeek
                if (daysDiff < 0) {
                    daysDiff += 7
                }

                val targetCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, daysDiff)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // If scheduled for today, but the start time has already passed (or is within 10 seconds), schedule for next week
                if (daysDiff == 0 && targetCal.timeInMillis <= System.currentTimeMillis() + 10_000L) {
                    targetCal.add(Calendar.DAY_OF_YEAR, 7)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val clockInfo = AlarmManager.AlarmClockInfo(targetCal.timeInMillis, pendingIntent)
                    alarmManager.setAlarmClock(clockInfo, pendingIntent)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetCal.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetCal.timeInMillis, pendingIntent)
                }
            }
        } catch (_: Exception) {}
    }

    private fun cancelSessionAlarm(session: TimetableSessionEntity) {
        try {
            val context = getApplication<Application>()
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, SessionAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                session.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (_: Exception) {}
    }

    // --- Subjects Management ---
    fun addSubject(
        name: String,
        description: String = "",
        colorHex: String = "#38BDF8",
        iconName: String = "School",
        weeklyHours: Float = 10f,
        onCreated: ((SubjectEntity) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val id = repository.insertSubject(name, description, colorHex, iconName, weeklyHours)
            val created = SubjectEntity(
                id = id,
                name = name,
                description = description,
                colorHex = colorHex,
                iconName = iconName,
                targetWeeklyHours = weeklyHours
            )
            onCreated?.invoke(created)
        }
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    // --- Voice Recording ---
    fun startVoiceRecording(): Boolean {
        val started = voiceRecorderManager.startRecording()
        _isAudioRecording.value = started
        return started
    }

    fun stopVoiceRecording(title: String) {
        val result = voiceRecorderManager.stopRecording()
        val recFile = result.first
        val durationSec = result.second
        _isAudioRecording.value = false
        if (recFile != null && recFile.exists()) {
            val finalTitle = if (title.isBlank()) "Voice Memo ${SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date())}" else title
            viewModelScope.launch {
                repository.insertVoiceRecording(finalTitle, recFile.absolutePath, durationSec)
            }
        }
    }

    fun playVoiceRecording(recording: VoiceRecordingEntity) {
        if (_currentlyPlayingId.value == recording.id) {
            voiceRecorderManager.stopPlaying()
            _currentlyPlayingId.value = null
        } else {
            _currentlyPlayingId.value = recording.id
            voiceRecorderManager.playAudio(recording.filePath) {
                _currentlyPlayingId.value = null
            }
        }
    }

    fun deleteVoiceRecording(recording: VoiceRecordingEntity) {
        viewModelScope.launch {
            if (_currentlyPlayingId.value == recording.id) {
                voiceRecorderManager.stopPlaying()
                _currentlyPlayingId.value = null
            }
            try {
                File(recording.filePath).delete()
            } catch (_: Exception) {}
            repository.deleteVoiceRecording(recording)
        }
    }

    // --- AI Features ---
    fun generateTimetableWithAi(prompt: String) {
        viewModelScope.launch {
            _isAiGenerating.value = true
            val plan = geminiService.generateTimetable(prompt)
            _aiGeneratedTimetable.value = plan
            _isAiGenerating.value = false
        }
    }

    fun applyAiGeneratedTimetable() {
        val plan = _aiGeneratedTimetable.value ?: return
        viewModelScope.launch {
            plan.sessions.forEach { s ->
                val matchingSub = subjects.value.firstOrNull { it.name.equals(s.subjectName, ignoreCase = true) }
                val subId = matchingSub?.id ?: repository.insertSubject(s.subjectName, "#38BDF8", "Study")

                val entity = TimetableSessionEntity(
                    dayOfWeek = s.dayOfWeek,
                    subjectId = subId,
                    subjectName = s.subjectName,
                    taskName = s.taskName,
                    startTime = s.startTime,
                    endTime = s.endTime,
                    durationMinutes = s.durationMinutes,
                    colorHex = matchingSub?.colorHex ?: "#38BDF8",
                    alarmEnabled = true,
                    focusModeEnabled = true
                )
                val id = repository.insertTimetableSession(entity)
                scheduleSessionAlarm(entity.copy(id = id))
            }
            _aiGeneratedTimetable.value = null
        }
    }

    fun dismissAiTimetable() {
        _aiGeneratedTimetable.value = null
    }

    fun generateGoalPlanWithAi(prompt: String) {
        viewModelScope.launch {
            _isAiGenerating.value = true
            val plan = geminiService.planGoal(prompt)
            _aiGoalPlan.value = plan
            _isAiGenerating.value = false
        }
    }

    fun dismissAiGoalPlan() {
        _aiGoalPlan.value = null
    }

    fun analyzeWeekWithAi() {
        requestWeeklyCoachAnalysis()
    }

    fun requestWeeklyCoachAnalysis() {
        viewModelScope.launch {
            _isAiGenerating.value = true
            val stats = recentWeekStats.value
            val attempts = allQuestionAttempts.value
            val weak = calculatedWeakTopics.value

            val subjectAccuracies = attempts.groupBy { it.subjectId.uppercase() }
                .mapValues { (_, atts) ->
                    val correct = atts.count { it.isCorrect }
                    if (atts.isNotEmpty()) ((correct.toFloat() / atts.size) * 100).toInt() else 70
                }

            val strongestSub = subjectAccuracies.maxByOrNull { it.value }
            val weakestSub = subjectAccuracies.minByOrNull { it.value }
            val topWeakTopic = weak.firstOrNull()?.topicName ?: "Kirchhoff's Laws"

            val strongText = if (strongestSub != null) {
                "You're consistent in ${strongestSub.key.lowercase().replaceFirstChar { it.uppercase() }} (${strongestSub.value}% accuracy)"
            } else {
                "You're maintaining solid focus consistency across your scheduled study windows"
            }

            val weakText = if (weakestSub != null && weakestSub.key != strongestSub?.key) {
                "your accuracy in ${weakestSub.key.lowercase().replaceFirstChar { it.uppercase() }} dropped to ${weakestSub.value}% this week"
            } else {
                "targeted problem-solving can be improved in complex numerical topics"
            }

            val recommendation = "Revise $topWeakTopic and attempt 20 practice questions before your next test."
            val synthesizedSummary = "$strongText, but $weakText. $recommendation"

            val fallbackCoachResult = com.example.data.ai.CoachAnalysisResult(
                overallSummary = synthesizedSummary,
                bestDayObservation = if (strongestSub != null) "Strong grasp in ${strongestSub.key.lowercase().replaceFirstChar { it.uppercase() }} (${strongestSub.value}%)" else "High adherence on your peak focus day",
                weakestDayObservation = if (weak.isNotEmpty()) "Accuracy drops in ${weak.take(2).joinToString(", ") { it.topicName }}" else "Afternoon fatigue observed in late sessions",
                completionInsight = "Consistent morning sessions yield 20% higher retention and focus completion.",
                distractionInsight = "Distractions remained low with Focus Shield enabled.",
                recommendations = listOf(
                    recommendation,
                    "Schedule a 45-minute revision block for $topWeakTopic in your timetable.",
                    "Review incorrect attempts in the Question Bank before taking the full mock."
                )
            )

            try {
                val analysis = geminiService.analyzeWeek(stats)
                _aiCoachAnalysis.value = if (analysis.overallSummary.isNotBlank() && !analysis.overallSummary.contains("Great consistency overall")) {
                    analysis
                } else {
                    fallbackCoachResult
                }
            } catch (e: Exception) {
                _aiCoachAnalysis.value = fallbackCoachResult
            }
            if (_aiCoachAnalysis.value == null) {
                _aiCoachAnalysis.value = fallbackCoachResult
            }
            _isAiGenerating.value = false
        }
    }

    // --- AI Personal Study Assistant ---
    fun sendAiAssistantPrompt(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank()) return

        val userMsg = AiAssistantMessage(
            isUser = true,
            text = trimmed
        )
        _aiAssistantMessages.value = _aiAssistantMessages.value + userMsg
        _isAiAssistantLoading.value = true

        viewModelScope.launch {
            val name = userSettings.value?.userName ?: "Scholar"
            val subs = subjects.value.map { it.name }
            val isFocusActive = activeSessionState.value.isActive

            val result = geminiService.interactWithAssistant(
                userInput = trimmed,
                userName = name,
                existingSubjects = subs,
                isFocusActive = isFocusActive
            )

            // Execute automated actions based on AI interpretation
            when (result.actionType) {
                "SCHEDULE_AND_LOCK" -> {
                    val subName = result.subjectName ?: subs.firstOrNull() ?: "Mathematics"
                    val existing = subjects.value.firstOrNull { it.name.equals(subName, ignoreCase = true) }
                    val subId = existing?.id ?: repository.insertSubject(subName, "#38BDF8", "Study")

                    val curDay = getCurrentDayOfWeek()
                    val sessionEntity = TimetableSessionEntity(
                        dayOfWeek = curDay,
                        subjectId = subId,
                        subjectName = subName,
                        taskName = result.taskName ?: "Core Study Block",
                        startTime = result.startTime,
                        endTime = result.endTime,
                        durationMinutes = result.durationMinutes,
                        colorHex = existing?.colorHex ?: "#38BDF8",
                        alarmEnabled = true,
                        focusModeEnabled = true,
                        isEnabled = true
                    )
                    val newId = repository.insertTimetableSession(sessionEntity)
                    scheduleSessionAlarm(sessionEntity.copy(id = newId))

                    // Ring alarm immediately so user gets active ringing understanding that timetable has started!
                    val ringInfo = RingingSessionInfo(
                        sessionId = newId,
                        subjectId = subId,
                        subjectName = subName,
                        taskName = result.taskName ?: "Core Study Block",
                        startTime = result.startTime,
                        endTime = result.endTime,
                        durationMinutes = result.durationMinutes,
                        colorHex = existing?.colorHex ?: "#38BDF8"
                    )
                    AlarmRingingManager.startRinging(getApplication(), ringInfo)

                    // If lockApps is requested, immediately engage distraction shield and start session
                    if (result.lockApps) {
                        startFocusSession(
                            subjectId = subId,
                            subjectName = subName,
                            taskName = result.taskName ?: "Core Study Block",
                            durationMinutes = result.durationMinutes,
                            mode = "COUNTDOWN"
                        )
                    }
                    syncWithRedis()
                }
                "STOP_FOCUS" -> {
                    confirmEndSession(isCompleted = false, reason = "Stopped via AI Assistant")
                    stopAlarmRinging()
                    syncWithRedis()
                }
            }

            val aiMsg = AiAssistantMessage(
                isUser = false,
                text = result.replyMessage,
                actionType = result.actionType,
                tips = result.guidanceTips
            )
            _aiAssistantMessages.value = _aiAssistantMessages.value + aiMsg
            _isAiAssistantLoading.value = false
        }
    }

    fun aiQuickScheduleAndLock(subjectName: String? = null, startTime: String = "06:00", endTime: String = "08:00") {
        val s = subjectName ?: subjects.value.firstOrNull()?.name ?: "Mathematics"
        sendAiAssistantPrompt("Please schedule $s timetable from $startTime to $endTime and lock social media apps")
    }

    fun aiQuickStudyGuidance() {
        sendAiAssistantPrompt("How should I prepare for studying? Give me guidance.")
    }

    fun aiQuickAppStructure() {
        sendAiAssistantPrompt("Explain the app structure and features.")
    }

    fun aiQuickStopFocus() {
        sendAiAssistantPrompt("Stop the focus session and unlock apps.")
    }

    // --- Ringing & Redis Backend Actions ---
    fun stopAlarmRinging() {
        AlarmRingingManager.stopRinging()
    }

    fun testOrPreviewRinging(session: TimetableSessionEntity) {
        val info = RingingSessionInfo(
            sessionId = session.id,
            subjectId = session.subjectId,
            subjectName = session.subjectName,
            taskName = session.taskName,
            startTime = session.startTime,
            endTime = session.endTime,
            durationMinutes = session.durationMinutes,
            colorHex = session.colorHex,
            soundUri = session.soundUri
        )
        AlarmRingingManager.startRinging(getApplication(), info)
    }

    fun startFocusFromRinging(session: RingingSessionInfo) {
        stopAlarmRinging()
        startFocusSession(
            subjectId = session.subjectId,
            subjectName = session.subjectName,
            taskName = session.taskName,
            durationMinutes = session.durationMinutes,
            mode = "COUNTDOWN"
        )
    }

    fun syncWithRedis() {
        viewModelScope.launch {
            redisDatabaseManager.syncDatabaseWithRedis(db, userSettings.value)
        }
    }

    // --- Settings & Profile ---
    fun updateSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.updateUserSettings(settings)
        }
    }

    fun completeOnboarding(goals: String, dailyHours: Int, schedule: String) {
        viewModelScope.launch {
            val current = repository.getUserSettingsSync()
            repository.updateUserSettings(
                current.copy(
                    isOnboardingComplete = true,
                    userGoals = goals,
                    dailyGoalMinutes = dailyHours * 60,
                    preferredSchedule = schedule
                )
            )
        }
    }

    fun signInWithGoogleCredential(activityContext: Context, customClientId: String? = null) {
        viewModelScope.launch {
            authManager.signInWithGoogleCredential(activityContext, customClientId)
        }
    }

    fun connectGoogleProfile(name: String, email: String, photoUrl: String? = null) {
        authManager.connectVerifiedGoogleProfile(name, email, photoUrl)
        viewModelScope.launch {
            val current = repository.getUserSettingsSync()
            repository.updateUserSettings(
                current.copy(
                    userName = name,
                    userEmail = email,
                    isGoogleSignedIn = true,
                    cloudSyncEnabled = true,
                    lastSyncTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun signInWithGoogle(name: String, email: String, photoUrl: String? = null) {
        connectGoogleProfile(name, email, photoUrl)
    }

    fun signOutGoogle(activityContext: Context? = null) {
        viewModelScope.launch {
            authManager.signOut(activityContext)
            val current = repository.getUserSettingsSync()
            repository.updateUserSettings(
                current.copy(
                    userName = "Scholar",
                    isGoogleSignedIn = false,
                    userEmail = null,
                    cloudSyncEnabled = false
                )
            )
        }
    }

    fun seedDemoData() {
        viewModelScope.launch {
            repository.seedDemoData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceRecorderManager.release()
    }

    companion object {
        fun getCurrentDayOfWeek(): Int {
            val cal = Calendar.getInstance()
            return when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }
        }

        fun getCurrentFormattedTime(): String {
            return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()).uppercase()
        }

        fun getGreetingForCurrentHour(): String {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when (hour) {
                in 5..11 -> "Good Morning"
                in 12..16 -> "Good Afternoon"
                else -> "Good Evening"
            }
        }
    }
}

data class WeakTopicInfo(
    val topicName: String,
    val subjectId: String,
    val chapterId: String,
    val totalAttempts: Int,
    val correctAttempts: Int,
    val accuracy: Int
)
