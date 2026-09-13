package com.example.data.importer

import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

/**
 * Source types defining the provenance of historical question material.
 * In accordance with Phase 5 guidelines: only legitimate, permitted sources are allowed.
 */
enum class PyqSourceType(val label: String) {
    USER_PROVIDED("User Provided Dataset"),
    OFFICIAL_PUBLIC("Official Public Examination Archive"),
    LICENSED_DATASET("Properly Licensed Dataset"),
    PUBLIC_DOMAIN("Public Domain / Educational Permitted"),
    SAMPLE_DEMO("Sample / Demonstration Only")
}

/**
 * Supported source document formats for user/developer ingestion.
 */
enum class SourceDocumentFormat {
    JSON,
    CSV,
    TXT,
    PDF_TEXT
}

/**
 * Duplicate classification as required by Phase 5.9 duplicate detection.
 */
enum class PyqDuplicateType {
    EXACT_DUPLICATE,
    NEAR_DUPLICATE,
    SAME_QUESTION_DIFFERENT_SET,
    POSSIBLE_DUPLICATE
}

/**
 * Detailed duplicate record for audit and review.
 */
data class PyqDuplicateRecord(
    val duplicateType: PyqDuplicateType,
    val questionId: String,
    val matchedSignatureOrId: String,
    val similarityScore: Float,
    val details: String
)

/**
 * Single paper entry in assets/pyq/manifest.json conforming to Phase 5.10.
 */
data class PaperManifestEntry(
    val id: String,
    val exam: String,
    val year: Int,
    val session: String = "MAIN",
    val sourceType: String = "OFFICIAL_PUBLIC",
    val sourceName: String = "",
    val sourceLocator: String? = null,
    val primaryEvidenceUri: String? = null,
    val sha256: String? = null,
    val questionCount: Int = 0,
    val physicsCount: Int = 0,
    val chemistryCount: Int = 0,
    val biologyCount: Int = 0,
    val answerKeyAvailable: Boolean = false,
    val verificationStatus: String = "NOT_IMPORTED", // "VERIFIED", "PARTIAL", "UNVERIFIED", "NOT_IMPORTED"
    val file: String? = null
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("exam", exam)
        obj.put("year", year)
        obj.put("session", session)
        obj.put("sourceType", sourceType)
        obj.put("sourceName", sourceName)
        sourceLocator?.let { obj.put("sourceLocator", it) }
        primaryEvidenceUri?.let { obj.put("primaryEvidenceUri", it) }
        sha256?.let { obj.put("sha256", it) }
        obj.put("questionCount", questionCount)
        obj.put("physicsCount", physicsCount)
        obj.put("chemistryCount", chemistryCount)
        obj.put("biologyCount", biologyCount)
        obj.put("answerKeyAvailable", answerKeyAvailable)
        obj.put("verificationStatus", verificationStatus)
        file?.let { obj.put("file", it) }
        return obj
    }
}

/**
 * Root manifest model conforming to Phase 5.10.
 */
data class DatasetManifestRoot(
    val datasetVersion: String = "2.0",
    val generatedAt: String = "2026-09-13",
    val sourceCount: Int = 0,
    val paperCount: Int = 0,
    val questionCount: Int = 0,
    val papers: List<PaperManifestEntry> = emptyList()
) {
    fun toJsonString(): String {
        val obj = JSONObject()
        obj.put("datasetVersion", datasetVersion)
        obj.put("generatedAt", generatedAt)
        obj.put("sourceCount", sourceCount)
        obj.put("paperCount", paperCount)
        obj.put("questionCount", questionCount)
        val arr = JSONArray()
        papers.forEach { arr.put(it.toJsonObject()) }
        obj.put("papers", arr)
        return obj.toString(2)
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): DatasetManifestRoot {
            val papersArr = obj.optJSONArray("papers") ?: obj.optJSONArray("datasets") ?: JSONArray()
            val list = mutableListOf<PaperManifestEntry>()
            for (i in 0 until papersArr.length()) {
                val p = papersArr.getJSONObject(i)
                list.add(
                    PaperManifestEntry(
                        id = p.optString("id", p.optString("datasetId", "")),
                        exam = p.optString("exam", ""),
                        year = p.optInt("year", 0),
                        session = p.optString("session", "REGULAR"),
                        sourceType = p.optString("sourceType", "OFFICIAL_PUBLIC"),
                        sourceName = p.optString("sourceName", ""),
                        sourceLocator = p.optString("sourceLocator").takeIf { it.isNotBlank() && it != "null" },
                        primaryEvidenceUri = p.optString("primaryEvidenceUri").takeIf { it.isNotBlank() && it != "null" },
                        sha256 = p.optString("sha256").takeIf { it.isNotBlank() && it != "null" },
                        questionCount = p.optInt("questionCount", p.optInt("totalQuestions", 0)),
                        physicsCount = p.optInt("physicsCount", 0),
                        chemistryCount = p.optInt("chemistryCount", 0),
                        biologyCount = p.optInt("biologyCount", 0),
                        answerKeyAvailable = p.optBoolean("answerKeyAvailable", true),
                        verificationStatus = p.optString("verificationStatus", "VERIFIED"),
                        file = p.optString("file").takeIf { it.isNotBlank() && it != "null" }
                    )
                )
            }
            return DatasetManifestRoot(
                datasetVersion = obj.optString("datasetVersion", obj.optString("version", "2.0")),
                generatedAt = obj.optString("generatedAt", "2026-09-13"),
                sourceCount = obj.optInt("sourceCount", list.size),
                paperCount = obj.optInt("paperCount", list.size),
                questionCount = obj.optInt("questionCount", list.sumOf { it.questionCount }),
                papers = list
            )
        }
    }
}

