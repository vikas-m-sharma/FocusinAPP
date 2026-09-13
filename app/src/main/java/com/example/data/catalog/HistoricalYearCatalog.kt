package com.example.data.catalog

/**
 * Authoritative Historical Exam Catalog for AIPMT (2005-2012, 2014, 2015)
 * and NEET UG (2013, 2016-2025).
 *
 * This catalog represents metadata only. It NEVER generates or fabricates questions.
 * Actual question counts must be queried directly from the local Room database.
 */
enum class DatasetImportStatus {
    NOT_IMPORTED,
    PARTIAL,
    IMPORTED_UNVERIFIED,
    VERIFIED
}

data class HistoricalExamYearInfo(
    val exam: String,
    val year: Int,
    val paperSession: String,
    val expectedTotal: Int,
    val expectedPhysics: Int,
    val expectedChemistry: Int,
    val expectedBiology: Int,
    val era: String,
    val notes: String = ""
)

data class YearCoverageReport(
    val info: HistoricalExamYearInfo,
    val actualImported: Int = 0,
    val actualVerified: Int = 0,
    val actualUnverified: Int = 0,
    val actualSample: Int = 0,
    val actualPhysics: Int = 0,
    val actualChemistry: Int = 0,
    val actualBiology: Int = 0,
    val status: DatasetImportStatus = DatasetImportStatus.NOT_IMPORTED
) {
    val missingCount: Int
        get() = maxOf(0, info.expectedTotal - actualImported)

    val coveragePercent: Float
        get() = if (info.expectedTotal > 0) (actualImported.toFloat() / info.expectedTotal) * 100f else 0f

    val isCompleteVerified: Boolean
        get() = actualVerified >= info.expectedTotal && actualImported >= info.expectedTotal
}

object HistoricalYearCatalog {

    /**
     * Canonical 21-year historical cycle from 2005 to 2025.
     * Expected numbers are based on the official test booklet formats of each era:
     * - AIPMT 2005-2012: 200 Questions (50 Physics, 50 Chemistry, 100 Biology)
     * - NEET 2013, AIPMT 2014, AIPMT 2015, NEET 2016-2020: 180 Questions (45 Physics, 45 Chemistry, 90 Biology)
     * - NEET 2021-2025: 200 Questions with Section B options (50 Physics, 50 Chemistry, 100 Biology)
     */
    val ALL_YEARS: List<HistoricalExamYearInfo> = listOf(
        // AIPMT ERA (2005 - 2012)
        HistoricalExamYearInfo("AIPMT", 2005, "PRELIMS", 200, 50, 50, 100, "AIPMT (CBSE)", "CBSE AIPMT Preliminary Examination"),
        HistoricalExamYearInfo("AIPMT", 2006, "PRELIMS", 200, 50, 50, 100, "AIPMT (CBSE)", "CBSE AIPMT Preliminary Examination"),
        HistoricalExamYearInfo("AIPMT", 2007, "PRELIMS", 200, 50, 50, 100, "AIPMT (CBSE)", "CBSE AIPMT Preliminary Examination"),
        HistoricalExamYearInfo("AIPMT", 2008, "PRELIMS", 200, 50, 50, 100, "AIPMT (CBSE)", "CBSE AIPMT Preliminary Examination"),
        HistoricalExamYearInfo("AIPMT", 2009, "PRELIMS", 200, 50, 50, 100, "AIPMT (CBSE)", "CBSE AIPMT Preliminary Examination"),
        HistoricalExamYearInfo("AIPMT", 2010, "PRELIMS", 200, 50, 50, 100, "AIPMT (CBSE)", "CBSE AIPMT Preliminary Examination"),
        HistoricalExamYearInfo("AIPMT", 2011, "PRELIMS", 200, 50, 50, 100, "AIPMT (CBSE)", "CBSE AIPMT Preliminary Examination"),
        HistoricalExamYearInfo("AIPMT", 2012, "PRELIMS", 200, 50, 50, 100, "AIPMT (CBSE)", "CBSE AIPMT Preliminary Examination"),

        // TRANSITION & NEET ERA
        HistoricalExamYearInfo("NEET_UG", 2013, "MAIN", 180, 45, 45, 90, "NEET (MCI/CBSE)", "Inaugural National Eligibility cum Entrance Test"),
        HistoricalExamYearInfo("AIPMT", 2014, "MAIN", 180, 45, 45, 90, "AIPMT (CBSE)", "AIPMT Reinstated by Supreme Court"),
        HistoricalExamYearInfo("AIPMT", 2015, "MAIN", 180, 45, 45, 90, "AIPMT / Re-AIPMT", "CBSE Re-AIPMT after paper cancellation"),

        // MODERN NEET ERA (2016 - 2025)
        HistoricalExamYearInfo("NEET_UG", 2016, "PHASE_1", 180, 45, 45, 90, "NEET (CBSE)", "NEET Phase 1 & Phase 2 combined era"),
        HistoricalExamYearInfo("NEET_UG", 2017, "MAIN", 180, 45, 45, 90, "NEET (CBSE)", "Centralized single national test"),
        HistoricalExamYearInfo("NEET_UG", 2018, "MAIN", 180, 45, 45, 90, "NEET (CBSE)", "Final exam conducted by CBSE"),
        HistoricalExamYearInfo("NEET_UG", 2019, "MAIN", 180, 45, 45, 90, "NEET (NTA)", "First edition conducted by National Testing Agency"),
        HistoricalExamYearInfo("NEET_UG", 2020, "MAIN", 180, 45, 45, 90, "NEET (NTA)", "Conducted during COVID-19 pandemic"),
        HistoricalExamYearInfo("NEET_UG", 2021, "MAIN", 200, 50, 50, 100, "NEET (NTA)", "Introduction of Section A & Section B choice format"),
        HistoricalExamYearInfo("NEET_UG", 2022, "MAIN", 200, 50, 50, 100, "NEET (NTA)", "Standardized 200-minute time allowance"),
        HistoricalExamYearInfo("NEET_UG", 2023, "MAIN", 200, 50, 50, 100, "NEET (NTA)", "National & Manipur special session"),
        HistoricalExamYearInfo("NEET_UG", 2024, "MAIN", 200, 50, 50, 100, "NEET (NTA)", "Conducted across 571 cities"),
        HistoricalExamYearInfo("NEET_UG", 2025, "MAIN", 200, 50, 50, 100, "NEET (NTA)", "Current examination cycle")
    )

    fun getYearInfo(year: Int): HistoricalExamYearInfo? =
        ALL_YEARS.find { it.year == year }

    fun getAipmtYears(): List<HistoricalExamYearInfo> =
        ALL_YEARS.filter { it.exam == "AIPMT" }

    fun getNeetYears(): List<HistoricalExamYearInfo> =
        ALL_YEARS.filter { it.exam == "NEET_UG" }

    fun isKnownYear(year: Int): Boolean =
        ALL_YEARS.any { it.year == year }
}
