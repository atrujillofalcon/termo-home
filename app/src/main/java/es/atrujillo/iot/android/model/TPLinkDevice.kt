package es.atrujillo.iot.android.model

data class TPLinkDevice(val deviceName: String, val status: Int, val alias: String,
                        val appServerUrl: String, val deviceId: String)