# Informe de Calidad en Verde · hospital-tpi v0.3.0

**TP Semana 3 – Punto 3** · Autor: Ivan Daniliuk · Metodología de Sistemas II (UTN)

| | |
|---|---|
| Repositorio | https://github.com/IvanDaniliuk01/hospital-tpi |
| Issue | [#1 – Compuerta de calidad automatizada](https://github.com/IvanDaniliuk01/hospital-tpi/issues/1) |
| Pull Request | [#3](https://github.com/IvanDaniliuk01/hospital-tpi/pull/3) · rama `feature/1-compuerta-calidad-precommit` → `main` |
| Issue relacionado | [#2 – Deuda técnica del módulo heredado](https://github.com/IvanDaniliuk01/hospital-tpi/issues/2) (diagnóstico entregado, sigue abierto) |
| Corrida de evidencias | 2026-10-05 · Windows 10, JDK 21.0.8, Maven 3.9.11, Git 2.49.0 |

## Dictamen

> **APTO PARA INTEGRACIÓN.** El incremento cumple todos los criterios de la DoD v2.0
> verificables por máquina (C1, C2, C3, C5 y C8), con evidencia de herramienta.
> El merge queda sujeto a C4 (aprobación de un par), que por definición no puede
> certificar el propio autor.

| Criterio DoD v2.0 | Umbral | Resultado medido | Evidencia | Estado |
|---|---|---|---|:--:|
| C1 Compilación limpia | 0 warnings con `-Xlint:all -Werror` | 0 warnings | `compuerta/mvn-clean-verify.log` | ✅ |
| C2 Pruebas | 100 % en verde | **137 / 137**, 0 fallas, 0 omitidas | `compuerta/surefire/` | ✅ |
| C3 Cobertura | ≥ 80 % líneas, ≥ 70 % ramas | **97,97 % líneas (193/197) · 100 % ramas (89/89)** | `compuerta/jacoco/` | ✅ |
| C4 Revisión por pares | ≥ 1 aprobación | Pendiente: PR #3 abierto para revisión | PR | ⏳ |
| C5 Formato | google-java-format | 23/23 archivos limpios | `spotless:check` | ✅ |
| C5 Checkstyle | 0 violaciones | **0** | `compuerta/checkstyle-result.xml` | ✅ |
| C5 PMD | 0 de prioridad 1 a 3 | **0** (0 en total) | `compuerta/pmd.xml` | ✅ |
| C5 CPD | 0 bloques ≥ 50 tokens | **0** | `compuerta/cpd.xml` | ✅ |
| C6 Documentación | Javadoc / ADR | DoD v2.0, README, auditoría e informe | `docs/` | ✅ |
| C7 Calidad de las pruebas | AAA, dobles solo de infraestructura | Sin cambios en las pruebas desde Semana 2 (solo formato) | Diff del commit `style:` | ✅ |
| C8 Dependencias | 0 vulnerabilidades altas o críticas | **0 vulnerabilidades** (OSV-Scanner v2.6.0) | `auditoria/osv-hospital-tpi.*` | ✅ |

## 1. Trazabilidad Issue ↔ Pull Request ↔ commits

- El Issue #1 se creó con la plantilla `feature_request` y las etiquetas
  `tipo/feature`, `prioridad/p1`, `estado/en-curso` y `dod/en-revision`.
- La rama lleva el número del Issue: `feature/1-compuerta-calidad-precommit`.
- Cada commit de la rama referencia el Issue en el mensaje: `(#1)` o `(refs #2)`.
- El cuerpo del PR contiene **`Closes #1`**. GitHub registra el vínculo
  (`closingIssuesReferences` en `evidencias/semana3/github/pr.json`) y va a cerrar el
  Issue automáticamente cuando el PR se integre a `main`.
- El Issue #2 se referencia (`Refs`), pero no se cierra: la estrategia de
  refactorización se ejecuta en PRs posteriores.

## 2. Logs del pre-commit hook

| Ejecución | Cambio | Resultado del hook | ¿Se creó el commit? | Log |
|---|---|---|:--:|---|
| Primer commit con el hook | Hooks + DoD v2.0 | 1/3 ✅ 2/3 ✅ 3/3 ✅ (33 s) | Sí | `precommit/01-*.hook.log` |
| Escenario A | `esValido(){ return a()&&b(); }` sin formato | **1/3 ❌ Spotless**: "format violations… Run 'mvn spotless:apply'" | **No** | `precommit/A-formatter.*` |
| Escenario B | `if (total > 0) return total * 1.15;` | 1/3 ✅ **2/3 ❌ Checkstyle**: `NeedBraces` + `MagicNumber` | **No** | `precommit/B-linter.*` |
| Escenario C | Bonificación desde 2 turnos en lugar de 3 | 1/3 ✅ 2/3 ✅ **3/3 ❌ 17 de 137 pruebas fallan** | **No** | `precommit/C-pruebas.*` |
| Snapshot del módulo heredado | `auditoria-legacy/` | 1/3 ✅ 2/3 ✅ 3/3 ✅ (26 s) | Sí | `precommit/02-*.hook.log` |
| Commit de evidencias | `docs/` y `evidencias/` | Ver log | Sí | `precommit/03-*.hook.log` |
| Push de la rama | — | pre-push: `mvn clean verify` | Sí | `precommit/04-push-rama.*` |
| Bypass | `--no-verify` y push a `main` | pre-push y protección de GitHub | — | `precommit/bypass/` |

En los tres escenarios de falla, **HEAD no se movió** (mismo hash antes y después) y los
cambios quedaron en staging para corregirlos.

## 3. Reporte analítico de cobertura (JaCoCo 0.8.12)

| Paquete | Líneas | Ramas | Instrucciones | Métodos |
|---|---:|---:|---:|---:|
| `hospital.facturacion` | 43/44 (97,7 %) | 18/18 (100 %) | 157/168 | 14/15 |
| `hospital.legacy` | 31/31 (100 %) | 24/24 (100 %) | 124/124 | 5/5 |
| `hospital.pagos` | 58/58 (100 %) | 24/24 (100 %) | 236/236 | 23/23 |
| `hospital.turnos` | 61/64 (95,3 %) | 23/23 (100 %) | 232/239 | 23/25 |
| **Total** | **193/197 (97,97 %)** | **89/89 (100 %)** | **749/767 (97,65 %)** | **65/68** |

Las 4 líneas sin cubrir no tienen lógica de decisión: el constructor por defecto
`TurnoManager()`, que solo delega en `System.out` (2 líneas), el getter `Turno.esUrgente()`
y `SolicitudFacturacion.toString()`. Ninguna rama queda sin cubrir.

## 4. Diagnóstico final de análisis estático

**Incremento (`hospital-tpi`, sin el paquete `legacy`, que guarda los originales de TP1/TP3
como referencia de las pruebas de equivalencia):** 0 violaciones de Checkstyle, 0 de PMD y
0 duplicados de CPD. Formato limpio. Ningún método supera ciclomática 7
(`ServicioDeCobro.cobrar`) ni cognitiva 4.
OSV-Scanner: 0 vulnerabilidades. `versions` informa que hay versiones nuevas de JUnit
(5.10.2 → 6.1.3, cambio mayor) y de Mockito (5.11 → 5.24). Las dos son de alcance `test` y
no tienen CVE: se registran como deuda `prioridad/p3`.

**Módulo heredado (`auditoria-legacy`, fuera del incremento):** ciclomática máxima 33,
5 bloques duplicados y 75 vulnerabilidades conocidas (17 críticas). Ver
[AUDITORIA_ESTATICA.md](AUDITORIA_ESTATICA.md) e Issue #2. Está **aislado**: no
participa del build ni de la compuerta, y la DoD aplica al incremento.

## 5. Evidencias consolidadas de los trabajos prácticos anteriores

| TP | Qué aportó | Dónde está en el repositorio |
|---|---|---|
| TP1 – Calidad y diseño | `TurnoManager` original y refactorizado | `src/main/java/hospital/legacy/TurnoManager.java` · `hospital/turnos/` |
| TP3 – Clean Code y code review | `Facturacion` original y refactor (Parameter Object, Extract/Move Method) | `hospital/legacy/Facturacion.java` · `hospital/facturacion/` |
| Semana 1 – Gobernanza | DoD, plantillas de Issue y PR, 25 etiquetas en 4 ejes | `DEFINITION_OF_DONE.md` (v1.1 → v2.0) · `.github/` |
| Semana 2 – Testing y contratos | 137 pruebas: AAA, Stub vs Mock, cobertura de ramas, equivalencia | `src/test/java/` · `evidencias/semana2/` |
| **Semana 3 – Automatización** | Formatter, linters, pre-commit/pre-push, auditoría, este informe | `.githooks/` · `config/` · `docs/` · `evidencias/semana3/` |
