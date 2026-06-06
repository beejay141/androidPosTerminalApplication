package com.iisysgroup.payvice.baseimpl.presenter

import android.util.Log
import com.iisysgroup.androidlite.login.Helper
import com.iisysgroup.androidlite.login.VasResult
import com.iisysgroup.androidlite.login.securestorage.SecureStorage
import com.iisysgroup.payvice.base.interactor.LoginInteractor
import com.iisysgroup.payvice.base.presenter.LoginPresenter
import com.iisysgroup.payvice.base.view.LoginView
import com.iisysgroup.payvice.securestorage.SecureStorageUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class LoginPresenterImpl(private val interactor: LoginInteractor,
                         private val view: LoginView) : LoginPresenter {

    override fun login(userID: String, password: String)
    {

        if (userID.isBlank()) {
            view.setInvalidUser()
            return
        }

        if (password.isBlank()) {
            view.setInvalidPassword()
            return
        }

        view.showProgress()

        interactor.getUserInfo(userID).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result, error ->
                    error?.let(this::handleError)

                    result?.let {
                        if (result.result != VasResult.Result.APPROVED) {
                            handleError(RuntimeException(result.message))

                        } else {
                            onUserInitResponse(userID, password, result)
                            Log.d("OkH", result.message)
                        }
                    }
                }
    }

    private fun onUserInitResponse(userId: String, password: String, result: VasResult) {

        val temp = result.message.trim().split("\\|".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        val serverKey = temp[0] + "|" + temp[1]

        SecureStorage.store(Helper.USER_KEY, serverKey)


        /*if (temp.size >= 3) {
            val returnedDeviceID = temp[2]
            val deviceID = interactor.getDeviceId()

            *//*if (returnedDeviceID != deviceID) {
                view.dismissProgress()
                view.setDeviceChangedError()
                return
            }*//*
        }*/

        if (temp.size == 4) {
            if (temp[3].trim({ it <= ' ' }).toLowerCase().equals("yes", ignoreCase = true)) {
                view.setShouldUpdatePin()
                return
            }
        }

        val terminalId = result.macrosTID
        SecureStorage.store(Helper.TERMINAL_ID, terminalId)

        val userKey = Helper.decryptUserKey(terminalId, serverKey)

        SecureStorage.store(Helper.PLAIN_PASSWORD, password)


        val ePassword = SecureStorageUtils.hashIt(password, userKey)!!

        interactor.login(userId, ePassword, terminalId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    response, error ->

                    error?.let {
                        handleError(it)
                    }
                    response?.let {

                        if (it.result == VasResult.Result.APPROVED){
                            interactor.storeLoginDetails(userId, ePassword, serverKey, result)


                            view.setLoginSuccessful()
                        } else {
                            view.dismissProgress()
                            view.showMessage(it.message)

                        }


                    }
                }
    }


    private fun handleError(error: Throwable) {
        view.dismissProgress()
        view.setLoginError(error)
    }
}