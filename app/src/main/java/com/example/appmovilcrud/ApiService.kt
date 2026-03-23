package com.example.appmovilcrud
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Modelos Auth
data class AuthRequest(val username: String, val password: String)
data class AuthResponse(val token: String?, val message: String?)

// Modelos para el CRUD de Tareas
data class Task(
    val id: Int? = null,
    val title: String,
    val description: String?,
    val is_completed: Boolean = false
)

interface ApiService {
    @POST("/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    // RUTAS CRUD (Nota el @Header("Authorization") para enviar el token)
    @GET("/tasks")
    suspend fun getTasks(@Header("Authorization") token: String): List<Task>

    @POST("/tasks")
    suspend fun createTask(@Header("Authorization") token: String, @Body task: Task): Task

    @PUT("/tasks/{id}")
    suspend fun updateTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body task: Task
    ): Task

    @DELETE("/tasks/{id}")
    suspend fun deleteTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    )
}

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.68:5000"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}