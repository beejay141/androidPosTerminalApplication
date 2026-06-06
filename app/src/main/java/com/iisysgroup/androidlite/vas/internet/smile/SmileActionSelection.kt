package com.iisysgroup.androidlite.vas.internet.smile

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.iisysgroup.androidlite.R
import kotlinx.android.synthetic.main.activity_smile_action_selection.*


class SmileActionSelection : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smile_action_selection)

        action_smile_buy_bundle.setOnClickListener {
            startActivity(Intent(this@SmileActionSelection, SmileLookup::class.java))
        }

        action_smile_top_up.setOnClickListener {
            startActivity(Intent(this@SmileActionSelection, SmileTopup::class.java))
        }

        /*action_smile_beneficiaries.setOnClickListener {
            //startActivity(
        }*/

    }
}
