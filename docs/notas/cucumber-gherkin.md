# Cucumber y Gherkin en el laboratorio

Esta nota explica, paso a paso, cómo automatizar un caso de uso escrito en Gherkin con Cucumber usando el ejemplo `UC-001 - Preparar curso`.

## Qué tipo de test es

El ejemplo implementado para `UC-001` es un test BDD (Behavior-Driven Development o Desarrollo Guiado por Comportamiento) de aceptación a nivel de servicio.

| Característica | En este laboratorio |
|---|---|
| BDD | El comportamiento está escrito como escenarios de negocio en Gherkin. |
| Aceptación | Verifica que una regla observable del caso de uso se cumpla. |
| Nivel de servicio | Ejecuta `CourseService` directamente. |
| Sin Spring completo | No levanta servidor, contexto Spring ni base de datos. |
| Con Mockito | Simula el repositorio para aislar la regla de negocio. |

No es un test end-to-end porque no llama HTTP real ni levanta todos los microservicios.

No es un test de integración porque no usa base de datos real.

Tampoco reemplaza los unit tests. 

Sirve para conectar el lenguaje de negocio con ejemplos ejecutables.

## BDD no define el nivel técnico del test

BDD no significa "usar Cucumber" ni "hacer tests end-to-end". BDD es una forma de describir comportamiento esperado con ejemplos de negocio.

Gherkin expresa la intención:

```gherkin
Scenario: Crear curso con título nuevo
  Given no existe un curso con título "Introducción a Java"
  When el administrador registra un curso publicado con precio 149.99
  Then el curso queda disponible para consulta
```

Cucumber conecta ese texto con código Java. El tipo técnico del test depende de lo que hagan los step definitions.

| Qué hace el `When` | Tipo técnico resultante |
|---|---|
| Llama `CourseService.create(...)` con repositorio mockeado. | BDD a nivel de servicio. |
| Llama el controller con MockMvc. | BDD de capa web / API slice. |
| Hace HTTP real contra el microservicio levantado. | BDD de integración HTTP del servicio. |
| Ejecuta varios microservicios reales. | BDD end-to-end. |

En nuestro primer caso, el flujo real es:

```text
Gherkin
  -> Cucumber
    -> Step definition
      -> CourseService real
        -> CourseRepository mock
```

Eso significa que sí ejecuta código real de negocio (`CourseService`), pero no usa base de datos real.

La clave: BDD es la capa de lenguaje e intención. Unit, integration, slice o end-to-end son niveles técnicos de ejecución.

## Entonces, qué aporta BDD

BDD aporta claridad cuando queremos que un comportamiento importante pueda leerse como ejemplo de negocio.

Buen escenario:

```gherkin
When el administrador registra un curso publicado con precio 149.99
Then el curso queda disponible para consulta
```

Escenario demasiado técnico:

```gherkin
When hago POST a "/courses" con JSON
Then recibo HTTP 201
And el método save del repository se llama una vez
```

El segundo puede ser un test útil, pero no es buen Gherkin de negocio. Habla en lenguaje de implementación.

La idea práctica:

1. El `.feature` dice qué comportamiento importa.
2. Los steps deciden cómo ejecutar ese comportamiento.
3. La suite técnica decide qué tan profundo se prueba.

## Piezas que participan

| Pieza | Archivo | Rol |
|---|---|---|
| Feature | `create_courses.feature` | Describe escenarios en lenguaje de negocio. |
| Runner | `CourseCucumberTest.java` | Le dice a JUnit que ejecute Cucumber sobre los features de curso. |
| Steps | `CreateCoursesStepDefinitions.java` | Conecta cada línea Gherkin con código Java. |
| Servicio probado | `CourseService` | Contiene la regla de negocio. |
| Repositorio simulado | `CourseRepository` | Se mockea con Mockito. |

Como el runner usa `@SelectDirectories("../../features/course")`, también ejecuta `get_courses.feature` con `GetCoursesStepDefinitions.java`.

## Paso 1: escribir el feature

El feature vive fuera del módulo Java porque representa documentación viva del producto:

```gherkin
Feature: Crear cursos
  Como administrador
  Quiero gestionar cursos
  Para disponibilizar cursos
```

Cada escenario usa la forma:

```gherkin
Scenario: Crear curso con título nuevo
  Given no existe un curso con título "Introducción a Java"
  When el administrador registra un curso publicado con precio 149.99
  Then el curso queda disponible para consulta
```

Lectura mental:

| Línea | Qué significa |
|---|---|
| `Given` | Precondición del mundo de negocio. |
| `When` | Acción que ejecuta el actor. |
| `Then` | Resultado observable esperado. |

