package com.example.pgm.view.Comic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pgm.R
import com.example.pgm.Controller.ComicController
import com.github.barteksc.pdfviewer.PDFView
import java.io.File

class ComicViewerActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var comicController: ComicController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comic_viewer)

        pdfView = findViewById(R.id.pdfView)
        val comicId = intent.getIntExtra("comicId", -1)

        comicController = ComicController(this)
        val comic = comicController.getComicById(comicId)

        comic?.let {
            if (!it.localPath.isNullOrEmpty()) {
                val file = File(it.localPath)
                pdfView.fromFile(file).load()
            } else if (!it.remoteUrl.isNullOrEmpty()) {
                // Download PDF temporarily
                DownloadPdfTask(pdfView).execute(it.remoteUrl)
            }
        }
    }
}