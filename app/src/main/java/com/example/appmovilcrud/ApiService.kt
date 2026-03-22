package com.example.appmovilcrud
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// 1. Modelos de datos que enviaremos y recibiremos
data class AuthRequest(val username: String, val password: String)
data class AuthResponse(val token: String?, val message: String?)

// 2. Definición de las rutas del backend
interface ApiService {
    @POST("/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse
}

// 3. El Cliente que hace la magia
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