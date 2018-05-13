package es.atrujillo.termohome.app.math

import com.github.mikephil.charting.data.Entry
import es.atrujillo.termohome.common.model.firebase.TermoHistoricCalendarData
import java.util.*
import java.util.stream.Collectors

class HistoricToEntryProcessor {

    companion object {
        suspend fun getMonthlyEntries(historic: List<TermoHistoricCalendarData>, selectedMonth: Calendar): List<Entry> {
            val previusMonth = GregorianCalendar(selectedMonth.get(Calendar.YEAR), selectedMonth.get(Calendar.MONTH), 1)
            val nextMonth = GregorianCalendar(selectedMonth.get(Calendar.YEAR), selectedMonth.get(Calendar.MONTH) + 1, 1)

            val filteredHistoric = historic.stream()
                    .filter { it.date >= previusMonth && it.date < nextMonth }
                    .collect(Collectors.toList())

            return proccessMapAndCreateEntries(filteredHistoric, Calendar.DAY_OF_MONTH)
        }

        suspend fun getDailyEntries(historic: List<TermoHistoricCalendarData>, selectedDay: Calendar): List<Entry> {
            val filteredHistoric = historic.stream()
                    .filter { it.date.get(Calendar.DAY_OF_YEAR) == selectedDay.get(Calendar.DAY_OF_YEAR) }
                    .filter { it.date.get(Calendar.YEAR) == selectedDay.get(Calendar.YEAR) }
                    .collect(Collectors.toList())

            return proccessMapAndCreateEntries(filteredHistoric, Calendar.HOUR_OF_DAY)
        }

        suspend private fun proccessMapAndCreateEntries(filteredHistoric: List<TermoHistoricCalendarData>,
                                                        calendarKey: Int): List<Entry> {

            val mapHistoric = mutableMapOf<Int, MutableList<Float>>()
            for (i in filteredHistoric) {
                val value = i.date.get(calendarKey)
                if (mapHistoric[value] == null)
                    mapHistoric[value] = mutableListOf()

                mapHistoric[value]!!.add(i.temperature)
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