package hospital.pagos;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ResultadoPago (objeto de valor)")
class ResultadoPagoTest {

    @Test
    @DisplayName("un pago aprobado lleva código de autorización y no tiene motivo de rechazo")
    void aprobado_tieneCodigoYNoMotivo() {
        // Arrange & Act
        ResultadoPago resultado = ResultadoPago.aprobado("AUT-9");

        // Assert
        assertAll(
            () -> assertTrue(resultado.fueAprobado()),
            () -> assertEquals("AUT-9", resultado.getCodigoAutorizacion()),
            () -> assertNull(resultado.getMotivoRechazo()),
            () -> assertEquals("APROBADO(AUT-9)", resultado.toString())
        );
    }

    @Test
    @DisplayName("un pago rechazado lleva motivo y no tiene código")
    void rechazado_tieneMotivoYNoCodigo() {
        // Arrange & Act
        ResultadoPago resultado = ResultadoPago.rechazado("sin fondos");

        // Assert
        assertAll(
            () -> assertFalse(resultado.fueAprobado()),
            () -> assertNull(resultado.getCodigoAutorizacion()),
            () -> assertEquals("sin fondos", resultado.getMotivoRechazo()),
            () -> assertEquals("RECHAZADO(sin fondos)", resultado.toString())
        );
    }

    @ParameterizedTest(name = "código de autorización inválido: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void aprobadoSinCodigo_seRechaza(String codigoInvalido) {
        assertThrows(IllegalArgumentException.class, () -> ResultadoPago.aprobado(codigoInvalido));
    }

    @ParameterizedTest(name = "motivo inválido: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rechazadoSinMotivo_seRechaza(String motivoInvalido) {
        assertThrows(IllegalArgumentException.class, () -> ResultadoPago.rechazado(motivoInvalido));
    }
}
