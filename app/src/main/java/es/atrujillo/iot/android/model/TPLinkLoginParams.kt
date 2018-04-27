package es.atrujillo.iot.android.model

import es.atrujillo.iot.android.util.AppUuidGenerator
import java.util.*

data class TPLinkLoginParams(val appType: String = "AndroidThings",
                             val cloudUserName: String, val cloudPassword: String,
                             val terminalUUID: String = AppUuidGenerator.uuid.toString())