# Alarm Walutowy

Alarm Walutowy to kompletny, dwuserwisowy system monitorowania kursów walut.  
Mikroserwis **DataGatherer** cyklicznie pobiera dane z Open Exchange Rates, 
wykrywa istotne zmiany i publikuje komunikaty do RabbitMQ.  
Mikroserwis **DataProvider** konsumuje te komunikaty, utrwala historię kursów, 
zapewnia uwierzytelniane REST API do zarządzania subskrypcjami oraz wysyła 
powiadomienia e-mail, gdy próg zmiany kursu zostanie przekroczony.

---

## Architektura

```
┌───────────────┐      ┌─────────────────┐      ┌────────────────────┐
│  OpenExchange │ ---> │  DataGatherer   │ ---> │     RabbitMQ       │
│    Rates API  │      │  (Scheduler)    │      │ (Exchange & Queue) │
└───────────────┘      └─────────────────┘      └─────────┬──────────┘
                                                           │
                                                   ┌───────▼────────┐
                                                   │  DataProvider  │
                                                   │ (REST + JPA)   │
                                                   └────────────────┘
```

Każdy mikroserwis korzysta z własnej bazy PostgreSQL zarządzanej przez Flyway.
Komunikacja między serwisami odbywa się asynchronicznie poprzez RabbitMQ, a
całość można uruchomić lokalnie lub w Dockerze.

---

## Kluczowe funkcjonalności

### DataGatherer
- Harmonogram CRON pobierający kursy co godzinę (konfigurowalne).
- Integracja z Open Exchange Rates przez dedykowanego klienta HTTP.
- `CurrencyChangeDetector` analizujący zmiany względem poprzedniego snapshotu.
- Publikacja zdarzeń `CurrencyRateChangeMessage` do RabbitMQ.
- Flyway `V1__initial_schema.sql` utrzymujący tabelę snapshotów w bazie modułu.

### DataProvider
- Konsumowanie zdarzeń z RabbitMQ i zapisywanie historii kursów.
- Rejestracja, logowanie i autoryzacja użytkowników z JWT.
- CRUD subskrypcji z walidacją progów i filtrowaniem po walucie.
- Powiadomienia e-mail o przekroczonych progach z wykorzystaniem szablonu HTML.
- Dokumentacja OpenAPI generowana przez SpringDoc (Swagger UI).
- Rozbudowany zestaw testów jednostkowych i integracyjnych (MockMvc, GreenMail).

---

## Technologie

- Java 17, Spring Boot 3, Spring Data JPA, Spring Security, Spring Mail.
- RabbitMQ, PostgreSQL, Flyway, Testcontainers.
- Maven, Docker, Docker Compose.

---

## Struktura repozytorium

| Katalog | Zawartość |
| --- | --- |
| `common/` | Wspólne kontrakty (np. `CurrencyRateChangeMessage`). |
| `data-gatherer/` | Kod mikroserwisu zbierającego dane, konfiguracja i testy. |
| `data-provider/` | REST API, logika domenowa, notyfikacje oraz testy. |
| `integration/` | Testy end-to-end z wykorzystaniem Testcontainers. |
| `docker-compose.yml` | Definicja środowiska (RabbitMQ, bazy, MailHog, serwisy). |

---

## Wymagania

