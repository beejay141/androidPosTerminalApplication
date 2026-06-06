package com.iisysgroup.androidlite.login

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Window
import android.view.WindowManager
import com.iisysgroup.androidlite.R
import com.iisysgroup.androidlite.login.securestorage.SecureStorage
import com.iisysgroup.androidlite.payments_menu.MainVasActivity
import com.iisysgroup.androidlite.payments_menu.PaymentsActivity
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.payvice.base.presenter.LoginPresenter
import com.iisysgroup.payvice.base.view.LoginView
import com.iisysgroup.payvice.baseimpl.interactor.LoginInteractorImpl
import com.iisysgroup.payvice.baseimpl.presenter.LoginPresenterImpl
import com.iisysgroup.payvice.securestorage.SecureStorageUtils
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragments_settings.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.okButton
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity(), LoginView {
    override fun showProgress() {
        loginProgressDialog.show()
    }

    override fun dismissProgress() {
        loginProgressDialog.dismiss()

    }

    override fun setInvalidUser() {
        loginProgressDialog.dismiss()
        toast("Your username is invalid")
    }

    override fun setInvalidPassword() {
        loginProgressDialog.dismiss()
        toast("Your password is invalid")
    }

    override fun showMessage(message: String) {
        loginProgressDialog.dismiss()
        toast(message)
    }

    override fun setLoginError(throwable: Throwable) {
        loginProgressDialog.dismiss()
        toast("Login Error")
    }

    override fun setLoginSuccessful() {
        loginProgressDialog.dismiss()
        SharedPreferenceUtils.setUserLoggedIn(this@LoginActivity, true)
        val intent = Intent(this@LoginActivity, MainVasActivity::class.java)
        startActivity(intent)
//        finish()
    }

    override fun setDeviceChangedError() {

    }

    override fun setShouldUpdatePin() {
        alert {
            title = "Change pin"
            message = "You have requested to recover your password, please change the pin using our payvice application - payvice.com"
            okButton {

            }
        }

    }

    private val loginProgressDialog by lazy {
        indeterminateProgressDialog(message = "Logging in")
    }

    private val presenter: LoginPresenter by lazy {
        LoginPresenterImpl(LoginInteractorImpl(applicationContext), this)
    }

    private lateinit var wallet_username : String
    private lateinit var wallet_password : String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_login)

        val payviceUsername = SharedPreferenceUtils.getPayviceUsername(this)

        if (payviceUsername.isNotEmpty()){
            val intent = Intent(this, MainVasActivity::class.java)
            startActivity(intent)
//            finish()
        } else {

            sign_in_button.setOnClickListener {
                wallet_username = username.text.toString()
                wallet_password = password.text.toString()

                if (wallet_username.isEmpty()){
                    username.error = "Please enter a valid username"
                } else if (wallet_password.isEmpty()){
                    password.error = "Please enter a vaid password"
                }

                loginProgressDialog.show()
                presenter.login(wallet_username, wallet_password)


            }
        }
    }
}
