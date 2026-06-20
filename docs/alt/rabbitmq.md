# alt/rabbitmq — Mensajería con RabbitMQ

| | |
|---|---|
| Rama | `alt/rabbitmq` |
| Bifurca de | `v4.0.0` / `v4/eda` |
| Diferencia clave | RabbitMQ (push, exchanges, routing) vs Kafka (pull, log distribuido, retención) |

## Qué cambia respecto a v4

v4 usa **Kafka** como bus de eventos: log distribuido, consumidores que leen a su propio ritmo (*pull*), retención configurable, ideal para replay y event sourcing.

Esta alternativa usa **RabbitMQ**: broker de mensajes tradicional con modelo *push*, exchanges que enrutan mensajes a colas según routing keys, y mensajes que se eliminan al ser consumidos.

## Qué explora

- Exchanges: `direct`, `topic`, `fanout`, `headers`
- Routing keys y bindings: cómo un mensaje llega a la cola correcta
- `spring-amqp` y `spring-boot-starter-amqp`
- Modelo push vs pull: diferencia de backpressure y consumo bajo carga
- Dead letter exchanges (DLX) para manejo de errores
- RabbitMQ vs Kafka: cuándo usar cada uno según los requisitos de retención y throughput

## Cuándo tiene sentido

- Mensajes que representan comandos o tareas (no necesariamente eventos históricos)
- Equipos ya familiarizados con AMQP o que usan RabbitMQ en otros sistemas
- Cuando la retención del log no es un requisito y la infraestructura debe ser más ligera
