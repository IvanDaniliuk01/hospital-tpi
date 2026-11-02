package hospital.pagos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PasarelaSimuladaTest {

    private final PasarelaSimulada pasarela = new PasarelaSimulada();

    @Test
    @DisplayName("Aprueba hasta 50.000 inclusive con código AUT-referencia")
    void apruebaHastaElMaximo() {
        ResultadoPago resultado = pasarela.cobrar("3", 50000.0);

        assertEquals(EstadoPago.APROBADO, resultado.getEstado());
        assertEquals("AUT-3", resultado.getCodigoAutorizacion());
    }

    @Test
    @DisplayName("Rechaza importes mayores a 50.000")
    void rechazaPorEncimaDelMaximo() {
        ResultadoPago resultado = pasarela.cobrar("4", 50000.01);

        assertEquals(EstadoPago.RECHAZADO, resultado.getEstado());
        assertNull(resultado.getCodigoAutorizacion());
    }

    @Test
    @DisplayName("Simula una caída cuando el importe termina en ,99")
    void simulaCaidaConNoventaYNueve() {
        assertEquals(EstadoPago.NO_DISPONIBLE, pasarela.cobrar("5", 500.99).getEstado());
        assertEquals(EstadoPago.NO_DISPONIBLE, pasarela.cobrar("6", 60000.99).getEstado());
    }
}
