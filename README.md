<div align="center">

# KuPreu API

**Supermarket price-comparison backend — find the cheapest store for your whole shopping list.**

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)
![JWT](https://img.shields.io/badge/Auth-JWT-000000?logo=jsonwebtokens&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue)

</div>

KuPreu is a REST API that tracks supermarket product prices across chains and stores, lets users build shopping lists, and computes which store is cheapest to buy the entire list. Built with Spring Boot 4, JPA/Hibernate, MySQL, and stateless JWT authentication with role-based access (`USER` / `ADMIN`). Auth endpoints are protected by a Redis-backed token-bucket rate limiter, and request DTOs are bean-validated.

## Features

- 🛒 **Cheapest-store calculator** — for a shopping list, aggregates current prices per store and ranks them cheapest-first, handling items unavailable at a given store
- 💶 **Price snapshots** — historical price tracking per product/store using a composite key (`PriceSnapshotId`)
- 🔍 **Dynamic product search** — filter by category, subcategory, brand, and free-text with pagination
- 🔐 **JWT auth + RBAC** — stateless tokens, BCrypt-hashed passwords, `@PreAuthorize` admin guards
- 🚦 **Rate limiting** — Redis-backed bucket4j token bucket on `/api/auth/**`, returns `429`; capacity/refill tunable via `rate.limit.*` (default 20 req/min per IP)
- ✅ **DTO validation** — `spring-boot-starter-validation` on request records, with a `GlobalExceptionHandler`
- 🌐 **Configurable CORS** — allowed origins driven by `CORS_ALLOWED_ORIGINS`
- 📚 **OpenAPI / Swagger UI** — interactive docs out of the box via springdoc
- 🐳 **Docker Compose** — one-command full stack (API + MySQL + Redis)

## Tech stack

- **Language:** Java 21
- **Framework:** Spring Boot 4.0 (Web MVC, Data JPA, Security, Validation)
- **Database:** MySQL 8.0 (H2 for tests)
- **Cache / rate limiting:** Redis 7 + bucket4j (Lettuce client)
- **Auth:** jjwt 0.12 (HS256)
- **Docs:** springdoc-openapi 3.0
- **Tooling:** Lombok, spring-dotenv, Maven

## Prerequisites

- JDK 21+
- Maven (or use the bundled `./mvnw` wrapper)
- Docker + Docker Compose (for the MySQL + Redis containers)

## Installation

The Compose stack builds the API and provisions MySQL and Redis together.

```bash
# 1. Clone
git clone https://github.com/ratoncaton/KuPreu.git
cd KuPreu/api

# 2. Configure env (JWT_SECRET is required)
cp .env.example .env
# edit .env -> generate a secret with: openssl rand -base64 64

# 3. Build + run the full stack (API + MySQL + Redis)
docker compose up -d --build
```

Or run the API on the host against the Compose-provided MySQL + Redis:

```bash
docker compose up -d mysql redis
export JWT_SECRET="$(openssl rand -base64 64)"
./mvnw spring-boot:run
```

API boots on `http://localhost:8080`. Hibernate auto-creates the schema (`ddl-auto=update`).

**Verify:**

```bash
curl http://localhost:8080/api/categories
# Swagger UI: http://localhost:8080/swagger-ui/index.html
```

## Quick start

Register, then call a protected endpoint with the returned token.

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"jane","name":"Jane","surname":"Doe","email":"jane@mail.com","password":"secret"}'

# Login -> { "token": "..." }
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@mail.com","password":"secret"}' | jq -r .token)

# Authenticated call
curl http://localhost:8080/api/profile/me \
  -H "Authorization: Bearer $TOKEN"
