package com.iisysgroup.androidlite.models

import com.google.gson.annotations.Expose
import com.iisysgroup.poslib.host.entities.TransactionResult

data class PfmResponse(val status : Int, val message : String, val meta : List<String>, val newRRN : String, val date : String)
