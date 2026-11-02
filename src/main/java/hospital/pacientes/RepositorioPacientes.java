package hospital.pacientes;

import java.util.HashMap;
import java.util.Map;

/** Pacientes registrados en memoria, con número correlativo. */
public class RepositorioPacientes {

  private final Map<Integer, Paciente> pacientes = new HashMap<>();
  private int ultimoNumero = 0;

  public Paciente registrar(String nombre, String edadDeclarada, String obraSocialDeclarada) {
    ultimoNumero++;
    Paciente paciente = new Paciente(ultimoNumero, nombre, edadDeclarada, obraSocialDeclarada);
    pacientes.put(ultimoNumero, paciente);
    return paciente;
  }

  /** Devuelve el paciente o null si el número no existe. */
  public Paciente buscar(int numero) {
    return pacientes.get(numero);
  }
}
