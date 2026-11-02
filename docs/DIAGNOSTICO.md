# Diagnóstico del sistema base (tag `estado-inicial`)

Analizo la versión inicial de la consola de turnos y facturación: `ConsolaHospital` más las clases heredadas `TurnoManager` (TP1) y `Facturacion` (TP3). Las líneas citadas corresponden al commit etiquetado `estado-inicial`. La evidencia de comportamiento sale del golden master (`pruebas/golden/guion.txt` y `salida_esperada.txt`), que ejecuta las 7 operaciones con datos sintéticos.

## 1. Calidad externa: lo que ve el usuario

Desde el lado del operador el sistema funciona. Las 7 operaciones del menú responden y en el camino feliz los resultados son correctos: registra pacientes con número correlativo, da turnos, lista turnos, registra estudios, factura con las reglas de cobertura y bonificación, cobra contra la pasarela y emite el reporte de sesión. Un usuario que prueba un caso típico no encuentra problemas.

El guion muestra cinco defectos que un usuario atento sí puede ver:

| Id | Defecto observable | Evidencia en `salida_esperada.txt` |
|---|---|---|
| D1 | El reporte cuenta turnos que nunca se dieron. A Dario (edad 0) `TurnoManager` no le agrega el turno, pero la consola lo cuenta igual | Se listan 6 turnos y el reporte dice `Turnos: 7` |
| D2 | Dos pacientes con el mismo nombre comparten el contador de turnos y la bonificación | `Factura 2 para Ana Gomez: 0 estudios, total $-100.0` (la segunda Ana tiene un solo turno) |
| D3 | La cobertura distingue mayúsculas. «osde» no se reconoce como prepaga | Carla se registra sin cobertura y su factura no tiene el 50 % |
| D4 | Se emite y se cobra una factura sin estudios con total negativo | `total $-100.0` y luego `Cobro APROBADO factura 2` |
| D5 | Una factura cobrada se puede volver a cobrar | `Cobro APROBADO factura 1` aparece dos veces y el reporte suma `Cobrado: $11700.0` |

Ninguno rompe la ejecución, por eso pasan inadvertidos. Para un hospital los cinco tienen costo real: D4 y D5 generan registros contables falsos y D2 regala bonificaciones.

## 2. Calidad interna: lo que ve el equipo técnico

La estructura interna es mucho peor que lo que sugiere la interfaz. Una sola clase de 191 líneas concentra la interfaz de consola, el almacenamiento, las reglas de cobertura, el conteo de turnos y la simulación de la pasarela. Todo el estado es `static`. Los datos de dominio viajan como `String[]` y `double[]` con significados por posición. La regla de cobertura está escrita tres veces en tres lugares distintos.

La relación con la calidad externa es directa. Cada defecto de la tabla anterior nace de un problema interno: D1 y D2 de llevar los turnos en dos estructuras (S9), D3 de interpretar el texto de la obra social en varios lugares (S6), D4 de que nadie modela qué es una factura válida (S3) y D5 de que el estado de la factura es un `double` que ninguna operación consulta (S3 y S5). Para cambiar cualquier regla hay que leer el método `main` entero y no hay forma de probar una regla sin levantar la consola completa. El costo del cambio crece con cada operación nueva.

## 3. Deuda técnica y code smells

Prioridad: **Alta** si causa un defecto observable o bloquea las pruebas, **Media** si encarece cada cambio, **Baja** si afecta solo la lectura.

