#!/usr/bin/env bash
# Positive and negative cases for scripts/release-check.sh. Each case writes its own
# gradle.properties and CHANGELOG.md, so the test does not depend on what the repository says today.
set -euo pipefail

check="$(cd "$(dirname "$0")" && pwd)/release-check.sh"
dir="$(mktemp -d)"
trap 'rm -rf "$dir"' EXIT
failures=0
cases=0

expect() { # expect ok|fail VERSION TAG CHANGELOG-TEXT
  printf 'group=io.github.sanamhub\nversion=%s\n' "$2" > "$dir/gradle.properties"
  printf '%b' "$4" > "$dir/CHANGELOG.md"
  if bash "$check" "$3" "$dir" > "$dir/out" 2>&1; then got=ok; else got=fail; fi
  cases=$((cases + 1))
  if [ "$got" != "$1" ]; then
    echo "FAIL: release-check version '$2' tag '$3' gave $got, want $1 ($(cat "$dir/out"))"
    failures=$((failures + 1))
  fi
}

stable='## [Unreleased]\n\n## [0.1.0] - 2026-10-21\n'
beta='## [Unreleased]\n\n## [0.1.0-beta.1] - 2026-10-21\n'

expect ok   0.1.0         v0.1.0         "$stable"
expect ok   0.1.0-beta.1  v0.1.0-beta.1  "$beta"
expect ok   0.1.0-beta.1  ''             "$beta"
expect ok   1.2.3-rc.10   v1.2.3-rc.10   '## [1.2.3-rc.10] - 2026-01-02\n'
# A beta version cannot use the stable section, nor a stable version the beta one.
expect fail 0.1.0         v0.1.0         "$beta"
expect fail 0.1.0-beta.1  v0.1.0-beta.1  "$stable"
# The tag must be v plus the Gradle version.
expect fail 0.1.0-beta.1  v0.1.0         "$beta"
expect fail 0.1.0-beta.1  0.1.0-beta.1   "$beta"
expect fail 0.1.0         v0.1.0-beta.1  "$stable"
# The dots are literal: 0x1x0 must not match a 0.1.0 heading.
expect fail 0.1.0         v0.1.0         '## [0x1x0] - 2026-10-21\n'
# Malformed versions.
expect fail 0.1           v0.1           '## [0.1] - 2026-10-21\n'
expect fail 01.1.0        v01.1.0        '## [01.1.0] - 2026-10-21\n'
expect fail 0.1.0-        v0.1.0-        '## [0.1.0-] - 2026-10-21\n'
expect fail 0.1.0-beta..1 v0.1.0-beta..1 '## [0.1.0-beta..1] - 2026-10-21\n'
expect fail 0.1.0-beta.01 v0.1.0-beta.01 '## [0.1.0-beta.01] - 2026-10-21\n'
expect fail 0.1.0+build.1 v0.1.0+build.1 '## [0.1.0+build.1] - 2026-10-21\n'
# The section needs a full date.
expect fail 0.1.0         v0.1.0         '## [0.1.0]\n'
expect fail 0.1.0         v0.1.0         '## [0.1.0] - 2026-10\n'
expect fail 0.1.0         v0.1.0         '## [0.1.0] - soon\n'
expect fail 0.1.0         v0.1.0         '### [0.1.0] - 2026-10-21\n'

if [ "$failures" -ne 0 ]; then
  echo "test-release-check: $failures of $cases case(s) failed"
  exit 1
fi
echo "test-release-check: $cases cases ok"
