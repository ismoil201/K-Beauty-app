package com.example.shopingapp.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiRetrofitClient {

    private const val BASE_URL = "https://cosmetic-server-production.up.railway.app/"

    fun create(token: String): ApiService {

        val okHttp = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
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
