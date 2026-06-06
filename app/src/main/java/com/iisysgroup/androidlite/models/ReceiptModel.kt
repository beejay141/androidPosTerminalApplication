package com.iisysgroup.androidlite.models

import java.io.Serializable

data class ReceiptModel(val date : String, val transactionType : String, val transactionStatus : String, val map : HashMap<String, String>, val amount : String, val transactionStatusReason : String) : Serializable