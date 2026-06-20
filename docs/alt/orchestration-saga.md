# alt/orchestration-saga — Saga con orquestación (Temporal)

| | |
|---|---|
| Rama | `alt/orchestration-saga` |
| Bifurca de | `v4.0.0` / `v4/eda` |
| Diferencia clave | Saga orquestada con Temporal vs Saga coreografiada con Kafka |

## Qué cambia respecto a v4

v4 implementa Saga con **coreografía**: cada servicio reacciona a eventos de Kafka y emite los suyos propios. No hay coordinador central; la lógica de negocio está distribuida en los consumidores.

Esta alternativa usa **orquestación con Temporal**: un workflow central define explícitamente los pasos de la saga, llama a cada actividad, y maneja compensaciones de forma programática.

## Qué explora

- Temporal Workflows y Activities en Java (`temporal-sdk`)
- Diferencia entre coreografía (acoplamiento por eventos) y orquestación (lógica centralizada)
- Retries, timeouts y compensaciones declarativas en el workflow
- Visibilidad: Temporal UI muestra el estado de cada ejecución en tiempo real
- Cuándo la complejidad de un workflow justifica un orquestador externo

## Cuándo tiene sentido

- Sagas con muchos pasos y lógica condicional compleja
- Cuando la visibilidad del estado del proceso es un requisito operacional
- Equipos que prefieren lógica explícita y centralizada sobre choreography distribuida
- No recomendado para sagas simples de 2-3 pasos donde Kafka es suficiente
