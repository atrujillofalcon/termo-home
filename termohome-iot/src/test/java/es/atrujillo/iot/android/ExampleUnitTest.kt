package es.atrujillo.iot.android

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        val i = ChronoUnit.MINUTES.between(LocalDateTime.MAX, LocalDateTime.now())
        assertEquals(4, 2 + 2)
    }
}
