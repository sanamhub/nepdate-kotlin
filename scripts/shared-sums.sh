#!/usr/bin/env bash
# Shared-data check that runs without the hub (ADR-0006). tools/shared.sha256 lists every file in
# shared/data and shared/spec with its SHA-256, under a "# hub <sha>" line that must equal
# shared/VERSION.
#
#   ./scripts/shared-sums.sh                       # exit 1 unless shared/ matches tools/shared.sha256
#   ./scripts/shared-sums.sh --write ../nepdate    # hub check first, then rewrite tools/shared.sha256
#
# --write runs the hub's shared_sync.py --check before it writes, so the sums only ever record a
# shared/ that equals the hub at shared/VERSION.
set -euo pipefail

cd "$(dirname "$0")/.."

sums=tools/shared.sha256

list_files() {
  find shared/data shared/spec -type f -not -path '*/__pycache__/*' | LC_ALL=C sort
}

if [ "${1:-}" = "--write" ]; then
  hub="${2:?usage: ./scripts/shared-sums.sh --write <hub checkout>}"
  python="$(command -v py || command -v python3)"
  "$python" "$hub/tools/shared_sync.py" . --check
  {
    echo "# hub $(tr -d '[:space:]' < shared/VERSION)"
    list_files | while IFS= read -r file; do printf '%s  %s\n' "$(sha256sum < "$file" | cut -c1-64)" "$file"; done
  } > "$sums"
  echo "shared-sums: wrote $sums ($(($(wc -l < "$sums") - 1)) files)"
  exit 0
fi

version="$(tr -d '[:space:]' < shared/VERSION)"
if ! [[ "$version" =~ ^[0-9a-f]{40}$ ]]; then
  echo "shared-sums: shared/VERSION is not a full hub commit SHA: '$version'"
  exit 1
fi
header="$(head -n 1 "$sums")"
if [ "$header" != "# hub $version" ]; then
  echo "shared-sums: $sums is for '${header#\# hub }', shared/VERSION is $version"
  echo "shared-sums: re-sync shared/ and run ./scripts/shared-sums.sh --write <hub checkout>"
  exit 1
fi
if ! diff <(tail -n +2 "$sums" | sed 's/^[0-9a-f]\{64\}  //') <(list_files); then
  echo "shared-sums: the files in shared/ differ from the list in $sums (< listed, > present)"
  exit 1
fi
if ! tail -n +2 "$sums" | sha256sum --check --strict --quiet; then
  echo "shared-sums: shared/ differs from hub $version. Never edit shared/ by hand: re-sync it."
  exit 1
fi
echo "shared-sums: shared/ matches $sums for hub $version ($(($(wc -l < "$sums") - 1)) files)"
