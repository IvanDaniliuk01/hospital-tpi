package hospital.pacientes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CoberturaTest {

    @Test
    @DisplayName("OSDE y SWISS son prepagas")
    void osdeYSwissSonPrepagas() {
        assertEquals(Cobertura.PREPAGA, Cobertura.desde("OSDE"));
        assertEquals(Cobertura.PREPAGA, Cobertura.desde("SWISS"));
    }

    @Test
    @DisplayName("PUBLICA es obra social")
    void publicaEsObraSocial() {
        assertEquals(Cobertura.OBRA_SOCIAL, Cobertura.desde("PUBLICA"));
    }

    @Test
    @DisplayName("Un texto vacío o desconocido es sin cobertura")
    void textoVacioODesconocidoEsSinCobertura() {
        assertEquals(Cobertura.SIN_COBERTURA, Cobertura.desde(""));
        assertEquals(Cobertura.SIN_COBERTURA, Cobertura.desde("IOMA"));
    }

    @Test
    @DisplayName("Caracterización D3: \"osde\" en minúsculas no se reconoce como prepaga")
    void minusculasNoSeReconocen() {
        assertEquals(Cobertura.SIN_COBERTURA, Cobertura.desde("osde"));
    }
}
