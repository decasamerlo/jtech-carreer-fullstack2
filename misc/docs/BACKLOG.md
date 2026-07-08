# Backlog

## Features

### frontend-error-handling
Add toast/snackbar notifications for API errors in frontend. Handle HTTP error responses gracefully with user-facing messages (e.g., network errors, 4xx/5xx). Concrete gap found in review: when the axios refresh-token interceptor (`services/api.ts`) fails to refresh and calls `auth.logout()`, nothing tells the user their session expired or redirects them — they sit on a page with a suddenly-unauthenticated store until their next navigation trips the router guard. Depends on: frontend-lists-backend-integration.

### router-not-found-page
No catch-all route exists in `router/index.ts` (no `path: '/:pathMatch(.*)*'`). Navigating to an undefined URL renders a blank page instead of a 404/NotFound view. Small fix, listed here since none of the current routes need it yet but any real deployment should have one.

## Quality

### error-mapping
Review and standardize error mapping across backend (exception handlers, HTTP status codes, error response DTOs) and frontend (API error interception, user-facing messages). Concrete finding: every "not found or access denied" case (task/tasklist not found, wrong owner) is thrown as `IllegalArgumentException` and mapped to `400 Bad Request` — semantically these are `404`/`403` situations, not malformed-request situations. The integration tests already hedge on this (`isGreaterThanOrEqualTo(400).isLessThan(500)` instead of asserting a specific code), which is itself a signal the convention was never nailed down. Recommend standardizing on `404` for both "doesn't exist" and "exists but isn't yours" (avoids leaking existence to unauthorized callers), reserving `400` for genuine validation failures. See also the security item on `debugMessage` leaking exception internals — same handler, different concern.

### admin-role-seeding
`UserRole.ROLE_ADMIN` is defined in the code and wired into `SecurityConfig` for actuator endpoint protection (`hasRole("ADMIN")`), but no registration path ever grants it — all new users get `ROLE_USER`. Either create an admin-seeding mechanism (e.g., a `data.sql` seed or an admin-only registration endpoint) or drop ROLE_ADMIN and the actuator security rule until admin functionality is actually needed.

### ci-pipeline
No CI/CD exists (no GitHub Actions or equivalent). At minimum, run `./gradlew test` and `npm run test:unit`/`npm run lint`/`npm run type-check` on every push/PR. Given `SPECIFICATION.md` explicitly lists test coverage as an evaluation criterion, a green CI badge is a cheap, visible way to back that up.

### soft-delete-migration
Migrate from `@SQLRestriction` + manual `markAsDeleted()` to Hibernate 7 `@SoftDelete`. Unify soft-delete lifecycle. (Note: distinct from `tasklist-delete-is-hard-delete-no-cascade` above — that one is a functional bug today regardless of which soft-delete mechanism is used; this item is about the internal implementation technique.)

### refresh-token-cleanup-job
Add a scheduled job to purge expired and revoked rows from the `refresh_tokens` table — it grows unbounded over time with no cleanup mechanism.

### misc-utils
Catch-all for utility tools and services that don't fit elsewhere — e.g., tunnel scripts to dev/qa/prod environments, a local JWT signing-key server, mock services, or any other auxiliary infrastructure needed during development or support.

### misc-tests
Build a `misc/tests/` folder with decision tables for system behavior, Postman collections for automated API tests against a running backend, and Playwright E2E tests for the frontend against a full-stack environment — covering flows that unit/integration tests don't exercise (auth flows, cross-feature workflows, error boundaries).

## Security

### auth-rate-limiting
No rate limiting on `/api/v1/auth/login` or `/register` — not required by spec, but cheap insurance against brute-force and registration spam.

## Infrastructure

### prod-docker-compose
Containerize the backend application (Dockerfile) and add a production-grade `docker-compose.yml` with the backend, frontend (served via nginx), and PostgreSQL — replacing the current dev-only compose that covers only the database.

### misc-scripts
Organize utility scripts under `misc/scripts/` with two subdirectories: `bash/` for shell scripts (dev, health checks, deployment helpers) and `sql/` for database queries (debugging, data analysis, manual audits). Document each script with inline help and consistent patterns.

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
- docs-readme
- soft-delete-tasklist-cascade
- tasklist-name-not-unique-in-api-mode
- register-email-race-condition
- task-title-uniqueness-case-mismatch
- register-flow-bypasses-usecase-layer
- jwt-secret-default-committed
- actuator-fully-exposed
- exception-handler-leaks-internal-messages
- vuetify
- refactor-domain-mappers
- tests-backend
- tests-frontend
- config-externalization-hygiene
- codebase-hygiene-cleanup
