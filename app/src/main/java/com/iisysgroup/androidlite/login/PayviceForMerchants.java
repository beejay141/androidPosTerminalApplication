package com.iisysgroup.androidlite.login;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

/**
 * Created by Agbede on 3/26/2018.
 */

public class PayviceForMerchants implements Parcelable {
    public static final Creator<PayviceForMerchants> CREATOR = new Creator<PayviceForMerchants>() {
        @Override
        public PayviceForMerchants createFromParcel(Parcel in) {
            return new PayviceForMerchants(in);
        }

        @Override
        public PayviceForMerchants[] newArray(int size) {
            return new PayviceForMerchants[size];
        }
    };
    @SerializedName("isMerchant")
    boolean isMerchant;

    @SerializedName("terminals")
    String[] terminals;

    @SerializedName("token")
    String token;

    @SerializedName("totalTransactionCount")
    double totalTransactionCount;

    @SerializedName("totalApprovedTransactions")
    double totalApprovedTransactions;

    @SerializedName("totalServiceCharge")
    double totalServiceCharge;

    @SerializedName("totalAmountSettled")
    double totalAmountSettled;

    @SerializedName("totalFailedTransactions")
    double totalFailedTransactions;

    public PayviceForMerchants() {

    }

    protected PayviceForMerchants(Parcel in) {
        isMerchant = in.readByte() != 0;
        terminals = in.createStringArray();
        token = in.readString();
        totalTransactionCount = in.readDouble();
        totalApprovedTransactions = in.readDouble();
        totalServiceCharge = in.readDouble();
        totalAmountSettled = in.readDouble();
        totalFailedTransactions = in.readDouble();
    }

    @Override
    public String toString() {
        return "PayviceForMerchants{" +
                "isMerchant=" + isMerchant +
                ", terminals=" + Arrays.toString(terminals) +
                ", token='" + token + '\'' +
                ", totalTransactionCount='" + totalTransactionCount + '\'' +
                ", totalApprovedTransactions='" + totalApprovedTransactions + '\'' +
                ", totalServiceCharge='" + totalServiceCharge + '\'' +
                ", totalAmountSettled='" + totalAmountSettled + '\'' +
                ", totalFailedTransactions='" + totalFailedTransactions + '\'' +
                '}';
    }

    public boolean isMerchant() {
        return isMerchant;
    }

    public void setMerchant(boolean merchant) {
        isMerchant = merchant;
    }

    public String[] getTerminals() {
        return terminals;
    }

    public void setTerminals(String[] terminals) {
        this.terminals = terminals;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public double getTotalTransactionCount() {
        return totalTransactionCount;
    }

    public void setTotalTransactionCount(double totalTransactionCount) {
        this.totalTransactionCount = totalTransactionCount;
    }

    public double getTotalApprovedTransactions() {
        return totalApprovedTransactions;
    }

    public void setTotalApprovedTransactions(double totalApprovedTransactions) {
        this.totalApprovedTransactions = totalApprovedTransactions;
    }

    public double getTotalServiceCharge() {
        return totalServiceCharge;
    }

    public void setTotalServiceCharge(double totalServiceCharge) {
        this.totalServiceCharge = totalServiceCharge;
    }

    public double getTotalAmountSettled() {
        return totalAmountSettled;
    }

    public void setTotalAmountSettled(double totalAmountSettled) {
        this.totalAmountSettled = totalAmountSettled;
    }

    public double getTotalFailedTransactions() {
        return totalFailedTransactions;
    }

    public void setTotalFailedTransactions(double totalFailedTransactions) {
        this.totalFailedTransactions = totalFailedTransactions;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isMerchant ? 1 : 0));
        dest.writeStringArray(terminals);
        dest.writeString(token);
        dest.writeDouble(totalTransactionCount);
        dest.writeDouble(totalApprovedTransactions);
        dest.writeDouble(totalServiceCharge);
        dest.writeDouble(totalAmountSettled);
        dest.writeDouble(totalFailedTransactions);
    }
}
