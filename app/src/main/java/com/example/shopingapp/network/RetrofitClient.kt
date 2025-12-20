package com.example.shopingapp.network

import SessionManager
import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL =
        "https://cosmetic-server-production.up.railway.app/"

    @Volatile
    private var apiService: ApiService? = null

    fun instance(context: Context): ApiService {
        return apiService ?: synchronized(this) {
            apiService ?: create(context).also { apiService = it }
        }
    }





    private fun create(context: Context): ApiService {

        val sessionManager = SessionManager(context.applicationContext)

        val okHttp = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder().apply {
                    sessionManager.getToken()?.let {
                        addHeader("Authorization", "Bearer $it")
                    }
                }.build()

                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
