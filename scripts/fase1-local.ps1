# =============================================================================
#  TP Semana 3 - FASE 1 (local): repositorio, tooling, pre-commit y auditoria
#  hospital-tpi / Metodologia de Sistemas II - UTN - Ivan Daniliuk
#
#  Uso (PowerShell, desde la carpeta TP_Semana3):
#     powershell -ExecutionPolicy Bypass -File .\fase1-local.ps1
#
#  Que hace (no toca GitHub ni la carpeta de la Semana 2):
#   1. Crea el repo Git local .\hospital-tpi a partir del estado entregado en
#      la Semana 2 (commit 1) y la gobernanza de la Semana 1 (commit 2).
#   2. En la rama feature/1-compuerta-calidad-precommit agrega el tooling
#      (Spotless, Checkstyle, PMD/CPD), aplica el formatter, instala los hooks.
#   3. Ejecuta los escenarios de falla del pre-commit (formatter, linter,
#      pruebas) y guarda los logs.
#   4. Corre la auditoria estatica del modulo heredado (PMD, CPD, Checkstyle,
#      versions, dependency:tree, OSV-Scanner) y la compuerta completa.
#  Todo queda en .\hospital-tpi\evidencias\semana3\
# =============================================================================
param([switch]$Reiniciar)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [Text.Encoding]::UTF8
$OutputEncoding = [Text.Encoding]::UTF8

$Base      = $PSScriptRoot
$Payload   = Join-Path $Base "payload"
$Semana2   = Join-Path (Split-Path $Base -Parent) "hospital-tpi"
$Repo      = Join-Path $Base "hospital-tpi"
$Ev        = Join-Path $Base "evidencias-semana3"   # fuera del repo; la fase 2 las copia y commitea
$Rama      = "feature/1-compuerta-calidad-precommit"

function Titulo([string]$t) { Write-Host ""; Write-Host "=== $t ===" -ForegroundColor Cyan }
function Detener([string]$m) {
    Write-Host ""; Write-Host "FASE 1 DETENIDA: $m" -ForegroundColor Red
    Write-Host "Avisale a Claude: los logs estan en $Ev" -ForegroundColor Yellow
    exit 1
}
function Guardar([object[]]$lineas, [string]$archivo) {
    $dir = Split-Path $archivo -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Force $dir | Out-Null }
    [IO.File]::WriteAllLines($archivo, [string[]]($lineas | ForEach-Object { "$_" }), (New-Object Text.UTF8Encoding($false)))
}
# Ejecuta un comando nativo sin que PowerShell aborte por lineas en stderr.
function Nativo([string]$exe, [string[]]$argumentos) {
    $prev = $ErrorActionPreference; $ErrorActionPreference = "Continue"
    $salida = & $exe @argumentos 2>&1 | ForEach-Object { $_.ToString() }
    $codigo = $LASTEXITCODE
    $ErrorActionPreference = $prev
    return @{ Salida = @($salida); Codigo = $codigo }
}
function GitRun([string[]]$a, [switch]$PermitirFalla) {
    $r = Nativo "git" $a
    $r.Salida | ForEach-Object { Write-Host "  $_" }
    if ($r.Codigo -ne 0 -and -not $PermitirFalla) { Detener "git $($a -join ' ') termino con codigo $($r.Codigo)" }
    return $r
}
function Maven([string[]]$a, [string]$log, [switch]$PermitirFalla, [string]$dir = $Repo) {
    Push-Location $dir
    try { $r = Nativo $script:MvnCmd (@("-B", "-ntp") + $a) } finally { Pop-Location }
    Guardar (@("> mvn -B -ntp $($a -join ' ')", "> directorio: $dir", "> $(Get-Date -Format s)", "") + $r.Salida + @("", "> codigo de salida: $($r.Codigo)")) $log
    $r.Salida | Select-Object -Last 25 | ForEach-Object { Write-Host "  $_" }
    if ($r.Codigo -ne 0 -and -not $PermitirFalla) { Detener "mvn $($a -join ' ') fallo (ver $log)" }
    return $r
}
function CopiarDir([string]$desde, [string]$hacia) {
    if (Test-Path $desde) {
        New-Item -ItemType Directory -Force $hacia | Out-Null
        Copy-Item -Recurse -Force (Join-Path $desde "*") $hacia
    }
}
function CopiarSiExiste([string]$desde, [string]$hacia) {
    if ($desde -and (Test-Path $desde)) {
        New-Item -ItemType Directory -Force (Split-Path $hacia -Parent) | Out-Null
        Copy-Item -Force $desde $hacia
    }
}
function UltimoLogHook() {
    $d = Join-Path $Repo ".git\precommit-logs"
    if (-not (Test-Path $d)) { return $null }
    $f = Get-ChildItem $d -Filter "precommit-*.log" | Sort-Object LastWriteTime | Select-Object -Last 1
    if ($f) { return $f.FullName } else { return $null }
}
# Los reportes HTML de PMD/Checkstyle quedan en target\reports (maven-reporting 4) o target\site
function CopiarReporteHtml([string]$proyecto, [string]$nombre, [string]$hacia) {
    foreach ($sub in @("target\reports", "target\site")) {
        $p = Join-Path $proyecto "$sub\$nombre"
        if (Test-Path $p) { CopiarSiExiste $p $hacia; return }
    }
}

