package es.atrujillo.iot.android.networking

import es.atrujillo.iot.android.extension.logError
import es.atrujillo.iot.android.extension.logInfo
import es.atrujillo.iot.android.model.TPLinkDevicesRequest
import es.atrujillo.iot.android.model.TPLinkLoginParams
import es.atrujillo.iot.android.model.TPLinkLoginRequest
import es.atrujillo.iot.android.model.TPLinkLoginResponse
import okhttp3.*
import java.io.IOException

class TPLinkServiceClient : TPLinkService {

    override fun getTPLinkToken(user: String, pass: String, callback: Callback) {
        val tokenMoshi = MoshiConverterHolder.createMoshiConverter()
                .adapter(TPLinkLoginRequest::class.java)
        val request = TPLinkLoginRequest(params = TPLinkLoginParams(cloudUserName = user, cloudPassword = pass))


        val body = RequestBody.create(JSON, tokenMoshi.toJson(request))
        val okRequest = Request.Builder()
                .url(BASE_TPLINK_URL)
                .post(body)
                .build()

        val responseMoshi = MoshiConverterHolder.createMoshiConverter().adapter(TPLinkLoginResponse::class.java)
        return TPLinkHttpClientHolder.httpClient.newCall(okRequest).enqueue(callback)
    }

    override fun getDeviceList(user: String, pass: String, callback: Callback) {
        getTPLinkToken(user, pass, object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                logError("Error getting token", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val token = MoshiConverterHolder.createMoshiConverter()
                                .adapter(TPLinkLoginResponse::class.java)
                                .fromJson(responseBody.string())
                                ?.result?.token

                        logInfo("TOKEN: $token")

                        val devicesToken = MoshiConverterHolder.createMoshiConverter()
                                .adapter(TPLinkDevicesRequest::class.java)
                        val body = RequestBody.create(JSON, devicesToken.toJson(TPLinkDevicesRequest()))
                        val okRequest = Request.Builder()
                                .url(BASE_TPLINK_URL + "?token=$token")
                                .post(body)
                                .build()

                        val responseMoshi = MoshiConverterHolder.createMoshiConverter()
                                .adapter(TPLinkLoginResponse::class.java)

                        return TPLinkHttpClientHolder.httpClient.newCall(okRequest)
                                .enqueue(callback)
                    }
                }

                throw Exception("Error in obtained response getting token")
            }
        })
    }


    companion object {
        const val BASE_TPLINK_URL = "https://wap.tplinkcloud.com"
        val JSON = MediaType.parse("application/json; charset=utf-8")
    }
}