# hospital-tpi

Consola de turnos, facturación y cobro de un hospital, usada como sistema base del Trabajo Práctico Integrador de Metodología de Sistemas II (UTN, Tecnicatura Universitaria en Programación a distancia). El repositorio muestra la evolución completa del sistema: una versión inicial con problemas reales de diseño, su diagnóstico, los refactorings que la mejoran sin cambiar el comportamiento, las pruebas que lo verifican, la revisión de código y la corrección de los defectos encontrados.

**Autor:** Ivan Daniliuk. Entrega individual.

## Funcionalidades

El operador usa un menú de texto con 7 operaciones:

| Opción | Operación | Qué hace |
|---|---|---|
| 1 | Registrar paciente | Pide nombre, edad y obra social, asigna un número correlativo y deriva la cobertura |
| 2 | Dar turno | Da un turno, urgente o no, a un paciente existente |
| 3 | Listar turnos | Muestra los turnos dados, en orden |
| 4 | Registrar estudio | Registra un estudio con su importe, pendiente de facturar |
| 5 | Facturar | Emite una factura con los estudios pendientes del paciente y aplica las reglas de cobertura y la bonificación por frecuencia |
| 6 | Cobrar factura | Envía la factura a la pasarela de pagos simulada |
| 7 | Reporte | Muestra el resumen de la sesión: turnos, urgentes, facturado, cobrado, rechazadas y pendientes |

Los datos viven en memoria mientras la consola está abierta. Los términos del dominio (Paciente, Cobertura, Factura, Bonificación por frecuencia y el resto) están definidos en [`CONTEXT.md`](CONTEXT.md).

## Tecnologías

- Java 17 (probado también con JDK 21), sin dependencias de ejecución.
- Maven 3.9 para la compuerta de calidad: JUnit 5, JaCoCo, Spotless con google-java-format, Checkstyle, PMD y CPD.
- Git hooks versionados en `.githooks/`.

## Uso

Ejecutar la consola sin Maven:

```
javac -d out $(find src/main/java -name "*.java")
java -cp out hospital.ConsolaHospital
```

En Windows (PowerShell):

```
javac -d out (Get-ChildItem -Recurse src\main\java -Filter *.java).FullName
java -cp out hospital.ConsolaHospital
```

Para ver una sesión completa sin escribir nada, se le puede pasar el guion de prueba: `java -cp out hospital.ConsolaHospital < pruebas/golden/guion.txt`.

## Verificación

| Comando | Qué verifica |
|---|---|
| `mvn clean verify` | Compilación sin warnings, 41 pruebas, cobertura mínima, formato, Checkstyle, PMD y CPD |
| `pruebas/golden/verificar.sh` o `pruebas\golden\verificar.ps1` | Golden master: la consola frente al guion produce exactamente `salida_esperada.txt` |
| `git config core.hooksPath .githooks` | Activa los hooks: el pre-commit corre formatter, linter y pruebas, y el pre-push corre `mvn clean verify` |

## Recorrido por etapas

Cada etapa del TPI tiene su tag. `git checkout <tag>` deja el código como estaba en ese momento.

| Tag | Etapa | Qué contiene |
|---|---|---|
| `estado-inicial` | 1 | Prototipo base: `ConsolaHospital` como God Class sobre las clases heredadas `TurnoManager` y `Facturacion` |
| `etapa-diagnostico` | 2 y 3 | Golden master y [`docs/DIAGNOSTICO.md`](docs/DIAGNOSTICO.md): calidad externa e interna, 12 smells y 5 defectos |
| `etapa-refactoring` | 4 y 5 | Refactorings R1 a R6, un commit cada uno, con la salida del golden master idéntica |
| `etapa-pruebas` | 6 y 7 | Suite JUnit, compuerta Maven, hooks, [revisión de código](docs/REVISION_CODIGO.md) y correcciones F1 a F6 |
| `etapa-docs` | 8 | Documentación: README, glosario, ADR, DoD y registro de uso de IA |
| `estado-refactorizado` | | Estado final del código |

La historia del `main` se reconstruyó para el TPI (ADR-0001). El trabajo original de las Unidades 3 y 4 está en la rama `archivo/unidad-3`.

## Documentación

- [`docs/DIAGNOSTICO.md`](docs/DIAGNOSTICO.md): diagnóstico del sistema base.
- [`docs/REVISION_CODIGO.md`](docs/REVISION_CODIGO.md): checklist técnico, observaciones, respuestas y criterios de aceptación.
- [`docs/adr/`](docs/adr/): decisiones de arquitectura (ADR-0001 a ADR-0005).
- [`docs/USO_DE_IA.md`](docs/USO_DE_IA.md): herramienta, finalidad, grado de intervención y registro de prompts.
- [`DEFINITION_OF_DONE.md`](DEFINITION_OF_DONE.md): criterios C1 a C8 y líneas de defensa de `main`.
- [`evidencias/RESUMEN.md`](evidencias/RESUMEN.md): análisis estático antes y después y cobertura.

Todos los datos de pacientes del guion son sintéticos.
