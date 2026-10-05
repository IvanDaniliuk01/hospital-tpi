package hospital.pagos;

/**
 * Puerto hacia la API externa de pagos.
 *
 * Es la única abstracción que el dominio conoce del proveedor de pagos.
 * La implementación real (HTTP, SDK del proveedor) vive fuera del núcleo;
 * en las pruebas se reemplaza por un doble de prueba (Stub o Mock).
 * Depender de esta interfaz y no de un cliente concreto es lo que hace
 * posible el aislamiento del componente {@link ServicioDeCobro}.
 */
public interface PasarelaDePagos {

    /**
     * Solicita al proveedor el cobro de un importe.
     *
     * @param idPaciente identificador del paciente que paga
     * @param importe    monto a cobrar, siempre mayor que cero
     * @param referencia referencia única de la operación (idempotencia)
     * @return el resultado informado por el proveedor
     * @throws PasarelaNoDisponibleException si el proveedor no responde
     */
    ResultadoPago cobrar(String idPaciente, double importe, String referencia);
}
