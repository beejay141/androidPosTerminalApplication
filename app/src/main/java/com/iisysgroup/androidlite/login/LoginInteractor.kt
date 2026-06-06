package com.iisysgroup.payvice.base.interactor

import com.iisysgroup.androidlite.login.PayviceForMerchants
import com.iisysgroup.androidlite.login.VasResult
import com.iisysgroup.payvice.models.PayviceForMerchantsSummary
import io.reactivex.Single

interface LoginInteractor {
    fun getUserInfo(userId: String): Single<VasResult>
    fun login(userID: String, password: String, walletId: String): Single<VasResult>
    fun getDeviceId(): String
    fun storeLoginDetails(userId: String, encryptedPassword: String, key: String, loginResult: VasResult)
    fun storePfmData(data: PayviceForMerchants, summary: PayviceForMerchantsSummary)
}