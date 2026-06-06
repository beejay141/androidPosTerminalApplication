package com.iisysgroup.androidlite.vas.services

import com.google.gson.GsonBuilder
import com.iisysgroup.androidlite.vas.cable.*
import com.iisysgroup.androidlite.vas.cable.startimes.StartimesModel
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

interface DstvService {
    //API endpoint for AirtimeService
    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("multichoice/lookup")
    fun dstvLookup(@Body lookup : DstvLookup) : Deferred<DstvResponse>

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("multichoice/lookup")
    fun gotvLookup(@Body lookup : GotvLookupModel) : Deferred<DstvResponse>

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("multichoice/pay")
    fun pay(@Body payDetails : PayDetails) : Deferred<PayResponse>

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("card/multichoice/pay")
    fun payWithCard(@Body payDetails : PayDetails) : Deferred<PayResponse>

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("startimes/validation")
    fun starTimesLookup(@Body lookup: StartimesModel.StartimesLookupDetails): Deferred<Any>

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("startimes/payment")
    fun starTimesPay(@Body lookup: StartimesModel.StartimesPayDetails): Deferred<Any>

    companion object Factory {
        val BASE_URL = "http://197.253.19.75:8090/vas/"
        fun create(): DstvService {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            val clientBuilder = OkHttpClient.Builder()
            clientBuilder.connectTimeout(20, TimeUnit.SECONDS)
            clientBuilder.readTimeout(30, TimeUnit.SECONDS)
            clientBuilder.writeTimeout(30, TimeUnit.SECONDS)
            clientBuilder.addInterceptor(logging)

            val client = clientBuilder.build()

            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(gson)).addCallAdapterFactory(CoroutineCallAdapterFactory()).client(client).baseUrl(BASE_URL).build()
            val service = retrofit.create(DstvService::class.java)

            return service
        }
    }
}