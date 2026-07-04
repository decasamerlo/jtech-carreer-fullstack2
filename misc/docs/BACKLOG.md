# Backlog

## Layer 1 — Infrastructure

### dev-docker-postgres
Set up `dev` profile with Docker PostgreSQL, similar to prod configs. Use H2 in-memory only for tests.

### flyway-migration
Add Flyway to build.gradle with `ddl-auto=none`. Create initial migration with the existing `tasklist` table. Add incremental migrations as new entities are created.

### prod-docker-compose
Define PostgreSQL service in docker-compose.yml. Configure `prod` profile pointing to the container. Depends on: flyway-migration.

### audit-base-class
Create `@MappedSuperclass` base entity with `createdAt`, `updatedAt`, `createdBy`, `updatedBy`. Enable `@EnableJpaAuditing` + `AuditingEntityListener`. Extend JPA entities from it.

### pinia-persist
Install `pinia-plugin-persistedstate`. Configure automatic localStorage persistence for auth and list stores.

## Layer 2 — Features

### backend-auth
Implement Spring Security + JWT + bcrypt + refresh token. Expose POST /auth/register with unique email validation. Expose POST /auth/login. Build User domain, JwtProvider, SecurityConfig.

### backend-tasks
Implement CRUD /tasks/* with ownership validation. Apply Bean Validation on DTOs. Build Task domain, controller, repository. Depends on: backend-auth.

### frontend-auth
Build Login view with validation. Implement mock auth (any credential). Set up Auth store with persistence (pinia-persist). Add route guards. Depends on: pinia-persist.

### frontend-lists-crud
Implement CRUD for lists with custom names, rename with validation, and delete with confirmation. Add navigation between lists. Persist store with pinia-persist. Depends on: frontend-auth, pinia-persist.

### frontend-tasks-crud
Implement CRUD for tasks per list (add, edit, remove, mark completion). Prevent duplicates, validate required fields. Depends on: frontend-lists-crud.

### vuetify
Install Vuetify + @mdi/font. Migrate UI to Material Design. Depends on: frontend-tasks-crud.

## Layer 3 — Quality

### tests-backend
Write unit tests (Mockito) and integration tests (Spring Test + H2). Depends on: backend-auth, backend-tasks.

### tests-frontend
Write Vitest tests for stores, views, and components. Depends on: frontend-auth, frontend-lists-crud, frontend-tasks-crud, vuetify.

### docs-readme
Write final README.md per SPEC: architecture overview, stack with justifications, setup, tests, folder structure, technical decisions, roadmap.
