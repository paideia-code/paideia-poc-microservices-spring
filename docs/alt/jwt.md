# alt/jwt — Autenticación con auth-service propio y JWT

| | |
|---|---|
| Rama | `alt/jwt` |
| Bifurca de | `v5.0.0` / `v5/security` |
| Diferencia clave | `auth-service` propio que emite JWT vs Keycloak como Identity Provider externo |

## Qué cambia respecto a v5

v5 delega toda la autenticación y gestión de identidad a **Keycloak**: Authorization Code Flow, tokens OIDC, gestión de usuarios y roles en la consola de Keycloak. Los microservicios validan el token usando el `jwks_uri` de Keycloak.

Esta alternativa construye un **`auth-service`** propio que:
- Expone `POST /auth/login` y `POST /auth/refresh`
- Emite JWT firmados con clave asimétrica (RS256)
- Gestiona usuarios en su propia base de datos

Los microservicios siguen validando JWT, pero la clave pública proviene del `auth-service`.

## Qué explora

- Estructura de un JWT: header, payload, signature
- Firma asimétrica (RS256): clave privada para firmar, pública para verificar
- `spring-security-oauth2-resource-server` configurado con clave pública estática
- Refresh tokens: emisión, rotación, revocación
- Diferencias de seguridad y mantenimiento respecto a un IdP dedicado

## Cuándo tiene sentido

- Para entender JWT desde cero sin la abstracción de un IdP completo
- Proyectos pequeños que no justifican operar Keycloak
- Cuando el control total sobre el proceso de autenticación es un requisito
