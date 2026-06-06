package com.iisysgroup.androidlite.vas.activity.energy.Ikeja

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.vas.activity.EnergyActivity
import kotlinx.android.synthetic.main.activity_ikeja_electric.*

class IkejaElectric : AppCompatActivity() {

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ikeja_electric)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ikeja_prepaid.setOnClickListener { setUpPrepaid() }
        ikeja_postpaid.setOnClickListener { setUpPostpaid() }
    }



    private fun setUpPostpaid() {
        val intent = Intent(this, IkejaPostpaid::class.java )
        intent.putExtra(EnergyActivity.ENERGY_TYPE, EnergyActivity.ENERGY_ENERGY)
        startActivity(intent)
    }

    private fun setUpPrepaid() {
       val intent = Intent(this, IkejaPrepaid::class.java)
        intent.putExtra(EnergyActivity.ENERGY_TYPE, EnergyActivity.ENERGY_ENERGY)
        startActivity(intent)
    }
}
