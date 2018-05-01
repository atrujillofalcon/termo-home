package es.atrujillo.iot.android.networking

import okhttp3.Callback

interface TPLinkService {

    fun getTPLinkToken(user: String = TPLINK_USER, pass: String = TPLINK_PASS, callback: Callback)

    fun getDeviceList(user: String = TPLINK_USER, pass: String = TPLINK_PASS, callback: Callback)

    fun setDeviceState(user: String = TPLINK_USER, pass: String = TPLINK_PASS,

                       deviceId: String, newState: TpLinkState)

    enum class TpLinkState(val value: Int) {
        ON(1), OFF(0)
    }

    companion object {
        val TPLINK_USER = "atrujillo92work@gmail.com"
        val TPLINK_PASS = "forKasa#13"
        val AIR_DEVICE_ID = "80067F946249F000A801A5F6014BF033172B236D"
        val ALEXA_DEVICE_ID = "80065A7EC8127B71DD91BC8654D2FB431942F847"
    }

}