package es.atrujillo.termohome.common.model.firebase

data class LimitData(val max: Int = 0, val min: Int = 0) {

    fun getRange() = min..max

}