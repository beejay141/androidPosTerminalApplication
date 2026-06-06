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

interface EkoService {

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("validate")
    fun ekoLookup(@Body lookup : EkoModel.EkoLookupDetails) : Deferred<Any>

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("payment/process")
    fun pay(@Body payDetails : EkoModel.EkoPayDetails) : Deferred<Any>


    companion object Factory {
        private val BASE_URL = "http://197.253.19.75:8090/ekedc/vas/"
        fun create(): EkoService {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            val clientBuilder = OkHttpClient.Builder()
            clientBuilder.addInterceptor(logging)
            clientBuilder.connectTimeout(30, TimeUnit.SECONDS)
            clientBuilder.readTimeout(90, TimeUnit.SECONDS)
            clientBuilder.writeTimeout(90, TimeUnit.SECONDS)

            val client = clientBuilder.build()

            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .client(client)
                    .baseUrl(BASE_URL).build()
            val service = retrofit.create(EkoService::class.java)

            return service
        }
    }
}