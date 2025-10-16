package com.example.pgm.model.Database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

open class BaseDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "ComicLibrary.db"
        const val DATABASE_VERSION = 4

        // Users table
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "_id"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_NAME = "name"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_ADDRESS = "address"
        const val COLUMN_PROFILE_IMAGE = "profile_picture"

        // Comics table
        const val TABLE_COMICS = "comics"
        const val COLUMN_ID = "_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_AUTHOR = "author"
        const val COLUMN_PAGES = "pages"
//        const val COLUMN_LOCAL_PATH = "local_path"
//        const val COLUMN_REMOTE_URL = "remote_url"
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
        const val COLUMN_PAGES_LOCAL = "pages_local"
        const val COLUMN_PAGES_REMOTE = "pages_remote"

        // User Comic History table
        const val TABLE_USER_COMIC_HISTORY = "user_comic_history"
        const val COLUMN_HISTORY_ID = "_id"
        const val COLUMN_HISTORY_USER_ID = "user_id"
        const val COLUMN_VIEWED_CHAPTERS = "viewed_chapters"
        const val COLUMN_LATEST_VIEWED_CHAPTER = "latest_viewed_chapter"
        const val COLUMN_CURRENT_PAGE = "current_page"
        const val COLUMN_TOTAL_READING_TIME = "total_reading_time"
        const val COLUMN_LAST_READING_DATE = "last_reading_date"
        const val COLUMN_BOOKMARKED_CHAPTERS = "bookmarked_chapters"
        const val COLUMN_LIKED_CHAPTERS = "liked_chapters"
        const val COLUMN_READING_PROGRESS = "reading_progress"
        const val COLUMN_IS_COMPLETED = "is_completed"
        const val COLUMN_IS_FAVORITE = "is_favorite"
        const val COLUMN_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val COLUMN_LAST_VIEWED_PAGE = "last_viewed_page"
        const val COLUMN_READING_STREAK = "reading_streak"
        const val COLUMN_TOTAL_CHAPTERS_READ = "total_chapters_read"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_UPDATED_AT = "updated_at"

        
    }

    override fun onCreate(db: SQLiteDatabase?) {
        createUsersTable(db)

        val createComicsTable = """
            CREATE TABLE $TABLE_COMICS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_AUTHOR TEXT,
                $COLUMN_PAGES INTEGER,
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
        $COLUMN_CHAPTER_PAGES INTEGER,
        $COLUMN_PAGES_LOCAL TEXT,
        $COLUMN_PAGES_REMOTE TEXT,
        FOREIGN KEY($COLUMN_COMIC_ID) REFERENCES $TABLE_COMICS($COLUMN_ID)
    )
""".trimIndent()
        db?.execSQL(createChaptersTable)
        
        // Create UserComicHistory table
        createUserComicHistoryTable(db)

        createFeedbackTable(db)
    }

    private fun createUserComicHistoryTable(db: SQLiteDatabase?) {
        val createUserComicHistoryTable = """
            CREATE TABLE $TABLE_USER_COMIC_HISTORY (
                $COLUMN_HISTORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_HISTORY_USER_ID INTEGER NOT NULL,
                $COLUMN_COMIC_ID INTEGER NOT NULL,
                $COLUMN_VIEWED_CHAPTERS TEXT DEFAULT '',
                $COLUMN_LATEST_VIEWED_CHAPTER INTEGER,
                $COLUMN_CURRENT_PAGE INTEGER DEFAULT 0,
                $COLUMN_TOTAL_READING_TIME INTEGER DEFAULT 0,
                $COLUMN_LAST_READING_DATE TEXT,
                $COLUMN_BOOKMARKED_CHAPTERS TEXT DEFAULT '',
                $COLUMN_LIKED_CHAPTERS TEXT DEFAULT '',
                $COLUMN_READING_PROGRESS REAL DEFAULT 0.0,
                $COLUMN_IS_COMPLETED INTEGER DEFAULT 0,
                $COLUMN_IS_FAVORITE INTEGER DEFAULT 0,
                $COLUMN_NOTIFICATIONS_ENABLED INTEGER DEFAULT 1,
                $COLUMN_LAST_VIEWED_PAGE INTEGER DEFAULT 0,
                $COLUMN_READING_STREAK INTEGER DEFAULT 0,
                $COLUMN_TOTAL_CHAPTERS_READ INTEGER DEFAULT 0,
                $COLUMN_CREATED_AT TEXT NOT NULL,
                $COLUMN_UPDATED_AT TEXT NOT NULL,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID),
                FOREIGN KEY($COLUMN_COMIC_ID) REFERENCES $TABLE_COMICS($COLUMN_ID),
                UNIQUE($COLUMN_USER_ID, $COLUMN_COMIC_ID)
            )
        """.trimIndent()
        db?.execSQL(createUserComicHistoryTable)
        Log.d("Database", "User Comic History table created")
    }

    private fun createUsersTable(db: SQLiteDatabase?) {
        // Only create if it doesn't exist
        val createUsersTable = """ 
            CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, 
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL, 
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_PHONE TEXT DEFAULT '',
                $COLUMN_ADDRESS TEXT DEFAULT '',
                $COLUMN_PROFILE_IMAGE TEXT
            )
        """.trimIndent()
        db?.execSQL(createUsersTable)
        Log.d("Database", "Users table created or already exists")
    }
    private fun createFeedbackTable(db: SQLiteDatabase?) {
        val createFeedbackTable = """
        CREATE TABLE IF NOT EXISTS feedback (
            _id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            comic_id INTEGER NOT NULL,
            rating REAL NOT NULL,
            comment TEXT NOT NULL,
            categories TEXT DEFAULT '',
            is_anonymous INTEGER DEFAULT 0,
            created_at TEXT NOT NULL,
            updated_at TEXT,
            likes INTEGER DEFAULT 0,
            is_edited INTEGER DEFAULT 0,
            FOREIGN KEY(user_id) REFERENCES $TABLE_USERS($COLUMN_USER_ID),
            FOREIGN KEY(comic_id) REFERENCES $TABLE_COMICS($COLUMN_ID),
            UNIQUE(user_id, comic_id)
        )
    """.trimIndent()
        db?.execSQL(createFeedbackTable)
        Log.d("Database", "Feedback table created")
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS feedback")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER_COMIC_HISTORY")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CHAPTERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_COMICS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }
}