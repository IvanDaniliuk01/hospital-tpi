# Definition of Done – hospital-tpi · v2.0

**Versión 2.0 (TP Semana 3).** Historia: v1.0 (Semana 1, criterios C1–C6) → v1.1
(Semana 2, se agrega C7) → **v2.0 (Semana 3): los criterios verificables por
máquina pasan a ejecutarse automáticamente en el pre-commit hook y se agrega C8
(seguridad de dependencias).**

Regla de evidencia: ninguna afirmación subjetiva vale. Cada criterio declara
umbral, herramienta, evidencia y condición de veto. La DoD aplica al
**incremento**; el código heredado que la incumple se inventaría como
`tipo/technical-debt` (paquete `hospital.legacy` y módulo `auditoria-legacy`).

| # | Criterio | Umbral | Herramienta / comando | Cuándo se verifica | Veto |
|---|---|---|---|---|---|
| C1 | Compilación limpia | 0 warnings (`-Xlint:all -Werror`) | `mvn compile` | **pre-commit 2/3** | `dod/bloqueado-c1` |
| C2 | Pruebas en verde | 100 % pasan, 0 `@Disabled` sin issue | `mvn test` (Surefire) | **pre-commit 3/3** | `dod/bloqueado-c2` |
| C3 | Cobertura | ≥ 80 % líneas y ≥ 70 % ramas (bundle); 100 % ramas en código nuevo con < 10 decisiones | `mvn verify` (`jacoco:check`) | **pre-push** | `dod/bloqueado-c3` |
| C4 | Revisión por pares | ≥ 1 aprobación de otra persona, 0 comentarios de severidad Alta abiertos | Pull Request en GitHub | PR (manual) | `dod/bloqueado-c4` |
| C5 | Estilo y análisis estático | Formato google-java-format; 0 violaciones Checkstyle (perfil DoD); 0 PMD prioridad 1–3; 0 duplicados ≥ 50 tokens; complejidad ciclomática ≤ 10 por método | `spotless:check`, `checkstyle:check`, `pmd:check`, `pmd:cpd-check` | **pre-commit 1/3 y 2/3** | `dod/bloqueado-c5` |
| C6 | Documentación | Javadoc en API pública nueva; ADR para decisiones de diseño | `mvn javadoc:javadoc` + revisión | PR (manual) | `dod/bloqueado-c6` |
| C7 | Calidad interna de las pruebas | AAA, un Act por prueba, sin lógica en las pruebas, dobles solo para infraestructura, Mock siempre con `verify` | Revisión del PR (checklist) | PR (manual) | `dod/bloqueado-c7` |
| C8 | **Seguridad de dependencias** (nuevo) | 0 vulnerabilidades conocidas de severidad Alta o Crítica en dependencias directas o transitivas | OSV-Scanner sobre `pom.xml` + `versions:display-dependency-updates` | Informe de Calidad en Verde / PR | `dod/bloqueado-c8`* |

\* Etiqueta propuesta para la próxima revisión del esquema de labels.

## Líneas de defensa de la rama `main`

1. **pre-commit** (local, cada commit): C1, C2 y C5. Falla → el commit no existe.
2. **pre-push** (local, cada push): rechaza push directo a `main` y corre `mvn verify` (C1, C2, C3, C5).
3. **Protección de rama en GitHub** (servidor): `main` solo cambia por Pull Request; sin force-push ni borrado; aplica también a administradores. Es la única barrera que no se puede saltear con `--no-verify`.
4. **Pull Request** (personas): C4, C6, C7 y verificación de las evidencias (C3, C8).

## Excepciones

Toda excepción se registra en el Issue con etiqueta `dod/excepcion-aprobada`,
justificación, responsable y **fecha límite obligatoria**. `git commit --no-verify`
solo se admite para commits de trabajo en ramas propias y queda cubierto por el
pre-push y por la protección de `main`.
