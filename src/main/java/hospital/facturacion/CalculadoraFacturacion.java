package hospital.facturacion;

/** Calcula el total de una factura a partir de una SolicitudFacturacion. */
public class CalculadoraFacturacion {

    public double calcularTotal(SolicitudFacturacion solicitud) {
        double total = 0;
        for (Double importe : solicitud.getImportesDeEstudios()) {
            if (solicitud.tienePlanDeSalud()) {
                total += importe * 0.5;
            } else {
                total += importe;
            }
        }
        if (solicitud.tieneObraSocial()) {
            total = total * 0.9;
        }
        if (solicitud.getCantidadDeTurnos() > 3) {
            total = total - 100;
        }
        return total;
    }
}
