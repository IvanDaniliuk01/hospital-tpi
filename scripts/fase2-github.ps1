# =============================================================================
#  TP Semana 3 - FASE 2 (GitHub): repo, labels, proteccion de main, Issues,
#  evidencias, Pull Request trazable al Issue
#  hospital-tpi / Metodologia de Sistemas II - UTN - Ivan Daniliuk
#
#  Uso (PowerShell, desde la carpeta TP_Semana3, DESPUES de la fase 1):
#     powershell -ExecutionPolicy Bypass -File .\fase2-github.ps1
#  Opcional: -NombreRepo otro-nombre   -Visibilidad private
# =============================================================================
param(
    [string]$NombreRepo = "hospital-tpi",
    [ValidateSet("public", "private")][string]$Visibilidad = "public"
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [Text.Encoding]::UTF8
$OutputEncoding = [Text.Encoding]::UTF8

$Base    = $PSScriptRoot
$Payload = Join-Path $Base "payload"
$Repo    = Join-Path $Base "hospital-tpi"
$EvLocal = Join-Path $Base "evidencias-semana3"
$EvGh    = Join-Path $EvLocal "github"
$Rama    = "feature/1-compuerta-calidad-precommit"

function Titulo([string]$t) { Write-Host ""; Write-Host "=== $t ===" -ForegroundColor Cyan }
function Detener([string]$m) {
    Write-Host ""; Write-Host "FASE 2 DETENIDA: $m" -ForegroundColor Red
    Write-Host "Avisale a Claude: los logs estan en $EvGh" -ForegroundColor Yellow
    exit 1
}
function Guardar([object[]]$lineas, [string]$archivo) {
    $dir = Split-Path $archivo -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Force $dir | Out-Null }
    [IO.File]::WriteAllLines($archivo, [string[]]($lineas | ForEach-Object { "$_" }), (New-Object Text.UTF8Encoding($false)))
}
function Nativo([string]$exe, [string[]]$argumentos) {
    $prev = $ErrorActionPreference; $ErrorActionPreference = "Continue"
    $salida = & $exe @argumentos 2>&1 | ForEach-Object { $_.ToString() }
    $codigo = $LASTEXITCODE
    $ErrorActionPreference = $prev
    return @{ Salida = @($salida); Codigo = $codigo }
}
function GitRun([string[]]$a, [switch]$PermitirFalla, [switch]$Silencio) {
    $r = Nativo "git" $a
    if (-not $Silencio) { $r.Salida | ForEach-Object { Write-Host "  $_" } }
    if ($r.Codigo -ne 0 -and -not $PermitirFalla) { Detener "git $($a -join ' ') termino con codigo $($r.Codigo)" }
    return $r
}
function GhRun([string[]]$a, [switch]$PermitirFalla) {
    $r = Nativo $script:Gh $a
    if ($r.Codigo -ne 0 -and -not $PermitirFalla) {
        $r.Salida | ForEach-Object { Write-Host "  $_" }
        Detener "gh $($a -join ' ') termino con codigo $($r.Codigo)"
    }
    return $r
}
function UltimoLog([string]$patron) {
    $d = Join-Path $Repo ".git\precommit-logs"
    $f = Get-ChildItem $d -Filter $patron -ErrorAction SilentlyContinue | Sort-Object LastWriteTime | Select-Object -Last 1
    if ($f) { return $f.FullName } else { return $null }
}
function CopiarSiExiste([string]$desde, [string]$hacia) {
    if ($desde -and (Test-Path $desde)) {
        New-Item -ItemType Directory -Force (Split-Path $hacia -Parent) | Out-Null
        Copy-Item -Force $desde $hacia
    }
}
function Reemplazar([string]$texto) {
    $valores = [ordered]@{
        "{{OWNER}}" = $script:Owner; "{{REPO}}" = $NombreRepo; "{{REPO_URL}}" = $script:RepoUrl
        "{{ISSUE1}}" = "$script:Issue1"; "{{ISSUE2}}" = "$script:Issue2"; "{{PR}}" = "$script:Pr"; "{{RAMA}}" = $Rama
    }
    foreach ($k in $valores.Keys) { $texto = $texto.Replace($k, [string]$valores[$k]) }
    return $texto
}
function SoloUrl([object[]]$lineas) {
    $u = $lineas | Where-Object { $_ -match '^https://github\.com/' } | Select-Object -Last 1
    if (-not $u) { Detener "gh no devolvio una URL: $($lineas -join ' | ')" }
    return $u.Trim()
}
function ArchivoReemplazado([string]$origen, [string]$destino) {
    $t = [IO.File]::ReadAllText($origen, [Text.Encoding]::UTF8)
    [IO.File]::WriteAllText($destino, (Reemplazar $t), (New-Object Text.UTF8Encoding($false)))
}

