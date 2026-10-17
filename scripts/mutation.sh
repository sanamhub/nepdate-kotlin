#!/usr/bin/env bash
# K7-03 mutation gate: PIT on :nepdate only, with the threshold in gradle.properties.
# A run takes about four minutes, so ci.yml runs this script in its own job and scripts/ci.sh
# leaves it out.
set -euo pipefail

cd "$(dirname "$0")/.."

threshold="$(sed -n 's/^nepdateMutationThreshold=//p' gradle.properties)"
maxSurviving="$(sed -n 's/^nepdateMaxSurviving=//p' gradle.properties)"
if [ -z "$threshold" ] || [ -z "$maxSurviving" ]; then
  echo "mutation: nepdateMutationThreshold or nepdateMaxSurviving is missing from gradle.properties"
  exit 1
fi

log="$(mktemp)"
trap 'rm -f "$log"' EXIT

set +e
./gradlew --console=plain :nepdate:pitest 2>&1 | tee "$log"
status=${PIPESTATUS[0]}
set -e

line="$(grep -E '^>> Generated [0-9]+ mutations Killed [0-9]+ \([0-9]+%\)$' "$log" | tail -n 1 || true)"
if [ -z "$line" ]; then
  echo "mutation: PIT printed no score line"
  exit 1
fi

score="$(printf '%s\n' "$line" | sed -E 's/.*\(([0-9]+)%\)$/\1/')"
echo "mutation: score ${score}% (threshold ${threshold}%, max surviving ${maxSurviving})"
if [ "$status" -ne 0 ]; then
  exit "$status"
fi
