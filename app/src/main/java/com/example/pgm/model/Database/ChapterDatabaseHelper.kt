package com.example.pgm.model.Database

import android.content.ContentValues
import android.content.Context
import com.example.pgm.model.Chapter

class ChapterDatabaseHelper(context: Context) : BaseDatabaseHelper(context) {

    companion object {
        const val TABLE_CHAPTERS = "chapters"
        const val COLUMN_CHAPTER_ID = "_id"
        const val COLUMN_COMIC_ID = "comic_id"
        const val COLUMN_CHAPTER_NUMBER = "chapter_number"
        const val COLUMN_CHAPTER_TITLE = "chapter_title"
        const val COLUMN_THUMBNAIL_URL = "thumbnail_url"
        const val COLUMN_RELEASE_DATE = "release_date"
        const val COLUMN_IS_LOCKED = "is_locked"
        const val COLUMN_COST = "cost"
        const val COLUMN_FREE_DAYS = "free_days"
    const val COLUMN_FREE_DATE = "free_date"
        const val COLUMN_LIKE_COUNT = "like_count"
        const val COLUMN_PAGES = "pages"
        const val COLUMN_PAGES_LOCAL = "pages_local"
        const val COLUMN_PAGES_REMOTE = "pages_remote"
    }

    fun addChapter(chapter: Chapter): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COLUMN_COMIC_ID, chapter.comicId)
            put(COLUMN_CHAPTER_NUMBER, chapter.chapterNumber)
            put(COLUMN_CHAPTER_TITLE, chapter.title)
            put(COLUMN_THUMBNAIL_URL, chapter.thumbnailUrl)
            put(COLUMN_RELEASE_DATE, chapter.releaseDate)
            put(COLUMN_IS_LOCKED, if (chapter.isLocked) 1 else 0)
            put(COLUMN_COST, chapter.cost)
            put(COLUMN_FREE_DAYS, chapter.freeDays)
            chapter.freeDate?.let { put(COLUMN_FREE_DATE, it) }
            put(COLUMN_LIKE_COUNT, chapter.likeCount)
            put(COLUMN_PAGES, chapter.pages)
            put(COLUMN_PAGES_REMOTE, chapter.remoteUrl)
            put(COLUMN_PAGES_LOCAL, chapter.localPath)
        }
        val result = db.insert(TABLE_CHAPTERS, null, cv)
        db.close()
        return result != -1L
    }

    fun getChapterById(id: Int): Chapter? {
        var chapter: Chapter? = null
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CHAPTERS WHERE $COLUMN_ID = ? ORDER BY $COLUMN_CHAPTER_NUMBER DESC",
            arrayOf(id.toString())
        )
        if (cursor.moveToFirst()) {
            chapter = Chapter(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CHAPTER_ID)),
                comicId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMIC_ID)),
                chapterNumber = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CHAPTER_NUMBER)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHAPTER_TITLE)),
                thumbnailUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_THUMBNAIL_URL)),
                releaseDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RELEASE_DATE)),
                isLocked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_LOCKED)) == 1,
                cost = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COST)),
                freeDays = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FREE_DAYS)),
                freeDate = run {
                    val idx = cursor.getColumnIndex(COLUMN_FREE_DATE)
                    if (idx != -1) {
                        val v = cursor.getLong(idx)
                        if (v == 0L) null else v
                    } else null
                },
                likeCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIKE_COUNT)),
                pages = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PAGES)),
                localPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAGES_LOCAL)),
                remoteUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAGES_REMOTE))
            )
        }
        cursor.close()
        db.close()
        return chapter
    }

    fun getChaptersByComicId(comicId: Int): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CHAPTERS WHERE $COLUMN_COMIC_ID = ? ORDER BY $COLUMN_CHAPTER_NUMBER DESC",
            arrayOf(comicId.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                chapters.add(
                    Chapter(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CHAPTER_ID)),
                        comicId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMIC_ID)),
                        chapterNumber = cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                COLUMN_CHAPTER_NUMBER
                            )
                        ),
                        title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHAPTER_TITLE)),
                        thumbnailUrl = cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                COLUMN_THUMBNAIL_URL
                            )
                        ),
                        releaseDate = cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                COLUMN_RELEASE_DATE
                            )
                        ),
                        isLocked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_LOCKED)) == 1,
                        cost = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COST)),
                        freeDays = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FREE_DAYS)),
                        freeDate = run {
                            val idx = cursor.getColumnIndex(COLUMN_FREE_DATE)
                            if (idx != -1) {
                                val v = cursor.getLong(idx)
                                if (v == 0L) null else v
                            } else null
                        },
                        likeCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIKE_COUNT)),
                        pages = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PAGES)),
                        localPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAGES_LOCAL)),
                        remoteUrl = cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                COLUMN_PAGES_REMOTE
                            )
                        )
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return chapters
    }

    fun updateChapterLikeCount(chapterId: Int, likeCount: Int): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COLUMN_LIKE_COUNT, likeCount)
        }
        val result =
            db.update(TABLE_CHAPTERS, cv, "$COLUMN_CHAPTER_ID = ?", arrayOf(chapterId.toString()))
        db.close()
        return result > 0
    }
    fun deleteChapter(chapterId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_CHAPTERS, "$COLUMN_CHAPTER_ID = ?", arrayOf(chapterId.toString()))
        db.close()
        return result > 0
    }
    fun updateChapter(chapter: Chapter): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COLUMN_COMIC_ID, chapter.comicId)
            put(COLUMN_CHAPTER_NUMBER, chapter.chapterNumber)
            put(COLUMN_CHAPTER_TITLE, chapter.title)
            put(COLUMN_THUMBNAIL_URL, chapter.thumbnailUrl)
            put(COLUMN_RELEASE_DATE, chapter.releaseDate)
            put(COLUMN_IS_LOCKED, if (chapter.isLocked) 1 else 0)
            put(COLUMN_COST, chapter.cost)
            put(COLUMN_FREE_DAYS, chapter.freeDays)
            put(COLUMN_LIKE_COUNT, chapter.likeCount)
            put(COLUMN_PAGES, chapter.pages)
            put(COLUMN_PAGES_REMOTE, chapter.remoteUrl)
            put(COLUMN_PAGES_LOCAL, chapter.localPath)
            chapter.freeDate?.let { put(COLUMN_FREE_DATE, it) }
        }
        val result = db.update(TABLE_CHAPTERS, cv, "$COLUMN_CHAPTER_ID = ?", arrayOf(chapter.id.toString()))
        db.close()
        return result > 0
    }
}