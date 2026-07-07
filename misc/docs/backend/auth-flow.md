# Fluxo de Autenticação JWT

## Visão Geral

O backend implementa autenticação stateless via **JWT access token** + **refresh token** rotativo. Nenhum estado de sessão é mantido no servidor — o access token carrega todas as informações necessárias para autorização.

---

## Endpoints

| Método | Rota | Autenticação | Descrição |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | ❌ | Cadastro de usuário |
| `POST` | `/api/v1/auth/login` | ❌ | Login (email + senha) |
| `POST` | `/api/v1/auth/refresh` | ❌ | Rotação de refresh token + novo access token |

---

## Fluxo de Registro

```
Client                          Server
  │                                │
  │  POST /api/v1/auth/register    │
  │  { name, email, password }     │
  │ ─────────────────────────────> │
  │                                ├─ Valida Bean Validation (nome, email, senha)
  │                                ├─ Verifica unicidade de email
  │                                ├─ Gera hash bcrypt da senha
  │                                ├─ Salva User no banco
  │                                ├─ Gera refresh token (UUID) no banco
  │                                ├─ Gera access token JWT (assinado HMAC)
  │ 201 { accessToken,             │
  │       refreshToken,            │
  │       tokenType: "Bearer" }    │
  │ <───────────────────────────── │
```

**Validações:**
- `name`: obrigatório, 1–255 caracteres
- `email`: obrigatório, formato válido, único no banco
- `password`: obrigatório, 6–255 caracteres

---

## Fluxo de Login

```
Client                          Server
  │                                │
  │  POST /api/v1/auth/login       │
  │  { email, password }           │
  │ ─────────────────────────────> │
  │                                ├─ Busca User por email
  │                                ├─ Verifica bcrypt(password)
  │                                ├─ Gera refresh token (UUID) no banco
  │                                ├─ Gera access token JWT (assinado HMAC)
  │ 200 { accessToken,             │
  │       refreshToken,            │
  │       tokenType: "Bearer" }    │
  │ <───────────────────────────── │
```

**Erros:**
- Email inexistente ou senha incorreta → `401 Unauthorized`

---

## Fluxo de Requisição Autenticada

```
Client                          Server
  │                                │
  │  GET /api/v1/tasks             │
  │  Authorization: Bearer <jwt>   │
  │ ─────────────────────────────> │
  │                                ├─ JwtAuthenticationFilter:
  │                                │   1. Extrai token do header Authorization
  │                                │   2. Valida assinatura HMAC
  │                                │   3. Verifica expiração
  │                                │   4. Extrai userId + role do payload
  │                                │   5. Seta SecurityContext (stateless)
  │                                ├─ Controller processa requisição
  │ <───────────────────────────── │
```

O filtro `JwtAuthenticationFilter` (estende `OncePerRequestFilter`) executa antes do `UsernamePasswordAuthenticationFilter`. Rotas públicas (`/api/v1/auth/**`, `/doc/**`, `/actuator/**`) são ignoradas pela configuração de segurança.

---

## Fluxo de Refresh

```
Client                          Server
  │                                │
  │  POST /api/v1/auth/refresh     │
  │  { refreshToken: "<token>" }   │
  │ ─────────────────────────────> │
  │                                ├─ Busca refresh token no banco
  │                                ├─ Valida: não expirado, não revogado
  │                                ├─ Revoga token atual
  │                                ├─ Gera novo refresh token
  │                                ├─ Gera novo access token JWT
  │ 200 { accessToken,             │
  │       refreshToken,            │
  │       tokenType: "Bearer" }    │
  │ <───────────────────────────── │
```

**Erros:**
- Refresh token inválido, expirado ou revogado → `401 Unauthorized`

O refresh token é **rotativo**: a cada uso, o anterior é revogado e um novo é gerado. Isso limita a janela de ataque em caso de vazamento. Um novo access token também é emitido a cada refresh.

---

## Estrutura do Access Token (JWT)

