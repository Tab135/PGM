package com.example.pgm.model

data class Chapter(
    val id: Int = 0,
    val comicId: Int,
    val chapterNumber: Int,
    val title: String,
    val thumbnailUrl: String? = null,
    val releaseDate: String? = null,
    val likeCount: Int = 0,
    val isLocked: Boolean = false,
    val cost: Int = 0, // Cost in coins if locked
    val freeDays: Int = 0, // Days until free
    val pages: Int? = null,
    val localPath: String? = null,
    val remoteUrl: String? = null
)