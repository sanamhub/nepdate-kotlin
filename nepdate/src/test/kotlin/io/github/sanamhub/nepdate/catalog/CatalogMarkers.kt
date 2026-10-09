package io.github.sanamhub.nepdate.catalog

// C# catalogue rows of phase 1 that need an API from a later task (PORT-PLAN, "C# test catalogue").
// The later task removes the marker and writes the body.

/** Strict parsing arrives in K3-04. */
const val NEEDS_PARSE = "K3-04: needs NepaliDate.parse"

/** Today in Nepal time arrives with the java.time interop in K4-07. */
const val NEEDS_TODAY = "K4-07: needs nepaliToday"
