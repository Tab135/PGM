package com.example.pgm.model.Database

import android.content.ContentValues
import android.content.Context
import com.example.pgm.model.TokenTransaction
import com.example.pgm.model.TransactionType
import com.example.pgm.model.TransactionStatus
import java.text.SimpleDateFormat
import java.util.*

class TokenTransactionDatabaseHelper(context: Context) : BaseDatabaseHelper(context) {

    companion object {
        const val TABLE_TRANSACTIONS = "token_transactions"
        const val COLUMN_TRANSACTION_ID = "_id"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_TYPE = "type"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_PRICE = "price"
        const val COLUMN_PAYMENT_METHOD = "payment_method"
        const val COLUMN_PACKAGE_NAME = "package_name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_STATUS = "status"
    }

    // Create transactions table if not exists
    private fun createTransactionsTable() {
        val db = writableDatabase
        val createTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_TRANSACTIONS (
                $COLUMN_TRANSACTION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_TYPE TEXT NOT NULL,
                $COLUMN_AMOUNT INTEGER NOT NULL,
                $COLUMN_PRICE REAL DEFAULT 0.0,
                $COLUMN_PAYMENT_METHOD TEXT DEFAULT '',
                $COLUMN_PACKAGE_NAME TEXT DEFAULT '',
                $COLUMN_DESCRIPTION TEXT DEFAULT '',
                $COLUMN_TIMESTAMP TEXT NOT NULL,
                $COLUMN_STATUS TEXT DEFAULT 'COMPLETED',
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    init {
        createTransactionsTable()
    }

    /**
     * Record a token purchase transaction
     */
    fun recordPurchase(
        userId: Int,
        amount: Int,
        price: Double,
        paymentMethod: String,
        packageName: String
    ): Long {
        val db = writableDatabase
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_TYPE, TransactionType.PURCHASE.name)
            put(COLUMN_AMOUNT, amount)
            put(COLUMN_PRICE, price)
            put(COLUMN_PAYMENT_METHOD, paymentMethod)
            put(COLUMN_PACKAGE_NAME, packageName)
            put(COLUMN_DESCRIPTION, "Purchased $amount tokens")
            put(COLUMN_TIMESTAMP, timestamp)
            put(COLUMN_STATUS, TransactionStatus.COMPLETED.name)
        }

        return db.insert(TABLE_TRANSACTIONS, null, values)
    }

    /**
     * Record spending tokens on a chapter
     */
    fun recordSpend(userId: Int, amount: Int, chapterTitle: String): Long {
        val db = writableDatabase
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_TYPE, TransactionType.SPEND.name)
            put(COLUMN_AMOUNT, -amount) // Negative for spending
            put(COLUMN_PRICE, 0.0)
            put(COLUMN_DESCRIPTION, "Unlocked: $chapterTitle")
            put(COLUMN_TIMESTAMP, timestamp)
            put(COLUMN_STATUS, TransactionStatus.COMPLETED.name)
        }

        return db.insert(TABLE_TRANSACTIONS, null, values)
    }

    /**
     * Record bonus tokens (daily login, promotions, etc.)
     */
    fun recordBonus(userId: Int, amount: Int, description: String): Long {
        val db = writableDatabase
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_TYPE, TransactionType.BONUS.name)
            put(COLUMN_AMOUNT, amount)
            put(COLUMN_PRICE, 0.0)
            put(COLUMN_DESCRIPTION, description)
            put(COLUMN_TIMESTAMP, timestamp)
            put(COLUMN_STATUS, TransactionStatus.COMPLETED.name)
        }

        return db.insert(TABLE_TRANSACTIONS, null, values)
    }

    /**
     * Get all transactions for a user
     */
    fun getUserTransactions(userId: Int): List<TokenTransaction> {
        val transactions = mutableListOf<TokenTransaction>()
        val db = readableDatabase

        val query = """
            SELECT * FROM $TABLE_TRANSACTIONS 
            WHERE $COLUMN_USER_ID = ? 
            ORDER BY $COLUMN_TIMESTAMP DESC
        """

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                transactions.add(
                    TokenTransaction(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                        type = TransactionType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE))),
                        amount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                        price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                        paymentMethod = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_METHOD)) ?: "",
                        packageName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PACKAGE_NAME)) ?: "",
                        description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)) ?: "",
                        timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                        status = TransactionStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return transactions
    }

    /**
     * Get total amount spent by user
     */
    fun getTotalSpent(userId: Int): Double {
        val db = readableDatabase
        val query = """
            SELECT SUM($COLUMN_PRICE) as total 
            FROM $TABLE_TRANSACTIONS 
            WHERE $COLUMN_USER_ID = ? AND $COLUMN_TYPE = ?
        """

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), TransactionType.PURCHASE.name))

        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }
        cursor.close()
        return total
    }

    /**
     * Get total tokens purchased by user
     */
    fun getTotalTokensPurchased(userId: Int): Int {
        val db = readableDatabase
        val query = """
            SELECT SUM($COLUMN_AMOUNT) as total 
            FROM $TABLE_TRANSACTIONS 
            WHERE $COLUMN_USER_ID = ? AND $COLUMN_TYPE = ?
        """

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), TransactionType.PURCHASE.name))

        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }

    /**
     * Get recent transactions (last 10)
     */
    fun getRecentTransactions(userId: Int, limit: Int = 10): List<TokenTransaction> {
        val transactions = mutableListOf<TokenTransaction>()
        val db = readableDatabase

        val query = """
            SELECT * FROM $TABLE_TRANSACTIONS 
            WHERE $COLUMN_USER_ID = ? 
            ORDER BY $COLUMN_TIMESTAMP DESC 
            LIMIT ?
        """

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), limit.toString()))

        if (cursor.moveToFirst()) {
            do {
                transactions.add(
                    TokenTransaction(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                        type = TransactionType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE))),
                        amount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                        price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                        paymentMethod = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_METHOD)) ?: "",
                        packageName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PACKAGE_NAME)) ?: "",
                        description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)) ?: "",
                        timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                        status = TransactionStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return transactions
    }
}