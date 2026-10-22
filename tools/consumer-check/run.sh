#!/usr/bin/env bash
# Consumer check: build and run a throwaway Gradle project (copied outside the repo) that depends on
# io.github.sanamhub:nepdate and io.github.sanamhub:nepdate-patro, converts one date and reads one
# patro event, and checks the printed lines.
#
#   tools/consumer-check/run.sh                              publish to mavenLocal, resolve from there
#   tools/consumer-check/run.sh --published 0.1.0-beta.1 [--wait-minutes 45]
#
# The default proves the coordinates, POMs and README install block of this checkout. --published
# proves a released version: no mavenLocal, only Maven Central, retried until Central serves it,
# because a new release can take 10 to 30 minutes to appear.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

published=""
wait_minutes=0
while [ $# -gt 0 ]; do
  case "$1" in
    --published) published="${2:?--published needs a version}"; shift 2 ;;
    --wait-minutes) wait_minutes="${2:?--wait-minutes needs a number}"; shift 2 ;;
    *) echo "consumer-check: unknown argument '$1'" >&2; exit 2 ;;
  esac
done

# The throwaway project lives outside the repo; override CONSUMER_DIR to pick a location.
CONSUMER_DIR="${CONSUMER_DIR:-${RUNNER_TEMP:-${TMPDIR:-/tmp}}/kt-consumer}"

cd "$REPO_ROOT"

if [ -n "$published" ]; then
  VERSION="$published"
  SOURCE=central
else
  VERSION="$(grep '^version=' gradle.properties | cut -d= -f2 | tr -d '[:space:]')"
  SOURCE=local
  echo "== publish $VERSION to mavenLocal =="
  ./gradlew --console=plain publishToMavenLocal
fi

echo "== copy the consumer out of the repo =="
rm -rf "$CONSUMER_DIR"
mkdir -p "$CONSUMER_DIR"
cp -r "$SCRIPT_DIR/settings.gradle.kts" "$SCRIPT_DIR/build.gradle.kts" "$SCRIPT_DIR/src" \
    "$CONSUMER_DIR/"

# The repo's own wrapper drives the copy (-p), so the consumer carries no second wrapper jar. The
# version and the repository choice reach the consumer build as Gradle properties.
GRADLE=("$REPO_ROOT/gradlew" --console=plain -p "$CONSUMER_DIR"
  "-PnepdateVersion=$VERSION" "-PnepdateSource=$SOURCE")

echo "== resolve $VERSION from $SOURCE =="
deadline=$(( $(date +%s) + wait_minutes * 60 ))
while true; do
  if resolved="$("${GRADLE[@]}" -q --refresh-dependencies printNepdatePath 2>&1)"; then
    break
  fi
  if [ "$(date +%s)" -ge "$deadline" ]; then
    printf '%s\n' "$resolved" >&2
    echo "consumer-check: $VERSION did not resolve from $SOURCE" >&2
    exit 1
  fi
  echo "consumer-check: $VERSION not served by $SOURCE yet, retrying in 60s"
  sleep 60
done
printf '%s\n' "$resolved" >&2
# Both jars must be on the classpath at exactly this version.
for artifact in nepdate nepdate-patro; do
  if ! printf '%s\n' "$resolved" | grep -qE "[\\/]$artifact-${VERSION//./\\.}\.jar\$"; then
    echo "consumer-check: $artifact-$VERSION.jar is not on the classpath" >&2
    exit 1
  fi
done
# A checkout's jars come from the local repository; a release's never do.
if [ "$SOURCE" = local ] && ! printf '%s\n' "$resolved" | grep -q '[\\/]\.m2[\\/]'; then
  echo "consumer-check: the jars did not come from mavenLocal" >&2
  exit 1
fi
if [ "$SOURCE" = central ] && printf '%s\n' "$resolved" | grep -q '[\\/]\.m2[\\/]'; then
  echo "consumer-check: a released check resolved from mavenLocal" >&2
  exit 1
fi

echo "== run the consumer =="
output="$("${GRADLE[@]}" -q run)"
printf '%s\n' "$output"
# AD 2024-04-13 is BS 2081-01-01, and its first event is New Year. The Nepali text in parentheses is
# printed for a reader but not compared, so a console code page cannot fail the check.
printf '%s\n' "$output" | tr -d '\r' | grep -qx 'converted: 2081-01-01 (.*)' || {
  echo "consumer-check: the conversion line is wrong" >&2
  exit 1
}
printf '%s\n' "$output" | tr -d '\r' | grep -qx 'event: New Year (.*)' || {
  echo "consumer-check: the patro line is wrong" >&2
  exit 1
}
echo "consumer-check: ok: $VERSION from $SOURCE"
