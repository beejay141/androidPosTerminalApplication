package com.iisysgroup.androidlite.utils

import com.iisysgroup.poslib.host.Host

/**
 * Created by Bamitale@Itex on 16/03/2018.
 */

    val Host.TransactionType.stringValue: String
    get() {
        return this.name.replace("_"," ")
    }
