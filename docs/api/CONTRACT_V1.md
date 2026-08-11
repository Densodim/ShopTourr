# API Contract v1 — FINAL for backend bootstrap

**Status:** frozen for Spring Boot 4 implementation.  
**Client mirror:** `shared/.../data/remote/dto/**`  
**Java records:** `docs/api/spring/com/shoptourr/api/v1/dto/**`

Do not rename fields without a mobile release. Additive optional fields are OK.

## Conventions

See [API_CONVENTIONS.md](./API_CONVENTIONS.md):

- Base `/api`, version `1`
- Money amounts as **decimal strings** (`"96.50"`), never float/double on the wire
- Problem Details (`application/problem+json`) with stable `code`
- `Idempotency-Key` on creating POSTs
- Dates `YYYY-MM-DD`, instants ISO-8601 UTC

## Endpoint ↔ DTO map

| Area | Endpoints doc section | Java package | Kotlin package |
|---|---|---|---|
| Auth | Auth | `…dto.auth` | `data.remote.dto.auth` |
| Me / Premium | Me + P3 | `…dto.user` | `data.remote.dto.user` |
| Home | Home | `…dto.home` | `data.remote.dto.home` |
| Trips / travelers / invite / FX | Trips + P3 | `…dto.trip` | `data.remote.dto.trip` |
| Purchases | Purchases | `…dto.purchase` | `data.remote.dto.purchase` |
| Diary | Diary | `…dto.diary` | `data.remote.dto.diary` |
| Wishlist | Wishlist | `…dto.wishlist` | `data.remote.dto.wishlist` |
| Stats | Stats | `…dto.stats` | `data.remote.dto.stats` |
| Alerts | Alerts | `…dto.alert` | `data.remote.dto.alert` |
| Tax Free | Tax Free | `…dto.taxfree` | `data.remote.dto.taxfree` |
| Map / route | Map | `…dto.map` | `data.remote.dto.map` |
| Export jobs | Export | `…dto.export` | `data.remote.dto.export` |
| Media / OCR | Media | `…dto.media` | `data.remote.dto.media` |
| Push devices | Push | `…dto.push` | `data.remote.dto.push` |
| Common (Money, Problem, Page) | — | `…dto.common` | `data.remote.dto.common` |

Full method/path table: [ENDPOINTS.md](./ENDPOINTS.md).

## Copy into Spring Boot

```text
docs/api/spring/com/shoptourr/api/v1/dto/
  → src/main/java/com/shoptourr/api/v1/dto/
```

Also copy `JacksonMoneyConfig.java.example` (BigDecimal → JSON string).

## Out of scope for v1 backend (client stubs OK)

- Real FCM/APNs delivery (device register endpoints are in contract)
- Live map tiles (route DTO only)
- OCR provider choice (response shape fixed)

## Change process

1. Propose field change in this repo (`docs/api` + Kotlin DTO + Java record).
2. Bump only if breaking → `API-Version: 2` later; v1 stays compatible.
