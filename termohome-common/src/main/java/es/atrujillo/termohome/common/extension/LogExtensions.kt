package es.atrujillo.termohome.common.extension

import android.util.Log

fun Any.logInfo(msg: String?) {
    if (msg != null)
        Log.i(this::class.simpleName, msg)
}

fun Any.logWarn(msg: String) = Log.w(this::class.simpleName, msg)

fun Any.logError(msg: String) = Log.e(this::class.simpleName, msg)

fun Any.logError(msg: String, e: Throwable?) = Log.e(this::class.simpleName, msg, e)