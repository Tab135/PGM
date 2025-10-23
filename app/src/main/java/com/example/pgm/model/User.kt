package com.example.pgm.model

data class User(
    val id: Int? = null,
    val email: String,
    val password: String,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val profileImage: String? = null,
    val role: String = "user",
    val tokens: Int = 100

)