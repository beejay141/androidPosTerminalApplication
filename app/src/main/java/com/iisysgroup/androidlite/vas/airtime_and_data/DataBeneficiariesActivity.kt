package com.iisysgroup.androidlite.vas.airtime_and_data

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.iisysgroup.androidlite.App
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.vas.adapter.DataBeneficiariesAdapter
import kotlinx.android.synthetic.main.activity_beneficiaries.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import java.util.*

class DataBeneficiariesActivity : AppCompatActivity(), DataBeneficiariesAdapter.DataClickListener {

    override fun onDataItemClick(data: ArrayList<DataBeneficiariesModel>, position: Int) {
        alert {
            title = "Transaction"
            message = "Do you want to purchase data for ${data[position].phone_number}?"
            okButton { passDataToActivity(data[position]) }
        }.show()
    }

    private fun passDataToActivity(dataModel : DataBeneficiariesModel) {

        when (dataModel.data_provider){
            "GLODATA" -> { setUpGloData(dataModel)}
            "ETISALATDATA" -> { setUpNineMobile(dataModel)}
            "MTNDATA" -> { setUpMtnData(dataModel) }
        }
        val intent = Intent(this@DataBeneficiariesActivity, DataActivity::class.java)
        intent.putExtra("dataModel", dataModel)
        startActivity(intent)
    }

    private val dataBeneficiariesDao by lazy {
        (application as App).beneficiariesDatabase.getDataBeneficiariesDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beneficiaries)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = "Data Beneficiaries"

        setUpRecyclerView()
    }

    private fun setUpMtnData(dataModel: DataBeneficiariesModel) {
        val intent = Intent(this@DataBeneficiariesActivity, AllData::class.java)
        intent.putExtra(AllData.KEYS.TYPE_OF_DATA_KEY, AllData.KEYS.DATA_TYPE.MTN)
        intent.putExtra(KEYS.PHONE_NUMBER, dataModel.phone_number)
        startActivity(intent)
    }


    private fun setUpGloData(dataModel: DataBeneficiariesModel) {
        val intent = Intent(this@DataBeneficiariesActivity, AllData::class.java)
        intent.putExtra(AllData.KEYS.TYPE_OF_DATA_KEY, AllData.KEYS.DATA_TYPE.GLO)
        intent.putExtra(KEYS.PHONE_NUMBER, dataModel.phone_number)
        startActivity(intent)
    }

    private fun setUpNineMobile(dataModel: DataBeneficiariesModel) {
        val intent = Intent(this@DataBeneficiariesActivity, AllData::class.java)
        intent.putExtra(AllData.KEYS.TYPE_OF_DATA_KEY, AllData.KEYS.DATA_TYPE.ETISALAT)
        intent.putExtra(KEYS.PHONE_NUMBER, dataModel.phone_number)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    private fun setUpRecyclerView() {
        val layoutManager = GridLayoutManager(this@DataBeneficiariesActivity, 1, GridLayoutManager.VERTICAL, false)
        airtime_beneficiaries_recyclerview.layoutManager = layoutManager

        launch(CommonPool){
            val beneficiaries = dataBeneficiariesDao.getAllBeneficiaries()
            if (beneficiaries.isEmpty()){
                launch(UI){
                    tv_empty_beneficiaries.visibility = View.VISIBLE
                }
            } else {
                launch(UI){
                    val adapter = DataBeneficiariesAdapter(beneficiaries as ArrayList<DataBeneficiariesModel>, this@DataBeneficiariesActivity, this@DataBeneficiariesActivity)
                    airtime_beneficiaries_recyclerview.adapter = adapter
                }

            }
            Log.d("Beneficiaries", beneficiaries.toString())
        }
    }

    object KEYS {
        const val PHONE_NUMBER = "phone_number"
    }
}
