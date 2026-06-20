Feature: Registro de notificaciones
  Como plataforma
  Quiero registrar notificaciones de eventos relevantes
  Para dejar mensajes pendientes de comunicación al usuario

  Rule: R-016 Toda notificación nueva queda inicialmente en estado PENDING.

    Scenario: Registrar notificación de inscripción confirmada
      Given existe un estudiante destinatario
      And ocurre un evento de inscripción confirmada
      When la plataforma registra la notificación
      Then la notificación queda registrada como "PENDING"
      And la notificación conserva asunto y cuerpo del mensaje
