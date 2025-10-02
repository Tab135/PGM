package com.example.pgm.model.Database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

open class BaseDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "ComicLibrary.db"
        const val DATABASE_VERSION = 2

        // Users table
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "_id"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_NAME = "name"

        // Comics table
        const val TABLE_COMICS = "comics"
        const val COLUMN_ID = "_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_AUTHOR = "author"
        const val COLUMN_PAGES = "pages"
        const val COLUMN_LOCAL_PATH = "local_path"
        const val COLUMN_REMOTE_URL = "remote_url"
        const val COLUMN_IMAGE_URL = "image_url"

        // Chapters table
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
        const val COLUMN_IS_LIKED = "is_liked"
        const val COLUMN_LIKE_COUNT = "like_count"
        const val COLUMN_IS_READ = "is_read"
        const val COLUMN_CHAPTER_PAGES = "pages"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsersTable = """ CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, 
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL, 
                $COLUMN_NAME TEXT NOT NULL 
                )
                """.trimIndent()
        db?.execSQL(createUsersTable)

        val createComicsTable = """
            CREATE TABLE $TABLE_COMICS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_AUTHOR TEXT,
                $COLUMN_PAGES INTEGER,
                $COLUMN_LOCAL_PATH TEXT,
                $COLUMN_REMOTE_URL TEXT,
                $COLUMN_IMAGE_URL TEXT
            )
        """.trimIndent()
        db?.execSQL(createComicsTable)

        val createChaptersTable = """
            CREATE TABLE $TABLE_CHAPTERS (
                $COLUMN_CHAPTER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_COMIC_ID INTEGER NOT NULL,
                $COLUMN_CHAPTER_NUMBER INTEGER NOT NULL,
                $COLUMN_CHAPTER_TITLE TEXT NOT NULL,
                $COLUMN_THUMBNAIL_URL TEXT,
                $COLUMN_RELEASE_DATE TEXT,
                $COLUMN_IS_LOCKED INTEGER DEFAULT 0,
                $COLUMN_COST INTEGER DEFAULT 0,
                $COLUMN_FREE_DAYS INTEGER DEFAULT 0,
                $COLUMN_IS_LIKED INTEGER DEFAULT 0,
                $COLUMN_LIKE_COUNT INTEGER DEFAULT 0,
                $COLUMN_IS_READ INTEGER DEFAULT 0,
                $COLUMN_CHAPTER_PAGES TEXT,
                FOREIGN KEY($COLUMN_COMIC_ID) REFERENCES $TABLE_COMICS($COLUMN_ID)
            )
        """.trimIndent()
        db?.execSQL(createChaptersTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CHAPTERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_COMICS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }
}