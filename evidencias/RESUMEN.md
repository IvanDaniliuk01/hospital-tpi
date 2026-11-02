# Resumen de evidencias

Los reportes se generaron con las mismas reglas que usa `mvn verify` (`config/checkstyle/checkstyle-dod.xml` y `config/pmd/ruleset-dod.xml`), corriendo Checkstyle 10.21.4, PMD 7.7.0 y JaCoCo 0.8.12 por línea de comandos. En la PC del autor, `mvn clean verify` regenera los mismos controles.

## Análisis estático: estado inicial contra estado final

| Herramienta | `estado-inicial` | Final | Archivos |
|---|---|---|---|
| Checkstyle | 29 violaciones | 0 | `analisis-estatico/*-checkstyle.txt` |
| PMD | 58 violaciones | 0 | `analisis-estatico/*-pmd.txt` |
| CPD (bloques duplicados de 50 tokens o más) | 2 | 0 | `analisis-estatico/*-cpd.txt` |

En el estado inicial, PMD mide sobre `ConsolaHospital.main` una complejidad ciclomática de 33, una complejidad cognitiva de 65 (umbral 16) y 135 sentencias (NCSS). Esos tres valores respaldan los smells S1 y S2 del diagnóstico. Las 42 violaciones `SystemPrintln` corresponden a S10, las 11 `LiteralsFirstInComparisons` a S6 y los 8 `MagicNumber` a S5. CPD encuentra la búsqueda de paciente copiada (S4).

## Pruebas y cobertura

41 pruebas, 0 fallas. Cobertura de líneas 96,8 % (331 de 342) y de ramas 92,9 % (91 de 98), por encima de los umbrales de la DoD (80 % y 70 %). Detalle por clase en `cobertura/jacoco.csv`. Lo que no se cubre es `main()` y la rama de error inesperado de la consola.

## Golden master

`pruebas/golden/verificar.sh` (o `verificar.ps1` en Windows) compila el código, ejecuta el guion y compara la salida con `diff`. El historial de `pruebas/golden/salida_esperada.txt` muestra que la salida no cambió entre `estado-inicial` y `etapa-refactoring`. Después cambia solo en los commits `fix(F1)` a `fix(F6)`, y cada uno explica en su mensaje qué líneas cambiaron y por qué.

```
git log --oneline -- pruebas/golden/salida_esperada.txt
```
