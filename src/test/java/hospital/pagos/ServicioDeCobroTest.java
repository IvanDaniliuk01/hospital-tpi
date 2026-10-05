package hospital.pagos;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import hospital.facturacion.CalculadoraFacturacion;
import hospital.facturacion.SolicitudFacturacion;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Punto 2 – Dobles de prueba ante una API externa de pagos.
 *
 * <p>Las dos clases anidadas prueban el MISMO componente ({@link ServicioDeCobro}) con dos dobles
 * distintos y con preguntas distintas:
 *
 * <ul>
 *   <li>{@link ConStub}: "¿qué hace el servicio cuando la pasarela responde X?" El Assert mira el
 *       ESTADO/resultado del servicio.
 *   <li>{@link ConMock}: "¿el servicio le habló a la pasarela como debe?" El Assert interroga al
 *       MOCK sobre las interacciones recibidas.
 * </ul>
 *
 * La calculadora se usa real: es lógica propia, determinista y barata; no hay razón arquitectónica
 * para reemplazarla.
 */
@DisplayName("ServicioDeCobro con dobles de prueba")
class ServicioDeCobroTest {

  private static final double DELTA = 0.0001;
  private static final String PACIENTE = "P-1001";

  /** 1000 sin plan, con obra social, 4 turnos -> 1000*0.9 - 100 = 800. */
  private static SolicitudFacturacion atencionDe800() {
    return new SolicitudFacturacion(Arrays.asList(1000.0), false, true, 4);
  }

  /** Sin estudios -> total 0: no hay nada que cobrar. */
  private static SolicitudFacturacion atencionSinCargo() {
    return new SolicitudFacturacion(Collections.emptyList(), false, false, 0);
  }

  // ------------------------------------------------------------------
  @Nested
  @DisplayName("Con STUB: inyección de respuestas del proveedor (estado)")
  class ConStub {

    @Test
    @DisplayName(
        "si la pasarela aprueba, el servicio emite comprobante PAGADO con el código de autorización")
    void pasarelaAprueba_emiteComprobantePagado() {
      // Arrange
      PasarelaDePagos pasarela = PasarelaDePagosStub.queAprueba("AUT-777");
      ServicioDeCobro servicio = new ServicioDeCobro(new CalculadoraFacturacion(), pasarela);

      // Act
      ComprobanteDeCobro comprobante = servicio.cobrar(PACIENTE, atencionDe800());

      // Assert
      assertAll(
          () -> assertEquals(ComprobanteDeCobro.Estado.PAGADO, comprobante.getEstado()),
          () -> assertEquals(800.0, comprobante.getImporte(), DELTA),
          () -> assertEquals("AUT-777", comprobante.getCodigoAutorizacion()),
          () -> assertEquals(PACIENTE, comprobante.getIdPaciente()),
          () -> assertTrue(comprobante.toString().contains("aut=AUT-777")));
    }

    @Test
    @DisplayName(
        "si la pasarela rechaza, el servicio lanza CobroNoProcesadoException con el motivo")
    void pasarelaRechaza_lanzaExcepcionControladaConMotivo() {
      // Arrange
      PasarelaDePagos pasarela = PasarelaDePagosStub.queRechaza("fondos insuficientes");
      ServicioDeCobro servicio = new ServicioDeCobro(new CalculadoraFacturacion(), pasarela);

      // Act
      CobroNoProcesadoException error =
          assertThrows(
              CobroNoProcesadoException.class, () -> servicio.cobrar(PACIENTE, atencionDe800()));

      // Assert
      assertAll(
          () -> assertTrue(error.getMessage().contains("fondos insuficientes")),
          () -> assertTrue(error.getMessage().contains(PACIENTE)));
    }

    @Test
    @DisplayName(
        "si la pasarela no responde, la excepción de infraestructura se traduce y conserva la causa")
    void pasarelaNoResponde_traduceLaExcepcionYConservaCausa() {
      // Arrange
      PasarelaDePagos pasarela = PasarelaDePagosStub.queNoResponde();
      ServicioDeCobro servicio = new ServicioDeCobro(new CalculadoraFacturacion(), pasarela);

      // Act
      CobroNoProcesadoException error =
          assertThrows(
              CobroNoProcesadoException.class, () -> servicio.cobrar(PACIENTE, atencionDe800()));

      // Assert
      assertAll(
          () -> assertTrue(error.getMessage().contains("puede reintentarse")),
          () -> assertInstanceOf(PasarelaNoDisponibleException.class, error.getCause()));
    }

    @Test
    @DisplayName("con total cero el servicio devuelve SIN_CARGO (el stub ni se entera)")
    void totalCero_devuelveSinCargo() {
      // Arrange
      PasarelaDePagos pasarela = PasarelaDePagosStub.queRechaza("nunca debería llamarse");
      ServicioDeCobro servicio = new ServicioDeCobro(new CalculadoraFacturacion(), pasarela);

      // Act
      ComprobanteDeCobro comprobante = servicio.cobrar(PACIENTE, atencionSinCargo());

      // Assert (solo podemos mirar el resultado: el stub no registra llamadas)
      assertAll(
          () -> assertEquals(ComprobanteDeCobro.Estado.SIN_CARGO, comprobante.getEstado()),
          () -> assertNull(comprobante.getCodigoAutorizacion()),
          () -> assertFalse(comprobante.toString().contains("aut=")));
    }
  }

