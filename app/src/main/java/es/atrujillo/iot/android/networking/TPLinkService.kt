package es.atrujillo.iot.android.networking

interface TPLinkService {

    fun getTPLinkToken(user: String, pass: String): String?

}