package hospital.pagos;

/** Doble de prueba (stub): responde siempre lo mismo y recuerda la última llamada. */
class PasarelaFija implements PasarelaDePagos {

  private final ResultadoPago respuesta;
  private int llamadas = 0;
  private String ultimaReferencia;
  private double ultimoImporte;

  PasarelaFija(ResultadoPago respuesta) {
    this.respuesta = respuesta;
  }

  @Override
  public ResultadoPago cobrar(String referencia, double importe) {
    llamadas++;
    ultimaReferencia = referencia;
    ultimoImporte = importe;
    return respuesta;
  }

  int getLlamadas() {
    return llamadas;
  }

  String getUltimaReferencia() {
    return ultimaReferencia;
  }

  double getUltimoImporte() {
    return ultimoImporte;
  }
}
