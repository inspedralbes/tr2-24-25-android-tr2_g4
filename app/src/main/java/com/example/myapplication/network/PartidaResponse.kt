package com.example.myapplication.network

data class PartidaResponse(
    val message: String,
    val gameCode: String,
    val estado: String? = null
)
