package com.iisysgroup.androidlite.all_history;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("reference")
    @Expose
    private String reference;
    @SerializedName("terminalIRN")
    @Expose
    private String terminalIRN;
    @SerializedName("account")
    @Expose
    private String account;
    @SerializedName("product")
    @Expose
    private String product;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("amount")
    @Expose
    private String amount;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("paymentMethod")
    @Expose
    private String paymentMethod;
    @SerializedName("pan")
    @Expose
    private String pan;
    @SerializedName("status")
    @Expose
    private Boolean status;
    @SerializedName("reversed")
    @Expose
    private Boolean reversed;
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("dateSentence")
    @Expose
    private String dateSentence;
    @SerializedName("merchantIRN")
    @Expose
    private String merchantIRN;
    @SerializedName("productReference")
    @Expose
    private String productReference;
    @SerializedName("internalAuditReference")
    @Expose
    private String internalAuditReference;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("beneficiary")
    @Expose
    private String beneficiary;
    @SerializedName("reversalReference")
    @Expose
    private String reversalReference;
    @SerializedName("auditReference")
    @Expose
    private String auditReference;
    @SerializedName("balanceReference")
    @Expose
    private String balanceReference;
    @SerializedName("auditAccountReference")
    @Expose
    private String auditAccountReference;
    @SerializedName("auditAmount")
    @Expose
    private String auditAmount;
    @SerializedName("auditDescription")
    @Expose
    private String auditDescription;
    @SerializedName("auditUserReference")
    @Expose
    private String auditUserReference;
    @SerializedName("auditBalanceReference")
    @Expose
    private String auditBalanceReference;
    @SerializedName("balanceAfter")
    @Expose
    private String balanceAfter;
    @SerializedName("commissionReference")
    @Expose
    private Object commissionReference;
    @SerializedName("commissionAmount")
    @Expose
    private String commissionAmount;
    @SerializedName("originalTransactionReference")
    @Expose
    private Object originalTransactionReference;
    @SerializedName("originalTransactionAmount")
    @Expose
    private String originalTransactionAmount;
    @SerializedName("originalTransactionCategory")
    @Expose
    private Object originalTransactionCategory;
    @SerializedName("originalTransactionType")
    @Expose
    private Object originalTransactionType;
    @SerializedName("originalTransactionProduct")
    @Expose
    private Object originalTransactionProduct;
    @SerializedName("originalTransactionDescription")
    @Expose
    private Object originalTransactionDescription;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTerminalIRN() {
        return terminalIRN;
    }

    public void setTerminalIRN(String terminalIRN) {
        this.terminalIRN = terminalIRN;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Object getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Boolean getReversed() {
        return reversed;
    }

    public void setReversed(Boolean reversed) {
        this.reversed = reversed;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateSentence() {
        return dateSentence;
    }

    public void setDateSentence(String dateSentence) {
        this.dateSentence = dateSentence;
    }

    public String getMerchantIRN() {
        return merchantIRN;
    }

    public void setMerchantIRN(String merchantIRN) {
        this.merchantIRN = merchantIRN;
    }

    public String getProductReference() {
        return productReference;
    }

    public void setProductReference(String productReference) {
        this.productReference = productReference;
    }

    public String getInternalAuditReference() {
        return internalAuditReference;
    }

    public void setInternalAuditReference(String internalAuditReference) {
        this.internalAuditReference = internalAuditReference;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(String beneficiary) {
        this.beneficiary = beneficiary;
    }

    public Object getReversalReference() {
        return reversalReference;
    }

    public void setReversalReference(String reversalReference) {
        this.reversalReference = reversalReference;
    }

    public String getAuditReference() {
        return auditReference;
    }

    public void setAuditReference(String auditReference) {
        this.auditReference = auditReference;
    }

    public String getBalanceReference() {
        return balanceReference;
    }

    public void setBalanceReference(String balanceReference) {
        this.balanceReference = balanceReference;
    }

    public String getAuditAccountReference() {
        return auditAccountReference;
    }

    public void setAuditAccountReference(String auditAccountReference) {
        this.auditAccountReference = auditAccountReference;
    }

    public String getAuditAmount() {
        return auditAmount;
    }

    public void setAuditAmount(String auditAmount) {
        this.auditAmount = auditAmount;
    }

    public String getAuditDescription() {
        return auditDescription;
    }

    public void setAuditDescription(String auditDescription) {
        this.auditDescription = auditDescription;
    }

    public String getAuditUserReference() {
        return auditUserReference;
    }

    public void setAuditUserReference(String auditUserReference) {
        this.auditUserReference = auditUserReference;
    }

    public Object getAuditBalanceReference() {
        return auditBalanceReference;
    }

    public void setAuditBalanceReference(String auditBalanceReference) {
        this.auditBalanceReference = auditBalanceReference;
    }

    public String getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(String balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public Object getCommissionReference() {
        return commissionReference;
    }

    public void setCommissionReference(Object commissionReference) {
        this.commissionReference = commissionReference;
    }

    public String getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(String commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public Object getOriginalTransactionReference() {
        return originalTransactionReference;
    }

    public void setOriginalTransactionReference(String originalTransactionReference) {
        this.originalTransactionReference = originalTransactionReference;
    }

    public String getOriginalTransactionAmount() {
        return originalTransactionAmount;
    }

    public void setOriginalTransactionAmount(String originalTransactionAmount) {
        this.originalTransactionAmount = originalTransactionAmount;
    }

    public Object getOriginalTransactionCategory() {
        return originalTransactionCategory;
    }

    public void setOriginalTransactionCategory(Object originalTransactionCategory) {
        this.originalTransactionCategory = originalTransactionCategory;
    }

    public Object getOriginalTransactionType() {
        return originalTransactionType;
    }

    public void setOriginalTransactionType(Object originalTransactionType) {
        this.originalTransactionType = originalTransactionType;
    }

    public Object getOriginalTransactionProduct() {
        return originalTransactionProduct;
    }

    public void setOriginalTransactionProduct(Object originalTransactionProduct) {
        this.originalTransactionProduct = originalTransactionProduct;
    }

    public Object getOriginalTransactionDescription() {
        return originalTransactionDescription;
    }

    public void setOriginalTransactionDescription(Object originalTransactionDescription) {
        this.originalTransactionDescription = originalTransactionDescription;
    }
}
