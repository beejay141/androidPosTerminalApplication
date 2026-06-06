package com.iisysgroup.androidlite.vas.activity.energy.Eko

import android.arch.persistence.room.Entity
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.iisysgroup.androidlite.models.Pfm

class EkoModel {
    data class EkoLookUpSuccessResponse(@Expose val accountNumber : String,
                                        @Expose val account_type : String,
                                        @Expose val address : String,
                                        @Expose val businessDistrict : String,
                                        @Expose val meterNumber : String,
                                        @Expose val name : String,
                                        @Expose val status : Int,
                                        @Expose val error : String,
                                        @Expose val message : String,
                                        @Expose val minimumPayableAmount : Double) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readInt(),
                parcel.readString(),
                parcel.readString(),
                parcel.readDouble()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(accountNumber)
            parcel.writeString(account_type)
            parcel.writeString(address)
            parcel.writeString(businessDistrict)
            parcel.writeString(meterNumber)
            parcel.writeString(name)
            parcel.writeInt(status)
            parcel.writeString(error)
            parcel.writeString(message)
            parcel.writeDouble(minimumPayableAmount)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<EkoLookUpSuccessResponse> {
            override fun createFromParcel(parcel: Parcel): EkoLookUpSuccessResponse {
                return EkoLookUpSuccessResponse(parcel)
            }

            override fun newArray(size: Int): Array<EkoLookUpSuccessResponse?> {
                return arrayOfNulls(size)
            }
        }
    }


    data class EkoLookUpFailedResponse(@Expose val error : String,
                                       @Expose val message : String,
                                       @Expose val status : Int,
                                       @Expose val response : String)

    data class EkoLookupDetails(@Expose val meter : String)


    data class EkoPayDetails(val meter : String,
                             val amount : String,
                             val terminal_id: String,
                             val user_id : String,
                             val type : String,
                             val phone : String,
                             val password : String,
                             val pin : String,
                             val channel : String,
                             val pfm : Pfm)


    data class EkoPaySuccessResponse(@Expose val error : Boolean,
                                     @Expose val status : Int,
                                     @Expose val customerId : String,
                                     @Expose val amount : String,
                                     @Expose val date : String,
                                     @Expose val account_type : String,
                                     @Expose val address : String,
                                     @Expose val customerBusinessUnit : String,
                                     @Expose val customerMeterNumber : String,
                                     @Expose val payer : String,
                                     @Expose val ref : String,
                                     @Expose val message : String,
                                     @Expose val token : String
                ) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readByte() != 0.toByte(),
                parcel.readInt(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeByte(if (error) 1 else 0)
            parcel.writeInt(status)
            parcel.writeString(customerId)
            parcel.writeString(amount)
            parcel.writeString(date)
            parcel.writeString(account_type)
            parcel.writeString(address)
            parcel.writeString(customerBusinessUnit)
            parcel.writeString(customerMeterNumber)
            parcel.writeString(payer)
            parcel.writeString(ref)
            parcel.writeString(message)
            parcel.writeString(token)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<EkoPaySuccessResponse> {
            override fun createFromParcel(parcel: Parcel): EkoPaySuccessResponse {
                return EkoPaySuccessResponse(parcel)
            }

            override fun newArray(size: Int): Array<EkoPaySuccessResponse?> {
                return arrayOfNulls(size)
            }
        }
    }

    //todo potential bomb here - varied responses
    data class EkoPayFailedResponse(@Expose val status : Int,@Expose val error: String, @Expose val message: String, @Expose val ref : String, @Expose val date : String) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readInt(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(status)
            parcel.writeString(error)
            parcel.writeString(message)
            parcel.writeString(ref)
            parcel.writeString(date)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<EkoPayFailedResponse> {
            override fun createFromParcel(parcel: Parcel): EkoPayFailedResponse {
                return EkoPayFailedResponse(parcel)
            }

            override fun newArray(size: Int): Array<EkoPayFailedResponse?> {
                return arrayOfNulls(size)
            }
        }
    }

    @Entity
    data class EkoBeneficiariesModel(val name : String, val meterNumber : String) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeString(meterNumber)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<EkoBeneficiariesModel> {
            override fun createFromParcel(parcel: Parcel): EkoBeneficiariesModel {
                return EkoBeneficiariesModel(parcel)
            }

            override fun newArray(size: Int): Array<EkoBeneficiariesModel?> {
                return arrayOfNulls(size)
            }
        }
    }

    //data class EkoCardDetails()
}