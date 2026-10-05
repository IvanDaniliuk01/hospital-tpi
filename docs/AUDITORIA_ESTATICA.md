# Auditoría estática del módulo heredado de liquidación

**TP Semana 3 – Punto 2 · Issue #2 · hospital-tpi**
Autor: Ivan Daniliuk · Fecha de la corrida: 2026-10-05 · JDK 21.0.8, Maven 3.9.11

## 1. Objeto y alcance

`auditoria-legacy/` es el módulo de **liquidación de prestaciones a obras sociales**,
recibido "tal como estaba" (versión 2.3, 2019). Tiene 3 clases y 278 líneas:

| Clase | Líneas | Responsabilidad |
|---|---:|---|
| `LiquidadorPrestaciones` | 121 | Calcula el importe a cargo de la obra social por prestación |
| `ReporteMensualLiquidaciones` | 75 | Reporte para gerencia (copia del cálculo "para no tocar la liquidación") |
| `ExportadorLiquidaciones` | 82 | Exporta a JSON, CSV y TXT; arma el comprobante del paciente |

No tiene pruebas: la cobertura es **0 %**. Sus dependencias son las declaradas en 2019,
y **no se actualizan a propósito** para que el reporte las muestre. El módulo se analiza,
pero no se integra: no forma parte del build de `hospital-tpi` ni de la compuerta del
pre-commit.

## 2. Herramientas y umbrales

Se usaron **los mismos perfiles que custodian `hospital-tpi`** (DoD v2.0, criterio C5),
de modo que cada hallazgo equivale a algo que el pre-commit rechazaría.

| Dimensión | Herramienta | Comando (en `auditoria-legacy/`) | Umbral DoD |
|---|---|---|---|
| Complejidad | PMD 7.7 (ruleset de métricas) | `mvn -Pmetricas pmd:pmd` | ciclomática ≤ 10, cognitiva ≤ 15, NPath < 200 |
| Reglas de diseño y errores probables | PMD 7.7 (`config/pmd/ruleset-dod.xml`) | `mvn pmd:pmd` | 0 violaciones de prioridad 1 a 3 |
| Duplicación | CPD (PMD 7.7) | `mvn pmd:cpd` | 0 bloques ≥ 50 tokens |
| Estilo y diseño | Checkstyle 10.21.4 (`config/checkstyle/checkstyle-dod.xml`) | `mvn checkstyle:checkstyle` | 0 violaciones |
| Dependencias desactualizadas | versions-maven-plugin 2.18.0 | `mvn versions:display-dependency-updates` | informativo |
| Árbol de dependencias | maven-dependency-plugin 3.8.1 | `mvn dependency:tree` | informativo |
| Vulnerabilidades conocidas | OSV-Scanner v2.6.0 (base osv.dev: GHSA + NVD) | `osv-scanner -L pom.xml` | 0 Altas/Críticas (C8) |

Las evidencias crudas están en `evidencias/semana3/auditoria/`: XML y HTML de cada
herramienta y los logs de Maven.

## 3. Resultados

### 3.1 Resumen

| Métrica | Valor medido | Umbral DoD | Estado |
|---|---:|---:|:--:|
| Complejidad ciclomática máxima (`liquidar`) | **33** | 10 | ❌ |
| Complejidad cognitiva máxima (`liquidar`) | **117** | 15 | ❌ |
| NPath máximo (`generar`) | **202** | < 200 | ❌ |
| Parámetros de `liquidar` | **10** | 5 | ❌ |
| Profundidad máxima de `if` anidados | **5** | 2 | ❌ |
| Bloques duplicados (CPD ≥ 50 tokens) | **5** (uno con 3 copias) | 0 | ❌ |
| Violaciones PMD bloqueantes (prioridad 1 a 3) | **37** | 0 | ❌ |
| Violaciones Checkstyle | **86** | 0 | ❌ |
| Vulnerabilidades conocidas | **75** en 8 paquetes (17 críticas, 46 altas) | 0 altas/críticas | ❌ |
| Cobertura de pruebas | **0 %** | 80 % líneas / 70 % ramas | ❌ |

