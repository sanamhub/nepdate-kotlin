#!/usr/bin/env bash
# Consumer check: publish both artifacts to mavenLocal, then build and run a throwaway Gradle
# project (copied outside the repo) that consumes io.github.sanamhub:nepdate:0.1.0 and
# io.github.sanamhub:nepdate-patro:0.1.0 from mavenLocal only. Success proves the published
# coordinates, POMs and the README install block work together.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
# The throwaway project lives outside the repo; override CONSUMER_DIR to pick a location.
CONSUMER_DIR="${CONSUMER_DIR:-${TMPDIR:-/tmp}/kt-consumer}"

cd "$REPO_ROOT"

echo "== publish to mavenLocal =="
./gradlew --console=plain publishToMavenLocal

echo "== copy the consumer out of the repo =="
rm -rf "$CONSUMER_DIR"
mkdir -p "$CONSUMER_DIR"
cp -r "$SCRIPT_DIR/settings.gradle.kts" "$SCRIPT_DIR/build.gradle.kts" "$SCRIPT_DIR/src" \
    "$CONSUMER_DIR/"

# The repo's own wrapper drives the copy (-p), so the consumer carries no second wrapper jar.
VERSION="$(grep '^version=' gradle.properties | cut -d= -f2)"
GRADLE=("$REPO_ROOT/gradlew" --console=plain -p "$CONSUMER_DIR")

echo "== prove the jars resolve from mavenLocal =="
"${GRADLE[@]}" -q printNepdatePath | tee /dev/stderr \
    | grep -qE "nepdate[\\/]${VERSION//./\\.}[\\/]"

echo "== run the consumer =="
"${GRADLE[@]}" run
