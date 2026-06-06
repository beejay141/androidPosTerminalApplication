package com.iisysgroup.androidlite.utils

import android.content.Context
import android.preference.PreferenceManager
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.login.Helper
import com.iisysgroup.androidlite.login.securestorage.SecureStorage

object SharedPreferenceUtils {

    fun getSharedPreferences(context : Context) = PreferenceManager.getDefaultSharedPreferences(context)

    fun getTerminalId(context : Context) = getSharedPreferences(context).getString(context.getString(R.string.key_terminal_id), "")

    fun getIpAddress(context : Context) = getSharedPreferences(context).getString(context.getString(R.string.key_ip_address), "")

    fun getSessionKey(context: Context) = getSharedPreferences(context).getString("sessinKey", "")
    fun saveSessionKey(context: Context, sessionKey: String){
        var editor = getSharedPreferences(context).edit()
        editor.putString("sessinKey", sessionKey)
        editor.apply()
    }

    fun getPort(context : Context) = getSharedPreferences(context).getString(context.getString(R.string.key_pref_port), "")

    fun isSsl(context: Context) = getSharedPreferences(context).getString(context.getString(R.string.key_pref_port_type), "") == "SSL"

    fun getPayviceWalletId(context : Context) = SecureStorage.retrieve(Helper.TERMINAL_ID, "")

    fun getPlainPassword(context : Context) = SecureStorage.retrieve(Helper.PLAIN_PASSWORD, "")

    fun getUserPhone(context : Context) = SecureStorage.retrieve(Helper.USER_PHONE, "")

    fun getPayvicePassword(context : Context) = SecureStorage.retrieve(Helper.STORED_PASSWORD, "")

    fun getPayviceUsername(context : Context) = SecureStorage.retrieve(Helper.USER_ID, "")

    fun setUserLoggedIn(context : Context, isLoggedIn: Boolean) = getSharedPreferences(context).edit().putBoolean(context.getString(R.string.key_is_user_logged_in), isLoggedIn).apply()

    fun getUserLoggedIn(context : Context) = getSharedPreferences(context).getBoolean(context.getString(R.string.key_is_user_logged_in), false)

    fun getIsTerminalPrepped(context : Context) = getSharedPreferences(context).getBoolean(context.getString(R.string.key_is_terminal_prepped), false)

    fun setIsTerminalPrepped(context : Context, isTerminalPrepped : Boolean) = getSharedPreferences(context).edit().putBoolean(context.getString(R.string.key_is_terminal_prepped), isTerminalPrepped).apply()


}