### 3.2 Complejidad por método (PMD, ruleset de métricas)

| Método | Ciclomática | Cognitiva | NPath | NCSS | Diagnóstico |
|---|---:|---:|---:|---:|---|
| `LiquidadorPrestaciones.liquidar(10 parámetros)` | **33** | **117** | 142 | 77 | Punto crítico n.º 1 |
| `ReporteMensualLiquidaciones.generar` | **14** | **34** | **202** | 53 | Punto crítico n.º 2 |
| `ExportadorLiquidaciones.exportarCsv` | 4 | 4 | 4 | 16 | Duplicado de `exportarTxt` |
| `ExportadorLiquidaciones.exportarTxt` | 4 | 4 | 4 | 16 | Duplicado de `exportarCsv` |
| `ExportadorLiquidaciones.exportarJson` | 2 | 1 | 2 | 4 | `catch` vacío, devuelve `null` |
| `LiquidadorPrestaciones.resumen` | 2 | 1 | 2 | 7 | — |
| `ExportadorLiquidaciones.comprobante` | 1 | 0 | 1 | 5 | Complejidad baja, **riesgo de seguridad alto** (§ 3.5) |

La complejidad está concentrada: **2 de 7 métodos suman 47 de los 60 puntos de complejidad
ciclomática del módulo** (78 %). La complejidad cognitiva de `liquidar` (117) es 3,5 veces
su ciclomática porque PMD penaliza el anidamiento: hay cadenas `if/else if` de hasta
5 niveles (Checkstyle `NestedIfDepth` informa 18 puntos con profundidad mayor a 2).

### 3.3 Duplicación (CPD, mínimo 50 tokens)

| # | Líneas | Tokens | Ubicaciones | Qué está duplicado |
|---|---:|---:|---|---|
| D1 | 20 | 103 | `LiquidadorPrestaciones` 53-72 · `ReporteMensualLiquidaciones` 33-52 | Cálculo del coseguro OSDE/SWISS + ajustes por edad y urgencia (copia "para no tocar la liquidación") |
| D2 | 12 | 67 | `LiquidadorPrestaciones` 26-37 y 53-64 · `ReporteMensualLiquidaciones` 33-44 | Tabla CONSULTA / IMAGEN / otras: **3 copias** de la misma regla |
| D3 | 10 | 112 | `ExportadorLiquidaciones` 28-37 · 49-58 | Exportación CSV y TXT: solo cambia el separador (`;` y `\|`) |
| D4 | 6 | 62 | `ExportadorLiquidaciones` 30-35 · `ReporteMensualLiquidaciones` 22-27 | Lectura de la prestación desde `Map<String,Object>` con casteos |
| D5 | 6 | 62 | `ExportadorLiquidaciones` 51-56 · `ReporteMensualLiquidaciones` 22-27 | Ídem D4 (tercera copia) |

D1 y D2 son la duplicación más cara. Una misma regla de negocio (el porcentaje de
coseguro) vive en tres lugares, y si cambia el convenio con una obra social hay que
acordarse de los tres. Ya hay una divergencia: el reporte no contempla los planes
310/410 de OSDE ni a PAMI ni a IOMA, así que **el reporte de gerencia y la liquidación
ya no dan el mismo total**.

### 3.4 Violaciones de reglas

**PMD (37 violaciones, todas de prioridad 1 a 3, es decir, bloqueantes):**
`LiteralsFirstInComparisons` 22 · `SystemPrintln` 3 · `CyclomaticComplexity` 2 ·
`CognitiveComplexity` 2 · `NcssCount` 2 · `NPathComplexity` 1 · `ExcessiveParameterList` 1 ·
**`CompareObjectsWithEquals` 1** · **`EmptyCatchBlock` 1 (prioridad 1)** · `ImmutableField` 1 ·
`AvoidDuplicateLiterals` 1.

