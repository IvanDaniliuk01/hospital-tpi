# Definition of Done · hospital-tpi · v2.1

Historia: v1.0 (TP Semana 1, criterios C1 a C6), v1.1 (TP Semana 2, se agrega C7), v2.0 (TP Semana 3, los criterios verificables por máquina corren en los hooks y se agrega C8) y **v2.1 (TPI): C4 se adapta a la entrega individual según ADR-0004.**

Regla de evidencia: ninguna afirmación subjetiva vale. Cada criterio declara umbral, herramienta, momento de verificación y etiqueta de veto. La DoD aplica al incremento.

| # | Criterio | Umbral | Herramienta o comando | Cuándo se verifica | Veto |
|---|---|---|---|---|---|
| C1 | Compilación limpia | 0 warnings (`-Xlint:all -Werror`) | `mvn compile` | pre-commit 2/3 | `dod/bloqueado-c1` |
| C2 | Pruebas en verde | 100 % pasan, incluido el golden master. 0 `@Disabled` sin Issue | `mvn test` | pre-commit 3/3 | `dod/bloqueado-c2` |
| C3 | Cobertura | 80 % de líneas y 70 % de ramas como mínimo | `mvn verify` (`jacoco:check`) | pre-push | `dod/bloqueado-c3` |
| C4 | Revisión | Con equipo: al menos 1 aprobación de otra persona. En entrega individual: checklist de `docs/REVISION_CODIGO.md` aplicado por el revisor IA declarado y respuesta del autor a cada observación. En los dos casos, 0 observaciones [Alta] o [Media] abiertas | Pull Request en GitHub | PR | `dod/bloqueado-c4` |
| C5 | Estilo y análisis estático | google-java-format, 0 violaciones de Checkstyle (perfil DoD), 0 de PMD con prioridad 1 a 3, 0 duplicados de 50 tokens o más, complejidad ciclomática de 10 o menos por método | `spotless:check`, `checkstyle:check`, `pmd:check`, `pmd:cpd-check` | pre-commit 1/3 y 2/3 | `dod/bloqueado-c5` |
| C6 | Documentación | Javadoc en la API pública nueva, ADR para decisiones difíciles de revertir y glosario (`CONTEXT.md`) al día | Revisión del PR | PR | `dod/bloqueado-c6` |
| C7 | Calidad interna de las pruebas | Estructura AAA, un Act por prueba, sin lógica en las pruebas y dobles solo para infraestructura | Revisión del PR | PR | `dod/bloqueado-c7` |
| C8 | Seguridad de dependencias | 0 vulnerabilidades conocidas de severidad Alta o Crítica | OSV-Scanner sobre `pom.xml` | PR | `dod/bloqueado-c8` |

## Líneas de defensa de la rama `main`

1. **pre-commit** (local, en cada commit): C1, C2 y C5. Si falla, el commit no se crea.
2. **pre-push** (local, en cada push): rechaza el push directo a `main` y corre `mvn clean verify` (C1, C2, C3 y C5).
3. **Protección de rama en GitHub:** `main` solo cambia por Pull Request, sin force-push ni borrado, también para administradores. Es la única barrera que no se puede saltear con `--no-verify`.
4. **Pull Request:** C4, C6, C7 y C8.

## Excepciones

Toda excepción se registra en una Issue con la etiqueta `dod/excepcion-aprobada`, con justificación, responsable y fecha límite obligatoria. `git commit --no-verify` solo se admite en commits de trabajo en ramas propias, y queda cubierto por el pre-push y por la protección de `main`.
