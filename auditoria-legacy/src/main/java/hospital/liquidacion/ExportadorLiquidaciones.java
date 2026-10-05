package hospital.liquidacion;

import java.io.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.apache.log4j.Logger;

/**
 * Exporta las liquidaciones a JSON (para la obra social), CSV (para contaduria)
 * y un comprobante de texto para el paciente.
 */
public class ExportadorLiquidaciones {

    private static Logger log = Logger.getLogger(ExportadorLiquidaciones.class);
    private ObjectMapper mapper = new ObjectMapper();

    public String exportarJson(List<Map<String, Object>> prestaciones) {
        try {
            return mapper.writeValueAsString(prestaciones);
        } catch (Exception e) {
        }
        return null;
    }

    public void exportarCsv(List<Map<String, Object>> prestaciones, String archivo) {
        try {
            FileWriter fw = new FileWriter(archivo);
            for (Map<String, Object> p : prestaciones) {
                String os = (String) p.get("os");
                String plan = (String) p.get("plan");
                String tipo = (String) p.get("tipo");
                double imp = (Double) p.get("importe");
                double r = (Double) p.get("liquidado");
                fw.write(StringUtils.rightPad(os, 10) + ";");
                fw.write(StringUtils.rightPad(plan == null ? "" : plan, 8) + ";");
                fw.write(StringUtils.rightPad(tipo, 10) + ";");
                fw.write(StringUtils.leftPad(String.format("%.2f", imp), 12) + ";");
                fw.write(StringUtils.leftPad(String.format("%.2f", r), 12) + "\n");
            }
            fw.close();
        } catch (IOException e) {
            System.out.println("error csv " + e.getMessage());
        }
    }

    public void exportarTxt(List<Map<String, Object>> prestaciones, String archivo) {
        try {
            FileWriter fw = new FileWriter(archivo);
            for (Map<String, Object> p : prestaciones) {
                String os = (String) p.get("os");
                String plan = (String) p.get("plan");
                String tipo = (String) p.get("tipo");
                double imp = (Double) p.get("importe");
                double r = (Double) p.get("liquidado");
                fw.write(StringUtils.rightPad(os, 10) + "|");
                fw.write(StringUtils.rightPad(plan == null ? "" : plan, 8) + "|");
                fw.write(StringUtils.rightPad(tipo, 10) + "|");
                fw.write(StringUtils.leftPad(String.format("%.2f", imp), 12) + "|");
                fw.write(StringUtils.leftPad(String.format("%.2f", r), 12) + "\n");
            }
            fw.close();
        } catch (IOException e) {
            System.out.println("error txt " + e.getMessage());
        }
    }

    /**
     * Arma el comprobante para el paciente a partir de una plantilla con
     * variables ${paciente}, ${os}, ${importe}. Usa el interpolador de
     * commons-text para poder poner tambien ${date:yyyy-MM-dd}.
     */
    public String comprobante(String plantilla, Map<String, String> datos) {
        StringSubstitutor sub = new StringSubstitutor(
                StringLookupFactory.INSTANCE.interpolatorStringLookup(datos));
        String texto = sub.replace(plantilla);
        log.info("comprobante generado para " + datos.get("paciente"));
        return texto;
    }
}