**Checkstyle (86 violaciones):** `MagicNumber` 53 · `NestedIfDepth` 18 · `AvoidStarImport` 4 ·
`VisibilityModifier` 3 (campos `public` mutables: `errores`, `totalMes`) ·
`CyclomaticComplexity` 2 · `MethodLength` 2 (91 y 62 líneas) · `ParameterNumber` 1 ·
**`StringLiteralEquality` 1** · `EmptyCatchBlock` 1 · `NeedBraces` 1.

Tres de estas violaciones **no son de estilo: son defectos latentes**.

1. **`os == "PAMI"` (línea 73).** Compara referencias, no contenido. Funciona por
   casualidad cuando el texto viene de un literal internado, pero si llega de la base de
   datos, de un archivo o de un `new String(...)`, PAMI deja de reconocerse y la
   prestación se liquida como **particular al 100 %**, en vez de 0 %. PMD y Checkstyle
   lo detectan por separado.
2. **`catch (Exception e) { }` en `exportarJson`.** Traga el error y devuelve `null`, y
   la obra social recibe un archivo vacío sin que nadie se entere.
3. **`FileWriter` sin try-with-resources** en `exportarCsv` y `exportarTxt`. Si una
   prestación tiene datos inválidos, el archivo queda abierto (fuga de recursos).
   Ninguna regla del perfil lo cubre: se detectó en la revisión manual y queda propuesto
   agregar `CloseResource` de PMD al ruleset.

### 3.5 Dependencias

**Árbol resuelto** (`mvn dependency:tree`): 6 dependencias directas, 4 transitivas.

| Dependencia (2019) | Última versión según `versions` | Vulnerabilidades (OSV) | Peor CVSS | Situación |
|---|---|---:|---:|---|
| `jackson-databind` 2.9.8 | 2.22.3 | **59** (12 críticas, 40 altas) | 9.8 | Gadgets de deserialización polimórfica, como CVE-2019-14379 y CVE-2020-8840 |
| ↳ `jackson-core` 2.9.8 (transitiva) | — | 4 (3 altas) | 8.7 | Incluye CVE-2025-52999 (StackOverflow con JSON muy anidado) |
| `log4j` 1.2.17 | *(no informa)* | **6** (3 críticas, 3 altas) | 9.8 | **Fin de vida desde 2015**, sin versión corregida en 1.x: CVE-2019-17571, CVE-2022-23302, 23305 y 23307 |
| `commons-text` 1.9 | 1.15.0 | **1 crítica** | 9.8 | **CVE-2022-42889 "Text4Shell"**, alcanzable desde nuestro código (ver abajo) |
| ↳ `commons-lang3` 3.11 (transitiva) | — | 1 media | 6.5 | CVE-2025-48924 |
| `commons-collections` 3.2.1 | "20040616" *(falso positivo)* | **2** (1 crítica) | 9.8 | CVE-2015-7501: deserialización insegura (corregida en 3.2.2) |
| `commons-lang` 2.6 | *(no informa)* | 1 media | 6.5 | CVE-2025-48924; rama 2.x abandonada, sin corrección |
| `junit` 4.12 (test) | 4.13.2 | 1 media | 4.4 | CVE-2020-15250; solo en pruebas |

**Total OSV-Scanner:** 8 paquetes afectados, **75 vulnerabilidades conocidas: 17 críticas,
46 altas, 10 medias y 2 sin puntaje**. De ellas, 69 tienen versión corregida disponible.
El `pom.xml` de `hospital-tpi`, analizado con la misma herramienta, da **0 vulnerabilidades**.

