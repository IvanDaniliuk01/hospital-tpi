package hospital.facturacion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hospital.pacientes.Cobertura;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalculadoraFacturacionTest {

    private static final double DELTA = 1e-9;

    private final CalculadoraFacturacion calculadora =
            new CalculadoraFacturacion(new PoliticaDescuentos());

    private double total(List<Double> importes, Cobertura cobertura, int turnos) {
        return calculadora.calcularTotal(new SolicitudFacturacion(importes, cobertura, turnos));
    }

    @Test
    @DisplayName("Sin cobertura se suman los importes completos")
    void sinCoberturaSumaImportes() {
        assertEquals(3000.0, total(List.of(1000.0, 2000.0), Cobertura.SIN_COBERTURA, 0), DELTA);
    }

    @Test
    @DisplayName("Con prepaga el paciente paga el 50 % de cada estudio")
    void prepagaPagaLaMitad() {
        assertEquals(1500.0, total(List.of(1000.0, 2000.0), Cobertura.PREPAGA, 0), DELTA);
    }

    @Test
    @DisplayName("Con obra social se descuenta el 10 % del total")
    void obraSocialDescuentaDiezPorCiento() {
        assertEquals(9000.0, total(List.of(10000.0), Cobertura.OBRA_SOCIAL, 1), DELTA);
    }

    @Test
    @DisplayName("Con 3 turnos no hay bonificación")
    void tresTurnosSinBonificacion() {
        assertEquals(1000.0, total(List.of(1000.0), Cobertura.SIN_COBERTURA, 3), DELTA);
    }

    @Test
    @DisplayName("Con 4 turnos se resta la bonificación por frecuencia de 100")
    void cuatroTurnosConBonificacion() {
        assertEquals(900.0, total(List.of(1000.0), Cobertura.SIN_COBERTURA, 4), DELTA);
    }

    @Test
    @DisplayName("Prepaga y bonificación se combinan: 50 % y después menos 100")
    void prepagaYBonificacion() {
        assertEquals(1400.0, total(List.of(1000.0, 2000.0), Cobertura.PREPAGA, 4), DELTA);
    }

    @Test
    @DisplayName("Caracterización D4: sin estudios y con 4 turnos el total da -100")
    void sinEstudiosTotalNegativo() {
        assertEquals(-100.0, total(List.of(), Cobertura.OBRA_SOCIAL, 4), DELTA);
    }
}
