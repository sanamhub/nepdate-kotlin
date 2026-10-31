# ADR-0008: Read the patro counts from the hub's summary, not from literals

- **Status:** accepted
- **Date:** 2026-10-31
- **Approved by:** Sanam
- **Amends:** ADR-0001 section 6 (the generator also reads `shared/spec/vectors/patro-summary.json`)

## Context

The patro tables have counts that follow the data: coverage days, tithi days and names, holidays,
event days and events, distinct names and their UTF-8 bytes, plus the coverage and tithi range
edges. The tests pinned all of them as literals, and the generator and the library pinned some of
them as constants. The hub now generates `spec/vectors/patro-summary.json` from the same data, and
every port vendors it as `shared/spec/vectors/patro-summary.json`. A real data change, such as the
BS 2084 New Year rename, changes those counts, so a port that pins them cannot merge the weekly
sync pull request without a person editing numbers by hand.

## Decision

1. `./gradlew :codegen:run` reads `shared/spec/vectors/patro-summary.json` and checks every
   data-derived count and range edge it generates against it. A mismatch fails with the field and
   both values in the message. A missing summary fails with a message to re-sync `shared/`, so a
   port whose `shared/` predates the file is not silently checked against nothing.
2. `nepdate-patro` keeps only the invariants the lookups rely on, such as the tithi index fitting
   the name table. It does not pin a count that a data update changes.
3. The tests read the summary at run time through a small reader: the codegen tests use the
   generator's reader, and `nepdate-patro` has a test-only helper under `src/test` because the
   patro module has no JSON library and these tests do not add one. The file has a fixed generated
   shape. The tests still compare each generated table and the coverage with the summary, so
   dropped or duplicated data fails them.
4. ADR-0002's `2793` stays a fact of its day.

## Consequences

- A hub data update that passes the hub's own vector check reaches this port without editing a
  number: codegen and the tests follow the summary.
- A port whose `shared/` predates the summary fails codegen with an instruction to re-sync it.
- The generated tables stay deterministic and unchanged for the same data; the summary is a check
  on their contents, not an input to them.
