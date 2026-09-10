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

    // Observe unresolved mistakes from Room database
    val allMistakesFlow = repository.allMistakes

    val uiState: StateFlow<MistakeDiaryUiState> = combine(
        repository.allMistakes,
        _selectedReasonFilter,
        _selectedSubjectFilter
    ) { allMistakes, reasonFilter, subjectFilter ->
        val unresolved = allMistakes.filter { !it.isResolved }
        val resolved = allMistakes.filter { it.isResolved }

        val sillyCount = unresolved.count { it.errorReason == MistakeReason.SILLY_MISTAKE.name }
        val formulaCount = unresolved.count { it.errorReason == MistakeReason.FORMULA_FORGOT.name }
        val misreadCount = unresolved.count { it.errorReason == MistakeReason.MISREAD_QUESTION.name }
        val conceptCount = unresolved.count { it.errorReason == MistakeReason.CONCEPT_GAP.name }
        val untaggedCount = unresolved.count { it.errorReason == MistakeReason.UNTAGGED.name }

        val filtered = unresolved.filter { mistake ->
            val matchesReason = reasonFilter == null || mistake.errorReason == reasonFilter.name
            val matchesSubject = subjectFilter == "ALL" || mistake.subjectName.equals(subjectFilter, ignoreCase = true)
            matchesReason && matchesSubject
        }

        MistakeDiaryUiState(
            mistakes = allMistakes,
            filteredMistakes = filtered,
            totalMistakesCount = allMistakes.size,
            unresolvedCount = unresolved.size,
            resolvedCount = resolved.size,
            selectedReasonFilter = reasonFilter,
            selectedSubjectFilter = subjectFilter,
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

    init {
        // Pre-populate initial high-yield mistakes if Room table is empty on first launch
        viewModelScope.launch {
            repository.allMistakes.collect { list ->
                if (list.isEmpty()) {
                    seedDefaultMistakes()
                }
            }
        }
    }

    private suspend fun seedDefaultMistakes() {
        val defaultMistakes = listOf(
            MistakeEntity(
                id = "seed_genetics_1",
                questionId = "q_genetics_101",
                testTitle = "NEET 2024 Full Mock 1",
                questionText = "In Mendelian dihybrid cross (RrYy x RrYy), what fraction of F2 progeny are recombinant phenotypes?",
                selectedOption = "9/16",
                correctOption = "6/16 (Round Green + Wrinkled Yellow)",
                optionsJson = "9/16|||6/16 (Round Green + Wrinkled Yellow)|||1/16|||7/16",
                explanation = "Recombinant phenotypes are round green (3/16) and wrinkled yellow (3/16), yielding total 6/16.",
                subjectName = "Biology",
                topicName = "Principles of Inheritance",
                errorReason = MistakeReason.MISREAD_QUESTION.name,
                studentNotes = "Read recombinant as total phenotypic ratio by mistake! Remember 9+1 are parental, 3+3 are recombinant.",
                timestamp = System.currentTimeMillis() - 3600000
            ),
            MistakeEntity(
                id = "seed_physics_1",
                questionId = "q_physics_102",
                testTitle = "NEET 2023 Paper",
                questionText = "A body starts from rest with uniform acceleration 'a'. Ratio of distance traveled in the 5th second to total distance in 5 seconds is:",
                selectedOption = "1/5",
                correctOption = "9/25",
                optionsJson = "9/25|||1/5|||11/25|||1/25",
                explanation = "Distance in nth second is S_nth = u + a/2(2n-1). S_5th = a/2(9). Total distance in 5s S = 1/2 * a * (25). Ratio = 9/25.",
                subjectName = "Physics",
                topicName = "Motion in a Straight Line",
                errorReason = MistakeReason.FORMULA_FORGOT.name,
                studentNotes = "Forgot the n-th second distance formula u + a/2(2n-1)!",
                timestamp = System.currentTimeMillis() - 7200000
            ),
            MistakeEntity(
                id = "seed_chemistry_1",
                questionId = "q_chem_103",
                testTitle = "High Yield Chemistry Mock",
                questionText = "Which of the following compounds will undergo Cannizzaro reaction when treated with concentrated NaOH?",
                selectedOption = "Acetaldehyde (CH3CHO)",
                correctOption = "Benzaldehyde (C6H5CHO)",
                optionsJson = "Acetaldehyde (CH3CHO)|||Benzaldehyde (C6H5CHO)|||Acetone (CH3COCH3)|||Propionaldehyde (CH3CH2CHO)",
                explanation = "Cannizzaro reaction is given only by aldehydes having NO alpha-hydrogen atoms. Benzaldehyde has no alpha-H.",
                subjectName = "Chemistry",
                topicName = "Aldehydes & Ketones",
                errorReason = MistakeReason.CONCEPT_GAP.name,
                studentNotes = "Cannizzaro requires NO alpha-H. Aldol requires presence of alpha-H.",
                timestamp = System.currentTimeMillis() - 10800000
            )
        )
        repository.insertMistakes(defaultMistakes)
    }

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
