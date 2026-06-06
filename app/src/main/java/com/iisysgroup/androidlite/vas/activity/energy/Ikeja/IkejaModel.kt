package com.iisysgroup.androidlite.vas.activity.energy.Ikeja

import android.arch.persistence.room.Entity
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.iisysgroup.androidlite.models.Pfm

class IkejaModel {

    data class IkejaLookUpSuccessResponse(@Expose val error : String, @Expose val name : String, @Expose val address : String, @Expose val agent : String)

    data class IkejaLookUpFailedResponse(@Expose val error : String, @Expose val message : String, @Expose val date : String, @Expose val ref : String)

    data class IkejaLookupDetails(@Expose val terminal_id: String, @Expose val user_id : String, @Expose val password : String, @Expose val meter : String, @Expose val account : String, @Expose val type : String = "getcus", @Expose val service_type : String = "pay")


    data class IkejaPayDetails(@Expose val terminal_id: String, @Expose val user_id : String, @Expose val amount : String, @Expose val phone : String, @Expose val pin : String, @Expose val password : String, @Expose val meter : String, @Expose val account: String, @Expose val type : String = "getcus", @Expose val service_type : String, val pfm: Pfm)

    data class IkejaPrePayDetails(val terminal_id: String, val terminal: String, val user_id : String, val amount : String, val phone : String,
                                  val pin : String, val password : String, val meter : String, val type : String = "getcus", val service_type : String,val clientReference: String, val pfm: Pfm)


    data class IkejaPaySuccessResponse(@Expose val error : String, @Expose val message : String, @Expose val date : String, @Expose val ref : String, @Expose val token : String, @Expose val address : String, @Expose val payer : String, @Expose val amount : String, @Expose val type : String, @Expose val client_id : String, @Expose val sgc : String,@Expose val msno : String, @Expose val tran_id : String, @Expose val krn : String, @Expose val ti : String, @Expose val tt : String, @Expose val unit : String, @Expose val unit_value : String, @Expose val unit_cost : String, @Expose val vat : String, @Expose val response_code : String, @Expose val agent : String)

    data class IkejaPayFailedResponse(@Expose val error : String, @Expose val message: String, @Expose val date : String, @Expose val ref : String)

    @Entity
    data class IkejaBeneficiariesModel(val name : String, val customerNumber : String) :  Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeString(customerNumber)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<IkejaBeneficiariesModel> {
            override fun createFromParcel(parcel: Parcel): IkejaBeneficiariesModel {
                return IkejaBeneficiariesModel(parcel)
            }

            override fun newArray(size: Int): Array<IkejaBeneficiariesModel?> {
                return arrayOfNulls(size)
            }
        }
    }
}