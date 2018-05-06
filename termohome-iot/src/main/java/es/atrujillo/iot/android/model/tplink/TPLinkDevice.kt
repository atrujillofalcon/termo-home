package es.atrujillo.iot.android.model.tplink

data class TPLinkDevice(val deviceName: String, val status: Int, val alias: String,
                        val appServerUrl: String, val deviceId: String)