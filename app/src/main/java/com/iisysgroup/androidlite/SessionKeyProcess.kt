package com.iisysgroup.androidlite

import com.itex.richard.payviceconnect.wrapper.NetworkInteractor
import io.reactivex.Observable

class SessionKeyProcess {
    fun getKey(url: String, body: String): Observable<SessionKey.Response> {
        val networkInteractor = NetworkInteractor.getInstance(SessionKey.Response())
        return networkInteractor.post(url, body) as Observable<SessionKey.Response>
    }
}