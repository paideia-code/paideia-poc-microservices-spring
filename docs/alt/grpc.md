# alt/grpc — Comunicación interna con gRPC

| | |
|---|---|
| Rama | `alt/grpc` |
| Bifurca de | `v2.0.0` / `v2/infrastructure` |
| Diferencia clave | gRPC / Protobuf vs HTTP REST + JSON para las llamadas entre servicios |

## Qué cambia respecto a v2

v2 usa `RestClient` HTTP con JSON para las llamadas entre servicios (enrollment → course, enrollment → payment, etc.).

Esta alternativa reemplaza esas llamadas con **gRPC**: contratos definidos en `.proto`, serialización binaria con Protobuf, y soporte nativo para streaming.

## Qué explora

- Definición de contratos en `.proto` y generación de código con `protoc`
- `grpc-spring-boot-starter` o `net.devh:grpc-server-spring-boot-starter`
- Diferencia entre RPC tipado (gRPC) vs HTTP genérico (REST)
- Serialización binaria (Protobuf) vs texto (JSON): tamaño, velocidad, legibilidad
- Streaming unidireccional y bidireccional
- Interoperabilidad: gRPC suele quedar dentro del clúster; el API Gateway sigue exponiendo REST o HTTP/2 hacia afuera

## Cuándo tiene sentido

- Alta frecuencia de llamadas internas donde el overhead de HTTP/JSON es medible
- Contratos fuertemente tipados entre equipos independientes
- Necesidad de streaming (ej. notificaciones en tiempo real entre servicios)
