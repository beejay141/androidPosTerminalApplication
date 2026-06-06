package com.iisysgroup.androidlite.models

data class BalanceModel(var status : Int, var error : Boolean, var message : String, var date : String, var errors : String,
                        var balance : Float, var commissionBalance : Float, var  walletID : String, var name : String, var email : String)