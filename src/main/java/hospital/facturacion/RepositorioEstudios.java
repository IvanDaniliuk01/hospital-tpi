package hospital.facturacion;

import java.util.ArrayList;
import java.util.List;

/** Estudios registrados en memoria. */
public class RepositorioEstudios {

    private final List<Estudio> estudios = new ArrayList<>();

    public void registrar(Estudio estudio) {
        estudios.add(estudio);
    }

    /** Estudios pendientes del paciente, en orden de registro. */
    public List<Estudio> pendientesDe(int numeroPaciente) {
        List<Estudio> pendientes = new ArrayList<>();
        for (Estudio estudio : estudios) {
            if (estudio.getNumeroPaciente() == numeroPaciente && estudio.estaPendiente()) {
                pendientes.add(estudio);
            }
        }
        return pendientes;
    }
}
