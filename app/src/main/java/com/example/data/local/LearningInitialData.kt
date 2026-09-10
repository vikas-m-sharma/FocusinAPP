package com.example.data.local

import com.example.data.local.dao.LearningDao
import com.example.data.local.entity.ChapterProgressEntity

object LearningInitialData {

    suspend fun populateInitialDataIfEmpty(learningDao: LearningDao) {
        val existingProgress = learningDao.getChapterProgressById("neet_phy_current_electricity")
        if (existingProgress != null) return

        val initialProgressList = listOf(
            ChapterProgressEntity(
                chapterId = "neet_phy_current_electricity",
                subjectName = "Physics",
                chapterName = "Current Electricity",
                completedTopicsJson = "[\"Ohm's Law\", \"Resistance\"]",
                totalTopicsCount = 8,
                lastStudiedTimestamp = System.currentTimeMillis()
            ),
            ChapterProgressEntity(
                chapterId = "neet_chem_thermodynamics",
                subjectName = "Chemistry",
                chapterName = "Thermodynamics",
                completedTopicsJson = "[\"First Law\"]",
                totalTopicsCount = 6,
                lastStudiedTimestamp = System.currentTimeMillis()
            ),
            ChapterProgressEntity(
                chapterId = "neet_bio_human_physio",
                subjectName = "Biology",
                chapterName = "Human Physiology",
                completedTopicsJson = "[\"Digestion\"]",
                totalTopicsCount = 10,
                lastStudiedTimestamp = System.currentTimeMillis()
            )
        )

        for (progress in initialProgressList) {
            learningDao.insertOrUpdateChapterProgress(progress)
        }
    }
}
