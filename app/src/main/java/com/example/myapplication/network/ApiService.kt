package com.example.myapplication.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // Obtener todos los códigos de las partidas
    @GET("/api/partidas")
    fun obtenerPartidas(): Call<List<Partida>>

    // Obtener el estado de una partida
    @GET("/api/partida/estado/{codigo}")
    fun obtenerEstadoPartida(@Path("codigo") codigo: String): Call<Estado>

    // Actualizar el estado de una partida
    @POST("/api/partida/estado")
    fun actualizarEstadoPartida(@Body estadoRequest: EstadoRequest): Call<Void>

}
