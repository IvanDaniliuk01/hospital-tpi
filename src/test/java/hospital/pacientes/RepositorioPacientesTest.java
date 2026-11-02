package hospital.pacientes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RepositorioPacientesTest {

    private final RepositorioPacientes repositorio = new RepositorioPacientes();

    @Test
    @DisplayName("Asigna números correlativos desde 1")
    void asignaNumerosCorrelativos() {
        Paciente primero = repositorio.registrar("Ana", "34", "OSDE");
        Paciente segundo = repositorio.registrar("Beto", "51", "PUBLICA");

        assertEquals(1, primero.getNumero());
        assertEquals(2, segundo.getNumero());
    }

    @Test
    @DisplayName("Busca por número y devuelve null si no existe")
    void buscaPorNumero() {
        Paciente ana = repositorio.registrar("Ana", "34", "OSDE");

        assertSame(ana, repositorio.buscar(1));
        assertNull(repositorio.buscar(9));
    }

    @Test
    @DisplayName("La cobertura se deriva de la obra social declarada al registrar")
    void derivaLaCobertura() {
        Paciente ana = repositorio.registrar("Ana", "34", "SWISS");

        assertEquals(Cobertura.PREPAGA, ana.getCobertura());
        assertEquals("SWISS", ana.getObraSocialDeclarada());
    }
}
