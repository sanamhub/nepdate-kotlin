#!/usr/bin/env bash
# K6-04 docs gate: the user-facing docs (README, docs/API, CHANGELOG) read like consumer pages.
# No em dashes, no task IDs, and every relative markdown link resolves.
set -euo pipefail

cd "$(dirname "$0")/.."

files=(README.md docs/API.md CHANGELOG.md)
issues=0

# 1. No em dashes (U+2014).
if grep -n '—' "${files[@]}" 2>/dev/null; then
  echo "check-docs: em dash found (use 'to', a comma, or parentheses instead)"
  issues=$((issues + 1))
fi

# 2. No task IDs (K<phase>-<n> or "phase K<n>").
if grep -nE 'K[0-9]+-[0-9]+|phase K[0-9]+' "${files[@]}" 2>/dev/null; then
  echo "check-docs: task ID found in a user-facing doc"
  issues=$((issues + 1))
fi

# 3. Every relative markdown link resolves.
for f in "${files[@]}"; do
  dir="$(dirname "$f")"
  while IFS= read -r link; do
    [ -z "$link" ] && continue
    path="${link%%#*}"
    case "$path" in
      http://*|https://*|mailto:*) continue ;;
    esac
    if [ ! -e "$dir/$path" ]; then
      echo "check-docs: broken link in $f -> $link"
      issues=$((issues + 1))
    fi
  done < <(grep -oE '\]\([^)]+\)' "$f" | sed -e 's/^\](//' -e 's/)$//')
done

if [ "$issues" -gt 0 ]; then
  echo "check-docs: $issues issue(s)"
  exit 1
fi
echo "check-docs: docs pass"
