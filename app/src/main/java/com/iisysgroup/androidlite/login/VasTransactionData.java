package com.iisysgroup.androidlite.login;


/**
 * Created by Bamitale @Itex on 11/23/2015.
 */
public class VasTransactionData {
    String beneficiary, amount;
    Service.Product product;

    String pin = "";

    public VasTransactionData(String beneficiary, Service.Product product, String amount) {
        this.beneficiary = beneficiary;
        this.product = product;
        this.amount = amount;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getBeneficiary() {
        return beneficiary;
    }


    public Service.Product getProduct() {
        return product;
    }

    public String getAmount() {
        return amount;
    }
}
