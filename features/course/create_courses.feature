Feature: Crear cursos
  Como administrador
  Quiero gestionar cursos
  Para disponibilizar cursos

  Rule: R-001 Solo usuarios con rol ADMIN pueden gestionar cursos y contenidos.

    Scenario: Crear curso con título nuevo
      Given no existe un curso con título "Introducción a Java"
      When el administrador registra un curso publicado con precio 50
      Then el curso queda disponible para consulta


  Rule: R-002 El título del curso debe ser único.

    Scenario: Rechazar curso con título duplicado
      Given existe un curso con título "Introducción a Java"
      When el administrador registra otro curso con título "Introducción a Java"
      Then la plataforma rechaza la operación por título duplicado


  Rule: R-003 El precio del curso no puede ser negativo.

    Scenario: Rechazar curso con precio negativo
      Given no existe un curso con título "Arquitectura de software"
      When el administrador registra el curso con precio -10
      Then la plataforma rechaza la operación por precio inválido
