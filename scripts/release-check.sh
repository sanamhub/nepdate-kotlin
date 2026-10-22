#!/usr/bin/env bash
# Release preflight (ADR-0005): the Gradle version is X.Y.Z or a semver pre-release such as
# 0.1.0-beta.1, the tag (when given) is "v" plus that version, and CHANGELOG.md has the version's own
# dated section. A stable section does not satisfy a pre-release version, nor the other way round,
# because the heading is matched exactly.
#
#   scripts/release-check.sh [TAG] [REPO_DIR]
#   scripts/release-check.sh v0.1.0-beta.1
set -euo pipefail

tag="${1:-}"
root="${2:-.}"

version="$(grep '^version=' "$root/gradle.properties" | cut -d= -f2 | tr -d '[:space:]')"

ident='(0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*)'
pattern="^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(-$ident(\.$ident)*)?$"

if ! printf '%s' "$version" | grep -Eq "$pattern"; then
  echo "release-check: gradle.properties version '$version' is not X.Y.Z or X.Y.Z-PRERELEASE"
  exit 1
fi
if [ -n "$tag" ] && [ "$tag" != "v$version" ]; then
  echo "release-check: tag '$tag' does not match gradle.properties version $version (want v$version)"
  exit 1
fi
# index() compares literally, so the dots in the version are not regex wildcards.
if ! awk -v v="$version" '
  index($0, "## [" v "] - ") == 1 && substr($0, length("## [" v "] - ") + 1) ~ /^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]$/ { found = 1 }
  END { exit found ? 0 : 1 }
' "$root/CHANGELOG.md"; then
  echo "release-check: CHANGELOG.md has no '## [$version] - YYYY-MM-DD' section"
  exit 1
fi
echo "release-check: $version ok"