```json
{
  "sub": "uuid-do-usuario",
  "email": "usuario@email.com",
  "role": "ROLE_USER",
  "type": "access",
  "iat": 1700000000,
  "exp": 1700000900
}
```

- **Algoritmo**: HMAC-SHA256
- **Payload**: `sub` (userId), `email`, `role`, `type` (access/refresh), `iat`, `exp`
- **Expiração**: configurável via `JWT_ACCESS_EXPIRATION` (padrão 15 min)
- **Type claim**: filtro rejeita tokens sem `type: "access"` — refresh tokens (UUID) nunca passam pelo filtro JWT
- **Stateless**: servidor não armazena o token — validação apenas por assinatura

---

## Estrutura do Refresh Token

- **Formato**: UUID v4 sem hífens (`550e8400e29b41d4a716446655440000`)
- **Armazenamento**: tabela `refresh_tokens` no PostgreSQL
- **Expiração**: configurável via `JWT_REFRESH_EXPIRATION` (padrão 7 dias)
- **Stateful**: servidor valida existência, expiração e flag `revoked` no banco

---

## Configuração via Variáveis de Ambiente

| Variável | Padrão | Obrigatório | Descrição |
|---|---|---|---|
| `JWT_SECRET` | *(padrão no yml — apenas dev)* | ✅ Produção | Chave HMAC para assinatura (min 256-bit) |
| `JWT_ACCESS_EXPIRATION` | `900000` (15 min) | ❌ | Duração do access token em ms |
| `JWT_REFRESH_EXPIRATION` | `604800000` (7 dias) | ❌ | Duração do refresh token em ms |

**⚠️ A chave `JWT_SECRET` deve ser uma string de no mínimo 32 caracteres (256 bits para HMAC-SHA256). O valor padrão é apenas para desenvolvimento local — **nunca utilize em produção**.**

---

## Segurança

- **Senhas**: hash bcrypt (implementado via `BCryptPasswordEncoder` do Spring Security)
- **Access token**: assinado HMAC-SHA256, sem armazenamento no servidor
- **Refresh token**: armazenado no banco, revogável individualmente, rotação a cada uso
- **CSRF**: desabilitado (API stateless, sem cookies de sessão)
- **CORS**: `CorsConfig` permite apenas `http://localhost:5173` (hardcoded) — ver `config-externalization-hygiene` no backlog para externalização
- **Endpoint público**: apenas `/api/v1/auth/**`, `/doc/**`, `/actuator/**` — demais exigem token

---

## Camadas (Hexagonal)

```
[AuthController]
    → RegisterUserInputGateway (porta de entrada)
        → RegisterUserUseCase
            → UserOutputGateway (porta de saída)
                → UserAdapter → UserRepository (JPA)

⚠️ Nota: atualmente AuthController.register() chama tokenOutputGateway
e refreshTokenOutputGateway diretamente, sem passar pelo use case
(diferente de login/refresh). Ver register-flow-bypasses-usecase-layer
no backlog.

[AuthController]
    → LoginInputGateway (porta de entrada)
        → LoginUseCase
            → UserOutputGateway
            → RefreshTokenOutputGateway
            → TokenOutputGateway
                → JwtTokenAdapter → JwtService
            → PasswordHasherOutputGateway
                → PasswordHasherAdapter → BCryptPasswordEncoder

[AuthController]
    → RefreshTokenInputGateway (porta de entrada)
        → RefreshUseCase
            → RefreshTokenOutputGateway
            → UserOutputGateway
            → TokenOutputGateway
```

- `User` (domínio puro) e `UserEntity` (JPA) são classes separadas — mapeamento nos adapters
- Casos de uso em `application/core/` têm **zero** imports de Spring Security ou JJWT
- Portas de saída em `application/ports/output/` definem contratos; adapters em `adapters/output/` implementam
- `AuditorAwareImpl` extrai o userId do `SecurityContextHolder` para auditoria JPA
