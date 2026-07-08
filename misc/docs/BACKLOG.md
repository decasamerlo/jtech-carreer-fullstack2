# Backlog

## Features

### frontend-error-handling
Add toast/snackbar notifications for API errors in frontend. Handle HTTP error responses gracefully with user-facing messages (e.g., network errors, 4xx/5xx). Concrete gap found in review: when the axios refresh-token interceptor (`services/api.ts`) fails to refresh and calls `auth.logout()`, nothing tells the user their session expired or redirects them — they sit on a page with a suddenly-unauthenticated store until their next navigation trips the router guard. Depends on: frontend-lists-backend-integration.

### router-not-found-page
No catch-all route exists in `router/index.ts` (no `path: '/:pathMatch(.*)*'`). Navigating to an undefined URL renders a blank page instead of a 404/NotFound view. Small fix, listed here since none of the current routes need it yet but any real deployment should have one.

### delete-list-dependency-check
SPEC §"CRUD Completo de Listas", item 3 requires "Excluir listas com confirmação **e verificação de dependências**". `DeleteListDialog.vue` only asks for confirmation ("Are you sure you want to delete '…'? This action cannot be undone.") — it does not show how many tasks the list contains or warn that they will be removed with it. This is the one genuine frontend spec deviation. Fix: pass the contained-task count into the dialog and display a cascade warning (e.g. "Esta lista contém N tarefas que também serão removidas."). Ties into [[tasklist-delete-orphans-child-tasks]] on the backend side.

## Quality

### error-mapping
Review and standardize error mapping across backend (exception handlers, HTTP status codes, error response DTOs) and frontend (API error interception, user-facing messages). Concrete finding: every "not found or access denied" case (task/tasklist not found, wrong owner) is thrown as `IllegalArgumentException` and mapped to `400 Bad Request` — semantically these are `404`/`403` situations, not malformed-request situations. The integration tests already hedge on this (`isGreaterThanOrEqualTo(400).isLessThan(500)` instead of asserting a specific code), which is itself a signal the convention was never nailed down. Recommend standardizing on `404` for both "doesn't exist" and "exists but isn't yours" (avoids leaking existence to unauthorized callers), reserving `400` for genuine validation failures. See also the security item on `debugMessage` leaking exception internals — same handler, different concern.

### admin-role-seeding
`UserRole.ROLE_ADMIN` is defined in the code and wired into `SecurityConfig` for actuator endpoint protection (`hasRole("ADMIN")`), but no registration path ever grants it — all new users get `ROLE_USER`. Either create an admin-seeding mechanism (e.g., a `data.sql` seed or an admin-only registration endpoint) or drop ROLE_ADMIN and the actuator security rule until admin functionality is actually needed.

### ci-pipeline
No CI/CD exists (no GitHub Actions or equivalent). At minimum, run `./gradlew test` and `npm run test:unit`/`npm run lint`/`npm run type-check` on every push/PR. Given `SPECIFICATION.md` explicitly lists test coverage as an evaluation criterion, a green CI badge is a cheap, visible way to back that up.

### soft-delete-migration
Migrate from `@SQLRestriction` + manual `markAsDeleted()` to Hibernate 7 `@SoftDelete`. Unify soft-delete lifecycle. (Note: distinct from [[tasklist-delete-orphans-child-tasks]] — that one is about cascading the soft-delete to child tasks and the missing FK `ON DELETE` rule; this item is only about the internal soft-delete implementation technique.)

### refresh-token-cleanup-job
Add a scheduled job to purge expired and revoked rows from the `refresh_tokens` table — it grows unbounded over time with no cleanup mechanism.

### misc-utils
Catch-all for utility tools and services that don't fit elsewhere — e.g., tunnel scripts to dev/qa/prod environments, a local JWT signing-key server, mock services, or any other auxiliary infrastructure needed during development or support.

### misc-tests
Build a `misc/tests/` folder with decision tables for system behavior, Postman collections for automated API tests against a running backend, and Playwright E2E tests for the frontend against a full-stack environment — covering flows that unit/integration tests don't exercise (auth flows, cross-feature workflows, error boundaries).

### refresh-token-integration-tests
`AuthIntegrationTest` covers register + login but has **no** coverage for `POST /api/v1/auth/refresh` — the most fragile auth flow (`RefreshUseCase.refresh` → `RefreshTokenAdapter.rotateRefreshToken`, which revokes-then-creates). `RefreshUseCaseTest` is unit-only (mocked). Add full-stack cases: refresh happy path returns new access + refresh tokens; invalid/unknown token → 401; expired token → 401; replay of an already-rotated token → 401.

