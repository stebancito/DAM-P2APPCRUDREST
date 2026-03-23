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

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Crear Cuenta", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Nuevo Usuario") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Contraseña") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it },
                label = { Text("Confirmar Contraseña") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        Toast.makeText(context, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (password != confirmPassword) {
                        Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        try {
                            val request = AuthRequest(username, password)
                            // Llamada a tu backend
                            RetrofitClient.instance.register(request)

                            isLoading = false
                            Toast.makeText(context, "Registro exitoso. Inicia sesión.", Toast.LENGTH_LONG).show()
                            onRegisterSuccess()

                        } catch (e: HttpException) {
                            // El servidor respondió, pero con un error (ej. 400 o 500)
                            isLoading = false
                            val mensajeError = if (e.code() == 400) {
                                "El usuario ya existe. Intenta con otro nombre."
                            } else {
                                "Error del servidor (${e.code()})"
                            }
                            Toast.makeText(context, mensajeError, Toast.LENGTH_LONG).show()

                        } catch (e: IOException) {
                            // El celular no pudo llegar al servidor (apagado, IP incorrecta, sin internet)
                            isLoading = false
                            Toast.makeText(context, "Error de red: ¿Está encendido el servidor y usas la IP correcta?", Toast.LENGTH_LONG).show()

                        } catch (e: Exception) {
                            // Cualquier otro error en la app
                            isLoading = false
                            Toast.makeText(context, "Error inesperado: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                else Text("Registrarse")
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onNavigateBack) { Text("¿Ya tienes cuenta? Inicia sesión") }
        }
    }
}