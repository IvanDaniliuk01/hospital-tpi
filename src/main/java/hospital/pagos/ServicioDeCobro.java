package hospital.pagos;

import hospital.facturacion.CalculadoraFacturacion;
import hospital.facturacion.SolicitudFacturacion;

/**
 * Caso de uso: cobrar al paciente el total de una atención.
 *
 * <p>Orquesta dos colaboradores: la {@link CalculadoraFacturacion} (lógica de negocio propia,
 * determinista) y la {@link PasarelaDePagos} (API externa, con efectos secundarios irreversibles).
 * Ambos se inyectan por constructor, de modo que en las pruebas la pasarela real se reemplaza por
 * un doble.
 *
 * <p>Reglas:
 *
 * <ul>
 *   <li>Si el total es cero o negativo no se llama a la pasarela (no hay nada que cobrar; cobrar un
 *       negativo sería un error grave).
 *   <li>Se llama a la pasarela exactamente una vez por cobro, con una referencia única, para evitar
 *       cobros duplicados.
 *   <li>Un rechazo o una caída de la pasarela se traducen a {@link CobroNoProcesadoException} con
 *       mensaje claro.
 * </ul>
 */
public class ServicioDeCobro {

  private static final String PREFIJO_REFERENCIA = "FAC-";

  private final CalculadoraFacturacion calculadora;
  private final PasarelaDePagos pasarela;

  public ServicioDeCobro(CalculadoraFacturacion calculadora, PasarelaDePagos pasarela) {
    if (calculadora == null || pasarela == null) {
      throw new IllegalArgumentException("Calculadora y pasarela son obligatorias.");
    }
    this.calculadora = calculadora;
    this.pasarela = pasarela;
  }

  public ComprobanteDeCobro cobrar(String idPaciente, SolicitudFacturacion solicitud) {
    if (idPaciente == null || idPaciente.isBlank()) {
      throw new IllegalArgumentException("El id del paciente es obligatorio.");
    }
    double total = calculadora.calcularTotal(solicitud);
    if (total <= 0) {
      return ComprobanteDeCobro.sinCargo(idPaciente, total);
    }
    ResultadoPago resultado = solicitarCobro(idPaciente, total);
    if (!resultado.fueAprobado()) {
      throw new CobroNoProcesadoException(
          "Cobro rechazado por la pasarela para el paciente "
              + idPaciente
              + ": "
              + resultado.getMotivoRechazo());
    }
    return ComprobanteDeCobro.pagado(idPaciente, total, resultado.getCodigoAutorizacion());
  }

  private ResultadoPago solicitarCobro(String idPaciente, double total) {
    try {
      return pasarela.cobrar(idPaciente, total, referenciaPara(idPaciente));
    } catch (PasarelaNoDisponibleException e) {
      throw new CobroNoProcesadoException(
          "La pasarela de pagos no respondió; el cobro del paciente "
              + idPaciente
              + " no fue registrado y puede reintentarse.",
          e);
    }
  }

  /** Referencia estable por paciente: la pasarela la usa como clave de idempotencia. */
  private static String referenciaPara(String idPaciente) {
    return PREFIJO_REFERENCIA + idPaciente;
  }
}
