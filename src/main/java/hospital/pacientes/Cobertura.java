package hospital.pacientes;

/** Forma en que se financia la atención del paciente (ADR-0002). */
public enum Cobertura {
  PREPAGA,
  OBRA_SOCIAL,
  SIN_COBERTURA;

  /**
   * Interpreta la obra social declarada. Conserva el comportamiento heredado: distingue mayúsculas,
   * así que "osde" no se reconoce como prepaga (defecto D3, se corrige en F3).
   */
  public static Cobertura desde(String obraSocialDeclarada) {
    if (obraSocialDeclarada.equals("OSDE") || obraSocialDeclarada.equals("SWISS")) {
      return PREPAGA;
    }
    if (obraSocialDeclarada.equals("PUBLICA")) {
      return OBRA_SOCIAL;
    }
    return SIN_COBERTURA;
  }
}
