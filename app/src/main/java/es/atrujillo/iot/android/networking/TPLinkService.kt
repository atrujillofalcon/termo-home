package es.atrujillo.iot.android.networking

import okhttp3.Callback

interface TPLinkService {

    fun getTPLinkToken(user: String, pass: String, callback: Callback)

}