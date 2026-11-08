package hospital.pagos;

import hospital.facturacion.Factura;

/** Cobra facturas a través de la pasarela y lleva el total cobrado en la sesión. */
public class ServicioDeCobro {

    private final PasarelaDePagos pasarela;
    private double totalCobrado = 0;

    public ServicioDeCobro(PasarelaDePagos pasarela) {
        this.pasarela = pasarela;
    }

    public ResultadoPago cobrar(Factura factura) {
        ResultadoPago resultado =
                pasarela.cobrar(String.valueOf(factura.getNumero()), factura.getTotal());
        if (resultado.getEstado() == EstadoPago.APROBADO) {
            factura.marcarCobrada();
            totalCobrado = totalCobrado + factura.getTotal();
        } else if (resultado.getEstado() == EstadoPago.RECHAZADO) {
            factura.marcarRechazada();
        }
        return resultado;
    }

    public double getTotalCobrado() {
        return totalCobrado;
    }
}
