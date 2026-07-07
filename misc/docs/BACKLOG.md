# Backlog

## Features

### frontend-error-handling
Add toast/snackbar notifications for API errors in frontend. Handle HTTP error responses gracefully with user-facing messages (e.g., network errors, 4xx/5xx). Depends on: frontend-lists-backend-integration.

### vuetify
Install Vuetify + @mdi/font. Migrate UI to Material Design. Depends on: frontend-tasks-crud.

## Quality

### refactor-domain-mappers
Move `toEntity()` and `toDomain()` from `UserAdapter`, `TasklistAdapter` (and any other adapter) into dedicated mapper classes in `adapters/output/repositories/mappers/`. Move entity→DTO conversion from domain classes to controller-layer mappers. Domain should have zero imports from adapters or input protocols (DIP violation). Update all callers. Depends on: audit-base-class.

### tests-backend
Write unit tests (Mockito) and integration tests (Spring Test + H2). Depends on: backend-auth, backend-tasks.

### tests-frontend
Write Vitest tests for stores, views, and components. Depends on: frontend-auth, frontend-lists-crud, frontend-tasks-crud, vuetify.

### docs-readme
Write final README.md per SPEC: architecture overview, stack with justifications, setup, tests, folder structure, technical decisions, roadmap.

### error-mapping
Review and standardize error mapping across backend (exception handlers, HTTP status codes, error response DTOs) and frontend (API error interception, user-facing messages).

## Parked

- soft-delete-migration: Migrate from `@SQLRestriction` + manual `markAsDeleted()` to Hibernate 7 `@SoftDelete`. Unify soft-delete lifecycle.
- prod-docker-compose
- misc-scripts
- misc-queries
- misc-utils
- misc-tests

## Done
- dev-docker-postgres
- flyway-migration
- audit-base-class
- pinia-persist
- frontend-auth
- backend-auth
- frontend-auth-config
- frontend-lists-crud
- frontend-lists-backend-integration
- backend-tasks
- frontend-tasks-crud
