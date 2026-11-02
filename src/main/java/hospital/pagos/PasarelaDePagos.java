package hospital.pagos;

/** Puerto hacia el sistema externo que autoriza los cobros (DIP). */
public interface PasarelaDePagos {

  ResultadoPago cobrar(String referencia, double importe);
}
