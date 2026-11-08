# Code review con revisor IA declarado en la entrega individual

El criterio C4 de la DoD exige la aprobación de al menos un par sin autoaprobación, algo que una entrega individual del TPI no puede cumplir al pie de la letra. Decidimos que cada etapa se integre mediante un PR revisado por un revisor IA declarado, que aplica el checklist de `docs/REVISION_CODIGO.md` al diff y deja observaciones con severidad [Alta], [Media] o [Baja]. El autor responde cada observación, corrige en commits propios y decide el merge. Para entregas individuales, C4 pasa a ser "revisión con checklist y respuesta del autor a cada observación" (DoD v2.1). Si un compañero de cursada acepta revisar, su aprobación tiene prioridad.

## Considered Options

- **Autorrevisión sin segundo revisor.** Descartada: no deja feedback externo ni evidencia de corrección.
- **Dejar C4 incumplido sin explicarlo.** Descartada: contradice la propia DoD frente al corrector.
