package hospital.liquidacion;

import java.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Reporte mensual para la gerencia. Copiado de LiquidadorPrestaciones para
 * no tocar la liquidacion (pedido urgente 03/2020).
 */
public class ReporteMensualLiquidaciones {

    public String generar(List<Map<String, Object>> prestaciones, String mes) {
        if (CollectionUtils.isEmpty(prestaciones)) {
            return "";
        }
        double total = 0;
        double totalOsde = 0;
        double totalSwiss = 0;
        double totalOtras = 0;
        StringBuffer sb = new StringBuffer();
        sb.append("REPORTE " + mes + "\n");
        for (Map<String, Object> p : prestaciones) {
            String os = (String) p.get("os");
            String plan = (String) p.get("plan");
            String tipo = (String) p.get("tipo");
            double imp = (Double) p.get("importe");
            int edad = (Integer) p.get("edad");
            int ses = (Integer) p.get("sesiones");
            boolean urg = (Boolean) p.get("urgente");
            double r = 0;
            if (os.equals("OSDE") || os.equals("SWISS")) {
                if (plan.equals("210") || plan.equals("SMG02")) {
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
                if (os.equals("OSDE")) {
                    totalOsde += r;
                } else {
                    totalSwiss += r;
                }
            } else {
                r = imp;
                totalOtras += r;
            }
            total += r;
            sb.append(StringUtils.rightPad(os, 10) + ";");
            sb.append(StringUtils.rightPad(plan == null ? "" : plan, 8) + ";");
            sb.append(StringUtils.rightPad(tipo, 10) + ";");
            sb.append(StringUtils.leftPad(String.format("%.2f", imp), 12) + ";");
            sb.append(StringUtils.leftPad(String.format("%.2f", r), 12) + "\n");
        }
        sb.append("TOTAL OSDE: " + totalOsde + "\n");
        sb.append("TOTAL SWISS: " + totalSwiss + "\n");
        sb.append("TOTAL OTRAS: " + totalOtras + "\n");
        sb.append("TOTAL: " + total + "\n");
        return sb.toString();
    }
}
