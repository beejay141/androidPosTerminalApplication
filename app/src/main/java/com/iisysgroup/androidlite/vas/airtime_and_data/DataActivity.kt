package com.iisysgroup.androidlite.vas.airtime_and_data

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.iisysgroup.androidlite.R
import kotlinx.android.synthetic.main.activity_data.*

class DataActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nine_mobile.setOnClickListener { setUpNineMobile() }

        glo.setOnClickListener { setUpGloData() }

        mtn.setOnClickListener { setUpMtnData() }

        airtel.setOnClickListener { setUpAirtelData() }

        beneficiaries.setOnClickListener { setUpBeneficiaries() }
    }

    private fun setUpAirtelData() {
        val intent = Intent(this@DataActivity, AllData::class.java)
        intent.putExtra(AllData.KEYS.TYPE_OF_DATA_KEY, AllData.KEYS.DATA_TYPE.AIRTEL)
        startActivity(intent)
    }

    private fun setUpMtnData(){
        val intent = Intent(this@DataActivity, AllData::class.java)
        intent.putExtra(AllData.KEYS.TYPE_OF_DATA_KEY, AllData.KEYS.DATA_TYPE.MTN)
        startActivity(intent)
    }



    private fun setUpBeneficiaries() {
        startActivity(Intent(this@DataActivity, DataBeneficiariesActivity::class.java))
    }

    private fun setUpGloData() {
        val intent = Intent(this@DataActivity, AllData::class.java)
        intent.putExtra(AllData.KEYS.TYPE_OF_DATA_KEY, AllData.KEYS.DATA_TYPE.GLO)
        startActivity(intent)
    }

    private fun setUpNineMobile() {
        val intent = Intent(this@DataActivity, AllData::class.java)
        intent.putExtra(AllData.KEYS.TYPE_OF_DATA_KEY, AllData.KEYS.DATA_TYPE.ETISALAT)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }
}
