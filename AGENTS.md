# AGENTS.md

## Repository Structure

Two-project monorepo: `jtech-tasklist-backend/` (Spring Boot) and `jtech-tasklist-frontend/` (Vue 3).

- **`README.md`** — Project documentation (architecture, setup, status)
- **`misc/docs/SPECIFICATION.md`** — Original challenge spec (was README.md, renamed for preservation)
- **`misc/docs/backend/auth-flow.md`** — Detailed JWT auth flow documentation (Portuguese, sequence diagrams)
- **`AGENTS.md`** — This file, guidance for AI agents
- **`.agents/skills/`** — 23 installed agent skills (process, backend, frontend)
- **`skills-lock.json`** — Lock file tracking skill sources and versions
- **`misc/docs/BACKLOG.md`** — Implementation backlog (bugs, security, features, quality, with dependencies)

## Known Issues

Found during code review; see `misc/docs/BACKLOG.md` for full detail and tracking. Worth knowing before touching related code:

- **`JWT_SECRET` has a real, working default value committed in `application.yml`** — any deployment that doesn't override it is using a key anyone with repo access already has.
- **`management.endpoints.web.exposure.include: '*'`** combined with `permitAll()` on `/actuator/**` exposes all actuator endpoints unauthenticated.
- Mock-mode and API-mode logic in the frontend stores (`tasks.ts`, `lists.ts`) is hand-duplicated per operation — when fixing a bug in one mode, check whether the other mode has the same bug.

## Backend (`jtech-tasklist-backend/`)

- **Stack**: Spring Boot 4.1.0, Java 25, Gradle 9.6.1, Spring Data JPA + Hibernate
- **Architecture**: Hexagonal (`application/` → core/domains/ports/usecases, `adapters/` → input/controllers, output/repositories, `config/`)
- **Dependencies**: Spring Web, Spring Data JPA, Spring Actuator, Springdoc OpenAPI 3.0.3, Hibernate Validator 9.1.0.Final, Spring Security, JJWT 0.12.6, Lombok, PostgreSQL, H2 (test), Flyway (core + postgresql)
- **Auth**: Spring Security + JWT + bcrypt + refresh token implemented. `POST /api/v1/auth/register` (unique email validation), `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`. Users table with `ROLE_USER` default role. See `misc/docs/backend/auth-flow.md` for the full flow.
- **Main class**: `br.com.jtech.tasklist.StartTasklist`
- **Commands** (run from `jtech-tasklist-backend/`):
  - `./gradlew bootRun` — start dev server
  - `./gradlew test` — run tests (JUnit 5 + AssertJ)
  - `./gradlew build` — build (includes tests)
  - `./gradlew jacocoTestReport` — coverage report
- **Database**: PostgreSQL via env vars (`DS_URL`, `DS_PORT`, `DS_DATABASE`, `DS_USER`, `DS_PASS`). Defaults to `localhost:5432/jtech_tasklist`
- **Tests**: H2 in-memory, config in `src/test/resources/application-test.properties`, `ddl-auto=create`, Flyway disabled. Suite: `AuthIntegrationTest`, `TasklistIntegrationTest`, `TaskIntegrationTest` (full-stack integration via HTTP), `RegisterUserUseCaseTest`, `TaskUseCaseTest`, `TasklistUseCaseTest` (unit, Mockito).
- **Test frameworks**: JUnit Platform Suite 6.1.1, AssertJ 3.27.7, Bean Matchers 0.14, Spring Security Test
- **Server port**: `PORT` env var (default `0` = random). `server.forward-headers-strategy: framework`
- **Swagger**: enabled at `/doc/tasklist/v1/api.html`, API docs at `/doc/tasklist/v3/api-documents`
- **Migrations**: Flyway migrations in `src/main/resources/db/migration/` with `V###__description.sql` naming convention
- **JPA**: `ddl-auto: none` in production — Flyway manages schema via `db/migration/V*.sql`
- **Audit base classes**: `application/core/domains/BaseDomain.java` (pure domain POJO with `T id` + audit + soft-delete helpers), `adapters/output/repositories/entities/BaseEntity.java` (`@MappedSuperclass` with Spring Data JPA Auditing annotations + `@Version`), `config/infra/audit/AuditorAwareImpl.java`, `config/infra/audit/JpaAuditingConfig.java`.
- **Profile**: `PROFILE` env var (default `dev`)
- **Publishing**: Nexus at `nexus.jtech.com.br`, requires `MAVEN_REPO_USER`/`MAVEN_REPO_PASS`
- **Docker compose**: PostgreSQL 18.4 service in `docker-compose.yml` (for local dev, run from `jtech-tasklist-backend/`) — covers the DB only; no Dockerfile for the app itself yet (see `prod-docker-compose` in backlog)
- **Mockserver**: Flask-based in `mockserver/http-mockserver/` (Python, `requirements.txt`)
- **Lombok**: used project-wide (compileOnly + annotationProcessor)

