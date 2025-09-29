package com.example.pgm.model

data class Comic(
    val id: Int = 0,
    val title: String,
    val author: String? = null,
    val pages: Int? = null,
    val localPath: String? = null,
    val remoteUrl: String? = null,
    val imageUrl: String? = null
)