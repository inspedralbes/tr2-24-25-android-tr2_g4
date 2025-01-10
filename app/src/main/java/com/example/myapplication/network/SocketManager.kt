package com.example.myapplication.network

import android.widget.Toast
import androidx.test.core.app.ApplicationProvider
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URI

object SocketManager {

    private var socket: Socket? = null

    fun conectar() {
        try {
            socket = IO.socket("http://10.0.2.2:3000")
            socket?.connect()

            socket?.on("estado_actualizado") { args ->
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(ApplicationProvider.getApplicationContext(), "Error al conectar con el servidor", Toast.LENGTH_LONG).show()
        }
    }


    fun desconectar() {
        socket?.disconnect()
    }
}