# Aplica el cambio de un escenario de falla sobre el archivo YA formateado por Spotless.
# (Los .txt de scripts\escenarios-precommit muestran el archivo resultante.)
function AplicarEscenario([string]$id, [string]$ruta) {
    $t = [IO.File]::ReadAllText($ruta, [Text.Encoding]::UTF8)
    $orig = $t
    switch ($id) {
        "A-formatter" {
            $t = [regex]::Replace($t, 'public boolean esValido\(\) \{\s*return tieneNombre\(\) && tieneEdadValida\(\);\s*\}',
                 'public boolean esValido(){ return tieneNombre()&&tieneEdadValida(); }')
        }
        "B-linter" {
            $metodo = "`n  /** Total con recargo por atencion de urgencia (agregado sin respetar la DoD). */`n" +
                      "  public double calcularTotalUrgente(SolicitudFacturacion solicitud) {`n" +
                      "    double total = calcularTotal(solicitud);`n" +
                      "    if (total > 0) return total * 1.15;`n" +
                      "    return total;`n" +
                      "  }`n"
            $i = $t.LastIndexOf("}")
            $t = $t.Substring(0, $i) + $metodo.Substring(1) + $t.Substring($i)
            $t = $t.Replace("  }`n  /** Total con recargo", "  }`n`n  /** Total con recargo")
        }
        "C-pruebas" {
            $t = $t.Replace("TURNOS_MINIMOS_PARA_BONIFICACION = 3;", "TURNOS_MINIMOS_PARA_BONIFICACION = 2;")
        }
    }
    if ($t -ceq $orig) { Detener "no se pudo aplicar el escenario $id sobre $ruta" }
    [IO.File]::WriteAllText($ruta, $t, (New-Object Text.UTF8Encoding($false)))
}

# --- 0. Prerrequisitos -------------------------------------------------------
Titulo "0. Prerrequisitos"
if (-not (Test-Path $Payload)) { Detener "no encuentro la carpeta payload junto al script" }
if (-not (Test-Path (Join-Path $Semana2 "pom.xml"))) { Detener "no encuentro el proyecto de la Semana 2 en $Semana2" }

$git = Get-Command git -ErrorAction SilentlyContinue
if (-not $git) { Detener "Git no esta instalado. Instalalo con: winget install --id Git.Git -e  (y abri una consola nueva)" }

$javac = Get-Command javac -ErrorAction SilentlyContinue
if (-not $javac -and $env:JAVA_HOME) { $javac = Get-Command (Join-Path $env:JAVA_HOME "bin\javac.exe") -ErrorAction SilentlyContinue }
if (-not $javac) { Detener "no encuentro un JDK (javac). Instala Temurin 17+ desde https://adoptium.net" }
if (-not $env:JAVA_HOME) { $env:JAVA_HOME = Split-Path (Split-Path $javac.Source -Parent) -Parent }
$env:PATH = (Join-Path $env:JAVA_HOME "bin") + ";" + $env:PATH

