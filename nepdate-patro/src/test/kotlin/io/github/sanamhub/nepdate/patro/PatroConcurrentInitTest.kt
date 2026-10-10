package io.github.sanamhub.nepdate.patro

import io.github.sanamhub.nepdate.NepaliDate
import java.net.URLClassLoader
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * First use of the patro tables from many threads at once (K4-06). Each trial loads both jars (their
 * class directories) into a fresh class loader, so PatroTables initialises again, and 16 threads
 * released together call Patro.info and read the PATRO section 4 anchors.
 */
class PatroConcurrentInitTest {
    private val urls =
        listOf(Patro::class.java, NepaliDate::class.java, KotlinVersion::class.java)
            .map { it.protectionDomain.codeSource.location }
            .toTypedArray()

    private val expected = ANCHORS.map { "${it.tithi}|${it.holiday}|${it.events}" }

    @Test
    fun `16 threads see the anchors on first use, 50 of 50 trials`() {
        val pool = Executors.newFixedThreadPool(THREADS)
        try {
            repeat(TRIALS) { trial ->
                URLClassLoader(urls, ClassLoader.getPlatformClassLoader()).use { loader ->
                    val patro = loader.loadClass(Patro::class.java.name)
                    check(patro !== Patro::class.java) { "trial $trial reused the test's Patro class" }
                    val dateType = loader.loadClass(NepaliDate::class.java.name)
                    val of =
                        dateType.getMethod(
                            "of",
                            Int::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType,
                        )
                    val info = patro.getMethod("info", dateType)
                    val infoType = loader.loadClass(CalendarInfo::class.java.name)
                    val namedType = loader.loadClass(Named::class.java.name)
                    val tithi = infoType.getMethod("getTithi")
                    val holiday = infoType.getMethod("isPublicHoliday")
                    val events = infoType.getMethod("getEvents")
                    val english = namedType.getMethod("getEnglish")
                    val start = CountDownLatch(1)
                    val results =
                        (1..THREADS).map {
                            pool.submit<List<String>> {
                                start.await()
                                ANCHORS.map { anchor ->
                                    val date = of.invoke(null, anchor.date.year, anchor.date.monthValue, anchor.date.dayOfMonth)
                                    val result = info.invoke(null, date)
                                    val t = tithi.invoke(result)?.let { english.invoke(it) }
                                    val e = (events.invoke(result) as List<*>).map { english.invoke(it) }
                                    "$t|${holiday.invoke(result)}|$e"
                                }
                            }
                        }
                    start.countDown()
                    for (result in results) assertEquals(expected, result.get(30, TimeUnit.SECONDS), "trial $trial")
                }
            }
        } finally {
            pool.shutdownNow()
        }
    }

    private companion object {
        const val THREADS = 16
        const val TRIALS = 50
    }
}
