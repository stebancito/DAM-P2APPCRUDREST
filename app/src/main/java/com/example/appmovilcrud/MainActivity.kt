package com.example.appmovilcrud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
// Asegúrate de importar tu tema si lo tienes, por ejemplo:
// import com.tu.paquete.ui.theme.AppTareasCRUDTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Si Android Studio te generó un AppTheme, envuelve AppNavigation() en él.
            // AppTareasCRUDTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AppNavigation()
            }
            // }
        }
    }
}

@Composable
fun AppNavigation() {
    // El NavController es el encargado de gestionar los viajes entre pantallas
    val navController = rememberNavController()

    // Definimos nuestras "rutas" (como si fueran URLs de una página web)
    NavHost(navController = navController, startDestination = "login") {

        // 1. Ruta del Login
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // Cuando el login sea exitoso, vamos a las tareas y borramos el historial
                    navController.navigate("tasks") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // 2. Ruta del Registro
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    // Si se registra bien, lo mandamos al login
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateBack = {
                    // Botón de regresar al login
                    navController.popBackStack()
                }
            )
        }

        // 3. Ruta del CRUD de Tareas (Placeholder por ahora)
        composable("tasks") {
            TasksScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("tasks") { inclusive = true }
                    }
                }
            )
        }
    }
}