package hospital.turnos;

import hospital.pacientes.Paciente;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Turnos dados, en orden. Reemplaza al TurnoManager heredado sin imprimir en consola. */
public class AgendaTurnos {

  private final List<Turno> turnos = new ArrayList<>();

  /**
   * Da un turno si el paciente es válido (nombre no vacío y edad mayor a cero). Si no lo es, no
   * hace nada y devuelve vacío, como el TurnoManager heredado.
   */
  public Optional<Turno> dar(Paciente paciente, boolean urgente) {
    int edad = paciente.getEdad();
    if (paciente.getNombre() == null || paciente.getNombre().isEmpty() || edad <= 0) {
      return Optional.empty();
    }
    Turno turno = new Turno(paciente, urgente);
    turnos.add(turno);
    return Optional.of(turno);
  }

  public List<Turno> todos() {
    return Collections.unmodifiableList(turnos);
  }
}
