package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.network.ApiService
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegisterActivity : ComponentActivity() {

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://10.0.2.2:3000") // Cambia esta URL por la de tu servidor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                RegisterUserScreen(apiService)
            }
        }
    }
}

@Composable
fun RegisterUserScreen(apiService: ApiService) {
    var step by remember { mutableStateOf(1) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var generatedCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        when (step) {
            1 -> {
                // Etapa 1: Ingresar nombre, apellidos y correo
                Text(text = "Step 1: Register Info")
                BasicTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (firstName.isEmpty()) Text("First Name")
                        innerTextField()
                    }
                )
                BasicTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (lastName.isEmpty()) Text("Last Name")
                        innerTextField()
                    }
                )
                BasicTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (email.isEmpty()) Text("Email")
                        innerTextField()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    // Enviar el código de verificación
                    generatedCode = sendVerificationCode(email, context)
                    step = 2
                }) {
                    Text("Send Verification Code")
                }
            }

            2 -> {
                // Etapa 2: Validar código de verificación
                Text(text = "Step 2: Validate Code")
                BasicTextField(
                    value = verificationCode,
                    onValueChange = { verificationCode = it },
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (verificationCode.isEmpty()) Text("Verification Code")
                        innerTextField()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    if (verificationCode == generatedCode) {
                        step = 3
                    } else {
                        Toast.makeText(context, "Invalid code!", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Validate Code")
                }
            }

            3 -> {
                // Etapa 3: Crear contraseña
                Text(text = "Step 3: Create Password")
                BasicTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (password.isEmpty()) Text("Password")
                        innerTextField()
                    }
                )
                BasicTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (confirmPassword.isEmpty()) Text("Confirm Password")
                        innerTextField()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    if (password == confirmPassword) {
                        createAccount(apiService, firstName, lastName, email, password, context)
                        step = 4
                    } else {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Create Account")
                }
            }
        }
    }
}

fun sendVerificationCode(email: String, context: Context): String {
    val code = (100000..999999).random().toString()
    Toast.makeText(context, "Verification code: $code", Toast.LENGTH_SHORT).show() // Simula el envío
    return code
}

fun createAccount(
    apiService: ApiService,
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    context: Context
) {
    CoroutineScope(Dispatchers.IO).launch {
        val newUser = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "password" to password
        )

        try {
            val response = apiService.addUser(newUser).execute()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "User registered successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to register user: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
