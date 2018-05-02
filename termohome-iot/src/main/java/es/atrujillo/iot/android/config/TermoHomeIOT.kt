package es.atrujillo.iot.android.config

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import es.atrujillo.iot.android.extension.logInfo


class TermoHomeIOT : Application() {

    override fun onCreate() {
        super.onCreate()
        val firebaseApp = FirebaseApp.initializeApp(this)

        if (firebaseApp != null) {
            logInfo("Authenticating anonymously")
            val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(firebaseApp)
            firebaseAuth.signInAnonymously()
        }
    }
}