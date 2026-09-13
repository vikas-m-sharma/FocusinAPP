package com.example.data.importer

import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/**
 * End-to-end Conversion and Normalization Pipeline for Historical AIPMT / NEET UG questions.
 *
 * Enforces:
 * - Parsing from JSON, CSV, TXT, and separate Answer Keys.
 * - Exact 4-option requirement.
 * - Subject and chapter mapping via canonical PyqChapterMapper.
 * - Zero random chapter assignment (unmapped questions go to review queue with chapterId="UNMAPPED").
 * - Answer key resolution without guessing or inventing answers.
 * - Stable natural key and text similarity duplicate detection.
 * - Strict verification rule: Questions can NEVER be marked VERIFIED without primary evidence.
 */
object PyqConversionPipeline {

    fun convert(
        document: PyqSourceDocument,
        existingQuestionSignatures: Set<String> = emptySet(),
        existingQuestionTextFingerprints: Set<String> = emptySet()
    ): Pair<List<PyqNormalizedQuestion>, PyqConversionReport> {
        val logs = mutableListOf<String>()
        logs.add("Starting ingestion pipeline for document: ${document.documentId} (${document.format})")
        logs.add("Target: ${document.targetExam} Year: ${document.targetYear ?: "Unspecified"} Session: ${document.targetSession}")
        logs.add("Source Provenance: ${document.metadata.sourceName} (${document.metadata.sourceType})")

        // 1. Parsing
        val rawQuestions = try {
            PyqSourceParser.parseDocument(document)
        } catch (e: Exception) {
            logs.add("FATAL: Parser threw exception: ${e.localizedMessage}")
            emptyList()
        }
        logs.add("Parsed ${rawQuestions.size} raw question blocks from source.")

        val normalizedList = mutableListOf<PyqNormalizedQuestion>()
        var validCount = 0
        var invalidCount = 0
        var missingAnswers = 0
        var unmappedSubjects = 0
        var unmappedChapters = 0
        var duplicates = 0
        val duplicateRecordsList = mutableListOf<PyqDuplicateRecord>()
        var verifiedCount = 0
        var unverifiedCount = 0
        var rejectedCount = 0
        var reviewQueueCount = 0

        val seenBatchSignatures = mutableSetOf<String>()
        val seenBatchFingerprints = mutableSetOf<String>()

        val exam = document.targetExam.trim().uppercase()
        val year = document.targetYear
        val session = document.targetSession.trim().uppercase()

        for ((index, rawQ) in rawQuestions.withIndex()) {
            val reviewNotes = mutableListOf<String>()
            var needsReview = false
            val itemNumber = rawQ.rawItemNumber ?: (index + 1)

            // Clean question text
            val qText = rawQ.rawQuestionText.trim()
            if (qText.isEmpty()) {
                invalidCount++
                rejectedCount++
                logs.add("Item #$itemNumber rejected: Empty question text.")
                continue
            }

            // Normalization of Options
            val rawOpts = rawQ.rawOptions
            val cleanOptions: List<String>
            if (rawOpts.size == 4 && rawOpts.all { it.isNotBlank() }) {
                cleanOptions = rawOpts.map { it.trim() }
            } else if (rawOpts.size > 4) {
                // Take first 4 if extra empty, otherwise flag
                cleanOptions = rawOpts.take(4).map { it.trim() }
                reviewNotes.add("Source had ${rawOpts.size} options; truncated to first 4.")
                needsReview = true
            } else {
                cleanOptions = (rawOpts + listOf("", "", "", "")).take(4).map { it.trim() }
                reviewNotes.add("Malformed options: Expected 4 non-empty options, found ${rawOpts.size}.")
                needsReview = true
                invalidCount++
            }

            // Normalization of Answer Key
            val correctIdx = parseAnswerIndex(rawQ.rawAnswer)
            if (correctIdx !in 0..3) {
                missingAnswers++
                reviewNotes.add("Missing or ambiguous answer key: '${rawQ.rawAnswer ?: "None"}'. Marked UNVERIFIED.")
                needsReview = true
            }

            // Subject Mapping
            val rawSub = rawQ.rawSubject ?: ""
            val canonicalSubject = PyqChapterMapper.canonicalizeSubject(rawSub)
            val finalSubject: String
            if (canonicalSubject != null) {
                finalSubject = canonicalSubject
            } else {
                // If subject is blank, cannot infer randomly
                finalSubject = "UNKNOWN"
                unmappedSubjects++
                reviewNotes.add("Unrecognized subject '${rawQ.rawSubject}'. Subject set to UNKNOWN.")
                needsReview = true
            }

            // Chapter Mapping via PyqChapterMapper
            val rawChap = rawQ.rawChapter ?: ""
            val rawTop = rawQ.rawTopic ?: ""
            val mappingResult = PyqChapterMapper.mapChapter(
                rawSubject = if (finalSubject != "UNKNOWN") finalSubject else "PHYSICS",
                rawChapterId = rawChap,
                rawTopicName = rawTop
            )

            val (finalChapterId, finalChapterName, finalTopicName, syllabusStatus) = when (mappingResult) {
                is ChapterMappingResult.Mapped -> {
                    Quadruple(
                        mappingResult.canonicalChapterId,
                        mappingResult.chapterName,
                        mappingResult.defaultTopicName,
                        mappingResult.syllabusStatus
                    )
                }
                is ChapterMappingResult.OutOfSyllabus -> {
                    Quadruple(
                        mappingResult.chapterId,
                        mappingResult.chapterName,
                        mappingResult.defaultTopicName,
                        "OUT_OF_CURRENT_SYLLABUS"
                    )
                }
                is ChapterMappingResult.Ambiguous -> {
                    unmappedChapters++
                    reviewNotes.add("Ambiguous chapter: ${mappingResult.reason}. Assigned 'UNMAPPED'.")
                    needsReview = true
                    Quadruple(
                        "UNMAPPED",
                        "Ambiguous Historical Chapter",
                        if (rawTop.isNotBlank()) rawTop else "General",
                        "UNKNOWN"
                    )
                }
                is ChapterMappingResult.ReviewRequired -> {
                    unmappedChapters++
                    reviewNotes.add("Chapter review required: ${mappingResult.reason}. Assigned 'UNMAPPED'.")
                    needsReview = true
                    Quadruple(
                        "UNMAPPED",
                        "Chapter Review Required",
                        if (rawTop.isNotBlank()) rawTop else "General",
                        "UNKNOWN"
                    )
                }
                is ChapterMappingResult.Unmapped -> {
                    unmappedChapters++
                    reviewNotes.add("Unmapped chapter: ${mappingResult.reason}. Assigned 'UNMAPPED'.")
                    needsReview = true
                    Quadruple(
                        "UNMAPPED",
                        "Unmapped Historical Topic",
                        if (rawTop.isNotBlank()) rawTop else "General",
                        "UNKNOWN"
                    )
                }
            }

            // Duplicate Detection (Phase 5.9: EXACT_DUPLICATE, NEAR_DUPLICATE, SAME_QUESTION_DIFFERENT_SET, POSSIBLE_DUPLICATE)
            val naturalKey = "$exam-${year ?: "ANY"}-$session-$itemNumber"
            val textFingerprint = generateFingerprint(qText)
            var dupWarning: String? = null
            var detectedDupType: PyqDuplicateType? = null
            var detectedDupRecord: PyqDuplicateRecord? = null

            val questionId = "${exam}_${year ?: "HIST"}_${session}_Q${itemNumber.toString().padStart(3, '0')}"

            if (seenBatchSignatures.contains(naturalKey) || existingQuestionSignatures.contains(naturalKey)) {
                duplicates++
                detectedDupType = PyqDuplicateType.EXACT_DUPLICATE
                dupWarning = "EXACT_DUPLICATE: Identical exam, year, session, and question number ($naturalKey)."
                reviewNotes.add(dupWarning)
                needsReview = true
                detectedDupRecord = PyqDuplicateRecord(
                    duplicateType = PyqDuplicateType.EXACT_DUPLICATE,
                    questionId = questionId,
                    matchedSignatureOrId = naturalKey,
                    similarityScore = 1.0f,
                    details = dupWarning
                )
                duplicateRecordsList.add(detectedDupRecord)
            } else {
                seenBatchSignatures.add(naturalKey)
            }

            if (detectedDupType == null) {
                if (seenBatchFingerprints.contains(textFingerprint) || existingQuestionTextFingerprints.contains(textFingerprint)) {
                    duplicates++
                    detectedDupType = PyqDuplicateType.NEAR_DUPLICATE
                    dupWarning = "NEAR_DUPLICATE: Normalized question text matches another question fingerprint."
                    reviewNotes.add(dupWarning)
                    needsReview = true
                    detectedDupRecord = PyqDuplicateRecord(
                        duplicateType = PyqDuplicateType.NEAR_DUPLICATE,
                        questionId = questionId,
                        matchedSignatureOrId = textFingerprint.take(24),
                        similarityScore = 0.95f,
                        details = dupWarning
                    )
                    duplicateRecordsList.add(detectedDupRecord)
                } else {
                    val matchingFingerprint = seenBatchFingerprints.find { isHighSimilarity(it, textFingerprint) }
                    if (matchingFingerprint != null) {
                        duplicates++
                        val isDifferentSet = session != "MAIN"
                        detectedDupType = if (isDifferentSet) PyqDuplicateType.SAME_QUESTION_DIFFERENT_SET else PyqDuplicateType.POSSIBLE_DUPLICATE
                        dupWarning = "$detectedDupType: High similarity text match across sets/batches."
                        reviewNotes.add(dupWarning)
                        needsReview = true
                        detectedDupRecord = PyqDuplicateRecord(
                            duplicateType = detectedDupType,
                            questionId = questionId,
                            matchedSignatureOrId = matchingFingerprint.take(24),
                            similarityScore = 0.85f,
                            details = dupWarning
                        )
                        duplicateRecordsList.add(detectedDupRecord)
                    }
                    seenBatchFingerprints.add(textFingerprint)
                }
            }

            // Verification Rule Enforcement
            // A question can only be VERIFIED if accompanied by primary evidence (official key or scan reference)
            val hasPrimaryEvidence = !document.metadata.primaryEvidenceUri.isNullOrBlank() ||
                    document.metadata.sourceType == PyqSourceType.OFFICIAL_PUBLIC.name
            val hasCleanAnswer = correctIdx in 0..3
            val isCleanCurriculum = finalSubject != "UNKNOWN" && finalChapterId != "UNMAPPED"

            val verificationStatus = when {
                document.metadata.sourceType == PyqSourceType.SAMPLE_DEMO.name -> "SAMPLE"
                hasPrimaryEvidence && hasCleanAnswer && isCleanCurriculum && !needsReview -> {
                    verifiedCount++
                    "VERIFIED"
                }
                else -> {
                    unverifiedCount++
                    if (hasPrimaryEvidence && !hasCleanAnswer) {
                        reviewNotes.add("Primary evidence provided but answer key is missing/ambiguous.")
                    } else if (!hasPrimaryEvidence) {
                        reviewNotes.add("Lacks official primary answer key citation. Marked UNVERIFIED.")
                    }
                    "UNVERIFIED"
                }
            }

            val normalized = PyqNormalizedQuestion(
                id = questionId,
                questionNumber = itemNumber,
                questionText = qText,
                options = cleanOptions,
                correctOptionIndex = correctIdx,
                subjectId = finalSubject,
                chapterId = finalChapterId,
                chapterName = finalChapterName,
                topicName = finalTopicName,
                syllabusStatus = syllabusStatus,
                sourceExam = exam,
                examYear = year,
                paperSession = session,
                historicalPaperId = "${exam}_${year ?: "HIST"}_$session",
                sourceReference = document.metadata.copy(
                    sourceLocator = rawQ.rawLocator ?: "Item_$itemNumber"
                ),
                verificationStatus = verificationStatus,
                duplicateWarning = dupWarning,
                duplicateType = detectedDupType,
                duplicateRecord = detectedDupRecord,
                needsHumanReview = needsReview,
                reviewNotes = reviewNotes
            )

            if (needsReview) {
                reviewQueueCount++
            } else {
                validCount++
            }

            normalizedList.add(normalized)
        }

        logs.add("Normalization completed: ${normalizedList.size} questions produced.")
        logs.add("Valid: $validCount, Needs Review: $reviewQueueCount, Missing Answers: $missingAnswers, Unmapped Chapters: $unmappedChapters")

        // Build canonical JSON preview
        val canonicalJson = buildCanonicalJson(exam, year, session, normalizedList)

        val report = PyqConversionReport(
            sourceDocumentId = document.documentId,
            sourceFileName = document.metadata.sourceName,
            exam = exam,
            year = year,
            totalRawQuestions = rawQuestions.size,
            parsedQuestions = normalizedList.size,
            validQuestions = validCount,
            invalidQuestions = invalidCount,
            missingAnswers = missingAnswers,
            unmappedSubjects = unmappedSubjects,
            unmappedChapters = unmappedChapters,
            duplicates = duplicates,
            duplicateRecords = duplicateRecordsList,
            verifiedCount = verifiedCount,
            unverifiedCount = unverifiedCount,
            rejectedCount = rejectedCount,
            reviewQueueCount = reviewQueueCount,
            checksum = document.contentChecksum,
            logs = logs,
            canonicalJsonPreview = canonicalJson
        )

        return Pair(normalizedList, report)
    }

