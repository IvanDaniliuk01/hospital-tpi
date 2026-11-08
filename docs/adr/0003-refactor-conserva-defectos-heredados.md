# El refactor conserva los defectos heredados y se corrigen después en commits `fix`

Hasta el tag `etapa-refactoring`, la versión refactorizada reproduce byte a byte la salida del prototipo base frente al mismo guion (golden master), con los defectos incluidos: turnos contados de más, homónimos que comparten la bonificación, cobertura sensible a mayúsculas, facturas negativas y doble cobro. Cada defecto se corrige después en un commit `fix` propio, ligado a una Issue, que actualiza la salida esperada y explica el cambio. Así quedan separadas la mejora interna (refactor, sin cambio observable) y la mejora externa (corrección, con cambio observable y justificado). El golden master reemplaza al paquete `hospital.legacy` que se usaba como oráculo en los TP anteriores.

## Considered Options

- **Corregir los defectos durante el refactor.** Descartada: mezcla cambios de estructura con cambios de comportamiento y deja al golden master sin poder probar nada.
