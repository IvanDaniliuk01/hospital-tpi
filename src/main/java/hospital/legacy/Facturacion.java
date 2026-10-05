package hospital.legacy;

import java.util.List;

/**
 * Código original recibido (sin modificaciones).
 * Se conserva únicamente como referencia para las pruebas de
 * equivalencia funcional del refactoring.
 */
public class Facturacion {

    public double calc(List<Double> estudios,
                       boolean p,
                       boolean o,
                       int t) {

        double r = 0;

        for (Double e : estudios) {

            if (p == true) {
                r += e * 0.5;
            } else {
                r += e;
            }

        }

        if (o == true) {
            r = r * 0.9;
        }

        if (t > 3) {
            r = r - 100;
        }

        return r;

    }

}
