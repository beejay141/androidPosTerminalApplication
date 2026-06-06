package com.iisysgroup.androidlite.all_history;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TransactionHistory {

    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("error")
    @Expose
    private Boolean error;
    @SerializedName("errors")
    @Expose
    private List<Object> errors = null;
    @SerializedName("userTransactions")
    @Expose
    private UserTransactions userTransactions;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public List<Object> getErrors() {
        return errors;
    }

    public void setErrors(List<Object> errors) {
        this.errors = errors;
    }

    public UserTransactions getUserTransactions() {
        return userTransactions;
    }

    public void setUserTransactions(UserTransactions userTransactions) {
        this.userTransactions = userTransactions;
    }

}
