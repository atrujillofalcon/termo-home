package es.atrujillo.termohome.app.math

import com.github.mikephil.charting.data.Entry
import es.atrujillo.termohome.app.model.firebase.TermoHistoricCalendarData
import java.util.*
import java.util.stream.Collectors

class HistoricToEntryProcessor {

    companion object {
        fun getMonthlyEntries(historic: List<TermoHistoricCalendarData>, selectedMonth: Calendar): List<Entry> {
            val previusMonth = GregorianCalendar(selectedMonth.get(Calendar.YEAR), selectedMonth.get(Calendar.MONTH), 1)
            val nextMonth = GregorianCalendar(selectedMonth.get(Calendar.YEAR), selectedMonth.get(Calendar.MONTH) + 1, 1)

            val filteredHistoric = historic.stream()
                    .filter { it.date >= previusMonth && it.date < nextMonth }
                    .collect(Collectors.toList())

            val mapHistoric = mutableMapOf<Int, MutableList<Float>>()
            for (i in filteredHistoric) {
                if (mapHistoric[i.date.get(Calendar.DAY_OF_MONTH)] == null)
                    mapHistoric[i.date.get(Calendar.DAY_OF_MONTH)] = mutableListOf()

                mapHistoric[i.date.get(Calendar.DAY_OF_MONTH)]!!.add(i.temperature)
            }

            return mapHistoric.keys.stream()
                    .map { Entry(it.toFloat(), calculateAverageTemperature(mapHistoric[it]!!).toFloat()) }
                    .collect(Collectors.toList())
        }


        private fun calculateAverageTemperature(temps: MutableList<Float>): Double {
            return temps.stream()
                    .mapToDouble(Float::toDouble)
                    .average()
                    .orElse(0.0)
        }
    }

}