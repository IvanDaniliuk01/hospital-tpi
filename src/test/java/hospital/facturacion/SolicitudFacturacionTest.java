package hospital.facturacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hospital.pacientes.Cobertura;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SolicitudFacturacionTest {

    @Test
    @DisplayName("Rechaza una lista de importes null")
    void rechazaListaNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SolicitudFacturacion(null, Cobertura.PREPAGA, 0));
    }

    @Test
    @DisplayName("Guarda una copia: cambiar la lista original no la afecta")
    void guardaCopiaDefensiva() {
        List<Double> importes = new ArrayList<>(List.of(100.0));
        SolicitudFacturacion solicitud =
                new SolicitudFacturacion(importes, Cobertura.SIN_COBERTURA, 0);

        importes.add(200.0);

        assertEquals(1, solicitud.getImportesDeEstudios().size());
    }

    @Test
    @DisplayName("La lista que expone no se puede modificar")
    void listaInmodificable() {
        SolicitudFacturacion solicitud =
                new SolicitudFacturacion(List.of(100.0), Cobertura.SIN_COBERTURA, 0);

        assertThrows(
                UnsupportedOperationException.class,
                () -> solicitud.getImportesDeEstudios().add(1.0));
    }
}
