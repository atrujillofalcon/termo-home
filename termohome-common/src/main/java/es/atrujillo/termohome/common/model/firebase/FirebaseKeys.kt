package es.atrujillo.termohome.common.model.firebase

enum class FirebaseKeys(val key: String) {
    LIMITS("limits"), POWER("power_on"), IDLE_INTERVAL("idle_interval"),
    ACTIVE("active"), TEMPERATURE("temperature"), TARGET("target"), OTHER("other");

    companion object {

        fun buildFromKey(key: String) = FirebaseKeys.values()
                .filter { it.key == key }
                .getOrElse(0) { OTHER }
    }
}