$mvn = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvn) {
    $script:MvnCmd = $mvn.Source
} else {
    $mavenHome = Join-Path $env:USERPROFILE ".m2\apache-maven-3.9.11"
    if (-not (Test-Path (Join-Path $mavenHome "bin\mvn.cmd"))) {
        $zip = Join-Path $env:TEMP "apache-maven-3.9.11-bin.zip"
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Write-Host "Descargando Maven 3.9.11..."
        Invoke-WebRequest -UseBasicParsing -OutFile $zip "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.zip"
        Expand-Archive -Force $zip (Join-Path $env:USERPROFILE ".m2")
        Remove-Item $zip
    }
    $script:MvnCmd = Join-Path $mavenHome "bin\mvn.cmd"
    # Los hooks (bash de Git for Windows) encuentran 'mvn' por el PATH heredado
    $env:PATH = (Join-Path $mavenHome "bin") + ";" + $env:PATH
}

$versiones = @()
$versiones += (Nativo "git" @("--version")).Salida
$versiones += (Nativo "java" @("-version")).Salida
$versiones += (Nativo $script:MvnCmd @("-v")).Salida
$versiones | ForEach-Object { Write-Host "  $_" }

$nombre = (Nativo "git" @("config", "--global", "user.name")).Salida -join ""
$correo = (Nativo "git" @("config", "--global", "user.email")).Salida -join ""
if (-not $nombre) { $nombre = Read-Host "Nombre para los commits (ej. Ivan Daniliuk)" }
if (-not $correo) { $correo = Read-Host "Email para los commits (el mismo de tu cuenta de GitHub)" }

# --- 1. Repositorio y commits base en main -----------------------------------
Titulo "1. Repositorio local y commits base (main)"
if (Test-Path $Repo) {
    if (-not $Reiniciar) { Detener "ya existe $Repo. Volve a correr con -Reiniciar para moverlo a un backup y empezar de nuevo." }
    $bak = "$Repo.bak-$(Get-Date -Format yyyyMMdd-HHmmss)"
    Rename-Item $Repo $bak
    Write-Host "  repo anterior movido a $bak"
}
if (Test-Path $Ev) { Rename-Item $Ev "$Ev.bak-$(Get-Date -Format yyyyMMdd-HHmmss)" }
New-Item -ItemType Directory -Force $Repo | Out-Null
Set-Location $Repo
GitRun @("init", "-b", "main") | Out-Null
GitRun @("config", "user.name", $nombre) | Out-Null
GitRun @("config", "user.email", $correo) | Out-Null
GitRun @("config", "core.autocrlf", "false") | Out-Null

# Commit 1: estado entregado en la Semana 2
foreach ($f in @("pom.xml", "README.md", "generar_evidencias.ps1", "generar_evidencias.sh")) {
    CopiarSiExiste (Join-Path $Semana2 $f) (Join-Path $Repo $f)
}
CopiarDir (Join-Path $Semana2 "src") (Join-Path $Repo "src")
CopiarDir (Join-Path $Semana2 "evidencias") (Join-Path $Repo "evidencias\semana2")
Copy-Item (Join-Path $Payload ".gitignore"), (Join-Path $Payload ".gitattributes") $Repo
GitRun @("add", "-A") | Out-Null
GitRun @("commit", "-q", "-m", "chore: estado inicial v0.2.0 (TP Semana 2 - testing practico y contratos)") | Out-Null

# Commit 2: gobernanza de la Semana 1
CopiarDir (Join-Path $Payload ".github") (Join-Path $Repo ".github")
Copy-Item -Force (Join-Path $Payload "scripts\base\PULL_REQUEST_TEMPLATE_v1.1.md") (Join-Path $Repo ".github\PULL_REQUEST_TEMPLATE.md")
Copy-Item -Force (Join-Path $Payload "scripts\base\DEFINITION_OF_DONE_v1.1.md") (Join-Path $Repo "DEFINITION_OF_DONE.md")
GitRun @("add", "-A") | Out-Null
GitRun @("commit", "-q", "-m", "docs(gobernanza): DoD v1.1, plantillas de Issue/PR y esquema de 25 etiquetas (TP Semana 1)") | Out-Null

# --- 2. Rama de trabajo: tooling y formatter ---------------------------------
Titulo "2. Rama $Rama : tooling de calidad"
GitRun @("switch", "-q", "-c", $Rama) | Out-Null
Copy-Item -Force (Join-Path $Payload "pom.xml") $Repo
CopiarDir (Join-Path $Payload "config") (Join-Path $Repo "config")
CopiarDir (Join-Path $Payload ".mvn") (Join-Path $Repo ".mvn")

