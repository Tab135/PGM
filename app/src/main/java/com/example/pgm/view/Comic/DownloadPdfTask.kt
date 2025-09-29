package com.example.pgm.view.Comic

import android.os.AsyncTask
import com.github.barteksc.pdfviewer.PDFView
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadPdfTask(private val pdfView: PDFView) : AsyncTask<String, Void, InputStream?>() {
    override fun doInBackground(vararg params: String?): InputStream? {
        return try {
            val url = URL(params[0])
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()
            BufferedInputStream(connection.inputStream)
        } catch (e: Exception) {
            null
        }
    }

    override fun onPostExecute(inputStream: InputStream?) {
        inputStream?.let { pdfView.fromStream(it).load() }
    }
}