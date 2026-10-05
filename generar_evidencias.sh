#!/usr/bin/env bash
# Genera las evidencias del TP Semana 2 en la carpeta evidencias/:
#   1) corrida INGENUA: solo la suite de camino feliz sobre el procesar original
#      -> 100 % de sentencias, ramas incompletas (evidencias/jacoco-ingenua)
#   2) corrida COMPLETA: todas las suites + compuerta DoD C3 (mvn verify)
#      -> evidencias/jacoco-completa y evidencias/surefire
set -euo pipefail
cd "$(dirname "$0")"

rm -rf evidencias/jacoco-ingenua evidencias/jacoco-completa evidencias/surefire
mkdir -p evidencias

echo "== 1/2 Corrida ingenua (solo TurnoManagerLegacyCoberturaIngenuaTest) =="
mvn -B clean test -Dtest=TurnoManagerLegacyCoberturaIngenuaTest | tee evidencias/log-ingenua.txt
cp -r target/site/jacoco evidencias/jacoco-ingenua

echo "== 2/2 Corrida completa (mvn verify = compuerta DoD) =="
mvn -B clean verify | tee evidencias/log-completa.txt
cp -r target/site/jacoco evidencias/jacoco-completa
mkdir -p evidencias/surefire
cp target/surefire-reports/*.txt evidencias/surefire/

echo
echo "Resumen de cobertura (jacoco.csv, corrida completa):"
python3 - <<'EOF' 2>/dev/null || awk -F, 'NR>1{print $2"."$3": lineas "$9"/"($8+$9)" ramas "$7"/"($6+$7)}' evidencias/jacoco-completa/jacoco.csv
import csv
for r in csv.DictReader(open("evidencias/jacoco-completa/jacoco.csv")):
    lm, lc = int(r["LINE_MISSED"]), int(r["LINE_COVERED"])
    bm, bc = int(r["BRANCH_MISSED"]), int(r["BRANCH_COVERED"])
    print(f'{r["PACKAGE"]}.{r["CLASS"]:<28} lineas {lc}/{lc+lm}  ramas {bc}/{bc+bm}')
EOF
echo "Listo. Evidencias en ./evidencias"
