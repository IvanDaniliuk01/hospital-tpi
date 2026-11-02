package hospital.facturacion;

/** Calcula el total de una factura a partir de una SolicitudFacturacion. */
public class CalculadoraFacturacion {

    private final PoliticaDescuentos politica;

    public CalculadoraFacturacion(PoliticaDescuentos politica) {
        this.politica = politica;
    }

    public double calcularTotal(SolicitudFacturacion solicitud) {
        double total = 0;
        for (Double importe : solicitud.getImportesDeEstudios()) {
            total += politica.importeACargoDelPaciente(importe, solicitud.tienePlanDeSalud());
        }
        total = politica.aplicarDescuentoObraSocial(total, solicitud.tieneObraSocial());
        return politica.aplicarBonificacionPorFrecuencia(total, solicitud.getCantidadDeTurnos());
    }
}