### refresh-rotation-not-transactional
`RefreshTokenAdapter.rotateRefreshToken` does two separate writes with no transaction boundary: `save(existing)` to revoke the old token, then `createRefreshToken(user)` to persist a new one. A crash between them leaves the user with a revoked old token and no new one — refresh capability is lost until re-login with credentials. More broadly, the adapters have no `@Transactional` on read-then-write operations (`update`/`delete` in `TaskAdapter`/`TasklistAdapter`); `@Version` guards concurrent overwrites but not multi-statement atomicity. Fix: annotate `rotateRefreshToken` (and the write paths) `@Transactional`, or express the rotation as a single atomic operation.

### password-encoder-duplicate-instance
`PasswordHasherAdapter` news up its own `private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder()` instead of injecting the `PasswordEncoder` bean already declared in `SecurityConfig.passwordEncoder()`. Two encoder instances exist; if bcrypt strength (or the encoder type) is ever changed in the config, the adapter silently won't follow. Fix: inject the Spring-managed `PasswordEncoder` via constructor.

### id-type-drift-uuid-string
IDs are typed inconsistently across layers. Domain objects (`Task`, `Tasklist`, `User` via `BaseDomain<T>`) and input ports use `String`, but the output ports mix types — e.g. `DeleteTaskOutputGateway.delete(String id, UUID userId)`, and the `Get*/Update*/Delete*OutputGateway` methods take `UUID`. Every use case bridges with `UUID.fromString(...)`, so a malformed id throws `IllegalArgumentException` → mapped to `400` (confusing) instead of a clean `404`, and readers must trace String↔UUID conversions everywhere. This also conflicts with the project convention that foreign-id fields are `String`. Fix: standardize output ports on `String` and keep `UUID.fromString(...)` as a private detail inside the adapters. (Pairs with [[error-mapping]].)

### null-instead-of-optional-gateways
`GetTasksOutputGateway.findByIdAndUserId` / `GetTasklistsOutputGateway.findByIdAndUserId` return the domain object or `null` (adapters do `.map(...).orElse(null)`), pushing null-handling outward: `GetTasksUseCase` does `if (task != null && ...)` and `TaskController.findById` re-checks `if (task == null) throw ...`. The return type doesn't communicate absence and the "discover the 404 in the controller" pattern is backwards. Fix: return `Optional<T>` from the gateways and let the use case throw a domain not-found exception (dovetails with [[error-mapping]]).

### dead-code-dtos-and-utils
Residual dead code the `codebase-hygiene-cleanup` pass missed:
- `TasklistResponse` (`adapters/input/protocols/`) has unused `of(List<TasklistEntity>)` and `of(TasklistEntity)` methods plus an unused `responses` field — and to support them it imports `TasklistEntity` (an **output-adapter** JPA entity) and `org.springframework.beans.BeanUtils` into an **input-adapter** DTO, a hexagonal boundary leak (input must not depend on output/persistence).
- `TasklistRequest` has unused `id` and `requests` fields (never used on create/update).
- `config/infra/utils/Jsons.java` and `config/infra/utils/GenId.java` — utility classes with zero references anywhere in `src/main`/`src/test`.
- `BaseDomain.markAsDeleted()` (no-arg), `restore()`, and `isDeleted()` — unused; entities carry their own soft-delete on `BaseEntity`, and adapters call `entity.markAsDeleted(userId)`.
Fix: delete all of the above.

### getcurrentuserid-duplicated
`TaskController` and `TasklistController` each define an identical private `getCurrentUserId()` (`SecurityContextHolder.getContext().getAuthentication().getName()`). Extract a shared resolver — e.g. a small `@Component`/argument resolver or a `@ControllerAdvice` model attribute — so controllers receive the current user id directly.

### list-api-unnecessary-refetch
`apiRenameList` and `apiDeleteList` (`stores/lists.ts`) call `await apiFetchLists()` after the mutation instead of patching local state. `updateTasklist` already returns the updated `TasklistResponse`, so the rename can patch `apiLists.value[idx]` and the delete can splice locally. Current behavior adds an extra HTTP round-trip and opens a race window where a concurrent client's change is refetched over the user's intent. Fix: patch/splice locally; reserve full refetch for initial load.

