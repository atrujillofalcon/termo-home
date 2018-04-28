package es.atrujillo.iot.android.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.DynamicSensorCallback
import android.content.Intent
import es.atrujillo.iot.android.R
import es.atrujillo.iot.android.extension.logError
import es.atrujillo.iot.android.extension.logInfo
import es.atrujillo.iot.android.model.TPLinkLoginResponse
import es.atrujillo.iot.android.networking.MoshiConverterHolder
import es.atrujillo.iot.android.networking.TPLinkServiceClient
import es.atrujillo.iot.android.service.TemperaturePressureService
import kotlinx.android.synthetic.main.display_temperature.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.text.DecimalFormat
import java.time.LocalDateTime

private val TAG = MainActivity::class.java.simpleName

class MainActivity : Activity() {

    private lateinit var mSensorManager: SensorManager
    private var lastReadSecond = 0

    private val mDynamicSensorCallback = object : DynamicSensorCallback() {
        override fun onDynamicSensorConnected(sensor: Sensor) {
            if (sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                Log.i(TAG, "Temperature sensor connected")
                mSensorEventListener = TemperaturePressureEventListener()
                mSensorManager.registerListener(mSensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    private lateinit var mSensorEventListener: TemperaturePressureEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.display_temperature)

        TPLinkServiceClient().getTPLinkToken("atrujillo92work@gmail.com",
                "forKasa#13", object : Callback {
            override fun onFailure(call: Call, e: IOException?) {
                logError("Error getting token", e)
            }

            override fun onResponse(call: Call, response: Response) {
                var responseBody = response.body()?.string()
                logInfo(responseBody)
                val token = MoshiConverterHolder.createMoshiConverter()
                        .adapter(TPLinkLoginResponse::class.java)
                        .fromJson(responseBody)
                        ?.result?.token

                logInfo("TOKEN: $token")
            }
        })

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL)
        Log.i("SENSORS", "Sensor count ${sensors.size}")
        for (s in sensors) {
            Log.i("SENSORS", "Sensor name: ${s.name}")
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
            val currentSecond = LocalDateTime.now().second
            if (currentSecond != lastReadSecond && currentSecond % 5 == 0) {
                temperatureText.text = "${DecimalFormat("##.##").format(event.values[0])} ºC"
                Log.i(TAG, "sensor changed: " + event.values[0])
                lastReadSecond = currentSecond
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            Log.i(TAG, "sensor accuracy changed: " + accuracy)
        }
    }

}
