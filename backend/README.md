# Voyage API (Spring Boot 4)

Standalone backend for ShopTourr / Voyage. Not part of the Android/KMP Gradle root — run from this folder.

## Run

```bash
cd backend
../../.gradle/wrapper/...   # or:
gradle test
gradle bootRun
```

Using the cached Gradle from the monorepo host:

```bash
export JAVA_HOME=…/jbr-21…
/path/to/gradle-9.1.0/bin/gradle -p backend test
/path/to/gradle-9.1.0/bin/gradle -p backend bootRun
```

Base URL: `http://localhost:8080/api`  
Point the client with `AppConfig(apiBaseUrl = "http://10.0.2.2:8080/api")` (Android emulator).

## Current stub surface

| Area | Endpoints |
|---|---|
| Auth | `POST /auth/register\|login\|refresh\|logout` (in-memory) |
| Media + OCR | `POST /media/upload-intents`, `POST /media/{id}/confirm`, `GET /media/{id}`, `GET /media/{id}/ocr` |
| Tracing | echoes `X-Request-Id` |

DTOs live under `com.shoptourr.api.v1.dto.*` (copied from `docs/api/spring`).
