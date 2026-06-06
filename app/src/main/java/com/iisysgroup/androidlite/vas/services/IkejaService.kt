package com.iisysgroup.androidlite.vas.services

import com.google.gson.GsonBuilder
import com.iisysgroup.androidlite.vas.activity.energy.Ikeja.IkejaModel
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

interface IkejaService {

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("ie/validate")
    fun ikejaLookup(@Body lookup : IkejaModel.IkejaLookupDetails) : Deferred<Any>

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("ie/purchase")
    fun pay(@Body payDetails : IkejaModel.IkejaPayDetails) : Deferred<Any>

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("ie/purchase")
    fun prepay(@Body payDetails : IkejaModel.IkejaPrePayDetails) : Deferred<Any>


    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("card/ie/purchase")
    fun payWithCard(@Body payDetails : IkejaModel.IkejaPayDetails) : Deferred<Any>

    companion object Factory {
        val BASE_URL = "http://vas.itexapp.com/vas/"
        fun create(): IkejaService {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            val clientBuilder = OkHttpClient.Builder()
            clientBuilder.addInterceptor(logging)
            clientBuilder.connectTimeout(50, TimeUnit.SECONDS)
            clientBuilder.readTimeout(50, TimeUnit.SECONDS)
            clientBuilder.writeTimeout(50, TimeUnit.SECONDS)

            val client = clientBuilder.build()

            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .client(client).baseUrl(BASE_URL)
                    .build()
            val service = retrofit.create(IkejaService::class.java)

            return service
        }
    }
}