# --- 0. Prerrequisitos -------------------------------------------------------
Titulo "0. Prerrequisitos"
if (-not (Test-Path (Join-Path $Repo ".git"))) { Detener "no existe el repo local: corre primero fase1-local.ps1" }
if (-not (Test-Path (Join-Path $Payload "docs\INFORME_CALIDAD_VERDE.md"))) { Detener "faltan los documentos en payload\docs (los prepara Claude despues de la fase 1)" }
Set-Location $Repo

# Maven y JDK para el pre-push
if (-not $env:JAVA_HOME) {
    $javac = Get-Command javac -ErrorAction SilentlyContinue
    if ($javac) { $env:JAVA_HOME = Split-Path (Split-Path $javac.Source -Parent) -Parent }
}
if ($env:JAVA_HOME) { $env:PATH = (Join-Path $env:JAVA_HOME "bin") + ";" + $env:PATH }
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    $env:PATH = (Join-Path $env:USERPROFILE ".m2\apache-maven-3.9.11\bin") + ";" + $env:PATH
}

# GitHub CLI
$ghCmd = Get-Command gh -ErrorAction SilentlyContinue
if ($ghCmd) { $script:Gh = $ghCmd.Source } else {
    $script:Gh = "C:\Program Files\GitHub CLI\gh.exe"
    if (-not (Test-Path $script:Gh)) {
        Write-Host "  Instalando GitHub CLI con winget..."
        winget install --id GitHub.cli -e --silent --accept-source-agreements --accept-package-agreements
        if (-not (Test-Path $script:Gh)) { Detener "no se pudo instalar GitHub CLI. Instalalo desde https://cli.github.com y volve a correr." }
    }
}
$auth = Nativo $script:Gh @("auth", "status")
if ($auth.Codigo -ne 0) {
    Write-Host "  Inicia sesion en GitHub (se abre el navegador; copia el codigo que aparece aca)" -ForegroundColor Yellow
    & $script:Gh auth login --hostname github.com --git-protocol https --web --scopes "repo,workflow"
    if ($LASTEXITCODE -ne 0) { Detener "no se pudo iniciar sesion en GitHub" }
}
GhRun @("auth", "setup-git") | Out-Null
$script:Owner = ((GhRun @("api", "user", "--jq", ".login")).Salida -join "").Trim()
$script:RepoUrl = "https://github.com/$script:Owner/$NombreRepo"
Write-Host "  Cuenta: $script:Owner  -> repo: $script:RepoUrl"

$existe = Nativo $script:Gh @("repo", "view", "$script:Owner/$NombreRepo", "--json", "name")
if ($existe.Codigo -eq 0) { Detener "ya existe $script:RepoUrl. Borralo o usa -NombreRepo otro-nombre." }

# --- 1. Repo en GitHub y publicacion de main ------------------------------------
Titulo "1. Creacion del repositorio y publicacion de main"
GhRun @("repo", "create", $NombreRepo, "--$Visibilidad", "--source", $Repo, "--remote", "origin",
        "--description", "TPI Metodologia de Sistemas II (UTN): turnos, facturacion y cobros hospitalarios con compuerta de calidad automatizada") | Out-Null
# Unica publicacion directa de main (bootstrap), antes de proteger la rama. --no-verify
# porque nuestro propio pre-push prohibe, a proposito, cualquier push a main.
GitRun @("push", "--no-verify", "-u", "origin", "main") | Out-Null

