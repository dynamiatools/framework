#!/usr/bin/env bash

set -e

echo "📦 Monorepo Version Bump (pnpm + Maven)"

# Ask for version
read -p "👉 Enter the new version (e.g. 26.4.0): " VERSION

# Basic validation
if [ -z "$VERSION" ]; then
  echo "❌ Version cannot be empty"
  exit 1
fi

echo "🚀 Updating pnpm workspace packages to $VERSION..."

pnpm -r exec npm version "$VERSION" --no-git-tag-version

echo "🔄 Syncing internal dependencies (optional)..."

if command -v syncpack &> /dev/null
then
  pnpm -r exec syncpack set-version "$VERSION"
else
  echo "⚠️ syncpack not found, skipping dependency sync"
fi

# --- Maven part ---
if [ -f "pom.xml" ]; then
  echo "☕ Updating Maven project version to $VERSION..."

  mvn versions:set -DnewVersion="$VERSION" -DgenerateBackupPoms=false

  echo "🔧 Ensuring child modules are updated..."
  mvn versions:update-child-modules
else
  echo "⚠️ No pom.xml found at root, skipping Maven step"
fi

echo "✅ Done. Everything is now at version $VERSION"