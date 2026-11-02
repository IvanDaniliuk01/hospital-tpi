package hospital;

import hospital.facturacion.CalculadoraFacturacion;
import hospital.facturacion.EstadoFactura;
import hospital.facturacion.Estudio;
import hospital.facturacion.Factura;
import hospital.facturacion.RepositorioEstudios;
import hospital.facturacion.RepositorioFacturas;
import hospital.facturacion.SolicitudFacturacion;
import hospital.pacientes.Paciente;
import hospital.pacientes.RepositorioPacientes;
import hospital.turnos.ContadorTurnos;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Consola del sistema de turnos y facturación del hospital. */
public class ConsolaHospital {

    private final RepositorioPacientes pacientes = new RepositorioPacientes();
    private final ContadorTurnos contadorTurnos = new ContadorTurnos();
    private final RepositorioEstudios estudios = new RepositorioEstudios();
    private final RepositorioFacturas facturas = new RepositorioFacturas();
    private final TurnoManager tm = new TurnoManager();
    private final CalculadoraFacturacion calculadora = new CalculadoraFacturacion();
    private double cobrado = 0;

    public static void main(String[] args) {
        new ConsolaHospital().ejecutar(new Scanner(System.in));
    }

    public void ejecutar(Scanner sc) {
        boolean seguir = true;
        while (seguir && sc.hasNextLine()) {
            mostrarMenu();
            String op = sc.nextLine().trim();
            try {
                switch (op) {
                    case "1":
                        registrarPaciente(sc);
                        break;
                    case "2":
                        darTurno(sc);
                        break;
                    case "3":
                        listarTurnos();
                        break;
                    case "4":
                        registrarEstudio(sc);
                        break;
                    case "5":
                        facturar(sc);
                        break;
                    case "6":
                        cobrarFactura(sc);
                        break;
                    case "7":
                        emitirReporte();
                        break;
                    case "0":
                        seguir = false;
                        System.out.println("Fin");
                        break;
                    default:
                        System.out.println("Opcion invalida");
                }
            } catch (Exception ex) {
                System.out.println("Error");
            }
        }
    }

    private void mostrarMenu() {
        System.out.println("=== HOSPITAL ===");
        System.out.println("1) Registrar paciente");
        System.out.println("2) Dar turno");
        System.out.println("3) Listar turnos");
        System.out.println("4) Registrar estudio");
        System.out.println("5) Facturar");
        System.out.println("6) Cobrar factura");
        System.out.println("7) Reporte");
        System.out.println("0) Salir");
    }

    /** Pide el número de paciente. Devuelve null (y avisa) si no existe. */
    private Paciente pedirPacienteExistente(Scanner sc) {
        System.out.println("Nro de paciente:");
        int numero = Integer.parseInt(sc.nextLine().trim());
        Paciente paciente = pacientes.buscar(numero);
        if (paciente == null) {
            System.out.println("No existe el paciente");
        }
        return paciente;
    }

    private void registrarPaciente(Scanner sc) {
        System.out.println("Nombre:");
        String nombre = sc.nextLine().trim();
        System.out.println("Edad:");
        String edad = sc.nextLine().trim();
        System.out.println("Obra social:");
        String os = sc.nextLine().trim();
        Paciente paciente = pacientes.registrar(nombre, edad, os);
        int nro = paciente.getNumero();
        if (os.equals("OSDE") || os.equals("SWISS")) {
            System.out.println("Paciente " + nro + " registrado (prepaga)");
        } else if (os.equals("PUBLICA")) {
            System.out.println("Paciente " + nro + " registrado (obra social)");
        } else {
            System.out.println("Paciente " + nro + " registrado");
        }
    }

    private void darTurno(Scanner sc) {
        Paciente paciente = pedirPacienteExistente(sc);
        if (paciente == null) {
            return;
        }
        System.out.println("Urgente (s/n):");
        boolean urgente = sc.nextLine().trim().equals("s");
        tm.procesar(paciente.getNombre(), paciente.getEdad(), paciente.getObraSocial(), urgente);
        contadorTurnos.registrar(paciente.getNombre(), urgente);
    }

    private void listarTurnos() {
        tm.mostrar();
    }

    private void registrarEstudio(Scanner sc) {
        Paciente paciente = pedirPacienteExistente(sc);
        if (paciente == null) {
            return;
        }
        System.out.println("Importe:");
        double importe = Double.parseDouble(sc.nextLine().trim());
        estudios.registrar(new Estudio(paciente.getNumero(), importe));
        System.out.println("Estudio registrado para " + paciente.getNombre());
    }

    private void facturar(Scanner sc) {
        Paciente paciente = pedirPacienteExistente(sc);
        if (paciente == null) {
            return;
        }
        List<Estudio> pendientes = estudios.pendientesDe(paciente.getNumero());
        List<Double> imps = new ArrayList<>();
        for (Estudio estudio : pendientes) {
            imps.add(estudio.getImporte());
            estudio.marcarFacturado();
        }
        boolean plan = false;
        boolean obra = false;
        String os = paciente.getObraSocial();
        if (os.equals("OSDE") || os.equals("SWISS")) {
            plan = true;
        }
        if (os.equals("PUBLICA")) {
            obra = true;
        }
        int t = contadorTurnos.turnosDe(paciente.getNombre());
        SolicitudFacturacion solicitud = new SolicitudFacturacion(imps, plan, obra, t);
        double total = calculadora.calcularTotal(solicitud);
        Factura factura = facturas.emitir(paciente.getNumero(), total);
        System.out.println("Factura " + factura.getNumero() + " para " + paciente.getNombre()
                + ": " + imps.size() + " estudios, total $" + total);
    }

    private void cobrarFactura(Scanner sc) {
        System.out.println("Nro de factura:");
        int nf = Integer.parseInt(sc.nextLine().trim());
        Factura factura = facturas.buscar(nf);
        // simulacion de la pasarela de pagos
        long centavos = Math.round(factura.getTotal() * 100);
        if (centavos % 100 == 99) {
            System.out.println("Pasarela no disponible, reintente");
        } else if (factura.getTotal() > 50000) {
            factura.marcarRechazada();
            System.out.println("Cobro RECHAZADO factura " + nf);
        } else {
            factura.marcarCobrada();
            cobrado = cobrado + factura.getTotal();
            System.out.println("Cobro APROBADO factura " + nf + " cod AUT-" + nf);
        }
    }

    private void emitirReporte() {
        double facturado = 0;
        int rechazadas = 0;
        int pendientes = 0;
        for (Factura factura : facturas.todas()) {
            facturado = facturado + factura.getTotal();
            if (factura.getEstado() == EstadoFactura.RECHAZADA) {
                rechazadas++;
            }
            if (factura.getEstado() == EstadoFactura.PENDIENTE_DE_COBRO) {
                pendientes++;
            }
        }
        System.out.println("--- REPORTE ---");
        System.out.println("Turnos: " + contadorTurnos.total()
                + " (urgentes: " + contadorTurnos.urgentes() + ")");
        System.out.println("Facturado: $" + facturado);
        System.out.println("Cobrado: $" + cobrado);
        System.out.println("Rechazadas: " + rechazadas + " / Pendientes: " + pendientes);
    }
}
