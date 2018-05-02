package es.atrujillo.termohome.app.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Switch
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import es.atrujillo.termohome.app.R
import es.atrujillo.termohome.app.service.PowerStateChangeObserver
import kotlinx.android.synthetic.main.activity_termo.*
import java.text.DecimalFormat


class TermoActivity : AppCompatActivity(), ValueEventListener {

    private var powerState: Boolean? = null

    private var temperature: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_termo)

        startService(Intent(this, PowerStateChangeObserver::class.java))

        FirebaseDatabase.getInstance().getReference("temperature").addValueEventListener(this)
        FirebaseDatabase.getInstance().getReference("power_on").addValueEventListener(this)
        FirebaseDatabase.getInstance().getReference("active").addValueEventListener(this)

        activeSwitch.setOnClickListener({
            if (it is Switch) {
                FirebaseDatabase.getInstance().getReference("active").setValue(it.isChecked)
            }
        })
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
            }
            else -> Log.w("TermoActivity", "Not found Firebase key")
        }
    }
}
