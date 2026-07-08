# JTech Tasklist

Sistema TODO List multi-usuário com arquitetura hexagonal no backend e componentização reativa no frontend. Solução fullstack para o desafio técnico Fullstack2 da JTech.

---

## Stack Tecnológica

### Backend (`jtech-tasklist-backend/`)

| Tecnologia | Versão | Justificativa |
|---|---|---|
| **Java** | 25 | Versão mais recente com suporte a padrões modernos de concorrência e performance |
| **Spring Boot** | 4.1.0 | Ecossistema maduro para aplicações enterprise com injeção de dependência nativa |
| **Spring Data JPA + Hibernate** | — | Mapeamento objeto-relacional robusto com suporte a locks otimistas e cache |
| **Springdoc OpenAPI** | 3.0.3 | Geração automática de documentação OpenAPI 3.0 sem acoplamento ao código |
| **Spring Security** | — | Autenticação JWT com filtros stateless e controle de acesso |
| **JJWT** | 0.12.6 | Criação e validação de tokens JWT com suporte a HMAC |
| **Hibernate Validator** | 9.1.0.Final | Validação declarativa via Jakarta Bean Validation 3.1 |
| **Spring Actuator** | — | Métricas e health checks para observabilidade em produção |
| **Spring Security Test** | — | Suporte a testes de integração com autenticação simulada |
| **PostgreSQL** | — | Banco relacional com suporte a JSONB, índices parciais e MVCC |
| **H2** | — | Banco em memória para testes de integração isolados |
| **Flyway** | — | Migrações de schema versionadas e reprodutíveis |
| **Lombok** | — | Redução de boilerplate em entidades e DTOs |
| **Gradle** | 9.6.1 | Build incremental com cache inteligente e suporte a toolchains |
| **AssertJ** | 3.27.7 | Asserções fluidas com mensagens de erro descritivas |
| **JUnit Platform Suite** | 6.1.1 | Suite de testes modular com tags e filtros |

### Frontend (`jtech-tasklist-frontend/`)

| Tecnologia | Versão | Justificativa |
|---|---|---|
| **Vue 3** | 3.5.39 | Composition API com reatividade fina e tree-shaking nativo |
| **TypeScript** | 6.0.3 | Tipagem estática para robustez em manutenção e refatoração |
| **Vite** | 7.3.5 | Build instantâneo com HMR nativo via ESM |
| **Vue Router** | 5.1.0 | Roteamento SPA com lazy-loading e guards de navegação |
| **Pinia** | 3.0.4 | Gerenciamento de estado com suporte a DevTools e extensões |
| **pinia-plugin-persistedstate** | 4.7.1 | Persistência automática de stores no localStorage/sessionStorage |
| **Vuetify** | 4.1.x | Material Design 3 component framework |
| **@mdi/font** | — | Material Design Icons |
| **vite-plugin-vuetify** | — | Vite plugin para auto-import de componentes/directives |
| **Vitest** | 4.1.9 | Testes unitários compatíveis com ecossistema Vite |
| **ESLint** | 10.6.0 | Análise estática com regras específicas para Vue e TypeScript |
| **Prettier** | 3.9.4 | Formatação consistente sem debates de estilo |
| **Axios** | — | Cliente HTTP com interceptores para token JWT e tratamento de erros |

---

## Visão Geral da Arquitetura

```
┌────────────────────────────────────────────────────────────┐
│                    Frontend (Vue 3 SPA)                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────────┐  │
│  │  Views   │→ │  Stores  │→ │  Router  │  │ Components │  │
│  │(páginas) │  │  (Pinia) │  │ (guards) │  │ (reutiliz.)│  │
│  └──────────┘  └──────────┘  └──────────┘  └────────────┘  │
└──────────────────────────┬─────────────────────────────────┘
                           │ HTTP (REST)                      
┌──────────────────────────▼─────────────────────────────────┐
│                    Backend (Spring Boot)                   │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                  Adaptadores Input                   │  │
│  │      ┌──────────────────────────────────────┐        │  │
│  │      │         Controllers REST             │        │  │
│  │      │         /tasklist/*                  │        │  │
│  │      └───────────────┬──────────────────────┘        │  │
│  └──────────────────────┼───────────────────────────────┘  │
│                         │                                  │
│  ┌──────────────────────▼───────────────────────────────┐  │
│  │            Portas de Entrada (Input Ports)           │  │
│  │       Interfaces que definem casos de uso            │  │
│  └──────────────────────┬───────────────────────────────┘  │
│                         │                                  │
│  ┌──────────────────────▼───────────────────────────────┐  │
│  │            Application Core (Casos de Uso)           │  │
│  │   Domínios   │   Use Cases   │   Ports (in/out)      │  │
│  │  (entities)  │  (serviços)   │  (interfaces)         │  │
│  └──────────────────────┬───────────────────────────────┘  │
│                         │                                  │
│  ┌──────────────────────▼───────────────────────────────┐  │
│  │             Portas de Saída (Output Ports)           │  │
│  └──────────────────────┬───────────────────────────────┘  │
│                         │                                  │
│  ┌──────────────────────▼───────────────────────────────┐  │
│  │              Adaptadores Output                      │  │
│  │  ┌────────────────┐  ┌──────────────────────────────┐│  │
│  │  │  Repositories  │  │  JPA Entities + Mappers      ││  │
│  │  └────────────────┘  └──────────────────────────────┘│  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────┘
```

