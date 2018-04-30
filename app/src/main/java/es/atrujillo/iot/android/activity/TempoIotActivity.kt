package es.atrujillo.iot.android.activity

import android.app.Activity
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.DynamicSensorCallback
import android.os.Bundle
import com.google.firebase.database.FirebaseDatabase
import es.atrujillo.iot.android.R
import es.atrujillo.iot.android.extension.logInfo
import es.atrujillo.iot.android.model.TermoHistoricData
import es.atrujillo.iot.android.networking.TPLinkService
import es.atrujillo.iot.android.networking.TPLinkServiceClient
import es.atrujillo.iot.android.service.TemperaturePressureService
import kotlinx.android.synthetic.main.display_temperature.*
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDateTime


class TempoIotActivity : Activity() {

    private lateinit var mSensorManager: SensorManager
    private var lastReadSecond = 0

    private val mDynamicSensorCallback = object : DynamicSensorCallback() {
        override fun onDynamicSensorConnected(sensor: Sensor) {
            if (sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                logInfo("Temperature sensor connected")
                mSensorEventListener = TemperaturePressureEventListener()
                mSensorManager.registerListener(mSensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    private lateinit var mSensorEventListener: TemperaturePressureEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.display_temperature)

        /*TPLinkServiceClient().getDeviceList("atrujillo92work@gmail.com", "forKasa#13",
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        logError("Error getting devices", e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful && response.body() != null) {
                            val responseBody = response.body()
                            if (responseBody != null) {
                                val devices = MoshiConverterHolder.createMoshiConverter()
                                        .adapter(TPLinkDevicesResponse::class.java)
                                        .fromJson(responseBody.string())
                                        ?.result?.deviceList

                                logInfo("Devices: $devices")
                            }
                        }
                    }
                })*/

        TPLinkServiceClient().setDeviceState("atrujillo92work@gmail.com", "forKasa#13",
                "80067F946249F000A801A5F6014BF033172B236D", TPLinkService.TpLinkState.OFF)

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL)
        logInfo("Sensor count ${sensors.size}")
        for (s in sensors) {
            logInfo("Sensor name: ${s.name}")
        }
        startTemperaturePressureRequest()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTemperaturePressureRequest()
    }

    private fun startTemperaturePressureRequest() {
        this.startService(Intent(this, TemperaturePressureService::class.java))
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mSensorManager.registerDynamicSensorCallback(mDynamicSensorCallback)
    }

    private fun stopTemperaturePressureRequest() {
        this.stopService(Intent(this, TemperaturePressureService::class.java))
        mSensorManager.unregisterDynamicSensorCallback(mDynamicSensorCallback)
        mSensorManager.unregisterListener(mSensorEventListener)
    }

    private inner class TemperaturePressureEventListener : SensorEventListener {

        override fun onSensorChanged(event: SensorEvent) {
            val now = LocalDateTime.now()
            val currentSecond = now.second
            if (currentSecond != lastReadSecond && currentSecond % 5 == 0) {
                temperatureText.text = "${DecimalFormat("##.##").format(event.values[0])} ºC"
                logInfo("sensor changed: ${event.values[0]}")
                lastReadSecond = currentSecond

                val database = FirebaseDatabase.getInstance()
                val tempRef = database.getReference("temperature")
                tempRef.setValue(event.values[0])

                //grabamos el histórico cada 1h
                if (now.minute == 0) {
                    val newHistoricEntry = database.getReference("historic").child("data").push()
                    newHistoricEntry.setValue(TermoHistoricData(Instant.now().toEpochMilli(), event.values[0]))
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            logInfo("sensor accuracy changed: $accuracy")
        }
    }

}
