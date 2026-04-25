package com.example.reduesmobile.data

import android.content.Context
import com.example.reduesmobile.data.auth.AuthInterceptor
import com.example.reduesmobile.data.auth.TokenManager
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {

    companion object {
        const val BASE_URL = "https://redues.runasp.net/api/"

        fun getRetrofitInstance(context: Context): Retrofit{
            val tokenManager = TokenManager(context)

            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(tokenManager))
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .build()
        }
    }
}