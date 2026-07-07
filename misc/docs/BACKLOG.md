# Backlog

## Bugs

### tasklist-name-not-unique-in-api-mode
The frontend mock store (`lists.ts` → `validateName()`) rejects duplicate tasklist names per user, and it's tested. The real backend has no equivalent check: `CreateTasklistUseCase`/`TasklistAdapter.create()` don't check for an existing name, and no migration adds a unique constraint on `(user_id, name)` for `tasklist` (contrast with `V007`, which adds one for `task(tasklist_id, title)`). `TasklistIntegrationTest` has no duplicate-name test. Result: in `api` mode, users can create multiple tasklists with the same name; in `mock` mode, they can't. Decide the intended behavior and enforce it consistently (use case check + DB constraint + test), matching the pattern already used for tasks.

### task-title-uniqueness-case-mismatch
Frontend `tasks.ts` `validateTitle()` compares titles case-insensitively (`.toLowerCase()`). The backend (`existsByTasklistIdAndTitle`) and the DB (`V007` unique index on `title`) compare case-sensitively. A title the UI blocks as a duplicate ("Buy Milk" vs "buy milk") is not actually a duplicate as far as the API and database are concerned, and vice versa. Pick one semantic (recommend case-insensitive, since that matches user expectation) and apply it in the backend query (e.g. `LOWER(title)`) and the unique index, not just the frontend.

### register-email-race-condition
`RegisterUserUseCase.register()` checks `existsByEmail()` then calls `save()` — two separate statements, not atomic. Two concurrent registrations with the same email can both pass the check and both attempt to save; one will fail the `uk_users_email` DB constraint and raise a raw `DataIntegrityViolationException`, which `GlobalExceptionHandler` doesn't special-case, so it becomes a 500 instead of the intended 400 "Email already registered." Either catch the constraint violation and map it to the existing `IllegalArgumentException` path, or add an explicit handler for `DataIntegrityViolationException`.

### register-flow-bypasses-usecase-layer
`AuthController.register()` calls `registerUserInputGateway.register(user)` and then *directly* calls `tokenOutputGateway.generateAccessToken()` and `refreshTokenOutputGateway.createRefreshToken()` from the controller. `LoginUseCase` and `RefreshUseCase`, by contrast, own their token issuance internally and return a result record — controllers just call one input port. This is an inconsistent application of the hexagonal boundary the rest of the codebase (and `AGENTS.md`) is careful about, and it means `RegisterUserUseCase` can't be unit-tested for "does registration return usable tokens" the way login can. Move token issuance into `RegisterUserUseCase` (or a new use case) so the controller only depends on input ports, matching login/refresh.

## Security

### jwt-secret-default-committed
`application.yml` ships `secret: ${JWT_SECRET:404a6141c4e2b0e5a1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3}`. The README already warns not to use this in production, but the warning doesn't stop the value from being a real, working HMAC key sitting in version control — anyone with repo access can forge valid access tokens against any deployment that forgets to override `JWT_SECRET`. Prefer no default at all (fail fast on missing config outside `dev`/`test` profiles), or move the fallback into `application-dev.yml` only.

### actuator-fully-exposed
`management.endpoints.web.exposure.include: '*'` plus `SecurityConfig` permitting all of `/actuator/**` unauthenticated exposes every actuator endpoint with no auth. Today that's mostly `health`/`info`, but the config as written also opens the door to anything added later (`env`, `beans`, `heapdump`, etc.) without anyone having to remember to lock it down. Restrict `exposure.include` to `health,info` and/or require an authenticated admin role for the rest.

### exception-handler-leaks-internal-messages
`GlobalExceptionHandler.handleGeneral()` sets `debugMessage` to `ex.getLocalizedMessage()` for *any* uncaught exception, in every profile, including whatever a database driver or library decides to put in an exception message. Gate `debugMessage` behind a non-prod profile check (or drop it from the client response entirely and rely on server-side logging).

## Features

### vuetify
Install Vuetify + `@mdi/font`. Migrate UI to Material Design. This is the one still-unmet item from `SPECIFICATION.md`'s mandatory stack table ("UI Framework: Material Design (Vuetify ou biblioteca equivalente)") — everything else on the required stack list is in place. Depends on: frontend-tasks-crud.

### frontend-error-handling
Add toast/snackbar notifications for API errors in frontend. Handle HTTP error responses gracefully with user-facing messages (e.g., network errors, 4xx/5xx). Concrete gap found in review: when the axios refresh-token interceptor (`services/api.ts`) fails to refresh and calls `auth.logout()`, nothing tells the user their session expired or redirects them — they sit on a page with a suddenly-unauthenticated store until their next navigation trips the router guard. Depends on: frontend-lists-backend-integration.

### router-not-found-page
No catch-all route exists in `router/index.ts` (no `path: '/:pathMatch(.*)*'`). Navigating to an undefined URL renders a blank page instead of a 404/NotFound view. Small fix, listed here since none of the current routes need it yet but any real deployment should have one.

## Quality

### refactor-domain-mappers — partially done
`TaskMapper`/`TasklistMapper` already exist as standalone classes and are used by `TaskAdapter`/`TasklistAdapter` — that part of this item is complete. Still open: `UserAdapter` keeps `toEntity`/`toDomain` as private inline methods, and `RefreshTokenAdapter.findValidUserByToken()` duplicates the exact same `User`-building logic inline a second time (a straightforward DRY violation — extract a `UserMapper`). Also still open: `Tasklist.java` (domain) imports `br.com.jtech.tasklist.adapters.input.protocols.TasklistRequest` for a static factory method `Tasklist.of(TasklistRequest)` — this is the DIP violation this item was written for, and as far as we can tell `TasklistController` doesn't even call it (it builds `Tasklist` via the builder directly), so it looks like dead code on top of being a layering violation. Remove or relocate it. Depends on: audit-base-class.

### tests-backend — partially done
Integration tests exist for all three controllers (`AuthIntegrationTest`, `TasklistIntegrationTest`, `TaskIntegrationTest`) and unit tests exist for Task/Tasklist use cases (`TaskUseCaseTest`, `TasklistUseCaseTest`, Mockito). Remaining gaps: no unit tests for `RegisterUserUseCase`, `LoginUseCase`, `RefreshUseCase`, or `JwtService` in isolation (only exercised indirectly through `AuthIntegrationTest`); no test asserts the tasklist-delete-with-tasks scenario (see Bugs); `AuthIntegrationTest` uses `RestTemplate` while `Task`/`TasklistIntegrationTest` use raw `java.net.http.HttpClient` — pick one for consistency. Depends on: backend-auth, backend-tasks.

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
