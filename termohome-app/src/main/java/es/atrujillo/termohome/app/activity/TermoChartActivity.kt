package es.atrujillo.termohome.app.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import es.atrujillo.termohome.app.R
import es.atrujillo.termohome.app.math.HistoricToEntryProcessor
import es.atrujillo.termohome.common.model.firebase.TermoHistoricCalendarData
import es.atrujillo.termohome.common.model.firebase.TermoHistoricRawData
import kotlinx.android.synthetic.main.activity_chart.*
import kotlinx.android.synthetic.main.content_termo_chart.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.stream.Collectors

class TermoChartActivity : AppCompatActivity(), ValueEventListener, View.OnClickListener {

    private lateinit var historicRaw: List<TermoHistoricRawData?>
    private lateinit var historicCaledar: List<TermoHistoricCalendarData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        FirebaseDatabase.getInstance()
                .getReference("historic/data")
                .addValueEventListener(this)

        configureTempChart()
        daily.setOnClickListener(this)
        monthly.setOnClickListener(this)
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

        populateChart(historicCaledar, getString(R.string.daily_chart_label),
                { hisCal: List<TermoHistoricCalendarData>, cal: Calendar ->
                    HistoricToEntryProcessor.getMonthlyEntries(hisCal, cal)
                })

    }

    private fun populateChart(data: List<TermoHistoricCalendarData>, label: String,
                              getDailyEntries: suspend (data: List<TermoHistoricCalendarData>,
                                                        filterCal: Calendar) -> List<Entry>) {
        GlobalScope.launch(Dispatchers.Main) {
            val dataset = LineDataSet(getDailyEntries(data, Calendar.getInstance()), label)

            tempChart.data = LineData(dataset)
            tempChart.invalidate()

            if (loader.visibility != View.GONE) {
                loader.visibility = View.GONE
                tempChart.visibility = View.VISIBLE
            }
        }
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

    override fun onClick(v: View) {
        tempChart.visibility = View.GONE
        loader.visibility = View.VISIBLE

        when (v.id) {
            monthly.id -> populateChart(historicCaledar, getString(R.string.daily_chart_label),
                    { hisCal: List<TermoHistoricCalendarData>, cal: Calendar ->
                        HistoricToEntryProcessor.getMonthlyEntries(hisCal, cal)
                    })
            daily.id -> populateChart(historicCaledar, getString(R.string.daily_chart_label),
                    { hisCal: List<TermoHistoricCalendarData>, cal: Calendar ->
                        HistoricToEntryProcessor.getDailyEntries(hisCal, cal)
                    })
        }
    }
}
