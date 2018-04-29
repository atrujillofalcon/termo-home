package es.atrujillo.iot.android.networking

import okhttp3.Callback

interface TPLinkService {

    fun getTPLinkToken(user: String, pass: String, callback: Callback)

    fun getDeviceList(user: String, pass: String, callback: Callback)

    fun setDeviceState(user: String, pass: String, deviceId: String, newState: TpLinkState)

    enum class TpLinkState(val value: Int) {
        ON(1), OFF(0)
    }

}