package hospital.facturacion;

import static hospital.facturacion.ReglasFacturacion.BONIFICACION_POR_TURNOS;
import static hospital.facturacion.ReglasFacturacion.FACTOR_DESCUENTO_OBRA_SOCIAL;
import static hospital.facturacion.ReglasFacturacion.PROPORCION_A_CARGO_CON_PLAN_DE_SALUD;
import static hospital.facturacion.ReglasFacturacion.TURNOS_MINIMOS_PARA_BONIFICACION;

/**
 * Reúne las reglas de descuento y bonificación de la facturación.
 *
 * <p>Estos métodos fueron primero extraídos (Extract Method) dentro de la calculadora y luego
 * movidos aquí (Move Method) porque son reglas comerciales puras: dependen solo de la solicitud y
 * de las constantes de negocio, no del proceso de cálculo. Así la calculadora orquesta y esta clase
 * decide.
 */
public class PoliticaDescuentos {

  /** Importe que efectivamente paga el paciente por un estudio según su plan. */
  public double importeACargoDelPaciente(double importeEstudio, SolicitudFacturacion solicitud) {
    if (solicitud.tienePlanDeSalud()) {
      return importeEstudio * PROPORCION_A_CARGO_CON_PLAN_DE_SALUD;
    }
    return importeEstudio;
  }

  /** Aplica el 10 % de descuento de obra social si corresponde. */
  public double aplicarDescuentoObraSocial(double subtotal, SolicitudFacturacion solicitud) {
    if (solicitud.tieneObraSocial()) {
      return subtotal * FACTOR_DESCUENTO_OBRA_SOCIAL;
    }
    return subtotal;
  }

  /** Descuenta la bonificación fija por fidelidad de turnos si corresponde. */
  public double aplicarBonificacionPorTurnos(double subtotal, SolicitudFacturacion solicitud) {
    if (solicitud.getCantidadDeTurnos() > TURNOS_MINIMOS_PARA_BONIFICACION) {
      return subtotal - BONIFICACION_POR_TURNOS;
    }
    return subtotal;
  }
}