/**
 * Source provenance metadata attached to every historical question and batch.
 */
data class PyqSourceMetadata(
    val sourceType: String = PyqSourceType.USER_PROVIDED.name,
    val sourceName: String,
    val sourceLocator: String? = null,
    val sourceDocumentId: String? = null,
    val primaryEvidenceUri: String? = null,
    val contributorNotes: String? = null
) {
    fun toJsonString(): String {
        val obj = JSONObject()
        obj.put("sourceType", sourceType)
        obj.put("sourceName", sourceName)
        sourceLocator?.let { obj.put("sourceLocator", it) }
        sourceDocumentId?.let { obj.put("sourceDocumentId", it) }
        primaryEvidenceUri?.let { obj.put("primaryEvidenceUri", it) }
        contributorNotes?.let { obj.put("contributorNotes", it) }
        return obj.toString()
    }

    companion object {
        fun fromJsonString(jsonStr: String?): PyqSourceMetadata {
            if (jsonStr.isNullOrBlank()) return PyqSourceMetadata(sourceName = "Unknown Source")
            return try {
                val obj = JSONObject(jsonStr)
                PyqSourceMetadata(
                    sourceType = obj.optString("sourceType", PyqSourceType.USER_PROVIDED.name),
                    sourceName = obj.optString("sourceName", "Unknown Source"),
                    sourceLocator = if (obj.has("sourceLocator")) obj.getString("sourceLocator") else null,
                    sourceDocumentId = if (obj.has("sourceDocumentId")) obj.getString("sourceDocumentId") else null,
                    primaryEvidenceUri = if (obj.has("primaryEvidenceUri")) obj.getString("primaryEvidenceUri") else null,
                    contributorNotes = if (obj.has("contributorNotes")) obj.getString("contributorNotes") else null
                )
            } catch (e: Exception) {
                PyqSourceMetadata(sourceName = jsonStr)
            }
        }
    }
}

/**
 * Encapsulates an incoming raw source file/document to be processed through the conversion pipeline.
 */
data class PyqSourceDocument(
    val documentId: String,
    val format: SourceDocumentFormat,
    val rawContent: String,
    val metadata: PyqSourceMetadata,
    val targetExam: String = "NEET_UG", // "AIPMT" or "NEET_UG"
    val targetYear: Int? = null,
    val targetSession: String = "MAIN", // "MAIN", "PRELIMS", "PHASE_1", etc.
    val answerKeyContent: String? = null
) {
    val contentChecksum: String by lazy {
        val bytes = rawContent.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        digest.joinToString("") { "%02x".format(it) }
    }
}

/**
 * Raw extracted question from parser before normalization and mapping.
 */
data class PyqRawQuestion(
    val rawItemNumber: Int? = null,
    val rawQuestionText: String,
    val rawOptions: List<String> = emptyList(),
    val rawAnswer: String? = null,
    val rawSubject: String? = null,
    val rawChapter: String? = null,
    val rawTopic: String? = null,
    val rawLocator: String? = null,
    val rawDifficulty: String? = null,
    val extractionConfidence: Float = 1.0f
)

/**
 * Cleaned, mapped question conforming to FOCUSIN academic standards.
 */
