package com.iisysgroup.androidlite

import com.google.gson.annotations.Expose

class SessionKey {
    data class Request(@Expose val wallet : String, @Expose val terminal : String?, @Expose val username : String, @Expose val password : String,
                       @Expose val channel : String, @Expose val deviceID : String)


    data  class  Response(@Expose val status : Int, @Expose val message : String, @Expose val error : Boolean, @Expose val description : String?,
                          @Expose val sessionKey : String, @Expose val reference : Int, @Expose val date : String){
        constructor():this(0, "", true, "","",0,"")
    }
}