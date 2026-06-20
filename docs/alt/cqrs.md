# alt/cqrs — CQRS con modelo de lectura separado

| | |
|---|---|
| Rama | `alt/cqrs` |
| Bifurca de | `v4.0.0` / `v4/eda` |
| Diferencia clave | Modelo de lectura desnormalizado y separado del modelo de escritura, proyectado desde eventos Kafka |

## Qué cambia respecto a v4

v4 escribe y lee desde el mismo modelo JPA. Las queries complejas (ej. "cursos disponibles con conteo de inscripciones activas") requieren joins entre servicios o llamadas síncronas.

Esta alternativa aplica **CQRS**: el modelo de escritura permanece en cada servicio (commands), pero se crea un modelo de lectura desnormalizado (queries) que se proyecta escuchando los eventos de Kafka. Las lecturas son rápidas y sin joins porque el read model ya tiene los datos combinados.

## Qué explora

- Separación command model (normalizado, consistente) vs query model (desnormalizado, rápido)
- Proyecciones: consumidores Kafka que construyen el read model incremental
- Eventual consistency en el read model: cuándo es aceptable
- Read model como vista materializada: PostgreSQL, Elasticsearch, o incluso Redis
- Cómo manejar la reconstrucción del read model desde cero (replay de eventos)

## Cuándo tiene sentido

- Queries complejas que requieren datos de múltiples servicios
- Diferencia de escala entre lecturas y escrituras (muchas más lecturas)
- Cuando la latencia de lectura es un requisito y los joins entre servicios son costosos
