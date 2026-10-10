package io.github.sanamhub.nepdate

import com.code_intelligence.jazzer.api.FuzzedDataProvider
import com.code_intelligence.jazzer.junit.FuzzTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * K3-07 (ADR-0004). Without JAZZER_FUZZ this replays the inputs in
 * src/test/resources/io/github/sanamhub/nepdate/ParseFuzzTestInputs/fuzz/; with JAZZER_FUZZ=1 it
 * fuzzes (the manual fuzz.yml workflow). Same rules as ParseRandomTest.
 */
private inline fun parseOrRejected(parser: () -> NepaliDate): NepaliDate? =
    runCatching(parser).let { result ->
        val error = result.exceptionOrNull()
        if (error != null && error !is NepDateException) throw error
        result.getOrNull()
    }

class ParseFuzzTest {
    @FuzzTest(maxDuration = "10m")
    fun fuzz(data: FuzzedDataProvider) {
        val text = data.consumeRemainingAsString()
        // Only NepDateException is an expected failure; anything else propagates and fails the run.
        val strict = parseOrRejected { NepaliDate.parse(text) }
        parseOrRejected { NepaliDate.parseLenient(text) }
        val orNull = NepaliDate.parseOrNull(text)
        if (strict == null) {
            assertNull(orNull)
        } else {
            assertEquals(strict, orNull)
            assertEquals(strict, NepaliDate.parse(strict.toString()))
        }
    }
}