- JDK 17+
- Docker & Docker Compose (zalecane)
- Konto w [Open Exchange Rates](https://openexchangerates.org/) i klucz API
- Dostęp do serwera SMTP lub MailHog (domyślne ustawienia w `docker-compose.yml`)

---

## Szybki start w Dockerze

```bash
# zdefiniuj klucz OXR
export OXR_APP_ID=9cd7eea4846d4cb196e8b84c2a553f15

# zbuduj i uruchom kompletne środowisko
docker compose up --build
```

Serwisy są dostępne pod adresami:
- DataProvider API – <http://localhost:8080>
- Dokumentacja Swagger – <http://localhost:8080/swagger-ui.html>
- RabbitMQ UI – <http://localhost:15672> (guest/guest)
- MailHog UI – <http://localhost:8025>

---

## Uruchomienie lokalne (bez Dockera)

1. Uruchom zależności (PostgreSQL, RabbitMQ, SMTP) – możesz użyć `docker compose up -d dataprovider-db datagatherer-db rabbitmq mailhog`.
2. Wykonaj migracje i testy:
   ```bash
   ./mvnw -q test
   ```
3. Start DataGatherer:
   ```bash
   ./mvnw -pl data-gatherer spring-boot:run
   ```
4. Start DataProvider:
   ```bash
   ./mvnw -pl data-provider spring-boot:run
   ```

---

## Konfiguracja

Najważniejsze właściwości można nadpisać zmiennymi środowiskowymi:

| Zmienna | Opis | Domyślna wartość |
| --- | --- | --- |
| `OXR_APP_ID` | Klucz Open Exchange Rates | `9cd7eea4846d4cb196e8b84c2a553f15` |
| `DATAGATHERER_DB_URL` / `...USERNAME` / `...PASSWORD` | Połączenie z bazą DataGatherer | `jdbc:postgresql://datagatherer-db:5432/datagatherer`, `datagatherer/datapass` |
| `DATAPROVIDER_DB_URL` / `...USERNAME` / `...PASSWORD` | Połączenie z bazą DataProvider | `jdbc:postgresql://dataprovider-db:5432/alarmdb`, `alarmuser/alarmpass` |
| `spring.rabbitmq.host` / `port` / `username` / `password` | Połączenie z RabbitMQ | `rabbitmq`, `5672`, `guest/guest` |
| `spring.mail.host` / `spring.mail.port` | Serwer SMTP | `mailhog`, `1025` |
| `app.jwt.secret` / `app.jwt.expiration` | Parametry tokenów JWT | wartości demonstracyjne |

Pełne listy właściwości znajdują się w plikach `application.yml` poszczególnych modułów.

---

### Założenia dotyczące waluty bazowej

System zakłada istnienie jednej globalnej waluty bazowej konfigurowanej przez
`app.exchange-rate.base-currency` (domyślnie `USD`). Wszystkie snapshoty w
`currency_rates` oraz progi subskrypcji odnoszą się do tej samej wartości.
Zmiana waluty bazowej w działającym środowisku wymaga wyczyszczenia dotychczas
zebranych danych (lub ponownego postawienia całego setupu), aby uniknąć
interpretowania historycznych kursów względem niewłaściwej waluty.

---

## REST API

### Uwierzytelnianie
| Metoda | Endpoint | Opis |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Rejestracja użytkownika (email + hasło). |
| `POST` | `/api/auth/login` | Logowanie i pobranie tokenu JWT. |

### Subskrypcje (wymaga JWT)
| Metoda | Endpoint | Opis |
| --- | --- | --- |
| `GET` | `/api/subscriptions` | Lista subskrypcji zalogowanego użytkownika. |
| `POST` | `/api/subscriptions` | Dodanie subskrypcji z progiem procentowym. |
| `PUT` | `/api/subscriptions/{id}` | Aktualizacja progu / aktywności. |
| `DELETE` | `/api/subscriptions/{id}` | Usunięcie subskrypcji. |

### Kursy walut (wymaga JWT)
| Metoda | Endpoint | Parametry | Opis |
| --- | --- | --- | --- |
| `GET` | `/api/rates/latest` | `currency` (opcjonalnie) | Ostatni kurs każdej waluty lub wskazanej. |
| `GET` | `/api/rates/history` | `currency`, `from`, `to`, paginacja | Historia kursów. |

### Dokumentacja
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

W Swagger UI użyj przycisku **Authorize** i wklej token w formacie `Bearer <token>`.

### Pliki HTTP do szybkiego testowania
- W katalogu `http/` znajduje się plik `data-provider.http` z kompletnymi przykładami zapytań HTTP
  kompatybilnymi z klientem wbudowanym w IDE JetBrains oraz rozszerzeniem REST Client dla VS Code.
- Plik definiuje zmienne środowiskowe (`@baseUrl`, `@userEmail`, `@userPassword`) oraz sekwencję żądań
  obsługujących rejestrację, logowanie, CRUD subskrypcji i odczyt kursów.
- Po uruchomieniu żądania `register` lub `login` token JWT jest automatycznie zapisywany do zmiennej
  `authToken`, dzięki czemu kolejne zapytania dodają nagłówek `Authorization` bez dodatkowych działań.
- Przed uruchomieniem żądań upewnij się, że serwis DataProvider działa pod adresem zdefiniowanym
  w `@baseUrl` (domyślnie `http://localhost:8080`). W razie potrzeby zmodyfikuj wartości zmiennych
  na początku pliku.
  
---

## Powiadomienia e-mail

`NotificationService` wyszukuje subskrypcje, dla których zmiana kursu przekroczyła
zadeklarowany próg, przygotowuje treść wiadomości (Thymeleaf) i wysyła ją przez
`JavaMailSender`. W środowisku Docker wiadomości trafiają do MailHog – możesz je
podejrzeć w przeglądarce pod adresem <http://localhost:8025>.

---

## Migracje baz danych

- DataGatherer: `src/main/resources/db/migration/datagatherer/V1__initial_schema.sql`
- DataProvider: `src/main/resources/db/migration/dataprovider/V1__initial_schema.sql` oraz `V2__ensure_domain_tables.sql`

Migracje uruchamiają się automatycznie przy starcie aplikacji. Możesz też wywołać je ręcznie:

```bash
./mvnw -pl data-gatherer flyway:migrate
./mvnw -pl data-provider flyway:migrate
```

---

## Testy

| Komenda | Zakres |
| --- | --- |
| `./mvnw -q test` | Testy jednostkowe wszystkich modułów. |
| `./mvnw -q -Dit verify -pl integration -am` | Integracyjne uruchomienie pipeline'u z Testcontainers. |

Testy w `integration/` inicjują RabbitMQ i PostgreSQL w kontenerach, uruchamiają oba serwisy i sprawdzają przepływ wiadomości od 
pozyskania kursu po zapis w bazie oraz wysyłkę notyfikacji.

---

## Dalsze kroki

Projekt jest jako zakończony system.  
Możliwe usprawnienia (opcjonalnie):
- panel administracyjny dla monitoringu subskrypcji,
- eksport danych historycznych do CSV,
- metryki Prometheus/Grafana.

---

## Licencja

Projekt dydaktyczny – brak formalnej licencji.
