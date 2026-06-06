package com.iisysgroup.androidlite.vas.cable.startimes

import com.google.gson.annotations.Expose
import com.iisysgroup.androidlite.vas.cable.Data

class StartimesModel {
    data class StartimesLookupDetails(
            @Expose val wallet: String,
            @Expose val username: String,
            @Expose val type: String,
            @Expose val channel: String,
            @Expose val smartCardCode : String
    )

    data class LookUpFailedResponse(@Expose val error: String,
                                    @Expose val message: String,
                                    @Expose val status: Int,
                                    @Expose val response: String)
    data class StartimesResponse(@Expose val name : String, @Expose val message: String,
                                 @Expose val error: String,@Expose val bouquet: String,
                                 @Expose val billAmount: String, @Expose val productCode : String,
                                 @Expose val smartCardCode: String)
    data class StartimesPayDetails(@Expose val wallet: String, @Expose val username: String, @Expose val type: String, @Expose val customerName: String,
                                   @Expose val channel: String, @Expose val pin: String, @Expose val smartCardCode : String, @Expose val amount: String,
                                   @Expose val phone: String, @Expose val productCode: String, @Expose val paymentMethod: String = "cash",
                                   @Expose val clientReference: String)
    data class StartimesPaySuccessResponse(@Expose val error: Boolean,
                                    @Expose val status: Int,
                                    @Expose val customerId: String,
                                    @Expose val amountPaid: String,
                                    @Expose val txndate: String,
                                    @Expose val transactionID: String,
                                    @Expose val type: String,
                                    @Expose val address: String,
                                    @Expose val customerMeterNo: String,
                                    @Expose val description: String,
                                    @Expose val token: String,
                                    @Expose val reference: String,
                                    @Expose val message: String,
                                    @Expose val receiptNumber: String)
    data class PayFailedResponse(@Expose val status: Int, @Expose val description: String, @Expose val message: String, @Expose val ref: String, @Expose val date: String)


}