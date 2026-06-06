package com.iisysgroup.androidlite.vas.internet.smile

import java.io.Serializable
import java.util.HashMap

data class SmilePrintModel(var date: String?, var transactionType: String?, var transactionStatus: String?,
                           var map: HashMap<String, String>, var amount: String?) : Serializable