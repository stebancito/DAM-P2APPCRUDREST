package com.example.appmovilcrud

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(onLogout: () -> Unit) {
    // Estados de la interfaz
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var isLoading by remember { mutableStateOf(true) }

    // Estados para el Diálogo de Crear/Editar
    var showDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogDesc by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // El token que nos dio el Login
    val authHeader = "Bearer ${Session.token}"

    // READ (Cargar tareas al abrir la pantalla)
    fun loadTasks() {
        isLoading = true
        coroutineScope.launch {
            try {
                tasks = RetrofitClient.instance.getTasks(authHeader)
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar tareas", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    // Cargamos las tareas apenas se entra a la pantalla
    LaunchedEffect(Unit) { loadTasks() }

    // Interfaz principal
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Tareas", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    TextButton(onClick = {
                        Session.token = "" // Borrar token
                        onLogout()
                    }) {
                        Text("Salir", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingTask = null
                dialogTitle = ""
                dialogDesc = ""
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Tarea")
            }
        }
    ) { paddingValues ->

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay tareas aún. ¡Crea una!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    // Tarjeta de cada tarea
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = task.title, style = MaterialTheme.typography.titleMedium)
                                if (!task.description.isNullOrBlank()) {
                                    Text(text = task.description, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            // Botones de Editar y Borrar
                            Row {
                                IconButton(onClick = {
                                    editingTask = task
                                    dialogTitle = task.title
                                    dialogDesc = task.description ?: ""
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = {
                                    // DELETE (Borrar tarea)
                                    coroutineScope.launch {
                                        try {
                                            RetrofitClient.instance.deleteTask(authHeader, task.id!!)
                                            loadTasks() // Recargar lista
                                            Toast.makeText(context, "Tarea borrada", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error al borrar", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo para  CREATE y UPDATE
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (editingTask == null) "Nueva Tarea" else "Editar Tarea") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = dialogTitle, onValueChange = { dialogTitle = it },
                            label = { Text("Título") }, singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = dialogDesc, onValueChange = { dialogDesc = it },
                            label = { Text("Descripción") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (dialogTitle.isBlank()) {
                            Toast.makeText(context, "El título no puede estar vacío", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        showDialog = false
                        isLoading = true

                        coroutineScope.launch {
                            try {
                                val newTask = Task(title = dialogTitle, description = dialogDesc)
                                if (editingTask == null) {
                                    // POST: Crear
                                    RetrofitClient.instance.createTask(authHeader, newTask)
                                } else {
                                    // PUT: Actualizar
                                    RetrofitClient.instance.updateTask(authHeader, editingTask!!.id!!, newTask)
                                }
                                loadTasks() // Recargamos las tareas para ver los cambios
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al guardar tarea", Toast.LENGTH_SHORT).show()
                                isLoading = false
                            }
                        }
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }
}