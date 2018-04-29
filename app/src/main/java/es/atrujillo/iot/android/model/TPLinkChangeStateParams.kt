package es.atrujillo.iot.android.model

import es.atrujillo.iot.android.networking.TPLinkService
import es.atrujillo.iot.android.util.AppUuidGenerator
import java.util.*

class TPLinkChangeStateParams {

    val deviceId: String

    val requestData: String


    constructor(deviceId: String, newState: TPLinkService.TpLinkState) {
        this.deviceId = deviceId
        requestData = "{\"system\":{\"set_relay_state\":{\"state\":${newState.value}}}}"
    }

}