# Seguridad — Estado actual y diseño futuro

> **Resumen ejecutivo**: la seguridad actual (validación JWT por servicio contra Keycloak) está **implementada y operativa**. El diseño futuro (patrón Phantom Token) está **completamente diseñado y documentado, pero NO implementado** — se activará cuando se decida hacer el overhaul de seguridad. **Decisión tomada**: mantener el modelo actual por ahora y avanzar con las demás características del laboratorio.

---

## 1. Seguridad actual (implementada)

### 1.1 Arquitectura

```
                    ┌────────────────────────────────────────────────────┐
                    │              Keycloak (IdP)                        │
                    │  realm: paideia · roles: STUDENT/TEACHER/ADMIN     │
                    └────────────────────────┬───────────────────────────┘
                                             │ emite JWT (login)
                                             ▼
                    ┌──────────────┐  Bearer JWT   ┌────────────────────┐
                    │   Cliente    │ ─────────────▶ │   API Gateway      │
                    └──────────────┘               │  (router puro)     │
                                                   └─────────┬──────────┘
                                                             │ enruta por path
        ┌────────────────────────────────────────────────────┼──────────────────────────────┐
        ▼                                                    ▼                              ▼
┌────────────────────┐                          ┌────────────────────┐          ┌────────────────────┐
│  course-service    │                          │  enrollment-service│          │  ... (6 servicios) │
│  valida JWT local  │                          │  valida JWT local  │          │  valida JWT local  │
│  contra Keycloak   │                          │  contra Keycloak   │          │  contra Keycloak   │
└────────────────────┘                          └────────────────────┘          └────────────────────┘
```

### 1.2 Flujo paso a paso

1. **Login**: el cliente se autentica en Keycloak y recibe un JWT con:
   - `sub` = UUID del usuario en Keycloak
   - `realm_access.roles` = roles del realm (STUDENT, TEACHER, ADMIN)
   - `email` = email del usuario (el client scope `email` está habilitado en el realm)
2. **Request**: el cliente envía `Authorization: Bearer <jwt>` al API Gateway.
3. **Ruteo**: el Gateway enruta por path (`/courses/**` → course-service, etc.). **No toca el token** — es un router puro.
4. **Validación local**: cada servicio es un resource server OAuth2. Valida el JWT contra el JWKS de Keycloak usando `issuer-uri` (configurado centralmente en el Config Server).
5. **Autorización**: cada `SecurityConfig` define políticas por endpoint usando roles extraídos de `realm_access.roles` (con prefijo `ROLE_`).
6. **Identidad**: los controllers acceden al JWT validado vía `@AuthenticationPrincipal Jwt` o `SecurityContext`.
7. **Servicio→servicio**: cuando un servicio llama a otro (enrollment → notification), **propaga su propio JWT** como Bearer.

### 1.3 Componentes y responsabilidades

| Componente | Rol en seguridad |
|---|---|
| **Keycloak** | Única fuente de verdad de identidad: email, password, roles. Emite y firma los JWT. |
| **API Gateway** | Router puro. No valida ni transforma tokens. |
| **Cada microservicio** | Resource server OAuth2: valida el JWT localmente y autoriza por roles. |
| **Config Server** | Centraliza `issuer-uri` (apunta a Keycloak) para todos los servicios. |
| **user-service** | Registro de usuarios (`POST /register`) + `GET /users/{id}`. **Sin protección** (ver 1.5). |

### 1.4 Configuración real (verificada en código)

**Config Server** (`platform/config-repo/application.yml`) — aplica a todos los servicios:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER:http://localhost:8090/realms/paideia}
```

**SecurityConfig típico** (ej. course-service) — el patrón se repite en 5 servicios:

```java
JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
http.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.GET, "/courses/**").permitAll()
        .requestMatchers(HttpMethod.POST, "/courses/**").hasRole("ADMIN")
        .anyRequest().authenticated())
        .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)));
