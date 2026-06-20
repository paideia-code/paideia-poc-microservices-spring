Feature: Acceso a contenido comprado
  Como estudiante
  Quiero acceder al contenido de un curso comprado
  Para estudiar los materiales disponibles

  Rule: R-020 El contenido solo se entrega con una inscripción ENROLLED.

    Scenario: Acceder a contenido comprado
      Given existe un estudiante registrado
      And el estudiante tiene una inscripción "ENROLLED" en el curso
      And el curso tiene materiales registrados
      And el header "X-Student-Id" identifica al estudiante
      When el estudiante consulta el contenido del curso
      Then la plataforma muestra los materiales del curso

    Scenario: Rechazar acceso a contenido no comprado
      Given existe un estudiante registrado
      And el estudiante no tiene una inscripción "ENROLLED" en el curso
      And el header "X-Student-Id" identifica al estudiante
      When el estudiante consulta el contenido del curso
      Then la plataforma rechaza la operación por permisos insuficientes
