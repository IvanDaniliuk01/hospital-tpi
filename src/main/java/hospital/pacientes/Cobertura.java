package hospital.pacientes;

import java.util.Locale;

/** Forma en que se financia la atención del paciente (ADR-0002). */
public enum Cobertura {
  PREPAGA,
  OBRA_SOCIAL,
  SIN_COBERTURA;

  /**
   * Interpreta la obra social declarada sin distinguir mayúsculas ni espacios. Un texto null, vacío
   * o desconocido es sin cobertura.
   */
  public static Cobertura desde(String obraSocialDeclarada) {
    if (obraSocialDeclarada == null) {
      return SIN_COBERTURA;
    }
    String normalizada = obraSocialDeclarada.trim().toUpperCase(Locale.ROOT);
    if ("OSDE".equals(normalizada) || "SWISS".equals(normalizada)) {
      return PREPAGA;
    }
    if ("PUBLICA".equals(normalizada)) {
      return OBRA_SOCIAL;
    }
    return SIN_COBERTURA;
  }
}
