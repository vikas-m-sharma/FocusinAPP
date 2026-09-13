package com.example.data.importer

import com.example.data.model.neetBiologyChapters
import com.example.data.model.neetChemistryChapters
import com.example.data.model.neetPhysicsChapters

enum class ChapterMappingStatus {
    MAPPED,
    UNMAPPED,
    AMBIGUOUS,
    OUT_OF_SYLLABUS,
    REVIEW_REQUIRED
}

sealed class ChapterMappingResult {
    data class Mapped(
        val canonicalSubjectId: String,
        val canonicalChapterId: String,
        val chapterName: String,
        val defaultTopicName: String,
        val syllabusStatus: String,
        val mappingStatus: ChapterMappingStatus = ChapterMappingStatus.MAPPED
    ) : ChapterMappingResult()

    data class OutOfSyllabus(
        val canonicalSubjectId: String,
        val chapterId: String,
        val chapterName: String,
        val defaultTopicName: String,
        val reason: String,
        val mappingStatus: ChapterMappingStatus = ChapterMappingStatus.OUT_OF_SYLLABUS
    ) : ChapterMappingResult()

    data class Ambiguous(
        val rawSubject: String,
        val rawChapterId: String,
        val possibleMatches: List<String>,
        val reason: String,
        val mappingStatus: ChapterMappingStatus = ChapterMappingStatus.AMBIGUOUS
    ) : ChapterMappingResult()

    data class ReviewRequired(
        val rawSubject: String,
        val rawChapterId: String,
        val rawTopicName: String,
        val reason: String,
        val mappingStatus: ChapterMappingStatus = ChapterMappingStatus.REVIEW_REQUIRED
    ) : ChapterMappingResult()

    data class Unmapped(
        val rawSubject: String,
        val rawChapterId: String,
        val rawTopicName: String,
        val reason: String,
        val mappingStatus: ChapterMappingStatus = ChapterMappingStatus.UNMAPPED
    ) : ChapterMappingResult()
}

/**
 * Centralized mapping and validation layer connecting raw historical question
 * metadata to the canonical FOCUSIN academic curriculum.
 *
 * Rule: Unmapped questions must NEVER be silently assigned to random chapters.
 * If mapping fails, it returns ChapterMappingResult.Unmapped to be tracked in the import report.
 */
object PyqChapterMapper {

    private val allCanonicalChapters by lazy {
        (neetPhysicsChapters + neetChemistryChapters + neetBiologyChapters).associateBy { it.id }
    }

    // Known alias dictionary for historical or legacy dataset IDs
    private val chapterAliasMap: Map<String, String> = mapOf(
        // Physics Aliases
        "neet_physics_current_electricity" to "neet_phy_current_electricity",
        "current_electricity" to "neet_phy_current_electricity",
        "neet_phy_current" to "neet_phy_current_electricity",
        "neet_physics_kinematics" to "neet_phy_kinematics",
        "kinematics" to "neet_phy_kinematics",
        "laws_of_motion" to "neet_phy_laws_of_motion",
        "neet_physics_laws_of_motion" to "neet_phy_laws_of_motion",
        "work_energy_power" to "neet_phy_work_energy_power",
        "electrostatics" to "neet_phy_electrostatics",
        "magnetism" to "neet_phy_magnetism",
        "optics" to "neet_phy_optics",
        "modern_physics" to "neet_phy_modern_physics",

        // Chemistry Aliases
        "some_basic_concepts" to "neet_chem_some_basic_concepts",
        "mole_concept" to "neet_chem_some_basic_concepts",
        "thermodynamics" to "neet_chem_thermodynamics",
        "chemical_thermodynamics" to "neet_chem_thermodynamics",
        "equilibrium" to "neet_chem_equilibrium",
        "chemical_equilibrium" to "neet_chem_equilibrium",
        "organic_basics" to "neet_chem_organic_basics",
        "goc" to "neet_chem_organic_basics",
        "hydrocarbons" to "neet_chem_hydrocarbons",
        "coordination" to "neet_chem_coordination",
        "coordination_compounds" to "neet_chem_coordination",

        // Biology Aliases
        "cell_cycle" to "neet_bio_cell_cycle",
        "cell_biology" to "neet_bio_cell_cycle",
        "genetics" to "neet_bio_genetics",
        "molecular_basis_of_inheritance" to "neet_bio_genetics",
        "human_physio" to "neet_bio_human_physio",
        "human_physiology" to "neet_bio_human_physio",
        "plant_physio" to "neet_bio_plant_physio",
        "plant_physiology" to "neet_bio_plant_physio",
        "reproduction" to "neet_bio_reproduction",
        "human_reproduction" to "neet_bio_reproduction"
    )

    // Known rationalized / historical topics no longer in the core NCERT syllabus
    private val rationalizedChapters: Set<String> = setOf(
        "neet_chem_solid_state",
        "neet_chem_surface_chemistry",
        "neet_chem_hydrogen",
        "neet_chem_p_block_elements",
        "neet_bio_digestion_absorption",
        "neet_phy_communication_systems"
    )