**Text4Shell es explotable en este módulo, no es teórico.**
`ExportadorLiquidaciones.comprobante()` construye el `StringSubstitutor` con
`StringLookupFactory.INSTANCE.interpolatorStringLookup(datos)`. Así habilita los lookups
`dns:`, `url:` y `script:` del interpolador por defecto de commons-text 1.9. Como los
valores sustituidos se vuelven a interpolar, un dato del paciente con forma `${...}`
(por ejemplo, un nombre cargado desde un formulario web) se evalúa al generar el
comprobante. La combinación "dependencia vulnerable + llamada al código vulnerable +
dato externo" es la que convierte un CVE del listado en un riesgo real. Por eso es el
hallazgo n.º 1.

**Dos límites de las herramientas.**

1. `versions:display-dependency-updates` **no ve el fin de vida**. Para `log4j` 1.2.17 y
   `commons-lang` 2.6 no informa nada, porque bajo esas coordenadas Maven no hay versión
   nueva: el proyecto siguió con otro `groupId` (`org.apache.logging.log4j` y
   `org.apache.commons:commons-lang3`). Para `commons-collections` sugiere "20040616",
   una versión **más vieja** con numeración por fecha (falso positivo conocido, que se
   resuelve con un archivo de reglas que la ignore).
2. OSV-Scanner informa qué versión tiene CVE, pero **no si el código las usa**. La
   alcanzabilidad (el caso Text4Shell) sale de leer el código.

Por eso C8 combina las dos herramientas y una revisión humana.

## 4. Puntos críticos priorizados

Prioridad = impacto × probabilidad. P0 = atender antes de cualquier otro cambio.

| # | Punto crítico | Evidencia | Impacto | Prioridad |
|---|---|---|---|:--:|
| 1 | Text4Shell alcanzable en `comprobante()` | OSV CVE-2022-42889 + uso de `interpolatorStringLookup` | Ejecución remota de código o exfiltración por DNS | **P0** |
| 2 | Log4j 1.x en fin de vida, sin parches | OSV: 6 CVE, 3 críticas | Deserialización y SQLi según configuración de appenders | **P0** |
| 3 | jackson-databind 2.9.8 | OSV: 59 CVE, 12 críticas | Deserialización insegura si se lee JSON externo | **P0** |
| 4 | commons-collections 3.2.1 | OSV CVE-2015-7501 | Cadena de gadgets clásica para deserialización | **P0** (se elimina) |
| 5 | `os == "PAMI"` | PMD `CompareObjectsWithEquals` + Checkstyle `StringLiteralEquality` | Liquidación errónea de afiliados PAMI (0 % → 100 %) | **P1** |
| 6 | `liquidar`: ciclomática 33, cognitiva 117, 10 parámetros | PMD y Checkstyle | Cada convenio nuevo toca el mismo método; probar todas las combinaciones es inviable | **P1** |
| 7 | Regla de coseguro triplicada y ya divergente | CPD D1 y D2 | El reporte de gerencia no coincide con la liquidación | **P1** |
| 8 | `catch` vacío + `return null` en `exportarJson` | PMD `EmptyCatchBlock` (prioridad 1) | Archivo vacío enviado a la obra social sin alerta | **P1** |
| 9 | Exportadores CSV y TXT duplicados; `FileWriter` sin cerrar | CPD D3; revisión manual | Fuga de recursos; cada cambio de formato se hace dos veces | **P2** |
| 10 | `Map<String,Object>` como modelo (Primitive Obsession) | CPD D4 y D5; casteos repetidos | `ClassCastException` en tiempo de ejecución | **P2** |
| 11 | 53 números mágicos; campos `public` mutables | Checkstyle | Reglas comerciales invisibles; estado compartido modificable | **P3** |
| 12 | Cobertura 0 % | Sin carpeta `src/test` | Ningún refactor es seguro hoy | **Prerrequisito** |

## 5. Estrategia de refactorización