  // ------------------------------------------------------------------
  @Nested
  @ExtendWith(MockitoExtension.class)
  @DisplayName("Con MOCK: auditoría del protocolo de interacción con la pasarela")
  class ConMock {

    @Mock private PasarelaDePagos pasarela;

    @Test
    @DisplayName(
        "el servicio llama a cobrar exactamente una vez, con el paciente, el total calculado y la referencia")
    void cobro_invocaLaPasarelaUnaVezConLosArgumentosCorrectos() {
      // Arrange
      when(pasarela.cobrar(anyString(), anyDouble(), anyString()))
          .thenReturn(ResultadoPago.aprobado("AUT-1"));
      ServicioDeCobro servicio = new ServicioDeCobro(new CalculadoraFacturacion(), pasarela);

      // Act
      servicio.cobrar(PACIENTE, atencionDe800());

      // Assert: se interroga al mock, no al resultado
      verify(pasarela, times(1)).cobrar(eq(PACIENTE), eq(800.0), eq("FAC-" + PACIENTE));
      verifyNoMoreInteractions(pasarela);
    }

    @Test
    @DisplayName("con total cero NUNCA se llama a la pasarela")
    void totalCero_noInteractuaConLaPasarela() {
      // Arrange
      ServicioDeCobro servicio = new ServicioDeCobro(new CalculadoraFacturacion(), pasarela);

      // Act
      servicio.cobrar(PACIENTE, atencionSinCargo());

      // Assert
      verify(pasarela, never()).cobrar(anyString(), anyDouble(), anyString());
      verifyNoInteractions(pasarela);
    }

    @Test
    @DisplayName("un rechazo no provoca reintentos: una sola llamada, sin cobros duplicados")
    void rechazo_noReintenta() {
      // Arrange
      when(pasarela.cobrar(anyString(), anyDouble(), anyString()))
          .thenReturn(ResultadoPago.rechazado("tarjeta vencida"));
      ServicioDeCobro servicio = new ServicioDeCobro(new CalculadoraFacturacion(), pasarela);

      // Act
      assertThrows(
          CobroNoProcesadoException.class, () -> servicio.cobrar(PACIENTE, atencionDe800()));

      // Assert
      verify(pasarela, times(1)).cobrar(anyString(), anyDouble(), anyString());
    }

    @Test
    @DisplayName(
        "el importe enviado a la pasarela es el total ya calculado (captura de argumentos)")
    void importeEnviado_esElTotalCalculado() {
      // Arrange
      when(pasarela.cobrar(anyString(), anyDouble(), anyString()))
          .thenReturn(ResultadoPago.aprobado("AUT-2"));
      ServicioDeCobro servicio = new ServicioDeCobro(new CalculadoraFacturacion(), pasarela);
      ArgumentCaptor<Double> importe = ArgumentCaptor.forClass(Double.class);

      // Act
      servicio.cobrar(PACIENTE, new SolicitudFacturacion(Arrays.asList(1000.0), true, true, 0));

      // Assert: 1000 * 0.5 * 0.9 = 450 — la pasarela recibe el neto, no el bruto
      verify(pasarela).cobrar(eq(PACIENTE), importe.capture(), any());
      assertEquals(450.0, importe.getValue(), DELTA);
    }

    @Test
    @DisplayName("varios cobros se envían a la pasarela en el orden en que se procesan")
    void variosCobros_respetanElOrden() {
      // Arrange
      when(pasarela.cobrar(anyString(), anyDouble(), anyString()))
          .thenReturn(ResultadoPago.aprobado("AUT-A"), ResultadoPago.aprobado("AUT-B"));
      ServicioDeCobro servicio = new ServicioDeCobro(new CalculadoraFacturacion(), pasarela);

      // Act
      servicio.cobrar("P-1", atencionDe800());
      servicio.cobrar("P-2", atencionDe800());

      // Assert
      InOrder orden = inOrder(pasarela);
      orden.verify(pasarela).cobrar(eq("P-1"), anyDouble(), eq("FAC-P-1"));
      orden.verify(pasarela).cobrar(eq("P-2"), anyDouble(), eq("FAC-P-2"));
      orden.verifyNoMoreInteractions();
    }
  }

  // ------------------------------------------------------------------
  @Nested
  @DisplayName("Validaciones del servicio")
  class Validaciones {

    @Test
    @DisplayName("id de paciente vacío -> IllegalArgumentException antes de calcular nada")
    void idPacienteVacio_lanzaExcepcion() {
      // Arrange
      ServicioDeCobro servicio =
          new ServicioDeCobro(new CalculadoraFacturacion(), PasarelaDePagosStub.queAprueba("X"));

      // Act & Assert
      assertAll(
          () ->
              assertThrows(
                  IllegalArgumentException.class, () -> servicio.cobrar("  ", atencionDe800())),
          () ->
              assertThrows(
                  IllegalArgumentException.class, () -> servicio.cobrar(null, atencionDe800())));
    }

    @Test
    @DisplayName("sin pasarela o sin calculadora el servicio no se puede construir")
    void dependenciasNull_lanzanExcepcion() {
      // Arrange
      PasarelaDePagos pasarela = PasarelaDePagosStub.queAprueba("X");

      // Act & Assert
      assertAll(
          () ->
              assertThrows(
                  IllegalArgumentException.class, () -> new ServicioDeCobro(null, pasarela)),
          () ->
              assertThrows(
                  IllegalArgumentException.class,
                  () -> new ServicioDeCobro(new CalculadoraFacturacion(), null)));
    }
  }
}
