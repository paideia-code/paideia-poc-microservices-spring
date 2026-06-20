# Casos de uso

Este documento ordena y numera los casos de uso por los cinco flujos principales descritos en [03-flujos.md](03-flujos.md). Cada caso indica intención, reglas relevantes, feature asociada y resultado observable.

## Mapa por flujo

| Flujo | ID | Caso de uso | Actor | Feature |
|---|---|---|---|---|
| 1. Gestión de cursos y contenidos | `UC-001` | Preparar curso | Administrador | [features/course/create_courses.feature](../../features/course/create_courses.feature) |
| 1. Gestión de cursos y contenidos | `UC-002` | Preparar contenido | Administrador | [features/content/create_contents.feature](../../features/content/create_contents.feature) |
| 2. Consulta pública de cursos | `UC-003` | Consultar cursos | Visitante / Estudiante / Administrador | [features/course/get_courses.feature](../../features/course/get_courses.feature) |
| 3. Registro e inicio de sesión | `UC-004` | Registrar estudiante | Visitante | [features/user/register_student.feature](../../features/user/register_student.feature) |
| 4. Inscripción, pago y notificación | `UC-005` | Solicitar inscripción | Estudiante | [features/enrollment/enroll_published_course.feature](../../features/enrollment/enroll_published_course.feature) |
| 4. Inscripción, pago y notificación | `UC-006` | Rechazar inscripción no disponible | Estudiante | [features/enrollment/reject_unavailable_course.feature](../../features/enrollment/reject_unavailable_course.feature) |
| 4. Inscripción, pago y notificación | `UC-007` | Procesar pago | Plataforma | [features/payment/process_payment.feature](../../features/payment/process_payment.feature) |
| 4. Inscripción, pago y notificación | `UC-008` | Registrar notificación | Plataforma | [features/notification/register_notification.feature](../../features/notification/register_notification.feature) |
| 5. Consulta de recursos propios y contenido comprado | `UC-009` | Consultar recurso propio desde v5 | Estudiante | [features/user/get_profile_by_student.feature](../../features/user/get_profile_by_student.feature), [features/enrollment/get_enrollments_by_student.feature](../../features/enrollment/get_enrollments_by_student.feature), [features/notification/get_notifications_by_student.feature](../../features/notification/get_notifications_by_student.feature) |
| 5. Consulta de recursos propios y contenido comprado | `UC-010` | Acceder a contenido comprado | Estudiante | [features/content/access_purchased_content.feature](../../features/content/access_purchased_content.feature) |

## Flujo 1 - Gestión de cursos y contenidos

### `UC-001` - Preparar curso

- Objetivo: crear un curso para que pueda ser consultado.
- Reglas: `R-001`, `R-002`, `R-003`.
- Flujo: el administrador envía título, descripción, precio y estado.
- Resultado: curso registrado o rechazo por título duplicado, precio inválido o falta de permisos.

### `UC-002` - Preparar contenido

- Objetivo: asociar materiales de contenido a un curso.
- Reglas: `R-001`, `R-004`, `R-005`.
- Flujo: el administrador registra un archivo de contenido para un curso, con metadata flexible opcional.
- Resultado: contenido asociado al curso o rechazo por datos inválidos/falta de permisos.

## Flujo 2 - Consulta pública de cursos

### `UC-003` - Consultar cursos

- Objetivo: permitir que visitantes, estudiantes y administradores exploren cursos antes de cualquier compra.
- Reglas: `R-006`.
- Flujo: el actor consulta la lista de cursos o el detalle de un curso.
- Resultado: lista/detalle de cursos con estado o 404 si el curso no existe.

## Flujo 3 - Registro e inicio de sesión

### `UC-004` - Registrar estudiante

- Objetivo: crear una cuenta básica de estudiante.
- Reglas: `R-007`, `R-008`.
- Flujo: el visitante envía email y nombre; la plataforma valida unicidad y crea un usuario `STUDENT`. En v1-v4 el `id` devuelto se usa como identidad simulada para flujos con propietario; desde v5 el estudiante puede consultar su perfil con `GET /users/me`.
- Resultado: usuario `STUDENT` creado con `201 Created` y `Location: /users/{id}`, o rechazo por email duplicado/datos inválidos.

## Flujo 4 - Inscripción, pago y notificación

### `UC-005` - Solicitar inscripción

- Objetivo: comprar acceso a un curso publicado.
- Reglas: `R-009` a `R-018`.
- Flujo: el estudiante solicita inscripción; la plataforma valida curso y duplicados, procesa pago y registra notificación.
- Resultado: inscripción `ENROLLED` si el pago aprueba; `REJECTED` si el pago falla.

### `UC-006` - Rechazar inscripción no disponible

- Objetivo: impedir compras de cursos no publicados.
- Reglas: `R-009`.
- Flujo: la plataforma detecta que el curso está `DRAFT`, `ARCHIVED` o no existe.
- Resultado: no se registra pago, inscripción ni notificación.

### `UC-007` - Procesar pago

- Objetivo: simular el resultado de cobro.
- Reglas: `R-013`, `R-014`, `R-015`.
- Flujo: la plataforma envía monto; en local/test puede forzar resultado con `X-Payment-Simulation`.
- Resultado: pago `APPROVED` o `REJECTED` con trazabilidad mínima.

### `UC-008` - Registrar notificación

- Objetivo: dejar trazabilidad del mensaje al estudiante.
- Reglas: `R-016`, `R-017`, `R-018`.
- Flujo: la plataforma registra destinatario, tipo, asunto y cuerpo.
- Resultado: notificación `PENDING`.

## Flujo 5 - Consulta de recursos propios y contenido comprado

### `UC-009` - Consultar recursos propios

- Objetivo: permitir que el estudiante autenticado revise su perfil, inscripciones y notificaciones desde v5.
- Reglas: `R-019`.
- Flujo: el estudiante envía un JWT válido y consulta: `GET /users/me`, `GET /enrollments/me` y `GET /notifications/me`.
- Resultado: recursos propios disponibles o 403 si intenta acceder a recursos ajenos.
- Features: una por recurso, en `features/user`, `features/enrollment` y `features/notification`; quedan asociadas a v5+.

### `UC-010` - Acceder a contenido comprado

- Objetivo: entregar materiales de un curso solo al estudiante que lo compró.
- Reglas: `R-020`.
- Flujo: el estudiante solicita `GET /contents?courseId={courseId}` y la plataforma valida que tenga inscripción `ENROLLED`.
- Resultado: contenido disponible o 403 si no tiene acceso.
