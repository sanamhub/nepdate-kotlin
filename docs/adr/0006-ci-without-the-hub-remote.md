# ADR-0006: CI without the hub on GitHub

- Status: Accepted
- Date: 2026-10-19
- Deciders: Sanam Pakuwal
- Supersedes: the reusable-workflow sentence of ADR-0001 §5 and the weekly `shared-sync` line of
  ADR-0005, until the hub has a GitHub remote

## Context

ADR-0001 §5 has CI call the hub's reusable `shared-check` workflow and a weekly `shared-sync`
workflow. Both are `uses: sanamhub/nepdate/.github/workflows/...`, and the hub has no GitHub remote
yet. GitHub fails a workflow at startup when a reusable workflow cannot be resolved, so `ci.yml`
ran no job on any push or pull request (run 39095046454), and `shared-sync` could never run. The
Dokka build failed dependency verification on GitHub (run 39095046240) without anyone seeing it,
because the `build` job that runs the same `:dokkaGenerate` gate never started.

## Decision

1. **Shared check from vendored sums.** `tools/shared.sha256` lists every file in `shared/data` and
   `shared/spec` with its SHA-256, under a `# hub <sha>` line. `./scripts/shared-sums.sh` fails
   unless that line equals `shared/VERSION`, the listed files equal the files present, and every
   hash matches. It is the first gate of `scripts/ci.sh`, so it runs locally and in the `build` job.
2. **Sums are written only after the hub check.** `./scripts/shared-sums.sh --write <hub checkout>`
   runs the hub's `tools/shared_sync.py . --check` first and writes the sums only when it passes.
   Syncing stays local: `shared_sync.py` from the hub checkout, then `--write`, then
   `./gradlew :codegen:run`.
3. **No `shared-sync` workflow.** It is deleted rather than kept failing every week.
4. **Docs deploy is manual.** GitHub Pages is not enabled on the repository, so `docs.yml` runs on
   `workflow_dispatch` only. The Dokka build is not lost: it is a `scripts/ci.sh` gate.
5. When the hub is on GitHub, the reusable `shared-check` and `shared-sync` come back in a new ADR,
   and the sums file goes.

## Consequences

- CI on GitHub runs every job again.
- The CI check is weaker than the hub's: it proves `shared/` has not changed since sums were written
  for `shared/VERSION`, not that it equals the hub. Someone who edits `shared/` and rewrites the sums
  by hand would pass CI; `--write` refuses to, and review of any `tools/shared.sha256` change
  without a `shared/VERSION` change catches it.
- Negative controls (2026-10-19): a byte appended to `shared/spec/PARITY.md`, an extra
  `shared/data/extra.txt` and a different `shared/VERSION` each make `./scripts/shared-sums.sh`
  exit 1.
