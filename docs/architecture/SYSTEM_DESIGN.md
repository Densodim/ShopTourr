# Voyage (ShopTourr) — Mobile System Design

**Product:** travel tracker (trips, purchases, VAT/Tax Free, diary, wishlist, map, stats)  
**Client:** Kotlin Multiplatform + Compose Multiplatform (`androidApp` / `iosApp` / `shared`)  
**Backend:** Spring Boot 4 (Jakarta EE 11, Jackson 3, Java 17+)  
**Design source:** `.mila-design/` (Voyage prototype)  
**Process:** TDD (Red → Green → Refactor) — see `docs/architecture/TDD.md`  
**Library APIs grounded via Context7** (Ktor, SQLDelight, Koin, kotlinx.serialization, multiplatform-settings, Turbine)

---

## 0. Assumptions (confirm / change)

| # | Assumption | Default |
|---|---|---|
| A1 | Platforms | Android + iOS only (no web/desktop v1) |
| A2 | UI sharing | Compose Multiplatform in `shared` (already in project) |
| A3 | Auth | Email/password + JWT access (15m) + refresh (30d) in HttpOnly-equivalent secure storage |
| A4 | Offline | Cache-first for trips/purchases/diary; mutation outbox with LWW |
| A5 | Media | Receipt photos via pre-signed upload (S3-compatible); OCR async job v1.1 |
| A6 | Money | `BigDecimal` + ISO-4217; trip locks currency at creation |
| A7 | FX | Snapshot rate at trip create (+ optional refresh); base display currency from user prefs (default RUB) |
| A8 | i18n UI | Client strings (RU/EN); user content is plain text (not bilingual maps) |
| A9 | API version | Boot 4 native versioning → `version = "1"`; path `/api` |
| A10 | Multi-user trip | Travelers are trip-local profiles (not full accounts) in v1 |
| A11 | Scale v1 | Single-region API, <50k MAU, Postgres |

---

## 1. Layers (client)

```
UI (Compose screens)
  → Navigation (typed routes)
    → Presentation (ViewModel + UiState/UiError, MVI/UDF)
      → Domain (use-cases, pure Kotlin, AppError)
        → Data (Repository implementations)
          ├─ Remote: Ktor Client + kotlinx.serialization (DTOs in data/remote/dto/)
          ├─ Local:  SQLDelight (relational) + multiplatform-settings (prefs)
          └─ Outbox: pending mutations → sync worker
Platform: Keychain/Keystore, Camera, Push, Maps
```

**Layer rules (KMP skill):**
- Dependencies point inward only: `presentation → domain ← data`
- `UiState.error` is always `UiError`, never raw `AppError` / `Throwable`
- Wire DTOs live under `data/remote/dto/` — not imported by `ui/` or `domain/`
- UI/App never injects `TokenStore`; session checks go through domain use-cases (`IsLoggedInUseCase`)

### Concrete choices (trade-offs)

| Concern | Choice | Trade-off |
|---|---|---|
| UI | Compose MP | One UI codebase; iOS look slightly less “native” than SwiftUI |
| Nav | Navigation-Compose MP / Decompose | Nav-Compose simpler; Decompose better for complex back-stacks |
| Server state | Repository + Flow (SQLDelight as SoT) | Heavier than “just Ktor”; correct for offline |
| Client prefs | multiplatform-settings | Tiny; enough for lang/theme/push |
| Auth tokens | SecureTokenStore (EncryptedSharedPreferences / Keychain) | Never store JWT in plain Settings on device |
| Net | Ktor + serialization | Idiomatic KMP; less codegen than Retrofit |
| DI | Koin MP | Lighter than Hilt-on-Android-only |
| Lists | LazyColumn + keys; paging later | FlashList N/A on CMP |
| Auth storage | Settings encrypted / Keychain via expect/actual | Must never put refresh token in plain prefs |
| Images | coil3 multiplatform | Disk cache separate from DB |
| Observability | Sentry Kotlin MP | Crash + breadcrumb; wire request-id header |

