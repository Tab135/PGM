package com.example.pgm.Controller

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.pgm.R
import com.example.pgm.model.Database.UserDatabaseHelper
import com.example.pgm.utils.SessionManager
import com.example.pgm.view.AdminActivity
import com.example.pgm.view.Comic.ComicListActivity
import com.example.pgm.view.RegisterActivity

class LoginController(private val context: Context, private val view: View) {

    private val emailInput: EditText = view.findViewById(R.id.emailInput)
    private val passwordInput: EditText = view.findViewById(R.id.passwordInput)
    private val loginBtn: Button = view.findViewById(R.id.loginBtn)
    private val registerBtn: Button = view.findViewById(R.id.RegisterBtn)
    private val forgotPasswordText: TextView = view.findViewById(R.id.forgotPasswordText)

    private val databaseHelper = UserDatabaseHelper(context)
    private val sessionManager = SessionManager(context) // Use SessionManager instead of direct SharedPreferences
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    init {
        setupClickListeners()
        checkIfAlreadyLoggedIn()
    }

    private fun setupClickListeners() {
        loginBtn.setOnClickListener { login() }
        registerBtn.setOnClickListener { goToRegister() }
        forgotPasswordText.setOnClickListener { handleForgotPassword() }
    }

    private fun checkIfAlreadyLoggedIn() {
        val userId = sessionManager.getUserId() // Use SessionManager to get user ID

        if (userId != -1) {
            Log.d("LoginController", "User already logged in with ID: $userId")
            // Get user role from database to navigate properly
            val user = databaseHelper.getUserById(userId)
            if (user != null) {
                navigateBasedOnRole(user.role, userId)
            } else {
                // User not found in database, clear session
                sessionManager.clearSession()
            }
        }
    }

    private fun login() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields")
            return
        }

        if (databaseHelper.checkUser(email, password)) {
            val user = databaseHelper.getUserByEmail(email)

            if (user != null) {
                Log.d("LoginController", "Login successful for user: ${user.name} (ID: ${user.id})")

                // Save user session using SessionManager
                sessionManager.saveUserId(user.id ?: -1)

                // Debug: Verify session was saved correctly
                sessionManager.debugSession()

                // Also save to old SharedPreferences for backward compatibility if needed
                saveUserSession(user.id ?: -1, user.role)

                // Navigate based on role
                navigateBasedOnRole(user.role, user.id ?: -1)

                showSuccess("Welcome ${user.name}!")
            } else {
                showError("Login failed. Please try again.")
            }
        } else {
            showError("Invalid email or password")
        }
    }

    private fun saveUserSession(userId: Int, role: String) {
        // Keep this for backward compatibility if other parts of app use it
        sharedPreferences.edit().apply {
            putInt("userId", userId)
            putString("userRole", role)
            putBoolean("isLoggedIn", true)
            apply()
        }
        Log.d("LoginController", "Saved to SharedPreferences - userId: $userId, role: $role")
    }

    private fun navigateBasedOnRole(role: String, userId: Int) {
        Log.d("LoginController", "Navigating based on role: $role for user ID: $userId")

        val intent = when (role) {
            "admin" -> Intent(context, AdminActivity::class.java)
            else -> Intent(context, ComicListActivity::class.java).apply {
                putExtra("userId", userId)
            }
        }

        context.startActivity(intent)
        if (context is androidx.appcompat.app.AppCompatActivity) {
            context.finish()
        }
    }

    private fun goToRegister() {
        context.startActivity(Intent(context, RegisterActivity::class.java))
    }

    private fun handleForgotPassword() {
        Toast.makeText(context, "Password reset feature coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        Log.e("LoginController", "Login error: $message")
    }

    private fun showSuccess(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        Log.d("LoginController", "Login success: $message")
    }
}