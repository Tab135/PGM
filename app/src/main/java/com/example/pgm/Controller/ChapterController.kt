package com.example.pgm.Controller

import android.content.Context
import com.example.pgm.model.Chapter
import com.example.pgm.model.Database.ChapterDatabaseHelper

class ChapterController(context: Context) {
    private val chapterDatabaseHelper = ChapterDatabaseHelper(context)

    fun addChapter(chapter: Chapter): Boolean {
        return chapterDatabaseHelper.addChapter(chapter)
    }

    fun getChapterById(chapterId: Int): Chapter? {
        return chapterDatabaseHelper.getChapterById(chapterId)
    }

    fun getChaptersByComicId(comicId: Int): List<Chapter> {
        return chapterDatabaseHelper.getChaptersByComicId(comicId)
    }

    fun markChapterAsRead(chapterId: Int): Boolean {
        return chapterDatabaseHelper.updateChapterReadStatus(chapterId, true)
    }

    fun toggleChapterLike(chapterId: Int, currentLikeStatus: Boolean, currentLikeCount: Int): Boolean {
        val newLikeStatus = !currentLikeStatus
        val newLikeCount = if (newLikeStatus) currentLikeCount + 1 else currentLikeCount - 1
        return chapterDatabaseHelper.updateChapterLikeStatus(chapterId, newLikeStatus, newLikeCount)
    }

    // Helper method to add sample chapters for testing
    fun addSampleChapters(comicId: Int) {
        val sampleChapters = listOf(
            Chapter(
                comicId = comicId,
                chapterNumber = 25,
                title = "Her Smile",
                thumbnailUrl = "https://example.com/thumb25.jpg",
                releaseDate = "Jun 14, 2024",
                isLocked = true,
                cost = 7,
                freeDays = 35,
                likeCount = 621
            ),
            Chapter(
                comicId = comicId,
                chapterNumber = 24,
                title = "Saturday Afternoon",
                thumbnailUrl = "https://example.com/thumb24.jpg",
                releaseDate = "Jun 7, 2024",
                isLocked = true,
                cost = 7,
                freeDays = 28,
                likeCount = 543
            ),
            Chapter(
                comicId = comicId,
                chapterNumber = 23,
                title = "Samy",
                thumbnailUrl = "https://example.com/thumb23.jpg",
                releaseDate = "May 31, 2024",
                isLocked = true,
                cost = 7,
                freeDays = 21,
                likeCount = 478
            ),
            Chapter(
                comicId = comicId,
                chapterNumber = 22,
                title = "Welcome!",
                thumbnailUrl = "https://example.com/thumb22.jpg",
                releaseDate = "May 24, 2024",
                isLocked = true,
                cost = 7,
                freeDays = 14,
                likeCount = 692
            ),
            Chapter(
                comicId = comicId,
                chapterNumber = 21,
                title = "A Day on the Farm",
                thumbnailUrl = "https://example.com/thumb21.jpg",
                releaseDate = "May 17, 2024",
                isLocked = true,
                cost = 7,
                freeDays = 7,
                likeCount = 521
            ),
            Chapter(
                comicId = comicId,
                chapterNumber = 20,
                title = "The Big Plan!",
                thumbnailUrl = "https://example.com/thumb20.jpg",
                releaseDate = "May 10, 2024",
                isLocked = false,
                cost = 0,
                freeDays = 0,
                likeCount = 758,
                isRead = true
            )
        )

        sampleChapters.forEach { chapter ->
            addChapter(chapter)
        }
    }
}