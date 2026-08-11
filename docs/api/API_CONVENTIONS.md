# API conventions — Voyage / ShopTourr

## Base

- Base path: `/api`
- Version: Spring Boot 4 mapping `version = "1"` (also accept header `API-Version: 1`)
- Content-Type: `application/json; charset=utf-8`
- Auth: `Authorization: Bearer <accessToken>`
- Time: ISO-8601 UTC (`2026-04-12T10:24:00Z`)
- Dates (calendar): `YYYY-MM-DD`
- IDs: UUID string
- Money amounts: JSON **string** decimal (`"96.50"`) — never float
- Currency: ISO-4217 (`EUR`, `JPY`, `NOK`, `RUB`)
- Null: omit optional fields on write; on read nullable fields may be `null`
- Pagination: `?page=0&size=20&sort=createdAt,desc` → `PageResponse`

## Headers (client → server)

| Header | Required | Notes |
|---|---|---|
| `Authorization` | except auth | Bearer JWT |
| `Idempotency-Key` | POST create | UUID; 24h retention |
| `X-Request-Id` | recommended | UUID; echoed |
| `Accept-Language` | optional | `ru` / `en` (errors/localized labels) |

## Problem Details (error)

`Content-Type: application/problem+json`

```json
{
  "type": "https://api.shoptourr.com/problems/validation",
  "title": "Validation failed",
  "status": 400,
  "detail": "amount must be positive",
  "instance": "/api/trips/…/purchases",
  "code": "VALIDATION_ERROR",
  "errors": [
    { "field": "amount", "code": "POSITIVE", "message": "must be > 0" }
  ],
  "requestId": "…"
}
```

### Stable `code` values

`VALIDATION_ERROR`, `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `CONFLICT`, `IDEMPOTENCY_CONFLICT`, `BUDGET_RULE`, `TAXFREE_RULE`, `RATE_LIMITED`, `MEDIA_NOT_READY`, `INTERNAL`

## Idempotency

- Same key + same user + same route → return original response (200/201)
- Same key + different body hash → `409 IDEMPOTENCY_CONFLICT`

## Soft delete

Trips/purchases/wishlist: `deletedAt` set; list endpoints exclude deleted unless `?includeDeleted=true` (admin/debug only).
