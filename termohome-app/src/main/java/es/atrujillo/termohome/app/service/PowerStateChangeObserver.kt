package es.atrujillo.termohome.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import es.atrujillo.termohome.app.R

class PowerStateChangeObserver : Service(), ValueEventListener {

    private var firstTime = true

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        FirebaseDatabase.getInstance().getReference(FIREBASE_POWER_KEY).addValueEventListener(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseDatabase.getInstance().getReference(FIREBASE_POWER_KEY).removeEventListener(this)
    }

    override fun onCancelled(e: DatabaseError) {
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        if (snapshot.key == FIREBASE_POWER_KEY) {
            showTermoHomeChangeStateNotification(snapshot.getValue(Boolean::class.java))
        }
    }


    fun showTermoHomeChangeStateNotification(powerOn: Boolean?) {
        if (powerOn != null && !firstTime) {
            val alertContent = if (powerOn) resources.getString(R.string.alert_content_on) else resources.getString(R.string.alert_content_off)
            val notificationId = if (powerOn) TERMO_HOME_ALERT_ON else TERMO_HOME_ALERT_OFF

            val termoHomeNotificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(resources.getString(R.string.notification_title))
                    .setContentText(alertContent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)

            NotificationManagerCompat.from(this).notify(notificationId, termoHomeNotificationBuilder.build())
        } else {
            firstTime = false
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = description

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val FIREBASE_POWER_KEY = "power_on"
        const val CHANNEL_ID = "IOT_CHANNEL_NOTIFICATION"
        const val TERMO_HOME_ALERT_ON = 11
        const val TERMO_HOME_ALERT_OFF = 10
    }
}