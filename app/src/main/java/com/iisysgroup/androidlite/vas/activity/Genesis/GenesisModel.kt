package com.iisysgroup.androidlite.vas.activity.Genesis

import com.google.gson.annotations.Expose

class GenesisModel {
    data class payDetail(@Expose val amount: Double, @Expose val clientreference:String,@Expose val terminal_id: String,
                         @Expose val user_id: String, @Expose val movie_id: String, @Expose val pin: String)

    data class moviesSuccesfulResponse(@Expose val error: Boolean, @Expose val title:String,@Expose val start_time: String,
                         @Expose val ref: String, @Expose val start_date:String, @Expose val screen: String,
                                       @Expose val ticket_id:String)
}