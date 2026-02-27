#!/usr/bin/env bash
# Generates ~/.m2/settings.xml using the GITHUB_TOKEN environment variable.
# In GitHub Codespaces, GITHUB_TOKEN is injected automatically and already has
# the read:packages scope needed to download packages from GitHub Packages.
# To use a personal access token instead, create a Codespace secret named
# MAVEN_GITHUB_TOKEN at:
# https://github.com/settings/codespaces

set -euo pipefail

TOKEN="${MAVEN_GITHUB_TOKEN:-${GITHUB_TOKEN:-}}"

if [ -z "$TOKEN" ]; then
  echo "WARNING: Neither MAVEN_GITHUB_TOKEN nor GITHUB_TOKEN is set." >&2
  echo "         Maven will not be able to authenticate with GitHub Packages." >&2
  echo "         Set a Codespace secret named MAVEN_GITHUB_TOKEN at:" >&2
  echo "         https://github.com/settings/codespaces" >&2
  exit 0
fi

GITHUB_USER="${GITHUB_USER:-${GITHUB_ACTOR:-$(git config --global user.email 2>/dev/null || echo "")}}"

mkdir -p ~/.m2
cat > ~/.m2/settings.xml <<EOF
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>${GITHUB_USER}</username>
      <password>${TOKEN}</password>
    </server>
  </servers>
</settings>
EOF

echo "Maven settings written to ~/.m2/settings.xml (token sourced from environment, not hardcoded)."