## Paso 2: agregar dependencias

En `services/course-service/pom.xml` se agregan dependencias de test:

```xml
<dependency>
  <groupId>io.cucumber</groupId>
  <artifactId>cucumber-java</artifactId>
  <version>${cucumber.version}</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>io.cucumber</groupId>
  <artifactId>cucumber-junit-platform-engine</artifactId>
  <version>${cucumber.version}</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>org.junit.platform</groupId>
  <artifactId>junit-platform-suite</artifactId>
  <scope>test</scope>
</dependency>
```

Qué aporta cada una:

| Dependencia | Para qué sirve |
|---|---|
| `cucumber-java` | Permite escribir step definitions con `@Given`, `@When`, `@Then`. |
| `cucumber-junit-platform-engine` | Permite ejecutar Cucumber sobre JUnit Platform. |
| `junit-platform-suite` | Permite crear un runner explícito con `@Suite`. |

## Paso 3: crear el runner

El runner selecciona la carpeta de features de curso y el paquete donde están los steps:

```java
@Suite
@IncludeEngines("cucumber")
@SelectDirectories("../../features/course")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "io.paideia.course.service.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,summary")
class CourseCucumberTest {
}
```

Qué hace cada anotación:

| Anotación | Rol |
|---|---|
| `@Suite` | Declara una suite JUnit Platform. |
| `@IncludeEngines("cucumber")` | Usa el engine de Cucumber. |
| `@SelectDirectories(...)` | Indica qué carpeta de `.feature` ejecutar. |
| `GLUE_PROPERTY_NAME` | Indica dónde buscar step definitions. |
| `PLUGIN_PROPERTY_NAME` | Define salida legible en consola. |

## Paso 4: crear los steps

Cada step Java corresponde a una frase del `.feature`.

Ejemplo:

```java
@Given("no existe un curso con título {string}")
public void noExisteUnCursoConTitulo(String title) {
    this.title = title;
    when(courseRepository.existsByTitle(title)).thenReturn(false);
}
```

La frase:

```gherkin
Given no existe un curso con título "Introducción a Java"
```

se conecta con:

```java
@Given("no existe un curso con título {string}")
```

`{string}` captura `"Introducción a Java"` y lo pasa como parámetro `String title`.

## Paso 5: preparar el contexto por escenario

Cucumber crea una instancia de la clase de steps y ejecuta los pasos. Antes de cada escenario usamos:

```java
@Before
public void setUp() {
    courseRepository = mock(CourseRepository.class);
    courseService = new CourseService(courseRepository, new CourseMapper());
    validator = Validation.buildDefaultValidatorFactory().getValidator();
    response = null;
    error = null;
    title = null;
}
```

Esto deja cada escenario aislado:

| Campo | Uso |
|---|---|
| `courseRepository` | Mock del repositorio. |
| `courseService` | Servicio real bajo prueba. |
| `validator` | Ejecuta validaciones del DTO. |
| `response` | Guarda resultado exitoso. |
| `error` | Guarda error si el caso falla como se espera. |

## Paso 6: ejecutar la acción

Un `When` normalmente ejecuta el comportamiento principal:

```java
@When("el administrador registra un curso publicado con precio {bigdecimal}")
public void elAdministradorRegistraUnCursoPublicadoConPrecio(BigDecimal price) {
    createCourse(title, price, CourseStatus.PUBLISHED);
}
```

El helper `createCourse` arma el DTO, valida y llama al servicio:

```java
CourseRequestDTO request = new CourseRequestDTO(
        title,
        "Curso creado desde un escenario Cucumber.",
        price,
        status);

validate(request);
response = courseService.create(request);
```

## Paso 7: verificar el resultado

Un `Then` expresa el resultado observable:

```java
@Then("el curso queda disponible para consulta")
public void elCursoQuedaDisponibleParaConsulta() {
    assertThat(error).isNull();
    assertThat(response).isNotNull();
    assertThat(response.title()).isEqualTo(title);
    assertThat(response.status()).isEqualTo(CourseStatus.PUBLISHED);
    verify(courseRepository).save(any(CourseEntity.class));
}
```

No basta con que no explote. Se verifica:

1. No hubo error.
2. Existe respuesta.
3. El título coincide.
4. El estado es `PUBLISHED`.
5. Se intentó guardar en repositorio.

## Paso 8: correr los escenarios

Comando:

```powershell
.\mvnw.cmd -pl services/course-service test
```

Salida esperada:

```text
4 scenarios (4 passed)
20 steps (20 passed)
```

Maven también corre los unit tests normales del servicio.

