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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }

    @Composable
    fun MainScreen() {
        var partidas by remember { mutableStateOf<List<Partida>>(emptyList()) }

        // Fetch partidas when the composable is first loaded
        fetchPartidas { fetchedPartidas ->
            partidas = fetchedPartidas
        }

        PartidaList(partidas = partidas)
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
        val updatedPartidas = mutableListOf<Partida>()

        partidas.forEach { partida ->
            val call = apiService.obtenerEstadoPartida(partida.codigo)
            call.enqueue(object : Callback<Estado> {
                override fun onResponse(call: Call<Estado>, response: Response<Estado>) {
                    if (response.isSuccessful) {
                        val estado = response.body()?.estado ?: "Desconocido"
                        updatedPartidas.add(partida.copy(estado = estado))

                        if (updatedPartidas.size == partidas.size) {
                            onResult(updatedPartidas)
                        }
                    } else {
                        showToast("Error al obtener el estado para el código ${partida.codigo}")
                    }
                }

                override fun onFailure(call: Call<Estado>, t: Throwable) {
                    showToast("Error de conexión al obtener estado: ${t.message}")
                }
            })
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @Composable
    fun PartidaList(partidas: List<Partida>) {
        if (partidas.isEmpty()) {
            Text(
                text = "Cargando partidas...",
                modifier = Modifier.fillMaxSize().padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
            Text(text = "Código: ${partida.codigo}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Estado: ${partida.estado ?: "Desconocido"}", style = MaterialTheme.typography.bodySmall)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = {
                    postEstadoPartida(EstadoRequest(partida.codigo, "En Partida"))
                }) {
                    Text("En Partida")
                }
                Button(onClick = {
                    postEstadoPartida(EstadoRequest(partida.codigo, "Pausa"))
                }) {
                    Text("Pausa")
                }
                Button(onClick = {
                    postEstadoPartida(EstadoRequest(partida.codigo, "Terminada"))
                }) {
                    Text("Terminada")
                }
            }
        }
    }
}
