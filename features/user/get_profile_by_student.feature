@v5 @security
Feature: Consulta de perfil de un estudiante
  Como estudiante
  Quiero consultar mi perfil
  Para revisar mis datos de cuenta

  Rule: R-019 El estudiante autenticado solo puede consultar sus recursos propios: perfil, inscripciones y notificaciones.

    Scenario: Consultar perfil propio
      Given existe un estudiante registrado
      When estudiante consulta su perfil
      Then la plataforma muestra los datos de su perfil
