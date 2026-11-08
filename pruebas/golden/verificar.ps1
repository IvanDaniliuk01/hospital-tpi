# Golden master para Windows: corre la consola con el guion y compara contra la salida esperada.
# Uso: .\pruebas\golden\verificar.ps1            (compara)
#      .\pruebas\golden\verificar.ps1 -Grabar    (regraba la salida esperada)
param([switch]$Grabar)
$ErrorActionPreference = "Stop"
$raiz = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$dir = Join-Path $raiz "pruebas\golden"
$tmp = Join-Path ([IO.Path]::GetTempPath()) ("golden-" + [guid]::NewGuid())
New-Item -ItemType Directory $tmp | Out-Null
try {
  $fuentes = Get-ChildItem -Recurse (Join-Path $raiz "src\main\java") -Filter *.java | ForEach-Object { $_.FullName }
  & javac -encoding UTF-8 -d (Join-Path $tmp "clases") $fuentes
  if ($LASTEXITCODE -ne 0) { throw "javac fallo" }
  $psi = New-Object Diagnostics.ProcessStartInfo "java", "-cp `"$(Join-Path $tmp 'clases')`" hospital.ConsolaHospital"
  $psi.RedirectStandardInput = $true; $psi.RedirectStandardOutput = $true; $psi.RedirectStandardError = $true; $psi.UseShellExecute = $false
  $p = [Diagnostics.Process]::Start($psi)
  $p.StandardInput.Write(([IO.File]::ReadAllText((Join-Path $dir "guion.txt"))))
  $p.StandardInput.Close()
  $salida = $p.StandardOutput.ReadToEnd().Replace("`r", "")
  $p.WaitForExit()
  $esperada = Join-Path $dir "salida_esperada.txt"
  if ($Grabar) { [IO.File]::WriteAllText($esperada, $salida); Write-Host "Salida esperada regrabada"; exit 0 }
  if ($salida -ceq [IO.File]::ReadAllText($esperada)) { Write-Host "GOLDEN MASTER OK: salida identica" }
  else { [IO.File]::WriteAllText((Join-Path $raiz "salida_obtenida.txt"), $salida); Write-Error "GOLDEN MASTER FALLA: ver salida_obtenida.txt" }
} finally { Remove-Item -Recurse -Force $tmp }
