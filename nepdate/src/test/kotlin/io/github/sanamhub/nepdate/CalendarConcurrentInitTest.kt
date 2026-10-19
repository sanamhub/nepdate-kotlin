package io.github.sanamhub.nepdate

import java.net.URLClassLoader
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * First use of the tables from many threads at once. Each trial loads the core into a fresh
 * class loader, so CalendarTables and NepaliDate's companion initialise again, and 16 threads
 * released together race on that first use.
 */
class CalendarConcurrentInitTest {
    private val urls =
        listOf(NepaliDate::class.java, KotlinVersion::class.java)
            .map { it.protectionDomain.codeSource.location }
            .toTypedArray()

    @Test
    fun `16 threads see the same tables on first use, 50 of 50 trials`() {
        val pool = Executors.newFixedThreadPool(THREADS)
        try {
            repeat(TRIALS) { trial ->
                URLClassLoader(urls, ClassLoader.getPlatformClassLoader()).use { loader ->
                    val type = loader.loadClass(NepaliDate::class.java.name)
                    check(type !== NepaliDate::class.java) { "trial $trial reused the test's NepaliDate class" }
                    val ofGregorian =
                        type.getMethod(
                            "ofGregorian",
                            Int::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType,
                        )
                    val start = CountDownLatch(1)
                    val results =
                        (1..THREADS).map {
                            pool.submit<String> {
                                start.await()
                                ofGregorian.invoke(null, 2024, 7, 30).toString()
                            }
                        }
                    start.countDown()
                    for (result in results) assertEquals("2081/04/15", result.get(10, TimeUnit.SECONDS), "trial $trial")
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
