package hospital.turnos;

/**
 * Categoría comercial de un paciente según su obra social.
 *
 * Reemplaza la cadena de if/else-if sobre strings mágicos ("OSDE", "SWISS",
 * "PUBLICA") que existía en el código original. Agregar una nueva obra social
 * ahora implica tocar un solo lugar (el método {@link #desde(String)}).
 */
public enum CategoriaPaciente {

    PREMIUM("Paciente premium"),
    PUBLICO("Paciente publico"),
    SIN_CATEGORIA(null);

    private final String descripcion;

    CategoriaPaciente(String descripcion) {
        this.descripcion = descripcion;
    }

    /** Devuelve la categoría que corresponde al código de obra social. */
    public static CategoriaPaciente desde(String obraSocial) {
        switch (obraSocial) {
            case "OSDE":
            case "SWISS":
                return PREMIUM;
            case "PUBLICA":
                return PUBLICO;
            default:
                return SIN_CATEGORIA;
        }
    }

    /** Texto que se muestra al usuario; {@code null} si no hay categoría. */
    public String getDescripcion() {
        return descripcion;
    }

    public boolean tieneDescripcion() {
        return descripcion != null;
    }
}
