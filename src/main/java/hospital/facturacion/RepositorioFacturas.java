package hospital.facturacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Facturas emitidas en memoria, numeradas desde 1. */
public class RepositorioFacturas {

  private final List<Factura> facturas = new ArrayList<>();

  public Factura emitir(int numeroPaciente, int cantidadDeEstudios, double total) {
    Factura factura = new Factura(facturas.size() + 1, numeroPaciente, cantidadDeEstudios, total);
    facturas.add(factura);
    return factura;
  }

  /** Busca la factura por número. Vacío si no existe. */
  public Optional<Factura> buscar(int numero) {
    if (numero < 1 || numero > facturas.size()) {
      return Optional.empty();
    }
    return Optional.of(facturas.get(numero - 1));
  }

  public List<Factura> todas() {
    return Collections.unmodifiableList(facturas);
  }
}