    /**
     * Canonicalizes raw subject text to "PHYSICS", "CHEMISTRY", or "BIOLOGY".
     */
    fun canonicalizeSubject(rawSubject: String): String? {
        val s = rawSubject.trim().uppercase()
        return when {
            s.contains("PHYSIC") || s == "PHY" -> "PHYSICS"
            s.contains("CHEM") -> "CHEMISTRY"
            s.contains("BIO") || s.contains("BOTANY") || s.contains("ZOOLOGY") -> "BIOLOGY"
            else -> null
        }
    }

    /**
     * Maps and validates a question's subject and chapter ID against the academic catalog.
     */
    fun mapChapter(
        rawSubject: String,
        rawChapterId: String,
        rawTopicName: String = "",
        explicitSyllabusStatus: String? = null
    ): ChapterMappingResult {
        val canonicalSubject = canonicalizeSubject(rawSubject)
            ?: return ChapterMappingResult.Unmapped(
                rawSubject = rawSubject,
                rawChapterId = rawChapterId,
                rawTopicName = rawTopicName,
                reason = "Unrecognized subject identifier '$rawSubject'"
            )

        val normalizedInputId = rawChapterId.trim().lowercase()

        if (normalizedInputId.isEmpty()) {
            return ChapterMappingResult.ReviewRequired(
                rawSubject = rawSubject,
                rawChapterId = rawChapterId,
                rawTopicName = rawTopicName,
                reason = "Chapter ID is blank. Never guess a chapter merely to increase coverage. Review required."
            )
        }

        // 1. Direct hit in active catalog
        val directChapter = allCanonicalChapters[normalizedInputId]
        if (directChapter != null) {
            val status = explicitSyllabusStatus?.uppercase()?.takeIf {
                it in setOf("CURRENT", "RATIONALIZED", "OUT_OF_CURRENT_SYLLABUS", "UNKNOWN")
            } ?: "CURRENT"

            return ChapterMappingResult.Mapped(
                canonicalSubjectId = canonicalSubject,
                canonicalChapterId = directChapter.id,
                chapterName = directChapter.name,
                defaultTopicName = if (rawTopicName.isNotBlank()) rawTopicName else directChapter.topics.firstOrNull() ?: "General",
                syllabusStatus = status
            )
        }

        // 2. Alias resolution
        val aliasedId = chapterAliasMap[normalizedInputId]
        if (aliasedId != null) {
            val chapter = allCanonicalChapters[aliasedId]
            if (chapter != null) {
                val status = explicitSyllabusStatus?.uppercase()?.takeIf {
                    it in setOf("CURRENT", "RATIONALIZED", "OUT_OF_CURRENT_SYLLABUS", "UNKNOWN")
                } ?: "CURRENT"

                return ChapterMappingResult.Mapped(
                    canonicalSubjectId = canonicalSubject,
                    canonicalChapterId = chapter.id,
                    chapterName = chapter.name,
                    defaultTopicName = if (rawTopicName.isNotBlank()) rawTopicName else chapter.topics.firstOrNull() ?: "General",
                    syllabusStatus = status
                )
            }
        }

        // 3. Rationalized historical topic (Out of current syllabus)
        if (rationalizedChapters.contains(normalizedInputId)) {
            return ChapterMappingResult.OutOfSyllabus(
                canonicalSubjectId = canonicalSubject,
                chapterId = normalizedInputId,
                chapterName = normalizedInputId.replace("neet_", "").replace("_", " ").capitalizeWords(),
                defaultTopicName = if (rawTopicName.isNotBlank()) rawTopicName else "Historical Rationalized Topic",
                reason = "Topic was rationalized / removed from modern NEET NCERT syllabus."
            )
        }

        // 4. Ambiguity check across curriculum
        val candidateChapters = allCanonicalChapters.values.filter {
            it.subjectName.equals(canonicalSubject, ignoreCase = true) &&
            (it.name.contains(normalizedInputId, ignoreCase = true) || it.id.contains(normalizedInputId, ignoreCase = true))
        }
        if (candidateChapters.size > 1) {
            return ChapterMappingResult.Ambiguous(
                rawSubject = rawSubject,
                rawChapterId = rawChapterId,
                possibleMatches = candidateChapters.map { it.id },
                reason = "Ambiguous chapter: matches multiple candidates ${candidateChapters.map { it.id }}. Never guess. Review required."
            )
        } else if (candidateChapters.size == 1) {
            val single = candidateChapters.first()
            return ChapterMappingResult.Mapped(
                canonicalSubjectId = canonicalSubject,
                canonicalChapterId = single.id,
                chapterName = single.name,
                defaultTopicName = if (rawTopicName.isNotBlank()) rawTopicName else single.topics.firstOrNull() ?: "General",
                syllabusStatus = "CURRENT"
            )
        }

        // 4. Chapter cannot be resolved
        return ChapterMappingResult.Unmapped(
            rawSubject = rawSubject,
            rawChapterId = rawChapterId,
            rawTopicName = rawTopicName,
            reason = "Chapter ID '$rawChapterId' does not match any FOCUSIN curriculum chapter or known alias"
        )
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}
