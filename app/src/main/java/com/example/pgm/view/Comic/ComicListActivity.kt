package com.example.pgm.view.Comic

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pgm.R
import com.example.pgm.Controller.ComicController
import com.example.pgm.view.Comic.ComicViewerActivity
import androidx.recyclerview.widget.GridLayoutManager
class ComicListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ComicAdapter
    private lateinit var comicController: ComicController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comic_list)

        recyclerView = findViewById(R.id.comicRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)


        comicController = ComicController(this)
        val comics = comicController.getAllComics()

        adapter = ComicAdapter(comics) { comic ->
            val intent = Intent(this, ChapterListActivity::class.java)
            intent.putExtra("comicId", comic.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }
}