# --- 2. Etiquetas -------------------------------------------------------------------
Titulo "2. Esquema de 25 etiquetas (Semana 1)"
$yml = Get-Content -Encoding UTF8 (Join-Path $Repo ".github\labels.yml")
$propias = @()
foreach ($l in $yml) {
    if ($l -match 'name:\s*"([^"]+)",\s*color:\s*"([0-9a-fA-F]{6})",\s*description:\s*"([^"]*)"') {
        $propias += $Matches[1]
        GhRun @("label", "create", $Matches[1], "--color", $Matches[2], "--description", $Matches[3], "--force") | Out-Null
    }
}
$actuales = (GhRun @("label", "list", "--limit", "100", "--json", "name", "--jq", ".[].name")).Salida
foreach ($n in $actuales) { if ($n -and ($propias -notcontains $n)) { GhRun @("label", "delete", $n, "--yes") -PermitirFalla | Out-Null } }
Guardar (GhRun @("label", "list", "--limit", "100")).Salida (Join-Path $EvGh "labels.txt")
Write-Host "  $($propias.Count) etiquetas creadas; etiquetas por defecto eliminadas"

# --- 3. Proteccion de main -------------------------------------------------------------
Titulo "3. Proteccion de la rama main"
$prot = Join-Path $env:TEMP "proteccion-main.json"
Guardar @('{',
  '  "required_status_checks": null,',
  '  "enforce_admins": true,',
  '  "required_pull_request_reviews": { "required_approving_review_count": 0, "dismiss_stale_reviews": true },',
  '  "restrictions": null,',
  '  "allow_force_pushes": false,',
  '  "allow_deletions": false,',
  '  "required_conversation_resolution": true',
  '}') $prot
$rp = GhRun @("api", "-X", "PUT", "repos/$script:Owner/$NombreRepo/branches/main/protection", "--input", $prot) -PermitirFalla
$protegida = ($rp.Codigo -eq 0)
Guardar ((GhRun @("api", "repos/$script:Owner/$NombreRepo/branches/main/protection") -PermitirFalla).Salida) (Join-Path $EvGh "proteccion-main.json")
if (-not $protegida) { Write-Host "  AVISO: no se pudo proteger main (repo privado sin plan Pro?). Se omite la prueba de bypass." -ForegroundColor Yellow }

# --- 4. Issues -------------------------------------------------------------------------
Titulo "4. Issues"
$script:Issue1 = "1"; $script:Issue2 = "2"; $script:Pr = "3"
$tmp1 = Join-Path $env:TEMP "issue1.md"; $tmp2 = Join-Path $env:TEMP "issue2.md"
ArchivoReemplazado (Join-Path $Payload "docs\github\issue-1.md") $tmp1
ArchivoReemplazado (Join-Path $Payload "docs\github\issue-2.md") $tmp2
$u1 = SoloUrl (GhRun @("issue", "create", "--title", "[FEATURE] Automatizar la compuerta de calidad local con un pre-commit hook (formatter -> linter -> pruebas)",
        "--body-file", $tmp1, "--assignee", "@me",
        "--label", "tipo/feature", "--label", "prioridad/p1", "--label", "estado/en-curso", "--label", "dod/en-revision")).Salida
$u2 = SoloUrl (GhRun @("issue", "create", "--title", "[TECH-DEBT] Modulo heredado de liquidacion: complejidad, duplicacion y dependencias vulnerables",
        "--body-file", $tmp2, "--assignee", "@me",
        "--label", "tipo/technical-debt", "--label", "prioridad/p0", "--label", "estado/aceptada", "--label", "dod/pendiente")).Salida
$script:Issue1 = ($u1 -split "/")[-1]; $script:Issue2 = ($u2 -split "/")[-1]
$script:Pr = [string]([int]$script:Issue2 + 1)
Write-Host "  Issue #$script:Issue1 -> $u1"
Write-Host "  Issue #$script:Issue2 -> $u2"

