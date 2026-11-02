package hospital;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// Consola del sistema de turnos y facturacion del hospital.
// Version 1.0 - todo junto para salir rapido.
public class ConsolaHospital {

    // pacientes: nro -> {nombre, edad, obraSocial}
    static Map<Integer, String[]> pacientes = new HashMap<>();
    // turnos por nombre de paciente (para la bonificacion)
    static Map<String, Integer> turnos = new HashMap<>();
    static int urgentes = 0;
    // estudios: {nroPaciente, importe, facturado(0/1)}
    static List<double[]> estudios = new ArrayList<>();
    // facturas: {nroPaciente, total, estado(0=pendiente,1=cobrada,2=rechazada)}
    static List<double[]> facturas = new ArrayList<>();
    static double cobrado = 0;
    static int nro = 0;

    static TurnoManager tm = new TurnoManager();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean seguir = true;
        while (seguir && sc.hasNextLine()) {
            System.out.println("=== HOSPITAL ===");
            System.out.println("1) Registrar paciente");
            System.out.println("2) Dar turno");
            System.out.println("3) Listar turnos");
            System.out.println("4) Registrar estudio");
            System.out.println("5) Facturar");
            System.out.println("6) Cobrar factura");
            System.out.println("7) Reporte");
            System.out.println("0) Salir");
            String op = sc.nextLine().trim();
            try {
                switch (op) {
                    case "1": {
                        System.out.println("Nombre:");
                        String n = sc.nextLine().trim();
                        System.out.println("Edad:");
                        String e = sc.nextLine().trim();
                        System.out.println("Obra social:");
                        String os = sc.nextLine().trim();
                        nro++;
                        pacientes.put(nro, new String[] {n, e, os});
                        if (os.equals("OSDE") || os.equals("SWISS")) {
                            System.out.println("Paciente " + nro + " registrado (prepaga)");
                        } else if (os.equals("PUBLICA")) {
                            System.out.println("Paciente " + nro + " registrado (obra social)");
                        } else {
                            System.out.println("Paciente " + nro + " registrado");
                        }
                        break;
                    }
                    case "2": {
                        System.out.println("Nro de paciente:");
                        int np = Integer.parseInt(sc.nextLine().trim());
                        String[] p = pacientes.get(np);
                        if (p == null) {
                            System.out.println("No existe el paciente");
                            break;
                        }
                        System.out.println("Urgente (s/n):");
                        String u = sc.nextLine().trim();
                        boolean urg = u.equals("s");
                        tm.procesar(p[0], Integer.parseInt(p[1]), p[2], urg);
                        if (turnos.containsKey(p[0])) {
                            turnos.put(p[0], turnos.get(p[0]) + 1);
                        } else {
                            turnos.put(p[0], 1);
                        }
                        if (urg) {
                            urgentes++;
                        }
                        break;
                    }
                    case "3": {
                        tm.mostrar();
                        break;
                    }
                    case "4": {
                        System.out.println("Nro de paciente:");
                        int np = Integer.parseInt(sc.nextLine().trim());
                        String[] p = pacientes.get(np);
                        if (p == null) {
                            System.out.println("No existe el paciente");
                            break;
                        }
                        System.out.println("Importe:");
                        double imp = Double.parseDouble(sc.nextLine().trim());
                        estudios.add(new double[] {np, imp, 0});
                        System.out.println("Estudio registrado para " + p[0]);
                        break;
                    }
                    case "5": {
                        System.out.println("Nro de paciente:");
                        int np = Integer.parseInt(sc.nextLine().trim());
                        String[] p = pacientes.get(np);
                        if (p == null) {
                            System.out.println("No existe el paciente");
                            break;
                        }
                        List<Double> imps = new ArrayList<>();
                        for (int i = 0; i < estudios.size(); i++) {
                            double[] es = estudios.get(i);
                            if (es[0] == np && es[2] == 0) {
                                imps.add(es[1]);
                                es[2] = 1;
                            }
                        }
                        boolean plan = false;
                        boolean obra = false;
                        if (p[2].equals("OSDE") || p[2].equals("SWISS")) {
                            plan = true;
                        }
                        if (p[2].equals("PUBLICA")) {
                            obra = true;
                        }
                        int t = 0;
                        if (turnos.get(p[0]) != null) {
                            t = turnos.get(p[0]);
                        }
                        Facturacion f = new Facturacion();
                        double total = f.calc(imps, plan, obra, t);
                        facturas.add(new double[] {np, total, 0});
                        System.out.println("Factura " + facturas.size() + " para " + p[0]
                                + ": " + imps.size() + " estudios, total $" + total);
                        break;
                    }
                    case "6": {
                        System.out.println("Nro de factura:");
                        int nf = Integer.parseInt(sc.nextLine().trim());
                        double[] fa = facturas.get(nf - 1);
                        // simulacion de la pasarela de pagos
                        long centavos = Math.round(fa[1] * 100);
                        if (centavos % 100 == 99) {
                            System.out.println("Pasarela no disponible, reintente");
                        } else if (fa[1] > 50000) {
                            fa[2] = 2;
                            System.out.println("Cobro RECHAZADO factura " + nf);
                        } else {
                            fa[2] = 1;
                            cobrado = cobrado + fa[1];
                            System.out.println("Cobro APROBADO factura " + nf + " cod AUT-" + nf);
                        }
                        break;
                    }
                    case "7": {
                        int tt = 0;
                        for (Integer x : turnos.values()) {
                            tt = tt + x;
                        }
                        double fact = 0;
                        int rech = 0;
                        int pend = 0;
                        for (double[] fa : facturas) {
                            fact = fact + fa[1];
                            if (fa[2] == 2) {
                                rech++;
                            }
                            if (fa[2] == 0) {
                                pend++;
                            }
                        }
                        System.out.println("--- REPORTE ---");
                        System.out.println("Turnos: " + tt + " (urgentes: " + urgentes + ")");
                        System.out.println("Facturado: $" + fact);
                        System.out.println("Cobrado: $" + cobrado);
                        System.out.println("Rechazadas: " + rech + " / Pendientes: " + pend);
                        break;
                    }
                    case "0": {
                        seguir = false;
                        System.out.println("Fin");
                        break;
                    }
                    default:
                        System.out.println("Opcion invalida");
                }
            } catch (Exception ex) {
                System.out.println("Error");
            }
        }
    }
}
