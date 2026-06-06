package com.iisysgroup.androidlite.models

import com.iisysgroup.poslib.host.entities.TransactionResult


/**
 * Created by Agbede on 2/28/2018.
 */
data class TransactionPassingHolder (val position : Int, val map : HashMap<Int, TransactionResult>)