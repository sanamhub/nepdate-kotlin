#!/usr/bin/env bash
# Docs gate: no em dashes, and every relative markdown link resolves.
set -euo pipefail

cd "$(dirname "$0")/.."

files=(README.md CHANGELOG.md CONTRIBUTING.md SECURITY.md docs/*.md docs/adr/*.md docs/runbooks/*.md)
issues=0

# 1. No em dashes (U+2014).
if grep -n '—' "${files[@]}" 2>/dev/null; then
  echo "check-docs: em dash found (use 'to', a comma, or parentheses instead)"
  issues=$((issues + 1))
fi

# 2. Every relative markdown link resolves.
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
