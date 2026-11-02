package hospital.pagos;

import hospital.facturacion.Factura;

/** Se intentó cobrar una factura que no está pendiente de cobro. */
public class FacturaNoCobrableException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public FacturaNoCobrableException(Factura factura) {
    super(
        "La factura "
            + factura.getNumero()
            + " no se puede cobrar porque esta "
            + factura.getEstado().name().replace('_', ' '));
  }
}
