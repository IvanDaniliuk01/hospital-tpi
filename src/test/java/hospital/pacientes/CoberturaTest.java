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
  @DisplayName("No distingue mayúsculas ni espacios (corrige D3)")
  void ignoraMayusculasYEspacios() {
    assertEquals(Cobertura.PREPAGA, Cobertura.desde("osde"));
    assertEquals(Cobertura.PREPAGA, Cobertura.desde(" Swiss "));
    assertEquals(Cobertura.OBRA_SOCIAL, Cobertura.desde("publica"));
  }

  @Test
  @DisplayName("null es sin cobertura")
  void nullEsSinCobertura() {
    assertEquals(Cobertura.SIN_COBERTURA, Cobertura.desde(null));
  }
}
