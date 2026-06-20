Feature: Registro de pagos
  Como plataforma
  Quiero registrar pagos aprobados o rechazados
  Para decidir el resultado de una inscripción

  Rule: R-013 Todo pago debe conservar el monto procesado y su estado final.

    Scenario: Registrar pago aprobado por defecto
      Given existe una solicitud de pago válida
      And no se fuerza resultado de pago por header
      When la plataforma registra el pago
      Then el pago queda registrado como "APPROVED"

    Scenario: Registrar pago rechazado por simulación local
      Given existe una solicitud de pago válida
      And el header "X-Payment-Simulation" indica "REJECTED"
      When la plataforma registra el pago
      Then el pago queda registrado como "REJECTED"
      And el pago conserva la razón de rechazo
