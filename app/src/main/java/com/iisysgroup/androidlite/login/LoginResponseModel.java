package com.iisysgroup.androidlite.login;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.iisysgroup.payvice.models.PayviceForMerchantsSummary;

/**
 * Created by Agbede on 3/26/2018.
 */

public class LoginResponseModel implements Parcelable {

    public static final Creator<LoginResponseModel> CREATOR = new Creator<LoginResponseModel>() {
        @Override
        public LoginResponseModel createFromParcel(Parcel in) {
            return new LoginResponseModel(in);
        }

        @Override
        public LoginResponseModel[] newArray(int size) {
            return new LoginResponseModel[size];
        }
    };
    String status;
    String message;
    String tams;
    @SerializedName("PayviceForMerchants")
    PayviceForMerchants payviceModel;
    @SerializedName("summarry")
    PayviceForMerchantsSummary payviceForMerchantsSummary;

    public LoginResponseModel(String tams, PayviceForMerchants payviceModel,
                              PayviceForMerchantsSummary payviceForMerchantsSummary) {
        this.tams = tams;
        this.payviceModel = payviceModel;
        this.payviceForMerchantsSummary = payviceForMerchantsSummary;
    }

    public LoginResponseModel() {

    }


    protected LoginResponseModel(Parcel in) {
        tams = in.readString();
        payviceModel = in.readParcelable(PayviceForMerchants.class.getClassLoader());
    }

    public PayviceForMerchantsSummary getPayviceForMerchantsSummary() {
        return payviceForMerchantsSummary;
    }

    public void setPayviceForMerchantsSummary(PayviceForMerchantsSummary payviceForMerchantsSummary) {
        this.payviceForMerchantsSummary = payviceForMerchantsSummary;
    }

    public String getTams() {
        return tams;
    }

    public void setTams(String tams) {
        this.tams = tams;
    }

    public PayviceForMerchants getPayviceModel() {
        return payviceModel;
    }

    public void setPayviceModel(PayviceForMerchants payviceModel) {
        this.payviceModel = payviceModel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tams);
        dest.writeParcelable(payviceModel, flags);
    }
}
