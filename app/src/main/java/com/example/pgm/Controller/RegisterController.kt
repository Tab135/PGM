package com.example.pgm.Controller

import com.example.pgm.model.Database.UserDatabaseHelper
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.pgm.R
import com.example.pgm.model.User
import com.example.pgm.view.LoginActivity
import com.example.pgm.view.RegisterActivity

class RegisterController(private val context: Context, private val view: View) {

    private val userEmail: EditText = view.findViewById(R.id.emailInput)
    private val password: EditText = view.findViewById(R.id.passwordInput)
    private val name: EditText = view.findViewById(R.id.nameInput)
    private val registerBtn: Button = view.findViewById(R.id.RegisterBtn)
    private val databaseHelper = UserDatabaseHelper(context)

    init {
        registerBtn.setOnClickListener { registerUser() }
    }

    fun registerUser() {
        val email = userEmail.text.toString()
        val pass = password.text.toString()
        val userName = name.text.toString()

        if (email.isEmpty() || pass.isEmpty() || userName.isEmpty()) {
            Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }
        if(!isValidEmailAndroid(email))
        {
            Toast.makeText(context,"The email must have the back as @example.com", Toast.LENGTH_SHORT).show()
            return
        }
        if (!databaseHelper.checkEmail(email)) {
            val user = User(email = email, password = pass, name = userName)
            val success = databaseHelper.addUser(user)
            if (success) {
                context.startActivity(Intent(context, LoginActivity::class.java))
                Toast.makeText(context, "Register success", Toast.LENGTH_SHORT).show()
                (context as? RegisterActivity)?.finish()
            } else {
                Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show()
        }
    }
    private fun isValidEmailAndroid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}