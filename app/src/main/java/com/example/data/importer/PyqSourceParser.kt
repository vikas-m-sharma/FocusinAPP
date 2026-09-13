package com.example.data.importer

import org.json.JSONArray
import org.json.JSONObject

/**
 * Robust parser for multiple incoming source formats:
 * - JSON (Canonical or relaxed formats)
 * - CSV (Structured spreadsheets)
 * - TXT / Structured Text (OCR extracts, numbered questions)
 * - Separate Answer Keys (Tables, lists, compact key pairs)
 */
object PyqSourceParser {

    /**
     * Parses an incoming source document into a list of raw questions.
     */
    fun parseDocument(document: PyqSourceDocument): List<PyqRawQuestion> {
        val rawQuestions = when (document.format) {
            SourceDocumentFormat.JSON -> parseJson(document.rawContent)
            SourceDocumentFormat.CSV -> parseCsv(document.rawContent)
            SourceDocumentFormat.TXT, SourceDocumentFormat.PDF_TEXT -> parseStructuredText(document.rawContent)
        }

        // If a separate answer key is provided, merge answers into raw questions
        if (!document.answerKeyContent.isNullOrBlank()) {
            val keyMap = parseSeparateAnswerKey(document.answerKeyContent)
            return rawQuestions.map { rawQ ->
                val itemNum = rawQ.rawItemNumber
                if (rawQ.rawAnswer.isNullOrBlank() && itemNum != null && keyMap.containsKey(itemNum)) {
                    rawQ.copy(rawAnswer = keyMap[itemNum])
                } else {
                    rawQ
                }
            }
        }

        return rawQuestions
    }

