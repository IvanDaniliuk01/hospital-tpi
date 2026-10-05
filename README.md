# hospital-tpi – Turnos y Facturación (TPI Metodología de Sistemas II)

Proyecto Maven que reúne el código de los TPs anteriores sobre el sistema base
del TPI y agrega, para el **TP Semana 2 (Testing práctico y contratos)**:

- una suite de pruebas unitarias JUnit 5 con patrón **AAA** sobre `CalculadoraFacturacion` (Punto 1);
- un componente mínimo de cobro (`hospital.pagos`) probado con un **Stub** artesanal y con un **Mock** de Mockito (Punto 2);
- dos suites sobre el `procesar` original de TP1 que muestran la diferencia entre **Statement** y **Branch Coverage** con JaCoCo (Punto 3);
- la red de regresión de TP3 (equivalencia original vs. refactor) migrada a pruebas parametrizadas.

## Estructura

```
src/main/java/hospital/
  legacy/        Facturacion (TP3) y TurnoManager (TP1) ORIGINALES, sin modificar*
  turnos/        refactor de TP1: Paciente, CategoriaPaciente, Turno, TurnoManager
  facturacion/   refactor de TP3: SolicitudFacturacion, ReglasFacturacion, PoliticaDescuentos, CalculadoraFacturacion
  pagos/         NUEVO (Semana 2): PasarelaDePagos (puerto), ResultadoPago, ComprobanteDeCobro,
                 ServicioDeCobro, CobroNoProcesadoException, PasarelaNoDisponibleException
src/test/java/hospital/
  facturacion/   CalculadoraFacturacionTest (AAA, Punto 1), EquivalenciaOriginalVsRefactorTest (66 casos)
  turnos/        TurnoManagerTest, CategoriaPacienteTest
  pagos/         PasarelaDePagosStub, ServicioDeCobroTest (Stub vs Mock, Punto 2), ResultadoPagoTest
  legacy/        TurnoManagerLegacyCoberturaIngenuaTest y ...CoberturaDeRamasTest (Punto 3)
evidencias/      reportes JaCoCo (ingenua y completa), Surefire y logs
```

\* A los originales solo se les agregó la línea `package hospital.legacy;` para poder
compilarlos junto al resto; la lógica es idéntica a la entregada.

## Requisitos

JDK 17 o superior y Maven 3.9. Sin otra configuración: las dependencias
(JUnit 5.10, Mockito 5.11, JaCoCo 0.8.12) se descargan de Maven Central.

## Comandos

```
mvn clean verify              # compuerta local de la DoD: compila sin warnings (C1),
                              # corre todas las pruebas (C2), genera el reporte JaCoCo
                              # y falla si la cobertura baja de 80 % líneas / 70 % ramas (C3)
mvn test -Dtest=TurnoManagerLegacyCoberturaIngenuaTest   # solo la suite ingenua del Punto 3
./generar_evidencias.sh       # (o .\generar_evidencias.ps1) genera todo en evidencias/
```

El reporte HTML queda en `target/site/jacoco/index.html`.
