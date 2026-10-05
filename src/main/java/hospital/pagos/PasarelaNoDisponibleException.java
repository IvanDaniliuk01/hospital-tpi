package hospital.pagos;

/**
 * La pasarela no pudo procesar la petición (timeout, red caída, error 5xx).
 * La lanza el adaptador concreto; el dominio la traduce a
 * {@link CobroNoProcesadoException}.
 */
public class PasarelaNoDisponibleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PasarelaNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
