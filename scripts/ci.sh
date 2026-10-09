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
  ":nepdate:javaApiTest"
  "animalsnifferMain"
)

passed=0
for gate in "${gates[@]}"; do
  echo "ci: ./gradlew $gate"
  # shellcheck disable=SC2086 # each gate is a list of Gradle arguments
  ./gradlew --console=plain $gate
  passed=$((passed + 1))
done

echo "ci: $passed/${#gates[@]} gates passed"
