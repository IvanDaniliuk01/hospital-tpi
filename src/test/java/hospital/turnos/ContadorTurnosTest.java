package hospital.turnos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ContadorTurnosTest {

  private final ContadorTurnos contador = new ContadorTurnos();

  @Test
  @DisplayName("Cuenta turnos por paciente, el total y los urgentes")
  void cuentaTurnos() {
    contador.registrar("Ana", false);
    contador.registrar("Ana", true);
    contador.registrar("Beto", true);

    assertEquals(2, contador.turnosDe("Ana"));
    assertEquals(0, contador.turnosDe("Carla"));
    assertEquals(3, contador.total());
    assertEquals(2, contador.urgentes());
  }

  @Test
  @DisplayName("Caracterización D2: dos pacientes con el mismo nombre comparten el contador")
  void homonimosCompartenContador() {
    contador.registrar("Ana Gomez", false);
    contador.registrar("Ana Gomez", false);

    assertEquals(2, contador.turnosDe("Ana Gomez"));
  }
}
