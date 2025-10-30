package com.example.pgm.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.security.MessageDigest

/**
 * Manages PDF file caching with LRU eviction policy
 * Keeps track of access times and automatically removes old files when cache limit is reached
 */
class PdfCacheManager(private val context: Context) {
    
    companion object {
        private const val TAG = "PdfCacheManager"
        private const val CACHE_DIR_NAME = "pdf_cache"
        private const val MAX_CACHE_SIZE_MB = 500 // Maximum cache size in MB
        private const val MAX_CACHE_SIZE_BYTES = MAX_CACHE_SIZE_MB * 1024 * 1024L
        
        @Volatile
        private var instance: PdfCacheManager? = null
        
        fun getInstance(context: Context): PdfCacheManager {
            return instance ?: synchronized(this) {
                instance ?: PdfCacheManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR_NAME).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * Get cached PDF file for a given URL
     * Returns null if not cached
     */
    fun getCachedFile(url: String): File? {
        val cacheFile = getCacheFile(url)
        return if (cacheFile.exists()) {
            // Update access time
            cacheFile.setLastModified(System.currentTimeMillis())
            cacheFile
        } else {
            null
        }
    }
    
    /**
     * Get the File object for caching (whether it exists or not)
     */
    fun getCacheFile(url: String): File {
        val hash = url.md5()
        return File(cacheDir, "$hash.pdf")
    }

    /**
     * Get current cache size in bytes
     */
    fun getCacheSize(): Long {
        return try {
            cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating cache size: ${e.message}")
            0L
        }
    }

    /**
     * Evict old files if cache size exceeds limit
     */
    fun evictIfNeeded() {
        try {
            var currentSize = getCacheSize()
            
            if (currentSize <= MAX_CACHE_SIZE_BYTES) {
                return
            }
            
            Log.d(TAG, "Cache size ($currentSize bytes) exceeds limit. Starting eviction...")
            
            // Sort files by last modified time (oldest first)
            val files = cacheDir.listFiles()?.sortedBy { it.lastModified() } ?: return
            
            for (file in files) {
                if (currentSize <= MAX_CACHE_SIZE_BYTES * 0.8) { // Keep 80% of max
                    break
                }
                
                val fileSize = file.length()
                if (file.delete()) {
                    currentSize -= fileSize
                    Log.d(TAG, "Evicted ${file.name} (${fileSize / 1024}KB)")
                }
            }
            
            Log.d(TAG, "Eviction complete. New cache size: ${currentSize / 1024 / 1024}MB")
        } catch (e: Exception) {
            Log.e(TAG, "Error during eviction: ${e.message}", e)
        }
    }

    /**
     * Get number of cached files
     */
    fun getCacheFileCount(): Int {
        return cacheDir.listFiles()?.size ?: 0
    }

    private fun String.md5(): String {
        val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
