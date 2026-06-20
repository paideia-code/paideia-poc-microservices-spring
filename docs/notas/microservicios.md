# Microservicios

## ¿Qué son?

Un microservicio es una unidad de despliegue independiente que modela un bounded context del negocio, expone una API bien definida y gestiona su propia persistencia.

La arquitectura de microservicios no es una tecnología nueva: es la codificación de un conjunto de decisiones de diseño orientadas a maximizar la **independencia entre equipos y componentes**.

### Breve historia

| Período | Contexto |
|---|---|
| **Años 90 – 2000s** | SOA (Service-Oriented Architecture) propone servicios desacoplados, pero los implementa con ESBs (Enterprise Service Bus) pesados y protocolos como SOAP/WSDL. El acoplamiento se mueve al middleware. |
| **2006–2012** | Netflix, Amazon y Google fragmentan sus monolitos para escalar de forma independiente. Cada equipo despliega su servicio sin coordinar con los demás. El concepto existe antes de tener nombre. |
| **2014** | Martin Fowler y James Lewis publican el artículo ["Microservices"](https://martinfowler.com/articles/microservices.html) que acuña y define el término. Se codifican las características: despliegue independiente, organización en torno a capacidades de negocio, descentralización de datos. |
| **2015–2019** | Spring Cloud, Docker y Kubernetes hacen viable la arquitectura para equipos medianos. El ecosistema explota. |
| **2020–hoy** | Madurez: la comunidad reconoce que los microservicios no son siempre la respuesta. El movimiento "modular monolith" y los enfoques híbridos ganan relevancia. |

---

## ¿Cuándo implementarlos?

La pregunta no es "¿son buenos los microservicios?" sino "¿justifican su coste en este contexto?".

### Criterios a favor

| Criterio | Explicación |
|---|---|
| Equipos grandes y autónomos | Conway's Law: la arquitectura del sistema refleja la estructura de comunicación de la organización. Si hay 5 equipos de 6 personas, 5 servicios evitan que se bloqueen entre sí. |
| Necesidad de escalado diferenciado | El módulo de búsqueda recibe 100x más tráfico que el de facturación. Con microservicios, se escalan de forma independiente. |
| Ciclos de despliegue independientes | Actualizar cursos no debe requerir redesplegar el módulo de pagos. |
| Aislamiento de fallos | Un bug en `notification-service` no derriba `enrollment-service`. |
| Heterogeneidad tecnológica | Un servicio de ML en Python puede convivir con servicios Java/Go sin forzar una stack uniforme. |

### Criterios en contra (cuándo NO)

| Situación | Recomendación |
|---|---|
| Equipo pequeño (< 5 personas) | El overhead operativo de gestionar 7+ servicios con sus BDs, pipelines y monitoreo supera el beneficio. |
| Dominio no explorado | Si no se conocen bien las fronteras del negocio, partir demasiado pronto produce microservicios mal delimitados que luego hay que fusionar ("distributed monolith"). |
| Startup en fase 0 | La velocidad de iteración es la prioridad. Un monolito bien estructurado permite pivotar más rápido. Conocido como **monolith-first approach** (Martin Fowler). |
| Bajo tráfico y sin requerimientos de escala independiente | La complejidad extra no aporta nada. |

> **"Microservices premium"** (Sam Newman): los microservicios son una solución compleja a problemas de escala y organización. Si no tienes esos problemas, pagas el precio sin el beneficio.

---

## Bounded Contexts

El concepto central de DDD (Domain-Driven Design) que justifica cómo partir un sistema.

Un bounded context es **el perímetro dentro del cual un modelo de dominio tiene significado coherente**. Fuera de ese perímetro, las mismas palabras pueden significar cosas diferentes.

### Ejemplo: "Curso" en este sistema

| Contexto | Qué es "Curso" |
|---|---|
| `course-service` | Entidad con título, precio y estado (DRAFT/PUBLISHED/ARCHIVED). Es la fuente de verdad. |
| `enrollment-service` | Solo necesita saber si el curso existe y está disponible para inscripción. Guarda `courseId` como referencia, no una copia completa. |
| `content-service` | Gestiona los archivos y videos asociados al curso. Su modelo incluye metadatos de storage, no precios. |
| `notification-service` | Solo usa datos mínimos del curso y estudiante para componer el mensaje. |

Cuatro contextos, cuatro modelos distintos del mismo concepto. Si compartiéramos un único modelo de `Curso` entre los cuatro, cualquier cambio en ese modelo afectaría a los cuatro contextos.

### Cómo identificar bounded contexts

1. **Por capacidad de negocio**: ¿qué hace el negocio? Cursos, inscripción, pagos, notificaciones… cada capacidad suele ser un candidato a servicio.
2. **Por cambio independiente**: ¿qué cambia junto? Si `course-service` y `enrollment-service` siempre se modifican juntos, probablemente son un solo contexto mal dividido.
3. **Por propiedad de equipo**: ¿qué equipo puede desplegar sin coordinar con otro? La frontera del servicio es la frontera del equipo.
4. **Por datos**: cada servicio es propietario de sus tablas. Nunca dos servicios comparten la misma base de datos. Si necesitan datos del otro, los piden vía API o los replican en su propia BD con consistencia eventual.

---

## Patrones de comunicación

La elección entre comunicación síncrona y asíncrona es una de las decisiones más importantes en microservicios.

### Síncrona (REST, gRPC)

El cliente espera la respuesta antes de continuar.

| | REST / HTTP | gRPC / Protobuf |
|---|---|---|
| Protocolo | HTTP/1.1, JSON | HTTP/2, binario |
| Contrato | OpenAPI (opcional) | `.proto` (obligatorio) |
| Uso típico | APIs externas, simplicidad | Llamadas internas de alta frecuencia |
| Acoplamiento temporal | Sí — el destino debe estar disponible | Sí |

**Cuándo usar REST síncrono:**
- Cuando el cliente necesita la respuesta inmediatamente para continuar (validar que el curso existe antes de inscribir)
- Comunicación con clientes externos (browser, móvil)
- Cuando la simplicidad operativa importa más que el rendimiento

### Asíncrona (Kafka, RabbitMQ)

El productor publica un evento y no espera respuesta. El consumidor lo procesa cuando puede.

| | Kafka | RabbitMQ |
|---|---|---|
| Modelo | Log distribuido (pull) | Colas con exchanges (push) |
| Retención | Configurable (días, semanas) | Hasta que el consumer ack |
| Orden | Por partición | Por cola (con limitaciones) |
| Replay | Sí (rewind del offset) | No (mensaje consumido = borrado) |

**Cuándo usar mensajería asíncrona:**
- Cuando el productor no necesita esperar resultado (enrollment confirma → notification envía email)
- Fan-out: el mismo evento lo consumen múltiples servicios independientes
- Desacoplamiento temporal: si `notification-service` está caído, los mensajes se acumulan en Kafka y se procesan al recuperar

---

## Vigencia y comunidad

Los microservicios siguen siendo la arquitectura dominante en sistemas distribuidos de escala media-grande (2024–2026). El ecosistema es maduro y estable.

| Área | Estado actual |
|---|---|
| **Contenedores + K8s** | Estándar de facto para despliegue. CNCF (Cloud Native Computing Foundation) gestiona el ecosistema. |
| **Service mesh** (Istio, Linkerd) | Para sistemas con decenas de servicios donde la observabilidad y la política de red entre servicios son críticas. Agrega complejidad operativa. |
| **Platform engineering** | La tendencia es crear "plataformas internas" que abstraigan K8s del desarrollador (Backstage, Crossplane). |
| **Monolito modular** | Resurgimiento como punto de partida válido. Un monolito bien modularizado puede migrarse a microservicios cuando escale el equipo. |
| **Serverless / FaaS** | Complementario, no sustituto. Funciona bien para workloads event-driven de baja frecuencia. |

**Comunidad:** CNCF Landscape, Spring Cloud, Quarkus, Micronaut, Dapr. Conferencias: KubeCon, Spring I/O, Devoxx.

---

## Relación con este proyecto

Este laboratorio recorre la evolución de una arquitectura desde un monolito funcional hasta un sistema cloud-native, introduciendo cada patrón solo cuando el problema que lo justifica es visible.

| Versión | Patrón / Concepto demostrado |
|---|---|
| v1 | Bounded contexts, un servicio por capacidad de negocio, una BD por servicio |
| v2 | Service discovery (Eureka), gateway como punto de entrada, config externalizada, client-side LB |
| v3 | Resiliencia: circuit breaker, retry, timeout, idempotencia |
| v4 | Comunicación asíncrona (Kafka), Saga pattern, fan-out, Outbox pattern |
| v5 | Autenticación centralizada (Keycloak), OAuth2/OIDC, zero-trust entre servicios |
| v6 | Observabilidad: trazas distribuidas, métricas RED, logs correlacionados |
| v7 | Contenerización, Kubernetes, CI/CD |
| v8 | Platform engineering: Vault, ArgoCD, Terraform — la infraestructura como código |

Cada versión es un "¿qué problema apareció que la versión anterior no resolvía?" Los microservicios no son el punto de llegada — son el contexto en el que aparecen todos estos problemas.
