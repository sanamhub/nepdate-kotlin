package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.NepaliDate
import java.lang.reflect.InvocationTargetException

/**
 * Calls the static `NepaliDate.<name>(CharSequence)` the way Java does, so a test can pass null,
 * which Kotlin's type system won't allow. Rethrows what the method threw.
 */
fun parseFromJava(
    name: String,
    text: CharSequence?,
): Any? =
    try {
        NepaliDate::class.java.getMethod(name, CharSequence::class.java).invoke(null, text)
    } catch (e: InvocationTargetException) {
        throw e.targetException
    }
