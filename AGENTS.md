# AGENTS.md

## Repository Structure

Two-project monorepo: `jtech-tasklist-backend/` (Spring Boot) and `jtech-tasklist-frontend/` (Vue 3).

- **`README.md`** — Project documentation (architecture, setup, status)
- **`misc/docs/SPECIFICATION.md`** — Original challenge spec (was README.md, renamed for preservation)
- **`AGENTS.md`** — This file, guidance for AI agents
- **`.agents/skills/`** — 23 installed agent skills (process, backend, frontend)
- **`skills-lock.json`** — Lock file tracking skill sources and versions
- **`misc/docs/BACKLOG.md`** — Implementation backlog (worktree tasks with dependencies)

## Backend (`jtech-tasklist-backend/`)

- **Stack**: Spring Boot 4.1.0, Java 25, Gradle 9.6.1, Spring Data JPA + Hibernate
- **Architecture**: Hexagonal (`application/` → core/domains/ports/usecases, `adapters/` → input/controllers, output/repositories, `config/`)
- **Dependencies**: Spring Web, Spring Data JPA, Spring Actuator, Springdoc OpenAPI 3.0.3, Hibernate Validator 9.1.0.Final, Lombok, PostgreSQL, H2 (test), Flyway (core + postgresql)
- **Still missing**: Spring Security + JWT (required per challenge spec, not yet added)
- **Main class**: `br.com.jtech.tasklist.StartTasklist`
- **Commands** (run from `jtech-tasklist-backend/`):
  - `./gradlew bootRun` — start dev server
  - `./gradlew test` — run tests (JUnit 5 + AssertJ)
  - `./gradlew build` — build (includes tests)
  - `./gradlew jacocoTestReport` — coverage report
- **Database**: PostgreSQL via env vars (`DS_URL`, `DS_PORT`, `DS_DATABASE`, `DS_USER`, `DS_PASS`). Defaults to `localhost:5432/jtech_tasklist`
- **Tests**: H2 in-memory, config in `src/test/resources/application-test.properties`, `ddl-auto=create`, Flyway disabled
- **Test frameworks**: JUnit Platform Suite 6.1.1, AssertJ 3.27.7, Bean Matchers 0.14
- **Server port**: `PORT` env var (default `0` = random). `server.forward-headers-strategy: framework`
- **Swagger**: enabled at `/doc/tasklist/v1/api.html`, API docs at `/doc/tasklist/v3/api-documents`
- **Migrations**: Flyway migrations in `src/main/resources/db/migration/` with `V###__description.sql` naming convention
- **JPA**: `ddl-auto: none` in production — Flyway manages schema via `db/migration/V*.sql`
- **Profile**: `PROFILE` env var (default `dev`)
- **Publishing**: Nexus at `nexus.jtech.com.br`, requires `MAVEN_REPO_USER`/`MAVEN_REPO_PASS`
- **Docker compose**: PostgreSQL 18.4 service in `docker-compose.yml` (for local dev, run from `jtech-tasklist-backend/`)
- **Mockserver**: Flask-based in `mockserver/http-mockserver/` (Python, `requirements.txt`)
- **Lombok**: used project-wide (compileOnly + annotationProcessor)

## Frontend (`jtech-tasklist-frontend/`)

- **Stack**: Vue 3.5 (Composition API), TypeScript 6, Vite 7, Vue Router 5, Pinia 3, Vitest 4
- **Node**: `^20.19.0 || >=22.12.0`
- **Still missing**: Vuetify / Material Design (required per challenge spec, not yet installed)
- **Commands** (run from `jtech-tasklist-frontend/`):
  - `npm run dev` — Vite dev server
  - `npm run build` — type-check + build in parallel (`run-p` via npm-run-all2)
  - `npm run type-check` — `vue-tsc --build`
  - `npm run test:unit` — Vitest (jsdom environment)
  - `npm run lint` — ESLint flat config (`eslint.config.ts`)
  - `npm run format` — Prettier (`src/` only, semi:false, singleQuote:true, printWidth:100)
- **Path alias**: `@` → `./src`
- **Test files**: located in `src/**/__tests__/` (inferred from `vitest.config.ts` exclude + eslint plugin pattern)
- **ESLint**: `pluginVue.configs['flat/essential']` + `vueTsConfigs.recommended` + `pluginVitest` for `__tests__` files
- **EditorConfig**: 2-space indent, lf, utf-8, final newline, printWidth 100
- **Build output**: `dist/` (gitignored)
- **Coverage**: `coverage/` (gitignored)

## Agent Skills

Skills in `.agents/skills/` extend agent capabilities for this project's stack:

| Category | Skills |
|---|---|
| **Process** | brainstorming, caveman-commit, writing-plans, executing-plans, tdd, systematic-debugging, verification-before-completion, requesting/receiving-code-review, subagent-driven-development, dispatching-parallel-agents, using-git-worktrees, finishing-a-development-branch, using-superpowers, writing-skills, find-skills |
| **Backend** | spring-boot-security-jwt, hexagonal-architecture |
| **Frontend** | vitest, typescript-advanced-types, vue-pinia-best-practices, vue-router-best-practices, vuetify0 |

- `npx skills list` — list installed skills
- `npx skills check` — check for updates
- `npx skills update` — update all skills

## General

- Update npm deps in the frontend directory, Gradle deps in the backend directory
- No CI/CD, no GitHub Actions, no Docker compose for backing services yet
- Backend has no test code yet only test config (`src/test/resources/application-test.properties`)
