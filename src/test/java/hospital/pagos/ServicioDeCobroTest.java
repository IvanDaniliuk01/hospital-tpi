package hospital.pagos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hospital.facturacion.EstadoFactura;
import hospital.facturacion.Factura;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServicioDeCobroTest {

  private static final double DELTA = 1e-9;

  @Test
  @DisplayName("Un cobro aprobado marca la factura cobrada y suma al total cobrado")
  void cobroAprobado() {
    PasarelaFija pasarela = new PasarelaFija(ResultadoPago.aprobado("AUT-7"));
    ServicioDeCobro servicio = new ServicioDeCobro(pasarela);
    Factura factura = new Factura(7, 1, 2, 1400.0);

    ResultadoPago resultado = servicio.cobrar(factura);

    assertEquals(EstadoPago.APROBADO, resultado.getEstado());
    assertEquals(EstadoFactura.COBRADA, factura.getEstado());
    assertEquals(1400.0, servicio.getTotalCobrado(), DELTA);
    assertEquals("7", pasarela.getUltimaReferencia());
    assertEquals(1400.0, pasarela.getUltimoImporte(), DELTA);
  }

  @Test
  @DisplayName("Un cobro rechazado marca la factura rechazada y no suma")
  void cobroRechazado() {
    ServicioDeCobro servicio = new ServicioDeCobro(new PasarelaFija(ResultadoPago.rechazado()));
    Factura factura = new Factura(4, 5, 1, 60000.0);

    servicio.cobrar(factura);

    assertEquals(EstadoFactura.RECHAZADA, factura.getEstado());
    assertEquals(0.0, servicio.getTotalCobrado(), DELTA);
  }

  @Test
  @DisplayName("Si la pasarela no está disponible la factura sigue pendiente")
  void pasarelaNoDisponible() {
    ServicioDeCobro servicio = new ServicioDeCobro(new PasarelaFija(ResultadoPago.noDisponible()));
    Factura factura = new Factura(5, 3, 1, 500.99);

    servicio.cobrar(factura);

    assertEquals(EstadoFactura.PENDIENTE_DE_COBRO, factura.getEstado());
    assertEquals(0.0, servicio.getTotalCobrado(), DELTA);
  }

  @Test
  @DisplayName("Una factura cobrada no se puede volver a cobrar (corrige D5)")
  void noPermiteDobleCobro() {
    PasarelaFija pasarela = new PasarelaFija(ResultadoPago.aprobado("AUT-1"));
    ServicioDeCobro servicio = new ServicioDeCobro(pasarela);
    Factura factura = new Factura(1, 1, 2, 1400.0);
    servicio.cobrar(factura);

    assertThrows(FacturaNoCobrableException.class, () -> servicio.cobrar(factura));
    assertEquals(1, pasarela.getLlamadas());
    assertEquals(1400.0, servicio.getTotalCobrado(), DELTA);
  }

  @Test
  @DisplayName("Una factura rechazada no se vuelve a enviar a la pasarela")
  void noReintentaFacturaRechazada() {
    PasarelaFija pasarela = new PasarelaFija(ResultadoPago.rechazado());
    ServicioDeCobro servicio = new ServicioDeCobro(pasarela);
    Factura factura = new Factura(4, 5, 1, 60000.0);
    servicio.cobrar(factura);

    assertThrows(FacturaNoCobrableException.class, () -> servicio.cobrar(factura));
    assertEquals(1, pasarela.getLlamadas());
  }

  @Test
  @DisplayName("Si la pasarela no estaba disponible se puede reintentar")
  void reintentaSiNoEstabaDisponible() {
    PasarelaFija pasarela = new PasarelaFija(ResultadoPago.noDisponible());
    ServicioDeCobro servicio = new ServicioDeCobro(pasarela);
    Factura factura = new Factura(5, 3, 1, 500.99);
    servicio.cobrar(factura);

    servicio.cobrar(factura);

    assertEquals(2, pasarela.getLlamadas());
  }
}
