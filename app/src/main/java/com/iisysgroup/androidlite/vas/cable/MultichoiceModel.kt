package com.iisysgroup.androidlite.vas.cable

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.iisysgroup.androidlite.models.Pfm
import com.iisysgroup.poslib.host.entities.TransactionResult

data class DstvLookup(@Expose val unit : String = "dstv", @Expose val iuc : String)

data class GotvLookupModel(@Expose val unit : String = "gotv", @Expose val iuc : String)

data class DstvResponse(@Expose val fullname : String?, @Expose val unit : String?, @Expose val data : List<Data>)


data class Data(@Expose val name : String, @Expose val product_code : String, @Expose val amount : String)

data class PayDetails(val iuc: String, val product_code: String, val user_id : String,
                      val terminal_id : String, val pin: String, val reference : String = "",
                      val unit : String, val pfm: Pfm)

data class PayResponse(@Expose val error : Boolean, @Expose val message : String, @Expose val ref : String, @Expose val date : String)

data class PfmDetails(val state : PfmState, val journal : PfmJournal)


data class PfmState(val serialNumber : String, val currentTime : String, val batteryLevel : String, val chargingStatus : String, val paperStatus : String, val terminal_id: String, val result : TransactionResult, val communicationsMethod : String, val currentLocation : String, val signalStrength: String, val terminalModel : String, val terminalManufacturer : String, val hasBattery : String = "true", val softwareVersion : String, val lastTransactionTime : String, val pads : String)

data class PfmJournal(val mid: String, val stan: String, val mPan: String, val rrn: String, val acode: String, val amount: String, val timeStamp: String, val mti: String, val ps: String, val resp: String, val tap: String, val rep: Boolean, val vm: Boolean, val vasProduct: String, val vasCategory: String, val mcc: String, val transMethod: String, val ostan: String, val orrn: String, val oacode: String)

@Entity
data class DstvBeneficiariesModel(@PrimaryKey val dstvNumber : String, val name : String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(dstvNumber)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DstvBeneficiariesModel> {
        override fun createFromParcel(parcel: Parcel): DstvBeneficiariesModel {
            return DstvBeneficiariesModel(parcel)
        }

        override fun newArray(size: Int): Array<DstvBeneficiariesModel?> {
            return arrayOfNulls(size)
        }
    }
}
