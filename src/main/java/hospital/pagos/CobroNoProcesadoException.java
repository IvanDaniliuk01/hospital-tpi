package hospital.pagos;

/**
 * Excepción controlada del dominio: el cobro no se pudo completar.
 * Reemplaza a la excepción cruda de infraestructura con un mensaje claro
 * y conserva la causa original para diagnóstico.
 */
public class CobroNoProcesadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CobroNoProcesadoException(String mensaje) {
        super(mensaje);
    }

    public CobroNoProcesadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
