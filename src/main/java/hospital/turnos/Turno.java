package hospital.turnos;

/**
 * Turno médico asignado a un paciente.
 *
 * <p>En el código original el turno era un {@code String} armado por concatenación (Primitive
 * Obsession). Ahora es un objeto propio y el formato de texto vive en un único método, {@link
 * #formatear()}.
 */
public class Turno {

  private static final String SEPARADOR = "-";
  private static final String MARCA_URGENTE = "URGENTE";

  private final Paciente paciente;
  private final boolean urgente;

  public Turno(Paciente paciente, boolean urgente) {
    this.paciente = paciente;
    this.urgente = urgente;
  }

  public Paciente getPaciente() {
    return paciente;
  }

  public boolean esUrgente() {
    return urgente;
  }

  /**
   * Representación textual del turno, idéntica a la del sistema original: {@code
   * nombre-edad-obraSocial[-URGENTE]}.
   */
  public String formatear() {
    StringBuilder texto =
        new StringBuilder()
            .append(paciente.getNombre())
            .append(SEPARADOR)
            .append(paciente.getEdad())
            .append(SEPARADOR)
            .append(paciente.getObraSocial());

    if (urgente) {
      texto.append(SEPARADOR).append(MARCA_URGENTE);
    }
    return texto.toString();
  }

  @Override
  public String toString() {
    return formatear();
  }
}
