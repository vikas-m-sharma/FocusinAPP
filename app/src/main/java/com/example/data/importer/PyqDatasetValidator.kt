package com.example.data.importer

import org.json.JSONArray
import org.json.JSONObject

data class PyqValidationReport(
    val totalQuestions: Int = 0,
    val validCount: Int = 0,
    val invalidCount: Int = 0,
    val duplicateCount: Int = 0,
    val missingAnswerCount: Int = 0,
    val invalidChapterCount: Int = 0,
    val invalidSubjectCount: Int = 0,
    val invalidExamYearCount: Int = 0,
    val verifiedCount: Int = 0,
    val unverifiedCount: Int = 0,
    val sampleCount: Int = 0,
    val unmappedChapters: List<String> = emptyList(),
    val errors: List<String> = emptyList()
) {
    val isClean: Boolean get() = invalidCount == 0 && duplicateCount == 0 && errors.isEmpty()

    fun generateHumanReadableSummary(): String = buildString {
        appendLine("==================================================")
        appendLine("           PYQ DATASET VALIDATION REPORT          ")
        appendLine("==================================================")
        appendLine("Total Questions Processed: $totalQuestions")
        appendLine("Valid Questions:           $validCount")
        appendLine("Invalid Questions:         $invalidCount")
        appendLine("Duplicates Found:          $duplicateCount")
        appendLine("Missing/Invalid Answers:   $missingAnswerCount")
        appendLine("Invalid Subject IDs:       $invalidSubjectCount")
        appendLine("Invalid Chapter IDs:       $invalidChapterCount")
        appendLine("Invalid Exam Years:        $invalidExamYearCount")
        appendLine("--------------------------------------------------")
        appendLine("Verification Breakdown:")
        appendLine("  • VERIFIED (Official):   $verifiedCount")
        appendLine("  • UNVERIFIED (Pending):  $unverifiedCount")
        appendLine("  • SAMPLE / DEMO:         $sampleCount")
        if (unmappedChapters.isNotEmpty()) {
            appendLine("--------------------------------------------------")
            appendLine("Unmapped Chapters (${unmappedChapters.size}):")
            unmappedChapters.distinct().take(10).forEach { appendLine("  - $it") }
            if (unmappedChapters.distinct().size > 10) {
                appendLine("  ...and ${unmappedChapters.distinct().size - 10} more")
            }
        }
        if (errors.isNotEmpty()) {
            appendLine("--------------------------------------------------")
            appendLine("Sample Errors (${errors.size}):")
            errors.take(10).forEach { appendLine("  ! $it") }
            if (errors.size > 10) {
                appendLine("  ...and ${errors.size - 10} more errors")
            }
        }
        appendLine("==================================================")
        appendLine("Integrity Status: ${if (isClean) "PASSED (Clean Dataset)" else "FAILED (${errors.size} issues found)"}")
        appendLine("==================================================")
    }
}

/**
 * Developer and ingestion validation utility to verify that any incoming PYQ dataset
 * conforms strictly to docs/PYQ_DATASET_SPEC.md.
 */
object PyqDatasetValidator {

    private val VALID_SOURCE_EXAMS = setOf("AIPMT", "NEET_UG", "NEET_RE", "SAMPLE")

