package com.iisysgroup.androidlite.payments_menu.models

import com.google.gson.annotations.Expose
import com.iisysgroup.androidlite.models.Pfm
import com.iisysgroup.androidlite.payments_menu.transfer.TransferAmountEntry
import java.io.Serializable

data class  AccountLookUpDetailTransfer( @Expose val wallet : String, @Expose val username : String, @Expose val password : String,  @Expose val beneficiary : String, @Expose val vendorBankCode : String, @Expose val type : String, @Expose val amount: Double, @Expose val channel : String)

data class AccountLookUpDetailWithdrawal(@Expose val wallet : String, @Expose val username : String, @Expose val password : String, @Expose val type : String, @Expose val amount: Float, @Expose val channel : String)


data class TransactionDetails(@Expose val action : String, @Expose val method : String, @Expose val amount : String, @Expose val beneficiary: String, @Expose val vendorBankCode: String, @Expose val walletID: String, @Expose val username: String, @Expose val password: String, @Expose val pin : String)

data class TransactionResponse(@Expose val status : Int, @Expose val message : String)

//data class TransferDetails(val transactionType : TransferAmountEntry.TRANSACTION_TYPE, val isApproved : Boolean, val beneficiary : String, val amount : String, val fee : String, val bankName : String, val terminalId : String)

//data class WithdrawalDetails(val transactionType : TransferAmountEntry.TRANSACTION_TYPE, val isApproved : Boolean, val beneficiary : String, val amount : String, val fee : String, val bankName : String, val terminalId : String)

data class TransferDetail(val wallet : String, val username : String, val password : String, val pin : String, val type : String, val amount : Long, val beneficiary : String, val vendorBankCode : String, val channel : String, val paymentMethod : String, val productCode : String)

data class TransferDetails(val wallet : String, val username : String, val password : String, val pin : String, val type : String, val amount : Float, val phone : String, val beneficiary : String, val vendorBankCode : String, val channel : String, val paymentMethod : String, val productCode: String, val pfm: Pfm)

data class WithdrawalDetails(val wallet : String, val username : String, val password : String, val pin : String, val type : String, val amount : Double, val phone : String, val vendorBankCode : String, val channel : String, val paymentMethod : String, val productCode: String, val pfm : Pfm) : Serializable