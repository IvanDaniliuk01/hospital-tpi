package hospital.pagos;

/**
 * STUB de la pasarela de pagos, escrito a mano.
 *
 * <p>No tiene lógica: devuelve siempre la respuesta "enlatada" que se le configuró (o lanza la
 * excepción configurada). Su único propósito es inyectar una entrada indirecta al {@link
 * ServicioDeCobro} para observar cómo reacciona el servicio ante cada respuesta posible del
 * proveedor, sin red ni proveedor real.
 *
 * <p>Deliberadamente NO registra qué se le pidió: eso es trabajo de un Mock.
 */
final class PasarelaDePagosStub implements PasarelaDePagos {

  private final ResultadoPago respuestaFija;
  private final RuntimeException fallaFija;

  private PasarelaDePagosStub(ResultadoPago respuestaFija, RuntimeException fallaFija) {
    this.respuestaFija = respuestaFija;
    this.fallaFija = fallaFija;
  }

  static PasarelaDePagosStub queAprueba(String codigoAutorizacion) {
    return new PasarelaDePagosStub(ResultadoPago.aprobado(codigoAutorizacion), null);
  }

  static PasarelaDePagosStub queRechaza(String motivo) {
    return new PasarelaDePagosStub(ResultadoPago.rechazado(motivo), null);
  }

  static PasarelaDePagosStub queNoResponde() {
    return new PasarelaDePagosStub(null, new PasarelaNoDisponibleException("timeout tras 30 s"));
  }

  @Override
  public ResultadoPago cobrar(String idPaciente, double importe, String referencia) {
    if (fallaFija != null) {
      throw fallaFija;
    }
    return respuestaFija;
  }
}
