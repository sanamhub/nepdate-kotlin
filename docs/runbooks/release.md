# Runbook: release `nepdate-kotlin`

Decided in ADR-0005. Owner: maintainer. Approver: a second person.

One-time setup (human):
1. Sign in to central.sonatype.com with GitHub; the `io.github.sanamhub` namespace verifies itself.
2. Create a Portal user token and a GPG signing key (publish the public key to
   `keyserver.ubuntu.com`). Store the four `ORG_GRADLE_PROJECT_*` values from ADR-0005 in the
   `production` environment's secrets. Never paste them into an issue, PR, log or chat.
3. Give the `production` environment a required reviewer.

Each release:
1. `main` is green; `./scripts/ci.sh` passes.
2. `gradle.properties` `version=X.Y.Z`; `CHANGELOG.md` has `## [X.Y.Z] - YYYY-MM-DD`, moved out of
   `[Unreleased]`.
3. Optional dry run: `./gradlew publishToMavenLocal`, then check the POMs in
   `~/.m2/repository/io/github/sanamhub/`: `nepdate` lists only `kotlin-stdlib`; `nepdate-patro`
   lists `kotlin-stdlib` and `nepdate` at version `X.Y.Z`. Both artifacts always ship together, even
   when only `shared/data/patro/` changed.
4. Tag and push:

   ```bash
   git tag -s vX.Y.Z -m "vX.Y.Z"
   git push origin vX.Y.Z
   ```

5. Approve the `production` environment in `release.yml`. It publishes, creates the GitHub Release
   and waits until `https://repo1.maven.org/maven2/io/github/sanamhub/nepdate/X.Y.Z/` and
   `https://repo1.maven.org/maven2/io/github/sanamhub/nepdate-patro/X.Y.Z/` serve their POMs
   (usually within 30 minutes).
6. Add an empty `## [Unreleased]` to the changelog.

Rolling back: Maven Central can't delete a version. Release `X.Y.(Z+1)` with the fix and note it.
