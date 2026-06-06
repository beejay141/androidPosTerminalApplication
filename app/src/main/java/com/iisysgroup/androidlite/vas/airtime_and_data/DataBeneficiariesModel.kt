package com.iisysgroup.androidlite.vas.airtime_and_data

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity
data class DataBeneficiariesModel(@PrimaryKey val phone_number :  String, val data_provider : String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(phone_number)
        parcel.writeString(data_provider)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataBeneficiariesModel> {
        override fun createFromParcel(parcel: Parcel): DataBeneficiariesModel {
            return DataBeneficiariesModel(parcel)
        }

        override fun newArray(size: Int): Array<DataBeneficiariesModel?> {
            return arrayOfNulls(size)
        }
    }
}