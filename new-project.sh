#!/usr/bin/env bash
# Usage: ./new-project.sh <applicationId> "<App Name>"
# Example: ./new-project.sh com.acme.notes "Acme Notes"
set -euo pipefail

OLD_PKG="org.dmn.template"
OLD_NAME="AndroidTemplate"

if [ $# -lt 2 ]; then
  echo "Usage: ./new-project.sh <applicationId> \"<App Name>\""
  echo "Example: ./new-project.sh com.acme.notes \"Acme Notes\""
  exit 1
fi

NEW_PKG="$1"
NEW_NAME="$2"
OLD_PATH="${OLD_PKG//.//}"
NEW_PATH="${NEW_PKG//.//}"

echo "Package: $OLD_PKG -> $NEW_PKG"
echo "Name:    $OLD_NAME -> $NEW_NAME"

# 1. Text replacements (build scripts, sources, manifest, res). Env vars avoid regex-escaping issues.
grep -rlI --exclude-dir=.git --exclude-dir=.gradle --exclude-dir=build \
     -e "$OLD_PKG" -e "$OLD_NAME" . | while IFS= read -r f; do
  OLD_PKG="$OLD_PKG" NEW_PKG="$NEW_PKG" OLD_NAME="$OLD_NAME" NEW_NAME="$NEW_NAME" \
    perl -i -pe 's/\Q$ENV{OLD_PKG}\E/$ENV{NEW_PKG}/g; s/\Q$ENV{OLD_NAME}\E/$ENV{NEW_NAME}/g' "$f"
done

# 2. Move package source dirs in each source set
for SET in main test androidTest; do
  SRC="app/src/$SET/kotlin/$OLD_PATH"
  [ -d "$SRC" ] || continue
  DEST="app/src/$SET/kotlin/$NEW_PATH"
  mkdir -p "$(dirname "$DEST")"
  mv "$SRC" "$DEST"
  find "app/src/$SET/kotlin" -type d -empty -delete 2>/dev/null || true
done

echo "✅ Renamed. Next: review the diff, then  rm -rf .git && git init && git add -A && git commit -m 'Initial commit'"