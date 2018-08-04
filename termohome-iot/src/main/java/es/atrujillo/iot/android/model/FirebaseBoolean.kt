package es.atrujillo.iot.android.model

sealed class FirebaseBoolean(val boolean: Boolean?) {

    class Uninitialized : FirebaseBoolean(null)
    class True : FirebaseBoolean(true)
    class False : FirebaseBoolean(false)

    companion object {

        fun buildFromBoolean(boolean: Boolean?): FirebaseBoolean {
            return when (boolean) {
                true -> True()
                false -> False()
                else -> Uninitialized()
            }
        }
    }

}