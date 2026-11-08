# Historial reconstruido para el TPI sobre un `main` nuevo

El TPI exige los tags `estado-inicial`, `etapa-*` y `estado-refactorizado` para mostrar cómo evolucionó el sistema, pero el historial original de `hospital-tpi` arranca (`a330cb7`, v0.2.0) con el código ya refactorizado. Decidimos renombrar ese `main` a `archivo/unidad-3`, que conserva las Issues #1 y #2, el PR #3 y las evidencias de las Unidades 3 y 4, y crear un `main` nuevo cuyo primer commit contiene solo el prototipo base con sus problemas. Los refactors se rehacen encima, en commits ordenados por etapa. La reconstrucción se declara en el informe.

## Considered Options

- **Etiquetar `a330cb7` como `estado-inicial`.** Descartada: el tag apuntaría a código ya refactorizado y no mostraría lo que dice mostrar.
- **Repositorio nuevo.** Descartada: se pierde el vínculo con las Issues, el PR y las evidencias de las semanas anteriores.

## Consequences

- El orden de los commits del `main` nuevo es pedagógico. La cronología real del desarrollo queda en `archivo/unidad-3`.
- Los artefactos de gobernanza que se traen del archivo (hooks, DoD, configuración de Checkstyle y PMD, plantillas de GitHub) entran como commits propios de la etapa que corresponde.
