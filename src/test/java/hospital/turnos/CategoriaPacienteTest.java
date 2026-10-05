package hospital.turnos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Cobertura de ramas del {@code switch} que reemplazó a la cadena de
 * {@code if/else-if} del original. Cada fila cubre una rama distinta.
 */
@DisplayName("CategoriaPaciente.desde")
class CategoriaPacienteTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        "OSDE,    PREMIUM",
        "SWISS,   PREMIUM",
        "PUBLICA, PUBLICO",
        "OTRA,    SIN_CATEGORIA",
        "osde,    SIN_CATEGORIA"   // sensible a mayúsculas, igual que el original
    })
    void clasificaSegunObraSocial(String obraSocial, CategoriaPaciente esperada) {
        // Arrange (el parámetro ya es el escenario)

        // Act
        CategoriaPaciente obtenida = CategoriaPaciente.desde(obraSocial);

        // Assert
        assertEquals(esperada, obtenida);
    }

    @Test
    @DisplayName("SIN_CATEGORIA no tiene descripción para imprimir")
    void sinCategoria_noTieneDescripcion() {
        // Arrange
        CategoriaPaciente sinCategoria = CategoriaPaciente.SIN_CATEGORIA;

        // Act
        boolean tiene = sinCategoria.tieneDescripcion();

        // Assert
        assertFalse(tiene);
    }

    @Test
    @DisplayName("obra social null sigue lanzando NPE (comportamiento heredado, hallazgo abierto TP1)")
    void obraSocialNull_conservaNpeDelOriginal() {
        // Arrange
        String sinObraSocial = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> CategoriaPaciente.desde(sinObraSocial));
    }
}