---

## 2. Backend layers (Spring Boot 4)

```
Controller (DTO in/out, version=1)
  → Application service (orchestration, transactions)
    → Domain (entities, VAT/TaxFree rules, budget alerts)
      → Persistence (JPA/Flyway + Postgres)
      → Integrations (FX rates, object storage, OCR, mail)
```

**Suggested modules (when you scaffold):**
- `voyage-api` — REST, security, DTOs (`docs/api/spring/…`)
- `voyage-domain` — pure domain + VAT/TaxFree calculators
- `voyage-infra` — JPA, S3, FX client

**Stack defaults:** Spring Web MVC (Tomcat), Spring Security 7 (JWT resource server), Spring Data JPA, Flyway, PostgreSQL, Redis (refresh token denylist / rate limit), Actuator + Micrometer.

---

## 3. Domain model (from Voyage mock)

```
User 1──* Trip 1──* Purchase
         │       └──* PurchaseSplit → Traveler
         ├──* Traveler
         ├──* DiaryEntry
         ├──  ExchangeRateSnapshot
         └──  TripStats (computed)
User 1──* WishlistItem
User 1──  Preferences
Purchase 0..1── MediaAsset (receipt)
Trip ── derived → Alerts, TaxFreeEligibility, RouteStops
```

**Trip status:** `ACTIVE | UPCOMING | PAST | ARCHIVED`  
**Purchase categories:** `FOOD | TRANSPORT | SOUVENIRS | HOTEL | CULTURE | OTHER`  
**VAT:** stored as rate on trip + per-purchase `vatIncluded`; server computes net/vat/gross.  
**Tax Free:** flag on purchase + country rules (min amount, refund rate) from `TaxFreeRules`.

---

## 4. Screen → API map

| Screen | Primary endpoints |
|---|---|
| Welcome / SignUp / SignIn / Forgot | `POST /auth/register`, `/auth/login`, `/auth/forgot-password`, `/auth/refresh`, `/auth/logout` |
| Home (tab) | `GET /home` (aggregate) |
| Trip list / detail | `GET/POST /trips`, `GET/PATCH /trips/{id}` |
| New trip | `POST /trips` |
| Add purchase | `POST /trips/{id}/purchases` + media intent |
| Stats | `GET /trips/{id}/stats` |
| Map | `GET /trips/{id}/route` |
| Diary | `GET/POST /trips/{id}/diary` |
| Alerts | `GET /trips/{id}/alerts` |
| Tax Free | `GET /trips/{id}/tax-free` |
| Export | `POST /trips/{id}/exports` → poll job |
| Wishlist (tab) | `GET/POST/DELETE /wishlist` |
| Profile (tab) / Settings / Privacy / About / Support | `GET/PATCH /me`, `GET/PATCH /me/preferences` |

---

## 5. Offline / sync

1. **Read path:** SQLDelight → UI immediately; background refresh if stale.
2. **Write path:** optimistic local write + `SyncOutbox` row (`PENDING`).
3. **Drain:** on network, FIFO by `createdAt`; success → delete outbox; conflict → **server wins** for v1 (return 409 + fresh entity).
4. **Idempotency:** client sends `Idempotency-Key` (UUID) on all POSTs that create money-moving entities.
5. **Media:** upload file first (pre-signed), then attach `mediaId` to purchase create.

---

## 6. Cross-cutting

- **Auth:** Bearer access JWT; refresh rotation; 401 → silent refresh once → logout.
- **Errors:** RFC 7807 Problem Details (`type`, `title`, `status`, `detail`, `code`, `errors[]`).
- **Idempotency / tracing:** `Idempotency-Key`, `X-Request-Id` (echo).
- **Rate limit:** login 10/min/IP; write APIs 120/min/user.
- **Privacy:** receipt photos private; export jobs expire 24h.
- **Push:** trip budget alerts via `POST /me/devices` (FCM/APNs token); prefs flag already in DTO.
- **Deep links:** `voyage://trips/{id}/alerts|tax-free|route` → `VoyageDeepLinkParser` (wire from FCM/APNs payload).
- **Media upload:** `ResumableMediaUploader` (v1 pre-signed PUT; tus/multipart resume later).

