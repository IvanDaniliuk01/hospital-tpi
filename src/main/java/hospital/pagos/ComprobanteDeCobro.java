package hospital.pagos;

/** Resultado del proceso de cobro visto desde el hospital. Inmutable. */
public final class ComprobanteDeCobro {

  /** Estado final del cobro. */
  public enum Estado {
    PAGADO,
    SIN_CARGO
  }

  private final String idPaciente;
  private final double importe;
  private final Estado estado;
  private final String codigoAutorizacion;

  private ComprobanteDeCobro(
      String idPaciente, double importe, Estado estado, String codigoAutorizacion) {
    this.idPaciente = idPaciente;
    this.importe = importe;
    this.estado = estado;
    this.codigoAutorizacion = codigoAutorizacion;
  }

  static ComprobanteDeCobro pagado(String idPaciente, double importe, String codigoAutorizacion) {
    return new ComprobanteDeCobro(idPaciente, importe, Estado.PAGADO, codigoAutorizacion);
  }

  static ComprobanteDeCobro sinCargo(String idPaciente, double importe) {
    return new ComprobanteDeCobro(idPaciente, importe, Estado.SIN_CARGO, null);
  }

  public String getIdPaciente() {
    return idPaciente;
  }

  public double getImporte() {
    return importe;
  }

  public Estado getEstado() {
    return estado;
  }

  public String getCodigoAutorizacion() {
    return codigoAutorizacion;
  }

  @Override
  public String toString() {
    return "Comprobante{"
        + idPaciente
        + ", "
        + importe
        + ", "
        + estado
        + (codigoAutorizacion == null ? "" : ", aut=" + codigoAutorizacion)
        + "}";
  }
}
