package com.example.data.model

/**
 * Error reasons a student can tag in their Mistake Diary
 */
enum class MistakeReason(val label: String, val emoji: String, val colorHex: Long) {
    SILLY_MISTAKE("Silly Calculation Mistake", "🟡", 0xFFF59E0B),
    FORMULA_FORGOT("Formula bhool gaya", "🔴", 0xFFEF4444),
    MISREAD_QUESTION("Question dhyan se nahi padha", "🟣", 0xFFA855F7),
    CONCEPT_GAP("Concept clear nahi tha", "🔵", 0xFF3B82F6),
    UNTAGGED("Not tagged yet", "⚪", 0xFF64748B)
}
