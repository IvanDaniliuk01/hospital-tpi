package hospital.facturacion;

/** Valores de las reglas de negocio de la facturación. */
public final class ReglasFacturacion {

  /** Proporción del importe de cada estudio que paga un paciente con prepaga. */
  public static final double PROPORCION_A_CARGO_CON_PREPAGA = 0.5;

  /** Factor que se aplica al total cuando el paciente tiene obra social (10 % de descuento). */
  public static final double FACTOR_DESCUENTO_OBRA_SOCIAL = 0.9;

  /** Cantidad de turnos que hay que superar para recibir la bonificación por frecuencia. */
  public static final int TURNOS_MINIMOS_PARA_BONIFICACION = 3;

  /** Importe fijo de la bonificación por frecuencia. */
  public static final double BONIFICACION_POR_FRECUENCIA = 100;

  private ReglasFacturacion() {}
}
