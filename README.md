# Android Compose Template

Opinionated starter for modern Android apps: Compose + Material 3, multi-module-ready,
Koin DI, a curated version catalog, and a JUnit 5 test setup. Kept current by Renovate.

## Create a new project
1. Click **"Use this template"** on GitHub → create your repo.
2. Clone it, then run the rename script:
   ```bash
   ./new-project.sh com.you.yourapp "Your App"
   rm -rf .git && git init && git add -A && git commit -m "Initial commit"
   ```