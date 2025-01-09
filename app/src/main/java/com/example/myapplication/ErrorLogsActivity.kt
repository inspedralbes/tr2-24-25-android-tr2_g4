package com.example.myapplication


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.SocketManager
import io.socket.emitter.Emitter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.json.JSONObject

class ErrorLogsActivity : ComponentActivity() {
    private val socket = SocketManager.getSocket()
    private lateinit var logsTextView: TextView
    private lateinit var titleTextView: TextView
    private var scriptName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error_logs)
        titleTextView = findViewById(R.id.titleTextView)
        logsTextView = findViewById(R.id.logsTextView)
        scriptName = intent.getStringExtra("scriptName")
        titleTextView.text = "Logs de Error para: $scriptName"
        fetchErrorLogs()
        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        setupSocketListeners()
    }

    private fun fetchErrorLogs() {
        scriptName?.let {
            RetrofitClient.apiService.getScriptErrorLogs(it).enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                    if (response.isSuccessful) {
                        val errorLogs = response.body()?.get("logs") ?: "No se encontraron logs de error."
                        logsTextView.text = errorLogs
                    } else {
                        Toast.makeText(applicationContext, "Error al obtener los logs de error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    Toast.makeText(applicationContext, "Error de red al obtener los logs de error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun setupSocketListeners() {
        socket.on("log_stderr", onLogStderr)
        if (!socket.connected()) {
            socket.connect()
        }
    }
    private val onLogStderr = Emitter.Listener { args ->
        val data = args[0] as? JSONObject
        data?.let {
            val receivedScriptName = it.getString("scriptName")
            val log = it.getString("errorLog")

            if (receivedScriptName == scriptName) {
                runOnUiThread {
                    logsTextView.append("\nSTDERR: $log")
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        socket.off("log_stderr", onLogStderr)
    }
}
