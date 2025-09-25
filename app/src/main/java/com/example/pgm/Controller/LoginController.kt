package com.example.pgm.Controller

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.pgm.R
import com.example.pgm.view.MainActivity
import com.example.pgm.model.Database.UserDatabaseHelper
import com.example.pgm.view.LoginActivity
import com.example.pgm.view.RegisterActivity

class LoginController(private val context: Context, private val view: View) {

    private val userEmail: EditText = view.findViewById(R.id.emailInput)
    private val password: EditText = view.findViewById(R.id.passwordInput)
    private val btnLogin: Button = view.findViewById(R.id.loginBtn)
    private val btnRegister: Button = view.findViewById(R.id.RegisterBtn)
    private val databaseHelper = UserDatabaseHelper(context)

    init {
        btnLogin.setOnClickListener { loginUser() }
        btnRegister.setOnClickListener { goToRegister() }
    }

    fun loginUser() {
        val email = userEmail.text.toString()
        val pass = password.text.toString()

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(context, "The email or password must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        if (databaseHelper.checkUser(email, pass)) {
            context.startActivity(Intent(context, MainActivity::class.java))
            Toast.makeText(context, "Login successfully", Toast.LENGTH_SHORT).show()
            (context as? LoginActivity)?.finish()
        } else {
            Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
        }
    }

    fun goToRegister() {
        context.startActivity(Intent(context, RegisterActivity::class.java))
    }
}