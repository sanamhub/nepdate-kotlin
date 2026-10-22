# Runbook: release `nepdate-kotlin`

Decided in ADR-0005. Owner: maintainer. Approver: a second person.

One-time setup (done for 0.1.0-beta.1):
1. Sign in to central.sonatype.com with GitHub; the `io.github.sanamhub` namespace verifies itself.
2. Create a Portal user token and a GPG signing key, and publish the public key to
   `keyserver.ubuntu.com`, which the workflows fetch it from. `keys.openpgp.org` serves it without
   its user ID until the address is verified, and gpg skips such a key. Store the four `ORG_GRADLE_PROJECT_*` values from
   ADR-0005 in the `production` environment's secrets. Never paste them into an issue, PR, log or
   chat. The release key is `44ED6E4129D97536CAE086175B979A307721A17B`; `release.yml` and
   `verify-published.yml` check every signature against that fingerprint, so a new key means
   changing it in both files.
3. Give the `production` environment a required reviewer.

## Each release

1. `main` is green; `./scripts/ci.sh` passes. Its last gate runs `scripts/release-check.sh`, so the
   Gradle version must already have its dated changelog section.
2. `gradle.properties` `version=X.Y.Z` (or `X.Y.Z-beta.N`); `CHANGELOG.md` has
   `## [X.Y.Z] - YYYY-MM-DD`, moved out of `[Unreleased]`. A pre-release has its own section: a
   `0.1.0` section does not satisfy `0.1.0-beta.1`, nor the other way round.
3. Dry run: run `release.yml` from the Actions tab with `tag: vX.Y.Z`. It runs the preflight and CI
   and skips everything that signs or uploads. `tools/consumer-check/run.sh` checks the same POMs
   and jars locally through `mavenLocal`.
4. Tag and push:

   ```bash
   git tag -s vX.Y.Z -m "vX.Y.Z"
   git push origin vX.Y.Z
   ```

5. Approve the `production` environment in `release.yml`. The publish job then:
   - signs both artifacts into `mavenLocal` and stops unless each holds exactly the jar, sources,
     javadoc, POM and Gradle module, each with a `.asc` that verifies against the release key;
   - runs `publishAndReleaseToMavenCentral`, which waits for the Central Portal's validation, so a
     rejected deployment fails the job;
   - creates the GitHub Release from the changelog section, marked as a pre-release when the
     version has a `-` part.
6. `verify-published.yml` runs next. On Linux, macOS and Windows it resolves both artifacts from
   Maven Central only (no `mavenLocal`), waiting up to 45 minutes, and checks one conversion and one
   patro event. A separate job checks the jars' signatures on `repo1.maven.org`. To re-check a
   version later, run `verify-published.yml` from `main` with that version.
7. Add an empty `## [Unreleased]` to the changelog if the release commit did not keep one.

Rolling back: Maven Central can't delete a version. Release `X.Y.(Z+1)` with the fix and note it.
