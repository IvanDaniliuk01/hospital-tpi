package hospital.turnos;

import hospital.pacientes.Paciente;

/** Reserva de atención asignada a un paciente. */
public class Turno {

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

  /** Formato heredado: nombre-edad-obraSocial y el sufijo -URGENTE si corresponde. */
  public String formatear() {
    String texto =
        paciente.getNombre() + "-" + paciente.getEdad() + "-" + paciente.getObraSocialDeclarada();
    return urgente ? texto + "-URGENTE" : texto;
  }
}
