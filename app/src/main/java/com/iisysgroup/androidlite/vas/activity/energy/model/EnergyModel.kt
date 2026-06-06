package com.iisysgroup.androidlite.vas.activity.energy.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.iisysgroup.androidlite.models.Pfm

class EnergyModel {
    data class KanoLookupDetails(@Expose val wallet: String, @Expose val username: String,
                                 @Expose val type: String, @Expose val channel: String,
                                 @Expose val account: String)

    data class KanoLookupSuccessResponse(
            @Expose val status: Int,
            @Expose val error: String,
            @Expose val message: String,
            @Expose val description: String,
            @Expose val name: String,
            @Expose val account: String,
            @Expose val type: String,
            @Expose val address: String,
            @Expose val minimumPayableAmount: String,
            @Expose val productCode: String
    )

    data class LookUpFailedResponse(@Expose val error: String,
                                    @Expose val message: String,
                                    @Expose val status: Int,
                                    @Expose val response: String)

    data class PhLookupDetails(@Expose val wallet: String, @Expose val username: String,
                               @Expose val type: String, @Expose val channel: String,
                               @Expose val account: String)

    data class PhLookupSuccessResponse(
            @Expose val status: Int,
            @Expose val error: String,
            @Expose val message: String,
            @Expose val description: String,
            @Expose val name: String,
            @Expose val account: String,
            @Expose val type: String,
            @Expose val address: String,
            @Expose val tariff: String,
            @Expose val arrears: String,
            @Expose val customerNumber: String,
            @Expose val productCode: String
    )

    data class PhPayDetails(val wallet: String, val username: String,
                            val type: String, val channel: String, val pin: String,
                            val account: String, val amount: String, val phone: String,
                            val productCode: String, val paymentMethod: String = "cash",
                            val clientReference: String, val pfm: Pfm)

    data class PhPayFailedResponse(@Expose val status: Int, @Expose val description: String, @Expose val error: String,
                                   @Expose val message: String, @Expose val ref: String, @Expose val date: String)
    data class PhPaySuccessResponse(@Expose val error: Boolean,
                                    @Expose val status: Int,
                                    @Expose val customerId: String,
                                    @Expose val amount: String,
                                    @Expose val paymentDate: String,
                                    @Expose val transactionID: String,
                                    @Expose val type: String,
                                    @Expose val address: String,
                                    @Expose val token: String,
                                    @Expose val arrears: String,
                                    @Expose val tariff : String,
                                    @Expose val meterNumber: String,
                                    @Expose val name: String,
                                    @Expose val reference: String,
                                    @Expose val message: String,
                                    @Expose val receiptNumber: String
    )

    data class AbjLookupDetails(@Expose val wallet: String, @Expose val username: String,
                                @Expose val requestType: String, @Expose val meterType: String,
                                @Expose val meterNo: String, @Expose val channel: String)

    data class AbjLookupSuccessResponse(
            @Expose val status: Int,
            @Expose val error: String,
            @Expose val message: String,
            @Expose val description: String,
            @Expose val name: String,
            @Expose val account: String,
            @Expose val type: String,
            @Expose val customerMeterNo: String,
            @Expose val minimumPayableAmount: String,
            @Expose val productCode: String
    )

    data class AbjPayDetails(val wallet: String, val username: String, val meterType: String,
                             val requestType: String, val channel: String, val pin: String,
                             val meterNo: String, val amount: String, val phone: String,
                             val productCode: String, val paymentMethod: String = "cash",
                             val pfm: Pfm)

    data class AbjPaySuccessResponse(@Expose val error: Boolean,
                                     @Expose val status: Int,
                                     @Expose val customerId: String,
                                     @Expose val amountPaid: String,
                                     @Expose val txndate: String,
                                     @Expose val transactionID: String,
                                     @Expose val type: String,
                                     @Expose val address: String,
                                     @Expose val token: String,
                                     @Expose val customerMeterNo: String,
                                     @Expose val description: String,
                                     @Expose val name: String,
                                     @Expose val reference: String,
                                     @Expose val message: String,
                                     @Expose val receiptNumber: String)

    data class IbLookupDetails(@Expose val wallet: String, @Expose val username: String,
                               @Expose val type: String,
                               @Expose val account: String, @Expose val channel: String)

    data class IbLookupSuccessResponse(
            @Expose val status: Int,
            @Expose val error: String,
            @Expose val message: String,
            @Expose val description: String,
            @Expose val name: String,
            @Expose val account: String,
            @Expose val type: String,
            @Expose val customerMeterNo: String,
            @Expose val minimumAmount: String,
            @Expose val paymentType: String,
            @Expose val productCode: String
    )

    data class IbPayDetails(val wallet: String, val username: String, @Expose val type: String, val customerName: String,
                            val channel: String, val pin: String, val account: String,val amount: String, val phone: String,
                            val productCode: String, val paymentMethod: String = "cash", val clientReference: String, val pfm: Pfm)

    data class IbPaySuccessResponse(@Expose val error: Boolean,
                                    @Expose val status: Int,
                                    @Expose val customerId: String,
                                    @Expose val amount: String,
                                    @Expose val txndate: String,
                                    @Expose val transactionID: String,
                                    @Expose val type: String,
                                    @Expose val address: String,
                                    @Expose val account: String,
                                    @Expose val description: String,
                                    @Expose val token: String,
                                    @Expose val arrears: String,
                                    @Expose val tariff: String,
                                    @Expose val reference: String,
                                    @Expose val message: String,
                                    @Expose val name: String)

    data class EnuguLookupDetails(@Expose val wallet: String, @Expose val username: String,
                               @Expose val type: String,
                               @Expose val account: String, @Expose val channel: String)
    data class EnuguLookupSuccessResponse(
            @Expose val status: Int,
            @Expose val error: String,
            @Expose val message: String,
            @Expose val description: String,
            @Expose val name: String,
            @Expose val account: String,
            @Expose val type: String,
            @Expose val productCode: String
    )
    data class EnuguPayDetails(val wallet: String, val username: String, val type: String, val customerName: String,
                            val channel: String, val pin: String, val account: String, val amount: String, val phone: String,
                            val productCode: String, val paymentMethod: String = "cash", val clientReference: String, val pfm : Pfm)

    data class EnuguPaySuccessResponse(@Expose val error: Boolean,
                                    @Expose val status: Int,
                                    @Expose val customerId: String,
                                    @Expose val tariff : String,
                                    @Expose val vat: String,
                                    @Expose val transactionID: String,
                                    @Expose val arrears: String,
                                    @Expose val type: String,
                                    @Expose val address: String,
                                    @Expose val customerMeterNo: String,
                                    @Expose val description: String,
                                    @Expose val token: String,
                                    @Expose val invoiceNumber : String,
                                    @Expose val reference: String,
                                    @Expose val message: String,
                                    @Expose val receiptNumber: String)
}
