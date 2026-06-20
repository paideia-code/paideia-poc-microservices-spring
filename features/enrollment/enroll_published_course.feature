Feature: Inscripción en curso publicado
  Como estudiante
  Quiero inscribirme en un curso publicado
  Para acceder al curso comprado

  Rule: R-009 Solo los cursos PUBLISHED aceptan inscripciones.

    Scenario: Estudiante se inscribe con pago aprobado
      Given existe un curso publicado con precio 149.99
      And el estudiante no está inscrito en ese curso
      And el header "X-Student-Id" identifica al estudiante
      And el header "X-Payment-Simulation" indica "APPROVED"
      When el estudiante solicita la inscripción
      Then la inscripción queda confirmada
      And se registra un pago aprobado
      And se registra una notificación para el estudiante

  Rule: R-010 Un estudiante no puede tener dos inscripciones al mismo curso.

    Scenario: Rechazar inscripción duplicada
      Given existe un curso publicado
      And el estudiante ya está inscrito en ese curso
      When el estudiante solicita nuevamente la inscripción
      Then la plataforma rechaza la solicitud por inscripción duplicada
