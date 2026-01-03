# UI Guidlines (Vorlage aus "dont do nothing")

Diese Datei beschreibt das UI/Design-Setup aus dem Projekt "dont do nothing".
Ziel: 1:1 Orientierung, damit das neue Projekt denselben Look & Feel bekommt.

## Farben
- Primary/Accent: Cyan/Tuerkis `#3CC7C2`
- Inaktive Tabs: Grau `#B0B0B0`
- Card-Hintergrund (Light): sehr helles Cyan `#E6F6F5`
- Dark Mode:
  - Colors aus `ColorScheme.fromSeed(...)`
  - Cards nutzen `surfaceVariant` statt `#E6F6F5`

## Typografie
- Standard Material Typography (keine Custom-Font)
- Title/Section: `titleMedium` + `fontWeight.w600`
- Buttons: `fontWeight.w600`, weißer Text auf Cyan
- Zahlen/Timer: `headlineSmall` + `fontWeight.w600`

## Layout & Spacing
- Screen-Padding: 24px rundum
- Section-Abstand: 12–20px
- Cards: `borderRadius` 16, `padding` 16–20
- Buttons: vertikal 12px Abstand

## Navigation
- Top-Navigation (Tabs) mit 4 Buttons
  - Aktiver Tab: Cyan
  - Inaktiv: Grau
  - Hoehe: 48
- AppBar:
  - Transparent/flat
  - Titel leicht nach unten gepaddet
  - toolbarHeight etwas erhoeht

## Komponenten

### Cyan Button (Primary)
- Hintergrund: Cyan (`#3CC7C2`)
- Text: Weiß
- Padding: horizontal 18, vertikal 12
- Schrift: `fontWeight.w600`

### Cards (Standard)
- Background: Light-Cyan oder `surfaceVariant` im Dark Mode
- Radius: 16
- Padding: 16–20

### Overview Cards (Home)
- 2-Spalten Grid (Wrap)
- Fixe Mindesthoehe 140
- Titel oben, Wert gross, optional Subtitle/Chart

## Animationen & States
- `AnimatedSwitcher` (250ms) fuer Session-Finish/Actions
- `AnimatedSwitcher` fuer Hintergrund (Running-Mode -> Black Overlay)
- "Running State" blendet Navigation aus und dunkelt UI ab
- Buttons im Running-State bleiben zentriert

## Interaktions-Details
- Session-Start/Stop als zentraler Kreis (200px)
- Timer zeigt Millisekunden (centiseconds)
- Nach Stop: Action-Buttons erscheinen an gleicher Position
- Snackbars fuer Status-Feedback (z. B. Reminder)

## Dark Mode Verhalten
- Cards wechseln von hellcyan zu `surfaceVariant`
- Textfarben bleiben Material-Default, keine Sonderfarben

## Formate (UI)
- Datum: deutsch `TT.MM.YYYY`
- Zeit: `HH:MM`
- Dezimal: Deutsch nutzt Komma

## TODO fuer neues Projekt
- Compose-Theme anlegen mit Accent und Card-Surface
- Primary Button als eigene Composable
- Top-Navigation analog implementieren
- AnimatedSwitcher-Analog in Compose (Crossfade/AnimatedContent)
