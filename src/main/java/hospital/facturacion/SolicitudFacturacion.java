package hospital.facturacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Datos necesarios para calcular el total de una factura (Parameter Object). */
public final class SolicitudFacturacion {

    private final List<Double> importesDeEstudios;
    private final boolean tienePlanDeSalud;
    private final boolean tieneObraSocial;
    private final int cantidadDeTurnos;

    public SolicitudFacturacion(
            List<Double> importesDeEstudios,
            boolean tienePlanDeSalud,
            boolean tieneObraSocial,
            int cantidadDeTurnos) {
        if (importesDeEstudios == null) {
            throw new IllegalArgumentException("La lista de importes no puede ser null");
        }
        this.importesDeEstudios = Collections.unmodifiableList(new ArrayList<>(importesDeEstudios));
        this.tienePlanDeSalud = tienePlanDeSalud;
        this.tieneObraSocial = tieneObraSocial;
        this.cantidadDeTurnos = cantidadDeTurnos;
    }

    public List<Double> getImportesDeEstudios() {
        return importesDeEstudios;
    }

    public boolean tienePlanDeSalud() {
        return tienePlanDeSalud;
    }

    public boolean tieneObraSocial() {
        return tieneObraSocial;
    }

    public int getCantidadDeTurnos() {
        return cantidadDeTurnos;
    }
}
