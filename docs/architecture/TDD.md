# TDD — обязательный режим разработки (ShopTourr / Voyage)

Этот проект ведётся в цикле **Red → Green → Refactor**.

## Правила

1. **Сначала тест** в `commonTest` (или platform test), который падает по делу.
2. **Минимальный код**, чтобы тест стал зелёным.
3. **Рефакторинг** без смены поведения; тесты остаются зелёными.
4. Domain / use-cases / ViewModels / калькуляторы (VAT, Money, Outbox) — только через TDD.
5. UI Compose — smoke/preview допускается без полного TDD; логика UI уходит в ViewModel и тестируется.

## Где живут тесты

| Слой | Где | Инструменты |
|---|---|---|
| Domain, use-cases | `shared/src/commonTest` | `kotlin.test`, `runTest` |
| Flow / ViewModel | `commonTest` | Turbine (Context7 `/cashapp/turbine`) |
| HTTP | `commonTest` | Ktor `MockEngine` |
| Settings | `commonTest` | `MapSettings` (multiplatform-settings) |
| SQLDelight | `androidHostTest` / `iosTest` | in-memory driver |

## Именование

- `*Test.kt` — unit
- `*IntegrationTest.kt` — несколько слоёв (repo + local + fake api)

## Команды

```bash
./gradlew :shared:testAndroidHostTest       # Android host + commonTest
./gradlew :shared:iosSimulatorArm64Test     # iOS sim + commonTest
./gradlew :shared:allTests                  # Aggregate
```

Не мержить фичу без зелёных тестов на затронутый domain/data код.
