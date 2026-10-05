package hospital.facturacion;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Punto 1 – Patrón AAA sobre la función de negocio {@link
 * CalculadoraFacturacion#calcularTotal(SolicitudFacturacion)}.
 *
 * <p>Cada prueba tiene exactamente tres secciones marcadas (Arrange / Act / Assert) y un único Act.
 * Las pruebas están agrupadas por regla de negocio con {@link Nested}, de modo que el reporte de
 * Surefire se lee como una especificación ejecutable de la facturación.
 *
 * <p>Tolerancia numérica: los importes son {@code double}; se compara con un delta de 0.0001 (medio
 * centavo es más que suficiente para la regla).
 */
@DisplayName("CalculadoraFacturacion.calcularTotal")
class CalculadoraFacturacionTest {

  private static final double DELTA = 0.0001;

  private CalculadoraFacturacion calculadora;

  @BeforeEach
  void prepararCalculadora() {
    calculadora = new CalculadoraFacturacion();
  }

  /** Pequeño constructor de escenarios para que el Arrange se lea en una línea. */
  private static SolicitudFacturacion solicitud(
      List<Double> estudios, boolean plan, boolean obraSocial, int turnos) {
    return new SolicitudFacturacion(estudios, plan, obraSocial, turnos);
  }

  @Nested
  @DisplayName("Subtotal de estudios y plan de salud")
  class SubtotalYPlanDeSalud {

    @Test
    @DisplayName("sin plan de salud el paciente paga el 100 % de cada estudio")
    void sinPlan_pagaImporteCompleto() {
      // Arrange
      SolicitudFacturacion sinPlan = solicitud(Arrays.asList(1000.0, 500.0), false, false, 0);

      // Act
      double total = calculadora.calcularTotal(sinPlan);

      // Assert
      assertEquals(1500.0, total, DELTA);
    }

    @Test
    @DisplayName("con plan de salud el paciente paga el 50 % de cada estudio")
    void conPlan_pagaLaMitad() {
      // Arrange
      SolicitudFacturacion conPlan = solicitud(Arrays.asList(1000.0, 500.0), true, false, 0);

      // Act
      double total = calculadora.calcularTotal(conPlan);

      // Assert
      assertEquals(750.0, total, DELTA);
    }

    @Test
    @DisplayName("una lista vacía de estudios factura cero")
    void sinEstudios_totalCero() {
      // Arrange
      SolicitudFacturacion vacia = solicitud(Collections.emptyList(), true, true, 0);

      // Act
      double total = calculadora.calcularTotal(vacia);

      // Assert
      assertEquals(0.0, total, DELTA);
    }

    @Test
    @DisplayName("los importes con decimales se suman sin pérdida apreciable")
    void importesConDecimales_seSumanCorrectamente() {
      // Arrange
      SolicitudFacturacion decimales =
          solicitud(Arrays.asList(333.33, 199.99, 0.01), false, false, 0);

      // Act
      double total = calculadora.calcularTotal(decimales);

      // Assert
      assertEquals(533.33, total, DELTA);
    }
  }

  @Nested
  @DisplayName("Descuento por obra social")
  class DescuentoObraSocial {

    @Test
    @DisplayName("con obra social se descuenta el 10 % del subtotal")
    void conObraSocial_descuentaDiezPorCiento() {
      // Arrange
      SolicitudFacturacion conObraSocial = solicitud(Arrays.asList(1000.0), false, true, 0);

      // Act
      double total = calculadora.calcularTotal(conObraSocial);

      // Assert
      assertEquals(900.0, total, DELTA);
    }

    @Test
    @DisplayName("el descuento de obra social se aplica sobre el subtotal ya reducido por el plan")
    void planYObraSocial_seAplicanEnOrden() {
      // Arrange: 1000 * 0.5 = 500 ; 500 * 0.9 = 450
      SolicitudFacturacion ambos = solicitud(Arrays.asList(1000.0), true, true, 0);

      // Act
      double total = calculadora.calcularTotal(ambos);

      // Assert
      assertEquals(450.0, total, DELTA);
    }
  }

  @Nested
  @DisplayName("Bonificación por cantidad de turnos (umbral estricto: más de 3)")
  class BonificacionPorTurnos {

    @Test
    @DisplayName("con exactamente 3 turnos NO corresponde bonificación (borde inferior)")
    void tresTurnos_sinBonificacion() {
      // Arrange
      SolicitudFacturacion tresTurnos = solicitud(Arrays.asList(1000.0), false, false, 3);

      // Act
      double total = calculadora.calcularTotal(tresTurnos);

      // Assert
      assertEquals(1000.0, total, DELTA);
    }

    @Test
    @DisplayName("con 4 turnos se descuentan $100 fijos (primer valor que califica)")
    void cuatroTurnos_bonificaCien() {
      // Arrange
      SolicitudFacturacion cuatroTurnos = solicitud(Arrays.asList(1000.0), false, false, 4);

      // Act
      double total = calculadora.calcularTotal(cuatroTurnos);

      // Assert
      assertEquals(900.0, total, DELTA);
    }

    @Test
    @DisplayName("la bonificación es fija: no crece con más turnos")
    void muchosTurnos_bonificacionSigueSiendoCien() {
      // Arrange
      SolicitudFacturacion muchosTurnos = solicitud(Arrays.asList(1000.0), false, false, 50);

      // Act
      double total = calculadora.calcularTotal(muchosTurnos);

      // Assert
      assertEquals(900.0, total, DELTA);
    }

    @Test
    @DisplayName("la bonificación se resta al final, después del descuento porcentual")
    void bonificacionDespuesDelDescuento() {
      // Arrange: 1000 * 0.9 = 900 ; 900 - 100 = 800 (y no (1000 - 100) * 0.9 = 810)
      SolicitudFacturacion obraSocialYTurnos = solicitud(Arrays.asList(1000.0), false, true, 4);

      // Act
      double total = calculadora.calcularTotal(obraSocialYTurnos);

      // Assert
      assertEquals(800.0, total, DELTA);
    }

    @ParameterizedTest(name = "estudios=1000, plan={0}, obraSocial={1}, turnos={2} -> {3}")
    @CsvSource({
      "false, false, 0,  1000.0",
      "true,  false, 0,   500.0",
      "false, true,  0,   900.0",
      "true,  true,  0,   450.0",
      "false, false, 4,   900.0",
      "true,  false, 4,   400.0",
      "false, true,  4,   800.0",
      "true,  true,  4,   350.0"
    })
    @DisplayName("tabla de decisión completa de las tres reglas")
    void tablaDeDecision(boolean plan, boolean obraSocial, int turnos, double esperado) {
      // Arrange
      SolicitudFacturacion escenario = solicitud(Arrays.asList(1000.0), plan, obraSocial, turnos);

      // Act
      double total = calculadora.calcularTotal(escenario);

      // Assert
      assertEquals(esperado, total, DELTA);
    }
  }

  @Nested
  @DisplayName("Validaciones e inyección de errores controlados")
  class Validaciones {

    @Test
    @DisplayName("solicitud null -> IllegalArgumentException con mensaje claro")
    void solicitudNull_lanzaExcepcionControlada() {
      // Arrange
      SolicitudFacturacion ninguna = null;

      // Act
      IllegalArgumentException error =
          assertThrows(IllegalArgumentException.class, () -> calculadora.calcularTotal(ninguna));

      // Assert
      assertTrue(
          error.getMessage().contains("solicitud"),
          "el mensaje debe nombrar qué faltó: " + error.getMessage());
    }

    @Test
    @DisplayName("lista de importes null -> la solicitud no se puede construir")
    void listaNull_lanzaExcepcionControlada() {
      // Arrange
      List<Double> sinLista = null;

      // Act
      IllegalArgumentException error =
          assertThrows(
              IllegalArgumentException.class,
              () -> new SolicitudFacturacion(sinLista, false, false, 0));

      // Assert
      assertTrue(error.getMessage().contains("null"));
    }

    @Test
    @DisplayName("un importe null dentro de la lista se rechaza indicando la posición")
    void importeNullEnLaLista_indicaPosicion() {
      // Arrange
      List<Double> conHueco = Arrays.asList(10.0, null, 30.0);

      // Act
      IllegalArgumentException error =
          assertThrows(
              IllegalArgumentException.class,
              () -> new SolicitudFacturacion(conHueco, false, false, 0));

      // Assert
      assertTrue(error.getMessage().contains("posición 1"), error.getMessage());
    }

    @Test
    @DisplayName("política de descuentos null -> la calculadora no se puede construir")
    void politicaNull_lanzaExcepcionControlada() {
      // Arrange
      PoliticaDescuentos ninguna = null;

      // Act & Assert (el Act es la construcción; assertThrows lo envuelve)
      assertThrows(IllegalArgumentException.class, () -> new CalculadoraFacturacion(ninguna));
    }
  }

  @Nested
  @DisplayName("Aseveraciones de identidad y de colecciones sobre el Parameter Object")
  class IdentidadYColecciones {

    @Test
    @DisplayName("la solicitud hace copia defensiva: no comparte referencia con la lista recibida")
    void copiaDefensiva_noCompartenReferencia() {
      // Arrange
      List<Double> original = new ArrayList<>(Arrays.asList(100.0, 200.0));
      SolicitudFacturacion solicitud = solicitud(original, false, false, 0);

      // Act
      original.add(999.0); // mutamos la lista externa después de construir

      // Assert
      assertAll(
          () ->
              assertNotSame(
                  original,
                  solicitud.getImportesDeEstudios(),
                  "debe ser otra instancia (identidad)"),
          () ->
              assertIterableEquals(
                  Arrays.asList(100.0, 200.0),
                  solicitud.getImportesDeEstudios(),
                  "el contenido y el orden se conservan (colección)"),
          () ->
              assertEquals(
                  300.0,
                  calculadora.calcularTotal(solicitud),
                  DELTA,
                  "el total no ve el elemento agregado afuera (estado)"));
    }

    @Test
    @DisplayName("la lista expuesta por la solicitud es de solo lectura")
    void listaExpuesta_esInmutable() {
      // Arrange
      SolicitudFacturacion solicitud = solicitud(Arrays.asList(100.0), false, false, 0);
      List<Double> expuesta = solicitud.getImportesDeEstudios();

      // Act & Assert
      assertThrows(UnsupportedOperationException.class, () -> expuesta.add(1.0));
    }
  }

  @Nested
  @DisplayName("Casos límite conservados del original (caracterización, hallazgos H1–H4 del TP3)")
  class CasosLimiteCaracterizados {

    @Test
    @DisplayName("H1: la bonificación puede dejar un total negativo (comportamiento heredado)")
    void bonificacionMayorQueSubtotal_totalNegativo() {
      // Arrange
      SolicitudFacturacion barato = solicitud(Arrays.asList(50.0), false, false, 4);

      // Act
      double total = calculadora.calcularTotal(barato);

      // Assert: se documenta, no se avala. Cambiarlo requiere decisión de negocio.
      assertEquals(-50.0, total, DELTA);
    }

    @Test
    @DisplayName("H2: un importe negativo se acepta y reduce el total (comportamiento heredado)")
    void importeNegativo_seAcepta() {
      // Arrange
      SolicitudFacturacion conNegativo = solicitud(Arrays.asList(-100.0), true, true, 1);

      // Act
      double total = calculadora.calcularTotal(conNegativo);

      // Assert: -100 * 0.5 = -50 ; -50 * 0.9 = -45
      assertEquals(-45.0, total, DELTA);
    }
  }
}