## Cómo leer el resultado

Cuando Cucumber imprime:

```text
Scenario: Crear curso con título nuevo
  Given ...
  When ...
  Then ...
```

significa que encontró el escenario en el `.feature`, buscó una definición Java compatible para cada línea y ejecutó esos métodos.

Si una frase no tiene step definition, Cucumber marca el step como undefined y sugiere un snippet Java.

Si un assert falla, el escenario falla.

## Qué estamos probando realmente

Para `UC-001`, estamos probando estas reglas:

| Regla | Cómo aparece en el escenario |
|---|---|
| `R-001`: gestión por admin | Está expresado en lenguaje del actor; la autorización real entra desde v5. |
| `R-002`: título único | Escenario de creación exitosa y duplicado. |
| `R-003`: precio no negativo | Escenario de precio negativo. |

En v1 no hay seguridad real, por eso el step dice "administrador" como actor del caso de uso, pero no valida JWT/rol todavía.

## Por qué no levantar Spring todavía

Para empezar con Cucumber, conviene mantener el primer caso liviano:

| Opción | Costo | Beneficio |
|---|---|---|
| Servicio + Mockito | Bajo | Aprender Gherkin, steps y reglas rápido. |
| Spring + MockMvc | Medio | Probar contrato HTTP y validación web. |
| Spring + base real | Alto | Probar integración completa. |
| End-to-end | Muy alto | Probar servicios vivos y red real. |

El primer paso busca aprender la mecánica sin ruido de infraestructura.

## Cuándo evolucionarlo

Después de entender este modelo, podemos avanzar así:

1. Mantener Gherkin igual.
2. Cambiar steps para llamar el controller con MockMvc.
3. Agregar Spring Test si queremos validar HTTP real.
4. Agregar Testcontainers si queremos base real.
5. Reservar end-to-end para pocos flujos críticos.

La gracia es que el `.feature` puede mantenerse estable mientras cambia la profundidad técnica del test.

## BDD y TDD

TDD y BDD están relacionados, pero no son lo mismo.

| Práctica | Pregunta principal | Forma típica |
|---|---|---|
| TDD | Cómo diseño y verifico una unidad de código. | Test unitario rojo, implementación, refactor. |
| BDD | Qué comportamiento espera el negocio o usuario. | Escenario Given/When/Then, implementación, conversación. |

TDD suele trabajar cerca del código:

```text
CourseService.create rechaza título duplicado
```

BDD suele trabajar cerca del comportamiento:

```gherkin
Scenario: Rechazar curso con título duplicado
  Given existe un curso con título "Introducción a Java"
  When el administrador registra otro curso con título "Introducción a Java"
  Then la plataforma rechaza la operación por título duplicado
```

Puedes usar ambos juntos:

1. Escribes o acuerdas un escenario BDD para aclarar el comportamiento.
2. Escribes unit tests TDD para diseñar la lógica interna.
3. Implementas la regla.
4. El escenario BDD confirma que el comportamiento completo se entiende.

También puedes hacer BDD primero y luego TDD por debajo. A veces se llama "outside-in":

```text
Escenario BDD rojo
  -> test unitario rojo
    -> código mínimo
  -> test unitario verde
Escenario BDD verde
```

No es obligatorio usar Cucumber para hacer TDD. TDD funciona perfectamente con JUnit y AssertJ.

## Unit tests, cobertura y features

Los unit tests no deberían existir solo para subir cobertura de líneas. La cobertura sirve como señal, pero no dice si el comportamiento importante está bien probado.

Una línea cubierta puede no estar bien verificada:

```java
courseService.create(request);
```

Si el test no afirma nada relevante, hay cobertura pero poca confianza.

Mejor pensar así:

| Herramienta | Para qué usarla |
|---|---|
| Unit tests | Probar reglas pequeñas, ramas, errores y casos borde rápido. |
| Cobertura | Detectar zonas sin tests, no medir calidad por sí sola. |
| Gherkin/Cucumber | Documentar y automatizar ejemplos de negocio importantes. |
| Integration tests | Confirmar que Spring, base de datos, migraciones y HTTP funcionan juntos. |

No conviene convertir cada unit test en un `.feature`.

Ejemplo de cosas que son mejores como unit tests:

- Precio negativo.
- Título vacío.
- Mapeo de entidad a DTO.
- Excepción cuando no existe un curso.
- Validación de tamaño máximo de archivo.

Ejemplo de cosas que sí pueden merecer BDD:

- Preparar un curso para consulta.
- Registrar estudiante.
- Inscribirse y pagar un curso publicado.
- Rechazar inscripción a curso no disponible.
- Consultar recursos propios del estudiante desde v5.

