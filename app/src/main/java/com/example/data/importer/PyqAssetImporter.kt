package com.example.data.importer

import android.content.Context
import android.util.Log
import com.example.data.local.dao.LearningDao
import com.example.data.local.entity.QuestionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

data class PyqImportReport(
    val totalProcessed: Int = 0,
    val importedCount: Int = 0,
    val skippedCount: Int = 0,
    val duplicateCount: Int = 0,
    val invalidCount: Int = 0,
    val unverifiedCount: Int = 0,
    val verifiedCount: Int = 0,
    val sampleCount: Int = 0,
    val unmappedChapterCount: Int = 0,
    val manifestDatasetsCount: Int = 0,
    val errors: List<String> = emptyList()
) {
    val isSuccess: Boolean get() = invalidCount == 0 && errors.isEmpty()

    fun toSummaryString(): String = buildString {
        appendLine("=== PYQ INGESTION SUMMARY ===")
        appendLine("Manifest Datasets Processed: $manifestDatasetsCount")
        appendLine("Total Questions Processed:   $totalProcessed")
        appendLine("Newly Imported into Room:    $importedCount")
        appendLine("Skipped (Already in DB):     $skippedCount")
        appendLine("Duplicate Questions:         $duplicateCount")
        appendLine("Invalid / Malformed:         $invalidCount")
        appendLine("Unmapped Chapters:           $unmappedChapterCount")
        appendLine("-----------------------------")
        appendLine("Verification Status:")
        appendLine("  • VERIFIED (Official Key): $verifiedCount")
        appendLine("  • UNVERIFIED (Pending):    $unverifiedCount")
        appendLine("  • SAMPLE / DEMO:           $sampleCount")
        if (errors.isNotEmpty()) {
            appendLine("-----------------------------")
            appendLine("Encountered Issues (${errors.size}):")
            errors.take(5).forEach { appendLine("  ! $it") }
            if (errors.size > 5) appendLine("  ...and ${errors.size - 5} more")
        }
        appendLine("=============================")
    }
}

/**
 * Hardened, offline-first ingestion framework for Historical AIPMT / NEET UG questions.
 *
 * Adheres strictly to docs/PYQ_DATASET_SPEC.md:
 * - Reads assets/pyq/manifest.json for dataset versioning & cataloging.
 * - Enforces zero synthetic question infiltration into historical datasets.
 * - Maps subjects and chapters strictly via PyqChapterMapper (never assigns random chapters).
 * - Enforces strict evidence requirements for VERIFIED questions.
 * - Extracts and stores historicalPaperId, originalQuestionNumber, and sourceReference.
 * - Completely idempotent: safe to run multiple times without duplicating records.
 */
