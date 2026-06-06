package com.iisysgroup.androidlite.vas.activity.energy.services

import com.google.gson.GsonBuilder
import com.iisysgroup.androidlite.vas.activity.energy.Eko.EkoModel
import com.iisysgroup.androidlite.vas.activity.energy.model.EnergyModel
import com.iisysgroup.androidlite.vas.services.EkoService
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import kotlinx.coroutines.experimental.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface EnergyServices{
    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("validate")
    fun ekoLookup(@Body lookup : EkoModel.EkoLookupDetails) : Deferred<Any>

//    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
//    @POST("payment/process")
//    fun pay(@Body payDetails : EkoModel.EkoPayDetails) : Deferred<Any>

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("kedco/validation")
    fun kanoLookup(@Body lookup : EnergyModel.KanoLookupDetails) : Deferred<Any>

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("phed/validation")
    fun PhLookup(@Body lookup : EnergyModel.PhLookupDetails) : Deferred<Any>

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("phed/payment")
    fun phPay(@Body payDetails : EnergyModel.PhPayDetails) : Deferred<Any>

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("abuja/validation")
    fun AbjLookup(@Body lookup : EnergyModel.AbjLookupDetails) : Deferred<Any>

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("abuja/payment")
    fun AbjPay(@Body payDetails : EnergyModel.AbjPayDetails) : Deferred<Any>

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("ibedc/validation")
    fun IbLookup(@Body lookup : EnergyModel.IbLookupDetails) : Deferred<Any>

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("ibedc/payment")
    fun IbadanPay(@Body payDetails : EnergyModel.IbPayDetails) : Deferred<Any>

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("eedc/validation")
    fun IbLookup(@Body lookup : EnergyModel.EnuguLookupDetails) : Deferred<Any>

    @Headers("Authorization:IISYSGROUP c1e750cf89b05b0fc56eecf6fc25cce85e2bb8e0c46d7bfed463f6c6c89d4b8e","sysid:ee2dadd1e684032929a2cea40d1b9a2453435da4f588c1ee88b1e76abb566c31", "Content-Type:application/json")
    @POST("eedc/payment")
    fun EnuguPay(@Body payDetails : EnergyModel.EnuguPayDetails) : Deferred<Any>


    companion object Factory {
        private val BASE_URL = "http://vas.itexapp.com/vas/"
        fun create(): EnergyServices {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            val clientBuilder = OkHttpClient.Builder()
            clientBuilder.addInterceptor(logging)
            clientBuilder.connectTimeout(30, TimeUnit.SECONDS)
            clientBuilder.readTimeout(90, TimeUnit.SECONDS)
            clientBuilder.writeTimeout(90, TimeUnit.SECONDS)

           // val client = clientBuilder.build()

            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .client(clientBuilder.build())
                    .baseUrl(BASE_URL)
                    .build()
            val service = retrofit.create(EnergyServices::class.java)

            return service
        }
    }
}