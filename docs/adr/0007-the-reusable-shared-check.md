# ADR-0007: The reusable shared check, without the sums

- **Status:** accepted
- **Date:** 2026-10-31
- **Approved by:** Sanam
- **Supersedes:** ADR-0006 (the shared check and the sums); its manual docs deploy is unchanged

## Context

ADR-0006 replaced the hub's reusable `shared-check` workflow with `tools/shared.sha256` and
`scripts/shared-sums.sh`, the first gate of `scripts/ci.sh`, because the hub had no GitHub remote
and GitHub fails a workflow at startup when a reusable workflow cannot be resolved. The hub is now
at `sanamhub/nepdate` on GitHub, so the reusable workflow resolves and the reason for the local
check is gone.

The sums also block every sync. `shared/` and `shared/VERSION` change on every hub sync, and
`tools/shared.sha256` records the SHA-256 of every vendored file. The hub's `shared-sync` workflow
cannot run `./scripts/shared-sums.sh --write <hub checkout>`, because that needs a local hub
checkout and Python. Gate 1 would fail on exactly the change the sync exists to make, and every
sync would need a person to rewrite the manifest by hand.

The hub workflow compares the vendored tree with the real hub at the commit in `shared/VERSION`,
which is the check this repository wants. The sums compare the tree with a second file in the same
repository, which proves only that nobody edited `shared/` after the sums were written.

## Decision

1. **Call the hub's `shared-check`.** `.github/workflows/ci.yml` gets a `shared-check` job with
   `permissions: contents: read` that calls the hub's reusable workflow, pinned to commit
   `f94d67c0c85de7909cf54d7e4e96f226ca750129` (main, 2026-10-31). The called workflow fetches the
   hub at the commit in `shared/VERSION` and fails when `shared/data` or `shared/spec` differs.
2. **Remove the sums.** Delete `tools/shared.sha256` and `scripts/shared-sums.sh`, and drop the
   `shared-sums` gate from `scripts/ci.sh`. The summary line still counts the gates it ran, so it
   now reads `ci: 13/13 gates passed`.
3. **Let the sync start CI.** Add `workflow_dispatch:` to the `on:` block of `ci.yml`. The hub's
   `shared-sync` workflow opens its pull request with `GITHUB_TOKEN`, and a pull request opened
   that way does not trigger `pull_request` workflows, so it dispatches CI instead.
4. **Keep the local check.** `py tools/shared_sync.py . --check` from a hub checkout still compares
   a working tree with the hub. This ADR removes the vendored sums, not the local tool.
5. **Docs deploy stays manual.** ADR-0006 section 4 still applies: `docs.yml` runs on
   `workflow_dispatch` only, and the Dokka build is a `scripts/ci.sh` gate.

## Consequences

- Every push and pull request runs the hub's check against the hub at the commit in
  `shared/VERSION`, not against a file this repository maintains.
- A sync pull request changes `shared/` and `shared/VERSION` only, so it can pass without hand
  edits.
- The reusable workflow is pinned to a full hub commit, so a hub change cannot alter this
  repository's check until the pin moves in a reviewed change.
- No `shared-sync` workflow is added here. A later change adds it against the hub.
