package hospital.turnos;

import java.util.HashMap;
import java.util.Map;

/**
 * Cantidad de turnos dados por nombre de paciente, usada para la bonificación por frecuencia.
 * Conserva el comportamiento heredado: cuenta por nombre y cuenta aunque el turno no se haya agregado.
 */
public class ContadorTurnos {

    private final Map<String, Integer> turnosPorNombre = new HashMap<>();
    private int urgentes = 0;

    public void registrar(String nombrePaciente, boolean urgente) {
        turnosPorNombre.merge(nombrePaciente, 1, Integer::sum);
        if (urgente) {
            urgentes++;
        }
    }

    public int turnosDe(String nombrePaciente) {
        return turnosPorNombre.getOrDefault(nombrePaciente, 0);
    }

    public int total() {
        int total = 0;
        for (int cantidad : turnosPorNombre.values()) {
            total += cantidad;
        }
        return total;
    }

    public int urgentes() {
        return urgentes;
    }
}
