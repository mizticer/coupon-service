# Coupon Service

REST API do zarządzania kuponami rabatowymi z obsługą geolokalizacji, limitów użyć i ochroną przed wielowątkowym dostępem.

## Stack technologiczny

- **Java 25**, **Spring Boot 3.5.7**
- **PostgreSQL 17** – persystencja danych
- **Redis 7** – atomowe countery i cache (ochrona przed race conditions)
- **Liquibase** – migracje bazodanowe
- **OpenFeign** – klient HTTP do geolokalizacji IP (ip-api.com)
- **SpringDoc OpenAPI** – dokumentacja API (Swagger UI)
- **Gradle Kotlin DSL** – budowanie projektu
- **Docker Compose** – infrastruktura lokalna (PostgreSQL + Redis)

## Architektura

```
pl.couponservice/
├── config/                  # CouponCacheWarmup (odbudowa Redis z DB przy starcie)
├── controller/              # CouponApi (interfejs + Swagger) + CouponController
├── service/                 # CouponService, CouponRedemptionHandler, CouponRedisService
├── client/                  # IpApiClient (Feign), IpApiResponse
├── model/
│   ├── command/             # CreateCouponCommand, RedeemCouponCommand (rekordy z walidacją)
│   ├── dto/                 # CouponResponse, RedeemCouponResponse
│   └── mapper/              # CouponMapper
├── repository/              # CouponRepository, CouponUsageRepository
└── exception/
    ├── handler/             # GlobalExceptionHandler (@RestControllerAdvice)
    ├── constraint/          # ConstraintErrorMapperStrategy (mapowanie DB constraints)
    └── model/               # ExceptionDto, ValidationErrorDto
```

## Endpointy API

| Metoda | Endpoint | Opis | Kody odpowiedzi |
|--------|----------|------|-----------------|
| POST | `/api/v1/coupons` | Tworzenie kuponu | 201, 400, 409 |
| POST | `/api/v1/coupons/redemptions` | Użycie kuponu | 200, 403, 404, 409 |

### Tworzenie kuponu

**Request:**
```json
{
  "code": "WIOSNA2025",
  "maxUsages": 100,
  "countryCode": "PL"
}
```

