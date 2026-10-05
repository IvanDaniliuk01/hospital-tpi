package hospital.pagos;

/**
 * Respuesta de la pasarela ante un intento de cobro. Objeto de valor inmutable.
 */
public final class ResultadoPago {

    private final boolean aprobado;
    private final String codigoAutorizacion;
    private final String motivoRechazo;

    private ResultadoPago(boolean aprobado, String codigoAutorizacion, String motivoRechazo) {
        this.aprobado = aprobado;
        this.codigoAutorizacion = codigoAutorizacion;
        this.motivoRechazo = motivoRechazo;
    }

    public static ResultadoPago aprobado(String codigoAutorizacion) {
        if (codigoAutorizacion == null || codigoAutorizacion.isBlank()) {
            throw new IllegalArgumentException("Un pago aprobado requiere código de autorización.");
        }
        return new ResultadoPago(true, codigoAutorizacion, null);
    }

    public static ResultadoPago rechazado(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Un pago rechazado requiere motivo.");
        }
        return new ResultadoPago(false, null, motivo);
    }

    public boolean fueAprobado() {
        return aprobado;
    }

    public String getCodigoAutorizacion() {
        return codigoAutorizacion;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    @Override
    public String toString() {
        return aprobado ? "APROBADO(" + codigoAutorizacion + ")"
                        : "RECHAZADO(" + motivoRechazo + ")";
    }
}
