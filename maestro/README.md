# Maestro E2E (ShopTourr / Voyage)

Smoke flows for ch.10: **auth → create trip → add purchase**, plus tab-bar a11y.

## Prerequisites

1. Install [Maestro](https://maestro.mobile.dev): `curl -Ls "https://get.maestro.mobile.dev" | bash`
2. Build and install the Android app on a device/emulator:
   ```bash
   ./gradlew :androidApp:installDebug
   ```
3. Backend reachable at the app `AppConfig.apiBaseUrl` (default `https://api.shoptourr.com/api`).
4. Export test credentials:
   ```bash
   export MAESTRO_EMAIL='e2e@example.com'
   export MAESTRO_PASSWORD='your-password'
   ```

## Run

```bash
# Login only
maestro test maestro/flows/auth_login.yaml

# Full smoke: login → trip → purchase
maestro test maestro/flows/add_purchase_smoke.yaml

# Tab bar semantics smoke (no credentials required if already logged in)
maestro test maestro/flows/tab_bar_a11y.yaml
```

## Selectors

Flows use Compose `testTag` ids from `VoyageTestTags` (enabled via `testTagsAsResourceId` in `MainActivity`).
Keep YAML ids in sync with `shared/.../ui/testing/VoyageTestTags.kt`.

## CI

`workflow_dispatch` job `maestro` on GitHub Actions installs the debug APK onto an Android emulator and runs `maestro/flows/tab_bar_a11y.yaml`. Auth/purchase smokes still need a live API and secrets — run those locally.
