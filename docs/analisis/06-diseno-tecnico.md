# Diseño técnico

## Servicios de negocio

| Servicio | Puerto | Responsabilidad |
|---|---:|---|
| `course-service` | 8081 | Cursos mínimos para preparar inscripciones. |
| `enrollment-service` | 8082 | Inscripción, estado y autorización de acceso a contenido. |
| `payment-service` | 8083 | Pago simulado. |
| `user-service` | 8084 | Registro de estudiantes y modelo mínimo de roles (`STUDENT`, `ADMIN`). |
| `content-service` | 8085 | Registro de contenidos y entrega protegida al estudiante con acceso. |
| `notification-service` | 8086 | Registro de notificaciones. |

## Contratos HTTP v1

| Servicio | Endpoints que permanecen |
|---|---|
| `user-service` | `POST /register`, `GET /users/{id}` |
| `course-service` | `POST /courses`, `GET /courses`, `GET /courses/{id}` |
| `enrollment-service` | `POST /enrollments` + `X-Student-Id`, `GET /enrollments/{id}`, `GET /enrollments/access?courseId` + `X-Student-Id` |
| `payment-service` | `POST /payments`, `GET /payments/{id}` |
| `notification-service` | `POST /notifications`, `GET /notifications/{id}` |
| `content-service` | `POST /contents` multipart con `courseId`, `description`, `metadata` y `file`; `GET /contents?courseId={courseId}` + `X-Student-Id` |

Los endpoints `/{recurso}/me` no forman parte del contrato v1-v4. Se incorporan desde v5, cuando existe autenticación real: representan la vista propia del estudiante, deben ser accesibles solo con rol `STUDENT`, leen el JWT enviado en `Authorization: Bearer <token>` y toman el UUID de negocio desde el claim `paideia_user_id`.

No se agrega `GET /courses/me` porque `course-service` no posee la relación estudiante-curso. Esa relación pertenece a `enrollment-service`, por lo que los cursos comprados se infieren desde las inscripciones propias desde v5 o se dejan para una versión posterior con una vista de lectura.

Tampoco se agrega `GET /payments/me` porque `payment-service` no posee `studentId`. El vínculo estudiante-pago se conoce desde la inscripción.

`GET /contents?courseId={courseId}` no usa `/me` porque no devuelve un resumen de recursos propios, sino el contenido de un curso específico. Aun así, valida la identidad del estudiante y solo entrega archivos si existe una inscripción `ENROLLED`.

Los endpoints del recurso sin `/me`, como `GET /users/{id}`, `GET /enrollments/{id}`, `GET /payments/{id}` o `GET /notifications/{id}`, quedan para operaciones administrativas o internas protegidas por rol/servicio. Se mantiene la decisión de no usar prefijo `/admin`; la diferencia se expresa por permisos y por intención del endpoint.

## Headers

| Header | Uso | Evolución |
|---|---|---|
| `X-Student-Id` | Identidad simulada del estudiante en operaciones que requieren propietario, como inscripción y acceso a contenido comprado. | En v5 se reemplaza por `Authorization: Bearer <jwt>` y claim `paideia_user_id`; desde esa versión se agregan las vistas `/{recurso}/me`. |
| `X-Payment-Simulation` | Simula resultado local de pago con `APPROVED` o `REJECTED`. | En v4 viaja como metadata de evento; en entornos no locales debe deshabilitarse o reemplazarse por proveedor real. |

## Persistencia

| Servicio | Persistencia | Datos |
|---|---|---|
| `course-service` | PostgreSQL `course` | Cursos seed o preparados para el lab. |
| `user-service` | PostgreSQL `users` | Estudiantes y administradores. |
| `enrollment-service` | PostgreSQL `enrollment` | Inscripciones y estado de acceso. |
| `payment-service` | PostgreSQL `payment` | Pagos simulados. |
| `notification-service` | PostgreSQL `notification` | Notificaciones pendientes. |
| `content-service` | MongoDB `content` | `Content`: `id`, `courseId`, `filename`, `contentType`, `sizeBytes`, `storageKey`, `description`, `metadata`, `fileBytes`. |

En v1, `content-service` guarda internamente los bytes del archivo recibido por `multipart/form-data` para mantener el laboratorio simple. `fileBytes` es un detalle de persistencia temporal. `storageKey` queda desde v1 como referencia estable del archivo, aunque todavía no apunte a MinIO/S3. Desde v7, los bytes se mueven a MinIO/S3 y el servicio conserva metadata como `filename`, `contentType`, `sizeBytes`, `storageKey` y `metadata`.

El campo `metadata` es un objeto JSON flexible. Justifica el uso de MongoDB desde v1 porque distintos tipos de material pueden guardar propiedades distintas sin cambiar el contrato principal: un PDF puede guardar `pages`, un video puede guardar `durationSeconds` y un recurso de texto puede guardar `tags`.

`sizeBytes` no es indispensable para persistir el archivo, pero conviene guardarlo: permite validar límites, mostrar tamaño al usuario, auditar uploads y evitar consultar el storage solo para conocer el peso del archivo.
