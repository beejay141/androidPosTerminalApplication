package com.iisysgroup.androidlite.vas.airtime_and_data

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.cardpaymentprocessors.ReversalCommunicator
import com.iisysgroup.androidlite.login.Helper
import com.iisysgroup.androidlite.login.securestorage.SecureStorage
import com.iisysgroup.androidlite.models.PfmDetails
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.androidlite.vas.services.AirtimeService
import com.iisysgroup.payvice.securestorage.SecureStorageUtils
import com.iisysgroup.poslib.host.entities.ConnectionData
import com.iisysgroup.poslib.host.entities.KeyHolder
import com.iisysgroup.poslib.host.entities.TransactionResult
import com.iisysgroup.poslib.utils.TransactionData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException

class AirtimeProcessor(val context : Context, listener : onAirtimeTransactionResultListener, airtimeProvider : String, phoneNumber : String, airtimeAmount : String, isCard : Boolean = false) :  Callback<Any> {

    private val mPayvicePin by lazy {
        PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.key_payvice_wallet_pin),"")
    }

    private var isCard : Boolean = false

   private val listener = listener
   private val airtimeProvider = airtimeProvider
   private val phoneNumber = phoneNumber
   private val airtimeAmount = airtimeAmount

    private lateinit var transactionResult:TransactionResult;
  // private val pfmDetails = pfm

    private  val transactionData by lazy {
        Gson().fromJson(SecureStorage.retrieve(Helper.TRANSACTION_DATA, ""), TransactionData::class.java)
    }
    private val conectionData by lazy {
        Gson().fromJson(SecureStorage.retrieve(Helper.CONNECTION_DATA, ""), ConnectionData::class.java)
    }
    val keyHolder by lazy {
        Gson().fromJson(SecureStorage.retrieve(Helper.KEY_HOLDER, ""), KeyHolder::class.java)
    }



    private val terminalID =  PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.key_terminal_id), null)

    private val wallet_username by lazy {
        SharedPreferenceUtils.getPayviceUsername(context)
    }

    private val wallet_id by lazy {
        SharedPreferenceUtils.getPayviceWalletId(context)
    }

    private val wallet_password by lazy {
        SharedPreferenceUtils.getPayvicePassword(context)
    }

    private val wallet_clear_password by lazy {
        SecureStorage.retrieve(Helper.PLAIN_PASSWORD, "")
    }

    fun performTransaction(isCard: Boolean = false, pin : String,transactionRes:TransactionResult?){
        this.isCard = isCard
        transactionResult =transactionRes!!
        if (isCard){
            val details = AirtimeRequestDetails(amount = airtimeAmount, phone = phoneNumber, service = airtimeProvider, terminal_id = wallet_id, user_id = wallet_username, password = wallet_clear_password, pin = pin)
            AirtimeService.create().airtimeCardPurchase(details).enqueue(this)
            return
        }

        val details = AirtimeRequestDetails(amount = airtimeAmount, phone = phoneNumber, service = airtimeProvider, terminal_id = wallet_id, user_id = wallet_username, password = wallet_clear_password, pin = pin)
        AirtimeService.create().airtimePurchase(details).enqueue(this)

    }

    interface
    onAirtimeTransactionResultListener {
        fun onResponse(model : AirtimeSuccessResponse)
        fun onError(errorMessage : String, isCard : Boolean)
    }

    override fun onFailure(call: Call<Any>?, t: Throwable?) {


        Thread {

            Log.d("reversal com >>>>>>>", "On failure  ")


            var reversalCommunicator = ReversalCommunicator(context, transactionData, conectionData, keyHolder,transactionResult)


            val reversalcom = reversalCommunicator.rollBackTransaction()
            Helper.runOnUiThread {
                //Update UI
                Toast.makeText(context, reversalcom.toString(), Toast.LENGTH_SHORT).show()

            }

        }.start()
        t?.let {
            Log.d("Special error", "Some error2")
            Log.d("Special error", it.toString())
            if (it is SocketTimeoutException){
                listener.onError("Connection is taking too long. Please try again later", isCard)

                return
            }

            if (it is ConnectException){
                listener.onError("Please check your internet connection.", isCard)
                return
            }

            if (it is retrofit2.HttpException){
                listener.onError("Server error", isCard)
                return
            }
            it.message?.let {
                listener.onError(it, isCard)
            }
        }

    }

    override fun onResponse(call: Call<Any>?, response: Response<Any>?) {
        response?.body()?.let {
            val jsonResponse = Gson().toJsonTree(it)
            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

            if (jsonResponse.toString().contains("amount")){
                val parsedResponse = gson.fromJson(jsonResponse.toString(),  AirtimeSuccessResponse::class.java)
                listener.onResponse(parsedResponse)




                Log.d("reversal com >>>>>>>",jsonResponse.toString())

                Toast.makeText(context,"Reverse here onResponse Success  Airtimeprocessor ", Toast.LENGTH_SHORT).show()

                Thread {

                    Log.d("reversal com >>>>>>>","Doing Reversal ")

                    //Do some Network Request
                    var reversalCommunicator= ReversalCommunicator(context,transactionData,conectionData,keyHolder,transactionResult)

                    Log.d("Gson of  transactionData >>>>>>>",Gson().toJson(transactionData))
                    Log.d("Gson of  conectionData >>>>>>>",Gson().toJson(conectionData))

                    Log.d("Gson of  keyHolder >>>>>>>",Gson().toJson(keyHolder))



//                    Toast.makeText(context,Gson().toJson(reversalCommunicator),Toast.LENGTH_LONG).show()

                    val reversalcom = reversalCommunicator.rollBackTransaction()

                    Helper.runOnUiThread {
                        //Update UI
                        Toast.makeText(context, reversalcom.toString(), Toast.LENGTH_SHORT).show()

                    }
                }.start()




            } else {
                val parsedResponse = gson.fromJson(jsonResponse.toString(), AirtimeFailedResponse::class.java)
                listener.onError(parsedResponse.message, isCard)

                Thread {

                    Log.d("reversal com >>>>>>>", "in else of response ")


                    var reversalCommunicator = ReversalCommunicator(context, transactionData, conectionData, keyHolder,transactionResult)


                    val reversalcom = reversalCommunicator.rollBackTransaction()
                    Helper.runOnUiThread {
                        //Update UI
                        Toast.makeText(context, reversalcom.toString(), Toast.LENGTH_SHORT).show()

                    }

                }.start()

                Toast.makeText(context,"Reverse here onResponse Failed  Airtimeprocessor", Toast.LENGTH_SHORT).show()


            }
        }
    }
}