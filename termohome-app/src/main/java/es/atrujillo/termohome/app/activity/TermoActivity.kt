package es.atrujillo.termohome.app.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import es.atrujillo.termohome.app.R
import es.atrujillo.termohome.app.service.PowerStateChangeObserver
import es.atrujillo.termohome.common.extension.logWarn
import es.atrujillo.termohome.common.model.firebase.FirebaseKeys
import es.atrujillo.termohome.common.model.firebase.LimitData
import kotlinx.android.synthetic.main.activity_termo.*
import java.text.DecimalFormat
import java.util.*


class TermoActivity : AppCompatActivity(), ValueEventListener, View.OnClickListener {

    private var temperature: Float? = null
    private var limits: LimitData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_termo)

        startService(Intent(this, PowerStateChangeObserver::class.java))

        FirebaseDatabase.getInstance().getReference(FirebaseKeys.TEMPERATURE.key).addValueEventListener(this)
        FirebaseDatabase.getInstance().getReference(FirebaseKeys.POWER.key).addValueEventListener(this)
        FirebaseDatabase.getInstance().getReference(FirebaseKeys.ACTIVE.key).addValueEventListener(this)
        FirebaseDatabase.getInstance().getReference(FirebaseKeys.LIMITS.key).addValueEventListener(this)

        activeSwitch.setOnClickListener(this)
        stateSwitch.setOnClickListener(this)
        toChartsButton.setOnClickListener(this)
        setLimitsListeners()
    }

    override fun onCancelled(e: DatabaseError) {
        logWarn(e.message)
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        if (snapshot.key != null) {
            val key = FirebaseKeys.buildFromKey(snapshot.key!!)
            when (key) {
                FirebaseKeys.LIMITS -> {
                    limits = snapshot.getValue(LimitData::class.java)
                    if (limits != null) {
                        minEdit.setText(limits!!.min.toString())
                        maxEdit.setText(limits!!.max.toString())
                    }
                }
                FirebaseKeys.TEMPERATURE -> {
                    temperature = snapshot.getValue(Float::class.java)
                    if (temperature != null)
                        temperatureTV.text = "${DecimalFormat("##.##").format(temperature)} ºC"
                }
                FirebaseKeys.POWER -> stateSwitch.isChecked = snapshot.getValue(Boolean::class.java)!!
                FirebaseKeys.ACTIVE -> {
                    activeSwitch.isChecked = snapshot.getValue(Boolean::class.java)!!
                    activeSwitch.invalidate()

                    stateSwitch.isEnabled = !activeSwitch.isChecked
                    stateSwitch.invalidate()
                }
                else -> Log.w("TermoActivity", "Not found Firebase key")
            }

            if (loader.visibility != View.GONE) {
                loader.visibility = View.GONE
                viewsContainer.visibility = View.VISIBLE
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.activeSwitch -> FirebaseDatabase.getInstance().getReference("active").setValue((v as Switch).isChecked)
            R.id.stateSwitch -> FirebaseDatabase.getInstance().getReference("power_on").setValue((v as Switch).isChecked)
            R.id.toChartsButton -> startActivity(Intent(this, TermoChartActivity::class.java))
        }
    }

    private fun setLimitsListeners() {
        lateinit var inputTimer: Timer
        minEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                inputTimer = Timer()
                inputTimer.schedule(object : TimerTask() {
                    override fun run() {
                        if (s != null && s.isNotEmpty()) {
                            val minValue = s.toString().toInt()
                            if (minValue != limits?.min)
                                FirebaseDatabase.getInstance().getReference("limits/min").setValue(minValue)
                        }
                    }
                }, 500)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        maxEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                inputTimer = Timer()
                inputTimer.schedule(object : TimerTask() {
                    override fun run() {
                        if (text != null && text.isNotEmpty()) {
                            val maxValue = text.toString().toInt()
                            if (maxValue != limits?.max)
                                FirebaseDatabase.getInstance().getReference("limits/max").setValue(maxValue)
                        }
                    }
                }, 500)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

}
