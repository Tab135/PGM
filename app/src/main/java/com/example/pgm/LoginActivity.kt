package com.example.pgm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
private lateinit var userEmail : EditText
private lateinit var password : EditText
private lateinit var btnLogin : Button
private lateinit var btnRegister : Button

private lateinit var databaseHelper : DatabaseHelper

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        databaseHelper = DatabaseHelper(this)
        val db = databaseHelper.readableDatabase
        db.close()
        setContentView(R.layout.activity_login)
        userEmail = findViewById<EditText>(R.id.emailInput)
        password = findViewById<EditText>(R.id.passwordInput)
        btnLogin = findViewById<Button>(R.id.loginBtn)
        btnRegister = findViewById<Button>(R.id.RegisterBtn)

        btnLogin.setOnClickListener {
            loginUser()

        }
        btnRegister.setOnClickListener {
            goToRegister()

        }
    }

    fun loginUser() {
            val email = userEmail.text.toString()
            val pass = password.text.toString()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "The email or password must be filled", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            if (databaseHelper.checkUser(email, pass)) {
                startActivity(Intent(this, MainActivity::class.java))
                Toast.makeText(this, "Login successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }

    fun goToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

        fun Login(view :View) {
            loginUser()
        }
        fun Register(view : View){
            goToRegister()
        }
    }








