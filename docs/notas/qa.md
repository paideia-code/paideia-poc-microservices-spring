# QA en el laboratorio

QA no es solo probar al final. En un sistema de microservicios, QA ayuda a definir qué significa calidad antes de escribir código: contratos claros, datos controlados, casos borde, automatización, trazabilidad de defectos y confianza para cambiar el sistema versión tras versión.

## Qué hace QA

En un equipo real, QA suele cubrir:

- Análisis de negocio: detectar ambigüedades, reglas faltantes y casos borde.
- Estrategia de pruebas: decidir que se prueba unitariamente, que se prueba con integración y que se deja para end-to-end.
- Diseño de casos: happy path, errores, límites, permisos, concurrencia, datos inválidos y regresión.
- Automatización: pruebas en pipeline, colecciones Postman/Newman, tests de contrato y suites de integración.
- Calidad no funcional: performance básica, resiliencia, observabilidad, seguridad y mantenibilidad.
- Feedback: reportar defectos con pasos reproducibles, evidencia y severidad clara.

## Piramide de pruebas para este lab

| Nivel | Objetivo | Herramientas recomendadas |
|---|---|---|
| Unitarias | Reglas de negocio sin Spring completo ni base real | JUnit 5, AssertJ, Mockito |
| Slices web/data | Validar controller, validación y repositorios acotados | Spring Boot Test, MockMvc, DataJpaTest |
| Integración | Servicio + base real + migraciones | Testcontainers, Flyway, MongoDB container |
| Contrato HTTP | Verificar compatibilidad entre servicios | WireMock, Spring Cloud Contract opcional |
| End-to-end local | Flujo completo con servicios vivos | Postman/Newman, scripts locales |
| BDD | Escenarios legibles negocio-tecnología | Cucumber, solo cuando el dominio lo justifique |
| Mutación | Medir si los tests detectan cambios pequenos en la lógica | PIT, cuando ya exista una suite estable |

## Librerías evaluadas

| Librería | Vale la pena | Uso en este lab |
|---|---|---|
| JUnit 5 | Si | Base de todos los tests Java. |
| AssertJ | Si | Assertions expresivas y legibles. Preferir sobre Hamcrest para unit tests. |
| Hamcrest | Opcional | Útil si ya aparece en matchers de MockMvc; no lo pondria como foco principal. |
| Mockito | Si | Mocking de collaborators en unit tests. Ya aplica en v1. |
| Awaitility | Si, desde v4 | Esperar condiciones asíncronas: eventos Kafka, retries, procesamiento eventual. |
| WireMock | Si, desde v2/v3 | Mockear servicios HTTP externos y probar clientes con errores, latencia y respuestas contractuales. |
| Testcontainers | Si, desde v1/v2 | PostgreSQL/MongoDB/Kafka reales y desechables para integración. Muy valioso para microservicios. |
| Cucumber | Opcional | Bueno para BDD si queremos escenarios de negocio. No usarlo para todo. |
| PIT Mutation Testing | Opcional desde v2/v3 | Cambia pequeñas partes del código y verifica si los tests fallan. Sirve para evaluar fuerza de la suite, no para reemplazar tests. |
| Wiser | Opcional tardío | SMTP en memoria. Solo si `notification-service` envía email real. Alternativa común: GreenMail. |
| MemoryFileSystem | Opcional tardío | Simular filesystem. Poco relevante si en v7 usamos MinIO/S3. |
| PowerMock | No recomendado | Evitarlo. Fuerza mal diseño y choca con JUnit/Mockito moderno. Mejor refactorizar código testeable. |

## Roadmap de QA por versión

| Versión | Foco QA |
|---|---|
| v1 | Unit tests de servicios, validation tests de controllers, Postman para flujo manual. |
| v2 | Tests de gateway/routing y service discovery con perfiles locales. |
| v3 | Tests de resiliencia: timeout, retry, circuit breaker, idempotencia. WireMock entra fuerte. |
| v4 | Tests asíncronos de Kafka con Awaitility + Testcontainers. |
| v5 | Tests de seguridad: JWT válido/inválido, estudiante propietario y endpoints protegidos. |
| v6 | Validar métricas, logs y trace propagation básica. |
| v7 | Integración con MinIO/MongoDB y smoke tests de Docker/Kubernetes local. |
| v8 | Pipeline, GitOps, secretos y pruebas de despliegue. |

## Orden recomendado

1. JUnit 5 + AssertJ.
2. Mockito.
3. Spring Boot Test slices (`@WebMvcTest`, `@DataJpaTest`, `MockMvc`).
4. Testcontainers.
5. WireMock.
6. Awaitility.
7. Cucumber, solo si queremos practicar BDD con escenarios de negocio.
8. Pruebas de mutación, cuando la suite ya sea estable.

Ese orden maximiza aprendizaje aplicable sin introducir herramientas antes de tener un problema real que resolver.

## Cucumber en este proyecto

Cucumber no reemplaza los unit tests ni los tests de integración. Su valor está en conectar análisis de producto con ejemplos ejecutables:

1. `docs/index.md` resume el producto y `docs/analysis/` define reglas y casos de uso en orden numerado.
2. `features/` expresa ejemplos en lenguaje de negocio.
3. Las step definitions, cuando existan, conectan esos ejemplos con código.

Usarlo bien significa elegir pocos escenarios importantes: registro de estudiante, inscripción aprobada, curso no disponible, consulta de recursos propios desde v5, acceso a contenido comprado y notificación de confirmación. Usarlo para cada validación pequeña termina duplicando tests y haciendo lenta la suite.

## Pruebas de mutación

Las pruebas de mutación modifican automáticamente el código de producción: por ejemplo cambian `>=` por `>`, eliminan una condición o alteran un retorno. Luego ejecutan los tests.

- Si los tests fallan, el mutante fue detectado.
- Si los tests pasan, hay una zona de lógica que no esta bien cubierta.

Para este laboratorio, PIT sería útil después de tener buenas pruebas unitarias en servicios como `payment-service`, `course-service` y `enrollment-service`. No conviene introducirlo antes de estabilizar la suite base.
