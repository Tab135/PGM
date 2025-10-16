package com.example.pgm.Controller

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.pgm.model.Chapter
import com.example.pgm.model.Database.ChapterDatabaseHelper
import java.io.File
import java.io.FileOutputStream

class ChapterController(context: Context) {
    private val context: Context = context

    private val chapterDatabaseHelper = ChapterDatabaseHelper(context)

    fun addChapter(chapter: Chapter): Boolean {
        return chapterDatabaseHelper.addChapter(chapter)
    }


    fun updateChapter(chapter: Chapter): Boolean {
        return chapterDatabaseHelper.updateChapter(chapter)
    }

    fun deleteChapter(chapterId: Int): Boolean {
        return chapterDatabaseHelper.deleteChapter(chapterId)
    }

    fun getChapterById(chapterId: Int): Chapter? {
        return chapterDatabaseHelper.getChapterById(chapterId)
    }

    fun getChaptersByComicId(comicId: Int): List<Chapter> {
        return chapterDatabaseHelper.getChaptersByComicId(comicId)
    }

    fun updateChapterLike(chapterId: Int, likeCount: Int): Boolean {
        return chapterDatabaseHelper.updateChapterLikeCount(chapterId, likeCount)
    }

    fun saveImageToInternalStorage(uri: Uri, fileName: String): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputDir = File(context.filesDir, "chapter_thumbnails")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            val outputFile = File(outputDir, fileName)
            val outputStream = FileOutputStream(outputFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            outputFile.absolutePath
        } catch (e: Exception) {
            Log.e("ChapterController", "Error saving image: ${e.message}")
            null
        }
    }

    // Save PDF to internal storage
    fun savePdfToInternalStorage(uri: Uri, fileName: String): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputDir = File(context.filesDir, "chapter_pdfs")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            val outputFile = File(outputDir, fileName)
            val outputStream = FileOutputStream(outputFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            outputFile.absolutePath
        } catch (e: Exception) {
            Log.e("ChapterController", "Error saving PDF: ${e.message}")
            null
        }
    }

    // Get PDF page count (placeholder - requires PDF library)
    fun getPdfPageCount(pdfPath: String): Int {
        // TODO: Implement với thư viện PDF như PdfRenderer hoặc iTextPDF
        // Tạm thời return 1
        return try {
            // Example với PdfRenderer (API 21+):
            // val fd = ParcelFileDescriptor.open(File(pdfPath), ParcelFileDescriptor.MODE_READ_ONLY)
            // val pdfRenderer = PdfRenderer(fd)
            // val pageCount = pdfRenderer.pageCount
            // pdfRenderer.close()
            // fd.close()
            // return pageCount

            1 // Placeholder
        } catch (e: Exception) {
            Log.e("ChapterController", "Error getting PDF page count: ${e.message}")
            1
        }
    }

    // Delete file from internal storage
    fun deleteFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("ChapterController", "Error deleting file: ${e.message}")
            false
        }
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
            )
        )

        sampleChapters.forEach { chapter ->
            addChapter(chapter)
        }
    }
}