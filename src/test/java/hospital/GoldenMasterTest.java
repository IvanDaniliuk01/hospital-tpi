package hospital;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hospital.pagos.PasarelaSimulada;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Golden master: la consola completa frente al guion debe producir la salida esperada. */
class GoldenMasterTest {

    private static final Path GUION = Path.of("pruebas", "golden", "guion.txt");
    private static final Path ESPERADA = Path.of("pruebas", "golden", "salida_esperada.txt");

    @Test
    @DisplayName("El guion de las 7 operaciones produce exactamente la salida esperada")
    void guionProduceLaSalidaEsperada() throws IOException {
        String guion = Files.readString(GUION, StandardCharsets.UTF_8);
        String esperada = Files.readString(ESPERADA, StandardCharsets.UTF_8);

        String obtenida = ejecutarConsola(guion);

        assertEquals(esperada, obtenida);
    }

    private static String ejecutarConsola(String guion) {
        PrintStream salidaOriginal = System.out;
        ByteArrayOutputStream capturada = new ByteArrayOutputStream();
        try (PrintStream captura = new PrintStream(capturada, true, StandardCharsets.UTF_8)) {
            System.setOut(captura);
            new ConsolaHospital(new PasarelaSimulada()).ejecutar(new Scanner(guion));
        } finally {
            System.setOut(salidaOriginal);
        }
        return capturada.toString(StandardCharsets.UTF_8).replace("\r", "");
    }
}
