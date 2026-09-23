# Observabilidad — Opciones de implementación y acoplamiento al código

> **Resumen**: hay 5 niveles de implementación de observabilidad, de más a menos acoplados al código. El acoplamiento baja según *dónde* ocurre la instrumentación — en tu código (niveles 1-2), en la JVM (nivel 3), en el kernel (nivel 4), en la infraestructura (nivel 5).

## El espectro completo

| Nivel | Enfoque | Acoplamiento | Cómo funciona | Ejemplo | Pros | Contras | Cuándo usarlo |
|---|---|---|---|---|---|---|---|
| **1** | **Instrumentación manual** | 🔴 Alto | Escribes spans, métricas y logs en el código | OpenTelemetry SDK, `@WithSpan`, `@Observed` | Control total · contexto de negocio real · métricas custom · sin magia | Mayor trabajo y acoplamiento · hay que mantenerlo al evolucionar el código | Métricas de negocio o spans de operaciones específicas |
| **2** | **Starter de framework** | 🟠 Medio | Una dependencia + config por repo; auto-instrumenta HTTP/JDBC/Kafka | `spring-boot-starter-opentelemetry` | Integración nativa con Micrometer · auto-instrumenta todo Spring · métricas custom fáciles · una dependencia por repo | Toca cada repo (pom + config) · solo apps Spring Boot | Apps Spring Boot que controlas — **el caso del lab** |
| **3** | **Java agent** | 🟡 Bajo (cero código) | Flag de JVM; bytecode instrumentation automática | `-javaagent:opentelemetry-javaagent.jar` | Cero código y cero repos · cualquier app JVM · fácil de agregar/quitar | Magia: acoplamiento de versiones · no instrumenta lógica custom · problemas con GraalVM/AOT | Muchos repos que no puedes tocar, quick win, apps de terceros |
| **4** | **eBPF** | 🟢 Ninguno | Nivel kernel; captura tráfico HTTP/gRPC sin agent ni código | Grafana Beyla, Pixie | Zero-touch real · cualquier lenguaje · sin acoplamiento de versiones | Solo lo visible a nivel de red · kernel Linux 4.18+ · setup complejo | Entornos poliglotas, servicios que no controlas, K8s con Pixie |
| **5** | **Service mesh** | 🟢 Ninguno | Sidecars Envoy capturan telemetría de red | Istio | Cero código y cero agent · captura todo el tráfico + mTLS · mapa de servicios | Solo K8s · infraestructura pesada · sin contexto de negocio | Producción K8s con muchos servicios, cuando ya necesitas mTLS |

---

## Nivel 1 — Instrumentación manual (OpenTelemetry SDK + API)

**Qué es**: escribes spans, métricas y correlación de logs **directamente en tu código** usando la API/SDK de OpenTelemetry.

**Cómo funciona**: obtienes un `Tracer` y creas spans alrededor de operaciones; usas anotaciones como `@WithSpan`; registras métricas con Micrometer.

```java
// Span manual alrededor de una operación de negocio
@WithSpan("enroll.student")
public void enroll(UUID studentId, UUID courseId) {
    // lógica de negocio
}

// Métrica de negocio custom
@Observed(name = "matricula.creada")
public void enroll(...) { ... }

// O con el SDK directo
Span span = tracer.spanBuilder("enroll.student").startSpan();
try (Scope scope = span.makeCurrent()) {
    // ...
} finally {
    span.end();
}
```

**Qué observa**: exactamente lo que tú instrumentas — **lógica de negocio**, operaciones específicas, métricas custom. Nada más.

**Pros**: control total · contexto de negocio real ("matrícula creada", no solo "POST /enrollments") · sin magia · métricas que responden preguntas de negocio.
**Contras**: el mayor trabajo · el mayor acoplamiento · hay que mantener la instrumentación cuando el código evoluciona.

**Cuándo**: cuando necesitas métricas de negocio o spans de operaciones específicas. **En el lab**: el ejemplo de la guía (`@Observed(name = "pedido.creado")`).

