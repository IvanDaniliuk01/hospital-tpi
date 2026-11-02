package hospital.facturacion;

import static hospital.facturacion.ReglasFacturacion.BONIFICACION_POR_FRECUENCIA;
import static hospital.facturacion.ReglasFacturacion.FACTOR_DESCUENTO_OBRA_SOCIAL;
import static hospital.facturacion.ReglasFacturacion.PROPORCION_A_CARGO_CON_PREPAGA;
import static hospital.facturacion.ReglasFacturacion.TURNOS_MINIMOS_PARA_BONIFICACION;

import hospital.pacientes.Cobertura;

/** Reglas de descuento de la factura, movidas desde el cálculo (Move Method). */
public class PoliticaDescuentos {

    public double importeACargoDelPaciente(double importe, Cobertura cobertura) {
        if (cobertura == Cobertura.PREPAGA) {
            return importe * PROPORCION_A_CARGO_CON_PREPAGA;
        }
        return importe;
    }

    public double aplicarDescuentoObraSocial(double total, Cobertura cobertura) {
        if (cobertura == Cobertura.OBRA_SOCIAL) {
            return total * FACTOR_DESCUENTO_OBRA_SOCIAL;
        }
        return total;
    }

    public double aplicarBonificacionPorFrecuencia(double total, int cantidadDeTurnos) {
        if (cantidadDeTurnos > TURNOS_MINIMOS_PARA_BONIFICACION) {
            return total - BONIFICACION_POR_FRECUENCIA;
        }
        return total;
    }
}