# --- 5. Bypass: --no-verify vs pre-push vs proteccion de main ------------------------------
$EvB = Join-Path $EvLocal "precommit\bypass"
if ($protegida) {
    Titulo "5. Demostracion: que pasa si alguien saltea el hook"
    $mainSha = ((GitRun @("rev-parse", "origin/main") -Silencio).Salida -join "").Trim()
    GitRun @("switch", "-q", "-c", "demo/bypass-no-verify") | Out-Null
    $cf = Join-Path $Repo "src\main\java\hospital\facturacion\CalculadoraFacturacion.java"
    $t = [IO.File]::ReadAllText($cf, [Text.Encoding]::UTF8)
    $i = $t.LastIndexOf("}")
    $t = $t.Substring(0, $i) + "`n  public double calcularTotalUrgente(SolicitudFacturacion s) {`n    double total = calcularTotal(s);`n    if (total > 0) return total * 1.15;`n    return total;`n  }`n" + $t.Substring($i)
    [IO.File]::WriteAllText($cf, $t, (New-Object Text.UTF8Encoding($false)))
    GitRun @("add", "src/main/java/hospital/facturacion/CalculadoraFacturacion.java") | Out-Null
    $b1 = GitRun @("commit", "--no-verify", "-m", "demo(bypass): commit que saltea el pre-commit con --no-verify") -PermitirFalla
    Guardar (@("> git commit --no-verify -m 'demo(bypass)...'", "") + $b1.Salida + @("", "> codigo: $($b1.Codigo)")) (Join-Path $EvB "1-commit-no-verify.log")
    $b2 = GitRun @("push", "origin", "demo/bypass-no-verify:main") -PermitirFalla
    Guardar (@("> git push origin demo/bypass-no-verify:main", "") + $b2.Salida + @("", "> codigo: $($b2.Codigo)")) (Join-Path $EvB "2-push-a-main-bloqueado-por-pre-push.log")
    $b3 = GitRun @("push", "--no-verify", "origin", "demo/bypass-no-verify:main") -PermitirFalla
    Guardar (@("> git push --no-verify origin demo/bypass-no-verify:main", "") + $b3.Salida + @("", "> codigo: $($b3.Codigo)")) (Join-Path $EvB "3-push-no-verify-bloqueado-por-github.log")
    if ($b3.Codigo -eq 0) {
        Write-Host "  ATENCION: GitHub acepto el push; se restaura main" -ForegroundColor Red
        GitRun @("push", "--no-verify", "--force", "origin", "$($mainSha):refs/heads/main") -PermitirFalla | Out-Null
    }
    GitRun @("switch", "-q", $Rama) | Out-Null
    GitRun @("branch", "-D", "demo/bypass-no-verify") | Out-Null
}

# --- 6. Documentos y evidencias en la rama -------------------------------------------------
Titulo "6. Documentos y evidencias (commit con el hook activo)"
New-Item -ItemType Directory -Force (Join-Path $Repo "docs") | Out-Null
foreach ($d in @("AUDITORIA_ESTATICA.md", "INFORME_CALIDAD_VERDE.md")) {
    ArchivoReemplazado (Join-Path $Payload "docs\$d") (Join-Path $Repo "docs\$d")
}
$EvRepo = Join-Path $Repo "evidencias\semana3"
New-Item -ItemType Directory -Force $EvRepo | Out-Null
Copy-Item -Recurse -Force (Join-Path $EvLocal "*") $EvRepo
GitRun @("add", "-A") | Out-Null
$c = GitRun @("commit", "-m", "docs(calidad): auditoria estatica, Informe de Calidad en Verde y evidencias (#$script:Issue1, refs #$script:Issue2)") -PermitirFalla
Guardar $c.Salida (Join-Path $EvLocal "precommit\03-commit-evidencias.consola.log")
CopiarSiExiste (UltimoLog "precommit-*.log") (Join-Path $EvLocal "precommit\03-commit-evidencias.hook.log")
if ($c.Codigo -ne 0) { Detener "el commit de evidencias fue rechazado por el hook" }

