package com.example.pgm.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.pgm.R
import com.example.pgm.model.Database.UserDatabaseHelper

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var resetButton: Button
    private lateinit var databaseHelper: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        databaseHelper = UserDatabaseHelper(this)
        emailInput = findViewById(R.id.emailInput)
        resetButton = findViewById(R.id.resetButton)

        resetButton.setOnClickListener { resetPassword() }
    }

    private fun resetPassword() {
        val email = emailInput.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        val user = databaseHelper.getUserByEmail(email)
        if (user != null) {
            // Show dialog to enter new password
            showNewPasswordDialog(user.id ?: 0)
        } else {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNewPasswordDialog(userId: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_new_password, null)
        val newPasswordInput = dialogView.findViewById<EditText>(R.id.newPasswordInput)
        val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.confirmPasswordInput)

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setView(dialogView)
            .setPositiveButton("Reset") { _, _ ->
                val newPass = newPasswordInput.text.toString()
                val confirmPass = confirmPasswordInput.text.toString()

                if (newPass.isEmpty() || confirmPass.isEmpty()) {
                    Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPass != confirmPass) {
                    Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPass.length < 1) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (databaseHelper.updateUserPassword(userId, newPass)) {
                    Toast.makeText(this, "Password reset successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to reset password", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}