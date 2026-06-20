Feature: Rechazo de inscripción en curso no disponible
  Como estudiante
  Quiero recibir rechazo claro si un curso no acepta inscripciones
  Para no iniciar pagos sobre cursos no disponibles

  Rule: R-009 Solo los cursos PUBLISHED aceptan inscripciones.

    Scenario: Rechazar inscripción en curso borrador
      Given existe un curso en estado "DRAFT"
      And el header "X-Student-Id" identifica al estudiante
      When el estudiante solicita la inscripción
      Then la plataforma rechaza la solicitud por curso no disponible
      And no se registra pago
      And no se registra inscripción

    Scenario: Rechazar inscripción en curso archivado
      Given existe un curso en estado "ARCHIVED"
      And el header "X-Student-Id" identifica al estudiante
      When el estudiante solicita la inscripción
      Then la plataforma rechaza la solicitud por curso no disponible
      And no se registra pago
      And no se registra inscripción
