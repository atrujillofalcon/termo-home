package es.atrujillo.iot.android.activity

import android.app.Activity
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.DynamicSensorCallback
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import es.atrujillo.iot.android.R
import es.atrujillo.iot.android.activity.TempoIotActivity.FirebaseKeys.Companion.FIREBASE_LIMITS_KEY
import es.atrujillo.iot.android.activity.TempoIotActivity.FirebaseKeys.Companion.FIREBASE_POWER_KEY
import es.atrujillo.iot.android.extension.logInfo
import es.atrujillo.iot.android.extension.logWarn
import es.atrujillo.iot.android.model.firebase.LimitData
import es.atrujillo.iot.android.model.firebase.TermoHistoricData
import es.atrujillo.iot.android.networking.TPLinkService
import es.atrujillo.iot.android.networking.TPLinkServiceClient
import es.atrujillo.iot.android.service.TemperaturePressureService
import kotlinx.android.synthetic.main.display_temperature.*
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDateTime


class TempoIotActivity : Activity(), ValueEventListener {

    private lateinit var mSensorManager: SensorManager
    private var lastReadSecond = 0

    private var limits: LimitData? = null
    private var powerOn: Boolean? = null

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

        FirebaseDatabase.getInstance().getReference(FIREBASE_LIMITS_KEY).addValueEventListener(this)
        FirebaseDatabase.getInstance().getReference(FIREBASE_POWER_KEY).addValueEventListener(this)

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
                temperatureTV.text = "${DecimalFormat("##.##").format(event.values[0])} ºC"

                logInfo("sensor changed: ${event.values[0]}")
                lastReadSecond = currentSecond

                processTemperatureData(event.values[0])
                saveDataToFirebase(now, event.values[0])
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            logInfo("sensor accuracy changed: $accuracy")
        }
    }

    private fun processTemperatureData(tempValue: Float) {
        if (limits != null && powerOn != null) {
            //si está encendido y la temperatura está en los rangos normales
            if (powerOn as Boolean && tempValue in limits!!.min..limits!!.max) {
                TPLinkServiceClient().setDeviceState(deviceId = TPLinkService.AIR_DEVICE_ID,
                        newState = TPLinkService.TpLinkState.OFF)
                FirebaseDatabase.getInstance().getReference(FIREBASE_POWER_KEY).setValue(false)
            }
            //si está apagado y la temperatura está fuera de rango
            else if (!powerOn!! && (tempValue < limits!!.min || tempValue > limits!!.max)) {
                TPLinkServiceClient().setDeviceState(deviceId = TPLinkService.AIR_DEVICE_ID,
                        newState = TPLinkService.TpLinkState.ON)
                FirebaseDatabase.getInstance().getReference(FIREBASE_POWER_KEY).setValue(true)
            }
        }
    }

    private fun saveDataToFirebase(now: LocalDateTime, tempValue: Float) {
        val database = FirebaseDatabase.getInstance()
        val tempRef = database.getReference("temperature")
        tempRef.setValue(tempValue)

        //grabamos el histórico cada 1h
        if (now.minute == 0) {
            val newHistoricEntry = database.getReference("historic").child("data").push()
            newHistoricEntry.setValue(TermoHistoricData(Instant.now().toEpochMilli(), tempValue))
        }
    }

    override fun onCancelled(e: DatabaseError) {
        logWarn(e.message)
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        val key = FirebaseKeys.buildFromKey(snapshot.key)
        when (key) {
            FirebaseKeys.LIMITS -> limits = snapshot.getValue(LimitData::class.java)
            FirebaseKeys.POWER -> powerOn = snapshot.getValue(Boolean::class.java)
            FirebaseKeys.OTHER -> logWarn("Not found firebase key ${snapshot.key}")
        }
    }

    private enum class FirebaseKeys(val key: String) {
        LIMITS("limits"), POWER("power_on"), OTHER("other");

        companion object {
            const val FIREBASE_LIMITS_KEY = "limits"
            const val FIREBASE_POWER_KEY = "power_on"
            fun buildFromKey(key: String) =
                    FirebaseKeys.values().filter { it.key == key }.getOrElse(0, { OTHER })
        }
    }
}
