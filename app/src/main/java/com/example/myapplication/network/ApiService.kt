package com.example.myapplication.network

import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // Login de usuario
    @POST("/login")
    fun login(@Body credentials: Map<String, String>): Call<Map<String, Any>>

    // Crear un nuevo usuario
    @POST("/addUser")
    fun addUser(@Body newUser: Map<String, Any>): Call<Map<String, Any>>

    // Obtener o crear código de partida
    @GET("/game-code")
    fun getGameCode(@Query("codigo") codigo: String?): Call<Map<String, String>>

    // Obtener alumnos de una partida
    @GET("/alumnos")
    fun getAlumnosFromPartida(@Query("codigo") codigo: String): Call<List<Map<String, Any>>>

    // Actualizar partida con un nuevo alumno
    @POST("/update-partida")
    fun updatePartida(@Body partidaData: Map<String, Any>): Call<Map<String, Any>>


}
