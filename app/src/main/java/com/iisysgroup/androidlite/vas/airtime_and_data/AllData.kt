package com.iisysgroup.androidlite.vas.airtime_and_data

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.vas.adapter.DataAdapter
import com.iisysgroup.androidlite.vas.services.DataService
import kotlinx.android.synthetic.main.activity_glo_data.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.okButton
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.*

@Suppress("IMPLICIT_CAST_TO_ANY")
class AllData : AppCompatActivity(), DataAdapter.DataClickListener {


    private lateinit var dataItem : DataModel.DataResponseElements
    private var amount = 0L

    private val isBeneficiary by lazy {
        intent.hasExtra(DataBeneficiariesActivity.KEYS.PHONE_NUMBER)
    }

    private val phoneNumber by lazy {
        intent.getStringExtra(DataBeneficiariesActivity.KEYS.PHONE_NUMBER)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }


    private val serviceType by lazy {
        val typeOfData = intent.getSerializableExtra(KEYS.TYPE_OF_DATA_KEY) as KEYS.DATA_TYPE
        when (typeOfData){
            KEYS.DATA_TYPE.ETISALAT -> "ETISALATDATA"
            KEYS.DATA_TYPE.GLO -> "GLODATA"
            KEYS.DATA_TYPE.MTN -> "MTNDATA"
            KEYS.DATA_TYPE.AIRTEL -> "AIRTELDATA"
        }
    }

    override fun onDataItemClick(data: ArrayList<DataModel.DataResponseElements>, position: Int) {
        val dataSelected = data[position]



        val intent = Intent(this@AllData, DataPhoneEntry::class.java)

        intent.putExtra(KEYS.DATA_VALUE, dataSelected)

        if (isBeneficiary)
            intent.putExtra(KEYS.PHONE_NUMBER, phoneNumber)


        startActivity(intent)


    }

    fun initializeRecyclerView(items: ArrayList<DataModel.DataResponseElements>, listener: DataAdapter.DataClickListener) {


        val dataAdapter = DataAdapter(items, this, listener)
        dataRecyclerView.setHasFixedSize(true)
        dataRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        dataRecyclerView.adapter = dataAdapter
    }

    private val mProgressDialog by lazy {
        indeterminateProgressDialog("Loading data services")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glo_data)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (serviceType == "ETISALATDATA"){
            supportActionBar?.title = "Etisalat Data plans"
        } else if (serviceType == "GLODATA"){
            supportActionBar?.title = "Glo Data plans"
        } else if (serviceType == "AIRTELDATA"){
            supportActionBar?.title = "Airtel Data plans"
        } else if (serviceType == "MTNDATA"){
            supportActionBar?.title = "Mtn Data plans"
        }
        mProgressDialog.show()

        async {

            try {
                val lookupDataModel = DataModel.DataLookUpDetails(serviceType)
                val response = DataService.create().dataLookup(lookupDataModel).await()

                val jsonResponse = Gson().toJsonTree(response).asJsonObject
                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                if (response.toString().contains("error:true")){
                    val convertedResponse = gson.fromJson(jsonResponse.toString(), DataModel.DataLookUpFailedResponse::class.java)
                    launch(UI){
                        alert {
                            title = "Response"
                            message = convertedResponse.message
                            okButton { gotoDataAndAirtime() }
                        }.show()
                    }

                }
                else {
                    val convertedResponse = gson.fromJson(jsonResponse.toString(), DataModel.DataLookUpSuccessResponse::class.java)
                    launch(UI){
                        mProgressDialog.dismiss()
                        initializeRecyclerView(convertedResponse.data as ArrayList<DataModel.DataResponseElements>, this@AllData)
                    }
            } }
            catch (exception : ConnectException)
            {
                launch(UI){
                    mProgressDialog.dismiss()
                    alert {
                        title = "Response"
                        message = "Error in connection. Please check your internet connection"
                        okButton { gotoDataAndAirtime() }
                    }.show()
                }

            }
            catch (exception : SocketTimeoutException){
            launch(UI){
                mProgressDialog.dismiss()
                alert {
                    title = "Response"
                    message = "This connection is taking too long. Please try again"
                    okButton { gotoDataAndAirtime()  }
                }.show()
            }
        }catch (e : retrofit2.HttpException){
                launch(UI){
                    mProgressDialog.dismiss()
                    alert {
                        title = "Error"
                        message = "Error from server. Please try again"
                        okButton {  }
                    }.show()
                }
            }
        }
    }


    private fun gotoDataAndAirtime() {
        val intent = Intent(this@AllData, DataActivity::class.java)
        finish()
        startActivity(intent)
    }



    object KEYS {
        const val DATA_VALUE = "data_value"

        const val PHONE_NUMBER = "phone_number"

        const val TYPE_OF_DATA_KEY = "type_of_data_key"
        enum class DATA_TYPE {
            GLO, ETISALAT, MTN, AIRTEL
        }
    }
}