---

## Nivel 2 — Starter de framework (`spring-boot-starter-opentelemetry`)

**Qué es**: el starter oficial de Spring Boot 4 — autoconfigura los exportadores OTLP y se integra nativamente con Micrometer.

**Cómo funciona**: agregas la dependencia al pom + config en `application.yml`. Boot autoconfigura la instrumentación de todo lo que toca Spring: HTTP server/client, JDBC, Kafka, RestClient/WebClient, tareas programadas, métricas de Actuator (JVM, HTTP).

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-opentelemetry</artifactId>
</dependency>
```

```yaml
management:
  otlp:
    metrics:
      export:
        url: http://alloy:4318/v1/metrics
  opentelemetry:
    tracing:
      export:
        otlp:
          endpoint: http://alloy:4318/v1/traces
```

**Qué observa**: todo el framework — cada request HTTP, cada query JDBC, cada llamada RestClient, cada mensaje Kafka, métricas JVM. **No** observa tu lógica de negocio (para eso combinas con Nivel 1).

**Pros**: integración nativa con Micrometer · sin magia (es el SDK real por debajo) · métricas custom fáciles (`@Observed`) · una dependencia por repo.
**Contras**: toca cada repo (pom + config) · solo funciona para apps Spring Boot.

**Cuándo**: apps Spring Boot que controlas. **En el lab**: es la recomendación de la guía — 7 repos, manejable.

> 💡 **Clave**: el Nivel 2 ES el Nivel 1, pero autoconfigurado. Puedes combinar ambos: el starter te da el framework, y agregas `@Observed`/`@WithSpan` donde necesitas contexto de negocio. Esa combinación es lo que recomienda la guía.

---

## Nivel 3 — Java agent de OpenTelemetry

**Qué es**: un flag de JVM (`-javaagent:opentelemetry-javaagent.jar`) que instrumenta tu app **sin tocar código**. Es lo que hace el colega con el "dockerfile wrapper".

**Cómo funciona**: el agent se inyecta en la JVM al arrancar y hace **bytecode instrumentation** — reescribe las clases en tiempo de carga para agregar instrumentación. Detecta automáticamente las librerías (Servlet, JDBC, Kafka, RestTemplate, etc.) vía "módulos de instrumentación". Se configura con variables de entorno `OTEL_*`.

```dockerfile
# El enfoque del colega: wrapper en el Dockerfile, cero repos tocados
FROM eclipse-temurin:17-jre
COPY target/app.jar /app.jar
COPY opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS="-javaagent:/otel/opentelemetry-javaagent.jar"
ENV OTEL_EXPORTER_OTLP_ENDPOINT="http://alloy:4318"
ENV OTEL_SERVICE_NAME="course-service"
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Qué observa**: todo lo que cubren los módulos de instrumentación — HTTP, JDBC, Kafka, Redis, etc. **No** observa tu lógica de negocio (no puede poner `@WithSpan` en tus métodos).

**Pros**: cero código · cero repos · funciona con cualquier app JVM · fácil de agregar/quitar (solo el flag).
**Contras**: **magia** — el bytecode instrumentation debe coincidir con tus versiones de librerías (acoplamiento de versiones) · no instrumenta lógica custom · problemas con GraalVM/AOT (las imágenes nativas no se pueden instrumentar por bytecode) · difícil de depurar cuando algo falla · menos control.

**Cuándo**: muchos repos que no puedes tocar, quick win, apps de terceros. **El plan del colega** (agent ahora, SDK después) es un camino incremental válido.

---

## Nivel 4 — eBPF (Grafana Beyla, Pixie)

**Qué es**: instrumentación a **nivel de kernel** — ni código, ni agent en la JVM.

**Cómo funciona**: eBPF (extended Berkeley Packet Filter) ejecuta programas sandboxed dentro del kernel de Linux. Se engancha a las syscalls de red y a uprobes para capturar el tráfico HTTP/gRPC **observándolo desde afuera del proceso**. Beyla auto-instrumenta servicios HTTP/gRPC; Pixie es nativo de K8s (captura tráfico + profiling).

