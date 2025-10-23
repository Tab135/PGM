package com.example.pgm.utils

import android.content.Context
import android.util.Log

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    private val TAG = "SessionManager"

    fun saveUserId(userId: Int) {
        Log.d(TAG, "Saving user ID to session: $userId")
        prefs.edit().putInt("USER_ID", userId).apply()

        // Verify it was saved
        val savedId = prefs.getInt("USER_ID", -1)
        Log.d(TAG, "Verified saved user ID: $savedId")
    }

    fun getUserId(): Int {
        val userId = prefs.getInt("USER_ID", -1)
        Log.d(TAG, "Retrieving user ID from session: $userId")
        return userId
    }

    fun clearSession() {
        Log.d(TAG, "Clearing user session")
        prefs.edit().clear().apply()
    }

    // Debug method to check session state
    fun debugSession() {
        val currentUserId = getUserId()
        Log.d(TAG, "=== SESSION DEBUG ===")
        Log.d(TAG, "Current user ID in session: $currentUserId")
        Log.d(TAG, "All session values: ${prefs.all}")
        Log.d(TAG, "=== END SESSION DEBUG ===")
    }
}