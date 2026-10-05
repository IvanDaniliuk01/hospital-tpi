# Definition of Done – hospital-tpi · v1.1

Versión 1.0 (TP Semana 1, criterios C1–C6) + C7 (TP Semana 2). Validación **local**
antes del push; GitHub es repositorio de evidencias y trazabilidad.

| # | Criterio | Umbral | Herramienta / comando |
|---|---|---|---|
| C1 | Compilación limpia | 0 warnings (`-Xlint:all -Werror`) | `mvn compile` |
| C2 | Pruebas en verde | 100 % pasan, 0 `@Disabled` huérfanos | `mvn test` |
| C3 | Cobertura | ≥ 80 % líneas del incremento, ≥ 70 % ramas, sin regresión | `mvn verify` (`jacoco:check`) |
| C4 | Revisión por pares | ≥ 1 aprobación de otra persona, 0 comentarios Altos abiertos | Pull Request |
| C5 | Estilo | 0 violaciones Checkstyle `severity=error` (perfil Google) | `mvn checkstyle:check` |
| C6 | Documentación | Javadoc en API pública y en métodos con complejidad > 5; ADR | `mvn javadoc:javadoc` |
| C7 | Calidad interna de las pruebas | AAA, un Act, sin lógica, dobles solo para infraestructura | Revisión del PR |

Comando único de compuerta local: `mvn -B clean verify checkstyle:check spotless:check javadoc:javadoc`
(C5 y el formato quedan declarados pero sin configurar en el `pom.xml` hasta el bloque 3).
