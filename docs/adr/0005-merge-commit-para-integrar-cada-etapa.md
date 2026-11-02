# Merge commit (no squash) para integrar cada etapa

En el TP de flujos colaborativos se eligió Squash and Merge para `hospital-tpi`. Para el TPI esa estrategia rompe los tags de etapa: el squash crea un commit nuevo en `main` y los tags `etapa-*`, que apuntan a los commits de la rama, quedan fuera de la historia principal. Además, un único commit por etapa borraría la secuencia R1 a R6 y F1 a F6 que el informe usa como evidencia. Decidimos integrar cada PR de etapa con merge commit, de modo que cada refactor y cada corrección conserven su commit y su mensaje en `main`.

## Consequences

- El grafo de `main` muestra una rama por etapa. Es más ruidoso que la historia lineal del squash, a cambio de trazabilidad completa.
