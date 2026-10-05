package hospital.turnos;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas AAA del gestor de turnos refactorizado (TP1), con foco en
 * aseveraciones de colecciones y de identidad.
 *
 * La salida por consola se captura inyectando un {@link PrintStream}
 * propio: es la "costura" que dejó el refactor de TP1 para hacer testeable
 * la clase sin tocar {@code System.out}.
 */
@DisplayName("TurnoManager (refactor TP1)")
class TurnoManagerTest {

    private ByteArrayOutputStream consola;
    private TurnoManager manager;

    @BeforeEach
    void prepararGestorConConsolaCapturada() {
        consola = new ByteArrayOutputStream();
        manager = new TurnoManager(new PrintStream(consola, true, StandardCharsets.UTF_8));
    }

    private List<String> lineasImpresas() {
        String texto = consola.toString(StandardCharsets.UTF_8);
        return texto.isEmpty() ? List.of() : Arrays.asList(texto.split("\\R"));
    }

    @Test
    @DisplayName("los turnos válidos se guardan en el orden en que se procesan")
    void turnosValidos_seGuardanEnOrden() {
        // Arrange
        manager.procesar("Ana", 30, "OSDE", false);
        manager.procesar("Bruno", 45, "SWISS", true);

        // Act
        List<String> formateados = manager.getTurnos().stream()
                .map(Turno::formatear)
                .collect(Collectors.toList());

        // Assert (colección: contenido, orden y tamaño en una sola aseveración)
        assertIterableEquals(List.of("Ana-30-OSDE", "Bruno-45-SWISS-URGENTE"), formateados);
    }

    @Test
    @DisplayName("un paciente inválido no genera turno ni salida por consola")
    void pacienteInvalido_seIgnoraEnSilencio() {
        // Arrange
        Paciente sinNombre = new Paciente("", 40, "OSDE");

        // Act
        manager.registrarTurno(sinNombre, false);

        // Assert
        assertAll(
            () -> assertTrue(manager.getTurnos().isEmpty(), "no debe registrar el turno"),
            () -> assertTrue(lineasImpresas().isEmpty(), "no debe imprimir nada")
        );
    }

    @Test
    @DisplayName("nombre null también se ignora (mismo silencio que el original)")
    void nombreNull_seIgnora() {
        // Arrange
        Paciente sinNombre = new Paciente(null, 40, "OSDE");

        // Act
        manager.registrarTurno(sinNombre, true);

        // Assert
        assertTrue(manager.getTurnos().isEmpty());
    }

    @Test
    @DisplayName("edad no positiva también se ignora")
    void edadCero_seIgnora() {
        // Arrange
        Paciente sinEdad = new Paciente("Elena", 0, "OSDE");

        // Act
        manager.registrarTurno(sinEdad, true);

        // Assert
        assertTrue(manager.getTurnos().isEmpty());
    }

    @Test
    @DisplayName("toString del turno coincide con formatear (una sola fuente de verdad del formato)")
    void toStringDelTurno_esElFormato() {
        // Arrange
        Turno turno = new Turno(new Paciente("Ana", 30, "OSDE"), true);

        // Act
        String texto = turno.toString();

        // Assert
        assertEquals("Ana-30-OSDE-URGENTE", texto);
        assertEquals(turno.formatear(), texto);
    }

    @Test
    @DisplayName("la salida por consola reproduce exactamente la del sistema original")
    void salidaPorConsola_esIdenticaAlOriginal() {
        // Arrange
        manager.procesar("Carla", 60, "PUBLICA", false);
        manager.procesar("Dario", 25, "OTRA", true);

        // Act
        manager.mostrar();

        // Assert (colección ordenada de líneas)
        assertLinesMatch(List.of(
                "Paciente publico",
                "Turno agregado",
                "Turno agregado",
                "Carla-60-PUBLICA",
                "Dario-25-OTRA-URGENTE"), lineasImpresas());
    }

    @Test
    @DisplayName("el turno guardado conserva la identidad del paciente registrado")
    void turnoGuardado_conservaLaMismaInstanciaDePaciente() {
        // Arrange
        Paciente elena = new Paciente("Elena", 33, "SWISS");

        // Act
        manager.registrarTurno(elena, false);

        // Assert (identidad: misma referencia, no una copia equivalente)
        assertSame(elena, manager.getTurnos().get(0).getPaciente());
    }

    @Test
    @DisplayName("la vista de turnos es de solo lectura")
    void vistaDeTurnos_esInmutable() {
        // Arrange
        manager.procesar("Ana", 30, "OSDE", false);
        List<Turno> vista = manager.getTurnos();

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> vista.clear());
        assertEquals(1, manager.getTurnos().size());
    }
}
