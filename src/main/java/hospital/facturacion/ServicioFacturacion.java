package hospital.facturacion;

import hospital.pacientes.Cobertura;
import java.util.ArrayList;
import java.util.List;

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

    public Factura facturarPendientes(
            int numeroPaciente, Cobertura cobertura, int cantidadDeTurnos) {
        List<Double> importes = new ArrayList<>();
        for (Estudio estudio : estudios.pendientesDe(numeroPaciente)) {
            importes.add(estudio.getImporte());
            estudio.marcarFacturado();
        }
        SolicitudFacturacion solicitud =
                new SolicitudFacturacion(importes, cobertura, cantidadDeTurnos);
        double total = calculadora.calcularTotal(solicitud);
        return facturas.emitir(numeroPaciente, importes.size(), total);
    }
}
