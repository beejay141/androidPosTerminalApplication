package com.iisysgroup.payvice.baseimpl.interactor

import android.content.Context
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.login.*
import com.iisysgroup.androidlite.login.securestorage.SecureStorage
import com.iisysgroup.payvice.base.interactor.LoginInteractor
import com.iisysgroup.payvice.models.PayviceForMerchantsSummary
import io.reactivex.Single

class LoginInteractorImpl(private val context: Context) : LoginInteractor {


    override fun storePfmData(data: PayviceForMerchants, summary: PayviceForMerchantsSummary) {

    }


    override fun getUserInfo(userId: String): Single<VasResult> {
        return Single.fromCallable {
            Requests.initUser(context, userId)
        }
    }

    override fun login(userID: String, password: String, walletId: String): Single<VasResult> {
        return Single.fromCallable {
            Requests.login(context, userID, password, walletId)
        }
    }


    override fun getDeviceId(): String = Helper.getDeviceID(context)


    override fun storeLoginDetails(userId: String, encryptedPassword: String, key: String, loginResult: VasResult) {
        val message = loginResult.message
        val balance = "\u20A6" + loginResult.balance.substring(1)
        val walletId = loginResult.macrosTID

        if (message.contains("<macros>")) {
            val storage = MacroStorage()
            storage.store(context, message, userId, walletId, encryptedPassword, key, balance)

        }



        SecureStorage.store(Helper.USER_ID, userId)
        SecureStorage.store(Helper.BALANCE, balance)
        SecureStorage.store(Helper.TERMINAL_ID, walletId)
        SecureStorage.store(Helper.STORED_PASSWORD, encryptedPassword)

    }

    /*override fun storePfmData(data: PayviceForMerchants, summary: PayviceForMerchantsSummary) {
        if (data.isMerchant) {
            thread {
                val appContext = this@LoginInteractorImpl.context
                val mDb = LoginMerchantsDataDb.getInstance(appContext)
                val mDbWorker = DbWorker(appContext, mDb)

                mDbWorker.insertDataInDb(data, summary)
            }
        }
    }*/
}