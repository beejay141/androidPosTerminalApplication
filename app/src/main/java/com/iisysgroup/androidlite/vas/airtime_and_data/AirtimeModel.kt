package com.iisysgroup.androidlite.vas.airtime_and_data

import com.google.gson.annotations.Expose
import com.iisysgroup.androidlite.models.PfmDetails


data class AirtimeSuccessResponse(@Expose val error : Boolean, @Expose val message: String, @Expose val amount: Boolean, @Expose val ref : String, @Expose val date : String, @Expose val transactionID: String)

data class AirtimeFailedResponse(@Expose val error : Boolean, @Expose val message: String, @Expose val ref : String, @Expose val date : String)

data class AirtimeRequestDetails(@Expose val terminal_id : String, @Expose val user_id: String, @Expose val amount: String, @Expose val phone : String, @Expose val service : String, @Expose val pin : String, @Expose val password : String)