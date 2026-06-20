# Reglas de negocio

Las reglas de negocio se agrupan y numeran por los cinco flujos principales descritos en [03-flujos.md](03-flujos.md).

Cuando una regla participa en más de un recorrido, se ubica en el flujo donde se decide su resultado observable principal.

## Flujo 1 - Gestión de cursos y contenidos

| ID | Regla | Servicio responsable | Versión |
|---|---|---|---|
| `R-001` | Solo usuarios con rol `ADMIN` pueden gestionar cursos y contenidos. | `course-service` / `content-service` | v5 |
| `R-002` | El título del curso debe ser único. | `course-service` | v1 |
| `R-003` | El precio del curso no puede ser negativo. | `course-service` | v1 |
| `R-004` | Todo contenido pertenece a un curso. | `content-service` | v1 |
| `R-005` | El registro de contenido debe conservar la información mínima para identificar el archivo y metadata flexible propia del tipo de material. | `content-service` | v1 |

## Flujo 2 - Consulta pública de cursos

| ID | Regla | Servicio responsable | Versión |
|---|---|---|---|
| `R-006` | Visitantes, estudiantes y administradores pueden consultar los cursos registrados y ver su estado. | `course-service` | v1 |

## Flujo 3 - Registro e inicio de sesión

| ID | Regla | Servicio responsable | Versión |
|---|---|---|---|
| `R-007` | El email del estudiante debe ser único. | `user-service` | v1 |
| `R-008` | El registro público siempre crea usuarios con rol `STUDENT`. | `user-service` | v1/v5 |

## Flujo 4 - Inscripción, pago y notificación

| ID | Regla | Servicio responsable | Versión |
|---|---|---|---|
| `R-009` | Solo los cursos `PUBLISHED` aceptan inscripciones; `DRAFT` y `ARCHIVED` pueden consultarse, pero no comprarse. | `course-service` / `enrollment-service` | v1 |
| `R-010` | Un estudiante no puede tener dos inscripciones al mismo curso. | `enrollment-service` | v1 |
| `R-011` | Un pago aprobado deja la inscripción en estado `ENROLLED`. | `enrollment-service` | v1 |
| `R-012` | Un pago rechazado deja la inscripción en estado `REJECTED`. | `enrollment-service` | v1 |
| `R-013` | Todo pago debe conservar el monto procesado y su estado final. | `payment-service` | v1 |
| `R-014` | Un pago `APPROVED` no debe tener motivo de rechazo. | `payment-service` | v1 |
| `R-015` | Un pago `REJECTED` debe registrar un motivo de rechazo. | `payment-service` | v1 |
| `R-016` | Toda notificación nueva queda inicialmente en estado `PENDING`. | `notification-service` | v1 |
| `R-017` | Una notificación debe estar asociada a un destinatario. | `notification-service` | v1 |
| `R-018` | Una notificación debe registrar el tipo de mensaje y el contenido mínimo necesario para su trazabilidad. | `notification-service` | v1 |

## Flujo 5 - Consulta de recursos propios y contenido comprado

| ID | Regla | Servicio responsable | Versión |
|---|---|---|---|
| `R-019` | El estudiante autenticado solo puede consultar sus recursos propios: perfil, inscripciones y notificaciones. | `user-service` / `enrollment-service` / `notification-service` | v5 |
| `R-020` | El contenido de un curso solo puede entregarse si el estudiante tiene una inscripción `ENROLLED` para ese curso. | `content-service` / `enrollment-service` | v1/v5 |
