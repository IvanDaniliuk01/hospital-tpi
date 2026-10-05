package hospital.turnos;

/**
 * Datos de un paciente. Agrupa los tres parámetros sueltos que antes viajaban juntos por el método
 * {@code procesar} (Data Clump) y concentra las reglas de validación en un único lugar.
 */
public class Paciente {

  private final String nombre;
  private final int edad;
  private final String obraSocial;

  public Paciente(String nombre, int edad, String obraSocial) {
    this.nombre = nombre;
    this.edad = edad;
    this.obraSocial = obraSocial;
  }

  public String getNombre() {
    return nombre;
  }

  public int getEdad() {
    return edad;
  }

  public String getObraSocial() {
    return obraSocial;
  }

  /** Un paciente es válido si tiene nombre no vacío y edad positiva. */
  public boolean esValido() {
    return tieneNombre() && tieneEdadValida();
  }

  private boolean tieneNombre() {
    return nombre != null && !nombre.isEmpty();
  }

  private boolean tieneEdadValida() {
    return edad > 0;
  }

  public CategoriaPaciente getCategoria() {
    return CategoriaPaciente.desde(obraSocial);
  }
}