### Backend: Arquitetura Hexagonal (Ports & Adapters)

Separa o núcleo da aplicação (regras de negócio) dos detalhes tecnológicos (bancos, frameworks, protocolos):

- **`application/core/`** — Domínios puros (entidades sem anotações de infra) e casos de uso (use cases) que orquestram regras de negócio
   - `application/core/domains/` também contém `BaseDomain<T>` — classe base abstrata com campos de auditoria (id, createdAt, createdBy, updatedAt, updatedBy, deletedAt, deletedBy) e helpers para soft delete
- **`application/ports/`** — Interfaces que definem contratos: portas de entrada (input: o que o sistema faz) e portas de saída (output: dependências externas que o sistema precisa)
- **`adapters/input/`** — Controladores REST que traduzem requisições HTTP em chamadas aos casos de uso
- **`adapters/output/`** — Implementações concretas de repositórios JPA, mapeamento entre entidades de domínio e entidades de persistência
   - Entidades JPA estendem `BaseEntity<T>` (`@MappedSuperclass` com Spring Data Auditing, `@Version` para lock otimista, mesmos campos de auditoria + soft delete)
- **`config/`** — Configuração de beans, swagger, exception handlers (`handlers/`), event listeners (`listeners/`), utils, auditoria JPA (`audit/`)

### Frontend: Arquitetura Reativa com Composição

- **Views** — Páginas associadas a rotas (home, about, lists)
- **Components** — Componentes reutilizáveis (dialogos de criação/renomeação/exclusão de listas, sidebar de navegação, componentes de tarefas: TaskItem, CreateTaskDialog, EditTaskDialog, DeleteTaskDialog)
- **Stores (Pinia)** — Gerenciamento de estado global com persistência automática (auth store com tokens, lists store com CRUD, tasks store com CRUD dual mock/API)
- **Services** — Camada de serviços HTTP com axios: interceptor de Bearer token com queueing automático de refresh em 401, serviços dedicados por domínio (auth, tasklists, tasks)
- **Router** — Navegação SPA com lazy-loading de rotas e guards de autenticação

---

## Estrutura de Pastas Detalhada

```
├── AGENTS.md                          # Instruções para agentes de IA
├── misc/docs/SPECIFICATION.md         # Especificação original do desafio
├── misc/docs/BACKLOG.md               # Backlog com bugs, segurança, features, qualidade
├── misc/docs/backend/auth-flow.md     # Documentação detalhada do fluxo JWT
├── README.md                          # Este arquivo
├── .agents/skills/                    # Skills para agentes de IA (23 skills)
├── skills-lock.json                   # Lock de versões das skills
├── jtech-tasklist-backend/
│   ├── build.gradle                   # Dependências e configuração de build
│   ├── docker-compose.yml             # PostgreSQL para dev local (somente banco)
│   ├── mockserver/                    # Mockserver Flask para testes de integração
│   └── src/
│       ├── main/
│       │   ├── java/br/com/jtech/tasklist/
│       │   │   ├── StartTasklist.java # Main class
│       │   │   ├── adapters/
│       │   │   │   ├── input/         # Controllers REST + DTOs de request/response
│       │   │   │   └── output/        # Implementações JPA + mappers domínio↔entidade
│       │   │   ├── application/
│       │   │   │   ├── core/          # Domínios puros e casos de uso (regras de negócio)
│       │   │   │   └── ports/         # Interfaces de entrada e saída (contratos)
│       │   │   ├── config/infra/audit/     # AuditorAwareImpl, JpaAuditingConfig
│       │   │   ├── config/infra/handlers/  # GlobalExceptionHandler
│       │   │   ├── config/infra/listeners/ # ReadyEventListener
│       │   │   ├── config/infra/utils/     # Jsons, GenId
│       │   │   └── config/              # Beans, swagger, utils
│       │   └── resources/
│       │       ├── application.yml     # Configuração principal
│       │       └── db/migration/       # Migrações Flyway (V###__description.sql)
│       └── test/
│           └── resources/             # application-test.properties (H2)
│
└── jtech-tasklist-frontend/
    ├── package.json                   # Dependências e scripts npm
    ├── vite.config.ts                 # Configuração do Vite
    ├── tsconfig.json                  # TypeScript config (app, node, vitest)
    ├── eslint.config.ts               # ESLint flat config
    ├── .prettierrc                    # Configuração do Prettier
    ├── env.d.ts                       # Declarações de tipos globais
    ├── index.html                     # Entry point HTML
    └── src/
        ├── main.ts                    # Bootstrap: Pinia + Router
        ├── App.vue                    # Componente raiz
        ├── assets/                    # CSS global e imagens
        ├── components/                # Componentes reutilizáveis + __tests__
        ├── router/                    # Configuração de rotas
        ├── services/                  # Serviços HTTP (axios + interceptores)
        ├── stores/                    # Stores Pinia (estado global)
        └── views/                     # Páginas da aplicação
```

