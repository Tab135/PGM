package com.example.pgm.Controller

import com.example.pgm.model.Comic
import com.example.pgm.model.Database.ComicDatabaseHelper
import android.content.Context

class ComicController(private val context: Context) {
    private val db = ComicDatabaseHelper(context)

    fun addComic(comic: Comic): Boolean {
        return db.addComic(comic)
    }

    fun getAllComics(): List<Comic> {
        return db.getAllComics()
    }

    fun getComicById(comicId: Int): Comic? {
        return db.getAllComics().find { it.id == comicId }
    }
}