Principio rector, el mismo de TP1 y TP3: **primero la red de seguridad, después los cambios
chicos y verificables**. Cada fase es un PR propio que referencia este Issue y tiene que
pasar la compuerta del pre-commit.

### Fase 0: red de seguridad (prerrequisito)

- **Pruebas de caracterización (golden master)** sobre `liquidar` y `generar`. Se usa el
  producto cartesiano de obra social (OSDE, SWISS, PAMI, IOMA, otra, vacía) × plan × tipo ×
  `urg`/`noct`/`fer` × edad (65/66) × sesiones (4/5/10/11), con la salida actual como
  oráculo. Es la misma técnica de los 66 casos de equivalencia del TP3 en `hospital-tpi`.
- El defecto de `os == "PAMI"` se **caracteriza** (con `new String("PAMI")`) y se marca
  como bug conocido. No se corrige en esta fase, para que la red sea fiel.
- Se agrega JaCoCo al módulo. La meta es al menos 90 % de ramas de `liquidar` antes de la Fase 3.

### Fase 1: seguridad de dependencias (inmediata, no toca lógica)

| Acción | Cómo | Resultado esperado |
|---|---|---|
| commons-text 1.9 → **1.15.0** | Además, reemplazar `interpolatorStringLookup(datos)` por `new StringSubstitutor(datos)` (solo variables del mapa) y desactivar la sustitución en valores | Se cierra el punto 1 aunque el día de mañana alguien baje la versión |
| jackson-databind 2.9.8 → **última 2.x** (hoy 2.22.3, según `versions`) vía `jackson-bom` | Un BOM alinea core, annotations y databind; sin `enableDefaultTyping` | Se eliminan las 63 vulnerabilidades informadas de databind + core |
| **Eliminar** commons-collections | Solo se usa `CollectionUtils.isEmpty`: `lista == null \|\| lista.isEmpty()` | Una dependencia menos y 2 CVE menos |
| commons-lang 2.6 → **commons-lang3 ≥ 3.18** | `rightPad`, `leftPad` y `join` tienen la misma firma; cambia el `import` | Sale una rama abandonada |
| log4j 1.2.17 → **SLF4J + Logback** (o Log4j 2 API) | `Logger.getLogger` → `LoggerFactory.getLogger`; logging parametrizado | Se elimina un componente en fin de vida |
| junit 4.12 → JUnit 5 | Junto con la Fase 0 | Mismo stack que `hospital-tpi` |
| Prevención | Dependabot semanal + **C8 en la DoD** + archivo de reglas para `versions` | La deuda de dependencias no vuelve a acumularse en silencio |

**Meta medible:** OSV-Scanner = 0 críticas y 0 altas sobre `auditoria-legacy/pom.xml`.

### Fase 2: defectos latentes (cada uno con su prueba)

1. `os == "PAMI"` → `"PAMI".equals(os)` (*Literals first*, que de paso resuelve las 22
   violaciones `LiteralsFirstInComparisons`). La prueba de caracterización de la Fase 0 se
   invierte y pasa a ser la prueba del bug.
2. `exportarJson`: se elimina el `catch` vacío y se propaga una excepción del dominio con
   la causa (el mismo patrón que `CobroNoProcesadoException` en `hospital.pagos`).
3. `FileWriter` → `try (Writer w = Files.newBufferedWriter(ruta, UTF_8))`.

### Fase 3: bajar la complejidad de `liquidar` (de 33 a ≤ 5 por método)

