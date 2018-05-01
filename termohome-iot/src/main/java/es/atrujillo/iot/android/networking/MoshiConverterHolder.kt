package es.atrujillo.iot.android.networking

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi

object MoshiConverterHolder {

    private val baseMoshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    fun createMoshiConverter() = baseMoshi.newBuilder().build()
}