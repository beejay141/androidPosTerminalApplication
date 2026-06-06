package com.iisysgroup.androidlite.models

import com.google.gson.annotations.Expose
import com.iisysgroup.androidlite.generators.PfmStateGenerator
import com.iisysgroup.poslib.host.entities.TransactionResult

data class PfmDetails(val state : PfmState, val journal : PfmJournal, val getRRN : String)


data class PfmState(val serial : String, val ctime : String, val bl : String, val cs : String, val ps : String, val tid: String, val coms : PfmStateGenerator.COMMS_METHOD, val cloc : String, val ss: String, val tmn : String, val tmanu : String, val hb : String = "true", val sv : String, val lTxnAt  : String, val pads : String, val sim : String, val customField : String)

data class PfmJournal(val mid: String, val stan: String, val mPan: String, val rrn: String, val acode: String, val amount: String, val timeStamp: String, val mti: String, val ps: String, val resp: String, val tap: Boolean, val rep: Boolean, val vm: String, val vasProduct: String, val vasCategory: String,val ostan: String, val orrn: String, val oacode: String, val mcc: String, val transMethod: String, val customField : String )