```

**`extractAuthorities`** — duplicado idéntico en course, enrollment, payment, content y notification:

```java
if (jwt.getClaim("realm_access") instanceof Map<?, ?> realmAccess
        && realmAccess.get("roles") instanceof List<?> roles) {
    roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
}
```

### 1.5 Matriz de políticas por servicio (verificada en código)

| Servicio | Endpoint | Política |
|---|---|---|
| course-service | `GET /courses/**` | `permitAll` |
| course-service | `POST /courses/**` | `ADMIN` |
| course-service | resto | `authenticated` |
| enrollment-service | `POST /enrollments/**` | `STUDENT` |
| enrollment-service | resto | `authenticated` |
| payment-service | `GET /payments/**` | `ADMIN` |
| payment-service | `POST /payments` | `STUDENT` |
| payment-service | resto | `authenticated` |
| content-service | `GET /contents/**` | `STUDENT` o `ADMIN` |
| content-service | `POST /contents` | `ADMIN` |
| content-service | resto | `authenticated` |
| notification-service | `GET /notifications/**` | `ADMIN` |
| notification-service | `POST /notifications` | `STUDENT` |
| notification-service | resto | `authenticated` |
| **user-service** | `GET /users/{id}`, `POST /register` | **SIN protección** — no tiene `SecurityConfig` |


### 1.6 Comunicación servicio-a-servicio

- **enrollment-service → notification-service**: propaga el JWT del estudiante autenticado como Bearer (vía `RestClient`).
- El servicio receptor valida el token igual que si viniera del cliente (mismo `issuer-uri`).
- **Implicación**: el token del caller se reutiliza downstream sin scoping — el servicio destino ve los mismos roles y `sub` que el servicio origen.

### 1.7 Limitaciones conocidas

| # | Limitación | Consecuencia | Se resuelve con |
|---|---|---|---|
| L1 | `extractAuthorities` duplicado en 5 servicios | Código repetido, riesgo de drift entre servicios | common-security (futuro) |
| L2 | `sub` = UUID de Keycloak en todos lados | Servicios acoplados al IdP; migrar de IdP toca todo | id interno + phantom token (futuro) |
| L3 | Propagación del JWT del caller | Sin scoping: el token del estudiante se reutiliza downstream | Phantom token (futuro) |
| L4 | Gateway sin seguridad | No hay punto único de validación; cada servicio valida por su cuenta | Phantom token (futuro) |

---

## 2. Seguridad futura (diseñada, no implementada)

### 2.1 Patrón: Phantom Token

Patrón de **Curity** (usado por Auth0/Okta a escala). Idea central: el token externo de Keycloak **nunca cruza el Gateway**. El Gateway lo valida, resuelve el ID interno del usuario, y **mintea un token interno** de corta vida con claims normalizados. Los microservicios solo confían en el issuer interno.

```
ZONA EXTERNA
  Cliente ──(Bearer token Keycloak)──▶ Gateway
  Keycloak ◀──(validación firma/JWKS)── Gateway

ZONA DE BORDE
  Gateway:
    1. Valida token externo (firma, issuer, exp, aud)
    2. Cache Caffeine: (provider, sub) → UUID interno
    3. Miss → POST /internal/identities/resolve (client-credentials) → user-service
    4. Mintea token interno (sub=UUID interno, roles, email, TTL 300s)
    5. remove(Authorization) → set(Bearer interno)   ← anti token-smuggling
    6. Enruta por path

ZONA INTERNA
  user-service:
    - /internal/identities/resolve  → valida client-credentials (Keycloak)
    - /users/me                     → valida token interno (Gateway)
  Microservicios:
    - common-security valida token interno (JWKS del Gateway)
    - ArgumentResolver → UserContext(internalUserId, roles)
    - Lógica de negocio usa UserContext, nunca un Jwt
```

### 2.2 Los dos tokens

| | **Token externo (Keycloak)** | **Token interno (Gateway)** |
|---|---|---|
| Emisor | `http://localhost:8090/realms/paideia` | `http://api-gateway:8080` (issuer interno) |
| `sub` | UUID de Keycloak | **UUID interno** (de user-service) |
| Claims | `email`, `name`, `preferred_username`, `realm_access.roles` | `roles` (normalizado a raíz), `email` (copiado) |
| TTL | 3600s (1 hora) | 300s (5 min) |
| Firmado por | Keycloak | **El Gateway** (su clave privada) |
| Quién lo valida | Solo el Gateway | Todos los microservicios |
| Quién lo ve | Cliente + Gateway | Gateway + microservicios |

**Regla de oro**: el token externo nunca cruza el Gateway.

### 2.3 Flujo paso a paso

1. **Cliente → Gateway**: request con `Authorization: Bearer <token_externo>`.
2. **Gateway valida el token externo**: firma contra el JWKS de Keycloak (cacheado), issuer, expiración, audiencia. Inválido → 401.
3. **Gateway resuelve el ID interno**: consulta cache Caffeine `(provider, sub) → UUID interno`. Cache miss → llama a user-service.
4. **user-service resuelve el mapeo**: `POST /internal/identities/resolve` con `{provider, sub}` → busca en `external_identities` → devuelve `{userId, created}`. Si no existe, lazy-create (misma transacción).
5. **Gateway mintea el token interno**: `sub` = UUID interno, `roles` normalizados, `email` copiado, TTL 300s, firmado con su clave privada.
6. **Anti token-smuggling**: `headers.remove(AUTHORIZATION)` antes de `set(Bearer interno)`.
7. **Gateway enruta** al microservicio por path.
8. **Microservicio valida el token interno**: common-security valida contra el issuer interno (JWKS del Gateway), extrae `roles`.
9. **ArgumentResolver** construye `UserContext(internalUserId, roles)` y lo inyecta en el controller.
10. **Lógica de negocio** usa `UserContext` — nadie toca un `Jwt` directamente.
11. **Servicio→servicio**: se propaga el **token interno** (no el de Keycloak).
12. **Respuesta** de vuelta: servicio → Gateway → Cliente.

### 2.4 Componentes nuevos

| Componente | Responsabilidad |
|---|---|
| **common-security** (módulo compartido) | `UserContext`, `UserContextArgumentResolver`, `SecurityConfig` con claims normalizados (`roles` a raíz). Elimina el `extractAuthorities` duplicado (L1). |
| **TokenExchangeFilter** (en Gateway) | Valida externo → resuelve → mintea interno → reemplaza header. |
| **`GET /internal/jwks`** (en Gateway) | Publica la clave pública del issuer interno para que los servicios validen la firma. |
| **`POST /internal/identities/resolve`** (en user-service) | Mapea `(provider, sub)` → UUID interno. Protegido con client credentials. |
| **Tabla `external_identities`** (en user-service) | Mapeo IdP → usuario interno. `UNIQUE(provider, external_sub)`. |
| **Keystore local** (en Gateway) | Clave de firma del issuer interno (PKCS12). Vault entra en Fase F/G. |
| **Cache Caffeine** (en Gateway) | Cachea el resolve `(provider, sub) → UUID interno`, TTL 60s. Valkey entra cuando haya réplicas. |

### 2.5 Modelo de datos

```sql
-- users: perfil de dominio (Keycloak es dueño de email/password/roles)
CREATE TABLE users (
    id UUID PRIMARY KEY,              -- UUID interno (GenerationType.UUID)
    display_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- external_identities: mapeo IdP → usuario interno
CREATE TABLE external_identities (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL,    -- 'keycloak' | 'cognito' | ...
    external_sub VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    UNIQUE (provider, external_sub)   -- invariante: un sub de un IdP = un solo usuario interno
);
```

> **Nota de diseño**: se eliminó la columna `keycloak_id` de `users` por redundancia — el mapeo vive completo en `external_identities`. Evita drift entre dos fuentes de verdad.

### 2.6 Decisiones de diseño cerradas

| # | Tema | Decisión | Alternativas descartadas |
|---|---|---|---|
| D1 | JWKS interno | Gateway expone `GET /internal/jwks`; servicios usan `jwk-set-uri` + validación de issuer | JWKS estático por servicio (rotación = redeploy) |
| D2 | Clave de firma | Keystore local PKCS12 + rotación documentada (nuevo `kid`, ambos en JWKS durante la transición) | Vault (no está en el stack; entra en Fase F/G) |
| D3 | Cache del resolve | Caffeine en memoria, TTL 60s, máx 1000 entradas | Valkey (no está en el stack; entra con réplicas) |
| D4 | Anti token-smuggling | `remove(AUTHORIZATION)` antes de `set(Bearer interno)` + issuer interno como defensa en profundidad | — |
| D5 | Protección del resolve | OAuth2 client credentials (service account del client `api-gateway` en Keycloak) | mTLS (no está en el stack); shared secret (débil) |
| D6 | `external_identities` | `UNIQUE(provider, external_sub)`, creación atómica con el usuario (lazy-create) | Columna `keycloak_id` (redundante, riesgo de drift) |

### 2.7 Escenario de migración (test del diseño)

Cambiar Keycloak → Cognito:

| Qué cambia | Dónde |
|---|---|
| `issuer-uri` y claim-path del token **externo** | Solo en el Gateway |
| Filas nuevas en `external_identities` (`provider='cognito'`) | user-service |
| **Nada más** | Los 6 microservicios ni se enteran |

**Test de validez**: si migrar el IdP requiere tocar un solo microservicio de negocio, el diseño está mal. En este diseño no se toca ninguno.

### 2.8 Plan de implementación (3 fases)

| Fase | Contenido | Desbloquea |
|---|---|---|
| **1. common-security** | `UserContext`, `UserContextArgumentResolver`, `SecurityConfig` con `roles` a raíz | Elimina L1 (código duplicado) |
| **2. user-service** | Modelo nuevo (`users` + `external_identities`), `POST /internal/identities/resolve` (client-credentials), `GET/PATCH /users/me` (lazy-create) | Elimina L5 (user-service sin protección) |
| **3. Gateway** | `TokenExchangeFilter`, `GET /internal/jwks`, switch de los servicios al issuer interno | Elimina L2, L3, L4 (acoplamiento al IdP, scoping, gateway sin seguridad) |

---

## 3. Estado y decisiones

| Ítem | Estado |
|---|---|
| Seguridad actual (JWT por servicio vs Keycloak) | ✅ Implementada y operativa |
| Matriz de políticas por servicio | ✅ Verificada en código (sección 1.5) |
| user-service sin protección | ⚠️ Pendiente — se corrige en Fase B |
| Diseño Phantom Token | ✅ Cerrado y documentado (sección 2) |
| Implementación Phantom Token | ⏸️ **Diferida** — se activa cuando se decida el overhaul de seguridad |
| Prioridad actual | Avanzar características: fixes menores → user-service Fase B → verificación → Fase F (Docker/CI-CD) → Fase G (K8s) |