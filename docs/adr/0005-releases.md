# ADR-0005: Releases

- Status: Accepted
- Date: 2026-10-06
- Deciders: Sanam Pakuwal

## Decision

- Coordinates `io.github.sanamhub:nepdate` and `io.github.sanamhub:nepdate-patro`, same version,
  semver, released together from one tag (hub ADR-0004). The patro POM depends on `nepdate` at
  exactly that version (what a Gradle project dependency publishes), which is inside the same minor
  version as hub ADR-0004 §3 asks. A data update that only touches the patro still releases both. The `io.github.sanamhub`
  namespace is verified on the Central Portal through the GitHub
  account (human task, once).
- Publish to Maven Central through the **Central Portal** (OSSRH closed on 2025-06-30) with
  `com.vanniktech.maven.publish` 0.37: `publishToMavenCentral(automaticRelease = true)`,
  `signAllPublications()`, POM with the MIT licence, SCM and developer.
- Tags `vX.Y.Z`. `release.yml` on tag: preflight (Gradle `version`, tag and `CHANGELOG.md` section
  agree), CI, `environment: production` approval, `publishAndReleaseToMavenCentral`, a GitHub Release
  with notes from the changelog, then a check that `repo1.maven.org` serves both POMs.
- Secrets (Portal user token, in-memory GPG key and its password) live only in the `production`
  environment's secret store as `ORG_GRADLE_PROJECT_mavenCentralUsername`,
  `ORG_GRADLE_PROJECT_mavenCentralPassword`, `ORG_GRADLE_PROJECT_signingInMemoryKey` and
  `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword`. Maven Central has no OIDC trusted publishing;
  revisit when it does.
- Never delete or overwrite a published version (Maven Central refuses anyway). A bad release is
  fixed by a new patch version and a changelog note.
- The weekly `shared-sync` PR, once merged, is released as a patch version.
