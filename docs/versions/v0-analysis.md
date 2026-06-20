# v0 - Análisis

v0 es la versión previa a la implementación. Su objetivo es ordenar el producto y el laboratorio antes de escribir servicios.

No contiene código de aplicación, infraestructura ni configuración Spring. Contiene claridad: producto, reglas, casos de uso, criterios de aceptación y escenarios Gherkin iniciales.

## Objetivo

Definir qué debe hacer la plataforma de cursos y dejar una base simple para que v1 implemente el comportamiento sin construir decisiones de negocio sobre la marcha.

## Artefactos de v0

| Artefacto | Ruta | Propósito |
|---|---|---|
| Análisis | [../index.md](../index.md) | Contexto, alcance, dominio, flujos, reglas, casos de uso y diseño técnico. |
| Gherkin | [../../features/README.md](../../features/README.md) | Escenarios de negocio como documentación viva. |
| QA | [../notas/qa.md](../notas/qa.md) | Estrategia de pruebas y posicionamiento de Cucumber/mutación. |

## Qué incluye

| Tema | Incluido |
|---|---|
| Contexto de producto | Plataforma de cursos online. |
| Actores | Visitante, estudiante, administrador y plataforma. |
| Roles | `STUDENT`, `ADMIN` mínimo para cursos. |
| Dominio | Cursos, usuarios, inscripciones, pagos, contenido y notificaciones. |
| Flujos | Recorridos principales detallados en [../analysis/03-flujos.md](../analysis/03-flujos.md). |
| Reglas | Reglas por flujo principal en [../analysis/04-reglas-de-negocio.md](../analysis/04-reglas-de-negocio.md). |
| Casos de uso | Casos agrupados por flujo principal en [../analysis/05-casos-de-uso.md](../analysis/05-casos-de-uso.md). |
| Criterios de aceptación | Criterios por caso de uso y escenarios Gherkin asociados. |
| Gherkin | Escenarios iniciales por área funcional. |

## Qué no incluye

| No incluido | Motivo |
|---|---|
| Microservicios Spring | Empiezan en v1. |
| Docker Compose | Empieza en v1 como soporte local. |
| Persistencia | Se define técnicamente en v1. |
| Seguridad | Se aborda en v5. |
| Automatización Cucumber | Los `.feature` primero son documentación viva; la automatización puede venir después. |


