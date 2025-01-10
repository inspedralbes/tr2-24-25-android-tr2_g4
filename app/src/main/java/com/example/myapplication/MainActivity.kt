package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import com.example.myapplication.network.ApiClient
import com.example.myapplication.network.Estado
import com.example.myapplication.network.EstadoRequest
import com.example.myapplication.network.Partida
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {

    private var partidas by mutableStateOf<List<Partida>>(listOf())
    private var codigo by mutableStateOf("")
    private var estado by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }

        // Obtener partidas desde la API cuando se crea la actividad
        obtenerPartidas()
    }

    private fun obtenerPartidas() {
        val apiService = ApiClient.apiService
        val call = apiService.obtenerPartidas()

        call.enqueue(object : Callback<List<Partida>> {
            override fun onResponse(call: Call<List<Partida>>, response: Response<List<Partida>>) {
                if (response.isSuccessful) {
                    val partidas = response.body()
                    partidas?.let {
                        // Para cada partida, obtener el estado
                        obtenerEstadoDePartidas(it)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Error al obtener las partidas: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Partida>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Obtener estado para cada partida
    private fun obtenerEstadoDePartidas(partidas: List<Partida>) {
        val apiService = ApiClient.apiService
        val partidasConEstado = mutableListOf<Partida>()
        val callCount = partidas.size
        var completedCalls = 0

        for (partida in partidas) {
            val call = apiService.obtenerEstadoPartida(partida.codigo)

            call.enqueue(object : Callback<Estado> {
                override fun onResponse(call: Call<Estado>, response: Response<Estado>) {
                    if (response.isSuccessful) {
                        val estado = response.body()
                        estado?.let {
                            // Actualizar el estado de la partida
                            partidasConEstado.add(partida.copy(estado = it.estado))
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Error al obtener el estado de la partida", Toast.LENGTH_SHORT).show()
                    }
                    completedCalls++

                    // Cuando todas las respuestas se hayan completado, actualizamos el estado
                    if (completedCalls == callCount) {
                        this@MainActivity.partidas = partidasConEstado
                    }
                }

                override fun onFailure(call: Call<Estado>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                    completedCalls++

                    // Cuando todas las respuestas se hayan completado, actualizamos el estado
                    if (completedCalls == callCount) {
                        this@MainActivity.partidas = partidasConEstado
                    }
                }
            })
        }
    }

    private fun actualizarEstadoPartida(codigo: String, estado: String) {
        val estadoRequest = EstadoRequest(codigo, estado)
        val apiService = ApiClient.apiService
        val call = apiService.actualizarEstadoPartida(estadoRequest)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Estado actualizado", Toast.LENGTH_SHORT).show()
                    // Aquí actualizamos el estado de la partida localmente en la lista
                    actualizarPartidaEnLista(codigo, estado)
                } else {
                    Toast.makeText(this@MainActivity, "Error al actualizar el estado", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun actualizarPartidaEnLista(codigo: String, nuevoEstado: String) {
        partidas = partidas.map { partida ->
            if (partida.codigo == codigo) {
                partida.copy(estado = nuevoEstado)
            } else {
                partida
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mostrar las partidas
            PartidaList(partidas = partidas)

            Spacer(modifier = Modifier.height(16.dp))

            // Campo para el código de la partida
            TextField(
                value = codigo,
                onValueChange = { codigo = it },
                label = { Text("Código de la partida") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo para el estado de la partida
            TextField(
                value = estado,
                onValueChange = { estado = it },
                label = { Text("Estado de la partida") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para actualizar el estado
            Button(
                onClick = {
                    if (codigo.isNotEmpty() && estado.isNotEmpty()) {
                        actualizarEstadoPartida(codigo, estado)
                    } else {
                        Toast.makeText(this@MainActivity, "Por favor ingresa un código y estado", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Actualizar Estado")
            }
        }
    }

    @Composable
    fun PartidaList(partidas: List<Partida>) {
        if (partidas.isEmpty()) {
            Text(text = "Cargando partidas...")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(partidas) { partida ->
                    PartidaItem(partida = partida)
                }
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
            Text("Código: ${partida.codigo}")
            Text("Estado: ${partida.estado}")
        }
    }
}
