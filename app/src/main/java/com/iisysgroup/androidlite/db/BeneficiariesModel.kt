package com.iisysgroup.androidlite.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity
data class AirtimeModel(@PrimaryKey val phone_number : String, val airtime_provider : String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(phone_number)
        parcel.writeString(airtime_provider)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AirtimeModel> {
        override fun createFromParcel(parcel: Parcel): AirtimeModel {
            return AirtimeModel(parcel)
        }

        override fun newArray(size: Int): Array<AirtimeModel?> {
            return arrayOfNulls(size)
        }
    }
}

@Entity
data class BankAccountModel(@PrimaryKey val bank_account : String, val bank_name : String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(bank_account)
        parcel.writeString(bank_name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BankAccountModel> {
        override fun createFromParcel(parcel: Parcel): BankAccountModel {
            return BankAccountModel(parcel)
        }

        override fun newArray(size: Int): Array<BankAccountModel?> {
            return arrayOfNulls(size)
        }
    }
}

@Entity
data class DstvModel(@PrimaryKey val dstvNumber : String) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(dstvNumber)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DstvModel> {
        override fun createFromParcel(parcel: Parcel): DstvModel {
            return DstvModel(parcel)
        }

        override fun newArray(size: Int): Array<DstvModel?> {
            return arrayOfNulls(size)
        }
    }
}

@Entity
data class IkejaElectricModel(@PrimaryKey val ikejaElectricNumber : String) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(ikejaElectricNumber)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<IkejaElectricModel> {
        override fun createFromParcel(parcel: Parcel): IkejaElectricModel {
            return IkejaElectricModel(parcel)
        }

        override fun newArray(size: Int): Array<IkejaElectricModel?> {
            return arrayOfNulls(size)
        }
    }
}