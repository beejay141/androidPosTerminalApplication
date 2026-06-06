package com.iisysgroup.androidlite.db

import com.iisysgroup.poslib.host.entities.VasTerminalData
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface VasTerminalService {

    @GET("xmerchant.php")
    fun getVasTerminalDetails() : Single<VasTerminalData>


    object Factory {
        private val baseUrl = "http://197.253.19.75/tams/eftpos/op/"

        var interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

        var client = OkHttpClient.Builder ()
                .readTimeout(120, TimeUnit.SECONDS)
                .connectTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build()



        fun getService() : VasTerminalService {
            val retrofitBuilder = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(client)
                    .baseUrl(baseUrl)
                    .build()

            return retrofitBuilder.create(VasTerminalService::class.java)
        }
    }
}