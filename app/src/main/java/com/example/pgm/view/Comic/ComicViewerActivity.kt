package com.example.pgm.view.Comic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pgm.Controller.ChapterController
import com.example.pgm.R
import com.github.barteksc.pdfviewer.PDFView
import java.io.File

class ComicViewerActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var chapterController: ChapterController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comic_viewer)

        pdfView = findViewById(R.id.pdfView)
        val chapterId = intent.getIntExtra("chapterId", -1)

        chapterController = ChapterController(this)
        val chapter = chapterController.getChapterById(chapterId)

        chapter?.let {
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