Regla simple:

```text
Si el caso ayuda a conversar con negocio/producto, puede ser BDD.
Si el caso ayuda a proteger una rama técnica específica, mejor unit test.
```

## Propuesta para este laboratorio

Usar BDD de forma selectiva, no para todo.

### Mantener features como documentación viva

Todos los casos de uso importantes pueden tener `.feature`, aunque no todos estén automatizados de inmediato.

| Caso | Feature | Automatizar con Cucumber |
|---|---|---|
| `UC-001` Preparar curso | Sí | Sí, ya implementado. |
| `UC-003` Consultar cursos | Sí | Sí, ya implementado a nivel de servicio. |
| `UC-004` Registrar estudiante | Sí | Sí, buen siguiente paso. |
| `UC-005` Solicitar inscripción | Sí | Sí, cuando el flujo esté estable. |
| `UC-009` Consultar recursos propios | Sí | Sí, útil para permisos desde v5. |
| `UC-010` Acceder a contenido comprado | Sí | Sí, útil para permisos. |

### Automatizar pocos escenarios por flujo

Elegir 1 o 2 escenarios importantes por flujo:

| Flujo | Escenarios BDD recomendados |
|---|---|
| Gestión | Crear curso, registrar contenido. |
| Consulta | Ver cursos con estado. |
| Registro | Registrar estudiante, rechazar email duplicado. |
| Inscripción | Inscripción aprobada, curso no disponible. |
| Recursos propios desde v5 | Consulta permitida, acceso rechazado. |
| Contenido | Acceso permitido, acceso rechazado. |

### Mantener unit tests para reglas finas

Cada servicio debería conservar unit tests rápidos:

| Servicio | Unit tests importantes |
|---|---|
| `course-service` | Duplicado, precio inválido, curso inexistente. |
| `user-service` | Email duplicado, rol `STUDENT`. |
| `enrollment-service` | Duplicados, estados `ENROLLED`/`REJECTED`. |
| `payment-service` | Aprobado/rechazado, razón de rechazo. |
| `content-service` | Archivo vacío, tamaño máximo, acceso denegado. |

### Subir profundidad cuando aporte valor

El mismo `.feature` puede ejecutarse en distintos niveles con el tiempo:

| Etapa | Cómo |
|---|---|
| Inicio | Steps llaman servicios con mocks. |
| API estable | Steps llaman controllers con MockMvc. |
| Persistencia importante | Steps usan Spring + Testcontainers. |
| Flujo crítico | Steps hacen HTTP contra servicios vivos. |

No hay que subir todos los escenarios a end-to-end. Los E2E son más lentos y frágiles; deben ser pocos.

## Modelo recomendado de pirámide

Para este laboratorio:

```text
Muchos unit tests
  Algunos slice/integration tests
    Pocos escenarios Cucumber automatizados
      Muy pocos end-to-end completos
```

Y en términos de propósito:

| Nivel | Pregunta |
|---|---|
| Unit test | La regla interna funciona. |
| Cucumber BDD | El comportamiento de negocio está claro y se cumple. |
| Integration test | Las piezas técnicas colaboran bien. |
| E2E | El sistema completo soporta el flujo principal. |

## Cómo decidir si un escenario va a Cucumber

Preguntas útiles:

1. ¿El escenario representa una conversación de negocio?
2. ¿Lo entendería alguien no técnico?
3. ¿Cubre un flujo importante o una regla crítica?
4. ¿Evita duplicar muchos unit tests?
5. ¿Puede mantenerse estable aunque cambie la implementación?

Si la mayoría es "sí", Cucumber puede aportar.

Si el escenario habla de DTOs, repositorios, headers internos o clases Java, probablemente es mejor unit/integration test.

## Buenas prácticas

- Un `.feature` debe tener un actor e intención principal.
- Los `Given` preparan contexto, no ejecutan otro flujo completo.
- Los `When` deberían ser la acción principal del escenario.
- Los `Then` deberían observar resultados de negocio.
- Evitar detalles HTTP, clases Java y puertos dentro del `.feature`.
- No automatizar todos los casos con Cucumber; elegir los que explican el negocio.

## Resumen

Cucumber conecta tres niveles:

1. Gherkin dice qué debería pasar.
2. Step definitions dicen cómo ejecutar ese ejemplo.
3. JUnit/Maven lo corren como parte de la suite.

En este laboratorio, el primer Cucumber test es un test BDD de aceptación a nivel de servicio: útil para aprender, rápido de ejecutar y suficientemente cercano a las reglas de negocio.
