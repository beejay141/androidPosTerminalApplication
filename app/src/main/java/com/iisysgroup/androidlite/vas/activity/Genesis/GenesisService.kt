package com.iisysgroup.androidlite.vas.activity.Genesis

import com.google.gson.GsonBuilder
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

interface GenesisService {
    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e",
            "sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31",
            "Content-Type:application/json")
    @POST("ticket/buy")
    fun buyTicket(@Body lookup : GenesisModel.payDetail) : Deferred<Any>



    companion object Factory {
        private val BASE_URL = "http://197.253.19.75:8092/api/movies/"
        fun create(): GenesisService {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            val clientBuilder = OkHttpClient.Builder()
            clientBuilder.addInterceptor(logging)
            clientBuilder.connectTimeout(30, TimeUnit.SECONDS)
            clientBuilder.readTimeout(90, TimeUnit.SECONDS)
            clientBuilder.writeTimeout(90, TimeUnit.SECONDS)

            val client = clientBuilder.build()

            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(gson)).addCallAdapterFactory(CoroutineCallAdapterFactory()).client(client).baseUrl(BASE_URL).build()
            val service = retrofit.create(GenesisService::class.java)

            return service
        }
    }
}