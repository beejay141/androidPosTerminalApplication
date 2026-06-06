package com.iisysgroup.androidlite.payments_menu.handlers

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import com.iisysgroup.poslib.deviceinterface.interactors.EmvInteractor
import com.iisysgroup.poslib.host.HostInteractor
import com.iisysgroup.poslib.host.dao.PosLibDatabase
import com.iisysgroup.poslib.host.entities.ConfigData
import com.iisysgroup.poslib.host.entities.ConnectionData
import com.iisysgroup.poslib.host.entities.KeyHolder
import com.iisysgroup.poslib.host.entities.TransactionResult
import org.jetbrains.anko.doAsync

/**
 * Created by Bamitale@Itex on 06/03/2018.
 */
abstract class BaseHandler(val owner: LifecycleOwner, val db: PosLibDatabase,
                           val hostInteractor: HostInteractor, val connData: ConnectionData,
                           val emvInteractor: EmvInteractor, val pinKey : String? = "", val sessionKey : String? = "") {

    lateinit var configData: ConfigData
    lateinit var keyHolder: KeyHolder

    init {
        doAsync {
            configData =  db.configDataDao.get()
            keyHolder = db.keyHolderDao.get()
        }
    }

    abstract fun getTransactionResult(): LiveData<TransactionResult>

}