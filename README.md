# Pokédex Desk

A full-stack exercise built on [PokeAPI](https://pokeapi.co/docs/v2): browse the catalog, open any Pokémon in detail, copy entries into a local PostgreSQL database and enrich them with proprietary fields (local name, region, internal tags, notes).

| Part | Stack |
|---|---|
| `auth-service` | Java 21, Spring Boot 3.5, Spring Security (JWT, HS256), JPA, Flyway |
| `pokemon-service` | Java 21, Spring Boot 3.5, RestClient, Caffeine cache, JPA, Flyway |
| `frontend` | Vue 3, Pinia, Vue Router, Vite, served by nginx |
| Infrastructure | PostgreSQL 17 (one database per service), Docker Compose |

---

## Quick start (Docker)

```bash
docker compose up --build
```

| URL | What |
|---|---|
| http://localhost:8080 | Web app |
| http://localhost:8081/swagger-ui.html | Auth service API (Swagger UI) |
| http://localhost:8082/swagger-ui.html | Pokemon service API (Swagger UI) |

**Demo credentials** (seeded on startup):

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN: everything, including deleting Pokémon and managing users |
| `ash` | `pikachu123` | USER: browse, add to the local Pokédex, edit local fields |

The local Pokédex is **pre-populated with 10 Pokémon** (Bulbasaur line, Charmander, Squirtle, Pikachu, Gengar, Eevee, Snorlax, Mewtwo), each with a Japanese name, region and tags already filled in.

To use your own JWT secret (recommended outside a demo), set it once for both services:

```bash
JWT_SECRET='a-long-random-string-of-at-least-32-characters' docker compose up --build
```

## Tests

```bash
cd auth-service && mvn verify        # same for pokemon-service
```

Following the template's conventions, `*Test` classes are unit tests (Surefire) and `*IntegrationTest` classes boot the full application on a random port and are run by Failsafe. `mvn test` runs the unit tests only.

| Level | What is covered | Tools |
|---|---|---|
| Domain | Invariants and normalisation: username/email/password rules, custom fields (tags, region), identifiers, pagination | JUnit 5, AssertJ |
| Application | Every use case with mocked ports: register/login, user administration rules, catalog, sync (create/refresh/batch), local update/delete | Mockito |
| Adapters | PokeAPI mapping against real-shaped JSON fixtures, 404/5xx translation, **caching** (second call never hits HTTP), JPA adapters on H2 with the real migrations, JWT issue/verify round trip | MockRestServiceServer, `@DataJpaTest` |
| HTTP | Status codes and payloads end-to-end: 200/201/204, 400 (bean, domain, malformed JSON, unknown fields, bad types), 401 (missing/expired/foreign token), 403, 404, 409, 502 | `@SpringBootTest`, REST Assured |
| Architecture | Dependencies only point inwards; domain and application layers are framework-free | ArchUnit |

The integration tests never call the real PokeAPI: the `PokemonCatalog` port is replaced with a mock.

---

## User stories

| Story | How it is delivered | Endpoint | Access |
|---|---|---|---|
| **US01 Enumeration** | Paginated catalog with sprite, category (species genus, e.g. "Seed Pokémon"), weight and abilities. Responses are cached. | `GET /catalog/pokemon?page=0&size=20` | Public |
| **US02 Detail** | Official artwork, base stats, Pokédex description and the full evolution chain (as a tree, so branching lines like Eevee's work). | `GET /catalog/pokemon/{idOrName}` | Public |
| **US03 Synchronisation** | Copies a Pokémon into the local database. Idempotent: syncing again refreshes PokeAPI data and **keeps** the proprietary fields. Batch sync for up to 50 Pokédex numbers. | `POST /pokemon/sync/{idOrName}` (201 created / 200 refreshed)<br>`POST /pokemon/sync?fromId=1&toId=50` | Signed in |
| **US04 Local modification** | Replaces the proprietary fields: `localizedName`, `region`, `tags`, `notes`. | `PUT /pokemon/{id}` | Signed in |

Other local Pokédex operations: `GET /pokemon?page=&size=&search=` (search by name or local name), `GET /pokemon/{id}`, `DELETE /pokemon/{id}` (ADMIN).


## Error format

Every error from both services, including 401/403 from Spring Security, uses [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) problem details:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/pokemon/25",
  "errors": { "tags": "Tag 'no spaces' is invalid: use 1-30 lowercase letters, digits or hyphens" }
}
```

| Status | Meaning |
|---|---|
| 400 | Invalid input (see above) |
| 401 | Missing, invalid or expired token; wrong credentials at login |
| 403 | Signed in but not allowed (e.g. a USER deleting a Pokémon) |
| 404 | Unknown Pokémon or user |
| 409 | Uniqueness or business-rule conflict |
| 502 | PokeAPI failed or timed out |

---

## Data model

**pokemon-service: `pokemon` table** (primary entity, PK = national Pokédex number, which makes sync idempotent)

| Group | Columns |
|---|---|
| From PokeAPI | `name` (unique), `category`, `description`, `height_m`, `weight_kg`, `types`, `abilities`, `hp`, `attack`, `defense`, `special_attack`, `special_defense`, `speed`, `sprite_url`, `image_url` |
| Proprietary | `localized_name`, `region`, `tags`, `notes` |
| Audit | `synced_at`, `updated_at` |

**auth-service: `users` table** (user management collection): `id` (PK), `username` (unique), `email` (unique), `password_hash` (BCrypt), `role`, `created_at`.

Schemas are managed with Flyway (`src/main/resources/db/migration`). The same migrations run on H2 and PostgreSQL. Demo users are created at startup by `DemoUserSeeder`, so no password hash is committed to the repository. Disable it with `app.seed.enabled=false`.


## Frontend

```
frontend/src
├── api/          http.js (fetch wrapper, problem-detail errors) + one module per resource
├── stores/       Pinia: auth (session), catalog (page/detail cache), pokedex (local CRUD), users
├── router/       Routes with meta.requiresAuth / requiresAdmin guards (mirror backend rules)
├── components/   PokemonTile, TypeChip, StatBars, EvolutionTree (recursive), TagInput, PagerControls, AppHeader
└── views/        Catalog, Detail, My Pokédex, Edit, Users, Login, Register, Not found
```

- **Views and data:** views call stores and stores call the API modules. The HTTP layer has no dependency on stores; the token source and the "session expired" reaction are injected in `main.js`.
- **Pagination:** the catalog page number lives in the URL, so refresh and the back button work.
- **Forms:** client-side validation mirrors the server rules for instant feedback, and server field errors are shown inline when they occur.
- **Responsive and accessible:** the layout works down to phone width. There is a skip link, keyboard focus is visible, form fields are labelled, and reduced-motion preferences are respected.

---
