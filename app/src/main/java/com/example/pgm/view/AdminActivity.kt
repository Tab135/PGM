package com.example.pgm.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.*
import com.example.pgm.R
import com.example.pgm.model.Comic
import com.example.pgm.view.Comic.ComicViewerActivity
import androidx.appcompat.app.AlertDialog
import com.example.pgm.Controller.ChapterController
import com.example.pgm.Controller.ComicController
import com.example.pgm.view.Admin.AdminComicAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AdminActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminComicAdapter
    private lateinit var comicController: ComicController
    private lateinit var chapterController: ChapterController  // THÊM DÒNG NÀY
    private lateinit var fabAdd: FloatingActionButton
    private var comicsList: MutableList<Comic> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        initViews()
        loadComics()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.adminRecyclerView)
        fabAdd = findViewById(R.id.fabAddComic)
        recyclerView.layoutManager = LinearLayoutManager(this)

        comicController = ComicController(this)
        chapterController = ChapterController(this)  // THÊM DÒNG NÀY

        fabAdd.setOnClickListener {
            val intent = Intent(this, AddEditComicActivity::class.java)
            startActivityForResult(intent, ADD_COMIC_REQUEST)
        }
    }

    private fun loadComics() {
        comicsList.clear()
        comicsList.addAll(comicController.getAllComics())

        adapter = AdminComicAdapter(
            comicsList,
            chapterController,  // TRUYỀN CHAPTER CONTROLLER VÀO
            onEdit = { comic ->
                val intent = Intent(this, AddEditComicActivity::class.java).apply {
                    putExtra("comic_id", comic.id)
                    putExtra("comic_title", comic.title)
                    putExtra("comic_author", comic.author)
                    putExtra("comic_pages", comic.pages)
                    putExtra("comic_image_url", comic.imageUrl)
                }
                startActivityForResult(intent, EDIT_COMIC_REQUEST)
            },
            onDelete = { comic ->
                showDeleteConfirmation(comic)
            },
            onView = { comic ->
                val intent = Intent(this, ComicViewerActivity::class.java).apply {
                    putExtra("comicId", comic.id)
                }
                startActivity(intent)
            },
            onManageChapters = { comic ->
                val intent = Intent(this, ChapterManagementActivity::class.java).apply {
                    putExtra("comicId", comic.id)
                }
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter
    }

    private fun showDeleteConfirmation(comic: Comic) {
        AlertDialog.Builder(this)
            .setTitle("Delete Comic")
            .setMessage("Are you sure you want to delete '${comic.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                deleteComic(comic)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteComic(comic: Comic) {
        if (comicController.deleteComic(comic.id)) {
            comicsList.remove(comic)
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Comic deleted successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to delete comic", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.admin_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchComics(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    loadComics()
                } else {
                    searchComics(newText)
                }
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun searchComics(query: String) {
        val searchResults = comicController.searchComics(query)
        comicsList.clear()
        comicsList.addAll(searchResults)
        adapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                ADD_COMIC_REQUEST, EDIT_COMIC_REQUEST -> {
                    loadComics()
                }
            }
        }
    }

    companion object {
        const val ADD_COMIC_REQUEST = 1001
        const val EDIT_COMIC_REQUEST = 1002
    }
}