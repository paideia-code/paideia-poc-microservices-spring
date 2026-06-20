# Postman - Paideia Microservices

Coleccion de endpoints para probar los microservicios del laboratorio Paideia POC.

## Convencion: una coleccion por rama

La carpeta `docs/postman/` vive dentro de `docs/` junto al resto de la documentacion. El versionado lo hace Git: la rama `v1/minimal` tiene la coleccion con los endpoints de v1, la rama `v2/infrastructure` tiene la de v2, y asi sucesivamente.

No hay subdirectorios por version. La coleccion evoluciona junto con el codigo en cada rama.

## Uso

### 1. Importar en Postman

1. Abre Postman Desktop.
2. Clic en `File` -> `Import`.
3. Selecciona ambos archivos:
   - `paideia-poc-microservices-spring.postman_collection.json`
   - `Local.postman_environment.json`

### 2. Activar el environment

En la esquina superior derecha de Postman, selecciona el environment `Local`.

### 3. Servicios disponibles

| Servicio | Puerto | Variable de URL |
|---|---:|---|
| course-service | 8081 | `{{course_service_url}}` |
| enrollment-service | 8082 | `{{enrollment_service_url}}` |
| payment-service | 8083 | `{{payment_service_url}}` |
| user-service | 8084 | `{{user_service_url}}` |
| content-service | 8085 | `{{content_service_url}}` |
| notification-service | 8086 | `{{notification_service_url}}` |

### 4. Variables del environment

El environment `Local` define URLs base, IDs de seed y variables capturadas por scripts Postman:

- Seeds: `test_course_id`, `test_course_draft_id`.
- IDs capturados: `test_enrollment_id`, `test_payment_id`, `test_student_id`, `test_notification_id`, `test_content_id`.
- Auxiliares: `test_user_email`, `run_suffix`.

La coleccion captura IDs desde respuestas `POST` con scripts `Tests`, por ejemplo una inscripcion creada actualiza `test_enrollment_id` y `test_payment_id`.

### 5. Flujo de prueba recomendado

1. `Health Checks`: verifica que todos los servicios estan levantados.
2. `Course Service`: consulta cursos seed y crea un curso temporal con `POST /courses`.
3. `User Service`: registra un estudiante; el request genera email unico.
4. `Enrollment Service`: crea una inscripcion `ENROLLED` con `test_course_id`.
5. `Payment Service`: consulta el pago capturado por enrollment o crea pagos manuales.
6. `Notification Service`: consulta una notificacion y crea una manual.
7. `Content Service`: registra contenido con `multipart/form-data` y consulta contenidos con `X-Student-Id` inscrito.

En `POST /contents`, selecciona manualmente un archivo en el campo `file` antes de enviar el request. Postman debe calcular el `Content-Type` multipart automáticamente; no agregues un header `Content-Type: application/json`.

Algunos requests estan pensados para validar errores:

- `POST /courses - validation error`: espera 400.
- `POST /enrollments - course not published`: espera 409.

### 6. Endpoints protegidos desde v5

v1 no tiene seguridad real, pero la coleccion ya apunta al flujo que v5 debe proteger por identidad del estudiante propietario:

- `GET /students/{id}`
- `POST /courses`
- `GET /enrollments/{id}`
- `GET /payments/{id}`
- `GET /notifications/{id}`
- `GET /contents?courseId={courseId}` + `X-Student-Id`

### 7. Agregar nuevos endpoints

Cuando implementes nuevos endpoints:

1. Clic derecho en la carpeta del servicio correspondiente -> `Add Request`.
2. Completa metodo, URL y body.
3. Usa variables del environment.
4. Si el endpoint crea recursos, agrega un script `Tests` para capturar el ID.
5. Guarda la coleccion y commitea el JSON actualizado.
