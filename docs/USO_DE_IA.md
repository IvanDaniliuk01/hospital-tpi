# Uso de inteligencia artificial

Declaración exigida por la sección 7 de la consigna del TPI.

## Herramienta

Claude (modelo Claude Opus 5.5, de Anthropic), usado desde la app de Claude en un Project llamado "Metodologia de sistemas 2", que guarda las notas de los TP anteriores de la materia. La sesión de trabajo del TPI empezó el 1 de noviembre de 2026.

## Finalidad y grado de intervención

| Actividad | Qué hizo la IA | Qué hizo el autor | Grado de intervención de la IA |
|---|---|---|---|
| Planificación y decisiones de diseño | Entrevista de diseño en 5 rondas (preguntas Q1 a Q24, cada una con alternativas y una recomendación). Detectó contradicciones entre el código y el dominio (cobertura, identidad del paciente, alcance de la factura) | Respondió cada ronda y aceptó las recomendaciones | Alto en la propuesta, decisión final del autor |
| Glosario y ADR | Redactó `CONTEXT.md` y los ADR 0001 a 0005 a medida que se tomaban las decisiones | Validó las decisiones registradas | Alto |
| Prototipo base | Escribió `ConsolaHospital` sobre las clases heredadas de TP1 y TP3, con problemas de diseño reales y el guion del golden master | Definió el alcance (7 operaciones, datos en memoria) | Alto |
| Diagnóstico | Identificó 12 smells y 5 defectos con ubicación y evidencia, y corrió Checkstyle y PMD sobre el estado inicial | Revisó el diagnóstico | Alto |
| Refactorings, pruebas y correcciones | Aplicó R1 a R6, escribió las 41 pruebas y las correcciones F1 a F6, verificando el golden master en cada commit | Aprobó el plan de refactorings y la estrategia de conservar los defectos hasta el final | Alto |
| Code review | Actuó como revisor declarado (ADR-0004): aplicó el checklist y escribió las observaciones O1 a O11 | Respondió cada observación y decidió cuáles se corrigen y cuáles quedan como deuda | Alto en la revisión, decisión del autor |
| Redacción | Redactó la documentación y la revisó con una pasada para quitar muletillas de texto generado | Pidió esa pasada de estilo | Alto |

Los commits en los que intervino la IA llevan el trailer `Co-Authored-By: Claude`. El autor comprende el contenido entregado y lo defiende con voz propia en el video. No se usaron voces generadas por IA.

## Registro de prompts

Prompts del autor, en orden. Las rondas de preguntas completas, con las alternativas y las recomendaciones, están en la exportación de la conversación incluida en el anexo ZIP.

| # | Fecha y hora | Prompt del autor (resumido) | Resultado |
|---|---|---|---|
| 1 | 01/11/2026 22:45 | Adjunta la consigna V2 del TPI (PDF y texto del campus) y pide una entrevista de diseño que deje documentación (`/grill-with-docs`) | Ronda 1: sistema base, entrega individual, fecha de entrega y registro de uso de IA |
| 2 | 01/11/2026 22:46 | "vamos con todas tus recomendaciones" | Se adopta `hospital-tpi` con una consola mínima, entrega individual y registro de IA en tres niveles. Ronda 2: historial de commits, prototipo base, forma de ingresar datos a la consola |
| 3 | 01/11/2026 22:51 | "vamos con tus recomendaciones" | Historial reconstruido (ADR-0001), prototipo God Class y menú por stdin con golden master. Ronda 3: qué se trae del archivo, alcance de la factura, cobertura, identidad del paciente y pasarela simulada |
| 4 | 01/11/2026 22:56 | "vamos con todas tus recomendaciones" | Factura por estudios pendientes, Cobertura (ADR-0002), número correlativo y pasarela determinista. Ronda 4: categoría de paciente, carga de estudios, estado de la factura, reporte y defectos heredados |
| 5 | 01/11/2026 22:59 | "vamos con tus recomendaciones" | Se elimina Categoría de paciente, se agrega Estudio pendiente y Estado de la factura, y los defectos se conservan hasta después del refactor (ADR-0003). Ronda 5: bonificación, factura vacía, code review individual y lista de refactorings |
| 6 | 01/11/2026 23:02 | "vamos con tus recomendaciones" | Bonificación en cada factura, rechazo de facturas vacías, revisor IA declarado (ADR-0004) y los refactorings R1 a R6 |
| 7 | 01/11/2026 23:06 | "si dale avanza", con la pasada de estilo `/quitar-muletillas-ia` | Construcción del repositorio etapa por etapa, con los tags de la consigna |

## TP anteriores

Los TP de la materia que alimentan este trabajo (TP1 a TP3, prácticas GoF y TP de las Unidades 3 y 4) también se hicieron con asistencia de Claude, declarada en cada entrega.
