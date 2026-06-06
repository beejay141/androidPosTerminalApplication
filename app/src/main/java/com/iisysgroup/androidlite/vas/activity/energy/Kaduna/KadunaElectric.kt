package com.iisysgroup.androidlite.vas.activity.energy.Kaduna

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.vas.activity.EnergyActivity
import kotlinx.android.synthetic.main.activity_kaduna_electric.*
import kotlinx.android.synthetic.main.content_energy.*

class KadunaElectric : AppCompatActivity() {

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
        setContentView(R.layout.activity_kaduna_electric)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        prepaid.setOnClickListener { setUpPrepaid() }
        postpaid.setOnClickListener { setUpPostpaid() }
    }

    private fun setUpPostpaid() {
        val intent = Intent(this, KadunaPostpaid::class.java)
        intent.putExtra(EnergyActivity.ENERGY_TYPE, EnergyActivity.ENERGY_NO_ENERGY)
        startActivity(intent)
    }

    private fun setUpPrepaid() {
        val intent = Intent(this, KadunaPrepaid::class.java)
        intent.putExtra(EnergyActivity.ENERGY_TYPE, EnergyActivity.ENERGY_NO_ENERGY)
        startActivity(intent)
    }
}
