package com.example.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ImportedQuestionEntity
import com.example.data.local.entity.ImportedTestEntity
import com.example.data.repository.FocusinRepository
import com.example.util.ExtractionProgress
import com.example.util.ExtractedQuestionDraft
import com.example.util.GeminiTestExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ImportTestUiState(
    val isProcessing: Boolean = false,
    val progress: ExtractionProgress? = null,
    val isReviewReady: Boolean = false,
    val testTitle: String = "Imported NEET Test",
    val durationMinutes: Int = 200,
    val positiveMarks: Float = 4.0f,
    val negativeMarks: Float = 1.0f,
    val questions: List<ExtractedQuestionDraft> = emptyList(),
    val errorMessage: String? = null,
    val createdTestId: String? = null
)

class ImportTestViewModel(
    private val repository: FocusinRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportTestUiState())
    val uiState: StateFlow<ImportTestUiState> = _uiState.asStateFlow()

    private val extractor = GeminiTestExtractor(context)

    fun startExtraction(
        pdfUri: Uri?,
        photoUris: List<Uri>,
        defaultTitle: String = "Imported Coaching Test"
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                isReviewReady = false,
                testTitle = defaultTitle,
                errorMessage = null,
                progress = ExtractionProgress("READING", 10, "Starting document parser...")
            )

            try {
                val extracted = extractor.extractQuestionsFromDocument(
                    pdfUri = pdfUri,
                    photoUris = photoUris
                ) { p ->
                    _uiState.value = _uiState.value.copy(progress = p)
                }

                // Automatically calculate recommended duration (~1 min per question or 200 mins for 180 questions)
                val duration = if (extracted.size >= 150) 200 else (extracted.size * 1.1).toInt().coerceAtLeast(15)

                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    isReviewReady = true,
                    questions = extracted,
                    durationMinutes = duration
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = e.message ?: "Failed to process document"
                )
            }
        }
    }

    fun updateTestTitle(title: String) {
        _uiState.value = _uiState.value.copy(testTitle = title)
    }

    fun updateDuration(minutes: Int) {
        _uiState.value = _uiState.value.copy(durationMinutes = minutes.coerceAtLeast(5))
    }

    fun updateMarkingScheme(positive: Float, negative: Float) {
        _uiState.value = _uiState.value.copy(positiveMarks = positive, negativeMarks = negative)
    }

    fun updateQuestion(index: Int, updated: ExtractedQuestionDraft) {
        val currentList = _uiState.value.questions.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = updated.copy(answerSource = "USER_REVIEWED", confidence = 1.0f)
            _uiState.value = _uiState.value.copy(questions = currentList)
        }
    }

    fun createAndSaveTest(onSuccess: (String) -> Unit) {
        val state = _uiState.value
        val questions = state.questions
        if (questions.isEmpty()) return

        viewModelScope.launch {
            val testId = "imp_" + UUID.randomUUID().toString().take(8)
            val importedTest = ImportedTestEntity(
                testId = testId,
                title = state.testTitle.ifBlank { "Imported NEET Test" },
                sourceType = "IMPORTED",
                exam = "NEET (UG)",
                durationMinutes = state.durationMinutes,
                totalQuestions = questions.size,
                positiveMarks = state.positiveMarks,
                negativeMarks = state.negativeMarks,
                createdAt = System.currentTimeMillis(),
                status = "NOT_ATTEMPTED",
                maxScore = (questions.size * state.positiveMarks).toInt()
            )

            val questionEntities = questions.mapIndexed { idx, q ->
                ImportedQuestionEntity(
                    id = "${testId}_q${idx + 1}",
                    testId = testId,
                    questionNumber = idx + 1,
                    questionText = q.questionText,
                    optionA = q.optionA,
                    optionB = q.optionB,
                    optionC = q.optionC,
                    optionD = q.optionD,
                    subject = q.subject,
                    chapter = q.chapter,
                    topic = q.topic,
                    correctAnswer = q.correctAnswer,
                    answerSource = q.answerSource,
                    confidence = q.confidence,
                    explanation = q.explanation
                )
            }

            repository.insertImportedTest(importedTest)
            repository.insertImportedQuestions(questionEntities)

            _uiState.value = _uiState.value.copy(createdTestId = testId)
            onSuccess(testId)
        }
    }

    fun reset() {
        _uiState.value = ImportTestUiState()
    }
}