**Qué observa**: requests HTTP/gRPC, latencia, errores, throughput — a nivel de red. **No** observa lógica de negocio, ni internals de la JVM, ni queries de BD (salvo vía uprobes específicos).

**Pros**: verdaderamente zero-touch (ni código, ni agent, ni flag) · funciona con **cualquier lenguaje** (no solo JVM) · sin acoplamiento de versiones.
**Contras**: limitado a lo visible a nivel de red — sin contexto de negocio · sin spans/métricas custom · requiere kernel Linux 4.18+ (no funciona en Windows/macOS para dev) · setup más complejo.

**Cuándo**: entornos poliglotas, servicios que no controlas, K8s con Pixie. **En el lab**: probablemente no — es un lab JVM y el valor vs el starter es bajo. Pero existe y es bueno saberlo.

---

## Nivel 5 — Service mesh (Istio)

**Qué es**: instrumentación a **nivel de infraestructura** — sidecars de Envoy capturan la telemetría.

**Cómo funciona**: cada pod recibe un sidecar Envoy; **todo el tráfico** pasa por los sidecars; estos generan telemetría (métricas HTTP, access logs, propagación de trazas) y la reenvían al backend. Istio además hace mTLS, routing de tráfico y retries.

**Qué observa**: todo el tráfico servicio-a-servicio (HTTP/gRPC), latencia, errores, throughput, **topología del sistema**. **No** observa lógica de negocio.

**Pros**: cero código · cero agent · captura TODO el tráfico incluyendo mTLS · te da el mapa de servicios.
**Contras**: solo en K8s (inyección de sidecars) · infraestructura pesada (control plane + sidecars = overhead de recursos) · sin contexto de negocio · agrega complejidad al cluster.

**Cuándo**: producción K8s con muchos servicios, cuando necesitas mTLS + gestión de tráfico de todos modos. **En el lab**: overkill por ahora — pero relevante para la Fase G si se quiere profundizar.

---

## La capa ortogonal: el Collector (Alloy)

Esto **no es instrumentación, es transporte** — y funciona con cualquiera de los 5 niveles:

```
App ──OTLP──▶ Alloy (collector) ──▶ Loki (logs) / Tempo (trazas) / Mimir (métricas)
```

- Desacopla tu app del backend concreto (cambiar Tempo por Jaeger no toca las apps)
- Hace batching, retry, sampling, enriquecimiento (ej. `environment=prod`)
- Scrapea targets Prometheus (ej. el `/metrics` de Keycloak, que no exporta por OTLP)
- La guía ya lo contempla (sección 10)

---

## Comparación final

| | Nivel 1 Manual | Nivel 2 Starter | Nivel 3 Agent | Nivel 4 eBPF | Nivel 5 Mesh |
|---|---|---|---|---|---|
| Acoplamiento al código | 🔴 Alto | 🟠 Medio | 🟡 Cero código | 🟢 Ninguno | 🟢 Ninguno |
| Repos a tocar | Todos | Todos (pom+config) | 0 | 0 | 0 |
| Lógica de negocio | ✅ | ✅ (combinando) | ❌ | ❌ | ❌ |
| Framework (HTTP/DB/Kafka) | Manual | ✅ Auto | ✅ Auto | Solo red | Solo red |
| Métricas custom | ✅ | ✅ | ❌ | ❌ | ❌ |
| Aprendizaje del SDK | Máximo | Alto | Bajo (magia) | Nulo | Nulo |
| Requiere | — | Spring Boot | JVM | Linux 4.18+ | K8s |

**Los niveles se combinan, no se excluyen**:
- **El lab (recomendado)**: Nivel 2 + Nivel 1 donde se necesite negocio — lo que dice la guía
- **El colega**: Nivel 3 ahora, Nivel 2/1 después — camino incremental válido
- **Niveles 4-5**: alternativas zero-touch para cuando no controlas el código o ya tienes mesh