### 6.1 Standard blocks (mobile-system-design ch.10) — decisions

| Block | Decision (v1) | Status |
|---|---|---|
| Force update | `GET /me/app-config` → `minAndroidBuild` / `minIosBuild`; soft prompt then hard block | Wired (client) |
| Feature flags / remote config | Same `/me/app-config` + boolean flags (`exportPdf`, `ocrAssist`, `nativeMaps`) | Wired (client) |
| A/B | Flags only until analytics funnel exists; no client experiment SDK in v1 | Deferred |
| Analytics | PostHog or Firebase Analytics; offline event queue in SQLDelight | Planned |
| Crash / observability | Sentry Kotlin MP + `X-Request-Id` breadcrumb | Chosen, not wired |
| Certificate pinning | Ktor `HttpClient` public-key pins for `api.shoptourr.com` (release builds) | Planned |
| Biometrics | Optional unlock after login via Keychain/Keystore `accessControl` | Deferred P3 |
| E2E | Maestro flows: auth → add purchase → offline sync | Planned |
| A11y | Compose semantics + TalkBack/VoiceOver smoke in Maestro | Planned |
| CI/CD | GitHub Actions: unit + androidHostTest; Fastlane/ASC later | Planned |
| App size | Budget 40 MB install; monitor ABI splits | Planned |
| Modularization | Keep `shared` monolith until backend + 2nd team; then `feature-*` + contract modules | Deferred |

---

## 7. Phased delivery

| Phase | Scope |
|---|---|
| **P0** | Auth, trips CRUD, purchases CRUD, home aggregate, prefs, basic stats |
| **P1** | Diary, wishlist, tax-free summary, alerts, media upload |
| **P2** | Map/route, export PDF/CSV, OCR assist, push |
| **P2.1** | TabBar + Settings, forgot password, privacy/about/support, deep-link parse, upload resume hook |
| **P3** | Shared trips (real accounts), FX live refresh, Premium, native maps SDK, force-update + flags |

---

## 8. What breaks at 10×

| Failure | Signal | Mitigation |
|---|---|---|
| Home N+1 (trips+purchases) | p95 `/home` latency | SQL views / denormalized trip counters |
| Photo storage cost | S3 bill | Resize client-side; lifecycle to Glacier |
| Outbox storm after offline weekend | sync worker backlog | Batch + backoff; cap queue size with user warning |
| FX provider down | trip create fails | Cached ECB/CBR rates + manual override |
| Export blocking request thread | timeouts | Always async job + pre-signed download |
| JWT theft | account takeover | Short access TTL, refresh rotation, device revoke |
| Missing force-update | broken clients after API break | Block below `minBuild` from `/me/app-config` |

---

## 9. Repo wiring (this project)

| Path | Role |
|---|---|
| `shared/.../data/remote/dto/**` | Kotlin DTO mirrors (client contract) |
| `docs/api/spring/**` | Java records for Spring Boot 4 (copy into backend) |
| `docs/api/API_CONVENTIONS.md` | Wire format, versioning, errors |
| `.mila-design/` | Visual/UX source of truth (local, not product code) |
| `shared/.../ui/navigation/MainShellVoyageScreen` | TabBar shell (Home / Wishlist / Profile) |
| `shared/.../navigation/VoyageDeepLinkParser` | Push / universal-link parse |

When Spring project is ready: copy `docs/api/spring/com/shoptourr/...` → backend `src/main/java`, keep field names in sync with `shared/.../data/remote/dto`.
