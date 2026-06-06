package com.iisysgroup.payvice.models

import java.util.*

/**
 * Created by simileoluwaaluko on 20/05/2018.
 */
data class PayviceForMerchantsSummary(
        var balance: String,
        var hist: String,
        var list: String,
        var journalCount: String,
        var myJournal: String,
        var merchantId: String,
        var successful: Array<Any>,
        var successfulCount: Int,
        var successfulSum: Double,
        var failedCount: Int,
        var failedSum: Double,
        var successfulTransactions: String,
        var unSuccessfulTransactions: String,
        var xAxisTicks: String,
        var terminalTransactions: Array<PfmTerminalTransactions>,
        var purchaseTransactionsCount: Int,
        var purchaseTransactionsSum: Double,
        var airtimeInternetTransactionsCount: Int,
        var airtimeInternetTransactionsSum: Double,
        var electricityTransactionsCount: Int,
        var electricityTransactionsSum: Double,
        var cableTransactionsCount: Int,
        var cableTransactionsSum: Double,
        var cardTransactionsCount: Int,
        var cardTransactionsSum: Double,
        var cashTransactionsCount: Int,
        var cashTransactionsSum: Double
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PayviceForMerchantsSummary

        if (balance != other.balance) return false
        if (hist != other.hist) return false
        if (list != other.list) return false
        if (journalCount != other.journalCount) return false
        if (myJournal != other.myJournal) return false
        if (merchantId != other.merchantId) return false
        if (successful != other.successful) return false
        if (successfulCount != other.successfulCount) return false
        if (successfulSum != other.successfulSum) return false
        if (failedCount != other.failedCount) return false
        if (failedSum != other.failedSum) return false
        if (successfulTransactions != other.successfulTransactions) return false
        if (unSuccessfulTransactions != other.unSuccessfulTransactions) return false
        if (xAxisTicks != other.xAxisTicks) return false
        if (!Arrays.equals(terminalTransactions, other.terminalTransactions)) return false
        if (purchaseTransactionsCount != other.purchaseTransactionsCount) return false
        if (purchaseTransactionsSum != other.purchaseTransactionsSum) return false
        if (airtimeInternetTransactionsCount != other.airtimeInternetTransactionsCount) return false
        if (airtimeInternetTransactionsSum != other.airtimeInternetTransactionsSum) return false
        if (electricityTransactionsCount != other.electricityTransactionsCount) return false
        if (electricityTransactionsSum != other.electricityTransactionsSum) return false
        if (cableTransactionsCount != other.cableTransactionsCount) return false
        if (cableTransactionsSum != other.cableTransactionsSum) return false
        if (cardTransactionsCount != other.cardTransactionsCount) return false
        if (cardTransactionsSum != other.cardTransactionsSum) return false
        if (cashTransactionsCount != other.cashTransactionsCount) return false
        if (cashTransactionsSum != other.cashTransactionsSum) return false

        return true
    }

    override fun hashCode(): Int {
        var result = balance.hashCode()
        result = 31 * result + hist.hashCode()
        result = 31 * result + list.hashCode()
        result = 31 * result + journalCount.hashCode()
        result = 31 * result + myJournal.hashCode()
        result = 31 * result + merchantId.hashCode()
        result = 31 * result + successful.hashCode()
        result = 31 * result + successfulCount
        result = 31 * result + successfulSum.hashCode()
        result = 31 * result + failedCount
        result = 31 * result + failedSum.hashCode()
        result = 31 * result + successfulTransactions.hashCode()
        result = 31 * result + unSuccessfulTransactions.hashCode()
        result = 31 * result + xAxisTicks.hashCode()
        result = 31 * result + Arrays.hashCode(terminalTransactions)
        result = 31 * result + purchaseTransactionsCount
        result = 31 * result + purchaseTransactionsSum.hashCode()
        result = 31 * result + airtimeInternetTransactionsCount
        result = 31 * result + airtimeInternetTransactionsSum.hashCode()
        result = 31 * result + electricityTransactionsCount
        result = 31 * result + electricityTransactionsSum.hashCode()
        result = 31 * result + cableTransactionsCount
        result = 31 * result + cableTransactionsSum.hashCode()
        result = 31 * result + cardTransactionsCount
        result = 31 * result + cardTransactionsSum.hashCode()
        result = 31 * result + cashTransactionsCount
        result = 31 * result + cashTransactionsSum.hashCode()
        return result
    }
}