| Paso | Refactoring (catálogo de Fowler) | Efecto sobre las métricas |
|---|---|---|
| 3.1 | **Introduce Parameter Object**: `Prestacion` (obra social, plan, tipo, importe, sesiones) y `Circunstancias` (urgente, nocturno, feriado, provincia, edad) | 10 parámetros → 2; se resuelve `ExcessiveParameterList` |
| 3.2 | **Replace Magic Number with Symbolic Constant**: `TablaCoseguros` (0.2, 0.3, 0.4, 0.5, 0.05, 1500, 2500, 1.2, 1.3, 65, 4) | 53 `MagicNumber` → 0; las reglas comerciales quedan con nombre |
| 3.3 | **Extract Method** de los ajustes transversales: `aplicarDescuentoPorEdad`, `aplicarRecargoUrgencia`, `aplicarRecargoNocturnoFeriado`, `aplicarTopeDeSesiones` | Saca 3 niveles de anidamiento |
| 3.4 | **Replace Conditional with Polymorphism (Strategy)**: interfaz `ReglaCobertura` con `ReglaOsde`, `ReglaSwiss`, `ReglaPami`, `ReglaIoma` y `ReglaParticular`, más un `Map<String, ReglaCobertura>` como registro | El `if/else if` por obra social desaparece; agregar un convenio = una clase nueva (OCP) |
| 3.5 | **Extract Class** `CalculadoraCoseguro` (CONSULTA / IMAGEN / otras) compartida por OSDE y SWISS | Elimina D2 dentro del liquidador |
| 3.6 | **Encapsulate Field**: `errores` y `totalMes` privados, con `getErrores()` inmodificable | Resuelve `VisibilityModifier` |

Es el mismo camino que ya recorrieron TP1 y TP3. Las mismas herramientas, sobre el
original y el refactor de `hospital-tpi`, muestran el efecto:

| Método original | Ciclomática / cognitiva | Reemplazo en el refactor | Ciclomática / cognitiva | Máximo del paquete refactorizado |
|---|---|---|---|---|
| `legacy.TurnoManager.procesar` (TP1) | 8 / 12 | `turnos.TurnoManager.registrarTurno` | 2 / 1 | 4 (`CategoriaPaciente.desde`, un `switch`) |
| `legacy.Facturacion.calc` (TP3) | 5 / 6 | `facturacion.CalculadoraFacturacion.calcularTotal` | 3 / 1 | 6 (`SolicitudFacturacion.copiaValidada`: validaciones de entrada que el original no tenía) |

En los dos casos la complejidad se **repartió** en métodos con un solo motivo de cambio,
en vez de desaparecer. Ese es el resultado esperado para `liquidar`.

### Fase 4: eliminar la duplicación

- **D1/D2:** `ReporteMensualLiquidaciones` deja de recalcular y **usa** `LiquidadorPrestaciones`
  (o la `CalculadoraCoseguro` de la Fase 3). La divergencia entre reporte y liquidación
  queda corregida y cubierta por una prueba que compara los dos totales.
- **D3:** un único `exportarDelimitado(prestaciones, archivo, separador)` (Parameterize
  Method). Si los formatos siguen divergiendo, se pasa a Strategy `FormatoExportacion`.
- **D4/D5:** `Prestacion` tipada (Fase 3.1) reemplaza `Map<String,Object>` y desaparecen
  los casteos repetidos.

**Meta medible:** CPD = 0 bloques de 50 tokens o más.

### Fase 5: blindaje

El módulo pasa a ser parte del build y de la compuerta, con Spotless, Checkstyle, PMD, CPD,
JaCoCo y OSV. Desde ese commit, el pre-commit rechaza cualquier regresión. **El Issue
#2 se cierra cuando las métricas de la tabla siguiente quedan en verde.**

| Métrica | Hoy | Meta |
|---|---:|---:|
| Ciclomática máxima por método | 33 | ≤ 10 (objetivo 5) |
| Cognitiva máxima por método | 117 | ≤ 15 |
| Bloques duplicados (CPD) | 5 | 0 |
| Violaciones PMD prioridad 1 a 3 / Checkstyle | 37 / 86 | 0 / 0 |
| Vulnerabilidades críticas + altas (OSV) | 63 | 0 |
| Cobertura de líneas / ramas | 0 % / 0 % | ≥ 80 % / ≥ 70 % (DoD C3) |
