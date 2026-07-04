# Backlog

## Layer 1 — Infrastructure

### audit-base-class
Create `BaseDomain<T>` (abstract domain POJO) and `BaseEntity<T>` (`@MappedSuperclass` with JPA auditing) containing: id, createdAt, createdBy, updatedAt, updatedBy, deletedAt, deletedBy. Entity adds `@Version` for optimistic locking. Includes soft-delete helpers (`markAsDeleted`, `restore`, `isDeleted`), `AuditorAwareImpl`, `JpaAuditingConfig`, and Flyway migration `V002__add_audit_columns_to_tasklist.sql`.

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

### refactor-domain-mappers
Move `toEntity()`, `of(TasklistEntity)`, and `of(TasklistRequest)` out of `Tasklist` domain class into dedicated adapter-layer mappers (`adapters/output/repositories/mappers/` and controller DTOs). Domain should have zero imports from adapters or input protocols (DIP violation). Update all callers. Depends on: audit-base-class.

### tests-backend
Write unit tests (Mockito) and integration tests (Spring Test + H2). Depends on: backend-auth, backend-tasks.

### tests-frontend
Write Vitest tests for stores, views, and components. Depends on: frontend-auth, frontend-lists-crud, frontend-tasks-crud, vuetify.

### docs-readme
Write final README.md per SPEC: architecture overview, stack with justifications, setup, tests, folder structure, technical decisions, roadmap.

## Parked

- prod-docker-compose

## Done
- dev-docker-postgres
- flyway-migration
