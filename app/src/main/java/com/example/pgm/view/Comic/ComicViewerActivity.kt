package com.example.pgm.view.Comic

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pgm.Controller.ChapterController
import com.example.pgm.R
import com.example.pgm.utils.PdfCacheManager
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnErrorListener
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ComicViewerActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var chapterController: ChapterController
    private lateinit var loadingOverlay: View
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var loadingMessage: TextView
    private lateinit var loadingPercentText: TextView
    private lateinit var pageInfoText: TextView
    private var currentPage = 0
    private var totalPages = 0
    
    // Cache manager for PDFs
    private lateinit var cacheManager: PdfCacheManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comic_viewer)

        pdfView = findViewById(R.id.pdfView)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        loadingMessage = findViewById(R.id.loadingMessage)
        loadingPercentText = findViewById(R.id.loadingPercentText)
        pageInfoText = findViewById(R.id.pageInfoText)
        
        cacheManager = PdfCacheManager.getInstance(this)
        
        val chapterId = intent.getIntExtra("chapterId", -1)

        chapterController = ChapterController(this)
        val chapter = chapterController.getChapterById(chapterId)

        chapter?.let {
            if (!it.localPath.isNullOrEmpty()) {
                loadLocalPdf(it.localPath)
            } else if (!it.remoteUrl.isNullOrEmpty()) {
                loadRemotePdf(it.remoteUrl)
            } else {
                showError("No PDF source available")
            }
        } ?: showError("Chapter not found")
    }

    private fun loadLocalPdf(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            showError("PDF file not found")
            return
        }
        
        showLoading("Loading comic...", 0)

        pdfView.fromFile(file)
            .defaultPage(0)
            .onPageChange(onPageChangeListener)
            .onLoad(onLoadCompleteListener)
            .onError(onErrorListener)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .pageSnap(true)
            .autoSpacing(true)
            .pageFling(true)
            .spacing(10)
            .pageFitPolicy(FitPolicy.BOTH)
            .scrollHandle(DefaultScrollHandle(this))
            .enableAnnotationRendering(false) // Disable annotations for faster rendering
            .enableAntialiasing(true)
            .spacing(0) // Remove spacing for faster rendering
            .load()
    }

    private fun loadRemotePdf(url: String) {
        showLoading("Fetching comic...", 0)
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Check if PDF is already cached
                val cachedFile = cacheManager.getCachedFile(url)
                
                if (cachedFile != null) {
                    android.util.Log.d("ComicViewer", "Loading from cache: ${cachedFile.absolutePath}")
                    updateProgress("Loading comic...", 100)
                    loadCachedPdf(cachedFile)
                } else {
                    // Download and cache the PDF
                    val cacheFile = cacheManager.getCacheFile(url)
                    downloadAndCachePdf(url, cacheFile)
                }
            } catch (e: Exception) {
                android.util.Log.e("ComicViewer", "Load error: ${e.message}", e)
                showError("Error loading comic: ${e.message}")
            }
        }
    }
    
    private suspend fun downloadAndCachePdf(url: String, cacheFile: File) {
        showLoading("Fetching comic...", 0)
        
        val startTime = System.currentTimeMillis()
        val success = withContext(Dispatchers.IO) {
            downloadPdfToFile(url, cacheFile)
        }
        
        val downloadTime = (System.currentTimeMillis() - startTime) / 1000
        android.util.Log.d("ComicViewer", "Download completed in ${downloadTime}s")
        
        if (success) {
            // Evict old cache if needed
            withContext(Dispatchers.IO) {
                cacheManager.evictIfNeeded()
            }
            
            updateProgress("Rendering comic...", 100)
            loadCachedPdf(cacheFile)
        } else {
            showError("Failed to fetch comic. Please check your connection and try again.")
        }
    }
    
    private fun loadCachedPdf(file: File) {
        pdfView.fromFile(file)
            .defaultPage(0)
            .onPageChange(onPageChangeListener)
            .onLoad(onLoadCompleteListener)
            .onError(onErrorListener)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .pageSnap(true)
            .autoSpacing(true)
            .pageFling(true)
            .spacing(10)
            .pageFitPolicy(FitPolicy.BOTH)
            .scrollHandle(DefaultScrollHandle(this))
            .enableAnnotationRendering(false) // Disable annotations for faster rendering
            .enableAntialiasing(true)
            .spacing(0) // Remove spacing for faster rendering
            .nightMode(false)
            .load()
    }
    
    private fun downloadPdfToFile(urlString: String, outputFile: File): Boolean {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 60000 // 60 seconds
            connection.readTimeout = 120000 // 2 minutes
            connection.doInput = true
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                android.util.Log.e("ComicViewer", "HTTP Error: ${connection.responseCode}")
                return false
            }

            val contentLength = connection.contentLength
            android.util.Log.d("ComicViewer", "Downloading PDF: ${contentLength / 1024} KB")

            // Download to temporary file first
            val tempFile = File(outputFile.parent, "${outputFile.name}.tmp")
            
            BufferedInputStream(connection.inputStream, 8192).use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        
                        // Update progress on UI thread
                        if (contentLength > 0) {
                            val progress = (totalBytesRead * 100 / contentLength).toInt()
                            runOnUiThread {
                                updateProgress("Fetching comic...", progress)
                            }
                        }
                    }
                }
            }
            
            // Move temp file to final location
            tempFile.renameTo(outputFile)
            true
        } catch (e: Exception) {
            android.util.Log.e("ComicViewer", "Download error: ${e.message}", e)
            false
        }
    }

    private val onLoadCompleteListener = OnLoadCompleteListener { nbPages ->
        hideLoading()
        totalPages = nbPages
        pageInfoText.text = "1 / $nbPages"
        pageInfoText.visibility = View.VISIBLE
    }

    private val onPageChangeListener = OnPageChangeListener { page, pageCount ->
        currentPage = page + 1
        totalPages = pageCount
        pageInfoText.text = "$currentPage / $totalPages"
    }

    private val onErrorListener = OnErrorListener { t ->
        showError("Error loading comic: ${t.message}")
    }

    private fun showLoading(message: String, progress: Int) {
        loadingOverlay.visibility = View.VISIBLE
        loadingMessage.text = message
        loadingProgressBar.progress = progress
        loadingPercentText.text = "$progress%"
    }
    
    private fun updateProgress(message: String, progress: Int) {
        loadingMessage.text = message
        loadingProgressBar.progress = progress
        loadingPercentText.text = "$progress%"
    }

    private fun hideLoading() {
        loadingOverlay.visibility = View.GONE
    }

    private fun showError(message: String) {
        hideLoading()
        pageInfoText.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up PDF resources
        pdfView.recycle()
    }
}