class PyqAssetImporter(
    private val context: Context,
    private val learningDao: LearningDao
) {
    companion object {
        private const val TAG = "PyqAssetImporter"
        private const val ASSETS_PYQ_ROOT = "pyq"
        private const val MANIFEST_FILE_PATH = "pyq/manifest.json"

        val VALID_SOURCE_EXAMS = setOf("AIPMT", "NEET_UG", "NEET_RE", "SAMPLE")
        val VALID_SYLLABUS_STATUSES = setOf("CURRENT", "RATIONALIZED", "OUT_OF_CURRENT_SYLLABUS", "UNKNOWN")
        val VALID_VERIFICATION_STATUSES = setOf("VERIFIED", "UNVERIFIED", "SAMPLE", "GENERATED")
    }

    suspend fun importAllPyqAssets(): PyqImportReport = withContext(Dispatchers.IO) {
        var totalProcessed = 0
        var importedCount = 0
        var skippedCount = 0
        var duplicateCount = 0
        var invalidCount = 0
        var unverifiedCount = 0
        var verifiedCount = 0
        var sampleCount = 0
        var unmappedChapterCount = 0
        var manifestDatasetsCount = 0
        val errors = mutableListOf<String>()

        try {
            val assetManager = context.assets
            val existingIds = learningDao.getAllQuestionIds().toMutableSet()
            val seenIdsInSession = mutableSetOf<String>()
            val seenSignatures = mutableSetOf<String>()
            val questionsToInsert = mutableListOf<QuestionEntity>()

            // 1. Read manifest.json to discover registered datasets
            val targetFiles = mutableListOf<String>()
            try {
                val manifestStream = assetManager.open(MANIFEST_FILE_PATH)
                val manifestJson = manifestStream.bufferedReader().use { it.readText() }
                val manifestRoot = JSONObject(manifestJson)
                val datasetsArray = manifestRoot.optJSONArray("papers") ?: manifestRoot.optJSONArray("datasets") ?: JSONArray()
                manifestDatasetsCount = datasetsArray.length()

                for (i in 0 until datasetsArray.length()) {
                    val datasetObj = datasetsArray.getJSONObject(i)
                    val filePath = datasetObj.optString("file", "")
                    if (filePath.isNotBlank() && filePath != "null") {
                        targetFiles.add(filePath)
                    }
                }
                Log.d(TAG, "Manifest loaded: found ${targetFiles.size} actionable dataset files")
            } catch (e: Exception) {
                Log.w(TAG, "Manifest not found or failed to parse. Falling back to asset traversal: ${e.message}")
            }

            // Fallback: If manifest was empty or missing, discover JSON files directly
            if (targetFiles.isEmpty()) {
                targetFiles.addAll(discoverJsonFiles(ASSETS_PYQ_ROOT))
            }

            // Always exclude manifest.json itself from question processing
            val actionableFiles = targetFiles.filterNot { it.endsWith("manifest.json", ignoreCase = true) }

            for (path in actionableFiles) {
                try {
                    val jsonString = assetManager.open(path).use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).readText()
                    }
                    val jsonBatch = JSONObject(jsonString)

                    val batchExam = jsonBatch.optString("sourceExam", "NEET_UG").trim()
                    val batchYear = if (jsonBatch.has("examYear")) jsonBatch.getInt("examYear") else null
                    val batchSession = jsonBatch.optString("paperSession", "MAIN").trim()
                    val batchHistoricalPaperId = jsonBatch.optString("historicalPaperId", "").trim()

                    if (batchExam.isNotEmpty() && !VALID_SOURCE_EXAMS.contains(batchExam)) {
                        errors.add("Invalid sourceExam '$batchExam' in $path")
                        invalidCount++
                        continue
                    }

                    if (batchYear != null && (batchYear < 1990 || batchYear > 2030)) {
                        errors.add("Invalid examYear $batchYear in $path")
                        invalidCount++
                        continue
                    }

                    val questionsArray = jsonBatch.optJSONArray("questions") ?: JSONArray()
                    for (i in 0 until questionsArray.length()) {
                        totalProcessed++
                        val qObj = questionsArray.getJSONObject(i)

                        val id = qObj.optString("id", "").trim()
                        val rawSubject = qObj.optString("subjectId", "").trim()
                        val rawChapter = qObj.optString("chapterId", "").trim()
                        val rawTopic = qObj.optString("topicName", "").trim()
                        val questionText = qObj.optString("questionText", "").trim()
                        val explanation = qObj.optString("explanation", "").trim()
                        val difficulty = qObj.optString("difficulty", "MEDIUM").trim().uppercase()
                        val explicitSyllabus = qObj.optString("syllabusStatus", "").trim()
                        val correctIndex = qObj.optInt("correctOptionIndex", -1)

                        val qYear = if (qObj.has("examYear")) qObj.getInt("examYear") else batchYear
                        val qExam = if (qObj.has("sourceExam")) qObj.getString("sourceExam").trim() else batchExam
                        val qSession = if (qObj.has("paperSession")) qObj.getString("paperSession").trim() else batchSession
                        val qPaperId = if (qObj.has("historicalPaperId")) qObj.getString("historicalPaperId").trim()
                                       else if (batchHistoricalPaperId.isNotEmpty()) batchHistoricalPaperId else null
                        val qOrigNum = if (qObj.has("originalQuestionNumber")) qObj.getInt("originalQuestionNumber") else null

                        // 1. Validation: Mandatory string fields
                        if (id.isEmpty()) {
                            errors.add("Question at index $i in $path is missing required 'id'")
                            invalidCount++
                            continue
                        }
                        if (questionText.isEmpty()) {
                            errors.add("Question $id in $path is missing questionText")
                            invalidCount++
                            continue
                        }

                        // 2. Validation: Options
                        val optionsArr = qObj.optJSONArray("options")
                        if (optionsArr == null || optionsArr.length() != 4) {
                            errors.add("Question $id must have exactly 4 options (found ${optionsArr?.length() ?: 0})")
                            invalidCount++
                            continue
                        }

                        val optA = optionsArr.getString(0).trim()
                        val optB = optionsArr.getString(1).trim()
                        val optC = optionsArr.getString(2).trim()
                        val optD = optionsArr.getString(3).trim()

                        if (optA.isEmpty() || optB.isEmpty() || optC.isEmpty() || optD.isEmpty()) {
                            errors.add("Question $id contains empty option text")
                            invalidCount++
                            continue
                        }

                        // 3. Validation: Correct Option Index
                        if (correctIndex !in 0..3) {
                            errors.add("Question $id invalid correctOptionIndex $correctIndex (must be 0..3)")
                            invalidCount++
                            continue
                        }
                        val correctOptionLetter = when (correctIndex) {
                            0 -> "A"
                            1 -> "B"
                            2 -> "C"
                            3 -> "D"
                            else -> "A"
                        }

                        // 4. Validation: Chapter & Subject Mapping via PyqChapterMapper
                        val mappingResult = PyqChapterMapper.mapChapter(
                            rawSubject = rawSubject,
                            rawChapterId = rawChapter,
                            rawTopicName = rawTopic,
                            explicitSyllabusStatus = explicitSyllabus
                        )

                        val (canonicalSubject, canonicalChapter, canonicalTopic, finalSyllabusStatus) = when (mappingResult) {
                            is ChapterMappingResult.Mapped -> {
                                Quadruple(
                                    mappingResult.canonicalSubjectId,
                                    mappingResult.canonicalChapterId,
                                    mappingResult.defaultTopicName,
                                    mappingResult.syllabusStatus
                                )
                            }
                            is ChapterMappingResult.OutOfSyllabus -> {
                                Quadruple(
                                    mappingResult.canonicalSubjectId,
                                    mappingResult.chapterId,
                                    mappingResult.defaultTopicName,
                                    "OUT_OF_CURRENT_SYLLABUS"
                                )
                            }
                            is ChapterMappingResult.Ambiguous -> {
                                errors.add("Question $id ambiguous chapter: ${mappingResult.reason}")
                                unmappedChapterCount++
                                invalidCount++
                                continue
                            }
                            is ChapterMappingResult.ReviewRequired -> {
                                errors.add("Question $id chapter review required: ${mappingResult.reason}")
                                unmappedChapterCount++
                                invalidCount++
                                continue
                            }
                            is ChapterMappingResult.Unmapped -> {
                                errors.add("Question $id mapping failure: ${mappingResult.reason}")
                                unmappedChapterCount++
                                invalidCount++
                                continue // Reject question: never map to a random chapter
                            }
                        }

                        // 5. Validation: Duplicate Detection
                        if (seenIdsInSession.contains(id)) {
                            errors.add("Duplicate question ID in session: $id")
                            duplicateCount++
                            continue
                        }
                        seenIdsInSession.add(id)

                        if (qOrigNum != null && qYear != null) {
                            val signature = "$qExam-$qYear-$qSession-$qOrigNum"
                            if (seenSignatures.contains(signature)) {
                                errors.add("Duplicate question item number in paper: $signature (id: $id)")
                                duplicateCount++
                                continue
                            }
                            seenSignatures.add(signature)
                        }

                        // Idempotency: Skip if already present in Room database
                        if (existingIds.contains(id)) {
                            skippedCount++
                            continue
                        }

                        // 6. Source Reference & Verification Resolution
                        val rawSourceRef = when {
                            qObj.has("sourceReference") -> qObj.get("sourceReference").toString()
                            jsonBatch.has("sourceReference") -> jsonBatch.get("sourceReference").toString()
                            else -> null
                        }

                        val rawStatus = when {
                            qObj.has("sourceVerificationStatus") -> qObj.getString("sourceVerificationStatus").trim().uppercase()
                            jsonBatch.has("sourceVerificationStatus") -> jsonBatch.getString("sourceVerificationStatus").trim().uppercase()
                            path.contains("sample", ignoreCase = true) || qExam == "SAMPLE" -> "SAMPLE"
                            else -> "UNVERIFIED"
                        }

                        val hasPrimaryArtifact = (qObj.has("sourceArtifact") && qObj.getString("sourceArtifact").isNotBlank()) ||
                                                 (jsonBatch.has("sourceArtifact") && jsonBatch.getString("sourceArtifact").isNotBlank())
                        val hasSufficientSourceRef = rawSourceRef != null && rawSourceRef.isNotBlank() && rawSourceRef != "null"

                        var finalVerificationStatus = if (VALID_VERIFICATION_STATUSES.contains(rawStatus)) rawStatus else "UNVERIFIED"

                        // Strict Verification Rule: VERIFIED requires primary evidence or explicit sourceReference
                        if (finalVerificationStatus == "VERIFIED" && !hasPrimaryArtifact && !hasSufficientSourceRef) {
                            Log.w(TAG, "Question $id claimed VERIFIED but lacks primary source citation. Downgraded to UNVERIFIED.")
                            finalVerificationStatus = "UNVERIFIED"
                        }

                        when (finalVerificationStatus) {
                            "VERIFIED" -> verifiedCount++
                            "SAMPLE" -> sampleCount++
                            else -> unverifiedCount++
                        }

                        // Official status: ONLY verified historical questions can be true official past questions
                        val finalIsOfficial = (finalVerificationStatus == "VERIFIED")
                        val pyqYearLabel = if (qYear != null) "$qExam $qYear" else null

                        val entity = QuestionEntity(
                            id = id,
                            examId = "NEET",
                            subjectId = canonicalSubject,
                            chapterId = canonicalChapter,
                            topicName = canonicalTopic,
                            questionText = questionText,
                            optionA = optA,
                            optionB = optB,
                            optionC = optC,
                            optionD = optD,
                            correctOption = correctOptionLetter,
                            explanation = if (explanation.isNotEmpty()) explanation else "Standard solution.",
                            difficulty = if (difficulty in listOf("EASY", "MEDIUM", "HARD")) difficulty else "MEDIUM",
                            pyqYear = pyqYearLabel,
                            isOfficialPYQ = finalIsOfficial,
                            isBookmarked = false,
                            sourceExam = qExam,
                            examYear = qYear,
                            paperSession = qSession,
                            syllabusStatus = finalSyllabusStatus,
                            sourceVerificationStatus = finalVerificationStatus,
                            historicalPaperId = qPaperId,
                            originalQuestionNumber = qOrigNum,
                            sourceReference = rawSourceRef
                        )

                        questionsToInsert.add(entity)
                        existingIds.add(id)
                        importedCount++
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing PYQ file $path", e)
                    errors.add("Error parsing $path: ${e.localizedMessage}")
                    invalidCount++
                }
            }

            // Batch insert into Room using transactions of 100
            if (questionsToInsert.isNotEmpty()) {
                questionsToInsert.chunked(100).forEach { chunk ->
                    learningDao.insertQuestions(chunk)
                }
                Log.d(TAG, "Successfully inserted ${questionsToInsert.size} historical questions into Room")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed scanning pyq assets", e)
            errors.add("Asset scan failure: ${e.localizedMessage}")
        }

        PyqImportReport(
            totalProcessed = totalProcessed,
            importedCount = importedCount,
            skippedCount = skippedCount,
            duplicateCount = duplicateCount,
            invalidCount = invalidCount,
            unverifiedCount = unverifiedCount,
            verifiedCount = verifiedCount,
            sampleCount = sampleCount,
            unmappedChapterCount = unmappedChapterCount,
            manifestDatasetsCount = manifestDatasetsCount,
            errors = errors
        )
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    private fun discoverJsonFiles(path: String): List<String> {
        val result = mutableListOf<String>()
        try {
            val list = context.assets.list(path) ?: return emptyList()
            for (item in list) {
                val fullPath = if (path.isEmpty()) item else "$path/$item"
                if (item.endsWith(".json", ignoreCase = true)) {
                    result.add(fullPath)
                } else if (!item.contains(".")) {
                    // It's a directory, recurse
                    result.addAll(discoverJsonFiles(fullPath))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error traversing path $path", e)
        }
        return result
    }
}
