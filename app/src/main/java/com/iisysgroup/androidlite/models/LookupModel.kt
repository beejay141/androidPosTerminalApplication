package com.iisysgroup.androidlite.models

import com.iisysgroup.androidlite.models.transfer.description
import org.stringtemplate.v4.STRawGroupDir


data class LookupSuccessModel(val status : Int, val message : String, val description : description, val convenienceFee : Int, val amountSettled : Int, val amountCharged : Int, val beneficiaryName : String, val account : String, val vendorBankCode : String, val productCode : String)

data class WithdrawalLookupSuccessModel(val status : Int, val error : String, val message : String, val description : String, val convenienceFee : Int, val amountSettled : Int, val amountToDebit : Int, val percentageCharged : String, val beneficiaryName : String, val beneficiaryWallet : String, val productCode : String)

data class description(val GetAccountInOtherBankResult : Response)

data class Response(val CODE : String, val ACCOUNTNAME : String)

data class LookupFailedModel(val status : Int, val message : String)

//@SerializedName("GetAccountInGTBResult") val result : GetAccountInGTBResult

/*data class GetAccountInGTBResult(@SerializedName("Response") val response : Response)

data class Response(@SerializedName("CODE")val CODE: String, @SerializedName("ACCOUNTNAME") val ACCOUNTNAME: String)*/

