package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.network.ApiClient
import com.example.myapplication.network.Estado
import com.example.myapplication.network.EstadoRequest
import com.example.myapplication.network.Partida
import com.example.myapplication.network.SocketManager
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {

    private val socketManager = SocketManager
    private var partidas by mutableStateOf<List<Partida>>(emptyList()) // Este es el estado reactivo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }

        // Conectar al socket y registrar el evento de estado actualizado
        socketManager.conectar()
        socketManager.registrarEventoEstadoActualizado { data ->
            runOnUiThread {
                actualizarPartidaDesdeSocket(data)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Desconectar el socket al destruir la actividad
        socketManager.desconectar()
    }

    @Composable
    fun MainScreen() {
        // Fetch inicial de partidas
        fetchPartidas { fetchedPartidas ->
            partidas = fetchedPartidas
        }

        // Renderizar la lista de partidas
        PartidaList(partidas = partidas)
    }

    private fun actualizarPartidaDesdeSocket(data: String) {
        val updatedPartida = parsePartida(data)
        partidas = partidas.map { partida ->
            if (partida.codigo == updatedPartida.codigo) updatedPartida else partida
        }
    }

    private fun fetchPartidas(onResult: (List<Partida>) -> Unit) {
        val apiService = ApiClient.apiService
        val call = apiService.obtenerPartidas()

        call.enqueue(object : Callback<List<Partida>> {
            override fun onResponse(call: Call<List<Partida>>, response: Response<List<Partida>>) {
                if (response.isSuccessful) {
                    val partidas = response.body() ?: emptyList()
                    fetchEstadosForPartidas(partidas, onResult)
                } else {
                    showToast("Error al obtener las partidas: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<Partida>>, t: Throwable) {
                showToast("Error de conexión: ${t.message}")
            }
        })
    }

    private fun fetchEstadosForPartidas(partidas: List<Partida>, onResult: (List<Partida>) -> Unit) {
        val apiService = ApiClient.apiService
        val estadosMap = mutableMapOf<String, String>()

        partidas.forEach { partida ->
            val call = apiService.obtenerEstadoPartida(partida.codigo)
            call.enqueue(object : Callback<Estado> {
                override fun onResponse(call: Call<Estado>, response: Response<Estado>) {
                    if (response.isSuccessful) {
                        estadosMap[partida.codigo] = response.body()?.estado ?: "Desconocido"
                    } else {
                        estadosMap[partida.codigo] = "Error"
                    }

                    if (estadosMap.size == partidas.size) {
                        val updatedPartidas = partidas.map { it.copy(estado = estadosMap[it.codigo]) }
                        onResult(updatedPartidas)
                    }
                }

                override fun onFailure(call: Call<Estado>, t: Throwable) {
                    estadosMap[partida.codigo] = "Error"
                    if (estadosMap.size == partidas.size) {
                        val updatedPartidas = partidas.map { it.copy(estado = estadosMap[it.codigo]) }
                        onResult(updatedPartidas)
                    }
                }
            })
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @Composable
    fun PartidaList(partidas: List<Partida>) {
        val displayedPartidas by remember(partidas) { mutableStateOf(partidas) }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(displayedPartidas, key = { it.codigo }) { partida ->
                PartidaItem(partida = partida)
            }
        }
    }


    @Composable
    fun PartidaItem(partida: Partida) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "Código: ${partida.codigo}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Estado: ${partida.estado ?: "Desconocido"}", style = MaterialTheme.typography.bodySmall)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = {
                    postEstadoPartida(EstadoRequest(partida.codigo, "En Partida"))
                    // Actualiza el estado de la partida localmente
                    actualizarEstadoLocal(partida.codigo, "En Partida")
                }) {
                    Text("En Partida")
                }
                Button(onClick = {
                    postEstadoPartida(EstadoRequest(partida.codigo, "Pausa"))
                    // Actualiza el estado de la partida localmente
                    actualizarEstadoLocal(partida.codigo, "Pausa")
                }) {
                    Text("Pausa")
                }
                Button(onClick = {
                    postEstadoPartida(EstadoRequest(partida.codigo, "Terminada"))
                    // Actualiza el estado de la partida localmente
                    actualizarEstadoLocal(partida.codigo, "Terminada")
                }) {
                    Text("Terminada")
                }
            }
        }
    }

    private fun actualizarEstadoLocal(codigo: String, nuevoEstado: String) {
        val index = partidas.indexOfFirst { it.codigo == codigo }
        if (index != -1) {
            val updatedPartidas = partidas.toMutableList()
            updatedPartidas[index] = updatedPartidas[index].copy(estado = nuevoEstado)
            partidas = updatedPartidas
        }
    }

    private fun postEstadoPartida(estadoRequest: EstadoRequest) {
        val apiService = ApiClient.apiService
        val call = apiService.actualizarEstadoPartida(estadoRequest)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showToast("Estado actualizado correctamente")
                } else {
                    showToast("Error al actualizar el estado: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Error de conexión: ${t.message}")
            }
        })
    }

    private fun parsePartida(data: String): Partida {
        return Gson().fromJson(data, Partida::class.java)
    }
}
