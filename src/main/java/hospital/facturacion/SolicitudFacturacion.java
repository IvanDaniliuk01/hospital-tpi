package hospital.facturacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parameter Object: agrupa los datos necesarios para facturar una atención.
 *
 * Reemplaza la lista de parámetros primitivos
 * {@code (List<Double> estudios, boolean p, boolean o, int t)} por un
 * objeto con nombre propio, inmutable y validado en el constructor.
 */
public final class SolicitudFacturacion {

    private final List<Double> importesDeEstudios;
    private final boolean tienePlanDeSalud;
    private final boolean tieneObraSocial;
    private final int cantidadDeTurnos;

    public SolicitudFacturacion(List<Double> importesDeEstudios,
                                boolean tienePlanDeSalud,
                                boolean tieneObraSocial,
                                int cantidadDeTurnos) {
        this.importesDeEstudios = copiaValidada(importesDeEstudios);
        this.tienePlanDeSalud = tienePlanDeSalud;
        this.tieneObraSocial = tieneObraSocial;
        this.cantidadDeTurnos = cantidadDeTurnos;
    }

    private static List<Double> copiaValidada(List<Double> importes) {
        if (importes == null) {
            throw new IllegalArgumentException(
                "La lista de importes de estudios no puede ser null.");
        }
        for (int i = 0; i < importes.size(); i++) {
            if (importes.get(i) == null) {
                throw new IllegalArgumentException(
                    "El importe del estudio en la posición " + i + " es null.");
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(importes));
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

    @Override
    public String toString() {
        return "SolicitudFacturacion{estudios=" + importesDeEstudios
             + ", planDeSalud=" + tienePlanDeSalud
             + ", obraSocial=" + tieneObraSocial
             + ", turnos=" + cantidadDeTurnos + "}";
    }
}
