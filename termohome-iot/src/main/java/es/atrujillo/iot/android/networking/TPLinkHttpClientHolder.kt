package es.atrujillo.iot.android.networking

import es.atrujillo.termohome.common.extension.logInfo
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object TPLinkHttpClientHolder {

    val httpClient: OkHttpClient

    init {
        logInfo("Configuring OKHttp client")
        httpClient = OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .authenticator(Authenticator.NONE)
                .build()
    }
}