| Id | Smell | Ubicación | Evidencia | Impacto | Riesgo de mantenimiento | Prioridad | Se corrige en |
|---|---|---|---|---|---|---|---|
| S1 | God Class | `ConsolaHospital` (todo el archivo) | Interfaz, almacenamiento, reglas de negocio y pasarela en una clase | Viola SRP. Cualquier cambio toca la misma clase | Alto: cada cambio puede romper operaciones no relacionadas | Alta | R1, R2, R6 |
| S2 | Long Method | `ConsolaHospital.main`, líneas 27 a 190 | 164 líneas, un `switch` de 8 ramas con lógica adentro | Ilegible, imposible de probar por partes | Alto | Alta | R1 |
| S3 | Primitive Obsession | Líneas 14, 19, 21, 70 | Paciente como `String[]{nombre, edad, obraSocial}`, factura como `double[]{nro, total, estado}`, edad guardada como texto y convertida en cada uso | El compilador no protege nada. `fa[2] == 2` no dice qué significa | Alto: un índice equivocado pasa la compilación | Alta | R2, R5 |
| S4 | Duplicated Code | Líneas 63 a 68, 89 a 94, 103 a 108 (búsqueda de paciente) y 52 y 119 (cobertura) | El mismo bloque copiado tres veces y la misma condición `OSDE \|\| SWISS` dos veces | Una corrección hay que repetirla en cada copia | Medio | Media | R1, R5 |
| S5 | Magic Numbers / Magic Strings | Líneas 52, 119, 124, 142, 144 y `Facturacion` | `"OSDE"`, `"SWISS"`, `"PUBLICA"`, `50000`, `99`, estados `0/1/2`, `0.5`, `0.9`, `100`, `3` | Las reglas de negocio no tienen nombre | Medio | Media | R4, R5, R6 |
| S6 | Dos fuentes de verdad para la cobertura | `TurnoManager.procesar`, `ConsolaHospital` líneas 52 y 119 | El texto de la obra social se interpreta tres veces con `equals` sensible a mayúsculas | Causa D3. Una cobertura nueva obliga a tocar tres lugares | Alto | Alta | R5 (ADR-0002) |
| S7 | Long Parameter List y Flag Arguments | `Facturacion.calc(List<Double> estudios, boolean p, boolean o, int t)` | Cuatro parámetros, dos booleanos crípticos | Llamadas imposibles de leer. Es fácil invertir `p` y `o` | Medio | Media | R3 |
| S8 | Estado global mutable | Líneas 14 a 25 | Todo es `static` | No se pueden crear dos consolas ni probar en aislamiento | Alto: bloquea las pruebas unitarias | Alta | R2 |
| S9 | Estado duplicado e identidad por nombre | Líneas 73 a 77 frente a la lista interna de `TurnoManager` | Los turnos se guardan en `TurnoManager` y además se cuentan en un `Map` por nombre | Causa D1 y D2 | Alto | Alta | R2, F1 y F2 |
| S10 | Dependencia directa de infraestructura | Líneas 140 a 152, `System.out` en `TurnoManager` | La pasarela simulada está escrita dentro del caso 6. El dominio imprime en consola | Viola DIP. No se puede reemplazar la pasarela ni probar sin consola | Medio | Media | R6 |
| S11 | Manejo de errores genérico | Líneas 186 a 188 | `catch (Exception ex)` imprime `Error` sin causa | El operador no sabe qué hizo mal y los errores reales quedan ocultos | Medio | Media | F6 |
| S12 | Nombres pobres | `tm`, `sc`, `p`, `e`, `os`, `fa`, `nf`, `tt`, `t`, `r`, `calc` | Abreviaturas sin significado de dominio | Lectura lenta | Bajo | Baja | R1 a R5 |

Los 12 smells superan el mínimo de 8 que pide la consigna. La deuda más cara es la de S3, S6, S8 y S9: además de encarecer los cambios, ya produce defectos.

## 4. Estrategia

El orden sigue el ADR-0003. Primero se congela el comportamiento actual con el golden master, incluidos los defectos. Después se refactoriza en pasos chicos (R1 a R6) verificando en cada commit que la salida sea idéntica. Recién al final se corrigen los defectos, cada uno en su propio commit con su Issue, actualizando la salida esperada y explicando el cambio. Las correcciones F1 a F5 resuelven D1 a D5 en el mismo orden, y F6 reemplaza el mensaje genérico `Error` por mensajes que explican la causa (S11).
