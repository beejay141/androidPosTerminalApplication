package com.iisysgroup.androidlite.utils

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.andrognito.pinlockview.PinLockListener
import com.iisysgroup.androidlite.R
import kotlinx.android.synthetic.main.activity_enter_pin.*
import org.jetbrains.anko.contentView
import org.jetbrains.anko.toast

class EnterPinActivity : AppCompatActivity(), PinLockListener {
    override fun onEmpty() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onComplete(pin: String?) {
        toast(pin!!)
        val intent = Intent()
        intent.putExtra("pin", pin!!)
        setResult(Activity.RESULT_OK, intent)
    }

    override fun onPinChange(pinLength: Int, intermediatePin: String?) {
        Log.d("OkH", intermediatePin!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_pin)
        contentView!!.post {
            Log.d("OkH","width ${contentView!!.width} and height ${contentView!!.height}")
        }


        pin_lock_view.setPinLockListener(this)
        pin_lock_view.attachIndicatorDots(indicator_dots)

        pin_lock_view.pinLength = 4
    }
}
