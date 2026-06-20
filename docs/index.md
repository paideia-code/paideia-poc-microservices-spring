# Índice

Índice de la carpeta `docs/`.

## Análisis (`analisis/`)

Contiene el análisis del negocio del laboratorio:

| Documento | Contenido |
|---|---|
| [01-contexto-y-alcance.md](analisis/01-contexto-y-alcance.md) | Contexto, flujos principales, actores y alcance. |
| [02-dominio.md](analisis/02-dominio.md) | Bounded contexts, entidades, estados y eventos de dominio. |
| [03-flujos.md](analisis/03-flujos.md) | Flujos principales, integraciones síncronas, eventos y seguridad por versión. |
| [04-reglas-de-negocio.md](analisis/04-reglas-de-negocio.md) | Reglas de negocio agrupadas por flujo principal, con servicio responsable. |
| [05-casos-de-uso.md](analisis/05-casos-de-uso.md) | Casos de uso esenciales agrupados por flujo principal. |
| [06-diseno-tecnico.md](analisis/06-diseno-tecnico.md) | Servicios, contratos y persistencia. |

## Versiones (`versions/`)

Ruta principal, lineal y acumulativa. 

Cada versión es una rama Git que incluye todo lo de la anterior.

El detalle completo de cada versión está en su propio documento.

| Versión | Rama | Descripción | Documento |
|---|---|---|---|
| v0 | `v0/analysis` | Análisis y diseño del problema de negocio | [versions/v0-analysis.md](versions/v0-analysis.md) |
| v1 | `v1/minimal` | Estructura base: 6 microservicios REST con persistencia propia | [versions/v1-minimal.md](versions/v1-minimal.md) |
| v2 | `v2/infrastructure` | API Gateway, Service Discovery, Config centralizada, Cache | [versions/v2-infrastructure.md](versions/v2-infrastructure.md) |
| v3 | `v3/resilience` | Circuit breaker, Retry con backoff, Timeout, Fallback | [versions/v3-resilience.md](versions/v3-resilience.md) |
| v4 | `v4/eda` | Event-driven, Apache Kafka, Saga pattern, Outbox | [versions/v4-eda.md](versions/v4-eda.md) |
| v5 | `v5/security` | OAuth2/OIDC, Keycloak, RBAC, Zero-trust | [versions/v5-security.md](versions/v5-security.md) |
| v6 | `v6/observability` | Trazas distribuidas, métricas y logs estructurados | [versions/v6-observability.md](versions/v6-observability.md) |
| v7 | `v7/deployment` | Docker, Kubernetes, GitHub Actions, CI/CD | [versions/v7-deployment.md](versions/v7-deployment.md) |
| v8 | `v8/platform` | Terraform, ArgoCD, Vault, secretos dinámicos opcionales | [versions/v8-platform.md](versions/v8-platform.md) |

## Alternativas (`alt/`)

Ramas que bifurcan de la ruta principal para explorar una tecnología o patrón diferente.

No son acumulativas entre sí.

Por ahora, las sugeridas son:

| Rama | Bifurca de | Tema | Documento |
|---|---|---|---|
| `alt/consul` | `v2.0.0` | Service Discovery + Config: Consul vs Eureka + Config Server | [alt/consul.md](alt/consul.md) |
| `alt/grpc` | `v2.0.0` | Comunicación interna: gRPC / Protobuf vs HTTP REST | [alt/grpc.md](alt/grpc.md) |
| `alt/graphql` | `v2.0.0` | API Gateway: GraphQL como BFF vs REST puro | [alt/graphql.md](alt/graphql.md) |
| `alt/rabbitmq` | `v4.0.0` | Mensajería: RabbitMQ vs Kafka | [alt/rabbitmq.md](alt/rabbitmq.md) |
| `alt/orchestration-saga` | `v4.0.0` | Coordinación: Saga orquestada con Temporal vs choreography | [alt/orchestration-saga.md](alt/orchestration-saga.md) |
| `alt/order-flow` | `v4.0.0` | Flujo de negocio: Saga extendida con order-service | [alt/order-flow.md](alt/order-flow.md) |
| `alt/cqrs` | `v4.0.0` | Modelo de lectura: CQRS con read model separado | [alt/cqrs.md](alt/cqrs.md) |
| `alt/jwt` | `v5.0.0` | Autenticación: auth-service propio con JWT vs Keycloak | [alt/jwt.md](alt/jwt.md) |
| `alt/service-mesh` | `v7.0.0` | Resiliencia de red: Istio/Linkerd vs Spring Cloud | [alt/service-mesh.md](alt/service-mesh.md) |
| `alt/platform-local` | `v7.0.0` | Plataforma sin cloud: Vault + ArgoCD + Terraform local | [alt/platform-local.md](alt/platform-local.md) |

## Notas de estudio (`notas/`)

Resúmenes temáticos sin atarse a una versión específica. Útiles como referencia rápida mientras se trabaja en cualquier rama.

| Documento | Tema |
|---|---|
| [notas/microservicios.md](notas/microservicios.md) | Qué son, cuándo usarlos, bounded contexts y patrones de comunicación. |
| [notas/clientes-http-spring.md](notas/clientes-http-spring.md) | RestClient, WebClient, Feign: diferencias y cuando usar cada uno. |
| [notas/cucumber-gherkin.md](notas/cucumber-gherkin.md) | Guía paso a paso para implementar tests BDD con Gherkin y Cucumber. |
| [notas/flyway.md](notas/flyway.md) | Migraciones de base de datos con Flyway. |
| [notas/qa.md](notas/qa.md) | Estrategia de pruebas para el laboratorio. |
| [notas/subida-archivos-http.md](notas/subida-archivos-http.md) | Comparación entre Base64 en JSON y multipart/form-data con MultipartFile. |
