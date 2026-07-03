# JTech Tasklist

Sistema TODO List multi-usuário com arquitetura hexagonal no backend e componentização reativa no frontend. Solução fullstack para o desafio técnico Fullstack2 da JTech.

---

## Stack Tecnológica

### Backend (`jtech-tasklist-backend/`)

| Tecnologia | Versão | Justificativa |
|---|---|---|
| **Java** | 25 | LTS com suporte a padrões modernos de concorrência e performance |
| **Spring Boot** | 4.1.0 | Ecossistema maduro para aplicações enterprise com injeção de dependência nativa |
| **Spring Data JPA + Hibernate** | — | Mapeamento objeto-relacional robusto com suporte a locks otimistas e cache |
| **Springdoc OpenAPI** | 3.0.3 | Geração automática de documentação OpenAPI 3.0 sem acoplamento ao código |
| **Hibernate Validator** | 9.1.0.Final | Validação declarativa via Jakarta Bean Validation 3.1 |
| **Spring Actuator** | — | Métricas e health checks para observabilidade em produção |
| **PostgreSQL** | — | Banco relacional com suporte a JSONB, índices parciais e MVCC |
| **H2** | — | Banco em memória para testes de integração isolados |
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
| **Vitest** | 4.1.9 | Testes unitários compatíveis com ecossistema Vite |
| **ESLint** | 10.6.0 | Análise estática com regras específicas para Vue e TypeScript |
| **Prettier** | 3.9.4 | Formatação consistente sem debates de estilo |

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
- **`application/ports/`** — Interfaces que definem contratos: portas de entrada (input: o que o sistema faz) e portas de saída (output: dependências externas que o sistema precisa)
- **`adapters/input/`** — Controladores REST que traduzem requisições HTTP em chamadas aos casos de uso
- **`adapters/output/`** — Implementações concretas de repositórios JPA, mapeamento entre entidades de domínio e entidades de persistência
- **`config/`** — Configuração de beans, swagger, exception handlers globais e utilitários

### Frontend: Arquitetura Reativa com Composição

- **Views** — Páginas associadas a rotas (home, about)
- **Components** — Componentes reutilizáveis (HelloWorld, WelcomeItem)
- **Stores (Pinia)** — Gerenciamento de estado global (counter store de exemplo)
- **Router** — Navegação SPA com lazy-loading de rotas

---

## Estrutura de Pastas Detalhada

```
├── AGENTS.md                          # Instruções para agentes de IA
├── SPECIFICATION.md                   # Especificação original do desafio
├── README.md                          # Este arquivo
├── jtech-tasklist-backend/
│   ├── build.gradle                   # Dependências e configuração de build
│   ├── composer/                      # Docker Compose para serviços de apoio
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
│       │   │   └── config/            # Beans, swagger, exception handlers, utils
│       │   └── resources/             # application.yml, banner.txt
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
        ├── stores/                    # Stores Pinia (estado global)
        └── views/                     # Páginas da aplicação
```

---

## Como Rodar Localmente

### Pré-requisitos

- **Java 25** (toolchain configurada no Gradle)
- **Node.js** `^20.19.0 || >=22.12.0`
- **PostgreSQL** (ou H2 para testes)

### Backend

```bash
cd jtech-tasklist-backend
./gradlew bootRun
```

O servidor inicia em uma porta aleatória por padrão. Defina `PORT` para fixar:

```bash
PORT=8080 ./gradlew bootRun
```

Acessar Swagger: http://localhost:8080/doc/tasklist/v1/api.html

### Frontend

```bash
cd jtech-tasklist-frontend
npm install
npm run dev
```

Acessar: http://localhost:5173

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

## Decisões Técnicas Aprofundadas

### Arquitetura Hexagonal (Ports & Adapters)

O projeto foi herdado com arquitetura hexagonal, que isola o núcleo de negócio de frameworks e bancos de dados. As regras de domínio em `application/core/domains/` não possuem nenhuma anotação JPA ou Spring — são POJOs puros testáveis sem infraestrutura. A inversão de dependência é garantida porque os adaptadores de saída implementam portas definidas pelo núcleo, e não o contrário.

### Composition API + TypeScript

O frontend utiliza Composition API com `<script setup>` para aproveitar inferência de tipos nativa do TypeScript sem decorators. A reatividade fina do Vue 3 (via `ref`/`reactive`) permite que o Pinia gerencie estado compartilhado entre componentes sem prop drilling.

### Separação Domínio vs Persistência

`TasklistEntity` (entidade JPA com anotações `@Entity`, `@Table`) é distinta de `Tasklist` (domínio puro). O adapter de saída (`CreateTasklistAdapter`) realiza o mapeamento entre ambas, permitindo evolução independente do schema de banco e do modelo de domínio.

---

## Status do Projeto

Projeto partiu de um skeleton mínimo. Nenhum requisito funcional da SPECIFICATION.md foi implementado ainda.

### O que já veio no skeleton
- **Backend**: Spring Boot com estrutura hexagonal de pacotes, Swagger, exception handler, Actuator
- **Frontend**: Vue 3 + Vite + Pinia + Vue Router + ESLint + Vitest (scaffold padrão)

### O que precisa ser implementado (SPECIFICATION.md)

#### Backend
- `POST /auth/register` — cadastro com bcrypt e validação de email único
- `POST /auth/login` — JWT + refresh token
- `POST /tasks` — criar tarefa vinculada ao usuário autenticado
- `GET /tasks` — listar tarefas do próprio usuário
- `GET /tasks/{id}` — buscar tarefa com validação de propriedade
- `PUT /tasks/{id}` — atualizar com controle de acesso
- `DELETE /tasks/{id}` — remover com validação de proprietário
- Autorização JWT em todas as rotas
- Testes unitários (serviços) e de integração (endpoints)

#### Frontend (autenticação simulada — não consome backend)
- Tela de login com validação de campos não vazios
- Mock de autenticação: qualquer credencial válida redireciona para a app
- Persistência de sessão no Pinia
- Múltiplas listas de tarefas com CRUD completo
- Navegação entre listas
- CRUD de tarefas dentro de cada lista
- Validações (duplicatas, campos obrigatórios)
- Vue Router separando auth → app
- Guards de rota para usuários não autenticados
- Vuetify (Material Design) — instalar e migrar UI

---

## Melhorias e Roadmap

Todas as melhorias abaixo correspondem diretamente aos requisitos da SPECIFICATION.md, organizadas por ordem de implementação.

### Fase 1 — Autenticação
1. **Backend**: Spring Security + JWT + bcrypt — endpoints `/auth/register` e `/auth/login`
2. **Frontend**: Tela de login com validação, mock auth, sessão no Pinia, guards de rota

### Fase 2 — Funcionalidade Core
3. **Backend**: CRUD de tarefas com validação de propriedade (`/tasks/*`)
4. **Frontend**: Múltiplas listas, CRUD de listas e tarefas, navegação entre listas
5. **Vuetify**: Instalar e migrar toda UI para Material Design

### Fase 3 — Qualidade
6. **Testes backend**: Unitários (serviços com Mockito) + integração (endpoints com Spring Test)
7. **Testes frontend**: Vitest para stores, componentes e views

### Futuro (pós-entrega)
8. Docker Compose com PostgreSQL + app + mockserver
9. CI/CD com build + testes + publish no Nexus
10. Persistência offline (LocalStorage + Service Worker)
