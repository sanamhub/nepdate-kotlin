#!/usr/bin/env bash
# The CI command (ADR-0004): runs the quality gates in order and stops at the first failure.
# .github/workflows/ci.yml runs this script, so a green local run means a green CI run.
set -euo pipefail

cd "$(dirname "$0")/.."

# PIT is not in this list: ./scripts/mutation.sh takes about four minutes, so ci.yml runs it in
# its own mutation job. It is not part of the per-change gate.
gates=(
  "exec-bits"
  "shared-sums"
  "ktlintCheck detekt"
  "build"
  "test koverVerify"
  ":codegen:run --args=--check"
  "checkKotlinAbi"
  ":dokkaGenerate"
  "javaApiTest"
  "checkCoreSplit"
  "CORE_ALONE"
  "animalsnifferMain"
  "check-docs"
)

passed=0
for gate in "${gates[@]}"; do
  if [ "$gate" = "CORE_ALONE" ]; then
    # Building the core must not touch the patro module.
    echo "ci: ./gradlew :nepdate:build --dry-run (no :nepdate-patro: task)"
    plan=$(./gradlew --console=plain :nepdate:build --dry-run)
    if grep -q ":nepdate-patro:" <<<"$plan"; then
      echo "ci: :nepdate:build runs a :nepdate-patro: task"
      exit 1
    fi
  elif [ "$gate" = "exec-bits" ]; then
    # A script committed from Windows loses its executable bit, and CI then fails with exit 126.
    echo "ci: every committed *.sh and gradlew is executable"
    if git ls-files -s -- '*.sh' '*gradlew' | grep -v '^100755'; then
      echo "ci: the files above are not executable in git; run git update-index --chmod=+x <file>"
      exit 1
    fi
  elif [ "$gate" = "shared-sums" ]; then
    # ADR-0006: shared/ matches the sums recorded for the hub commit in shared/VERSION.
    echo "ci: ./scripts/shared-sums.sh"
    ./scripts/shared-sums.sh
  elif [ "$gate" = "check-docs" ]; then
    echo "ci: ./scripts/check-docs.sh"
    ./scripts/check-docs.sh
  else
    echo "ci: ./gradlew $gate"
    # shellcheck disable=SC2086 # each gate is a list of Gradle arguments
    ./gradlew --console=plain $gate
  fi
  passed=$((passed + 1))
done

echo "ci: $passed/${#gates[@]} gates passed"
