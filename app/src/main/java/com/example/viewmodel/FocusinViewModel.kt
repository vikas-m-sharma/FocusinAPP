package com.example.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.VoiceRecorderManager
import com.example.data.ai.AiChatMessage
import com.example.data.ai.CoachAnalysisResult
import com.example.data.ai.GeminiFocusinService
import com.example.data.ai.GeneratedTimetablePlan
import com.example.data.ai.GoalPlanResult
import com.example.data.auth.AuthManager
import com.example.data.auth.AuthState
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AchievementEntity
import com.example.data.local.entity.ChapterProgressEntity
import com.example.data.local.entity.DailyStatsEntity
import com.example.data.local.entity.FocusSessionRecordEntity
import com.example.data.local.entity.QuestionAttemptRecordEntity
import com.example.data.local.entity.QuizAttemptRecordEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TimetableSessionEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.local.entity.VoiceRecordingEntity
import com.example.data.repository.FocusinRepository
import com.example.receiver.SessionAlarmReceiver
import com.example.service.ActiveSessionState
import com.example.service.FocusSessionService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FocusinViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = FocusinRepository(db)
    val voiceRecorderManager = VoiceRecorderManager(application)
    private val geminiService = GeminiFocusinService()
    val authManager = AuthManager(application)
    private val authPrefs = application.getSharedPreferences("focusin_auth_session", Context.MODE_PRIVATE)
    private val _isUserLoggedIn = MutableStateFlow(authPrefs.getBoolean("is_logged_in", false))
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    val authState: StateFlow<AuthState> = authManager.authState

    // Text To Speech Engine
    private var tts: TextToSpeech? = null

    // Live clock and greeting state
    private val _currentTimeString = MutableStateFlow(getCurrentFormattedTime())
    val currentTimeString: StateFlow<String> = _currentTimeString.asStateFlow()

    private val _currentGreeting = MutableStateFlow(getGreetingForCurrentHour())
    val currentGreeting: StateFlow<String> = _currentGreeting.asStateFlow()

    // Database Flows
    val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allSessions: StateFlow<List<TimetableSessionEntity>> = repository.allTimetableSessions
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val historyRecords: StateFlow<List<FocusSessionRecordEntity>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentWeekStats: StateFlow<List<DailyStatsEntity>> = repository.recentWeekStats
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentMonthStats: StateFlow<List<DailyStatsEntity>> = repository.recentMonthStats
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentYearStats: StateFlow<List<DailyStatsEntity>> = repository.recentYearStats
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val voiceRecordings: StateFlow<List<VoiceRecordingEntity>> = repository.allVoiceRecordings
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val achievements: StateFlow<List<AchievementEntity>> = repository.allAchievements
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val userSettings: StateFlow<UserSettingsEntity?> = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val todayStats: StateFlow<DailyStatsEntity?> = repository.getTodayStats()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val chapterProgressList: StateFlow<List<ChapterProgressEntity>> = repository.allChapterProgress
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val questionAttemptsList: StateFlow<List<QuestionAttemptRecordEntity>> = repository.allQuestionAttempts
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val quizAttemptsList: StateFlow<List<QuizAttemptRecordEntity>> = repository.allQuizAttempts
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allQuestionAttempts: StateFlow<List<QuestionAttemptRecordEntity>> = questionAttemptsList
    val allQuizAttempts: StateFlow<List<QuizAttemptRecordEntity>> = quizAttemptsList
    val allChapterProgress: StateFlow<List<ChapterProgressEntity>> = chapterProgressList

    val totalQuestionsAttempted: StateFlow<Int> = questionAttemptsList.map { it.size }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val totalQuestionsCorrect: StateFlow<Int> = questionAttemptsList.map { attempts -> attempts.count { it.isCorrect } }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val calculatedWeakTopics: StateFlow<List<WeakTopicInfo>> = questionAttemptsList.map { attempts ->
        if (attempts.isEmpty()) emptyList()
        else {
            attempts.groupBy { it.chapterName }.mapNotNull { (chName, list) ->
                val total = list.size
                val correct = list.count { it.isCorrect }
                val accuracy = ((correct.toFloat() / total) * 100).toInt()
                if (accuracy < 65) {
                    val first = list.first()
                    WeakTopicInfo(
                        chapterId = first.chapterName.lowercase().replace(" ", "_"),
                        topicName = if (first.topicName.isNotBlank()) first.topicName else first.chapterName,
                        subjectName = first.subjectName,
                        accuracy = accuracy,
                        totalAttempts = total,
                        correctAttempts = correct
                    )
                } else null
            }.sortedBy { it.accuracy }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val activeSessionState: StateFlow<ActiveSessionState> = FocusSessionService.sessionState

    // Timetable selected day (1=Mon..7=Sun)
    private val _selectedDay = MutableStateFlow(getCurrentDayOfWeek())
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    // Next upcoming or current session for Today
    val todaySessions: StateFlow<List<TimetableSessionEntity>> = combine(allSessions, _selectedDay) { sessions, _ ->
        val currentDay = getCurrentDayOfWeek()
        sessions.filter { it.dayOfWeek == currentDay && it.isEnabled }
            .sortedBy { it.startTime }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val nextSession: StateFlow<TimetableSessionEntity?> = todaySessions.combine(activeSessionState) { sessions, active ->
        if (active.isActive) {
            null // Active session is shown instead
        } else {
            val nowTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            sessions.firstOrNull { it.endTime > nowTime } ?: sessions.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // AI States
    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _aiGeneratedTimetable = MutableStateFlow<GeneratedTimetablePlan?>(null)
    val aiGeneratedTimetable: StateFlow<GeneratedTimetablePlan?> = _aiGeneratedTimetable.asStateFlow()

    private val _aiGoalPlan = MutableStateFlow<GoalPlanResult?>(null)
    val aiGoalPlan: StateFlow<GoalPlanResult?> = _aiGoalPlan.asStateFlow()

    private val _aiCoachAnalysis = MutableStateFlow<CoachAnalysisResult?>(null)
    val aiCoachAnalysis: StateFlow<CoachAnalysisResult?> = _aiCoachAnalysis.asStateFlow()

    // Interactive AI Assistant Voice & Multi-Turn Gemini Conversation
    private val _aiAssistantMessage = MutableStateFlow<String>("")
    val aiAssistantMessage: StateFlow<String> = _aiAssistantMessage.asStateFlow()

    private val _isAiAssistantSpeaking = MutableStateFlow(false)
    val isAiAssistantSpeaking: StateFlow<Boolean> = _isAiAssistantSpeaking.asStateFlow()

    private val _isAiAssistantActive = MutableStateFlow(true)
    val isAiAssistantActive: StateFlow<Boolean> = _isAiAssistantActive.asStateFlow()

    private val _isSocialAppsLocked = MutableStateFlow(false)
    val isSocialAppsLocked: StateFlow<Boolean> = _isSocialAppsLocked.asStateFlow()

    private val _aiChatHistory = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val aiChatHistory: StateFlow<List<AiChatMessage>> = _aiChatHistory.asStateFlow()

    // Requested Page Navigation Event
    private val _aiRequestedNavigation = MutableStateFlow<String?>(null)
    val aiRequestedNavigation: StateFlow<String?> = _aiRequestedNavigation.asStateFlow()

    // Timetable Ringing Alarm Overlay State
    private val _isRingingSession = MutableStateFlow<TimetableSessionEntity?>(null)
    val isRingingSession: StateFlow<TimetableSessionEntity?> = _isRingingSession.asStateFlow()

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

    // PYQ Ingestion Report State
    private val _pyqImportReport = MutableStateFlow<com.example.data.importer.PyqImportReport?>(null)
    val pyqImportReport: StateFlow<com.example.data.importer.PyqImportReport?> = _pyqImportReport.asStateFlow()

    // PYQ Review Queue & Conversion State
    val pyqReviewQueue: StateFlow<List<com.example.data.importer.PyqNormalizedQuestion>> =
        repository.pyqReviewQueueManager.queue

    private val _latestConversionReport = MutableStateFlow<com.example.data.importer.PyqConversionReport?>(null)
    val latestConversionReport: StateFlow<com.example.data.importer.PyqConversionReport?> =
        _latestConversionReport.asStateFlow()

    fun convertPyqSource(
        document: com.example.data.importer.PyqSourceDocument,
        onComplete: ((com.example.data.importer.PyqConversionReport) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                val (_, report) = repository.convertPyqSourceDocument(document)
                _latestConversionReport.value = report
                onComplete?.invoke(report)
            } catch (e: Exception) {
                Log.e("FocusinViewModel", "Conversion failed", e)
            }
        }
    }

    fun approveReviewItem(questionId: String, citation: String) {
        repository.approveReviewItem(questionId, citation)
    }

    fun keepReviewItemUnverified(questionId: String) {
        repository.keepReviewItemUnverified(questionId)
    }

    fun rejectReviewItem(questionId: String) {
        repository.rejectReviewItem(questionId)
    }

    fun updateReviewItemChapter(questionId: String, subject: String, chapter: String, topic: String) {
        repository.updateReviewItemChapter(questionId, subject, chapter, topic)
    }

    fun updateReviewItemAnswer(questionId: String, answerIdx: Int) {
        repository.updateReviewItemAnswer(questionId, answerIdx)
    }

    fun commitReviewedPyqs(onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.commitReviewedQuestions()
            onResult(count)
        }
    }

    fun clearReviewQueue() {
        repository.clearReviewQueue()
    }

    // Custom Test Session for OMR Exam Mode parameterization
    private val _customTestQuestions = MutableStateFlow<List<com.example.data.model.NeetQuestion>?>(null)
    val customTestQuestions: StateFlow<List<com.example.data.model.NeetQuestion>?> = _customTestQuestions.asStateFlow()

    fun setCustomTestQuestions(questions: List<com.example.data.model.NeetQuestion>) {
        _customTestQuestions.value = questions
    }

    fun clearCustomTestQuestions() {
        _customTestQuestions.value = null
    }

    fun triggerPyqImport() {
        viewModelScope.launch {
            try {
                val report = repository.importPyqAssets(getApplication())
                _pyqImportReport.value = report
                Log.d("FocusinViewModel", "Manual PYQ import completed: imported=${report.importedCount}, skipped=${report.skippedCount}, errors=${report.errors.size}")
            } catch (e: Exception) {
                Log.e("FocusinViewModel", "Failed importing PYQ assets", e)
            }
        }
    }

    fun getQuestionsForChapter(chapterId: String): Flow<List<com.example.data.local.entity.QuestionEntity>> =
        repository.getQuestionsForChapter(chapterId)

    fun getQuestionsForChapterWithFilters(
        chapterId: String,
        sourceExam: String? = null,
        startYear: Int? = null,
        endYear: Int? = null,
        difficulty: String? = null,
        isOfficialOnly: Boolean = false
    ): Flow<List<com.example.data.local.entity.QuestionEntity>> =
        repository.getQuestionsForChapterWithFilters(chapterId, sourceExam, startYear, endYear, difficulty, isOfficialOnly)

    suspend fun getQuestionsForChapterWithFiltersSync(
        chapterId: String,
        sourceExam: String? = null,
        startYear: Int? = null,
        endYear: Int? = null,
        difficulty: String? = null,
        isOfficialOnly: Boolean = false
    ): List<com.example.data.local.entity.QuestionEntity> =
        repository.getQuestionsForChapterWithFiltersSync(chapterId, sourceExam, startYear, endYear, difficulty, isOfficialOnly)

    fun getQuestionCountByYear(chapterId: String): Flow<List<com.example.data.local.dao.YearCount>> =
        repository.getQuestionCountByYear(chapterId)

    fun getVerifiedQuestionCountByYear(chapterId: String): Flow<List<com.example.data.local.dao.YearCount>> =
        repository.getVerifiedQuestionCountByYear(chapterId)

    fun getUnverifiedQuestionCountByYear(chapterId: String): Flow<List<com.example.data.local.dao.YearCount>> =
        repository.getUnverifiedQuestionCountByYear(chapterId)

    fun getBookmarkedQuestions(): Flow<List<com.example.data.local.entity.QuestionEntity>> =
        repository.bookmarkedQuestions

    fun getQuestionCountByExam(chapterId: String): Flow<List<com.example.data.local.dao.ExamCount>> =
        repository.getQuestionCountByExam(chapterId)

    fun getQuestionCountByTopic(chapterId: String): Flow<List<com.example.data.local.dao.TopicCount>> =
        repository.getQuestionCountByTopic(chapterId)

    fun getQuestionCountForChapter(chapterId: String): Flow<Int> =
        repository.getQuestionCountForChapter(chapterId)

    fun getUnansweredQuestionsForChapter(chapterId: String): Flow<List<com.example.data.local.entity.QuestionEntity>> =
        repository.getUnansweredQuestionsForChapter(chapterId)

    fun getMistakeQuestionsForChapter(chapterId: String): Flow<List<com.example.data.local.entity.QuestionEntity>> =
        repository.getMistakeQuestionsForChapter(chapterId)

    fun getTotalQuestionCount(): Flow<Int> = repository.getTotalQuestionCount()
    fun getDistinctExamYears(): Flow<List<Int>> = repository.getDistinctExamYears()
    fun getTotalOfficialPyqCount(): Flow<Int> = repository.getTotalOfficialPyqCount()
    fun getTotalUnverifiedCount(): Flow<Int> = repository.getTotalUnverifiedCount()
    fun getTotalSampleCount(): Flow<Int> = repository.getTotalSampleCount()
    fun getTotalGeneratedCount(): Flow<Int> = repository.getTotalGeneratedCount()
    fun getTotalImportedPapersCount(): Flow<Int> = repository.getTotalImportedPapersCount()
    fun getHistoricalCoverageReports(): Flow<List<com.example.data.catalog.YearCoverageReport>> =
        repository.getHistoricalCoverageReports()

    fun toggleQuestionBookmark(questionId: String, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleQuestionBookmark(questionId, isBookmarked)
        }
    }

    init {
        // Initialize Clear Male Voice TTS Engine
        try {
            tts = TextToSpeech(application) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                    tts?.setPitch(0.90f)        // Deeper, articulate male voice pitch
                    tts?.setSpeechRate(0.95f)    // Calm, natural male speech rate

                    val availableVoices = tts?.voices.orEmpty()
                    // Select explicit MALE voice
                    val maleVoice = availableVoices.firstOrNull { voice ->
                        val name = voice.name.lowercase(Locale.US)
                        voice.locale.language == "en" &&
                        !voice.isNetworkConnectionRequired &&
                        !name.contains("female") && !name.contains("-f-") &&
                        (name.contains("male") || name.contains("-m-") || name.contains("en-us-x-sfg") || name.contains("en-us-x-iom") || name.contains("en-us-x-tpd") || name.contains("en-us-x-iol") || name.contains("en-us-x-jol"))
                    } ?: availableVoices.firstOrNull { voice ->
                        val name = voice.name.lowercase(Locale.US)
                        voice.locale.language == "en" && !name.contains("female") && !name.contains("-f-")
                    }

                    if (maleVoice != null) {
                        tts?.voice = maleVoice
                        Log.d("FocusinViewModel", "Selected Male TTS Voice: ${maleVoice.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FocusinViewModel", "TTS initialization failed", e)
        }

        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
            try {
                val report = repository.importPyqAssets(getApplication())
                _pyqImportReport.value = report
                Log.d("FocusinViewModel", "Startup PYQ Ingestion: imported=${report.importedCount}, skipped=${report.skippedCount}, errors=${report.errors.size}")
            } catch (e: Exception) {
                Log.e("FocusinViewModel", "Error running startup PYQ ingestion", e)
            }
            if (authPrefs.getBoolean("is_logged_in", false)) {
                val savedName = authPrefs.getString("displayName", "Scholar") ?: "Scholar"
                authManager.continueAsLocalUser(savedName)
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

        // Observe active session for natural completion & immediate transition to next alarm
        viewModelScope.launch {
            activeSessionState.collect { state ->
                if (state.isActive && state.isNaturallyCompleted) {
                    confirmEndSession(isCompleted = true, reason = null)
                }
            }
        }
    }

    // --- AI Assistant Speech & Interactions ---
    fun speakText(text: String) {
        if (!_isAiAssistantActive.value) return
        _isAiAssistantSpeaking.value = true
        _aiAssistantMessage.value = text
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "FocusinTTS")
        } catch (e: Exception) {
            Log.e("FocusinViewModel", "TTS speak failed", e)
        }
    }

    fun speakAiGreeting(userName: String) {
        if (!_isAiAssistantActive.value) return
        val greetingPrompt = "Welcome to Focusin, $userName! I am your Gemini AI Schedule Assistant. Can I create your study timetable (including 6 AM to 8 AM block), set high-priority alarms, and lock unnecessary social media apps for you?"
        speakText(greetingPrompt)
    }

    fun toggleTopicCompletion(chapterId: String, subjectName: String, chapterName: String, topicName: String) {
        viewModelScope.launch {
            val current = repository.getChapterProgressById(chapterId)
            val completedList = if (current != null) {
                try {
                    val arr = JSONArray(current.completedTopicsJson)
                    val list = mutableListOf<String>()
                    for (i in 0 until arr.length()) list.add(arr.getString(i))
                    list
                } catch (_: Exception) {
                    mutableListOf()
                }
            } else mutableListOf()

            if (completedList.contains(topicName)) {
                completedList.remove(topicName)
            } else {
                completedList.add(topicName)
            }

            val updatedEntity = ChapterProgressEntity(
                chapterId = chapterId,
                subjectName = subjectName,
                chapterName = chapterName,
                completedTopicsJson = JSONArray(completedList as List<*>).toString(),
                totalTopicsCount = 10,
                lastStudiedTimestamp = System.currentTimeMillis()
            )
            repository.updateChapterProgress(updatedEntity)
        }
    }

    fun recordQuestionAttempt(attempt: QuestionAttemptRecordEntity) {
        viewModelScope.launch {
            repository.recordQuestionAttempt(attempt)
        }
    }

    /**
     * Records an incorrect question attempt directly into Mistake Diary (Room DB)
     */
    fun recordMistake(
        questionId: String,
        testTitle: String,
        questionText: String,
        selectedOption: String,
        correctOption: String,
        options: List<String>,
        explanation: String,
        subjectName: String,
        topicName: String,
        sourceExam: String? = null,
        examYear: Int? = null
    ) {
        viewModelScope.launch {
            val entity = com.example.data.local.entity.MistakeEntity(
                id = java.util.UUID.randomUUID().toString(),
                questionId = questionId,
                testTitle = testTitle,
                questionText = questionText,
                selectedOption = selectedOption,
                correctOption = correctOption,
                optionsJson = options.joinToString("|||"),
                explanation = explanation,
                subjectName = subjectName,
                topicName = topicName,
                errorReason = com.example.data.model.MistakeReason.UNTAGGED.name,
                studentNotes = "",
                timestamp = System.currentTimeMillis(),
                isResolved = false,
                sourceExam = sourceExam,
                examYear = examYear
            )
            repository.insertMistake(entity)
        }
    }

    fun recordQuizAttempt(attempt: QuizAttemptRecordEntity) {
        viewModelScope.launch {
            repository.recordQuizAttempt(attempt)
        }
    }

    fun sendUserQueryToGemini(query: String) {
        if (query.isBlank()) return
        val userName = userSettings.value?.userName ?: "Scholar"
        val currentHistory = _aiChatHistory.value.toMutableList()
        val userMsg = AiChatMessage("USER", query)
        currentHistory.add(userMsg)
        _aiChatHistory.value = currentHistory

        viewModelScope.launch {
            _isAiGenerating.value = true
            val responseText = geminiService.chatWithGemini(query, currentHistory, userName)
            _isAiGenerating.value = false

            val updatedHistory = _aiChatHistory.value.toMutableList()
            updatedHistory.add(AiChatMessage("GEMINI", responseText))
            _aiChatHistory.value = updatedHistory

            // Speak response in clear male voice
            speakText(responseText)
        }
    }

    fun stopAiAssistant() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isAiAssistantSpeaking.value = false
        _isAiAssistantActive.value = false
        _aiAssistantMessage.value = "AI Assistant stopped by user."
    }

    fun triggerAiNavigation(route: String) {
        _aiRequestedNavigation.value = route
    }

    fun consumeAiNavigation() {
        _aiRequestedNavigation.value = null
    }

    fun autoScheduleAndLockSocialApps(userName: String = "Scholar") {
        viewModelScope.launch {
            _isAiAssistantActive.value = true
            val defaultSubjects = listOf(
                Triple("Mathematics", "Calculus & Problem Sets", "#38BDF8"),
                Triple("Programming & AI", "Data Structures & Neural Nets", "#FBBF24"),
                Triple("Physics & Engineering", "Core Mechanics & Electricity", "#A78BFA")
            )

            val currentDay = getCurrentDayOfWeek()

            // 1. Create timetable sessions (e.g., 06:00 - 08:00)
            val s1 = TimetableSessionEntity(
                dayOfWeek = currentDay,
                subjectId = 0,
                subjectName = defaultSubjects[0].first,
                taskName = defaultSubjects[0].second,
                startTime = "06:00",
                endTime = "08:00",
                durationMinutes = 120,
                colorHex = defaultSubjects[0].third,
                alarmEnabled = true,
                focusModeEnabled = true
            )
            val id1 = repository.insertTimetableSession(s1)
            scheduleSessionAlarm(s1.copy(id = id1))

            val s2 = TimetableSessionEntity(
                dayOfWeek = currentDay,
                subjectId = 0,
                subjectName = defaultSubjects[1].first,
                taskName = defaultSubjects[1].second,
                startTime = "09:00",
                endTime = "11:00",
                durationMinutes = 120,
                colorHex = defaultSubjects[1].third,
                alarmEnabled = true,
                focusModeEnabled = true
            )
            val id2 = repository.insertTimetableSession(s2)
            scheduleSessionAlarm(s2.copy(id = id2))

            val s3 = TimetableSessionEntity(
                dayOfWeek = currentDay,
                subjectId = 0,
                subjectName = defaultSubjects[2].first,
                taskName = defaultSubjects[2].second,
                startTime = "14:00",
                endTime = "16:00",
                durationMinutes = 120,
                colorHex = defaultSubjects[2].third,
                alarmEnabled = true,
                focusModeEnabled = true
            )
            val id3 = repository.insertTimetableSession(s3)
            scheduleSessionAlarm(s3.copy(id = id3))

            // 2. Lock unnecessary social media apps
            val socialApps = "[\"Instagram\", \"TikTok\", \"YouTube\", \"Twitter\", \"Facebook\", \"Snapchat\", \"Netflix\", \"Reddit\"]"
            val currentSettings = repository.getUserSettingsSync()
            repository.updateUserSettings(
                currentSettings.copy(
                    blockedAppsJson = socialApps,
                    focusProtectionLevel = "ENHANCED"
                )
            )
            _isSocialAppsLocked.value = true

            val speechMsg = "Done, $userName! I have created your study timetable (including 6:00 AM to 8:00 AM block) with start alarms and locked 8 unnecessary social media apps. Opening your timetable schedule page now!"
            speakText(speechMsg)

            // Automatically navigate user to Schedule Page!
            delay(1200L)
            triggerAiNavigation("schedule")
        }
    }

    fun provideStudyGuidanceAndStructure(userName: String = "Scholar") {
        _isAiAssistantActive.value = true
        val guidance = "Certainly, $userName! Here is your personalized Focusin study strategy:\n1. Timetable: Your 6:00 AM to 8:00 AM study block triggers loud start alarms automatically.\n2. App Lock: Instagram, YouTube, TikTok, and Twitter are locked during sessions.\n3. Voice Studio: Personal motivational voice notes kickstart your study blocks.\nFocus on your prime morning window and take a 10-minute break after every 50 minutes of deep study!"
        speakText(guidance)
    }

    fun triggerTimetableRinging(session: TimetableSessionEntity) {
        _isRingingSession.value = session
    }

    fun dismissRingingOverlay() {
        _isRingingSession.value = null
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
        mode: String = "COUNTDOWN"
    ) {
        val context = getApplication<Application>()
        val intent = Intent(context, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_START
            putExtra(FocusSessionService.EXTRA_SUBJECT_ID, subjectId)
            putExtra(FocusSessionService.EXTRA_SUBJECT_NAME, subjectName)
            putExtra(FocusSessionService.EXTRA_TASK_NAME, taskName)
            putExtra(FocusSessionService.EXTRA_DURATION_MINUTES, durationMinutes)
            putExtra(FocusSessionService.EXTRA_MODE, mode)
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

            // Immediately check for next timetable alarm transition
            val upcoming = nextSession.value
            val userName = userSettings.value?.userName ?: "Scholar"
            if (upcoming != null && upcoming.subjectName != current.subjectName) {
                delay(800L)
                val speechAnnouncement = "Your ${current.subjectName} session has completed, $userName! Next scheduled session starting immediately: ${upcoming.subjectName} (${upcoming.taskName}). Alarm ringing!"
                speakText(speechAnnouncement)

                // Immediately activate alarm and ringing overlay for the next session
                triggerTimetableRinging(upcoming)
                testTriggerAlarmNow(upcoming)
            } else {
                val finishMsg = "Great job, $userName! You have completed all scheduled focus blocks for today."
                speakText(finishMsg)
            }
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

    fun testTriggerAlarmNow(session: TimetableSessionEntity) {
        val context = getApplication<Application>()
        val intent = Intent(context, SessionAlarmReceiver::class.java).apply {
            putExtra("EXTRA_SESSION_ID", session.id)
            putExtra("EXTRA_SUBJECT_NAME", session.subjectName)
            putExtra("EXTRA_TASK_NAME", session.taskName)
            putExtra("EXTRA_VOICE_NOTE_ID", session.voiceNoteId ?: -1L)
            putExtra("EXTRA_SOUND_URI", session.soundUri)
        }
        context.sendBroadcast(intent)
        _isRingingSession.value = session
    }

    private fun scheduleSessionAlarm(session: TimetableSessionEntity) {
        try {
            val context = getApplication<Application>()
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            val intent = Intent(context, SessionAlarmReceiver::class.java).apply {
                putExtra("EXTRA_SESSION_ID", session.id)
                putExtra("EXTRA_SUBJECT_NAME", session.subjectName)
                putExtra("EXTRA_TASK_NAME", session.taskName)
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
                val hour = timeParts[0].toIntOrNull() ?: 9
                val minute = timeParts[1].toIntOrNull() ?: 0

                val now = Calendar.getInstance()
                val target = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)

                    val targetCalendarDay = when (session.dayOfWeek) {
                        1 -> Calendar.MONDAY
                        2 -> Calendar.TUESDAY
                        3 -> Calendar.WEDNESDAY
                        4 -> Calendar.THURSDAY
                        5 -> Calendar.FRIDAY
                        6 -> Calendar.SATURDAY
                        else -> Calendar.SUNDAY
                    }

                    val currentDayOfWeek = get(Calendar.DAY_OF_WEEK)
                    var daysUntil = targetCalendarDay - currentDayOfWeek
                    if (daysUntil < 0) {
                        daysUntil += 7
                    } else if (daysUntil == 0 && timeInMillis <= now.timeInMillis) {
                        daysUntil = 7
                    }
                    add(Calendar.DAY_OF_YEAR, daysUntil)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
                }
            }
        } catch (e: Exception) {
            Log.e("FocusinViewModel", "Failed to schedule session alarm", e)
        }
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
        viewModelScope.launch {
            _isAiGenerating.value = true
            val stats = recentWeekStats.value
            val analysis = geminiService.analyzeWeek(stats)
            _aiCoachAnalysis.value = analysis
            _isAiGenerating.value = false
        }
    }

    fun requestWeeklyCoachAnalysis() {
        analyzeWeekWithAi()
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

    fun signInWithGoogle(name: String, email: String) {
        loginUser(name = name, email = email, exam = "NEET", isGoogle = true)
    }

    fun loginUser(name: String, email: String, exam: String = "NEET", isGoogle: Boolean = false) {
        val cleanEmail = email.trim().lowercase()
        val userKey = "has_logged_in_before_${if (cleanEmail.isBlank()) "guest_user" else cleanEmail}"
        val userHasLoggedBefore = authPrefs.getBoolean(userKey, false)

        authPrefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("displayName", name)
            .putString("email", email)
            .putString("exam", exam)
            .putBoolean(userKey, true)
            .apply()
        _isUserLoggedIn.value = true

        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded(isFirstTimeUser = !userHasLoggedBefore)
            val current = repository.getUserSettingsSync()
            val formattedName = if (name.isNotBlank()) name else "Scholar"
            repository.updateUserSettings(
                current.copy(
                    userName = formattedName,
                    userEmail = email.ifBlank { null },
                    userGoals = exam,
                    isGoogleSignedIn = isGoogle,
                    isOnboardingComplete = true
                )
            )
            if (isGoogle) {
                authManager.connectVerifiedGoogleProfile(formattedName, email)
            } else {
                authManager.continueAsLocalUser(formattedName)
            }
            speakAiGreeting(formattedName)
        }
    }

    fun logoutUser(activityContext: Context? = null) {
        authPrefs.edit()
            .putBoolean("is_logged_in", false)
            .apply()
        _isUserLoggedIn.value = false

        viewModelScope.launch {
            authManager.signOut(activityContext)
            val current = repository.getUserSettingsSync()
            repository.updateUserSettings(
                current.copy(
                    isGoogleSignedIn = false,
                    userEmail = null
                )
            )
        }
    }

    fun signOutGoogle(activityContext: Context? = null) {
        logoutUser(activityContext)
    }

    fun getQuizAttemptById(id: Long): Flow<QuizAttemptRecordEntity?> {
        return flow {
            emit(repository.getQuizAttemptById(id))
        }
    }

    fun scheduleLearningSession(
        subjectName: String,
        topicName: String,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        durationMinutes: Int,
        focusModeEnabled: Boolean = true,
        alarmEnabled: Boolean = true,
        protectionLevel: String = "ENHANCED"
    ) {
        viewModelScope.launch {
            val colorHex = when (subjectName.lowercase()) {
                "physics" -> "#06B6D4"
                "chemistry" -> "#10B981"
                "biology" -> "#8B5CF6"
                else -> "#3B82F6"
            }
            val session = TimetableSessionEntity(
                subjectId = 1L,
                subjectName = subjectName,
                taskName = topicName,
                dayOfWeek = dayOfWeek,
                startTime = startTime,
                endTime = endTime,
                durationMinutes = durationMinutes,
                colorHex = colorHex,
                focusModeEnabled = focusModeEnabled,
                alarmEnabled = alarmEnabled,
                isEnabled = true
            )
            val id = repository.insertTimetableSession(session)
            if (alarmEnabled) {
                scheduleSessionAlarm(session.copy(id = id))
            }
        }
    }

    fun seedDemoData() {
        viewModelScope.launch {
            repository.seedDemoData()
        }
    }

    fun ensureDefaultDataSeeded() {
        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
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
                in 5..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                in 17..22 -> "Good evening"
                else -> "Night owl focus"
            }
        }
    }
}

data class WeakTopicInfo(
    val chapterId: String,
    val topicName: String,
    val subjectName: String,
    val accuracy: Int,
    val totalAttempts: Int,
    val correctAttempts: Int
)

