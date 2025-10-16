package com.example.pgm.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.pgm.Controller.ComicController
import com.example.pgm.R
import com.example.pgm.model.Comic
import com.google.android.material.textfield.TextInputEditText

class AddEditComicActivity : AppCompatActivity() {
    private lateinit var etTitle: TextInputEditText
    private lateinit var etAuthor: TextInputEditText
    private lateinit var etPages: TextInputEditText
    private lateinit var ivCoverPreview: ImageView
    private lateinit var tvImageFileName: TextView
    private lateinit var btnSelectImage: Button
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private lateinit var comicController: ComicController
    private var selectedImageUri: Uri? = null
    private var selectedImagePath: String? = null
    private var isEditMode = false
    private var comicId = 0
    private var existingImagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_comic)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        checkEditMode()
        setupClickListeners()
    }

    private fun initViews() {
        etTitle = findViewById(R.id.etTitle)
        etAuthor = findViewById(R.id.etAuthor)
        etPages = findViewById(R.id.etPages)
        ivCoverPreview = findViewById(R.id.ivCoverPreview)
        tvImageFileName = findViewById(R.id.tvImageFileName)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)

        comicController = ComicController(this)
    }

    private fun checkEditMode() {
        comicId = intent.getIntExtra("comic_id", 0)
        if (comicId > 0) {
            isEditMode = true
            supportActionBar?.title = "Edit Comic"
            loadComicData()
        } else {
            supportActionBar?.title = "Add New Comic"
        }
    }

    private fun loadComicData() {
        etTitle.setText(intent.getStringExtra("comic_title"))
        etAuthor.setText(intent.getStringExtra("comic_author"))
        intent.getIntExtra("comic_pages", 0).let {
            if (it > 0) etPages.setText(it.toString())
        }

        existingImagePath = intent.getStringExtra("comic_image_url")
        existingImagePath?.let { imagePath ->
            selectedImagePath = imagePath
            tvImageFileName.text = "Current: ${imagePath.substringAfterLast("/")}"

            // Load existing image
            Glide.with(this)
                .load(imagePath)
                .placeholder(R.drawable.ic_comic_placeholder)
                .error(R.drawable.ic_comic_placeholder)
                .into(ivCoverPreview)
        }
    }

    private fun setupClickListeners() {
        btnSelectImage.setOnClickListener {
            selectImageFile()
        }

        btnSave.setOnClickListener {
            saveComic()
        }

        btnCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun selectImageFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(
            Intent.createChooser(intent, "Select Cover Image"),
            IMAGE_SELECT_REQUEST
        )
    }

    private fun saveComic() {
        val title = etTitle.text.toString().trim()
        val author = etAuthor.text.toString().trim()
        val pagesText = etPages.text.toString().trim()

        if (title.isEmpty()) {
            etTitle.error = "Title is required"
            return
        }

        // Handle image file if selected
        if (selectedImageUri != null) {
            val fileName = "${System.currentTimeMillis()}_${getFileName(selectedImageUri!!)}"
            selectedImagePath = comicController.saveImageToInternalStorage(selectedImageUri!!, fileName)

            if (selectedImagePath == null) {
                Toast.makeText(this, "Failed to save cover image", Toast.LENGTH_SHORT).show()
                return
            }

            // Delete old image if updating and new image selected
            if (isEditMode && existingImagePath != null && existingImagePath != selectedImagePath) {
                comicController.deleteComicFile(existingImagePath!!)
            }
        }

        // Validate that we have an image
        if (selectedImagePath == null) {
            Toast.makeText(this, "Please select a cover image", Toast.LENGTH_SHORT).show()
            return
        }

        val pages = if (pagesText.isNotEmpty()) pagesText.toIntOrNull() ?: 0 else 0

        val comic = Comic(
            id = comicId,
            title = title,
            author = author.ifEmpty { null },
            pages = if (pages > 0) pages else null,
            imageUrl = selectedImagePath
        )

        val success = if (isEditMode) {
            comicController.updateComic(comic)
        } else {
            comicController.addComic(comic)
        }

        if (success) {
            Toast.makeText(
                this,
                if (isEditMode) "Comic updated successfully" else "Comic added successfully",
                Toast.LENGTH_SHORT
            ).show()
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            Toast.makeText(
                this,
                if (isEditMode) "Failed to update comic" else "Failed to add comic",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "cover.jpg"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_SELECT_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                tvImageFileName.text = "Selected: ${getFileName(uri)}"

                // Preview selected image
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_comic_placeholder)
                    .error(R.drawable.ic_comic_placeholder)
                    .into(ivCoverPreview)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val IMAGE_SELECT_REQUEST = 2001
    }
}