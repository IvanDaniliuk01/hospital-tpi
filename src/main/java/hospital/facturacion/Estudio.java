package hospital.facturacion;

/** Prestación realizada a un paciente, con su importe. Queda pendiente hasta que se factura. */
public class Estudio {

    private final int numeroPaciente;
    private final double importe;
    private boolean facturado = false;

    public Estudio(int numeroPaciente, double importe) {
        this.numeroPaciente = numeroPaciente;
        this.importe = importe;
    }

    public int getNumeroPaciente() {
        return numeroPaciente;
    }

    public double getImporte() {
        return importe;
    }

    public boolean estaPendiente() {
        return !facturado;
    }

    public void marcarFacturado() {
        facturado = true;
    }
}
