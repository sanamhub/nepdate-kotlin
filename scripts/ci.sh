#!/usr/bin/env bash
# The CI command (ADR-0004): runs the quality gates in order and stops at the first failure.
# .github/workflows/ci.yml runs this script, so a green local run means a green CI run.
set -euo pipefail

cd "$(dirname "$0")/.."

gates=(
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
    # K4-05: building the core must not touch the patro module.
    echo "ci: ./gradlew :nepdate:build --dry-run (no :nepdate-patro: task)"
    plan=$(./gradlew --console=plain :nepdate:build --dry-run)
    if grep -q ":nepdate-patro:" <<<"$plan"; then
      echo "ci: :nepdate:build runs a :nepdate-patro: task"
      exit 1
    fi
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
