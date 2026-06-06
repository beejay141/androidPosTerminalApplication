package com.iisysgroup.androidlite.vas.services

import com.google.gson.GsonBuilder
import com.iisysgroup.androidlite.vas.activity.energy.Eko.EkoModel
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import kotlinx.coroutines.experimental.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface InternetService {

    @Headers("Content-Type:application/json")
    @POST("smile/validate")
    fun pay(@Body payDetails : EkoModel.EkoPayDetails) : Deferred<Any>


    companion object Factory {
        private val BASE_URL = "http://197.253.19.75:8090"
        fun create(): EkoService {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            val clientBuilder = OkHttpClient.Builder()
            clientBuilder.addInterceptor(logging)
            clientBuilder.connectTimeout(20, TimeUnit.SECONDS)
            clientBuilder.readTimeout(30, TimeUnit.SECONDS)
            clientBuilder.writeTimeout(30, TimeUnit.SECONDS)

            val client = clientBuilder.build()

            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(gson)).addCallAdapterFactory(CoroutineCallAdapterFactory()).client(client).baseUrl(BASE_URL).build()
            val service = retrofit.create(EkoService::class.java)

            return service
        }
    }
}