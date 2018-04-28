package es.atrujillo.iot.android.networking

import es.atrujillo.iot.android.extension.logError
import es.atrujillo.iot.android.model.TPLinkLoginParams
import es.atrujillo.iot.android.model.TPLinkLoginRequest
import es.atrujillo.iot.android.model.TPLinkLoginResponse
import okhttp3.*

class TPLinkServiceClient : TPLinkService {

    override fun getTPLinkToken(user: String, pass: String, callback: Callback) {
        val tokenMoshi = MoshiConverterHolder.createMoshiConverter()
                .adapter(TPLinkLoginRequest::class.java)
        val request = TPLinkLoginRequest(params = TPLinkLoginParams(cloudUserName = user, cloudPassword = pass))


        val body = RequestBody.create(JSON, tokenMoshi.toJson(request))
        val okRequest = Request.Builder()
                .url(TOKEN_URL)
                .post(body)
                .build()

        val responseMoshi = MoshiConverterHolder.createMoshiConverter().adapter(TPLinkLoginResponse::class.java)
        return TPLinkHttpClientHolder.httpClient.newCall(okRequest).enqueue(callback)

    }

    companion object {
        const val TOKEN_URL = "https://wap.tplinkcloud.com"
        val JSON = MediaType.parse("application/json; charset=utf-8")
    }
}