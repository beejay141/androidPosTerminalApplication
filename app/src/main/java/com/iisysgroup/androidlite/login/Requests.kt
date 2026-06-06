package com.iisysgroup.androidlite.login

import android.content.Context
import com.iisysgroup.androidlite.R
import java.util.*

/**
 * Created by Bamitale@Itex on 27/02/2018.
 */


object Requests {

    const val BASE_PFM_URL = "http://www.pfm.payvice.com/api/mobile/"

    @JvmStatic
    fun processRequest(url: String, params: Map<String, String>): VasResult {
        val transactionManager = VasTransactionManager(url, params)
        return transactionManager.processTransaction()
    }


    @JvmStatic
    fun initUser(context: Context, userId: String): VasResult {
        val terminalID = context.getString(R.string.tams_default_terminal_id)

        return initUser(context, terminalID, userId)
    }

    @JvmStatic
    fun initUser(context: Context, terminalID: String, userID: String): VasResult {
        val params = HashMap<String, String>()

        val action = context.getString(R.string.tams_login_action)
        val initControl = context.getString(R.string.tams_init_action)
        val url = context.resources.getString(R.string.tams_url)


        //for init set CONTROL = INIT
        params["action"] = action
        params["termid"] = terminalID
        params["userid"] = userID
        params["control"] = initControl


        return processRequest(url, params)
    }

    @JvmStatic
    fun login(context: Context, userId: String, password: String, walletId: String): VasResult {
        val params = HashMap<String, String>()

        val action = context.getString(R.string.tams_login_action)
        val url = context.resources.getString(R.string.tams_url)

        params["action"] = action
        params["termid"] = walletId
        params["userid"] = userId
        params["password"] = password

        return processRequest(url, params)
    }


}