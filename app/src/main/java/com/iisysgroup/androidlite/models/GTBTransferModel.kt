package com.iisysgroup.androidlite.models

import com.google.gson.annotations.SerializedName

data class GTBTransferModel(@SerializedName("UploadFileNew_XMLResult") val result : Result)

data class Result(@SerializedName("Response") val response : GTResponse)

data class GTResponse(@SerializedName("ResCode")val code: String, @SerializedName("Message") val message: String)