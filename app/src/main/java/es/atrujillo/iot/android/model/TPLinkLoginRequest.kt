package es.atrujillo.iot.android.model

data class TPLinkLoginRequest(val method: String = "login", val params: TPLinkLoginParams)