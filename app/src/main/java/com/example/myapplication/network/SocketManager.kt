package com.example.myapplication.network

import io.socket.client.IO
import io.socket.client.Socket


object SocketManager {
    private var socket: Socket? = null

    fun conectar() {
        try {
            socket = IO.socket("http://g-maths-tr2.dam.inspedralbes.cat:25672")
            socket?.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun registrarEventoEstadoActualizado(onEstadoActualizado: (String) -> Unit) {
        socket?.on("estado_actualizado") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as String
                onEstadoActualizado(data) // Envía los datos a MainActivity
            }
        }
    }

    fun desconectar() {
        socket?.disconnect()
    }
}