### tasklist-delete-orphans-child-tasks
Deleting a tasklist soft-deletes only the tasklist row (`TasklistAdapter.delete` → `markAsDeleted`); child `task` rows keep `deleted_at IS NULL` and are merely *hidden* by the parent-existence check in `GetTasksUseCase`. So orphaned-but-live task rows accumulate, and `fk_task_tasklist` (`V006`) has no `ON DELETE` rule — any future hard-purge of tasklists will fail on the FK. Functionally correct today (tasks are inaccessible via the API), but it's a latent data-integrity issue and the reason the parent-existence check costs an extra query per task read. Fix: cascade the soft-delete to children (`UPDATE task SET deleted_at=…, deleted_by=… WHERE tasklist_id=:id`) in `TasklistAdapter.delete` (or a dedicated use case) and/or add an FK `ON DELETE` policy. Related: [[soft-delete-migration]], [[delete-list-dependency-check]]. (Note: current behavior is soft-delete, not the hard-delete/500 the old README "Bugs" table described.)

### usecase-bean-wiring-opaque
`TaskUseCaseConfig` wires use cases by passing the same `TaskAdapter` bean into two positional constructor slots — e.g. `new CreateTaskUseCase(taskAdapter, tasklistAdapter, taskAdapter)` (the adapter satisfies both `CreateTaskOutputGateway` and `GetTasksOutputGateway`). Functionally correct, but a reader must cross-reference the use-case constructor to see which slot is which port. This is a deliberate trade-off for keeping the use cases framework-annotation-free. Options if it's judged not worth it: use `@Qualifier`/named beans, or annotate the use cases `@Component` and delete the `*UseCaseConfig` classes (constructor param *types* are distinct, so Spring won't confuse them). Low priority.

### api-response-timestamps-dropped
Backend `TasklistResponse`/`TaskResponse` expose only `id`/`name` (and task fields), not `createdAt`/`updatedAt`, and the frontend mappers in `tasklistApi.ts`/`taskApi.ts` drop them too. `ListsView.vue` therefore renders "Created: —" for lists in `api` mode (the domain/entities do carry the audit timestamps). If created/updated dates matter in the UI, expose them in the response DTOs and map them through. Low priority.

### frontend-a11y-nits
Small accessibility gaps: `index.html` has `<html lang="">` (should be `en` or `pt-BR`); the `TaskItem.vue` completion checkbox (`v-checkbox-btn`) and its edit/delete icon-buttons have no `aria-label`/`title`. Add labels and a document language.

### about-route-requires-auth
`/about` is declared with `meta: { requiresAuth: true }` in `router/index.ts`; an institutional "About" page normally shouldn't require login. It's a leftover from the Vue scaffold (the spec doesn't mention it). Decide: make it public, or remove it. (While here: the backend `GET /api/v1/tasks/{id}` endpoint is never called by the frontend — dead API surface, noted for awareness, not necessarily for removal since the spec lists it.)

## Security

### auth-rate-limiting
No rate limiting on `/api/v1/auth/login` or `/register` — not required by spec, but cheap insurance against brute-force and registration spam.

### register-email-enumeration
`RegisterUserUseCase.register` throws `new IllegalArgumentException("Email already registered: " + command.email())` (line 23), echoing the submitted email back — an account-enumeration signal. The `DataIntegrityViolationException` path in `GlobalExceptionHandler` already returns the generic `"Email already registered"` (no email), so the two duplicate-email paths are inconsistent. Fix: use the same generic message in the use case. Note: `RegisterUserUseCaseTest` currently asserts the message *includes* the email, so update that test too. Combined with [[auth-rate-limiting]] this is a credential-stuffing aid. Also relates to [[error-mapping]] — a duplicate email is arguably `409 Conflict`, not `400`.

### jwt-tokens-in-localstorage
The `auth` store persists `accessToken` + `refreshToken` to `localStorage` (`persist.pick`, plugin default storage). Any XSS then yields a long-lived (7-day refresh) session hijack. This is acknowledged in `AGENTS.md` as a trade-off of header-based Bearer auth, but is not otherwise tracked. Hardening options: issue the refresh token as a `Secure; HttpOnly; SameSite=Strict` cookie (backend change), move tokens to `sessionStorage` as a marginal mitigation, and/or add a strict CSP plus input sanitization. Decide whether to harden or to formally accept the trade-off.

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
- cors-not-wired-into-security
- list-dialogs-swallow-errors
