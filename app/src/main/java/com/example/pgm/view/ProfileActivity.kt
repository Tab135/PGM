package com.example.pgm.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pgm.Controller.UserController
import com.example.pgm.R
import com.example.pgm.utils.SessionManager

class ProfileActivity : AppCompatActivity() {
    private lateinit var userController: UserController
    private lateinit var sessionManager: SessionManager
    private lateinit var logoutButton: Button

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { intent ->
                userController.handleImageResult(UserController.PICK_IMAGE_REQUEST, result.resultCode, intent)
            }
        }
    }

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            selectImage()
        } else {
            Toast.makeText(this, "Permission denied to access photos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileContainer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SessionManager
        sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId()
        android.util.Log.d("ProfileActivity", "Received User ID: $userId")

        initializeController(userId)

        // Setup Logout button
        setupLogoutButton()
    }

    private fun initializeController(userId : Int) {
        userController = UserController(this, findViewById(R.id.profileContainer), userId)

        // Set up the image selection callback after controller is initialized
        userController.onImageSelectClick = {
            checkPermissionAndSelectImage()
        }
    }

    private fun setupLogoutButton() {
        logoutButton = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        // Clear session
        sessionManager.clearSession()

        // Clear SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Navigate to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun checkPermissionAndSelectImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                selectImage()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickImage.launch(intent)
    }
}