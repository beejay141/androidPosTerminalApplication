package com.iisysgroup.androidlite.utils

import android.content.Context
import java.math.BigInteger
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*

class ClientReferenceKey {
    fun generateReference(context: Context): String {
       var json = "{" +
                "    \"sessionKey\": \"${SharedPreferenceUtils.getSessionKey(context)}\"," +
                "    \"timestamp\": \" ${getCurrentTimeStamp()}\"," +
                "    \"randomString\":\"${generateRandomHexToken(16)}\"" +
                "}"

        return String(org.apache.commons.codec.binary.Base64.encodeBase64(json.toByteArray()))
    }


    fun generateRandomHexToken(byteLength: Int): String {
        val secureRandom = SecureRandom()
        val token = ByteArray(byteLength)
        secureRandom.nextBytes(token)
        return BigInteger(1, token).toString(16) //hex encoding
    }

    fun getCurrentTimeStamp(): String {
        val sdfDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")//dd/MM/yyyy
        val now = Date()
        return sdfDate.format(now)
    }

}