    /**
     * Maps raw answer token (A, B, C, D, 1, 2, 3, 4, etc.) to 0..3 index.
     */
    fun parseAnswerIndex(raw: String?): Int {
        if (raw.isNullOrBlank()) return -1
        val clean = raw.trim().uppercase(Locale.ROOT)
            .replace("(", "")
            .replace(")", "")
            .replace(".", "")
            .replace("OPTION", "")
            .trim()

        return when (clean) {
            "A", "1" -> 0
            "B", "2" -> 1
            "C", "3" -> 2
            "D", "4" -> 3
            else -> -1
        }
    }

    /**
     * Calculates 3-gram Jaccard similarity between two normalized strings.
     */
    fun calculateTextSimilarity(s1: String, s2: String): Float {
        if (s1 == s2) return 1.0f
        if (s1.isEmpty() || s2.isEmpty()) return 0.0f
        val grams1 = s1.chunked(3).toSet()
        val grams2 = s2.chunked(3).toSet()
        val intersection = grams1.intersect(grams2).size
        val union = grams1.union(grams2).size
        return if (union > 0) intersection.toFloat() / union else 0.0f
    }

    /**
     * Checks whether two normalized strings have similarity above a threshold.
     */
    fun isHighSimilarity(s1: String, s2: String, threshold: Float = 0.82f): Boolean {
        return calculateTextSimilarity(s1, s2) >= threshold
    }

    /**
     * Generates normalized alphanumeric fingerprint for text similarity / duplicate detection.
     */
    fun generateFingerprint(text: String): String {
        return text.lowercase(Locale.ROOT)
            .replace(Regex("""[^a-z0-9]"""), "")
            .take(120)
    }

    private fun buildCanonicalJson(
        exam: String,
        year: Int?,
        session: String,
        questions: List<PyqNormalizedQuestion>
    ): String {
        val root = JSONObject()
        root.put("sourceExam", exam)
        year?.let { root.put("examYear", it) }
        root.put("paperSession", session)
        val arr = JSONArray()
        questions.forEach { arr.put(it.toCanonicalJsonObject()) }
        root.put("questions", arr)
        return root.toString(2)
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
