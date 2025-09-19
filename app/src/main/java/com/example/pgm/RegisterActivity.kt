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

class RegisterActivity : AppCompatActivity() {
    private lateinit var userEmail : EditText
    private lateinit var password : EditText
    private lateinit var name : EditText
    private lateinit var registerBtn : Button
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var loginBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        databaseHelper = DatabaseHelper(this)
        val db = databaseHelper.readableDatabase
        db.close()

        }
    fun Register(view : View){
        userEmail = view.rootView.findViewById<EditText>(R.id.emailInput)
        password = view.rootView.findViewById<EditText>(R.id.passwordInput)
        name = view.rootView.findViewById<EditText>(R.id.nameInput)
        registerBtn = view.rootView.findViewById<Button>(R.id.RegisterBtn)
        databaseHelper = DatabaseHelper(this)
        registerBtn.setOnClickListener {
            val email = userEmail.text.toString()
            val pass = password.text.toString()
            val userName = name.text.toString()
            if (email.isEmpty() || pass.isEmpty() || userName.isEmpty()) {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
                if(!databaseHelper.CheckEmail(email)) {
                    val success = databaseHelper.addUser(email, pass, userName)
                    if (success) {
                        startActivity(Intent(this, LoginActivity::class.java))
                        Toast.makeText(this, "Register success", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                }else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
        }
            }
        }

    }
