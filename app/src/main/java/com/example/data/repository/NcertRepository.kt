package com.example.data.repository

import com.example.data.local.dao.NcertDao
import com.example.data.local.entity.NcertProgressEntity
import com.example.data.model.NcertBook
import com.example.data.model.NcertCatalog
import com.example.data.model.NcertClass
import com.example.data.model.NcertChapter
import com.example.data.model.NcertSearchResult
import com.example.data.model.NcertSubject
import kotlinx.coroutines.flow.Flow

/**
 * Repository providing a data-driven contract for NCERT textbooks, chapters,
 * reading state, bookmarks, and search.
 *
 * Designed to work 100% offline out-of-the-box, with built-in capability
 * to receive dynamic catalog sync from remote Firestore without UI disruption.
 */
class NcertRepository(
    private val ncertDao: NcertDao
) {

    // =========================================================================
    // CATALOG ACCESS (Data-Driven)
    // =========================================================================

    fun getClasses(): List<NcertClass> {
        return NcertCatalog.classes.sortedBy { it.displayOrder }
    }

    fun getClass(classNumber: Int): NcertClass? {
        return NcertCatalog.classes.firstOrNull { it.classNumber == classNumber }
    }

    fun getSubjectsForClass(classNumber: Int): List<NcertSubject> {
        return NcertCatalog.subjects
            .filter { it.classNumber == classNumber }
            .sortedBy { it.displayOrder }
    }

    fun getSubject(subjectId: String): NcertSubject? {
        return NcertCatalog.subjects.firstOrNull { it.id == subjectId }
    }

    fun getBooksForSubject(subjectId: String): List<NcertBook> {
        return NcertCatalog.books
            .filter { it.subjectId == subjectId }
            .sortedBy { it.displayOrder }
    }

    fun getBook(bookId: String): NcertBook? {
        return NcertCatalog.books.firstOrNull { it.id == bookId }
    }

    fun getChaptersForBook(bookId: String, language: String = "en"): List<NcertChapter> {
        return NcertCatalog.chapters
            .filter { it.bookId == bookId && (language.isBlank() || it.language == language || it.language == "en") }
            .sortedBy { it.displayOrder }
    }

    fun getChapter(chapterId: String): NcertChapter? {
        return NcertCatalog.chapters.firstOrNull { it.id == chapterId }
    }

    /**
     * Searches across all classes, subjects, books, and chapters.
     */
    fun searchCatalog(query: String): List<NcertSearchResult> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isBlank()) return emptyList()

        val results = mutableListOf<NcertSearchResult>()

        NcertCatalog.chapters.forEach { chapter ->
            val book = getBook(chapter.bookId)
            val subject = getSubject(chapter.subjectId)
            val ncertClass = getClass(chapter.classNumber)

            val bookTitle = book?.title ?: "NCERT Book"
            val subjectName = subject?.name ?: "Subject"
            val classTitle = ncertClass?.displayName ?: "Class ${chapter.classNumber}"

            val titleMatch = chapter.title.lowercase().contains(trimmed)
            val hindiTitleMatch = chapter.hindiTitle?.lowercase()?.contains(trimmed) == true
            val descMatch = chapter.description.lowercase().contains(trimmed)
            val subtopicMatch = chapter.subtopics.any { it.lowercase().contains(trimmed) }
            val bookMatch = bookTitle.lowercase().contains(trimmed)
            val subjectMatch = subjectName.lowercase().contains(trimmed)

            if (titleMatch || hindiTitleMatch || descMatch || subtopicMatch || bookMatch || subjectMatch) {
                val matchType = when {
                    titleMatch || hindiTitleMatch -> "Chapter"
                    subtopicMatch -> "Topic"
                    bookMatch -> "Book"
                    else -> "Subject"
                }
                results.add(
                    NcertSearchResult(
                        chapter = chapter,
                        bookTitle = bookTitle,
                        subjectName = subjectName,
                        classDisplayName = classTitle,
                        matchType = matchType
                    )
                )
            }
        }

        return results.take(30)
    }

    // =========================================================================
    // READING PROGRESS & BOOKMARK PERSISTENCE (Room Local Database)
    // =========================================================================

    fun getProgressForChapter(chapterId: String): Flow<NcertProgressEntity?> {
        return ncertDao.getProgressForChapter(chapterId)
    }

    fun getRecentProgress(limit: Int = 5): Flow<List<NcertProgressEntity>> {
        return ncertDao.getRecentProgress(limit)
    }

    fun getBookmarkedChapters(): Flow<List<NcertProgressEntity>> {
        return ncertDao.getBookmarkedChapters()
    }

    suspend fun recordReadingProgress(
        chapterId: String,
        currentPage: Int,
        totalPages: Int
    ) {
        val chapter = getChapter(chapterId)
        val now = System.currentTimeMillis()
        val calculatedPercentage = if (totalPages > 0) {
            ((currentPage.toFloat() / totalPages) * 100).toInt().coerceIn(0, 100)
        } else 0

        val existing = ncertDao.getProgressForChapterSync(chapterId)
        if (existing != null) {
            ncertDao.updatePageRead(
                chapterId = chapterId,
                page = currentPage,
                totalPages = totalPages,
                progressPercentage = calculatedPercentage,
                timestamp = now
            )
            if (calculatedPercentage >= 95 && !existing.isCompleted) {
                ncertDao.updateCompletionState(chapterId, true, now)
            }
        } else {
            val newRecord = NcertProgressEntity(
                chapterId = chapterId,
                bookId = chapter?.bookId ?: "",
                subjectId = chapter?.subjectId ?: "",
                classNumber = chapter?.classNumber ?: 11,
                lastPageRead = currentPage,
                totalPages = totalPages,
                progressPercentage = calculatedPercentage,
                isBookmarked = false,
                isCompleted = calculatedPercentage >= 95,
                lastOpenedAt = now,
                startedAt = now
            )
            ncertDao.insertOrUpdate(newRecord)
        }
    }

    suspend fun toggleBookmark(chapterId: String) {
        val existing = ncertDao.getProgressForChapterSync(chapterId)
        val now = System.currentTimeMillis()
        if (existing != null) {
            val newState = !existing.isBookmarked
            ncertDao.updateBookmarkState(chapterId, newState, now)
        } else {
            val chapter = getChapter(chapterId)
            val newRecord = NcertProgressEntity(
                chapterId = chapterId,
                bookId = chapter?.bookId ?: "",
                subjectId = chapter?.subjectId ?: "",
                classNumber = chapter?.classNumber ?: 11,
                lastPageRead = 1,
                totalPages = chapter?.estimatedPages ?: 20,
                progressPercentage = 0,
                isBookmarked = true,
                isCompleted = false,
                lastOpenedAt = now,
                startedAt = now
            )
            ncertDao.insertOrUpdate(newRecord)
        }
    }

    suspend fun markChapterCompleted(chapterId: String, isCompleted: Boolean) {
        val now = System.currentTimeMillis()
        val existing = ncertDao.getProgressForChapterSync(chapterId)
        if (existing != null) {
            ncertDao.updateCompletionState(chapterId, isCompleted, now)
        } else {
            val chapter = getChapter(chapterId)
            val newRecord = NcertProgressEntity(
                chapterId = chapterId,
                bookId = chapter?.bookId ?: "",
                subjectId = chapter?.subjectId ?: "",
                classNumber = chapter?.classNumber ?: 11,
                lastPageRead = chapter?.estimatedPages ?: 1,
                totalPages = chapter?.estimatedPages ?: 1,
                progressPercentage = if (isCompleted) 100 else 0,
                isBookmarked = false,
                isCompleted = isCompleted,
                lastOpenedAt = now,
                startedAt = now
            )
            ncertDao.insertOrUpdate(newRecord)
        }
    }
}
