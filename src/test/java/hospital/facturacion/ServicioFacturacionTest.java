package hospital.facturacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hospital.pacientes.Cobertura;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServicioFacturacionTest {

  private static final double DELTA = 1e-9;

  private final RepositorioEstudios estudios = new RepositorioEstudios();
  private final RepositorioFacturas facturas = new RepositorioFacturas();
  private final ServicioFacturacion servicio =
      new ServicioFacturacion(
          estudios, facturas, new CalculadoraFacturacion(new PoliticaDescuentos()));

  @Test
  @DisplayName("Factura solo los estudios pendientes del paciente y los marca facturados")
  void facturaPendientesDelPaciente() {
    estudios.registrar(new Estudio(1, 1000.0));
    estudios.registrar(new Estudio(2, 5000.0));
    estudios.registrar(new Estudio(1, 2000.0));

    Factura factura = servicio.facturarPendientes(1, Cobertura.SIN_COBERTURA, 0).orElseThrow();

    assertEquals(1, factura.getNumero());
    assertEquals(2, factura.getCantidadDeEstudios());
    assertEquals(3000.0, factura.getTotal(), DELTA);
    assertTrue(estudios.pendientesDe(1).isEmpty());
    assertEquals(1, estudios.pendientesDe(2).size());
  }

  @Test
  @DisplayName("La factura nueva queda pendiente de cobro y se numera en orden")
  void facturaNuevaPendiente() {
    estudios.registrar(new Estudio(1, 1000.0));
    servicio.facturarPendientes(1, Cobertura.SIN_COBERTURA, 0);
    estudios.registrar(new Estudio(1, 500.0));

    Factura segunda = servicio.facturarPendientes(1, Cobertura.SIN_COBERTURA, 0).orElseThrow();

    assertEquals(2, segunda.getNumero());
    assertEquals(EstadoFactura.PENDIENTE_DE_COBRO, segunda.getEstado());
  }

  @Test
  @DisplayName("Sin estudios pendientes no se emite factura (corrige D4)")
  void sinEstudiosNoEmiteFactura() {
    assertTrue(servicio.facturarPendientes(1, Cobertura.OBRA_SOCIAL, 4).isEmpty());
    assertTrue(facturas.todas().isEmpty());
  }
}
