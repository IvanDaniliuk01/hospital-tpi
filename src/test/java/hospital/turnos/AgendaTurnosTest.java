package hospital.turnos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hospital.pacientes.Paciente;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AgendaTurnosTest {

  private final AgendaTurnos agenda = new AgendaTurnos();

  @Test
  @DisplayName("Da el turno a un paciente válido y lo guarda en orden")
  void daTurnoAPacienteValido() {
    Paciente ana = new Paciente(1, "Ana", "34", "OSDE");

    Optional<Turno> turno = agenda.dar(ana, false);

    assertTrue(turno.isPresent());
    assertEquals(1, agenda.todos().size());
    assertEquals("Ana-34-OSDE", agenda.todos().get(0).formatear());
  }

  @Test
  @DisplayName("Un turno urgente se muestra con el sufijo -URGENTE")
  void turnoUrgenteLlevaSufijo() {
    Paciente beto = new Paciente(2, "Beto", "51", "PUBLICA");

    Turno turno = agenda.dar(beto, true).orElseThrow();

    assertEquals("Beto-51-PUBLICA-URGENTE", turno.formatear());
  }

  @Test
  @DisplayName("No da turno con edad cero")
  void noDaTurnoConEdadCero() {
    Paciente dario = new Paciente(4, "Dario", "0", "PUBLICA");

    assertFalse(agenda.dar(dario, false).isPresent());
    assertTrue(agenda.todos().isEmpty());
  }

  @Test
  @DisplayName("No da turno con nombre vacío")
  void noDaTurnoConNombreVacio() {
    Paciente anonimo = new Paciente(5, "", "40", "");

    assertFalse(agenda.dar(anonimo, false).isPresent());
  }

  @Test
  @DisplayName("Una edad no numérica lanza NumberFormatException (comportamiento heredado)")
  void edadNoNumericaLanzaExcepcion() {
    Paciente raro = new Paciente(6, "Raro", "abc", "");

    assertThrows(NumberFormatException.class, () -> agenda.dar(raro, false));
  }
}
