package hospital.liquidacion;

import java.util.*;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

/**
 * Liquida las prestaciones del mes a las obras sociales.
 * Version 2.3 - 2019 - NO TOCAR sin avisar a facturacion
 */
public class LiquidadorPrestaciones {

    static Logger log = Logger.getLogger(LiquidadorPrestaciones.class);

    public List<String> errores = new ArrayList<String>();
    public double totalMes = 0;
    private int contador;

    public double liquidar(String os, String plan, String tipo, double imp, int edad,
                           boolean urg, boolean noct, boolean fer, String prov, int ses) {
        double r = 0;
        contador++;
        if (os != null && !os.equals("")) {
            if (tipo != null) {
                if (os.equals("OSDE")) {
                    if (plan.equals("210")) {
                        if (tipo.equals("CONSULTA")) {
                            r = imp * 0.2;
                        } else if (tipo.equals("IMAGEN")) {
                            r = imp * 0.3;
                            if (ses > 4) {
                                r = r - (ses - 4) * imp * 0.05;
                            }
                        } else {
                            r = imp * 0.4;
                        }
                    } else if (plan.equals("310") || plan.equals("410")) {
                        if (tipo.equals("CONSULTA")) {
                            r = 0;
                        } else {
                            r = imp * 0.1;
                        }
                    } else {
                        r = imp * 0.5;
                    }
                    if (edad > 65) {
                        r = r * 0.8;
                    }
                    if (urg) {
                        r = r + 1500;
                    }
                } else if (os.equals("SWISS")) {
                    if (plan.equals("SMG02")) {
                        if (tipo.equals("CONSULTA")) {
                            r = imp * 0.2;
                        } else if (tipo.equals("IMAGEN")) {
                            r = imp * 0.3;
                            if (ses > 4) {
                                r = r - (ses - 4) * imp * 0.05;
                            }
                        } else {
                            r = imp * 0.4;
                        }
                    } else {
                        r = imp * 0.5;
                    }
                    if (edad > 65) {
                        r = r * 0.8;
                    }
                    if (urg) {
                        r = r + 1500;
                    }
                } else if (os == "PAMI") {
                    r = 0;
                    if (tipo.equals("IMAGEN") && ses > 10) {
                        errores.add("PAMI: supera sesiones autorizadas " + ses);
                    }
                } else if (os.equals("IOMA")) {
                    if (prov != null && prov.equals("BA")) {
                        r = imp * 0.25;
                    } else {
                        r = imp * 0.35;
                        if (noct || fer) {
                            r = r * 1.2;
                        }
                    }
                } else {
                    r = imp;
                    if (noct) {
                        r = r * 1.3;
                    }
                    if (fer) {
                        r = r * 1.3;
                    }
                    if (urg) {
                        r = r + 2500;
                    }
                }
            } else {
                errores.add("tipo nulo");
            }
        } else {
            System.out.println("obra social vacia, se cobra particular");
            r = imp;
        }
        if (r < 0) r = 0;
        totalMes += r;
        log.info("liquidado " + os + " " + plan + " " + tipo + " = " + r);
        return r;
    }

    public String resumen() {
        String s = "";
        s = s + "Prestaciones: " + contador + "\n";
        s = s + "Total: " + totalMes + "\n";
        if (errores.size() > 0) {
            s = s + "Errores: " + StringUtils.join(errores, ", ") + "\n";
        }
        return s;
    }
}
