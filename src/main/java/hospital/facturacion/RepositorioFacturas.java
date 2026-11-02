package hospital.facturacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Facturas emitidas en memoria, numeradas desde 1. */
public class RepositorioFacturas {

  private final List<Factura> facturas = new ArrayList<>();

  public Factura emitir(int numeroPaciente, int cantidadDeEstudios, double total) {
    Factura factura = new Factura(facturas.size() + 1, numeroPaciente, cantidadDeEstudios, total);
    facturas.add(factura);
    return factura;
  }

  /** Lanza IndexOutOfBoundsException si el número no existe (comportamiento heredado). */
  public Factura buscar(int numero) {
    return facturas.get(numero - 1);
  }

  public List<Factura> todas() {
    return Collections.unmodifiableList(facturas);
  }
}
