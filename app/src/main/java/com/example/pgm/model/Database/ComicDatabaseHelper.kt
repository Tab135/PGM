package com.example.pgm.model.Database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
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

    fun updateComic(comic: Comic): Boolean{
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, comic.title)
            put(COLUMN_AUTHOR, comic.author)
            put(COLUMN_PAGES, comic.pages)
//            put(COLUMN_LOCAL_PATH, comic.localPath)
//            put(COLUMN_REMOTE_URL, comic.remoteUrl)
            put(COLUMN_IMAGE_URL, comic.imageUrl)
        }
            val result = db.update(TABLE_COMICS, values, "$COLUMN_ID = ?", arrayOf(comic.id.toString()))
        db.close()
        return result >0
        }
    fun deleteComic(comicId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_COMICS, "$COLUMN_ID = ?", arrayOf(comicId.toString()))
        db.close()
        return result > 0
    }
    fun searchComics(query: String): List<Comic> {
        val comics = mutableListOf<Comic>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_COMICS,
            null,
            "$COLUMN_TITLE LIKE ? OR $COLUMN_AUTHOR LIKE ?",
            arrayOf("%$query%", "%$query%"),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            do {
                comics.add(cursorToComic(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return comics
    }

    private fun cursorToComic(cursor: Cursor): Comic {
        return Comic(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
            author = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTHOR)),
            pages = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PAGES)),
       //     localPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCAL_PATH)),
       //     remoteUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMOTE_URL)),
            imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL))
        )
    }





    }
