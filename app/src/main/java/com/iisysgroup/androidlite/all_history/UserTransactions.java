package com.iisysgroup.androidlite.all_history;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserTransactions {

    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("error")
    @Expose
    private Boolean error;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("walletID")
    @Expose
    private String walletID;
    @SerializedName("currentViewWallet")
    @Expose
    private String currentViewWallet;
    @SerializedName("currentViewName")
    @Expose
    private String currentViewName;
    @SerializedName("permission")
    @Expose
    private Boolean permission;
    @SerializedName("childAgent")
    @Expose
    private Boolean childAgent;
    //    @SerializedName("transactionSummarry")
//    @Expose
//    private TransactionSummarry transactionSummarry;
    @SerializedName("transactions")
    @Expose
    private List<Transaction> transactions = null;
    @SerializedName("user")
    @Expose
    private String user;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("phone")
    @Expose
    private Object phone;
    @SerializedName("subAgents")
    @Expose
    private List<SubAgent> subAgents = null;
    @SerializedName("maj")
    @Expose
    private String maj;
    @SerializedName("massj")
    @Expose
    private List<String> massj = null;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getWalletID() {
        return walletID;
    }

    public void setWalletID(String walletID) {
        this.walletID = walletID;
    }

    public String getCurrentViewWallet() {
        return currentViewWallet;
    }

    public void setCurrentViewWallet(String currentViewWallet) {
        this.currentViewWallet = currentViewWallet;
    }

    public String getCurrentViewName() {
        return currentViewName;
    }

    public void setCurrentViewName(String currentViewName) {
        this.currentViewName = currentViewName;
    }

    public Boolean getPermission() {
        return permission;
    }

    public void setPermission(Boolean permission) {
        this.permission = permission;
    }

    public Boolean getChildAgent() {
        return childAgent;
    }

    public void setChildAgent(Boolean childAgent) {
        this.childAgent = childAgent;
    }

//    public TransactionSummarry getTransactionSummarry() {
//        return transactionSummarry;
//    }
//
//    public void setTransactionSummarry(TransactionSummarry transactionSummarry) {
//        this.transactionSummarry = transactionSummarry;
//    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Object getPhone() {
        return phone;
    }

    public void setPhone(Object phone) {
        this.phone = phone;
    }

    public List<SubAgent> getSubAgents() {
        return subAgents;
    }

    public void setSubAgents(List<SubAgent> subAgents) {
        this.subAgents = subAgents;
    }

    public String getMaj() {
        return maj;
    }

    public void setMaj(String maj) {
        this.maj = maj;
    }

    public List<String> getMassj() {
        return massj;
    }

    public void setMassj(List<String> massj) {
        this.massj = massj;
    }
}