# --- 7. Push de la rama (pre-push: mvn verify) --------------------------------------------
Titulo "7. Push de la rama (el pre-push ejecuta mvn verify)"
$p = GitRun @("push", "-u", "origin", $Rama) -PermitirFalla
Guardar $p.Salida (Join-Path $EvLocal "precommit\04-push-rama.consola.log")
CopiarSiExiste (UltimoLog "prepush-*.log") (Join-Path $EvLocal "precommit\04-push-rama.prepush.log")
if ($p.Codigo -ne 0) { Detener "el push de la rama fallo" }

# --- 8. Pull Request -----------------------------------------------------------------------
Titulo "8. Pull Request"
$tmpPr = Join-Path $env:TEMP "pr.md"
ArchivoReemplazado (Join-Path $Payload "docs\github\pr.md") $tmpPr
$prUrl = SoloUrl (GhRun @("pr", "create", "--base", "main", "--head", $Rama,
    "--title", "Compuerta de calidad automatizada: pre-commit (formatter -> linter -> pruebas), auditoria estatica e Informe en Verde",
    "--body-file", $tmpPr, "--assignee", "@me",
    "--label", "tipo/feature", "--label", "dod/en-revision", "--label", "prioridad/p1")).Salida
$script:Pr = ($prUrl -split "/")[-1]
Write-Host "  PR #$script:Pr -> $prUrl"
$tmpCom = Join-Path $env:TEMP "comentario.md"
ArchivoReemplazado (Join-Path $Payload "docs\github\pr-comentario.md") $tmpCom
GhRun @("pr", "comment", $script:Pr, "--body-file", $tmpCom) | Out-Null
GhRun @("issue", "comment", $script:Issue2, "--body", "Diagnostico y estrategia de refactorizacion entregados en #$script:Pr (docs/AUDITORIA_ESTATICA.md). El Issue queda abierto: el pago de la deuda se hace en PRs posteriores, uno por fase de la estrategia.") | Out-Null

# --- 9. Exportar la trazabilidad y cerrar ---------------------------------------------------
Titulo "9. Trazabilidad exportada desde GitHub"
Guardar (GhRun @("pr", "view", $script:Pr, "--json", "number,url,title,state,headRefName,baseRefName,closingIssuesReferences,commits,labels,createdAt")).Salida (Join-Path $EvGh "pr.json")
Guardar (GhRun @("issue", "view", $script:Issue1, "--json", "number,url,title,state,labels,createdAt")).Salida (Join-Path $EvGh "issue-1.json")
Guardar (GhRun @("issue", "view", $script:Issue2, "--json", "number,url,title,state,labels,createdAt")).Salida (Join-Path $EvGh "issue-2.json")
$tl = GhRun @("api", "repos/$script:Owner/$NombreRepo/issues/$script:Issue1/timeline", "-H", "Accept: application/vnd.github+json") -PermitirFalla
Guardar $tl.Salida (Join-Path $EvGh "issue-1-timeline.json")
Guardar (GitRun @("log", "--all", "--graph", "--format=%h %ad %an %s", "--date=format:%Y-%m-%d %H:%M") -Silencio).Salida (Join-Path $EvGh "git-log.txt")
Guardar @("owner=$script:Owner", "repo=$script:RepoUrl", "issue1=$u1", "issue2=$u2", "pr=$prUrl", "rama=$Rama", "main_protegida=$protegida", "fecha=$(Get-Date -Format s)") (Join-Path $EvGh "URLS.txt")

# Commit final con la trazabilidad exportada (vuelve a pasar pre-commit y pre-push)
Copy-Item -Recurse -Force (Join-Path $EvLocal "*") $EvRepo
GitRun @("add", "-A") | Out-Null
$c2 = GitRun @("commit", "-m", "chore(evidencias): trazabilidad Issue-PR exportada de GitHub y logs de la fase 2 (#$script:Issue1)") -PermitirFalla
if ($c2.Codigo -eq 0) { GitRun @("push") -PermitirFalla | Out-Null }

Write-Host ""
Write-Host "FASE 2 COMPLETA" -ForegroundColor Green
Write-Host "  Repo : $script:RepoUrl"
Write-Host "  Issue: $u1"
Write-Host "  Issue: $u2"
Write-Host "  PR   : $prUrl"
Write-Host "Avisale a Claude para armar el PDF final."
