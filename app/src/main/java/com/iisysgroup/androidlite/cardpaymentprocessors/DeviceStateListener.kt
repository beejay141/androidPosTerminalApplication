package com.iisysgroup.androidlite.cardpaymentprocessors

class DeviceStateListener {


    interface OnInsertCardListener : BaseListener {
        fun onInsertCard()
    }

    interface OnTransactionFailedListener : BaseListener {
        fun onTransactionFailed()
    }

    interface OnTransactionDeclinedListener : BaseListener {
        fun onTransactionDeclined()
    }

    interface OnTransactionApprovedListener : BaseListener {
        fun onTransactionApproved()
    }

    interface OnTransactionProcessingListener : BaseListener {
        fun onTransactionProcessing()
    }

    interface OnAwaitingOnlineResponseListener : BaseListener {
        fun onAwaitingOnlineResponse()
    }

    interface BaseListener
}