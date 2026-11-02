# Cobertura como única fuente de verdad para facturar

El código heredado modela la misma realidad dos veces. Turnos guarda la obra social como texto (OSDE, SWISS, PUBLICA) y deriva una "categoría" (premium o público), mientras que Facturación recibe dos booleanos independientes (plan de salud y obra social) que el operador podría cargar en contradicción con el paciente. Decidimos que el paciente tenga una única **Cobertura** (Prepaga, Obra social o Sin cobertura), de la que se derivan tanto los textos de la consola como las reglas de facturación. `CategoriaPaciente` desaparece como concepto de dominio. En el prototipo base la duplicación se conserva a propósito como smell diagnosticable (S6).

## Considered Options

- **Pedir los dos booleanos al facturar.** Descartada: deja dos fuentes de verdad que pueden contradecirse.
- **Mantener Categoría y Cobertura como conceptos distintos, uno comercial y otro financiero.** Descartada: la regla heredada las vuelve equivalentes uno a uno.
