package es.atrujillo.termohome.common.model.firebase

enum class FirebaseKeys(val key: String) {
    LIMITS("limits"), POWER("power_on"), IDLE_INTERVAL("idle_interval"), ACTIVE("active"), OTHER("other");

    companion object {
        const val FIREBASE_LIMITS_KEY = "limits"
        const val FIREBASE_POWER_KEY = "power_on"
        const val FIREBASE_IDLE_KEY = "idle_interval"
        const val FIREBASE_ACTIVE_KEY = "active"

        fun buildFromKey(key: String) =
                FirebaseKeys.values().filter { it.key == key }.getOrElse(0, { OTHER })
    }
}