Write-Host "  Linea base: spotless:check sobre el codigo sin formatear (se espera FALLA)"
$lineaBase = Maven @("spotless:check") (Join-Path $Ev "formatter\00-spotless-check-linea-base.log") -PermitirFalla
GitRun @("add", "-A") | Out-Null
GitRun @("commit", "-q", "-m", "build(calidad): Spotless, Checkstyle, PMD/CPD y versions-maven-plugin en el pom (#1)") | Out-Null

Write-Host "  Aplicando el formatter (spotless:apply)"
Maven @("spotless:apply") (Join-Path $Ev "formatter\01-spotless-apply.log") | Out-Null
$diffStat = (Nativo "git" @("diff", "--stat")).Salida
Guardar $diffStat (Join-Path $Ev "formatter\02-git-diff-stat-formato.txt")
Maven @("spotless:check") (Join-Path $Ev "formatter\03-spotless-check-despues.log") | Out-Null
GitRun @("add", "-A") | Out-Null
GitRun @("commit", "-q", "-m", "style: aplicar google-java-format con Spotless a main y test (#1)") | Out-Null

Write-Host "  Linea base de linters (debe pasar antes de activar el hook)"
Maven @("compile", "checkstyle:check", "pmd:check", "pmd:cpd-check") (Join-Path $Ev "linter\00-linters-linea-base.log") | Out-Null

# --- 3. Hooks ------------------------------------------------------------------
Titulo "3. Instalacion del pre-commit hook"
CopiarDir (Join-Path $Payload ".githooks") (Join-Path $Repo ".githooks")
CopiarDir (Join-Path $Payload "scripts") (Join-Path $Repo "scripts")
Remove-Item -Recurse -Force (Join-Path $Repo "scripts\base")
Copy-Item -Force (Join-Path $Payload "README.md"), (Join-Path $Payload "DEFINITION_OF_DONE.md") $Repo
Copy-Item -Force (Join-Path $Payload ".github\PULL_REQUEST_TEMPLATE.md") (Join-Path $Repo ".github\PULL_REQUEST_TEMPLATE.md")
GitRun @("config", "core.hooksPath", ".githooks") | Out-Null
GitRun @("add", "-A") | Out-Null
GitRun @("update-index", "--chmod=+x", ".githooks/pre-commit", ".githooks/pre-push") | Out-Null
Write-Host "  Primer commit con el hook activo (se espera VERDE)"
$r = GitRun @("commit", "-m", "feat(hooks): pre-commit secuencial formatter -> linter -> pruebas, pre-push y DoD v2.0 (#1)") -PermitirFalla
Guardar $r.Salida (Join-Path $Ev "precommit\01-primer-commit-con-hook.consola.log")
CopiarSiExiste (UltimoLogHook) (Join-Path $Ev "precommit\01-primer-commit-con-hook.hook.log")
if ($r.Codigo -ne 0) { Detener "el primer commit con el hook fallo (debia pasar)" }

# --- 4. Escenarios de falla ------------------------------------------------------
Titulo "4. Escenarios de falla del pre-commit"
$escenarios = @(
    @{ Id = "A-formatter"; Origen = "A_formatter_Paciente.java.txt";           Destino = "src\main\java\hospital\turnos\Paciente.java" },
    @{ Id = "B-linter";    Origen = "B_linter_CalculadoraFacturacion.java.txt"; Destino = "src\main\java\hospital\facturacion\CalculadoraFacturacion.java" },
    @{ Id = "C-pruebas";   Origen = "C_pruebas_ReglasFacturacion.java.txt";     Destino = "src\main\java\hospital\facturacion\ReglasFacturacion.java" }
)
$resumenEsc = @()
foreach ($e in $escenarios) {
    Write-Host ""; Write-Host "  Escenario $($e.Id)" -ForegroundColor Yellow
    $headAntes = ((Nativo "git" @("rev-parse", "HEAD")).Salida -join "").Trim()
    AplicarEscenario $e.Id (Join-Path $Repo $e.Destino)
    $rutaGit = $e.Destino -replace '\\', '/'
    GitRun @("add", $rutaGit) | Out-Null
    $r = GitRun @("commit", "-m", "demo($($e.Id)): este commit debe ser RECHAZADO por el hook") -PermitirFalla
    $headDespues = ((Nativo "git" @("rev-parse", "HEAD")).Salida -join "").Trim()
    $staged = (Nativo "git" @("diff", "--cached", "--name-only")).Salida
    $lineas = @("> git commit -m 'demo($($e.Id))'", "") + $r.Salida + @(
        "", "> codigo de salida de git commit: $($r.Codigo)",
        "> HEAD antes : $headAntes", "> HEAD despues: $headDespues",
        "> commit creado: $(if ($headAntes -eq $headDespues) { 'NO' } else { 'SI' })",
        "> archivos que siguen en staging: $($staged -join ', ')")
    Guardar $lineas (Join-Path $Ev "precommit\$($e.Id).consola.log")
    CopiarSiExiste (UltimoLogHook) (Join-Path $Ev "precommit\$($e.Id).hook.log")
    $resumenEsc += "$($e.Id): git commit codigo=$($r.Codigo), commit creado=$(if ($headAntes -eq $headDespues) { 'NO' } else { 'SI' })"
    # Restaurar el archivo (staging y arbol de trabajo)
    GitRun @("checkout", "HEAD", "--", $rutaGit) | Out-Null
    if ($headAntes -ne $headDespues) { Detener "el escenario $($e.Id) NO fue rechazado por el hook" }
}
Guardar $resumenEsc (Join-Path $Ev "precommit\resumen-escenarios.txt")