---

## Como Rodar Localmente

### Pré-requisitos

- **Java 25** (toolchain configurada no Gradle)
- **Node.js** `^20.19.0 || >=22.12.0`
- **PostgreSQL** (via Docker Compose em `docker-compose.yml`, ou H2 para testes)

### Backend

Primeiro, inicie o PostgreSQL via Docker Compose:

```bash
cd jtech-tasklist-backend
docker compose up -d
docker compose ps   # Verificar se está pronto
```

Depois, inicie a aplicação:

```bash
./gradlew bootRun
```

O servidor inicia em uma porta aleatória por padrão. Defina `PORT` para fixar:

```bash
PORT=8080 ./gradlew bootRun
```

**Variáveis de ambiente relevantes:**

| Variável | Padrão | Descrição |
|---|---|---|
| `PORT` | `0` (aleatória) | Porta do servidor |
| `DS_URL` | `localhost` | Host do PostgreSQL |
| `DS_PORT` | `5432` | Porta do PostgreSQL |
| `DS_DATABASE` | `jtech_tasklist` | Nome do banco |
| `DS_USER` | `postgres` | Usuário do banco |
| `DS_PASS` | `postgres` | Senha do banco |
| `JWT_SECRET` | *(apenas dev)* | Chave HMAC para assinatura JWT (min 256-bit) — definido em `application-dev.yml` para dev; **obrigatório via env var em outros ambientes** |
| `JWT_ACCESS_EXPIRATION` | `900000` (15 min) | Duração do access token em ms |
| `JWT_REFRESH_EXPIRATION` | `604800000` (7 dias) | Duração do refresh token em ms |

Acessar Swagger: http://localhost:8080/doc/tasklist/v1/api.html

Para parar o banco ao finalizar:
```bash
docker compose down
# Para remover os dados (volume):
docker compose down -v
```

### Frontend

```bash
cd jtech-tasklist-frontend
npm install
npm run dev
```

Acessar: http://localhost:5173

### Rodando Tudo de Uma Vez

```bash
./misc/scripts/dev.sh
```

Inicia backend (porta 8080) e frontend (porta 5173) simultaneamente. Ctrl+C para parar ambos.

**Variáveis de ambiente relevantes:**

| Variável | Padrão | Descrição |
|---|---|---|
| `VITE_AUTH_MODE` | `mock` | Modo de autenticação: `mock` (simulado, sem backend) ou `api` (real, com backend) |
| `VITE_API_BASE_URL` | `http://localhost:8080` | URL base do backend para chamadas API |

---

## Como Rodar os Testes

### Backend (JUnit 5 + AssertJ)

```bash
cd jtech-tasklist-backend
./gradlew test                      # Executa todos os testes
./gradlew build                     # Build completo com testes
./gradlew jacocoTestReport          # Relatório de cobertura
```

### Frontend (Vitest)

```bash
cd jtech-tasklist-frontend
npm run test:unit                   # Testes unitários
npm run type-check                  # Verificação de tipos
npm run lint                        # Análise estática
npm run build                       # Type-check + build
```

---

## Pontos de Atenção Conhecidos

Problemas e lacunas identificados durante revisão de código. O backlog completo com prioridades e dependências está em [`misc/docs/BACKLOG.md`](./misc/docs/BACKLOG.md).

### Bugs

| ID | Resumo | Impacto |
|---|---|---|
| `tasklist-delete-is-hard-delete-no-cascade` | Exclusão de tasklist faz `DELETE` real (hard delete) em vez de soft delete como tasks. FK sem `ON DELETE` causa `500` ao excluir tasklist que possui tarefas. | Perda de trilha de auditoria + erro 500 para o usuário |
| `task-title-uniqueness-case-mismatch` | Frontend compara títulos case-insensitively, backend/DB compara case-sensitively. Mesma tarefa pode ser duplicada ou bloqueada dependendo do modo. | Validação inconsistente |
### Segurança

