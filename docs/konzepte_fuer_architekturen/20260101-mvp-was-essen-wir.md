# Sprint-Konzept: MVP "was_essen_wir"

## Ziel
- Essensplanung fuer 2 Personen: Rezepte pflegen, Woche planen, Einkaufsliste erzeugen.
- Einkaufsliste ist Snapshot, offline nutzbar, abhaken/teilen.
- Screenshot-Import (Cookidoo Zutatenliste) mit Review-Flow.

## Weg
- Android App mit Kotlin + Jetpack Compose, MVVM (StateFlow).
- Firebase Auth + Firestore (Offline Persistence an).
- Datenmodell exakt nach MVP-Spezifikation (Household, Recipe, PlanEntry, ShoppingList).
- MVP ohne Ads/Analytics, ohne oeffentliche Inhalte.

## Akzeptanzkriterien (MVP)
- Zwei Accounts sehen synchron: Rezepte, Plan, Einkaufsliste inkl. haveIt/checked.
- Einkaufsliste aggregiert korrekt, Export als Text + CSV funktioniert.
- REWE-Suche pro Item oeffnet sich.
- Screenshot-Import aus Cookidoo funktioniert inkl. Review/Korrektur.
- Offline nutzbar, Sync nach Reconnect.

## Phasenplan

### Phase 1: Projekt-Setup
- Repo initialisieren (Kotlin/Compose)
- Firebase Projekt anbinden (Auth + Firestore)
- Security Rules Grundgeruest

### Phase 2: Basis-Datenmodelle
- Household, Ingredient, Recipe, PlanEntry, ShoppingList (Firestore)
- CRUD fuers Minimum (UI + Repo)

### Phase 3: Wochenplan + Einkaufsliste
- Wochenansicht Mo–So
- Aggregationslogik + Snapshot speichern
- Einkaufsliste UI (haveIt/checked + Filter)

### Phase 4: Export + REWE
- Share Text / Copy / CSV
- REWE-Suche pro Item

### Phase 5: Screenshot-Import
- Bildwahl + OCR (ML Kit)
- Parsing Cookidoo-Zutaten
- Review + Save

## Offene Fragen
- Welche Login-Optionen genau im MVP (Google, Email oder beides)?
- Wie soll der Household-Join konkret laufen (Code-Format)?
- Welche Offline-Faelle sind kritisch (z. B. gleichzeitige Aenderungen)?
- Soll Web-Version parallel oder erst nach Android-MVP starten?

## Status
- 2026-01-01: Konzept neu angelegt. (Autor: Codex)