# --- 5. Modulo heredado para la auditoria --------------------------------------
Titulo "5. Snapshot del modulo heredado (auditoria-legacy)"
CopiarDir (Join-Path $Payload "auditoria-legacy") (Join-Path $Repo "auditoria-legacy")
GitRun @("add", "-A") | Out-Null
$r = GitRun @("commit", "-m", "chore(auditoria): snapshot del modulo heredado de liquidacion, objeto de la auditoria estatica (refs #2)") -PermitirFalla
Guardar $r.Salida (Join-Path $Ev "precommit\02-commit-auditoria-legacy.consola.log")
CopiarSiExiste (UltimoLogHook) (Join-Path $Ev "precommit\02-commit-auditoria-legacy.hook.log")
if ($r.Codigo -ne 0) { Detener "el commit del modulo heredado fue rechazado" }

# --- 6. Auditoria estatica --------------------------------------------------------
Titulo "6. Auditoria estatica del modulo heredado"
$Leg = Join-Path $Repo "auditoria-legacy"
$EvA = Join-Path $Ev "auditoria"
Maven @("clean", "compile") (Join-Path $EvA "00-compile.log") -dir $Leg | Out-Null
Maven @("pmd:pmd", "pmd:cpd") (Join-Path $EvA "01-pmd-cpd.log") -dir $Leg | Out-Null
CopiarSiExiste (Join-Path $Leg "target\pmd.xml") (Join-Path $EvA "pmd-dod.xml")
CopiarSiExiste (Join-Path $Leg "target\cpd.xml") (Join-Path $EvA "cpd.xml")
CopiarReporteHtml $Leg "pmd.html" (Join-Path $EvA "html\pmd-dod.html")
CopiarReporteHtml $Leg "cpd.html" (Join-Path $EvA "html\cpd.html")
Maven @("-Pmetricas", "pmd:pmd") (Join-Path $EvA "02-pmd-metricas.log") -dir $Leg | Out-Null
CopiarSiExiste (Join-Path $Leg "target\pmd.xml") (Join-Path $EvA "pmd-metricas.xml")
Maven @("checkstyle:checkstyle") (Join-Path $EvA "03-checkstyle.log") -dir $Leg -PermitirFalla | Out-Null
CopiarSiExiste (Join-Path $Leg "target\checkstyle-result.xml") (Join-Path $EvA "checkstyle-result.xml")
CopiarReporteHtml $Leg "checkstyle.html" (Join-Path $EvA "html\checkstyle.html")
Maven @("versions:display-dependency-updates") (Join-Path $EvA "04-versions-dependency-updates.log") -dir $Leg -PermitirFalla | Out-Null
Maven @("dependency:tree") (Join-Path $EvA "05-dependency-tree.log") -dir $Leg -PermitirFalla | Out-Null

