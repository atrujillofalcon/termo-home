package es.atrujillo.termohome.app.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Switch
import es.atrujillo.termohome.app.R
import es.atrujillo.termohome.app.service.PowerStateChangeObserver
import java.text.DecimalFormat


class TermoActivity : AppCompatActivity(), ValueEventListener, View.OnClickListener {

    private var temperature: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_termo)

        startService(Intent(this, PowerStateChangeObserver::class.java))

        FirebaseDatabase.getInstance().getReference("temperature").addValueEventListener(this)
        FirebaseDatabase.getInstance().getReference("power_on").addValueEventListener(this)
        FirebaseDatabase.getInstance().getReference("active").addValueEventListener(this)

        activeSwitch.setOnClickListener(this)
        stateSwitch.setOnClickListener(this)
        toChartsButton.setOnClickListener(this)
    }

    override fun onCancelled(e: DatabaseError) {
        Log.e("TermoActivity", e.message)
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        when (snapshot.key) {
            "temperature" -> {
                temperature = snapshot.getValue(Float::class.java)
                if (temperature != null)
                    temperatureTV.text = "${DecimalFormat("##.##").format(temperature)} ºC"
            }
            "power_on" -> stateSwitch.isChecked = snapshot.getValue(Boolean::class.java)!!
            "active" -> {
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.activeSwitch -> FirebaseDatabase.getInstance().getReference("active").setValue((v as Switch).isChecked)
            R.id.stateSwitch -> FirebaseDatabase.getInstance().getReference("power_on").setValue((v as Switch).isChecked)
            R.id.toChartsButton -> startActivity(Intent(this, TermoChartActivity::class.java))
        }
    }
}
