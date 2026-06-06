package com.iisysgroup.androidlite.generators

import com.iisysgroup.androidlite.models.Journal
import com.iisysgroup.androidlite.models.PfmJournal
import com.iisysgroup.poslib.ISO.common.IsoConfigData
import com.iisysgroup.poslib.commons.emv.EmvCard
import com.iisysgroup.poslib.deviceinterface.Printer
import com.iisysgroup.poslib.host.Host
import com.iisysgroup.poslib.host.entities.ConfigData
import com.iisysgroup.poslib.host.entities.TransactionResult

class PfmJournalGenerator(private val transactionResult: TransactionResult, private  val configData : ConfigData, private val isReceiptPrinted: Boolean, private val amount : Long, private val cardData : EmvCard?) {
    fun generateJournal(): Journal {

        return Journal(getstan(),getmPan(),getRrn(),getAcode(),amount,getTimeStamp(),getmti(),"",getResp(),getTap(),getRep().toString(),"",getOstan(),getOrrn(),getOacode(),getMid(),"",getMcc())
    }

    private fun getOacode(): String {
        return transactionResult.authID
    }

    private fun getOrrn(): String {
        return transactionResult.RRN
    }

    private fun getOstan(): String {
        return transactionResult.STAN
    }

    private fun getTransMethod(): String {
        return if (transactionResult.PAN.isNotEmpty()){
            "card"
        } else {"cash"}
    }

    //Verification method - OnlinePin, OfflinePin, Signature, NoCVM, Others,
    private fun getVm(): String {
        if (cardData == null)
            return "";
        val vm = if (cardData!!.pinInfo.pinBlock.isEmpty() || cardData.pinInfo.pinBlock == null){
            "offline"
        } else {
            "online"
        }
        return vm
    }

    private fun getRep(): Boolean {
        return isReceiptPrinted
    }

    //Response code from the upstream entity usually field 39 of ISO8583
    private fun getResp(): String {
        return transactionResult.responseCode
    }

    private fun getTap(): Boolean {
        return transactionResult.isApproved
    }

    private fun getMcc(): String {
        return configData.getConfigData(IsoConfigData.TAG_LEN_MERCHANT_CATEGORY_CODE) as String
    }


//    private fun getps(): String {
//        return printer.printerStatus.toString()
//    }

    //todo handle mti
    private fun getmti(): String {
        val mti = if (transactionResult.transactionType == Host.TransactionType.REVERSAL)
            440
        else 200
        return mti.toString()
    }

    private fun getTimeStamp(): String {
        return transactionResult.longDateTime.toString()
    }

    private fun getAmount(): String {
        return transactionResult.amount.toString()
    }

    //todo handle acode
    private fun getAcode(): String {
        return transactionResult.authID
    }

    private fun getRrn(): String {
        return transactionResult.RRN
    }

    private fun getmPan(): String {
        return transactionResult.PAN
    }

    private fun getstan(): String {
        return transactionResult.STAN
    }

    private fun getMid(): String {
        return transactionResult.merchantID
    }


}

