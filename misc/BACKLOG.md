# Backlog

## Camada 1 — Infraestrutura

### h2-dev-profile
Profile `dev` com H2 em memória, `ddl-auto=create`, H2 console habilitado.

### flyway-migration
Flyway no build.gradle, `ddl-auto=none`, migration inicial com a tabela `tasklist` existente. Migrações incrementais conforme novas entidades forem criadas.

### prod-docker-compose
Serviço PostgreSQL no docker-compose.yml. Profile `prod` apontando para o container. Docker obrigatório. Depende de: flyway-migration.

### audit-base-class
`@MappedSuperclass` base com `createdAt`, `updatedAt`, `createdBy`, `updatedBy`. `@EnableJpaAuditing` + `AuditingEntityListener`. Entidades JPA estendem.

### pinia-persist
Instalar `pinia-plugin-persistedstate`. Configurar persistência automática em localStorage para stores de autenticação e listas.

## Camada 2 — Features

### backend-auth
Spring Security + JWT + bcrypt + refresh token. POST /auth/register com validação de email único. POST /auth/login. User domain, JwtProvider, SecurityConfig.

### backend-tasks
CRUD /tasks/* com ownership validation, Bean Validation nos DTOs. Task domain, controller, repository. Depende de: backend-auth.

### frontend-auth
Login view com validação. Mock auth (qualquer credencial). Auth store com persistência (pinia-persist). Route guards. Depende de: pinia-persist.

### frontend-lists-crud
CRUD de listas com nomes personalizados, renomear com validação, excluir com confirmação. Navegação entre listas. Store persistida (pinia-persist). Depende de: frontend-auth, pinia-persist.

### frontend-tasks-crud
CRUD de tarefas por lista (adicionar, editar, remover, marcar conclusão). Prevenção de duplicatas, validação de campos obrigatórios. Depende de: frontend-lists-crud.

### vuetify
Vuetify + @mdi/font. Migrar UI para Material Design. Depende de: frontend-tasks-crud.

## Camada 3 — Qualidade

### tests-backend
Unitários (Mockito) + integração (Spring Test + H2). Depende de: backend-auth, backend-tasks.

### tests-frontend
Vitest para stores, views, components. Depende de: frontend-auth, frontend-lists-crud, frontend-tasks-crud, vuetify.

### docs-readme
README.md final conforme SPEC: visão da arquitetura, stack com justificativas, setup, testes, estrutura de pastas, decisões técnicas, roadmap.