```

## Configuration

Copy `.env.example` to `.env` (Compose reads it automatically), or set the variables in your environment.

| Variable | Default | Description |
|---|---|---|
| `JWT_SECRET` | _(required)_ | Base64 HMAC key for signing tokens |
| `JWT_EXPIRATION` | `86400000` | Token lifetime in ms (24h) |
| `MYSQL_DATABASE` | `kupreu` | MySQL database name |
| `MYSQL_USER` | `kupreu` | DB user |
| `MYSQL_PASSWORD` | `kupreu` | DB password |
| `MYSQL_ROOT_PASSWORD` | `rootpassword` | MySQL root password |
| `REDIS_PASSWORD` | _(empty)_ | Redis password (empty for dev) |
| `RATE_LIMIT_CAPACITY` | `20` | Token-bucket capacity for `/api/auth/**` |
| `RATE_LIMIT_REFILL_TOKEN` | `20` | Tokens refilled per period |
| `RATE_LIMIT_REFILL_PERIOD` | `PT1M` | Refill period (ISO-8601 duration) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Comma-separated allowed frontend origins |

> Compose provisions database `kupreu` with matching `kupreu`/`kupreu` credentials and a Redis 7 instance on `6379`.

## API overview

Auth is via `Authorization: Bearer <token>`. `[ADMIN]` requires `isAdmin = true`.

**Implemented**

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | public | Create account, returns JWT |
| POST | `/api/auth/login` | public | Authenticate, returns JWT |
| GET | `/api/profile/me` | auth | Current user's profile |
| PUT | `/api/profile/update/password` | auth | Change password |
| GET | `/api/categories` | public | List categories |
| POST/PUT/DELETE | `/api/categories/**` | admin | Manage categories |
| GET | `/api/users` | admin | Paginated user list |
| PUT | `/api/users/{id}/role` | admin | Grant/revoke admin |
| DELETE | `/api/users/{id}` | admin | Delete user |

<details>
<summary><strong>Planned / domain endpoints</strong> (see <code>ENDPOINTS.txt</code>)</summary>

- `/api/products`, `/api/products/{id}/prices`, `.../prices/cheapest`, `.../prices/history`
- `/api/prices` (admin price snapshots)
- `/api/subcategories`, `/api/brands`, `/api/units`, `/api/attributes`
- `/api/chains`, `/api/stores`, `/api/stores/{id}/products`
- `/api/shopping-lists` + items, and the flagship `GET /api/shopping-lists/{id}/cheapest-store`

</details>

## Data model

Core entities: `User`, `Product`, `Category` / `Subcategory`, `Brand`, `UnitOfMeasure`, `Attribute`, `SupermarketChain`, `Store`, `PostalCode`, `PriceSnapshot` (+ `DateDIM`), `ShoppingList` / `ShoppingListItem`.

See the full ERD in [`kupreu_erd.png`](kupreu_erd.png).

## Project structure

```
api/
├── src/main/java/com/kupreu/api/
│   ├── ApiApplication.java
│   ├── config/               # RedisConfig, RateLimitProperties
│   │   └── security/         # JWT filter/provider, RateLimitFilter, SecurityConfig
│   ├── controller/           # REST endpoints + GlobalExceptionHandler
│   ├── service/              # business logic
│   ├── repository/           # Spring Data JPA
│   ├── entity/               # JPA entities
│   └── DTOs/                 # request/response records
├── src/main/resources/       # application.properties
├── Dockerfile                # API image
├── docker-compose.yml        # API + MySQL 8.0 + Redis 7
├── .env.example
└── pom.xml
```

## Testing

```bash
./mvnw test
```

92 tests across three layers:

- **`@WebMvcTest` controller slices** — auth, categories, profile, users; cover RBAC (anonymous / `USER` / `ADMIN`), bean validation (`400`), and error mapping via `GlobalExceptionHandler` (`RateLimitFilter` excluded from the slice).
- **Mockito service unit tests** — `AuthService`, `CategoryService`, `ProfileService`, `UserService`, `UserDetailsServiceImpl`; every happy path and error branch (not-found, duplicates, missing user).
- **`JwtProvider` tests** — token round-trip, expiry, tampered/garbage signatures.

Controller slices run against in-memory H2 with Spring Security test support; service and JWT tests are pure unit tests.

## Contributing

1. Fork the repo
2. Create a branch: `git checkout -b feature/my-change`
3. Commit and open a PR against `main`

## License

MIT — see [LICENSE](LICENSE).

<div align="center">

Built by [@ratoncaton](https://github.com/ratoncaton) with Spring Boot ☕

</div>
