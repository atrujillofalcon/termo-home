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
import es.atrujillo.iot.android.networking.TPLinkService
import es.atrujillo.iot.android.networking.TPLinkServiceClient
import es.atrujillo.iot.android.service.TemperaturePressureService
import es.atrujillo.termohome.common.extension.logInfo
import es.atrujillo.termohome.common.extension.logWarn
import es.atrujillo.termohome.common.model.firebase.FirebaseKeys
import es.atrujillo.termohome.common.model.firebase.FirebaseKeys.Companion.FIREBASE_ACTIVE_KEY
import es.atrujillo.termohome.common.model.firebase.FirebaseKeys.Companion.FIREBASE_IDLE_KEY
import es.atrujillo.termohome.common.model.firebase.FirebaseKeys.Companion.FIREBASE_LIMITS_KEY
import es.atrujillo.termohome.common.model.firebase.FirebaseKeys.Companion.FIREBASE_POWER_KEY
import es.atrujillo.termohome.common.model.firebase.LimitData
import es.atrujillo.termohome.common.model.firebase.TermoHistoricRawData
import kotlinx.android.synthetic.main.display_temperature.*
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


class TempoIotActivity : Activity(), ValueEventListener {

    private lateinit var mSensorManager: SensorManager
    private var lastReadSecond = 0
    private var lastChangeState = LocalDateTime.MIN

    private var limits: LimitData? = null
    private var powerOn: Boolean? = null
    private var isEngineActive: Boolean? = null
    private var idleInterval: Long = 30

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
        FirebaseDatabase.getInstance().getReference(FIREBASE_IDLE_KEY).addValueEventListener(this)
        FirebaseDatabase.getInstance().getReference(FIREBASE_ACTIVE_KEY).addValueEventListener(this)

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
        val engineActive = if (isEngineActive != null) isEngineActive!! else false
        if (engineActive && limits != null && powerOn != null) {
            //si está encendido y la temperatura está en los rangos normales activar trigger
            if (isIdleIntervalDone() && powerOn as Boolean && tempValue in limits!!.min..limits!!.max) {
                FirebaseDatabase.getInstance().getReference(FIREBASE_POWER_KEY).setValue(false)
            }
            //si está apagado y la temperatura está fuera de rango
            else if (isIdleIntervalDone() && !powerOn!! && (tempValue < limits!!.min || tempValue > limits!!.max)) {
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
            newHistoricEntry.setValue(TermoHistoricRawData(Instant.now().toEpochMilli(), tempValue))
        }
    }

    private fun isIdleIntervalDone(): Boolean {
        return ChronoUnit.MINUTES.between(lastChangeState, LocalDateTime.now()) > idleInterval
    }

    override fun onCancelled(e: DatabaseError) {
        logWarn(e.message)
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        val key = FirebaseKeys.buildFromKey(snapshot.key)
        when (key) {
            FirebaseKeys.LIMITS -> limits = snapshot.getValue(LimitData::class.java)
            FirebaseKeys.POWER -> {
                if (powerOn != null)
                    lastChangeState = LocalDateTime.now()  //ignoramos la primera vez

                powerOn = snapshot.getValue(Boolean::class.java)
                if (powerOn as Boolean) {
                    TPLinkServiceClient().setDeviceState(deviceId = TPLinkService.AIR_DEVICE_ID,
                            newState = TPLinkService.TpLinkState.ON)
                } else {
                    TPLinkServiceClient().setDeviceState(deviceId = TPLinkService.AIR_DEVICE_ID,
                            newState = TPLinkService.TpLinkState.OFF)
                }
            }
            FirebaseKeys.IDLE_INTERVAL -> idleInterval = snapshot.getValue(Long::class.java)!!
            FirebaseKeys.ACTIVE -> isEngineActive = snapshot.getValue(Boolean::class.java)!!
            FirebaseKeys.OTHER -> logWarn("Not found firebase key ${snapshot.key}")
        }
    }

}
