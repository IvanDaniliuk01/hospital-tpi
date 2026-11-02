package hospital.facturacion;

import hospital.pacientes.Cobertura;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Datos necesarios para calcular el total de una factura (Parameter Object). */
public final class SolicitudFacturacion {

  private final List<Double> importesDeEstudios;
  private final Cobertura cobertura;
  private final int cantidadDeTurnos;

  public SolicitudFacturacion(
      List<Double> importesDeEstudios, Cobertura cobertura, int cantidadDeTurnos) {
    if (importesDeEstudios == null) {
      throw new IllegalArgumentException("La lista de importes no puede ser null");
    }
    this.importesDeEstudios = Collections.unmodifiableList(new ArrayList<>(importesDeEstudios));
    this.cobertura = cobertura;
    this.cantidadDeTurnos = cantidadDeTurnos;
  }

  public List<Double> getImportesDeEstudios() {
    return importesDeEstudios;
  }

  public Cobertura getCobertura() {
    return cobertura;
  }

  public int getCantidadDeTurnos() {
    return cantidadDeTurnos;
  }
}
