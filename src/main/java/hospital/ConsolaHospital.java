package hospital;

import hospital.facturacion.CalculadoraFacturacion;
import hospital.facturacion.EstadoFactura;
import hospital.facturacion.Estudio;
import hospital.facturacion.Factura;
import hospital.facturacion.PoliticaDescuentos;
import hospital.facturacion.RepositorioEstudios;
import hospital.facturacion.RepositorioFacturas;
import hospital.facturacion.ServicioFacturacion;
import hospital.pacientes.Cobertura;
import hospital.pacientes.Paciente;
import hospital.pacientes.RepositorioPacientes;
import hospital.pagos.PasarelaDePagos;
import hospital.pagos.PasarelaSimulada;
import hospital.pagos.ResultadoPago;
import hospital.pagos.ServicioDeCobro;
import hospital.turnos.AgendaTurnos;
import hospital.turnos.ContadorTurnos;
import hospital.turnos.Turno;
import java.util.Optional;
import java.util.Scanner;

/** Consola del sistema de turnos y facturación del hospital. */
public class ConsolaHospital {

    private final RepositorioPacientes pacientes = new RepositorioPacientes();
    private final ContadorTurnos contadorTurnos = new ContadorTurnos();
    private final RepositorioEstudios estudios = new RepositorioEstudios();
    private final RepositorioFacturas facturas = new RepositorioFacturas();
    private final AgendaTurnos agenda = new AgendaTurnos();
    private final ServicioFacturacion facturacion = new ServicioFacturacion(
            estudios, facturas, new CalculadoraFacturacion(new PoliticaDescuentos()));
    private final ServicioDeCobro cobros;

    public ConsolaHospital(PasarelaDePagos pasarela) {
        this.cobros = new ServicioDeCobro(pasarela);
    }

    public static void main(String[] args) {
        new ConsolaHospital(new PasarelaSimulada()).ejecutar(new Scanner(System.in));
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
        String obraSocial = sc.nextLine().trim();
        Paciente paciente = pacientes.registrar(nombre, edad, obraSocial);
        System.out.println("Paciente " + paciente.getNumero() + " registrado"
                + etiquetaDeRegistro(paciente.getCobertura()));
    }

    private static String etiquetaDeRegistro(Cobertura cobertura) {
        switch (cobertura) {
            case PREPAGA:
                return " (prepaga)";
            case OBRA_SOCIAL:
                return " (obra social)";
            default:
                return "";
        }
    }

    private void darTurno(Scanner sc) {
        Paciente paciente = pedirPacienteExistente(sc);
        if (paciente == null) {
            return;
        }
        System.out.println("Urgente (s/n):");
        boolean urgente = sc.nextLine().trim().equals("s");
        Optional<Turno> turno = agenda.dar(paciente, urgente);
        if (turno.isPresent()) {
            if (paciente.getCobertura() == Cobertura.PREPAGA) {
                System.out.println("Paciente premium");
            } else if (paciente.getCobertura() == Cobertura.OBRA_SOCIAL) {
                System.out.println("Paciente publico");
            }
            System.out.println("Turno agregado");
        }
        contadorTurnos.registrar(paciente.getNombre(), urgente);
    }

    private void listarTurnos() {
        for (Turno turno : agenda.todos()) {
            System.out.println(turno.formatear());
        }
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
        int turnos = contadorTurnos.turnosDe(paciente.getNombre());
        Factura factura = facturacion.facturarPendientes(
                paciente.getNumero(), paciente.getCobertura(), turnos);
        System.out.println("Factura " + factura.getNumero() + " para " + paciente.getNombre()
                + ": " + factura.getCantidadDeEstudios() + " estudios, total $" + factura.getTotal());
    }

    private void cobrarFactura(Scanner sc) {
        System.out.println("Nro de factura:");
        int nf = Integer.parseInt(sc.nextLine().trim());
        Factura factura = facturas.buscar(nf);
        ResultadoPago resultado = cobros.cobrar(factura);
        switch (resultado.getEstado()) {
            case NO_DISPONIBLE:
                System.out.println("Pasarela no disponible, reintente");
                break;
            case RECHAZADO:
                System.out.println("Cobro RECHAZADO factura " + nf);
                break;
            default:
                System.out.println("Cobro APROBADO factura " + nf
                        + " cod " + resultado.getCodigoAutorizacion());
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
        System.out.println("Cobrado: $" + cobros.getTotalCobrado());
        System.out.println("Rechazadas: " + rechazadas + " / Pendientes: " + pendientes);
    }
}
