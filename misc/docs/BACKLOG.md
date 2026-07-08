# Backlog

## Features

### frontend-error-handling
Add toast/snackbar notifications for API errors in frontend. Handle HTTP error responses gracefully with user-facing messages (e.g., network errors, 4xx/5xx). Concrete gap found in review: when the axios refresh-token interceptor (`services/api.ts`) fails to refresh and calls `auth.logout()`, nothing tells the user their session expired or redirects them — they sit on a page with a suddenly-unauthenticated store until their next navigation trips the router guard. Depends on: frontend-lists-backend-integration.

### router-not-found-page
No catch-all route exists in `router/index.ts` (no `path: '/:pathMatch(.*)*'`). Navigating to an undefined URL renders a blank page instead of a 404/NotFound view. Small fix, listed here since none of the current routes need it yet but any real deployment should have one.

## Quality

### refactor-domain-mappers — partially done
`TaskMapper`/`TasklistMapper` already exist as standalone classes and are used by `TaskAdapter`/`TasklistAdapter` — that part of this item is complete. Still open: `UserAdapter` keeps `toEntity`/`toDomain` as private inline methods, and `RefreshTokenAdapter.findValidUserByToken()` duplicates the exact same `User`-building logic inline a second time (a straightforward DRY violation — extract a `UserMapper`). Also still open: `Tasklist.java` (domain) imports `br.com.jtech.tasklist.adapters.input.protocols.TasklistRequest` for a static factory method `Tasklist.of(TasklistRequest)` — this is the DIP violation this item was written for, and as far as we can tell `TasklistController` doesn't even call it (it builds `Tasklist` via the builder directly), so it looks like dead code on top of being a layering violation. Remove or relocate it. Depends on: audit-base-class.

### tests-backend — partially done
Integration tests exist for all three controllers (`AuthIntegrationTest`, `TasklistIntegrationTest`, `TaskIntegrationTest`) and unit tests exist for Task/Tasklist use cases (`TaskUseCaseTest`, `TasklistUseCaseTest`, Mockito) and for `RegisterUserUseCase` (`RegisterUserUseCaseTest`). Remaining gaps: no unit tests for `LoginUseCase`, `RefreshUseCase`, or `JwtService` in isolation (only exercised indirectly through `AuthIntegrationTest`); no test asserts the tasklist-delete-with-tasks scenario (see Bugs); `AuthIntegrationTest` uses `RestTemplate` while `Task`/`TasklistIntegrationTest` use raw `java.net.http.HttpClient` — pick one for consistency. Depends on: backend-auth, backend-tasks.

### tests-frontend — partially done
Solid coverage already exists across stores, services, most components, and the router guard. Remaining gaps: no `RegisterView.spec.ts` (LoginView has one; Register has equivalent validation/error-display complexity), no dedicated test for `TaskListSidebar.vue`'s own list-rendering logic (its dialogs are tested individually), and no test for `HomeView.vue`/`AboutView.vue`. Depends on: frontend-auth, frontend-lists-crud, frontend-tasks-crud, vuetify.

### error-mapping
Review and standardize error mapping across backend (exception handlers, HTTP status codes, error response DTOs) and frontend (API error interception, user-facing messages). Concrete finding: every "not found or access denied" case (task/tasklist not found, wrong owner) is thrown as `IllegalArgumentException` and mapped to `400 Bad Request` — semantically these are `404`/`403` situations, not malformed-request situations. The integration tests already hedge on this (`isGreaterThanOrEqualTo(400).isLessThan(500)` instead of asserting a specific code), which is itself a signal the convention was never nailed down. Recommend standardizing on `404` for both "doesn't exist" and "exists but isn't yours" (avoids leaking existence to unauthorized callers), reserving `400` for genuine validation failures. See also the security item on `debugMessage` leaking exception internals — same handler, different concern.

### config-externalization-hygiene
Two related gaps: (1) `CorsConfig` hardcodes `http://localhost:5173` as the only allowed origin with no property injection, so CORS will simply break the moment the frontend is served from anywhere else — externalize via `@Value`/`application.yml` per profile. (2) Frontend `.env` (with `VITE_AUTH_MODE`/`VITE_API_BASE_URL`) is committed and not in `.gitignore`. Its current contents aren't sensitive, but the pattern invites someone to add a real secret to it later without noticing it's tracked. Rename to `.env.example`, gitignore `.env`.

### codebase-hygiene-cleanup
Grab-bag of small, low-risk cleanups found during review — batch them into one pass:
- Fix package/directory mismatches: `ReadyEventListener.java` lives under `.../config/infra/utils/` but declares `package ...config.infra.listeners`; `GlobalExceptionHandler.java` lives under `.../config/infra/utils/` but declares `package ...config.infra.handlers`. Compiles fine today, but breaks the folder-mirrors-package convention every other class follows.
- `CreateTasklistUseCase`, `CreateTasklistInputGateway`, `CreateTasklistOutputGateway` all carry stale Javadoc referring to a class called `TasklistUseCase`/`TasklistOutputGateway`/`TasklistInputGateway` (copy-paste leftover) — update or remove the headers.
- Remove starter-kit "Copyright (c) J-Tech Solucoes em Informatica... confidential and proprietary" headers on `ReadyEventListener`, `ApiError`, `ApiSubError`, `ApiValidationError`, `Jsons`, `GenId` — these came from a template and don't apply to this repo.
- `OpenAPI30Configuration` has a placeholder title (`"???"`) and a hardcoded third-party contact email (`helder.puia@veolia.com`) left over from the same template — replace with real project info.
- Frontend: `src/stores/counter.ts` (default Pinia scaffold store) and the five default template icon components (`IconCommunity`, `IconDocumentation`, `IconEcosystem`, `IconSupport`, `IconTooling`) are unused — delete.
- `jtech-tasklist-backend/README.md` is an empty stub (section headers only, no content) left over from before `misc/docs/SPECIFICATION.md` existed; `jtech-tasklist-frontend/README.md` is still the generic `create-vue` template text. Delete both or replace with a one-line pointer to the root `README.md`.
- `UserRole.ROLE_ADMIN` is defined but never checked anywhere (no endpoint uses `hasRole`/`hasAuthority`) — either wire it into an actual restricted endpoint or drop it until it's needed.

### ci-pipeline
No CI/CD exists (no GitHub Actions or equivalent). At minimum, run `./gradlew test` and `npm run test:unit`/`npm run lint`/`npm run type-check` on every push/PR. Given `SPECIFICATION.md` explicitly lists test coverage as an evaluation criterion, a green CI badge is a cheap, visible way to back that up.

## Parked

- soft-delete-migration: Migrate from `@SQLRestriction` + manual `markAsDeleted()` to Hibernate 7 `@SoftDelete`. Unify soft-delete lifecycle. (Note: distinct from `tasklist-delete-is-hard-delete-no-cascade` above — that one is a functional bug today regardless of which soft-delete mechanism is used; this item is about the internal implementation technique.)
- auth-rate-limiting: no rate limiting on `/api/v1/auth/login` or `/register` — not required by spec, but cheap insurance against brute-force/registration spam.
- refresh-token-cleanup-job: no scheduled job purges expired/revoked rows from `refresh_tokens`; table grows unbounded over time.
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
