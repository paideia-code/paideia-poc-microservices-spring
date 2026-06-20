Feature: Crear contenidos
  Como administrador
  Quiero registrar contenidos para un curso
  Para que estén disponibles cuando un estudiante compre el curso

  Rule: R-004 Todo contenido pertenece a un curso.

    Scenario: Registrar material base de un curso
      Given existe un curso publicado
      When el administrador registra un material con nombre "introduccion.pdf" y metadata flexible
      Then el material queda asociado al curso
