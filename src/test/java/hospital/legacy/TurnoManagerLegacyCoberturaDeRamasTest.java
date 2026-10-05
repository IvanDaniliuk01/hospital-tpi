package hospital.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
 * Punto 3 – Suite COMPLETA: 100 % de Branch Coverage del {@code procesar}
 * ORIGINAL de TP1.
 *
 * El método tiene 7 condiciones booleanas (JaCoCo cuenta cada una como una
 * decisión con dos ramas, incluidas las dos mitades del {@code &&}):
 * <pre>
 *  C1  nombrePaciente != null           V/F
 *  C2  !nombrePaciente.equals("")       V/F   (solo se evalúa si C1 es V)
 *  C3  edad > 0                         V/F
 *  C4  obraSocial.equals("OSDE")        V/F
 *  C5  obraSocial.equals("SWISS")       V/F   (solo si C4 es F)
 *  C6  obraSocial.equals("PUBLICA")     V/F   (solo si C5 es F)
 *  C7  urgente == true                  V/F
 * </pre>
 * Conjunto mínimo de casos que recorre las 14 ramas: los siete de esta clase.
 * El caso {@code obraSocial == null} se documenta aparte: no es una rama del
 * método sino una excepción no controlada (NPE) del código original.
 */
@DisplayName("Legacy TurnoManager.procesar – suite completa (100 % ramas)")
class TurnoManagerLegacyCoberturaDeRamasTest {

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
        String texto = consola.toString(StandardCharsets.UTF_8);
        return texto.isEmpty() ? List.of() : Arrays.asList(texto.split("\\R"));
    }

    @Test
    @DisplayName("R1: nombre null -> C1 falsa: no imprime nada ni agrega turno")
    void nombreNull_noHaceNada() {
        // Act
        manager.procesar(null, 30, "OSDE", false);
        manager.mostrar();

        // Assert
        assertEquals(List.of(), lineas());
    }

    @Test
    @DisplayName("R2: nombre vacío -> C1 verdadera, C2 falsa: no hace nada")
    void nombreVacio_noHaceNada() {
        // Act
        manager.procesar("", 30, "OSDE", false);
        manager.mostrar();

        // Assert
        assertEquals(List.of(), lineas());
    }

    @Test
    @DisplayName("R3: edad 0 -> C3 falsa: no hace nada")
    void edadCero_noHaceNada() {
        // Act
        manager.procesar("Elena", 0, "OSDE", false);
        manager.mostrar();

        // Assert
        assertEquals(List.of(), lineas());
    }

    @Test
    @DisplayName("R4: OSDE + urgente -> C4 verdadera, C7 verdadera")
    void osdeUrgente() {
        // Act
        manager.procesar("Ana", 30, "OSDE", true);
        manager.mostrar();

        // Assert
        assertLinesMatch(List.of("Paciente premium", "Turno agregado", "Ana-30-OSDE-URGENTE"), lineas());
    }

    @Test
    @DisplayName("R5: SWISS, no urgente -> C4 falsa, C5 verdadera, C7 falsa")
    void swissNoUrgente() {
        // Act
        manager.procesar("Bruno", 45, "SWISS", false);
        manager.mostrar();

        // Assert
        assertLinesMatch(List.of("Paciente premium", "Turno agregado", "Bruno-45-SWISS"), lineas());
    }

    @Test
    @DisplayName("R6: PUBLICA -> C5 falsa, C6 verdadera")
    void publica() {
        // Act
        manager.procesar("Carla", 60, "PUBLICA", false);
        manager.mostrar();

        // Assert
        assertLinesMatch(List.of("Paciente publico", "Turno agregado", "Carla-60-PUBLICA"), lineas());
    }

    @Test
    @DisplayName("R7: obra social desconocida -> C6 falsa: sin categoría pero el turno se agrega")
    void obraSocialDesconocida_agregaSinCategoria() {
        // Act
        manager.procesar("Dario", 25, "OTRA", true);
        manager.mostrar();

        // Assert
        assertLinesMatch(List.of("Turno agregado", "Dario-25-OTRA-URGENTE"), lineas());
    }

    @Test
    @DisplayName("Fuera de las ramas: obra social null revienta con NPE (defecto heredado, no una rama)")
    void obraSocialNull_lanzaNpe() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> manager.procesar("Fabio", 40, null, false));
    }
}