# OSV-Scanner (base de datos de vulnerabilidades osv.dev, incluye GHSA y NVD)
$tools = Join-Path $Repo "tools"
New-Item -ItemType Directory -Force $tools | Out-Null
$osv = Join-Path $tools "osv-scanner.exe"
try {
    if (-not (Test-Path $osv)) {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        $rel = Invoke-RestMethod -UseBasicParsing "https://api.github.com/repos/google/osv-scanner/releases/latest"
        $asset = $rel.assets | Where-Object { $_.name -eq "osv-scanner_windows_amd64.exe" } | Select-Object -First 1
        Write-Host "  Descargando OSV-Scanner $($rel.tag_name)"
        Invoke-WebRequest -UseBasicParsing -OutFile $osv $asset.browser_download_url
        Guardar @("OSV-Scanner $($rel.tag_name)", $asset.browser_download_url) (Join-Path $EvA "osv-version.txt")
    }
    foreach ($obj in @(@{ Pom = (Join-Path $Leg "pom.xml"); Nombre = "osv-legacy" }, @{ Pom = (Join-Path $Repo "pom.xml"); Nombre = "osv-hospital-tpi" })) {
        foreach ($fmt in @("json", "table")) {
            $salida = Join-Path $EvA "$($obj.Nombre).$fmt.txt"
            $err = Join-Path $EvA "$($obj.Nombre).$fmt.stderr.txt"
            $p = Start-Process -FilePath $osv -ArgumentList @("-L", "`"$($obj.Pom)`"", "--format", $fmt) `
                 -RedirectStandardOutput $salida -RedirectStandardError $err -NoNewWindow -Wait -PassThru
            Write-Host "  OSV $($obj.Nombre) ($fmt): codigo $($p.ExitCode) (0 = sin vulnerabilidades, 1 = con vulnerabilidades)"
            Add-Content -Encoding UTF8 (Join-Path $EvA "osv-codigos.txt") "$($obj.Nombre) $fmt codigo=$($p.ExitCode)"
        }
    }
} catch {
    Write-Host "  OSV-Scanner no disponible: $($_.Exception.Message)" -ForegroundColor Yellow
    Guardar @("OSV-Scanner no disponible", $_.Exception.Message) (Join-Path $EvA "osv-ERROR.txt")
}

# Metricas del proyecto principal: original (legacy) vs refactor
Maven @("-Pmetricas", "clean", "compile", "pmd:pmd") (Join-Path $EvA "06-hospital-tpi-pmd-metricas.log") | Out-Null
CopiarSiExiste (Join-Path $Repo "target\pmd.xml") (Join-Path $EvA "hospital-tpi-pmd-metricas.xml")
Maven @("versions:display-dependency-updates") (Join-Path $EvA "07-hospital-tpi-versions.log") -PermitirFalla | Out-Null

# --- 7. Compuerta completa (mvn verify) ------------------------------------------
Titulo "7. Compuerta completa de la DoD v2.0 (mvn clean verify)"
$EvC = Join-Path $Ev "compuerta"
Maven @("clean", "verify") (Join-Path $EvC "mvn-clean-verify.log") | Out-Null
CopiarDir (Join-Path $Repo "target\site\jacoco") (Join-Path $EvC "jacoco")
CopiarSiExiste (Join-Path $Repo "target\checkstyle-result.xml") (Join-Path $EvC "checkstyle-result.xml")
CopiarSiExiste (Join-Path $Repo "target\pmd.xml") (Join-Path $EvC "pmd.xml")
CopiarSiExiste (Join-Path $Repo "target\cpd.xml") (Join-Path $EvC "cpd.xml")
New-Item -ItemType Directory -Force (Join-Path $EvC "surefire") | Out-Null
Copy-Item (Join-Path $Repo "target\surefire-reports\*.txt") (Join-Path $EvC "surefire")

# --- 8. Resumen -----------------------------------------------------------------
Titulo "8. Resumen"
$log = (Nativo "git" @("log", "--all", "--graph", "--format=%h %ad %s", "--date=format:%Y-%m-%d %H:%M")).Salida
Guardar (@("Fase 1 completada: $(Get-Date -Format s)", "", "== Versiones ==") + $versiones + @("", "== Escenarios ==") + $resumenEsc + @("", "== Historia git ==") + $log) (Join-Path $Ev "RESUMEN-fase1.txt")
$log | ForEach-Object { Write-Host "  $_" }
Write-Host ""
Write-Host "FASE 1 COMPLETA. Avisale a Claude para que revise las evidencias y prepare la fase 2 (GitHub)." -ForegroundColor Green
