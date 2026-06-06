package com.iisysgroup.androidlite.models

import android.os.Parcel
import android.os.Parcelable

data class BaseReceiptDetails(val terminalID : String) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(terminalID)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BaseReceiptDetails> {
        override fun createFromParcel(parcel: Parcel): BaseReceiptDetails {
            return BaseReceiptDetails(parcel)
        }

        override fun newArray(size: Int): Array<BaseReceiptDetails?> {
            return arrayOfNulls(size)
        }
    }
}