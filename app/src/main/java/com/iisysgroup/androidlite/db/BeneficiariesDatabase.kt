package com.iisysgroup.androidlite.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.iisysgroup.androidlite.vas.airtime_and_data.DataBeneficiariesModel
import com.iisysgroup.androidlite.vas.cable.DstvBeneficiariesModel

@Database(entities = [(AirtimeModel::class), (DataBeneficiariesModel::class), (DstvBeneficiariesModel::class)], version = 1, exportSchema = false)
abstract class BeneficiariesDatabase() : RoomDatabase() {
    abstract fun getAirtimeBeneficiariesDao() : AirtimeBeneficiariesDao

    abstract fun getDataBeneficiariesDao() : DataBeneficiariesDao

    //abstract fun getBankAccountBeneficiariesDao() : DstvBeneficiariesDao


    abstract fun getDstvBeneficiariesDao() : DstvBeneficiariesDao

    abstract fun getGotvBeneficiariesDao() : GotvBeneficiariesDao

    /*
    abstract fun getIkejaElectricBeneficiariesDao() : IkejaElecticBeneficiaries */

}