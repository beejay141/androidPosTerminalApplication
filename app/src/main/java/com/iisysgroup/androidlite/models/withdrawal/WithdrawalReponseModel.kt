package com.iisysgroup.androidlite.models.withdrawal



data class WithdrawalSuccessModel(val status : Int, val error : String, val message : String, val description : description, val transactionID : Int, val convenienceFee : Int, val amountSettled : Int, val amountDebited : Int, val beneficiaryName : String, val beneficiary : String, val reference : String)

data class description(val status : Int, val message: String, val description: String, val reference :  String, val requeries : String, val pending :String, val account: String, val vendorBankCode: String)
