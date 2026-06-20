@v5 @security
Feature: Consulta de inscripciones de un estudiante
  Como estudiante
  Quiero consultar mis inscripciones
  Para revisar el estado de mis compras de cursos

  Rule: R-019 El estudiante autenticado solo puede consultar sus recursos propios: perfil, inscripciones y notificaciones.

    Scenario: Consultar inscripciones propias
      Given existen inscripciones del estudiante
      When estudiante consulta sus inscripciones
      Then la plataforma muestra la lista de sus inscripciones