| ID | Resumo | Recomendação |
|---|---|---|
| ~~`jwt-secret-default-committed`~~ | ✅ Resolvido — default removido de `application.yml`, movido para `application-dev.yml` | Falha rápida em ambientes sem `JWT_SECRET` configurado |
| `actuator-fully-exposed` | `management.endpoints.web.exposure.include: '*'` + `permitAll()` em `/actuator/**` expõe todos os endpoints sem autenticação. | Restringir a `health,info` ou exigir role admin |
| ~~`exception-handler-leaks-internal-messages`~~ | ✅ Resolvido — `debugMessage` removido do `ApiError`; exceções não capturadas são logadas no servidor | Nunca mais expõe detalhes internos ao cliente |

### Pendências de Stack

- **Vuetify / Material Design** — Único item obrigatório da especificação (`SPECIFICATION.md`) ainda não implementado. A UI atual funciona mas não usa a biblioteca de componentes exigida.

---

## Decisões Técnicas Aprofundadas

### Arquitetura Hexagonal (Ports & Adapters)

O projeto foi herdado com arquitetura hexagonal, que isola o núcleo de negócio de frameworks e bancos de dados. As regras de domínio em `application/core/domains/` não possuem nenhuma anotação JPA ou Spring — são POJOs puros testáveis sem infraestrutura. A inversão de dependência é garantida porque os adaptadores de saída implementam portas definidas pelo núcleo, e não o contrário.

### Composition API + TypeScript

O frontend utiliza Composition API com `<script setup>` para aproveitar inferência de tipos nativa do TypeScript sem decorators. A reatividade fina do Vue 3 (via `ref`/`reactive`) permite que o Pinia gerencie estado compartilhado entre componentes sem prop drilling.

### Separação Domínio vs Persistência

`TasklistEntity` (entidade JPA com anotações `@Entity`, `@Table`) é distinta de `Tasklist` (domínio puro). O adapter de saída (`CreateTasklistAdapter`) realiza o mapeamento entre ambas, permitindo evolução independente do schema de banco e do modelo de domínio.

### Autenticação JWT com Refresh Token

A autenticação segue o padrão **access token + refresh token**:

- `POST /api/v1/auth/register` — Cadastro com nome, email e senha (bcrypt). Retorna access + refresh tokens.
- `POST /api/v1/auth/login` — Autenticação com email + senha. Retorna access + refresh tokens.
- `POST /api/v1/auth/refresh` — Rotação de refresh token. Recebe o refresh token atual, revoga-o e retorna um novo refresh token.

O **access token** (JWT stateless, 15 min) é validado em toda requisição a endpoints protegidos via assinatura HMAC — sem consulta ao banco. O **refresh token** (stateful, 7 dias) armazenado no banco permite renovação sem reenvio de credenciais e pode ser revogado individualmente.

A chave HMAC (`JWT_SECRET`) deve ser configurada via variável de ambiente em qualquer ambiente fora de desenvolvimento local.

### Modo Dual Mock/API no Frontend

O frontend suporta dois modos de operação (`VITE_AUTH_MODE`):

- **`mock`** — Armazenamento local (localStorage) sem necessidade de backend. Ideal para desenvolvimento isolado do frontend.
- **`api`** — Comunicação real com o backend via axios, com interceptor automático de Bearer token e queueing de refresh em 401.

Os dois modos são caminhos de código separados por método de store (não uma abstração compartilhada), o que significa que correções em um modo devem ser verificadas no outro.

---

## Status do Projeto

Projeto partiu de um skeleton mínimo.

### Implementado

- **Backend**: Spring Boot com estrutura hexagonal de pacotes, Swagger, exception handler, Actuator, **autenticação JWT com refresh token** (registro, login, refresh), CORS config, **validação de duplicatas** — tarefas (única por lista via constraint + use case) e nomes de tasklist (únicos por usuário, case-insensitive, via constraint + use case), **migrações Flyway** para evolução de schema
- **Frontend**: Vue 3 + Vite + Pinia + Vue Router + ESLint + Vitest (scaffold padrão), **autenticação com flag mock/api** (login e registro assíncronos com axios), tela de cadastro, **CRUD de listas com persistência** (criar, renomear, excluir com confirmação, navegação entre listas, sidebar), **CRUD de tarefas** (adicionar, editar, remover, marcar concluída com validação de duplicatas e campos obrigatórios, dual mode mock/API), **camada de serviços HTTP** com interceptor de refresh token

### Próximos passos

Ver [`misc/docs/BACKLOG.md`](./misc/docs/BACKLOG.md) — backlog organizado por categoria (bugs, segurança, features, qualidade) com dependências entre tarefas.
