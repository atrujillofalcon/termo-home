package es.atrujillo.iot.android.model

import com.squareup.moshi.JsonQualifier

data class TPLinkDevicesResponse(val error_code: Int, val result: TPLinkDeviceResponseResult)