package es.atrujillo.termohome.common.config

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import es.atrujillo.termohome.common.extension.logInfo


class TermoHomeFirebaseApp : Application() {

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