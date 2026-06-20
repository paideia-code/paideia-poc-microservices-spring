@v5 @security
Feature: Consulta de notificaciones de un estudiante
  Como estudiante
  Quiero consultar mis notificaciones
  Para revisar mensajes asociados a mis compras

  Rule: R-019 El estudiante autenticado solo puede consultar sus recursos propios: perfil, inscripciones y notificaciones.

    Scenario: Consultar notificaciones propias
      Given existen notificaciones del estudiante
      When estudiante consulta sus notificaciones
      Then la plataforma muestra la lista de sus notificaciones
