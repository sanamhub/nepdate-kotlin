#!/usr/bin/env bash
# Fast controls for scripts/mutation.sh. The fixtures simulate a cached PIT report; PIT is not run.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

write_report() {
  local killed="$1" survived="$2" path="$3"
  local i
  {
    echo "<mutations>"
    for ((i = 0; i < killed; i++)); do
      echo "<mutation status='KILLED'></mutation>"
    done
    for ((i = 0; i < 33; i++)); do
      echo "<mutation status='TIMED_OUT'></mutation>"
    done
    for ((i = 0; i < survived; i++)); do
      echo "<mutation status='SURVIVED'></mutation>"
    done
    for ((i = 0; i < 24; i++)); do
      echo "<mutation status='NO_COVERAGE'></mutation>"
    done
    echo "</mutations>"
  } > "$path"
}

expect_failure() {
  local report="$1" message="$2" output status
  set +e
  output="$(NEPDATE_MUTATION_CACHED=1 NEPDATE_MUTATION_REPORT="$report" \
    "$SCRIPT_DIR/mutation.sh" 2>&1)"
  status=$?
  set -e

  if [ "$status" -ne 1 ]; then
    printf 'expected exit 1, got %s\n%s\n' "$status" "$output" >&2
    exit 1
  fi
  if ! printf '%s\n' "$output" | grep -qF "$message"; then
    printf 'expected message: %s\ngot:\n%s\n' "$message" "$output" >&2
    exit 1
  fi
}

# 622 mutations: 538 killed + 33 timed out = 571 detected; 27 survived + 24 no coverage = 51 missed.
write_report 538 27 "$tmp/mutations-51.xml"
expect_failure "$tmp/mutations-51.xml" \
  "mutation: 51 missed mutations exceeds max surviving 49"

printf "<mutations><mutation status='KILLED'></mutations>\n" > "$tmp/malformed.xml"
expect_failure "$tmp/malformed.xml" "mutation: report is not well-formed XML"

echo "mutation controls: pass"
