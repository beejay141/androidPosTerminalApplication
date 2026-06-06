package com.iisysgroup.androidlite.vas.services

import com.google.gson.GsonBuilder
import com.iisysgroup.androidlite.models.PfmDetails
import com.iisysgroup.androidlite.models.PfmResponse
import kotlinx.coroutines.experimental.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface PfmService {

    //API endpoint for PfmService

        @Headers("Authorization:IISYS 74f230cc6cc96f7672aeb1f1745ccaec56de6e61f1d2ef2122441040ec58d044","iisysgroup:21155ded2430abf93108bef7a62cf2cca1bcf3c3ea8a75e6527a53409be495d0", "Content-Type:application/json")
    @POST("api/tms/iisys/auth2")
    fun sendPfm(@Body request : PfmDetails) : Deferred<PfmResponse>



    companion object Factory {
        val BASE_URL = "https://merchant.payvice.com/"
        fun create(): PfmService {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            val clientBuilder = OkHttpClient.Builder()
            clientBuilder.connectTimeout(20, TimeUnit.SECONDS)
            clientBuilder.readTimeout(30, TimeUnit.SECONDS)
            clientBuilder.writeTimeout(30, TimeUnit.SECONDS)
            clientBuilder.addInterceptor(logging)

            val client = clientBuilder.build()

            //val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder().client(client).addConverterFactory(GsonConverterFactory.create()).baseUrl(BASE_URL).build()
            val service = retrofit.create(PfmService::class.java)

            return service
        }
    }
}