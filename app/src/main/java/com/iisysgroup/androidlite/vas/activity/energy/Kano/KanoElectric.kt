package com.iisysgroup.androidlite.vas.activity.energy.Kano

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

import com.iisysgroup.androidlite.R
import kotlinx.android.synthetic.main.activity_eko_electric.*

class KanoElectric : AppCompatActivity() {

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
        setContentView(R.layout.activity_kano_electric)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
