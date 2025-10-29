package com.example.pgm.model.Database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

open class BaseDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "ComicLibrary.db"
        const val DATABASE_VERSION = 9  // Increment version to trigger upgrade

        // Users table
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "_id"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_NAME = "name"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_ADDRESS = "address"
        const val COLUMN_PROFILE_IMAGE = "profile_picture"
        const val COLUMN_ROLE = "role"
        const val COLUMN_TOKENS = "tokens"  // New column for token balance

        // Comics table
        const val TABLE_COMICS = "comics"
        const val COLUMN_ID = "_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_AUTHOR = "author"
        const val COLUMN_PAGES = "pages"
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
        const val COLUMN_FREE_DATE = "free_date"
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
        const val COLUMN_BOOKMARKED_CHAPTERS = "bookmarked_chapters"
        const val COLUMN_PURCHASED_CHAPTERS = "purchased_chapters"
        const val COLUMN_READING_PROGRESS = "reading_progress"
        const val COLUMN_IS_FAVORITE = "is_favorite"
        const val COLUMN_LAST_VIEWED_PAGE = "last_viewed_page"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_UPDATED_AT = "updated_at"

        // Token Transactions table
        const val TABLE_TRANSACTIONS = "token_transactions"
        const val COLUMN_TRANSACTION_ID = "_id"
        const val COLUMN_TRANSACTION_USER_ID = "user_id"
        const val COLUMN_TYPE = "type"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_PRICE = "price"
        const val COLUMN_PAYMENT_METHOD = "payment_method"
        const val COLUMN_PACKAGE_NAME = "package_name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_STATUS = "status"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        createUsersTable(db)
        createComicsTable(db)
        createChaptersTable(db)
        createUserComicHistoryTable(db)
        createFeedbackTable(db)
        createTokenTransactionsTable(db) // Add this line

        // Insert default admin user with tokens
        db?.execSQL("""
            INSERT INTO $TABLE_USERS ($COLUMN_EMAIL, $COLUMN_PASSWORD, $COLUMN_NAME, $COLUMN_ROLE, $COLUMN_TOKENS) 
            VALUES ('admin@comic.com', '1', 'Admin', 'admin', 999999)
        """)
        Log.d("Database", "Database created with all tables")
    }

    private fun createUsersTable(db: SQLiteDatabase?) {
        val createUsersTable = """ 
            CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, 
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL, 
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_PHONE TEXT DEFAULT '',
                $COLUMN_ADDRESS TEXT DEFAULT '',
                $COLUMN_PROFILE_IMAGE TEXT,
                $COLUMN_ROLE TEXT DEFAULT 'user',
                $COLUMN_TOKENS INTEGER DEFAULT 100
            )
        """.trimIndent()
        db?.execSQL(createUsersTable)
        Log.d("Database", "Users table created")
    }

    private fun createComicsTable(db: SQLiteDatabase?) {
        val createComicsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_COMICS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_AUTHOR TEXT,
                $COLUMN_PAGES INTEGER,
                $COLUMN_IMAGE_URL TEXT
            )
        """.trimIndent()
        db?.execSQL(createComicsTable)
        Log.d("Database", "Comics table created")
    }

    private fun createChaptersTable(db: SQLiteDatabase?) {
        val createChaptersTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_CHAPTERS (
                $COLUMN_CHAPTER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_COMIC_ID INTEGER NOT NULL,
                $COLUMN_CHAPTER_NUMBER INTEGER NOT NULL,
                $COLUMN_CHAPTER_TITLE TEXT NOT NULL,
                $COLUMN_THUMBNAIL_URL TEXT,
                $COLUMN_RELEASE_DATE TEXT,
                $COLUMN_IS_LOCKED INTEGER DEFAULT 0,
                $COLUMN_COST INTEGER DEFAULT 0,
                $COLUMN_FREE_DAYS INTEGER DEFAULT 0,
                $COLUMN_FREE_DATE INTEGER DEFAULT 0,
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
        Log.d("Database", "Chapters table created")
    }

    private fun createUserComicHistoryTable(db: SQLiteDatabase?) {
        val createUserComicHistoryTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_USER_COMIC_HISTORY (
                $COLUMN_HISTORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_HISTORY_USER_ID INTEGER NOT NULL,
                $COLUMN_COMIC_ID INTEGER NOT NULL,
                $COLUMN_VIEWED_CHAPTERS TEXT DEFAULT '',
                $COLUMN_LATEST_VIEWED_CHAPTER INTEGER,
                $COLUMN_CURRENT_PAGE INTEGER DEFAULT 0,
                $COLUMN_TOTAL_READING_TIME INTEGER DEFAULT 0,
                $COLUMN_BOOKMARKED_CHAPTERS TEXT DEFAULT '',
                $COLUMN_PURCHASED_CHAPTERS TEXT DEFAULT '',
                $COLUMN_READING_PROGRESS REAL DEFAULT 0.0,
                $COLUMN_IS_FAVORITE INTEGER DEFAULT 0,
                $COLUMN_LAST_VIEWED_PAGE INTEGER DEFAULT 0,
                $COLUMN_CREATED_AT TEXT NOT NULL,
                $COLUMN_UPDATED_AT TEXT NOT NULL,
                FOREIGN KEY($COLUMN_HISTORY_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID),
                FOREIGN KEY($COLUMN_COMIC_ID) REFERENCES $TABLE_COMICS($COLUMN_ID),
                UNIQUE($COLUMN_HISTORY_USER_ID, $COLUMN_COMIC_ID)
            )
        """.trimIndent()
        db?.execSQL(createUserComicHistoryTable)
        Log.d("Database", "User Comic History table created")
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

    private fun createTokenTransactionsTable(db: SQLiteDatabase?) {
        val createTokenTransactionsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_TRANSACTIONS (
                $COLUMN_TRANSACTION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TRANSACTION_USER_ID INTEGER NOT NULL,
                $COLUMN_TYPE TEXT NOT NULL,
                $COLUMN_AMOUNT INTEGER NOT NULL,
                $COLUMN_PRICE REAL DEFAULT 0.0,
                $COLUMN_PAYMENT_METHOD TEXT DEFAULT '',
                $COLUMN_PACKAGE_NAME TEXT DEFAULT '',
                $COLUMN_DESCRIPTION TEXT DEFAULT '',
                $COLUMN_TIMESTAMP TEXT NOT NULL,
                $COLUMN_STATUS TEXT DEFAULT 'COMPLETED',
                FOREIGN KEY($COLUMN_TRANSACTION_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """.trimIndent()
        db?.execSQL(createTokenTransactionsTable)
        Log.d("Database", "Token Transactions table created")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d("Database", "Upgrading database from version $oldVersion to $newVersion")

        // Drop all tables
        db?.execSQL("DROP TABLE IF EXISTS feedback")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS") // Add this line
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER_COMIC_HISTORY")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CHAPTERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_COMICS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")

        // Recreate all tables
        onCreate(db)
    }
}