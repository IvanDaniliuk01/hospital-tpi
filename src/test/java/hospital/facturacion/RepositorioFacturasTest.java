package hospital.facturacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RepositorioFacturasTest {

  private final RepositorioFacturas repositorio = new RepositorioFacturas();

  @Test
  @DisplayName("Numera las facturas desde 1 y las encuentra por número")
  void numeraYBusca() {
    Factura primera = repositorio.emitir(1, 2, 1500.0);
    Factura segunda = repositorio.emitir(2, 1, 9000.0);

    assertEquals(1, primera.getNumero());
    assertSame(segunda, repositorio.buscar(2).orElseThrow());
  }

  @Test
  @DisplayName("Un número inexistente devuelve vacío en lugar de lanzar excepción (F6)")
  void numeroInexistenteDevuelveVacio() {
    repositorio.emitir(1, 2, 1500.0);

    assertTrue(repositorio.buscar(0).isEmpty());
    assertTrue(repositorio.buscar(2).isEmpty());
  }
}