    /**
     * Parses JSON source text.
     */
    fun parseJson(jsonText: String): List<PyqRawQuestion> {
        val result = mutableListOf<PyqRawQuestion>()
        val trimmed = jsonText.trim()
        try {
            val questionsArray = if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                val root = JSONObject(trimmed)
                root.optJSONArray("questions") ?: JSONArray()
            }

            for (i in 0 until questionsArray.length()) {
                val obj = questionsArray.optJSONObject(i) ?: continue
                val qText = when {
                    obj.has("questionText") -> obj.getString("questionText")
                    obj.has("question") -> obj.getString("question")
                    obj.has("text") -> obj.getString("text")
                    obj.has("q") -> obj.getString("q")
                    else -> ""
                }.trim()

                val rawOptions = mutableListOf<String>()
                if (obj.has("options")) {
                    val arr = obj.optJSONArray("options")
                    if (arr != null) {
                        for (j in 0 until arr.length()) {
                            rawOptions.add(arr.optString(j, "").trim())
                        }
                    }
                } else if (obj.has("optionA")) {
                    rawOptions.add(obj.optString("optionA", "").trim())
                    rawOptions.add(obj.optString("optionB", "").trim())
                    rawOptions.add(obj.optString("optionC", "").trim())
                    rawOptions.add(obj.optString("optionD", "").trim())
                }

                val rawAnswer = when {
                    obj.has("correctOptionIndex") -> {
                        val idx = obj.getInt("correctOptionIndex")
                        if (idx in 0..3) listOf("A", "B", "C", "D")[idx] else null
                    }
                    obj.has("correctOption") -> obj.getString("correctOption").trim()
                    obj.has("answer") -> obj.getString("answer").trim()
                    obj.has("ans") -> obj.getString("ans").trim()
                    else -> null
                }

                val itemNum = when {
                    obj.has("originalQuestionNumber") -> obj.getInt("originalQuestionNumber")
                    obj.has("questionNumber") -> obj.getInt("questionNumber")
                    obj.has("number") -> obj.getInt("number")
                    obj.has("q_no") -> obj.getInt("q_no")
                    else -> i + 1
                }

                val subject = when {
                    obj.has("subjectId") -> obj.getString("subjectId")
                    obj.has("subject") -> obj.getString("subject")
                    else -> null
                }

                val chapter = when {
                    obj.has("chapterId") -> obj.getString("chapterId")
                    obj.has("chapter") -> obj.getString("chapter")
                    else -> null
                }

                val topic = when {
                    obj.has("topicName") -> obj.getString("topicName")
                    obj.has("topic") -> obj.getString("topic")
                    else -> null
                }

                result.add(
                    PyqRawQuestion(
                        rawItemNumber = itemNum,
                        rawQuestionText = qText,
                        rawOptions = rawOptions,
                        rawAnswer = rawAnswer,
                        rawSubject = subject,
                        rawChapter = chapter,
                        rawTopic = topic,
                        rawLocator = "JSON_INDEX_$i"
                    )
                )
            }
        } catch (e: Exception) {
            // If whole JSON fails, return empty list (will be reported in ConversionReport)
        }
        return result
    }

    /**
     * Parses CSV source text with support for quoted strings and flexible column headers.
     */
    fun parseCsv(csvText: String): List<PyqRawQuestion> {
        val result = mutableListOf<PyqRawQuestion>()
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return result

        val headerTokens = parseCsvLine(lines.first()).map { it.trim().lowercase() }
        val qIdx = headerTokens.indexOfFirst { it in listOf("question", "questiontext", "text", "q") }
        val optAIdx = headerTokens.indexOfFirst { it in listOf("optiona", "option a", "opt a", "a") }
        val optBIdx = headerTokens.indexOfFirst { it in listOf("optionb", "option b", "opt b", "b") }
        val optCIdx = headerTokens.indexOfFirst { it in listOf("optionc", "option c", "opt c", "c") }
        val optDIdx = headerTokens.indexOfFirst { it in listOf("optiond", "option d", "opt d", "d") }
        val ansIdx = headerTokens.indexOfFirst { it in listOf("answer", "ans", "correct", "correctoption", "key") }
        val numIdx = headerTokens.indexOfFirst { it in listOf("number", "qno", "q_no", "q num", "item", "#") }
        val subIdx = headerTokens.indexOfFirst { it in listOf("subject", "subjectid") }
        val chapIdx = headerTokens.indexOfFirst { it in listOf("chapter", "chapterid") }
        val topicIdx = headerTokens.indexOfFirst { it in listOf("topic", "topicname") }

        for (i in 1 until lines.size) {
            val tokens = parseCsvLine(lines[i])
            if (tokens.isEmpty()) continue

            val questionText = if (qIdx in tokens.indices) tokens[qIdx].trim() else ""
            if (questionText.isEmpty()) continue

            val options = mutableListOf<String>()
            if (optAIdx in tokens.indices) options.add(tokens[optAIdx].trim())
            if (optBIdx in tokens.indices) options.add(tokens[optBIdx].trim())
            if (optCIdx in tokens.indices) options.add(tokens[optCIdx].trim())
            if (optDIdx in tokens.indices) options.add(tokens[optDIdx].trim())

            val ans = if (ansIdx in tokens.indices) tokens[ansIdx].trim() else null
            val num = if (numIdx in tokens.indices) tokens[numIdx].trim().toIntOrNull() else i
            val subject = if (subIdx in tokens.indices) tokens[subIdx].trim() else null
            val chapter = if (chapIdx in tokens.indices) tokens[chapIdx].trim() else null
            val topic = if (topicIdx in tokens.indices) tokens[topicIdx].trim() else null

            result.add(
                PyqRawQuestion(
                    rawItemNumber = num ?: i,
                    rawQuestionText = questionText,
                    rawOptions = options,
                    rawAnswer = ans,
                    rawSubject = subject,
                    rawChapter = chapter,
                    rawTopic = topic,
                    rawLocator = "CSV_ROW_${i + 1}"
                )
            )
        }
        return result
    }

    /**
     * Parses structured or OCR text with numbered questions and option bullets.
     */
    fun parseStructuredText(rawText: String): List<PyqRawQuestion> {
        val result = mutableListOf<PyqRawQuestion>()
        val lines = rawText.lines()
        val questionStartRegex = Regex("""^(?:Q(?:uestion)?\.?\s*)?(\d+)[\.:\)]\s*(.*)""", RegexOption.IGNORE_CASE)
        val optionRegex = Regex("""^(?:\(([A-Da-d1-4])\)|([A-Da-d1-4])[\.:\)])\s*(.*)""")
        val answerRegex = Regex("""^(?:Ans(?:wer)?|Key)\s*[:=-]?\s*\(?([A-Da-d1-4])\)?""", RegexOption.IGNORE_CASE)

        var currentNumber: Int? = null
        val currentQTextLines = mutableListOf<String>()
        val currentOptions = mutableListOf<String>()
        var currentAnswer: String? = null
        var inOptions = false

        fun flushCurrent() {
            val qText = currentQTextLines.joinToString(" ").trim()
            if (qText.isNotBlank()) {
                result.add(
                    PyqRawQuestion(
                        rawItemNumber = currentNumber,
                        rawQuestionText = qText,
                        rawOptions = currentOptions.toList(),
                        rawAnswer = currentAnswer,
                        rawLocator = "TXT_ITEM_${currentNumber ?: (result.size + 1)}"
                    )
                )
            }
            currentNumber = null
            currentQTextLines.clear()
            currentOptions.clear()
            currentAnswer = null
            inOptions = false
        }

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            val qMatch = questionStartRegex.matchEntire(trimmed)
            if (qMatch != null) {
                flushCurrent()
                currentNumber = qMatch.groupValues[1].toIntOrNull()
                val inlineText = qMatch.groupValues[2].trim()
                if (inlineText.isNotEmpty()) {
                    currentQTextLines.add(inlineText)
                }
                inOptions = false
                continue
            }

            val ansMatch = answerRegex.find(trimmed)
            if (ansMatch != null) {
                currentAnswer = ansMatch.groupValues[1].uppercase()
                continue
            }

            val optMatch = optionRegex.matchEntire(trimmed)
            if (optMatch != null) {
                inOptions = true
                val optText = optMatch.groupValues[3].trim()
                currentOptions.add(optText)
                continue
            }

            // Continuation lines
            if (inOptions && currentOptions.isNotEmpty()) {
                val lastIdx = currentOptions.lastIndex
                currentOptions[lastIdx] = currentOptions[lastIdx] + " " + trimmed
            } else {
                currentQTextLines.add(trimmed)
            }
        }
        flushCurrent()

        return result
    }

    /**
     * Parses separate answer key files/strings:
     * Examples:
     * "1: B, 2: D, 3: A"
     * "1. (b)\n2. (d)\n3. (a)"
     * "1-4, 2-2, 3-1"
     */
    fun parseSeparateAnswerKey(keyText: String): Map<Int, String> {
        val map = mutableMapOf<Int, String>()
        val pairRegex = Regex("""(?:Q(?:uestion)?\.?\s*)?(\d+)[\s\.:\=-]+\(?([A-Da-d1-4])\)?""")

        for (match in pairRegex.findAll(keyText)) {
            val qNum = match.groupValues[1].toIntOrNull() ?: continue
            val ans = match.groupValues[2].uppercase()
            map[qNum] = ans
        }
        return map
    }

    /**
     * CSV line tokenizer with quote awareness.
     */
    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            if (ch == '\"') {
                inQuotes = !inQuotes
            } else if (ch == ',' && !inQuotes) {
                tokens.add(sb.toString())
                sb.setLength(0)
            } else {
                sb.append(ch)
            }
        }
        tokens.add(sb.toString())
        return tokens
    }
}