    fun validateDataset(jsonString: String, datasetName: String = "Dataset"): PyqValidationReport {
        val errors = mutableListOf<String>()
        val unmappedChapters = mutableListOf<String>()

        var total = 0
        var valid = 0
        var invalid = 0
        var duplicates = 0
        var missingAnswers = 0
        var invalidChapters = 0
        var invalidSubjects = 0
        var invalidYears = 0
        var verified = 0
        var unverified = 0
        var sample = 0

        val seenIds = mutableSetOf<String>()
        val seenSignatures = mutableSetOf<String>()

        try {
            val root = JSONObject(jsonString)
            val batchExam = root.optString("sourceExam", "").trim()
            val batchYear = if (root.has("examYear")) root.getInt("examYear") else null
            val batchSession = root.optString("paperSession", "MAIN").trim()

            if (batchExam.isNotEmpty() && !VALID_SOURCE_EXAMS.contains(batchExam)) {
                errors.add("[$datasetName] Batch has invalid sourceExam '$batchExam'")
            }

            if (batchYear != null && (batchYear < 2005 || batchYear > 2025)) {
                errors.add("[$datasetName] Batch has examYear outside 2005-2025 range: $batchYear")
                invalidYears++
            }

            val questionsArray = root.optJSONArray("questions") ?: JSONArray()
            total = questionsArray.length()

            for (i in 0 until questionsArray.length()) {
                val qObj = questionsArray.optJSONObject(i)
                if (qObj == null) {
                    errors.add("[$datasetName] Question at index $i is not a JSON object")
                    invalid++
                    continue
                }

                val id = qObj.optString("id", "").trim()
                val questionText = qObj.optString("questionText", "").trim()
                val rawSubject = qObj.optString("subjectId", "").trim()
                val rawChapter = qObj.optString("chapterId", "").trim()
                val rawTopic = qObj.optString("topicName", "").trim()
                val correctIndex = qObj.optInt("correctOptionIndex", -1)
                val qYear = if (qObj.has("examYear")) qObj.getInt("examYear") else batchYear
                val qExam = if (qObj.has("sourceExam")) qObj.getString("sourceExam").trim() else batchExam
                val qSession = if (qObj.has("paperSession")) qObj.getString("paperSession").trim() else batchSession
                val qOrigNum = if (qObj.has("originalQuestionNumber")) qObj.getInt("originalQuestionNumber") else null

                var isItemValid = true

                // ID Check
                if (id.isEmpty()) {
                    errors.add("[$datasetName] Question at index $i is missing required 'id'")
                    isItemValid = false
                } else if (seenIds.contains(id)) {
                    errors.add("[$datasetName] Duplicate question id '$id'")
                    duplicates++
                    isItemValid = false
                } else {
                    seenIds.add(id)
                }

                // Natural key duplicate check
                if (qOrigNum != null && qYear != null) {
                    val signature = "$qExam-$qYear-$qSession-$qOrigNum"
                    if (seenSignatures.contains(signature)) {
                        errors.add("[$datasetName] Duplicate question number in exam: $signature (question id: $id)")
                        duplicates++
                        isItemValid = false
                    } else {
                        seenSignatures.add(signature)
                    }
                }

                // Question Text Check
                if (questionText.isEmpty()) {
                    errors.add("[$datasetName] Question '$id' is missing questionText")
                    isItemValid = false
                }

                // Options Check
                val optionsArr = qObj.optJSONArray("options")
                if (optionsArr == null || optionsArr.length() != 4) {
                    errors.add("[$datasetName] Question '$id' must have exactly 4 options")
                    isItemValid = false
                } else {
                    for (optIdx in 0 until 4) {
                        if (optionsArr.optString(optIdx, "").trim().isEmpty()) {
                            errors.add("[$datasetName] Question '$id' option index $optIdx is empty")
                            isItemValid = false
                            break
                        }
                    }
                }

                // Answer Check
                if (correctIndex !in 0..3) {
                    errors.add("[$datasetName] Question '$id' missing or invalid correctOptionIndex ($correctIndex)")
                    missingAnswers++
                    isItemValid = false
                }

                // Year Check
                if (qYear == null || qYear !in 2005..2025) {
                    errors.add("[$datasetName] Question '$id' exam year outside supported range 2005-2025: $qYear")
                    invalidYears++
                    isItemValid = false
                }

                // Subject / Chapter Mapping Check
                when (val mapping = PyqChapterMapper.mapChapter(rawSubject, rawChapter, rawTopic)) {
                    is ChapterMappingResult.Mapped -> {
                        // Successfully mapped to canonical curriculum
                    }
                    is ChapterMappingResult.Unmapped -> {
                        errors.add("[$datasetName] Question '$id' unmapped chapter: ${mapping.reason}")
                        unmappedChapters.add(rawChapter)
                        invalidChapters++
                        isItemValid = false
                    }
                    else -> {
                        // Other mapping results like OutOfSyllabus, Ambiguous, ReviewRequired
                    }
                }

                // Subject Validation
                if (PyqChapterMapper.canonicalizeSubject(rawSubject) == null) {
                    invalidSubjects++
                }

                // Verification Rules Check
                val rawStatus = qObj.optString("sourceVerificationStatus", "UNVERIFIED").trim().uppercase()
                when (rawStatus) {
                    "VERIFIED" -> {
                        val hasRefObj = qObj.has("sourceReference") && qObj.optJSONObject("sourceReference") != null
                        val hasArtifact = qObj.has("sourceArtifact") && qObj.optString("sourceArtifact").isNotBlank()
                        if (!hasRefObj && !hasArtifact) {
                            errors.add("[$datasetName] Question '$id' claims VERIFIED but lacks sourceReference or sourceArtifact")
                            unverified++
                        } else {
                            verified++
                        }
                    }
                    "SAMPLE" -> sample++
                    else -> unverified++
                }

                if (isItemValid) {
                    valid++
                } else {
                    invalid++
                }
            }

        } catch (e: Exception) {
            errors.add("[$datasetName] JSON Syntax/Parsing Error: ${e.localizedMessage}")
            invalid++
        }

        return PyqValidationReport(
            totalQuestions = total,
            validCount = valid,
            invalidCount = invalid,
            duplicateCount = duplicates,
            missingAnswerCount = missingAnswers,
            invalidChapterCount = invalidChapters,
            invalidSubjectCount = invalidSubjects,
            invalidExamYearCount = invalidYears,
            verifiedCount = verified,
            unverifiedCount = unverified,
            sampleCount = sample,
            unmappedChapters = unmappedChapters,
            errors = errors
        )
    }
}