## Frontend (`jtech-tasklist-frontend/`)

- **Stack**: Vue 3.5 (Composition API), TypeScript 6, Vite 7, Vue Router 5, Pinia 3, Vitest 4, Axios
- **Node**: `^20.19.0 || >=22.12.0`
- **Still missing**: Vuetify / Material Design (required per challenge spec, not yet installed) — the only unmet item from the mandatory stack list
- **Commands** (run from `jtech-tasklist-frontend/`):
  - `npm run dev` — Vite dev server
  - `npm run build` — type-check + build in parallel (`run-p` via npm-run-all2)
  - `npm run type-check` — `vue-tsc --build`
  - `npm run test:unit` — Vitest (jsdom environment)
  - `npm run lint` — ESLint flat config (`eslint.config.ts`)
  - `npm run format` — Prettier (`src/` only, semi:false, singleQuote:true, printWidth:100)
- **Path alias**: `@` → `./src`
- **Auth mode**: `VITE_AUTH_MODE=mock|api` env var (default `mock`). `api` mode POSTs to backend via axios. API base URL from `VITE_API_BASE_URL` (default `http://localhost:8080`). Mock mode does not support authenticated API calls beyond login/register (no tokens attached to requests). The two modes are separate code paths per store method, not a shared implementation — they have already drifted apart in a couple of places (see Known Issues), so a fix in one mode isn't automatically a fix in the other.
- **Services layer**: `src/services/api.ts` (axios instance with Bearer token interceptor and refresh-on-401 queueing), `src/services/authApi.ts` (login/register API calls), `src/services/tasklistApi.ts` (CRUD operations for tasklists in API mode), `src/services/taskApi.ts` (CRUD operations for tasks in API mode)
- **Pinia persistence**: `pinia-plugin-persistedstate@4.7.1` installed, registered in `src/main.ts`. Usage for setup stores:
  ```ts
  defineStore('id', () => { ... }, { persist: true })
  // or granular config:
  defineStore('id', () => { ... }, { persist: { storage: sessionStorage, pick: ['user'] } })
  ```
  Note: `auth`, `lists`, and `tasks` stores all persist to `localStorage` (the plugin default), including the JWT access/refresh tokens in the `auth` store — a known trade-off of header-based Bearer auth, not a bug, but worth remembering if this ever needs to be hardened against XSS.
- **Lists feature**: Pinia store (`src/stores/lists.ts`) with CRUD operations, localStorage persistence, dialog components (Create, Rename, Delete), sidebar navigation, and a `/lists` route with auth guard (list selection is in-store state, not a route param — there's no `/lists/:id`). Supports dual mode: `mock` (localStorage) and `api` (backend via `tasklistApi.ts`).
- **Tasks feature**: Pinia store (`src/stores/tasks.ts`) with CRUD operations, per-list filtering (`tasksForActiveList`), completion toggle, localStorage persistence via `pinia-plugin-persistedstate`. Components: `TaskItem.vue` (checkbox, edit/delete on hover), `CreateTaskDialog.vue`, `EditTaskDialog.vue`, `DeleteTaskDialog.vue`. Services: `taskApi.ts` (API mode CRUD). Rendered within `/lists` (`ListsView.vue`), not a separate route.
- **Test files**: in `src/**/__tests__/`, covering stores, services, most components, and the router guard.
- **ESLint**: `pluginVue.configs['flat/essential']` + `vueTsConfigs.recommended` + `pluginVitest` for `__tests__` files
- **EditorConfig**: 2-space indent, lf, utf-8, final newline, printWidth 100
- **Build output**: `dist/` (gitignored)
- **Coverage**: `coverage/` (gitignored)

## Agent Skills

Skills in `.agents/skills/` extend agent capabilities for this project's stack:

| Category | Skills |
|---|---|
| **Process** | brainstorming, caveman-commit, writing-plans, executing-plans, test-driven-development, systematic-debugging, verification-before-completion, requesting/receiving-code-review, subagent-driven-development, dispatching-parallel-agents, using-git-worktrees, finishing-a-development-branch, using-superpowers, writing-skills, find-skills |
| **Backend** | spring-boot-security-jwt, hexagonal-architecture |
| **Frontend** | vitest, typescript-advanced-types, vue-pinia-best-practices, vue-router-best-practices, vuetify0 |

- `npx skills list` — list installed skills
- `npx skills check` — check for updates
- `npx skills update` — update all skills

## General

- Update npm deps in the frontend directory, Gradle deps in the backend directory
- No CI/CD, no GitHub Actions. `docker-compose.yml` currently covers only the Postgres dependency for local dev — no Dockerfile for the app itself and no compose service for the frontend (see `prod-docker-compose` in backlog for full containerization)
- **Dev script**: `misc/scripts/dev.sh` — starts backend (port 8080) + frontend concurrently, cleans up both on Ctrl+C
