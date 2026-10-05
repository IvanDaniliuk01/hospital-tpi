package hospital.legacy;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Punto 3 – Suite "INGENUA" sobre el {@code procesar} ORIGINAL de TP1.
 *
 * <p>Tres pruebas de camino feliz alcanzan el 100 % de Statement Coverage del método: todas las
 * líneas ejecutables se ejecutan al menos una vez. Sin embargo dejan sin recorrer las ramas FALSAS
 * de los condicionales que no tienen {@code else} (nombre null, nombre vacío, edad no positiva) y
 * la rama falsa del último {@code else if} (obra social desconocida).
 *
 * <p>Se ejecuta sola con: {@code mvn clean test -Dtest=TurnoManagerLegacyCoberturaIngenuaTest} y el
 * reporte de JaCoCo muestra 100 % de líneas pero no de ramas.
 */
@DisplayName("Legacy TurnoManager.procesar – suite ingenua (100 % sentencias, ramas incompletas)")
class TurnoManagerLegacyCoberturaIngenuaTest {

  private final ByteArrayOutputStream consola = new ByteArrayOutputStream();
  private PrintStream salidaOriginal;
  private TurnoManager manager;

  @BeforeEach
  void capturarConsola() {
    salidaOriginal = System.out;
    System.setOut(new PrintStream(consola, true, StandardCharsets.UTF_8));
    manager = new TurnoManager();
  }

  @AfterEach
  void restaurarConsola() {
    System.setOut(salidaOriginal);
  }

  private List<String> lineas() {
    return Arrays.asList(consola.toString(StandardCharsets.UTF_8).split("\\R"));
  }

  @Test
  @DisplayName("T1: OSDE + urgente")
  void osdeUrgente() {
    // Arrange (paciente válido, obra social premium)

    // Act
    manager.procesar("Ana", 30, "OSDE", true);
    manager.mostrar();

    // Assert
    assertLinesMatch(
        List.of("Paciente premium", "Turno agregado", "Ana-30-OSDE-URGENTE"), lineas());
  }

  @Test
  @DisplayName("T2: SWISS, no urgente")
  void swissNoUrgente() {
    // Act
    manager.procesar("Bruno", 45, "SWISS", false);
    manager.mostrar();

    // Assert
    assertLinesMatch(List.of("Paciente premium", "Turno agregado", "Bruno-45-SWISS"), lineas());
  }

  @Test
  @DisplayName("T3: PUBLICA, no urgente")
  void publicaNoUrgente() {
    // Act
    manager.procesar("Carla", 60, "PUBLICA", false);
    manager.mostrar();

    // Assert
    assertLinesMatch(List.of("Paciente publico", "Turno agregado", "Carla-60-PUBLICA"), lineas());
  }
}
