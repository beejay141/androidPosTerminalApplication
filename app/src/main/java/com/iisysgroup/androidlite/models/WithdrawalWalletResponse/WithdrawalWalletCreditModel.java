package com.iisysgroup.androidlite.models.WithdrawalWalletResponse;

public class WithdrawalWalletCreditModel {


    private int transactionID, status, amountSettled, percentageCharged, convenienceFee;
    private String message, beneficiaryName, reference, beneficiaryWallet, description;
    private Boolean error;

    public int getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getAmountSettled() {
        return amountSettled;
    }

    public void setAmountSettled(int amountSettled) {
        this.amountSettled = amountSettled;
    }

    public int getPercentageCharged() {
        return percentageCharged;
    }

    public void setPercentageCharged(int percentageCharged) {
        this.percentageCharged = percentageCharged;
    }

    public boolean getError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public WithdrawalWalletCreditModel(int transactionID, int status, int amountSettled, int percentageCharged, Boolean error, String message, String beneficiaryName, String reference) {
        this.transactionID = transactionID;
        this.status = status;
        this.amountSettled = amountSettled;
        this.percentageCharged = percentageCharged;
        this.error = error;
        this.message = message;
        this.beneficiaryName = beneficiaryName;
        this.reference = reference;
    }

    public WithdrawalWalletCreditModel(int status, Boolean error, String message, String description, int convenienceFee, int amountSettled, int percentageCharged, String beneficiaryName, String beneficiaryWallet, String reference){

        this.status = status;
        this.error = error;
        this.message = message;
        this.description = description;
        this.convenienceFee = convenienceFee;
        this.amountSettled = amountSettled;
        this.percentageCharged = percentageCharged;
        this.beneficiaryName = beneficiaryName;
        this.beneficiaryWallet = beneficiaryWallet;
        this.reference = reference;

    }

        
}
