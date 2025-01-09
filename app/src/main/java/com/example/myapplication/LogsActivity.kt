package com.example.myapplication


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.ComponentActivity
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.SocketManager
import io.socket.emitter.Emitter
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogsActivity : ComponentActivity() {
    private val socket = SocketManager.getSocket()
    private lateinit var logsTextView: TextView
    private lateinit var titleTextView: TextView
    private var scriptName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)
        titleTextView = findViewById(R.id.titulo)
        logsTextView = findViewById(R.id.logsTextView)
        scriptName = intent.getStringExtra("scriptName")

        titleTextView.text = "Logs de $scriptName"
        val initialLogs = intent.getStringExtra("logs") ?: "Sin logs disponibles"
        logsTextView.text = initialLogs
        fetchLogs()
        setupSocketListeners()
        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }
    private fun fetchLogs() {
        RetrofitClient.apiService.getScriptLogsActividad(scriptName ?: "").enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    val logs = response.body()?.get("logs") ?: "No se encontraron logs"
                    logsTextView.text = logs
                } else {
                    Log.e("LogsActivity", "Error al obtener los logs desde la API")
                    logsTextView.text = "Error al obtener los logs desde la API."
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("LogsActivity", "Error en la llamada: ${t.message}")
                logsTextView.text = "Error de red al obtener los logs."
            }
        })
    }

    private fun setupSocketListeners() {
        socket.on("log_stdout", onLogStdout)
        if (!socket.connected()) {
            socket.connect()
        }
    }
    private val onLogStdout = Emitter.Listener { args ->
        val data = args[0] as? JSONObject
        data?.let {
            val receivedScriptName = it.getString("scriptName")
            val log = it.getString("log")
            if (receivedScriptName == scriptName) {
                runOnUiThread {
                    logsTextView.append("\nSTDOUT: $log")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.off("log_stdout", onLogStdout)
        socket.disconnect()
    }
}
