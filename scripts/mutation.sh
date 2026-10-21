#!/usr/bin/env bash
# Mutation gate: PIT on :nepdate only, with the threshold in gradle.properties.
# A run takes about four minutes, so ci.yml runs this script in its own job and scripts/ci.sh
# leaves it out.
set -euo pipefail

cd "$(dirname "$0")/.."

# The override lets the missing-report control run without deleting the real build output.
report="${NEPDATE_MUTATION_REPORT:-nepdate/build/reports/pitest/mutations.xml}"
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

if [ "$status" -ne 0 ]; then
  echo "mutation: PIT failed with exit code ${status}"
  exit "$status"
fi

if [ ! -f "$report" ]; then
  echo "mutation: report not found at ${report}"
  exit 1
fi

count() {
  grep -c "$1" "$report" || true
}

total="$(count '<mutation ')"
killed="$(count "status='KILLED'")"
timed_out="$(count "status='TIMED_OUT'")"
survived="$(count "status='SURVIVED'")"
no_coverage="$(count "status='NO_COVERAGE'")"
detected=$((killed + timed_out))
missed=$((survived + no_coverage))
unclassified=$((total - detected - missed))

if [ "$total" -eq 0 ]; then
  echo "mutation: report has no mutations at ${report}"
  exit 1
fi
if [ "$unclassified" -ne 0 ]; then
  echo "mutation: ${unclassified} mutations have a status the gate does not classify"
  exit 1
fi

score=$((detected * 100 / total))

# PIT prints this line on a real run. A FROM-CACHE restore prints nothing, so the XML report is
# the source of truth and the console is only a cross-check when it is present.
line="$(grep -E '^>> Generated [0-9]+ mutations Killed [0-9]+ \([0-9]+%\)$' "$log" | tail -n 1 || true)"
if [ -n "$line" ]; then
  read -r console_total console_detected console_score <<< "$(printf '%s\n' "$line" |
    sed -E 's/^>> Generated ([0-9]+) mutations Killed ([0-9]+) \(([0-9]+)%\)$/\1 \2 \3/')"
  if [ "$console_total" -ne "$total" ] || [ "$console_detected" -ne "$detected" ] ||
    [ "$console_score" -ne "$score" ]; then
    echo "mutation: XML report (${detected}/${total}, ${score}%) disagrees with PIT (${console_detected}/${console_total}, ${console_score}%)"
    exit 1
  fi
fi

if [ "$score" -lt "$threshold" ]; then
  echo "mutation: score ${score}% is below threshold ${threshold}%"
  exit 1
fi
if [ "$missed" -gt "$maxSurviving" ]; then
  echo "mutation: ${missed} missed mutations exceeds max surviving ${maxSurviving}"
  exit 1
fi

echo "mutation: score ${score}% (threshold ${threshold}%, max surviving ${maxSurviving})"
