# Features Gherkin

Escenarios de aceptación.

Estos archivos son documentación viva. No todos requieren automatización inmediata.

Cuando se automaticen con Cucumber:

- Cada escenario debe mapear a un flujo y a un caso de uso en `docs/index.md` y `docs/analisis/05-casos-de-uso.md`.
- Las reglas asociadas deben vivir en `docs/analisis/04-reglas-de-negocio.md`.
- Los steps deben hablar en lenguaje de negocio.
- Los detalles de HTTP, puertos, repositorios y clases Java deben quedar fuera del `.feature`.
- Cada `.feature` debe tener un actor e intención principal. Si necesita datos de otro dominio, esos datos deben aparecer como contexto (`Given`), no como otro flujo completo dentro del mismo archivo.

## Features iniciales

| Feature | Flujo | Casos de uso |
|---|---|---|
| [course/create_courses.feature](course/create_courses.feature) | 1. Gestión de cursos y contenidos | `UC-001` |
| [content/prepare_course_content.feature](content/prepare_course_content.feature) | 1. Gestión de cursos y contenidos | `UC-002` |
| [course/get_courses.feature](course/get_courses.feature) | 2. Consulta pública de cursos | `UC-003` |
| [user/register_student.feature](user/register_student.feature) | 3. Registro e inicio de sesión | `UC-004` |
| [enrollment/enroll_published_course.feature](enrollment/enroll_published_course.feature) | 4. Inscripción, pago y notificación | `UC-005` |
| [enrollment/reject_unavailable_course.feature](enrollment/reject_unavailable_course.feature) | 4. Inscripción, pago y notificación | `UC-006` |
| [payment/process_payment.feature](payment/process_payment.feature) | 4. Inscripción, pago y notificación | `UC-007` |
| [notification/register_notification.feature](notification/register_notification.feature) | 4. Inscripción, pago y notificación | `UC-008` |
| [user/get_profile_by_student.feature](user/get_profile_by_student.feature) | 5. Consulta de recursos propios y contenido comprado desde v5 | `UC-009` |
| [enrollment/get_enrollments_by_student.feature](enrollment/get_enrollments_by_student.feature) | 5. Consulta de recursos propios y contenido comprado desde v5 | `UC-009` |
| [notification/get_notifications_by_student.feature](notification/get_notifications_by_student.feature) | 5. Consulta de recursos propios y contenido comprado desde v5 | `UC-009` |
| [content/access_purchased_content.feature](content/access_purchased_content.feature) | 5. Consulta de recursos propios y contenido comprado | `UC-010` |

## Casos automatizados con Cucumber

`UC-001` y `UC-003` ya están conectados a Cucumber en `course-service`.

| Pieza | Ubicación | Rol |
|---|---|---|
| Feature | [course/create_courses.feature](course/create_courses.feature) | Escenarios de creación de cursos. |
| Feature | [course/get_courses.feature](course/get_courses.feature) | Escenario de consulta de cursos. |
| Runner | `services/course-service/src/test/java/io/paideia/course/service/bdd/CourseCucumberTest.java` | Selecciona los features de curso y glue de Cucumber. |
| Steps | `services/course-service/src/test/java/io/paideia/course/service/bdd/CreateCoursesStepDefinitions.java` | Traduce Given/When/Then de creación a código Java. |
| Steps | `services/course-service/src/test/java/io/paideia/course/service/bdd/GetCoursesStepDefinitions.java` | Traduce Given/When/Then de consulta a código Java. |

Para correrlo:

```powershell
.\mvnw.cmd -pl services/course-service test
```

Guía de estudio: [docs/notas/cucumber-gherkin.md](../docs/notas/cucumber-gherkin.md).