data class PyqNormalizedQuestion(
    val id: String,
    val questionNumber: Int?,
    val questionText: String,
    val options: List<String>, // Exactly 4 options
    val correctOptionIndex: Int, // 0..3 or -1 if missing
    val subjectId: String, // "PHYSICS", "CHEMISTRY", "BIOLOGY", or "UNKNOWN"
    val chapterId: String, // Canonical chapter id or "UNMAPPED"
    val chapterName: String,
    val topicName: String,
    val syllabusStatus: String, // "CURRENT", "RATIONALIZED", "OUT_OF_CURRENT_SYLLABUS", "UNKNOWN"
    val sourceExam: String,
    val examYear: Int?,
    val paperSession: String,
    val historicalPaperId: String?,
    val sourceReference: PyqSourceMetadata,
    val verificationStatus: String, // "VERIFIED", "UNVERIFIED", "SAMPLE", "GENERATED"
    val explanation: String = "Standard solution.",
    val difficulty: String = "MEDIUM",
    val duplicateWarning: String? = null,
    val duplicateType: PyqDuplicateType? = null,
    val duplicateRecord: PyqDuplicateRecord? = null,
    val needsHumanReview: Boolean = false,
    val reviewNotes: List<String> = emptyList()
) {
    val isVerified: Boolean get() = verificationStatus == "VERIFIED"
    val isMissingAnswer: Boolean get() = correctOptionIndex !in 0..3
    val isUnmappedChapter: Boolean get() = chapterId == "UNMAPPED" || chapterId.isBlank()
    val isUnmappedSubject: Boolean get() = subjectId !in listOf("PHYSICS", "CHEMISTRY", "BIOLOGY")

    fun toCanonicalJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("subjectId", subjectId)
        obj.put("chapterId", chapterId)
        obj.put("topicName", topicName)
        obj.put("questionText", questionText)
        val opts = org.json.JSONArray()
        options.forEach { opts.put(it) }
        obj.put("options", opts)
        obj.put("correctOptionIndex", correctOptionIndex)
        obj.put("explanation", explanation)
        obj.put("difficulty", difficulty)
        obj.put("syllabusStatus", syllabusStatus)
        obj.put("sourceExam", sourceExam)
        examYear?.let { obj.put("examYear", it) }
        obj.put("paperSession", paperSession)
        obj.put("sourceVerificationStatus", verificationStatus)
        historicalPaperId?.let { obj.put("historicalPaperId", it) }
        questionNumber?.let { obj.put("originalQuestionNumber", it) }
        obj.put("sourceReference", sourceReference.toJsonString())
        obj.put("isOfficialPYQ", isVerified)
        return obj
    }
}

/**
 * Comprehensive conversion report summarizing every step of dataset processing.
 */
data class PyqConversionReport(
    val sourceDocumentId: String,
    val sourceFileName: String,
    val exam: String,
    val year: Int?,
    val totalRawQuestions: Int,
    val parsedQuestions: Int,
    val validQuestions: Int,
    val invalidQuestions: Int,
    val missingAnswers: Int,
    val unmappedSubjects: Int,
    val unmappedChapters: Int,
    val duplicates: Int,
    val duplicateRecords: List<PyqDuplicateRecord> = emptyList(),
    val verifiedCount: Int,
    val unverifiedCount: Int,
    val rejectedCount: Int,
    val reviewQueueCount: Int,
    val checksum: String,
    val logs: List<String> = emptyList(),
    val canonicalJsonPreview: String? = null
) {
    val isClean: Boolean get() = invalidQuestions == 0 && missingAnswers == 0 && unmappedChapters == 0 && duplicates == 0

    fun toHumanSummary(): String = buildString {
        appendLine("==================================================")
        appendLine("       PYQ SOURCE CONVERSION AUDIT REPORT         ")
        appendLine("==================================================")
        appendLine("Document ID:          $sourceDocumentId")
        appendLine("Source File/Name:     $sourceFileName")
        appendLine("Target Exam & Year:   $exam ${year ?: "N/A"}")
        appendLine("SHA-256 Checksum:     $checksum")
        appendLine("--------------------------------------------------")
        appendLine("Raw Questions:        $totalRawQuestions")
        appendLine("Parsed Successfully:  $parsedQuestions")
        appendLine("Valid Questions:      $validQuestions")
        appendLine("Invalid / Malformed:  $invalidQuestions")
        appendLine("Missing Answer Keys:  $missingAnswers")
        appendLine("Unmapped Subjects:    $unmappedSubjects")
        appendLine("Unmapped Chapters:    $unmappedChapters")
        appendLine("Detected Duplicates:  $duplicates")
        appendLine("--------------------------------------------------")
        appendLine("Verification Status:")
        appendLine("  • VERIFIED (Approved with Evidence): $verifiedCount")
        appendLine("  • UNVERIFIED (Needs Review/Pending): $unverifiedCount")
        appendLine("  • REJECTED:                          $rejectedCount")
        appendLine("Pending in Review Queue:               $reviewQueueCount")
        if (logs.isNotEmpty()) {
            appendLine("--------------------------------------------------")
            appendLine("Processing Logs (${logs.size}):")
            logs.take(12).forEach { appendLine("  > $it") }
            if (logs.size > 12) {
                appendLine("  ...and ${logs.size - 12} more log entries")
            }
        }
        appendLine("==================================================")
        appendLine("Overall Pipeline Status: ${if (isClean) "READY FOR REVIEW / IMPORT" else "ISSUES DETECTED - SENT TO REVIEW QUEUE"}")
        appendLine("==================================================")
    }
}
