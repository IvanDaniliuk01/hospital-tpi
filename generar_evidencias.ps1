# Genera las evidencias del TP Semana 2 en la carpeta evidencias\ (Windows / PowerShell).
#   1) corrida INGENUA: solo la suite de camino feliz sobre el procesar original
#      -> 100 % de sentencias, ramas incompletas (evidencias\jacoco-ingenua)
#   2) corrida COMPLETA: todas las suites + compuerta DoD C3 (mvn verify)
#      -> evidencias\jacoco-completa, evidencias\surefire y logs
#
# Requisitos: JDK 17 o superior (javac en el PATH o JAVA_HOME definido).
# Maven: si no está instalado, el script descarga Apache Maven 3.9.11 (binario oficial,
# ~10 MB) en %USERPROFILE%\.m2\apache-maven-3.9.11 y lo usa desde ahí. No toca el PATH.
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

# --- JDK -------------------------------------------------------------------
$javac = Get-Command javac -ErrorAction SilentlyContinue
if (-not $javac -and $env:JAVA_HOME) { $javac = Get-Command "$env:JAVA_HOME\bin\javac.exe" -ErrorAction SilentlyContinue }
if (-not $javac) {
    Write-Host "No se encontró un JDK (javac). Instalá Temurin 17+ desde https://adoptium.net y volvé a correr." -ForegroundColor Red
    exit 1
}
& $javac.Source -version 2>&1 | Write-Host

# --- Maven -----------------------------------------------------------------
$mvn = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvn) {
    $mvnCmd = $mvn.Source
} else {
    $mavenVersion = "3.9.11"
    $mavenHome = Join-Path $env:USERPROFILE ".m2\apache-maven-$mavenVersion"
    $mvnCmd = Join-Path $mavenHome "bin\mvn.cmd"
    if (-not (Test-Path $mvnCmd)) {
        $zip = Join-Path $env:TEMP "apache-maven-$mavenVersion-bin.zip"
        # Mismo binario que usa el Maven Wrapper oficial (Maven Central); archive.apache.org como respaldo.
        $urls = @(
            "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$mavenVersion/apache-maven-$mavenVersion-bin.zip",
            "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
        )
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        $descargado = $false
        foreach ($url in $urls) {
            try {
                Write-Host "Maven no está en el PATH: descargando $url ..."
                Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing
                $descargado = $true; break
            } catch { Write-Host "  no disponible: $($_.Exception.Message)" -ForegroundColor Yellow }
        }
        if (-not $descargado) { Write-Host "No se pudo descargar Maven." -ForegroundColor Red; exit 1 }
        Expand-Archive -Path $zip -DestinationPath (Join-Path $env:USERPROFILE ".m2") -Force
        Remove-Item $zip
    }
    Write-Host "Usando Maven en $mavenHome"
}

# Corre Maven mostrando la salida y guardándola en UTF-8. Los avisos que Java escribe
# por stderr no deben abortar el script (PowerShell los convierte en errores): se
# baja ErrorActionPreference solo durante la llamada y se valida el código de salida.
function Ejecutar-Maven([string[]] $argumentos, [string] $log) {
    $prev = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $salida = & $mvnCmd @argumentos 2>&1 | ForEach-Object { $_.ToString() }
    $codigo = $LASTEXITCODE
    $ErrorActionPreference = $prev
    $salida | Out-File -FilePath $log -Encoding utf8
    $salida | ForEach-Object { Write-Host $_ }
    if ($codigo -ne 0) { Write-Host "Maven terminó con código $codigo (ver $log)" -ForegroundColor Red; exit $codigo }
}

# --- Evidencias ------------------------------------------------------------
Remove-Item -Recurse -Force evidencias\jacoco-ingenua, evidencias\jacoco-completa, evidencias\surefire -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force evidencias | Out-Null

Write-Host "== 1/2 Corrida ingenua (solo TurnoManagerLegacyCoberturaIngenuaTest) ==" -ForegroundColor Cyan
Ejecutar-Maven @("-B", "clean", "test", "-Dtest=TurnoManagerLegacyCoberturaIngenuaTest") "evidencias\log-ingenua.txt"
Copy-Item -Recurse target\site\jacoco evidencias\jacoco-ingenua

Write-Host "== 2/2 Corrida completa (mvn verify = compuerta DoD) ==" -ForegroundColor Cyan
Ejecutar-Maven @("-B", "clean", "verify") "evidencias\log-completa.txt"
Copy-Item -Recurse target\site\jacoco evidencias\jacoco-completa
New-Item -ItemType Directory -Force evidencias\surefire | Out-Null
Copy-Item target\surefire-reports\*.txt evidencias\surefire\

"LISTO $(Get-Date -Format s)" | Out-File -Encoding utf8 evidencias\LISTO.txt
Write-Host "Listo. Evidencias en .\evidencias" -ForegroundColor Green
