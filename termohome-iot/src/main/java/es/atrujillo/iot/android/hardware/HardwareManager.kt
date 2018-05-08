package es.atrujillo.iot.android.hardware

import com.google.android.things.update.UpdateManager
import com.google.android.things.update.UpdatePolicy
import java.util.concurrent.TimeUnit

class HardwareManager {

    companion object {
        fun setUpdateUpdateManagerPolicy(policy: Int) = UpdateManager.getInstance()
                .setPolicy(UpdatePolicy.Builder()
                        .setPolicy(UpdatePolicy.POLICY_APPLY_AND_REBOOT)
                        .setApplyDeadline(10, TimeUnit.MINUTES)
                        .build()
                )
    }
}