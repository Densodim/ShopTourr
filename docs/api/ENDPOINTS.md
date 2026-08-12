# Endpoint catalog (v1)

All under `/api`, Spring Boot 4 `version = "1"`.

## Auth
| Method | Path | Request | Response |
|---|---|---|---|
| POST | `/auth/register` | `RegisterRequest` | `AuthTokensResponse` 201 |
| POST | `/auth/login` | `LoginRequest` | `AuthTokensResponse` 200 |
| POST | `/auth/forgot-password` | `ForgotPasswordRequest` | 204 |
| POST | `/auth/refresh` | `RefreshTokenRequest` | `AuthTokensResponse` 200 |
| POST | `/auth/logout` | `LogoutRequest` | 204 |

## Me
| Method | Path | Request | Response |
|---|---|---|---|
| GET | `/me` | — | `UserDto` |
| PATCH | `/me` | `UpdateProfileRequest` | `UserDto` |
| GET | `/me/preferences` | — | `UserPreferencesDto` |
| PATCH | `/me/preferences` | `UpdatePreferencesRequest` | `UserPreferencesDto` |
| GET | `/me/app-config` | — | `ClientRemoteConfigDto` |

## Home
| Method | Path | Response |
|---|---|---|
| GET | `/home` | `HomeResponse` |

## Trips
| Method | Path | Request | Response |
|---|---|---|---|
| GET | `/trips` | — | `TripListResponse` |
| POST | `/trips` | `CreateTripRequest` | `TripDto` 201 |
| GET | `/trips/{tripId}` | — | `TripDto` |
| PATCH | `/trips/{tripId}` | `UpdateTripRequest` | `TripDto` |
| DELETE | `/trips/{tripId}` | — | 204 |

## Purchases
| Method | Path | Request | Response |
|---|---|---|---|
| GET | `/trips/{tripId}/purchases` | — | `TripPurchasesResponse` |
| POST | `/trips/{tripId}/purchases` | `CreatePurchaseRequest` | `PurchaseDto` 201 |
| GET | `/trips/{tripId}/purchases/{id}` | — | `PurchaseDto` |
| PATCH | `/trips/{tripId}/purchases/{id}` | `UpdatePurchaseRequest` | `PurchaseDto` |
| DELETE | `/trips/{tripId}/purchases/{id}` | — | 204 |

## Diary
| Method | Path | Request | Response |
|---|---|---|---|
| GET | `/trips/{tripId}/diary` | — | `TripDiaryResponse` |
| POST | `/trips/{tripId}/diary` | `CreateDiaryEntryRequest` | `DiaryEntryDto` 201 |
| PATCH | `/trips/{tripId}/diary/{id}` | `UpdateDiaryEntryRequest` | `DiaryEntryDto` |
| DELETE | `/trips/{tripId}/diary/{id}` | — | 204 |

## Stats / Alerts / Tax Free / Map / Export
| Method | Path | Request | Response |
|---|---|---|---|
| GET | `/trips/{tripId}/stats` | — | `TripStatsDto` |
| GET | `/trips/{tripId}/alerts` | — | `TripAlertsResponse` |
| GET | `/trips/{tripId}/tax-free` | — | `TaxFreeSummaryDto` |
| GET | `/trips/{tripId}/route` | — | `TripRouteDto` |
| POST | `/trips/{tripId}/exports` | `CreateExportRequest` | `ExportJobDto` 202 |
| GET | `/exports/{exportId}` | — | `ExportJobDto` |

## Wishlist
| Method | Path | Request | Response |
|---|---|---|---|
| GET | `/wishlist` | — | `WishlistResponse` |
| POST | `/wishlist` | `CreateWishlistItemRequest` | `WishlistItemDto` 201 |
| PATCH | `/wishlist/{id}` | `UpdateWishlistItemRequest` | `WishlistItemDto` |
| DELETE | `/wishlist/{id}` | — | 204 |

## Media
| Method | Path | Request | Response |
|---|---|---|---|
| POST | `/media/upload-intents` | `CreateMediaUploadIntentRequest` | `MediaUploadIntentResponse` 201 |
| POST | `/media/{mediaId}/confirm` | `ConfirmMediaUploadRequest` | `MediaAssetDto` |
| GET | `/media/{mediaId}` | — | `MediaAssetDto` |
| GET | `/media/{mediaId}/ocr` | — | `ReceiptOcrResultDto` (P2) |

## Shared trips / FX / Premium (P3)
| Method | Path | Request | Response |
|---|---|---|---|
| POST | `/trips/{tripId}/travelers` | `CreateTravelerRequest` | `TravelerDto` 201 |
| POST | `/trips/{tripId}/invites` | `InviteTravelerRequest` | `TripInviteDto` 201 |
| POST | `/trips/{tripId}/exchange-rate/refresh` | — | `ExchangeRateDto` 200 |
| POST | `/me/premium/activate` | `ActivatePremiumRequest` | `UserDto` 200 |

## Push devices (P2)
| Method | Path | Request | Response |
|---|---|---|---|
| POST | `/me/devices` | `RegisterDeviceRequest` | `DeviceDto` 201 |
| DELETE | `/me/devices/{deviceId}` | — | 204 |

## Copy into Spring Boot

```text
docs/api/spring/com/shoptourr/api/v1/dto/
  → src/main/java/com/shoptourr/api/v1/dto/
```

Add dependencies: `jakarta.validation-api` (via `spring-boot-starter-validation`), Jackson 3 (Boot 4 default).  
Configure `BigDecimal` as JSON string globally to match mobile contract.

**Contract freeze:** see [CONTRACT_V1.md](./CONTRACT_V1.md) — use this as the backend bootstrap checklist.
