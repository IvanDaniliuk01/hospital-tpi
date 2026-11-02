package hospital.pacientes;

/** Persona atendida por el hospital. Su identidad es el número de paciente. */
public class Paciente {

  private final int numero;
  private final String nombre;
  private final String edadDeclarada;
  private final String obraSocialDeclarada;
  private final Cobertura cobertura;

  public Paciente(int numero, String nombre, String edadDeclarada, String obraSocialDeclarada) {
    this.numero = numero;
    this.nombre = nombre;
    this.edadDeclarada = edadDeclarada;
    this.obraSocialDeclarada = obraSocialDeclarada;
    this.cobertura = Cobertura.desde(obraSocialDeclarada);
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

  /** Texto tal como se ingresó, usado para mostrar el turno. */
  public String getObraSocialDeclarada() {
    return obraSocialDeclarada;
  }

  public Cobertura getCobertura() {
    return cobertura;
  }
}
