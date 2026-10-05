package hospital.turnos;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gestor de turnos del Hospital Central.
 *
 * Mantiene la misma interfaz pública que la versión original
 * ({@link #procesar(String, int, String, boolean)} y {@link #mostrar()}) y
 * exactamente la misma salida por consola, pero delega en {@link Paciente},
 * {@link Turno} y {@link CategoriaPaciente} las responsabilidades que antes
 * estaban mezcladas en un solo método.
 */
public class TurnoManager {

    private static final String MENSAJE_TURNO_AGREGADO = "Turno agregado";

    private final List<Turno> turnos = new ArrayList<>();
    private final PrintStream salida;

    /** Constructor por defecto: escribe en la consola estándar, como el original. */
    public TurnoManager() {
        this(System.out);
    }

    /** Permite inyectar la salida (útil para pruebas automatizadas). */
    public TurnoManager(PrintStream salida) {
        this.salida = salida;
    }

    /**
     * Punto de entrada compatible con el sistema original.
     * Se conserva la firma para no romper a los llamadores existentes.
     */
    public void procesar(String nombrePaciente, int edad, String obraSocial, boolean urgente) {
        registrarTurno(new Paciente(nombrePaciente, edad, obraSocial), urgente);
    }

    /**
     * Registra un turno para el paciente. Si el paciente no es válido, no hace
     * nada (mismo comportamiento silencioso que el código original).
     */
    public void registrarTurno(Paciente paciente, boolean urgente) {
        if (!paciente.esValido()) {
            return;
        }
        informarCategoria(paciente);
        turnos.add(new Turno(paciente, urgente));
        salida.println(MENSAJE_TURNO_AGREGADO);
    }

    private void informarCategoria(Paciente paciente) {
        CategoriaPaciente categoria = paciente.getCategoria();
        if (categoria.tieneDescripcion()) {
            salida.println(categoria.getDescripcion());
        }
    }

    /** Imprime todos los turnos registrados, uno por línea. */
    public void mostrar() {
        for (Turno turno : turnos) {
            salida.println(turno.formatear());
        }
    }

    /** Vista de solo lectura de los turnos, para quien necesite consultarlos sin imprimir. */
    public List<Turno> getTurnos() {
        return Collections.unmodifiableList(turnos);
    }
}
