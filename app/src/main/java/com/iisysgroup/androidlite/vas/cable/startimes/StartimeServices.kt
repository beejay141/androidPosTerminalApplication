package com.iisysgroup.androidlite.vas.cable.startimes

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

interface StartimeServices {
    @Headers("Content-Type:application/json")
    @POST("startimes/validation")
    fun starTimesLookup(@Body lookup: StartimesModel.StartimesLookupDetails): Deferred<Any>


    companion object Factory {
        private val BASE_URL = "http://baseflat.itexapp.com:8029/vas/"
        fun create(): StartimeServices {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            val clientBuilder = OkHttpClient.Builder()
            clientBuilder.addInterceptor(logging)
            clientBuilder.connectTimeout(30, TimeUnit.SECONDS)
            clientBuilder.readTimeout(90, TimeUnit.SECONDS)
            clientBuilder.writeTimeout(90, TimeUnit.SECONDS)

            val client = clientBuilder.build()

            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(CoroutineCallAdapterFactory()).client(client)
                    .baseUrl(BASE_URL).build()
            val service = retrofit.create(StartimeServices::class.java)

            return service
        }
    }
}