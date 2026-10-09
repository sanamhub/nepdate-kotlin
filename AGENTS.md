# AGENTS.md: rules for AI coding agents (Kotlin port)

You are implementing a planned project. Follow the plan; don't redesign it.

## 0. Before writing any code

1. Read `README.md`, this file, every ADR in `docs/adr/`, and `docs/API.md`.
2. Open `docs/plan/00-ROADMAP.md`, find your phase, then read its plan file (`docs/plan/K<n>-*.md`)
   and the files it lists under "Read first". Check the preconditions; if one is unmet, stop and
   say so.

## 1. Sources of truth (in priority order)

1. `shared/spec/*.md` + `shared/spec/vectors/*`: behaviour. Vectors win over prose (report the
   disagreement).
2. `docs/adr/*`: decisions. 3. `docs/plan/*` and `shared/spec/PORT-PLAN.md`: tasks and acceptance
   criteria. 4. `docs/API.md`: the public surface.

`shared/` is a read-only copy of the hub. **Never edit it.** A data or spec problem is reported to
the human as a hub issue. Never use the C# NepDate docs as a source (`shared/spec/PARITY.md` §1).

## 2. Hard rules

- `nepdate` depends only on `kotlin-stdlib`; `nepdate-patro` only on `kotlin-stdlib` and `nepdate`.
  Test, codegen and benchmark dependencies stay in their own modules or test configurations.
- The core (`:nepdate`) never depends on `:nepdate-patro` and holds no tithi, holiday or event data,
  not even in tests (hub ADR-0004; `checkCoreSplit` in ADR-0004 step 7).
- `explicitApi()` is on. No `public` symbol beyond `docs/API.md`; `checkKotlinAbi` must pass.
- The core never imports `java.*` except in `JavaTime.kt` (the `java.time` interop file). No
  `kotlinx-datetime`, no reflection, no `android.*`.
- No `value class`, no `suspend`, no coroutines in the public API (Java callers can't use them well).
- Hot paths follow ADR-0002: no allocation besides the returned `NepaliDate` in conversion,
  arithmetic and parsing, none at all in patro lookups. The allocation tests (ADR-0004) enforce it.
- No `!!`, `error()`, `require()` or `check()` on paths reachable from public API except where
  `docs/API.md` says the function throws `NepDateException`.
- No global mutable state. Every public type is immutable and safe to share across threads.
- Don't edit `*.generated.kt` by hand. Change `:codegen` and run it.
- Parity tests may skip rows **only** for deviations in `shared/spec/PARITY.md`, with a comment naming
  the D-ID. Don't weaken or delete a test to make CI pass.
- No features outside the plan. Secrets never go in code, logs, Gradle files or commits.

## 3. How to work

- One task ID at a time (`K1-03`). Test first, see it fail, then implement.
- Run `./scripts/ci.sh` before saying you're done; paste its last line in the PR.
- PR description: task IDs, what changed and why, how tested, "What to look at first" (2 or 3 bullets).

## 4. Writing

All prose follows [`.claude/skills/writing-style/SKILL.md`](.claude/skills/writing-style/SKILL.md),
for every agent. KDoc on every public symbol: summary, `@return`, `@throws`.
**No AI attribution** in commits or PRs.
