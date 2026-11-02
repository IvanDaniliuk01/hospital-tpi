package hospital.facturacion;

/** Factura de los estudios pendientes de un paciente. */
public class Factura {

    private final int numero;
    private final int numeroPaciente;
    private final double total;
    private EstadoFactura estado = EstadoFactura.PENDIENTE_DE_COBRO;

    public Factura(int numero, int numeroPaciente, double total) {
        this.numero = numero;
        this.numeroPaciente = numeroPaciente;
        this.total = total;
    }

    public int getNumero() {
        return numero;
    }

    public int getNumeroPaciente() {
        return numeroPaciente;
    }

    public double getTotal() {
        return total;
    }

    public EstadoFactura getEstado() {
        return estado;
    }

    public void marcarCobrada() {
        estado = EstadoFactura.COBRADA;
    }

    public void marcarRechazada() {
        estado = EstadoFactura.RECHAZADA;
    }
}
