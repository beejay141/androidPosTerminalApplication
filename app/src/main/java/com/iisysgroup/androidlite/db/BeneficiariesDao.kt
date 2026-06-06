package com.iisysgroup.androidlite.db

import android.arch.persistence.room.*
import com.iisysgroup.androidlite.vas.airtime_and_data.DataBeneficiariesModel
import com.iisysgroup.androidlite.vas.cable.DstvBeneficiariesModel

@Dao
interface AirtimeBeneficiariesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(airtimeModel: AirtimeModel)

    @Delete
    fun delete(airtimeModel: AirtimeModel)

    @Query("Select * from AirtimeModel where phone_number = :phone_number")
    fun getAirtimeBeneficiaryByPhoneNumber(phone_number : String) : AirtimeModel

    @Query("Select * from AirtimeModel")
    fun getAllBeneficiaries() : List<AirtimeModel>
}

@Dao
interface DataBeneficiariesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(dataModel: DataBeneficiariesModel)

    @Delete
    fun delete(dataModel: DataBeneficiariesModel)

    @Query("Select * from DataBeneficiariesModel where phone_number = :phone_number")
    fun getDataBeneficiaryByPhoneNumber(phone_number : String) : DataBeneficiariesModel

    @Query("Select * from DataBeneficiariesModel")
    fun getAllBeneficiaries() : List<DataBeneficiariesModel>
}

@Dao
interface DstvBeneficiariesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(dataModel: DstvBeneficiariesModel)

    @Delete
    fun delete(dataModel: DstvBeneficiariesModel)

    @Query("Select * from DstvBeneficiariesModel where dstvNumber = :iuc")
    fun getDstvBeneficiariesByNumber(iuc: String): DstvBeneficiariesModel

    @Query("Select * from DstvBeneficiariesModel")
    fun getAllBeneficiaries(): List<DstvBeneficiariesModel>

}

@Dao
interface GotvBeneficiariesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(dataModel: DstvBeneficiariesModel)

    @Delete
    fun delete(dataModel: DstvBeneficiariesModel)

    @Query("Select * from DstvBeneficiariesModel where dstvNumber = :iuc")
    fun getDstvBeneficiariesByNumber(iuc : String) : DstvBeneficiariesModel

    @Query("Select * from DstvBeneficiariesModel")
    fun getAllBeneficiaries() : List<DstvBeneficiariesModel>
}


