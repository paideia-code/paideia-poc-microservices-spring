# alt/service-mesh — Resiliencia de red con Istio o Linkerd

| | |
|---|---|
| Rama | `alt/service-mesh` |
| Bifurca de | `v7.0.0` / `v7/deployment` |
| Diferencia clave | Service mesh (Istio/Linkerd) maneja resiliencia a nivel de red vs Spring Cloud + Resilience4j en código |

## Qué cambia respecto a v7

v7 despliega en Kubernetes, pero la resiliencia (circuit breaker, retry, timeout) sigue siendo responsabilidad del código de la aplicación vía Resilience4j.

Esta alternativa introduce un **service mesh** (sidecar proxy por pod) que maneja resiliencia, observabilidad y mTLS a nivel de infraestructura de red, sin cambios en el código de la aplicación.

## Qué explora

- Arquitectura sidecar: Envoy (Istio) o Linkerd proxy inyectado en cada pod
- Políticas de retry y circuit breaking declarativas (`VirtualService`, `DestinationRule`)
- mTLS automático entre servicios: credenciales TLS gestionadas por el mesh
- Observabilidad integrada: trazas, métricas y gráficos de tráfico en Kiali/Jaeger
- Comparación de complejidad operacional: mesh vs código
- Cuándo la capa de red es el lugar correcto para la resiliencia

## Cuándo tiene sentido

- Entornos multi-lenguaje donde no todos los servicios pueden usar Resilience4j
- Equipos de plataforma que quieren aplicar políticas de red sin tocar código
- Para estudiar el concepto de infraestructura declarativa vs resiliencia en código
