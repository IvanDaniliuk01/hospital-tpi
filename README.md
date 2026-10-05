# hospital-tpi – Turnos, Facturación y Cobros

Sistema base del **TP Integrador de Metodología de Sistemas II** (UTN – Tecnicatura
Universitaria en Programación a Distancia). Autor: **Ivan Daniliuk**.

| Versión | TP | Qué agregó |
|---|---|---|
| 0.1 | TP1 / TP3 | Refactor de `TurnoManager` (turnos) y de `Facturacion` (facturación) |
| 0.2.0 | Semana 2 – Testing y contratos | Proyecto Maven, 137 pruebas JUnit 5, Stub vs Mock (`hospital.pagos`), JaCoCo |
| **0.3.0** | **Semana 3 – Automatización de calidad** | Formatter, linters, **pre-commit hook**, auditoría estática, Informe de Calidad en Verde |

La gobernanza (Definition of Done, plantillas de Issue y PR, etiquetas) viene del
TP Semana 1 y está en [`DEFINITION_OF_DONE.md`](DEFINITION_OF_DONE.md) y [`.github/`](.github).

## Estructura

```
src/main/java/hospital/
  legacy/        Facturacion (TP3) y TurnoManager (TP1) ORIGINALES (referencia de equivalencia)
  turnos/        refactor de TP1
  facturacion/   refactor de TP3 (Parameter Object, Extract/Move Method)
  pagos/         componente de cobro con puerto PasarelaDePagos (Semana 2)
src/test/java/   137 pruebas: AAA, Stub vs Mock, cobertura de ramas, equivalencia original/refactor
config/          perfiles de Checkstyle y PMD de la DoD (+ ruleset de métricas)
.githooks/       pre-commit (formatter -> linter -> pruebas) y pre-push (mvn verify, main protegida)
auditoria-legacy/  módulo heredado de liquidación: objeto de la auditoría estática (Issue #2)
docs/            AUDITORIA_ESTATICA.md e INFORME_CALIDAD_VERDE.md
evidencias/      logs y reportes de herramientas (semana2/, semana3/)
scripts/         scripts que generaron las evidencias y archivos de los escenarios de falla
```

## Requisitos

JDK 17+ (probado con JDK 21) y Maven 3.9. Git for Windows (los hooks son scripts bash).

## Instalación del pre-commit hook (una vez por clon)

```
git config core.hooksPath .githooks
```

Desde ese momento **cada `git commit`** ejecuta, en orden y sin excepción:

| Paso | Control | Comando | Falla si… |
|---|---|---|---|
| 1/3 | Formatter | `mvn spotless:check` | algún archivo no respeta google-java-format |
| 2/3 | Linter | `mvn compile checkstyle:check pmd:check pmd:cpd-check` | warning de javac, violación Checkstyle, PMD prioridad 1–3 o bloque duplicado ≥ 50 tokens |
| 3/3 | Pruebas | `mvn test` | una sola prueba falla |

Si un paso falla, los siguientes no corren y **el commit no se crea**. Cada ejecución
queda registrada en `.git/precommit-logs/`. El `pre-push` rechaza el push directo a
`main` y corre la compuerta completa (`mvn verify`, con umbrales de cobertura).

## Comandos útiles

```
mvn spotless:apply                         # corrige el formato
mvn clean verify                           # compuerta completa de la DoD v2.0
mvn -Pmetricas pmd:pmd                     # métricas de complejidad de todos los métodos
mvn versions:display-dependency-updates    # dependencias con versiones nuevas
cd auditoria-legacy && mvn compile pmd:pmd pmd:cpd checkstyle:checkstyle   # auditoría del módulo heredado
```
