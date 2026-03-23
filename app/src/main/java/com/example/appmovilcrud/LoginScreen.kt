package com.example.appmovilcrud

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

object Session {
    var token: String = ""
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Iniciar Sesión", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Usuario") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Contraseña") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        try {
                            val request = AuthRequest(username, password)
                            val response = RetrofitClient.instance.login(request)

                            // Guardamos el token recibido
                            Session.token = response.token ?: ""

                            isLoading = false
                            Toast.makeText(context, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()

                        } catch (e: HttpException) {
                            isLoading = false
                            val mensaje = when (e.code()) {
                                404 -> "El usuario no existe."
                                401 -> "Contraseña incorrecta."
                                else -> "Error del servidor (${e.code()})."
                            }
                            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                        } catch (e: IOException) {
                            isLoading = false
                            Toast.makeText(context, "Error de red. Verifica IP y servidor.", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            isLoading = false
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                else Text("Entrar")
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onNavigateToRegister) { Text("¿No tienes cuenta? Regístrate aquí") }
        }
    }
}