**Response (201):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "code": "WIOSNA2025",
  "maxUsages": 100,
  "currentUsages": 0,
  "countryCode": "PL",
  "createdAt": "2026-03-02T10:15:30Z"
}
```

### Użycie kuponu

**Request:**
```json
{
  "code": "WIOSNA2025",
  "userId": "user-123"
}
```

**Response (200):**
```json
{
  "couponCode": "WIOSNA2025",
  "userId": "user-123",
  "redeemedAt": "2026-03-02T10:16:45Z",
  "remainingUsages": 99
}
```

### Kody błędów

| Kod | Sytuacja |
|-----|----------|
| 400 | Błąd walidacji danych wejściowych |
| 403 | Użycie kuponu z niedozwolonego kraju |
| 404 | Kupon o podanym kodzie nie istnieje |
| 409 | Kupon wyczerpany / duplikat kodu / user już użył kuponu |
| 503 | Błąd geolokalizacji (niedostępność ip-api.com) |

## Obsługa wielowątkowości i skalowalność

Serwis został zaprojektowany z myślą o działaniu w środowisku wielowątkowym i wieloinstancyjnym (np. Kubernetes). Strategia obrony opiera się na wielu warstwach:

### Redis jako szybki filtr (sub-millisecond)

- **DECR** – atomowa dekrementacja countera użyć.
- **SADD** – atomowe sprawdzenie i dodanie użytkownika do setu. Check-and-add w jednej operacji.
- **setIfAbsent** – inicjalizacja countera bez ryzyka nadpisania aktywnego stanu.

### PostgreSQL jako source of truth

- **Atomic UPDATE** – `WHERE current_usages < max_usages` gwarantuje, że baza nigdy nie przekroczy limitu.
- **UNIQUE constraint** – `(coupon_id, user_id)` jako ostatnia linia obrony przed duplikatami.
- **CHECK constraints** – `max_usages > 0`, `current_usages <= max_usages`.

### Spójność Redis ↔ DB

- **TransactionSynchronization** – rollback Redis po nieudanym COMMIT bazodanowym (afterCompletion).
- **CouponCacheWarmup** – odbudowa Redis z bazy przy starcie aplikacji (ApplicationRunner).
- **Fallback** – jeśli klucz Redis nie istnieje, serwis ładuje dane z DB i inicjalizuje cache.

### Separacja odpowiedzialności transakcyjnych

- **GeoLocationService** wywoływany POZA `@Transactional` – nie blokuje DB connection pool na czas HTTP call.
- **CouponRedemptionHandler** jako osobny bean – unikamy problemu self-invocation w Spring AOP proxy.

### Flow użycia kuponu

```
1. GeoCheck (poza transakcją)     → HTTP call do ip-api.com
2. Redis: couponExists             → fallback z DB jeśli brak
3. Redis: getCountryCode           → walidacja kraju (zero DB query)
4. Redis: DECR                     → atomowy check limitu
5. Redis: SADD                     → atomowy check duplikatu usera
6. Register TransactionSync        → rollback Redis przy DB failure
7. DB: incrementUsage              → atomic UPDATE z WHERE clause
8. DB: save CouponUsage            → UNIQUE constraint jako backup
9. DB: getRemainingUsages           → świeży stan po UPDATE
```

## Uruchomienie

### Wymagania

- Java 25+
- Docker i Docker Compose

### Kroki

1. Uruchom infrastrukturę:
```bash
docker compose up -d
```

2. Zbuduj i uruchom aplikację:
```bash
./gradlew bootRun
```

3. Swagger UI dostępny pod adresem:
```
http://localhost:8080/swagger-ui.html
```

## Konfiguracja

Główna konfiguracja w `src/main/resources/application.yml`:

| Parametr | Wartość domyślna | Opis |
|----------|-----------------|------|
| `server.port` | 8080 | Port aplikacji |
| `spring.datasource.url` | jdbc:postgresql://localhost:5432/coupon_db | URL bazy danych |
| `spring.data.redis.host` | localhost | Host Redis |
| `spring.data.redis.port` | 6379 | Port Redis |
| `geolocation.ip-api.base-url` | http://ip-api.com | URL serwisu geolokalizacji |

## Geolokalizacja

Serwis wykorzystuje darmowe API [ip-api.com](http://ip-api.com) do rozpoznawania kraju na podstawie adresu IP. Darmowy plan pozwala na 45 zapytań na minutę.

Przy testowaniu przez Swagger UI (localhost), należy użyć parametru `ip` do nadania publicznego adresu IP, np. `217.119.79.XXX` (Polska).

## Baza danych

Migracje zarządzane przez Liquibase (`src/main/resources/db/changelog/changelog-master.xml`).

### Tabela `coupon`

| Kolumna | Typ | Opis |
|---------|-----|------|
| id | UUID | Klucz główny |
| code | VARCHAR(50) | Kod kuponu (UNIQUE na UPPER(code)) |
| created_at | TIMESTAMPTZ | Data utworzenia |
| max_usages | INT | Maksymalna liczba użyć |
| current_usages | INT | Bieżąca liczba użyć |
| country_code | VARCHAR(2) | Kod kraju ISO 3166-1 alpha-2 |

### Tabela `coupon_usage`

| Kolumna | Typ | Opis |
|---------|-----|------|
| id | UUID | Klucz główny |
| coupon_id | UUID | FK → coupon |
| user_id | VARCHAR(255) | Identyfikator użytkownika |
| ip_address | VARCHAR(45) | Adres IP użytkownika |
| used_at | TIMESTAMPTZ | Data użycia |

## Decyzje projektowe

### Dlaczego Redis + Atomic UPDATE zamiast Optimistic/Pessimistic Locking?

**Optimistic Lock (@Version)** generuje dużo retry przy popularnych kuponach (np. Black Friday) – każdy konflikt wymaga powtórzenia całej operacji.

**Pessimistic Lock (SELECT FOR UPDATE)** tworzy kolejkę na poziomie bazy danych, ograniczając throughput.

**Redis DECR + DB Atomic UPDATE** daje najlepszy stosunek wydajności do spójności:
- Redis odrzuca 90%+ niepoprawnych requestów w sub-millisecond (wyczerpane kupony, duplikaty userów, zły kraj)
- DB atomic UPDATE gwarantuje spójność danych
- Brak retry, brak kolejek, brak locków
- Skalowalność horyzontalna – wiele instancji aplikacji, jeden Redis
