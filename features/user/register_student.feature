Feature: Registro e inicio de sesión de estudiantes
  Como visitante
  Quiero registrarme como estudiante
  Para inscribirme a cursos y acceder al contenido comprado

  Rule: R-007 El email del estudiante debe ser único.

    Scenario: Rechazar email duplicado
      Given existe un estudiante con email "student@paideia.io"
      When otra persona se registra con email "student@paideia.io"
      Then la plataforma rechaza la operación por email duplicado

  Rule: R-008 El registro público siempre crea usuarios con rol STUDENT.

    Scenario: Auto-registrar estudiante
      Given no existe un estudiante con email "student@paideia.io"
      When la persona se registra con email "student@paideia.io"
      Then el estudiante queda registrado con rol "STUDENT"
