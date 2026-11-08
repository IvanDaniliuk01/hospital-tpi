# Code review de la versión refactorizada

**Alcance:** diff `estado-inicial..4b15fc3` (refactorings R1 a R6, suite de pruebas y compuerta Maven).
**Autor:** Ivan Daniliuk. **Revisor:** asistente de IA declarado (Claude), según ADR-0004. El autor responde cada observación y decide si se acepta.
**Herramientas usadas en la revisión:** golden master, 35 pruebas JUnit, Checkstyle 10.21.4 y PMD 7.7.0 con la configuración de la DoD (`config/`).

## 1. Checklist técnico

| # | Criterio | Cómo se verifica | Resultado |
|---|---|---|---|
| C1 | Compila sin warnings | `javac -Xlint:all -Werror` | ✔ |
| C2 | Las pruebas pasan | `mvn test` (35 pruebas) | ✔ |
| C3 | El comportamiento externo no cambió | Golden master con `diff` vacío en cada commit R1 a R6 | ✔ |
| C4 | Cada clase tiene una sola responsabilidad (SRP) | Lectura del diff | ◐ La consola todavía arma el reporte |
| C5 | Las dependencias externas entran por abstracciones (DIP) | Lectura del diff | ◐ La consola escribe directo en `System.out` |
| C6 | Nombres de dominio, sin abreviaturas | Lectura del diff contra `CONTEXT.md` | ✔ |
| C7 | Sin números ni textos mágicos | Checkstyle `MagicNumber` | ✘ 2 violaciones en `PasarelaSimulada` |
| C8 | Complejidad acotada | PMD `CyclomaticComplexity` (máximo 10) | ✘ `ejecutar` tiene 12 |
| C9 | Comparaciones de texto seguras ante null | PMD `LiteralsFirstInComparisons` | ✘ 4 violaciones |
| C10 | Errores con mensajes que explican la causa | Lectura del diff | ✘ `catch` genérico que imprime `Error` |
| C11 | Reglas de negocio coherentes con el glosario | Lectura contra `CONTEXT.md` y pruebas de caracterización | ✘ Los defectos D1 a D5 siguen presentes (a propósito, ADR-0003) |
| C12 | Pruebas con estructura AAA, un Act por prueba y dobles solo para infraestructura | Lectura de `src/test` | ✔ |

## 2. Observaciones

Severidad: **[Alta]** bloquea la aprobación, **[Media]** se corrige antes de cerrar la etapa, **[Baja]** puede quedar como deuda registrada, **[Pregunta]** pide una aclaración.

| Id | Severidad | Ubicación | Observación del revisor | Respuesta del autor | Acción |
|---|---|---|---|---|---|
| O1 | [Alta] | `ContadorTurnos` y `ConsolaHospital.darTurno` | El contador cuenta por nombre y suma aunque `AgendaTurnos` no haya dado el turno. Es la causa de D1 y D2 | De acuerdo. Contar por número de paciente y solo turnos efectivamente dados | Corrección F1 y F2 |
| O2 | [Alta] | `Cobertura.desde` | `equals` distingue mayúsculas y no tolera null (causa D3) | De acuerdo | Corrección F3 |
| O3 | [Alta] | `ServicioFacturacion.facturarPendientes` y `PoliticaDescuentos` | Se puede emitir una factura sin estudios y el total puede quedar negativo (D4). Pasa también con estudios: una prepaga con un estudio de $100 y 4 turnos da −$50 | De acuerdo con las dos cosas. No se factura sin estudios pendientes y la bonificación no puede superar el total | Corrección F4 |
| O4 | [Alta] | `ServicioDeCobro.cobrar` | No consulta el estado: una factura cobrada se vuelve a cobrar (D5) | De acuerdo | Corrección F5 |
| O5 | [Media] | `ConsolaHospital.ejecutar` | Complejidad ciclomática 12 por el `switch` de opciones | De acuerdo. Reemplazar por un mapa de operaciones | Refactor sin cambio de comportamiento |
| O6 | [Media] | `ConsolaHospital` | 35 llamadas a `System.out.println`. La consola no se puede probar sin redirigir la salida global | De acuerdo. Inyectar un `PrintStream` | Refactor sin cambio de comportamiento |
| O7 | [Media] | `PasarelaSimulada` | `100` aparece dos veces como número mágico | De acuerdo | Constante `CENTAVOS_POR_PESO` |
| O8 | [Media] | `ConsolaHospital.ejecutar` | El `catch` imprime `Error` sin causa. Una factura inexistente termina en `IndexOutOfBoundsException` | De acuerdo | Corrección F6 |
| O9 | [Baja] | `Paciente.getEdad` | La edad se guarda como texto y se interpreta en cada uso. Una edad no numérica recién falla al dar el turno | Se acepta como deuda: validar al registrar cambia la numeración de pacientes y el flujo del operador | Issue abierta, mejora futura |
| O10 | [Pregunta] | `PoliticaDescuentos.aplicarBonificacionPorFrecuencia` | ¿La bonificación se aplica en cada factura del paciente o una sola vez? | En cada factura. Es la regla de negocio vigente y quedó escrita en el glosario | Sin cambios |

## 3. Criterios de aceptación

La etapa se aprueba cuando se cumplen todas estas condiciones:

1. No queda ninguna observación [Alta] ni [Media] abierta.
2. `mvn clean verify` termina en verde: pruebas, JaCoCo (80 % de líneas y 70 % de ramas), Spotless, Checkstyle, PMD y CPD sin violaciones.
3. Cada corrección que cambia el comportamiento (F1 a F6) tiene su Issue, actualiza la salida esperada del golden master y explica en el commit qué cambió y por qué.
4. Cada refactor de la revisión (O5 a O7) deja el golden master sin cambios.
5. Las observaciones [Baja] aceptadas como deuda quedan registradas como Issue.
