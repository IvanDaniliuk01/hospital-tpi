package hospital.pacientes;

/** Persona atendida por el hospital. Su identidad es el número de paciente. */
public class Paciente {

    private final int numero;
    private final String nombre;
    private final String edadDeclarada;
    private final String obraSocial;

    public Paciente(int numero, String nombre, String edadDeclarada, String obraSocial) {
        this.numero = numero;
        this.nombre = nombre;
        this.edadDeclarada = edadDeclarada;
        this.obraSocial = obraSocial;
    }

    public int getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    /** La edad se guarda tal como se ingresó y se interpreta al usarla (comportamiento heredado). */
    public int getEdad() {
        return Integer.parseInt(edadDeclarada);
    }

    public String getObraSocial() {
        return obraSocial;
    }
}
