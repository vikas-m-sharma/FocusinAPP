package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.MistakeEntity
import com.example.data.model.MistakeReason
import com.example.data.repository.FocusinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * UI state for Mistake Diary screen
 */
data class MistakeDiaryUiState(
    val mistakes: List<MistakeEntity> = emptyList(),
    val filteredMistakes: List<MistakeEntity> = emptyList(),
    val totalMistakesCount: Int = 0,
    val unresolvedCount: Int = 0,
    val resolvedCount: Int = 0,
    val selectedReasonFilter: MistakeReason? = null,
    val selectedSubjectFilter: String = "ALL",
    val selectedYearFilter: Int? = null,
    val selectedChapterFilter: String? = null,
    val availableYears: List<Int> = emptyList(),
    val sillyMistakeCount: Int = 0,
    val formulaForgotCount: Int = 0,
    val misreadQuestionCount: Int = 0,
    val conceptGapCount: Int = 0,
    val untaggedCount: Int = 0,
    val isLoading: Boolean = false
)

/**
 * Dedicated Mistake Diary ViewModel that manages Room database persistence,
 * automatic capture of incorrect test answers, tagging by error type,
 * notes editing, and targeted re-testing.
 */
class MistakeDiaryViewModel(
    private val repository: FocusinRepository
) : ViewModel() {

    private val _selectedReasonFilter = MutableStateFlow<MistakeReason?>(null)
    val selectedReasonFilter: StateFlow<MistakeReason?> = _selectedReasonFilter.asStateFlow()

    private val _selectedSubjectFilter = MutableStateFlow("ALL")
    val selectedSubjectFilter: StateFlow<String> = _selectedSubjectFilter.asStateFlow()

    private val _selectedYearFilter = MutableStateFlow<Int?>(null)
    val selectedYearFilter: StateFlow<Int?> = _selectedYearFilter.asStateFlow()

    private val _selectedChapterFilter = MutableStateFlow<String?>(null)
    val selectedChapterFilter: StateFlow<String?> = _selectedChapterFilter.asStateFlow()

    // Observe unresolved mistakes from Room database
    val allMistakesFlow = repository.allMistakes

    val uiState: StateFlow<MistakeDiaryUiState> = combine(
        repository.allMistakes,
        _selectedReasonFilter,
        _selectedSubjectFilter,
        _selectedYearFilter,
        _selectedChapterFilter
    ) { allMistakes, reasonFilter, subjectFilter, yearFilter, chapterFilter ->
        val unresolved = allMistakes.filter { !it.isResolved }
        val resolved = allMistakes.filter { it.isResolved }

        val sillyCount = unresolved.count { it.errorReason == MistakeReason.SILLY_MISTAKE.name }
        val formulaCount = unresolved.count { it.errorReason == MistakeReason.FORMULA_FORGOT.name }
        val misreadCount = unresolved.count { it.errorReason == MistakeReason.MISREAD_QUESTION.name }
        val conceptCount = unresolved.count { it.errorReason == MistakeReason.CONCEPT_GAP.name }
        val untaggedCount = unresolved.count { it.errorReason == MistakeReason.UNTAGGED.name }

        val years = unresolved.mapNotNull { it.examYear }.distinct().sortedDescending()

        val filtered = unresolved.filter { mistake ->
            val matchesReason = reasonFilter == null || mistake.errorReason == reasonFilter.name
            val matchesSubject = subjectFilter == "ALL" || mistake.subjectName.equals(subjectFilter, ignoreCase = true)
            val matchesYear = yearFilter == null || mistake.examYear == yearFilter
            val matchesChapter = chapterFilter == null || mistake.testTitle.contains(chapterFilter, ignoreCase = true) || mistake.topicName.contains(chapterFilter, ignoreCase = true)
            matchesReason && matchesSubject && matchesYear && matchesChapter
        }

        MistakeDiaryUiState(
            mistakes = allMistakes,
            filteredMistakes = filtered,
            totalMistakesCount = allMistakes.size,
            unresolvedCount = unresolved.size,
            resolvedCount = resolved.size,
            selectedReasonFilter = reasonFilter,
            selectedSubjectFilter = subjectFilter,
            selectedYearFilter = yearFilter,
            selectedChapterFilter = chapterFilter,
            availableYears = years,
            sillyMistakeCount = sillyCount,
            formulaForgotCount = formulaCount,
            misreadQuestionCount = misreadCount,
            conceptGapCount = conceptCount,
            untaggedCount = untaggedCount,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MistakeDiaryUiState(isLoading = true)
    )

    fun setYearFilter(year: Int?) {
        _selectedYearFilter.value = year
    }

    fun setChapterFilter(chapter: String?) {
        _selectedChapterFilter.value = chapter
    }

    // Initially empty. Mistakes are automatically added when student attempts question papers and makes errors.

    /**
     * Automatically called after any test submission to log wrong questions into Room database
     */
    fun recordWrongQuestion(
        questionId: String,
        testTitle: String,
        questionText: String,
        selectedOption: String,
        correctOption: String,
        options: List<String>,
        explanation: String,
        subjectName: String,
        topicName: String,
        initialReason: MistakeReason = MistakeReason.UNTAGGED,
        studentNotes: String = ""
    ) {
        viewModelScope.launch {
            val entity = MistakeEntity(
                id = UUID.randomUUID().toString(),
                questionId = questionId,
                testTitle = testTitle,
                questionText = questionText,
                selectedOption = selectedOption,
                correctOption = correctOption,
                optionsJson = options.joinToString("|||"),
                explanation = explanation,
                subjectName = subjectName,
                topicName = topicName,
                errorReason = initialReason.name,
                studentNotes = studentNotes,
                timestamp = System.currentTimeMillis(),
                isResolved = false
            )
            repository.insertMistake(entity)
        }
    }

    /**
     * Updates the student's selected error tag (Silly, Formula, Misread, Concept)
     */
    fun updateErrorTag(mistakeId: String, reason: MistakeReason, notes: String? = null) {
        viewModelScope.launch {
            repository.updateMistakeReason(mistakeId, reason.name, notes)
        }
    }

    /**
     * Updates personal reminder notes for this question
     */
    fun updateStudentNotes(mistakeId: String, notes: String) {
        viewModelScope.launch {
            val current = uiState.value.mistakes.find { it.id == mistakeId }
            val currentReason = current?.errorReason ?: MistakeReason.UNTAGGED.name
            repository.updateMistakeReason(mistakeId, currentReason, notes)
        }
    }

    /**
     * Mark mistake as resolved (student solved it successfully in Re-Test)
     */
    fun markResolved(mistakeId: String, isResolved: Boolean) {
        viewModelScope.launch {
            repository.markMistakeResolved(mistakeId, isResolved)
        }
    }

    /**
     * Delete mistake from database
     */
    fun deleteMistake(mistakeId: String) {
        viewModelScope.launch {
            repository.deleteMistakeById(mistakeId)
        }
    }

    fun setReasonFilter(reason: MistakeReason?) {
        _selectedReasonFilter.value = reason
    }

    fun setSubjectFilter(subject: String) {
        _selectedSubjectFilter.value = subject
    }

    /**
     * Prepares list of question ids or texts for targeted 1-Tap Re-Test
     */
    fun getTargetedReTestQuestionIds(): List<String> {
        val list = uiState.value.filteredMistakes
        return if (list.isNotEmpty()) list.map { it.id } else uiState.value.mistakes.filter { !it.isResolved }.map { it.id }
    }
}
