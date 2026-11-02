package hospital.pagos;

/**
 * Pasarela determinista para la consola y las pruebas. Responde "no disponible" si el importe
 * termina en ,99, rechaza los importes mayores a 50.000 y aprueba el resto.
 */
public class PasarelaSimulada implements PasarelaDePagos {

  static final double IMPORTE_MAXIMO_APROBADO = 50000;
  static final long CENTAVOS_QUE_SIMULAN_CAIDA = 99;

  @Override
  public ResultadoPago cobrar(String referencia, double importe) {
    long centavos = Math.round(importe * 100);
    if (centavos % 100 == CENTAVOS_QUE_SIMULAN_CAIDA) {
      return ResultadoPago.noDisponible();
    }
    if (importe > IMPORTE_MAXIMO_APROBADO) {
      return ResultadoPago.rechazado();
    }
    return ResultadoPago.aprobado("AUT-" + referencia);
  }
}
