package es.atrujillo.iot.android.model.tplink

import es.atrujillo.iot.android.networking.TPLinkService

class TPLinkChangeStateParams {

    val deviceId: String

    val requestData: String


    constructor(deviceId: String, newState: TPLinkService.TpLinkState) {
        this.deviceId = deviceId
        requestData = "{\"system\":{\"set_relay_state\":{\"state\":${newState.value}}}}"
    }

}