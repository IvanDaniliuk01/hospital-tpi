package hospital.facturacion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hospital.legacy.Facturacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Red de regresión del refactoring de TP3, migrada a JUnit 5.
 *
 * Son las mismas 66 combinaciones de la clase {@code PruebasCalculadoraFacturacion}
 * original (4 listas × 2 × 2 × 4 turnos + 2 casos límite), ahora como prueba
 * parametrizada: cada combinación es un caso independiente en el reporte de
 * Surefire, con su propio nombre, en lugar de un contador manual.
 *
 * Contrato: para cualquier entrada, el código refactorizado devuelve
 * exactamente el mismo valor que el original ({@code Double.compare == 0}).
 */
@DisplayName("Equivalencia funcional: Facturacion.calc (original) vs CalculadoraFacturacion (refactor)")
class EquivalenciaOriginalVsRefactorTest {

    static Stream<Arguments> combinaciones() {
        List<List<Double>> listas = Arrays.asList(
                Arrays.asList(1000.0),
                Arrays.asList(1000.0, 500.0, 250.0),
                Collections.emptyList(),
                Arrays.asList(333.33, 199.99, 0.01));
        List<Arguments> casos = new ArrayList<>();
        for (List<Double> estudios : listas) {
            for (boolean plan : new boolean[] {false, true}) {
                for (boolean obraSocial : new boolean[] {false, true}) {
                    for (int turnos : new int[] {0, 3, 4, 10}) {
                        casos.add(Arguments.of(estudios, plan, obraSocial, turnos));
                    }
                }
            }
        }
        // Casos límite conservados a propósito (el original los permite)
        casos.add(Arguments.of(Arrays.asList(50.0), false, false, 4));    // total negativo
        casos.add(Arguments.of(Arrays.asList(-100.0), true, true, 1));    // importe negativo
        return casos.stream();
    }

    @ParameterizedTest(name = "[{index}] estudios={0} plan={1} obraSocial={2} turnos={3}")
    @MethodSource("combinaciones")
    void elRefactorDevuelveExactamenteLoMismoQueElOriginal(List<Double> estudios, boolean plan,
                                                            boolean obraSocial, int turnos) {
        // Arrange
        Facturacion original = new Facturacion();
        CalculadoraFacturacion refactor = new CalculadoraFacturacion();
        SolicitudFacturacion solicitud = new SolicitudFacturacion(estudios, plan, obraSocial, turnos);

        // Act
        double esperado = original.calc(estudios, plan, obraSocial, turnos);
        double obtenido = refactor.calcularTotal(solicitud);

        // Assert (igualdad exacta: es el mismo algoritmo, no debe haber ni un ulp de diferencia)
        assertEquals(0, Double.compare(esperado, obtenido),
                () -> "original=" + esperado + " refactor=" + obtenido);
    }
}
