#!/usr/bin/env bash
# K6 consumer check: publish both artifacts to mavenLocal, then build and run a throwaway Gradle
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
cp -r "$SCRIPT_DIR/settings.gradle.kts" "$SCRIPT_DIR/build.gradle.kts" "$SCRIPT_DIR/gradle" \
    "$SCRIPT_DIR/gradlew" "$SCRIPT_DIR/gradlew.bat" "$SCRIPT_DIR/src" "$CONSUMER_DIR/"
chmod +x "$CONSUMER_DIR/gradlew"

cd "$CONSUMER_DIR"

echo "== prove the jars resolve from mavenLocal =="
./gradlew --console=plain -q printNepdatePath | tee /dev/stderr | grep -qE 'nepdate[\\/]0\.1\.0[\\/]'

echo "== run the consumer =="
./gradlew --console=plain run
