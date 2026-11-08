package hospital.pagos;

/** Resultado de pedir un cobro a la pasarela. El código solo existe si fue aprobado. */
public final class ResultadoPago {

    private final EstadoPago estado;
    private final String codigoAutorizacion;

    private ResultadoPago(EstadoPago estado, String codigoAutorizacion) {
        this.estado = estado;
        this.codigoAutorizacion = codigoAutorizacion;
    }

    public static ResultadoPago aprobado(String codigoAutorizacion) {
        return new ResultadoPago(EstadoPago.APROBADO, codigoAutorizacion);
    }

    public static ResultadoPago rechazado() {
        return new ResultadoPago(EstadoPago.RECHAZADO, null);
    }

    public static ResultadoPago noDisponible() {
        return new ResultadoPago(EstadoPago.NO_DISPONIBLE, null);
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public String getCodigoAutorizacion() {
        return codigoAutorizacion;
    }
}
