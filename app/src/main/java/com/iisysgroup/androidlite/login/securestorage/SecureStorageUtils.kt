package com.iisysgroup.payvice.securestorage

import com.iisysgroup.androidlite.login.TripleDES
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Created by Bamitale @Itex on 3/30/2016.
 */
object SecureStorageUtils {
    @JvmStatic
    fun hash(input: String, algorithm: String = "SHA-512"): ByteArray {
        try {
            val messageDigest = MessageDigest.getInstance(algorithm)

            return messageDigest.digest(input.toByteArray(charset("UTF-8")))

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return byteArrayOf()
    }

    @JvmStatic
    fun hashIt(msg: String, key: String): String? {

        var m: MessageDigest? = null
        var hashText: String? = null
        val actualKeyBytes = TripleDES.hexStringToBytes(key)
        try {
            m = MessageDigest.getInstance("SHA-256")
            m!!.update(actualKeyBytes, 0, actualKeyBytes.size)
            try {
                m.update(msg.toByteArray(charset("UTF-8")), 0, msg.length)
            } catch (ex: UnsupportedEncodingException) {

            }

            hashText = TripleDES.bytesToHexString(m.digest()) //new BigInteger(1, m.digest()).toString(16);

        } catch (ex: NoSuchAlgorithmException) {

        }

        return hashText

    }

}
