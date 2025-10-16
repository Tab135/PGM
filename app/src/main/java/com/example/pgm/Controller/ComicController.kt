package com.example.pgm.Controller

import com.example.pgm.model.Comic
import com.example.pgm.model.Database.ComicDatabaseHelper
import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class ComicController(private val context: Context) {
    private val comicDatabaseHelper = ComicDatabaseHelper(context)

    fun addComic(comic: Comic): Boolean {
        return comicDatabaseHelper.addComic(comic)
    }

    fun getAllComics(): List<Comic> {
        return comicDatabaseHelper.getAllComics()
    }

    fun getComicById(comicId: Int): Comic? {
        return getAllComics().find { it.id == comicId }
    }

        fun updateComic(comic: Comic): Boolean {
            return comicDatabaseHelper.updateComic(comic)
        }

        fun deleteComic(comicId: Int): Boolean {
            return comicDatabaseHelper.deleteComic(comicId)
        }

        fun searchComics(query: String): List<Comic> {
            return comicDatabaseHelper.searchComics(query)
        }

        // Save image to internal storage
        fun saveImageToInternalStorage(uri: Uri, fileName: String): String? {
            return try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val outputDir = File(context.filesDir, "comic_covers")
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
                Log.e("ComicController", "Error saving image: ${e.message}")
                null
            }
        }

        // Save PDF to internal storage (for backward compatibility if needed)
        fun savePdfToInternalStorage(uri: Uri, fileName: String): String? {
            return try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val outputDir = File(context.filesDir, "comics")
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
                Log.e("ComicController", "Error saving PDF: ${e.message}")
                null
            }
        }

        // Delete file from internal storage
        fun deleteComicFile(filePath: String): Boolean {
            return try {
                val file = File(filePath)
                if (file.exists()) {
                    file.delete()
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("ComicController", "Error deleting file: ${e.message}")
                false
            }
        }
    }

