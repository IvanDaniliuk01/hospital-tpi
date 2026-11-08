#!/usr/bin/env bash
# Golden master: corre la consola con el guion y compara contra la salida esperada.
# Uso: pruebas/golden/verificar.sh            (compara)
#      pruebas/golden/verificar.sh --grabar   (regraba la salida esperada)
set -euo pipefail
raiz="$(cd "$(dirname "$0")/../.." && pwd)"
dir="$raiz/pruebas/golden"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
javac -encoding UTF-8 -d "$tmp/clases" $(find "$raiz/src/main/java" -name '*.java')
java -cp "$tmp/clases" hospital.ConsolaHospital < "$dir/guion.txt" 2>/dev/null | tr -d '\r' > "$tmp/salida.txt"
if [ "${1:-}" = "--grabar" ]; then
  cp "$tmp/salida.txt" "$dir/salida_esperada.txt"
  echo "Salida esperada regrabada ($(wc -l < "$dir/salida_esperada.txt") lineas)"
  exit 0
fi
if diff -u "$dir/salida_esperada.txt" "$tmp/salida.txt"; then
  echo "GOLDEN MASTER OK: salida identica ($(wc -l < "$tmp/salida.txt") lineas)"
else
  echo "GOLDEN MASTER FALLA: la salida cambio" >&2
  exit 1
fi
