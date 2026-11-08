# Turnos y Facturación Hospitalaria

Módulo que registra pacientes, les asigna turnos, factura los estudios realizados y los cobra a través de una pasarela de pagos externa.

## Language

### Pacientes y turnos

**Paciente**:
Persona atendida por el hospital, identificada por su Número de paciente, con nombre, edad y Cobertura.
_Avoid_: Cliente, usuario, afiliado

**Número de paciente**:
Identificador correlativo que el sistema asigna a cada Paciente al registrarlo. Es la única identidad del Paciente.
_Avoid_: ID, DNI, nombre (como identificador)

**Cobertura**:
Forma en que se financia la atención del Paciente: Prepaga, Obra social o Sin cobertura.
_Avoid_: Obra social (para referirse a la cobertura en general), plan, seguro

**Prepaga**:
Cobertura privada (p. ej. OSDE, Swiss Medical) por la cual el Paciente paga solo una parte del importe de cada Estudio.
_Avoid_: Plan de salud, obra social premium

**Obra social**:
Cobertura de la seguridad social que otorga un descuento sobre el total de la Factura.
_Avoid_: Cobertura pública, mutual

**Turno**:
Reserva de atención asignada a un Paciente, que puede ser urgente o no.
_Avoid_: Cita, reserva, solicitud

### Facturación y cobro

**Estudio**:
Prestación médica realizada a un Paciente que tiene un importe a facturar.
_Avoid_: Práctica, servicio, ítem

**Estudio pendiente**:
Estudio registrado que todavía no fue incluido en ninguna Factura.
_Avoid_: Estudio sin facturar, deuda

**Factura**:
Documento que agrupa los Estudios pendientes de un Paciente y calcula el total a su cargo según su Cobertura y su frecuencia de Turnos.
_Avoid_: Liquidación, cuenta, ticket

**Estado de la Factura**:
Situación de cobro de una Factura: Pendiente de cobro, Cobrada o Rechazada. Solo una Factura Pendiente de cobro puede cobrarse.
_Avoid_: Estado de pago, status

**Bonificación por frecuencia**:
Rebaja fija que se aplica en cada Factura de un Paciente que tiene más de tres Turnos registrados.
_Avoid_: Descuento por turnos, premio

**Pasarela de pagos**:
Sistema externo que autoriza o rechaza el cobro de una Factura.
_Avoid_: Procesador, gateway, banco

**Comprobante de cobro**:
Constancia del resultado de cobrar una Factura: aprobado, rechazado o pasarela no disponible.
_Avoid_: Recibo, ticket, factura

### Reportes

**Reporte de sesión**:
Resumen de la actividad desde que se abrió la consola: Turnos (y cuántos urgentes), total facturado, total cobrado y Facturas rechazadas o pendientes.
_Avoid_: Reporte del día, reporte diario, cierre de caja
