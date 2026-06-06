package com.iisysgroup.androidlite.utils

import android.content.Context
import android.util.Log
import com.iisysgroup.androidlite.login.securestorage.SecureStorage
import com.iisysgroup.payvice.securestorage.SecureStorageUtils

object EncryptionUtils {
    fun encryptPin(context: Context, pin : String) : String {
        val ePassword = SecureStorage.retrieve("epassword", "")
        Log.d("OkH", ePassword)
        if (ePassword.isNotEmpty()){
            val ePin = SecureStorageUtils.hashIt(pin, ePassword)!!
            Log.d("OkH", ePin)
            return ePin
        }
          return ""
    }
}