package es.atrujillo.iot.android.model.tplink

data class TPLinkLoginRequest(val method: String = "login", val params: TPLinkLoginParams)