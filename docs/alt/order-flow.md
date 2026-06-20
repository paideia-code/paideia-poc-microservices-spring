# alt/order-flow — Flujo de negocio extendido con order-service

| | |
|---|---|
| Rama | `alt/order-flow` |
| Bifurca de | `v4.0.0` / `v4/eda` |
| Diferencia clave | Saga extendida que introduce `order-service` como agregador del flujo de inscripción |

## Qué cambia respecto a v4

v4 tiene `enrollment-service` coordinando directamente el flujo: crea la inscripción, dispara el pago y luego el evento de notificación.

Esta alternativa introduce un nuevo servicio, **`order-service`**, que actúa como agregador del proceso completo. `enrollment-service` delega a `order-service` la responsabilidad de orquestar los pasos downstream, produciendo un modelo más alineado con el patrón *Order Saga* de referencia.

## Qué explora

- Separación de responsabilidades: inscripción (datos) vs proceso (coordinación)
- Patrón Order Saga completo con más participantes
- Gestión del estado del pedido/proceso a través de múltiples servicios
- Idempotencia con más puntos de fallo posibles
- Modelo de dominio extendido: cómo un bounded context adicional cambia las fronteras

## Cuándo tiene sentido

- Cuando el flujo de inscripción crece con más pasos o condiciones
- Para estudiar sagas más largas y realistas que las de 3-4 pasos
- Si se quiere practicar diseño de bounded contexts adicionales sin cambiar los servicios base
