package com.iisysgroup.androidlite.vas.airtime_and_data

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.iisysgroup.androidlite.R
import kotlinx.android.synthetic.main.activity_selection.*

class SelectionActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_selection)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        airtime.setOnClickListener {setUpAirtime() }
        data.setOnClickListener{ setUpData() }
    }

    private fun setUpData() {
        val intent = Intent(this@SelectionActivity, DataActivity::class.java)
        startActivity(intent)
    }

    private fun setUpAirtime() {
        val intent = Intent(this@SelectionActivity, AirtimeActivity::class.java)
        startActivity(intent)
    }
}
