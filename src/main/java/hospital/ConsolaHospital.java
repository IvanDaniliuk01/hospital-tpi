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
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/** Consola del sistema de turnos y facturación del hospital. */
public class ConsolaHospital {

  private static final String OPCION_SALIR = "0";

  private final RepositorioPacientes pacientes = new RepositorioPacientes();
  private final ContadorTurnos contadorTurnos = new ContadorTurnos();
  private final RepositorioEstudios estudios = new RepositorioEstudios();
  private final RepositorioFacturas facturas = new RepositorioFacturas();
  private final AgendaTurnos agenda = new AgendaTurnos();
  private final ServicioFacturacion facturacion =
      new ServicioFacturacion(
          estudios, facturas, new CalculadoraFacturacion(new PoliticaDescuentos()));
  private final ServicioDeCobro cobros;

  private final PrintStream salida;
  private final Map<String, Operacion> operaciones = new LinkedHashMap<>();

  /** Una opción del menú. Recibe el scanner para pedir sus propios datos. */
  @FunctionalInterface
  private interface Operacion {
    void ejecutar(Scanner sc);
  }

  public ConsolaHospital(PasarelaDePagos pasarela, PrintStream salida) {
    this.cobros = new ServicioDeCobro(pasarela);
    this.salida = salida;
    operaciones.put("1", this::registrarPaciente);
    operaciones.put("2", this::darTurno);
    operaciones.put("3", sc -> listarTurnos());
    operaciones.put("4", this::registrarEstudio);
    operaciones.put("5", this::facturar);
    operaciones.put("6", this::cobrarFactura);
    operaciones.put("7", sc -> emitirReporte());
  }

  public static void main(String[] args) {
    new ConsolaHospital(new PasarelaSimulada(), System.out).ejecutar(new Scanner(System.in));
  }

  public void ejecutar(Scanner sc) {
    while (sc.hasNextLine()) {
      mostrarMenu();
      String opcion = sc.nextLine().trim();
      if (OPCION_SALIR.equals(opcion)) {
        salida.println("Fin");
        return;
      }
      Operacion operacion = operaciones.get(opcion);
      if (operacion == null) {
        salida.println("Opcion invalida");
        continue;
      }
      try {
        operacion.ejecutar(sc);
      } catch (RuntimeException ex) {
        salida.println("Error");
      }
    }
  }

  private void mostrarMenu() {
    salida.println("=== HOSPITAL ===");
    salida.println("1) Registrar paciente");
    salida.println("2) Dar turno");
    salida.println("3) Listar turnos");
    salida.println("4) Registrar estudio");
    salida.println("5) Facturar");
    salida.println("6) Cobrar factura");
    salida.println("7) Reporte");
    salida.println("0) Salir");
  }

  /** Pide el número de paciente. Devuelve null (y avisa) si no existe. */
  private Paciente pedirPacienteExistente(Scanner sc) {
    salida.println("Nro de paciente:");
    int numero = Integer.parseInt(sc.nextLine().trim());
    Paciente paciente = pacientes.buscar(numero);
    if (paciente == null) {
      salida.println("No existe el paciente");
    }
    return paciente;
  }

  private void registrarPaciente(Scanner sc) {
    salida.println("Nombre:");
    String nombre = sc.nextLine().trim();
    salida.println("Edad:");
    String edad = sc.nextLine().trim();
    salida.println("Obra social:");
    String obraSocial = sc.nextLine().trim();
    Paciente paciente = pacientes.registrar(nombre, edad, obraSocial);
    salida.println(
        "Paciente "
            + paciente.getNumero()
            + " registrado"
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
    salida.println("Urgente (s/n):");
    boolean urgente = "s".equals(sc.nextLine().trim());
    Optional<Turno> turno = agenda.dar(paciente, urgente);
    if (turno.isEmpty()) {
      salida.println("Turno no agregado: el paciente tiene nombre vacio o edad no valida");
      return;
    }
    if (paciente.getCobertura() == Cobertura.PREPAGA) {
      salida.println("Paciente premium");
    } else if (paciente.getCobertura() == Cobertura.OBRA_SOCIAL) {
      salida.println("Paciente publico");
    }
    salida.println("Turno agregado");
    contadorTurnos.registrar(paciente.getNombre(), urgente);
  }

  private void listarTurnos() {
    for (Turno turno : agenda.todos()) {
      salida.println(turno.formatear());
    }
  }

  private void registrarEstudio(Scanner sc) {
    Paciente paciente = pedirPacienteExistente(sc);
    if (paciente == null) {
      return;
    }
    salida.println("Importe:");
    double importe = Double.parseDouble(sc.nextLine().trim());
    estudios.registrar(new Estudio(paciente.getNumero(), importe));
    salida.println("Estudio registrado para " + paciente.getNombre());
  }

  private void facturar(Scanner sc) {
    Paciente paciente = pedirPacienteExistente(sc);
    if (paciente == null) {
      return;
    }
    int turnos = contadorTurnos.turnosDe(paciente.getNombre());
    Factura factura =
        facturacion.facturarPendientes(paciente.getNumero(), paciente.getCobertura(), turnos);
    salida.println(
        "Factura "
            + factura.getNumero()
            + " para "
            + paciente.getNombre()
            + ": "
            + factura.getCantidadDeEstudios()
            + " estudios, total $"
            + factura.getTotal());
  }

  private void cobrarFactura(Scanner sc) {
    salida.println("Nro de factura:");
    int nf = Integer.parseInt(sc.nextLine().trim());
    Factura factura = facturas.buscar(nf);
    ResultadoPago resultado = cobros.cobrar(factura);
    switch (resultado.getEstado()) {
      case NO_DISPONIBLE:
        salida.println("Pasarela no disponible, reintente");
        break;
      case RECHAZADO:
        salida.println("Cobro RECHAZADO factura " + nf);
        break;
      default:
        salida.println(
            "Cobro APROBADO factura " + nf + " cod " + resultado.getCodigoAutorizacion());
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
    salida.println("--- REPORTE ---");
    salida.println(
        "Turnos: " + contadorTurnos.total() + " (urgentes: " + contadorTurnos.urgentes() + ")");
    salida.println("Facturado: $" + facturado);
    salida.println("Cobrado: $" + cobros.getTotalCobrado());
    salida.println("Rechazadas: " + rechazadas + " / Pendientes: " + pendientes);
  }
}
