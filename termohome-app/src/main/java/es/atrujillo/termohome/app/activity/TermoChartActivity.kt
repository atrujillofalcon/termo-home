package es.atrujillo.termohome.app.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import es.atrujillo.termohome.app.R
import es.atrujillo.termohome.app.math.HistoricToEntryProcessor
import es.atrujillo.termohome.common.model.firebase.TermoHistoricCalendarData
import java.util.*
import java.util.stream.Collectors

class TermoChartActivity : AppCompatActivity(), ValueEventListener {

    private lateinit var historicRaw: List<TermoHistoricRawData?>
    private lateinit var historicCaledar: List<TermoHistoricCalendarData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        FirebaseDatabase.getInstance()
                .getReference("historic/data")
//                .limitToLast(100)
                .addValueEventListener(this)

        configureTempChart()
    }

    override fun onCancelled(e: DatabaseError) {}

    override fun onDataChange(snapshot: DataSnapshot) {
        historicRaw = snapshot.children.toList().stream()
                .map { it.getValue(TermoHistoricRawData::class.java) }
                .filter(Objects::nonNull)
                .collect(Collectors.toList())

        historicCaledar = historicRaw
                .filter(Objects::nonNull)
                .map { TermoHistoricCalendarData(getCalendarFromEpoch(it!!.ms), it.temperature) }

        val dataset = LineDataSet(HistoricToEntryProcessor.getMonthlyEntries(historicCaledar, Calendar.getInstance()),
                "Temperatura Promedio Diaria")
        tempChart.data = LineData(dataset)
        tempChart.invalidate()
    }

    private fun getCalendarFromEpoch(epochMs: Long): Calendar {
        val dateTime = Calendar.getInstance()
        dateTime.timeInMillis = epochMs
        return dateTime
    }

    private fun configureTempChart() {
        tempChart.xAxis.granularity = 1F
        tempChart.setPinchZoom(true)
        tempChart.isAutoScaleMinMaxEnabled = true
    }

}
