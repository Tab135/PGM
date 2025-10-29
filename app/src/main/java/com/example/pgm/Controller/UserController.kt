package com.example.pgm.Controller

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.pgm.R
import com.example.pgm.model.Database.DatabaseManager
import com.example.pgm.model.User
import com.example.pgm.view.Comic.ComicListActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.appcompat.app.AlertDialog
class UserController(private val context: Context, private val view: View, private val userId: Int) {

    private val nameInput: EditText = view.findViewById(R.id.nameInput)
    private val emailInput: EditText = view.findViewById(R.id.emailInput)
    private val phoneInput: EditText = view.findViewById(R.id.phoneInput)
    private val addressInput: EditText = view.findViewById(R.id.addressInput)
    private val updateProfileBtn: Button = view.findViewById(R.id.updateProfileBtn)
    private val changePasswordBtn: Button = view.findViewById(R.id.changePasswordBtn)
    private val backButton: Button = view.findViewById(R.id.backButton)
    private val profileImage: ImageView = view.findViewById(R.id.profileImage)

    private val changeImageBtn: Button = view.findViewById(R.id.changeImageBtn)

    private val databaseHelper = DatabaseManager.getUserHelper(context)
    private var currentUser: User? = null

    private var selectedImage : Uri? = null;

    companion object {
        const val PICK_IMAGE_REQUEST = 100
    }
    var onImageSelectClick: (() -> Unit)? = null

    init {
        loadUserProfile()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        updateProfileBtn.setOnClickListener { updateProfile() }
        changePasswordBtn.setOnClickListener { changePassword() }
        backButton.setOnClickListener { goBackToComicList() }
        changeImageBtn.setOnClickListener { onImageSelectClick?.invoke() }
        profileImage.setOnClickListener { onImageSelectClick?.invoke() }

    }
    private fun changePassword() {
        // Create a dialog to get current and new passwords
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_change_password, null)
        val currentPasswordInput = dialogView.findViewById<EditText>(R.id.currentPasswordInput)
        val newPasswordInput = dialogView.findViewById<EditText>(R.id.newPasswordInput)
        val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.confirmPasswordInput)

        AlertDialog.Builder(context)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPass = currentPasswordInput.text.toString()
                val newPass = newPasswordInput.text.toString()
                val confirmPass = confirmPasswordInput.text.toString()

                if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                    showError("All fields are required")
                    return@setPositiveButton
                }

                if (newPass != confirmPass) {
                    showError("New passwords don't match")
                    return@setPositiveButton
                }

                if (newPass.length < 1) {
                    showError("Password must be at least 6 characters")
                    return@setPositiveButton
                }

                currentUser?.let { user ->
                    // Verify current password
                    if (databaseHelper.checkUser(user.email, currentPass)) {
                        if (databaseHelper.updateUserPassword(user.id ?: 0, newPass)) {
                            showSuccess("Password changed successfully")
                        } else {
                            showError("Failed to change password")
                        }
                    } else {
                        showError("Current password is incorrect")
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    //This func handle the image upload
    fun handleImageResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == android.app.Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedImage = uri
                // Display the selected image
                profileImage.setImageURI(uri)
            }
        }
    }
    private fun saveImageToInternalStorage(bitmap: Bitmap, userId: Int): String {
        val directory = File(context.filesDir, "profile_images")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, "profile_$userId.jpg")
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun loadUserProfile() {
        try {
            // Try multiple methods to get user data
            currentUser = getCurrentUserFromDatabase()

            currentUser?.let { user ->
                displayUserData(user)
                loadProfileImage(user.profileImage)
            } ?: run {
                showError("No user profile found")
                goBackToComicList()
            }
        } catch (e: Exception) {
            showError("Error loading profile: ${e.message}")
            e.printStackTrace()
        }
    }
    private fun getCurrentUserFromDatabase(): User? {
        if (userId > 0) {
            val user = databaseHelper.getUserById(userId)
            if (user != null) {
                return user
            }
        }
        return null
    }
    private fun loadProfileImage(imagePath: String?) {
        if (!imagePath.isNullOrEmpty()) {
            try {
                val file = File(imagePath)
                if (file.exists()) {
                    Glide.with(context)
                        .load(file)
                        .placeholder(R.drawable.ic_person_enhanced)
                        .error(R.drawable.ic_person_enhanced)
                        .into(profileImage)
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Set default image if no profile image exists
        profileImage.setImageResource(R.drawable.ic_person_enhanced)
    }



    private fun displayUserData(user: User) {
        nameInput.setText(user.name)
        emailInput.setText(user.email)
        phoneInput.setText(user.phone)
        addressInput.setText(user.address)

        emailInput.isEnabled = false
        Glide.with(context).load(user.profileImage).into(profileImage)
    }

    private fun updateProfile() {
        val name = nameInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val address = addressInput.text.toString().trim()

        if (name.isEmpty()) {
            showError("Name cannot be empty")
            return
        }

        currentUser?.let { user ->
            var imagePath = user.profileImage

            // If a new image was selected, save it
            selectedImage?.let { uri ->
                val bitmap = getBitmapFromUri(uri)
                bitmap?.let {
                    imagePath = saveImageToInternalStorage(it, user.id ?: 0)
                }
            }

            val updatedUser = user.copy(
                name = name,
                phone = phone,
                address = address,
                profileImage = imagePath
            )

            if (databaseHelper.updateUser(updatedUser)) {
                currentUser = updatedUser
                showSuccess("Profile updated successfully")
                selectedImage = null
            } else {
                showError("Failed to update profile")
            }
        } ?: showError("No user data available")
    }



    private fun goBackToComicList() {
        context.startActivity(Intent(context, ComicListActivity::class.java))
        if (context is androidx.appcompat.app.AppCompatActivity) {
            context.finish()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showInfo(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}