package com.example.pgm.model.Database

import android.content.ContentValues
import android.content.Context
import com.example.pgm.model.Comic

class ComicDatabaseHelper(context: Context) : BaseDatabaseHelper(context) {

    companion object {
        const val COLUMN_IMAGE_URL = "image_url"
    }

    fun addComic(comic: Comic): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COLUMN_TITLE, comic.title)
            put(COLUMN_AUTHOR, comic.author)
            put(COLUMN_PAGES, comic.pages)
            put(COLUMN_IMAGE_URL, comic.imageUrl)
        }
        val result = db.insert(TABLE_COMICS, null, cv)
        db.close()
        return result != -1L
    }

    fun getAllComics(): List<Comic> {
        val comics = mutableListOf<Comic>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_COMICS", null)
        if (cursor.moveToFirst()) {
            do {
                comics.add(
                    Comic(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        author = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTHOR)),
                        pages = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PAGES)),
                        imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return comics
    }
}