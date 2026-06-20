Feature: Consulta de cursos
  Como visitante, estudiante o administrador
  Quiero consultar los cursos registrados
  Para conocer la oferta y el estado de cada curso

  Rule: R-006 Visitantes, estudiantes y administradores pueden consultar los cursos registrados.

    Scenario: Consultar cursos registrados
      Given existen cursos registrados
      When cualquier actor consulta los cursos
      Then la plataforma muestra la lista de cursos con su estado
