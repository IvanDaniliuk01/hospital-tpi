package hospital.facturacion;

import hospital.pacientes.Cobertura;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Factura los estudios pendientes de un paciente (responsabilidad movida desde la consola). */
public class ServicioFacturacion {

  private final RepositorioEstudios estudios;
  private final RepositorioFacturas facturas;
  private final CalculadoraFacturacion calculadora;

  public ServicioFacturacion(
      RepositorioEstudios estudios,
      RepositorioFacturas facturas,
      CalculadoraFacturacion calculadora) {
    this.estudios = estudios;
    this.facturas = facturas;
    this.calculadora = calculadora;
  }

  /**
   * Emite una factura con los estudios pendientes del paciente. Si no tiene estudios pendientes no
   * emite nada y devuelve vacío.
   */
  public Optional<Factura> facturarPendientes(
      int numeroPaciente, Cobertura cobertura, int cantidadDeTurnos) {
    List<Estudio> pendientes = estudios.pendientesDe(numeroPaciente);
    if (pendientes.isEmpty()) {
      return Optional.empty();
    }
    List<Double> importes = new ArrayList<>();
    for (Estudio estudio : pendientes) {
      importes.add(estudio.getImporte());
      estudio.marcarFacturado();
    }
    SolicitudFacturacion solicitud =
        new SolicitudFacturacion(importes, cobertura, cantidadDeTurnos);
    double total = calculadora.calcularTotal(solicitud);
    return Optional.of(facturas.emitir(numeroPaciente, importes.size(), total));
  }
}
