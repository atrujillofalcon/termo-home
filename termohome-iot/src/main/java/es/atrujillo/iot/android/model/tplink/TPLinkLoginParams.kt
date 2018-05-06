package es.atrujillo.iot.android.model.tplink

import es.atrujillo.iot.android.util.AppUuidGenerator

data class TPLinkLoginParams(val appType: String = "AndroidThings",
                             val cloudUserName: String, val cloudPassword: String,
                             val terminalUUID: String = AppUuidGenerator.uuid.toString())