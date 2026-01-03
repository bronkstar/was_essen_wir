# AGENTS.md

## Projekt
- Name: was_essen_wir
- Ziel: Essensplanungs-App fuer zwei Personen (Android/Web), Fokus auf Einkaufsliste, Planung, Screenshot-Import.
- Stack: Android (Kotlin, Jetpack Compose), MVVM (ViewModel + StateFlow), Firebase Auth + Firestore (Offline an).

## Arbeitsweise (kurz)
- Vor jedem Arbeitspaket kurze, nummerierte Fragen stellen und erst nach Bestaetigung starten.
- Danach ein Satz "Scope-Ack" (was drin ist, was explizit nicht).
- Kleine, klare Commits pro Phase.
- UI-Aenderungen nur nach frischem Build + Reinstall pruefen; Update-Zeit via `adb shell dumpsys package com.wasessenwir.app | grep lastUpdateTime` verifizieren.
- Keine Secrets ins Repo; Firebase Keys nur ueber .env/.local files.

## Rollen (Agent-Setup)
- orchestrator: Scope klaeren, Phasenplan steuern, Reviews koordinieren.
- android-policy-expert: Abgleich mit Play Store Richtlinien (Datenschutz, Permissions, Billing).
- firebase-specialist: Auth, Firestore-Struktur, Security Rules, Offline Sync, Query-Patterns.
- data-model-guardian: MVP-Datenmodell strikt pruefen (Household, Recipe, PlanEntry, ShoppingList, Ingredient) inkl. Schema-Versionierung.
- sync-conflict-analyst: Offline-Konflikte, Merge-Strategien, Transaktionen, idempotente Updates.
- qa-lead: Testplan, Smoke Tests, Offline-Checks; Phase-Ende Pflicht-Tests koordinieren und dokumentieren.
- ux-director: Minimal-UX fuer Planung/Einkauf/Import inkl. OCR-Review und Snapshot-Einkaufsliste.

## Guardrails
- Keine Analytics/Ads im MVP.
- Firestore: Zugriff nur fuer Household-Mitglieder.
- Datenmodell strikt nach MVP-Spezifikation.
- Screenshot-Import: nur OCR + Zutaten, keine Zubereitungstexte.

## MVP Scope + Definition of Done
- Auth laeuft (Google und/oder Email/Password) und Household-Join ist definiert und getestet.
- Datenmodell entspricht MVP (Household, Recipe, PlanEntry, ShoppingList, Ingredient) inkl. Versionierung und Migrations-Notiz.
- Firestore Rules erzwingen Household-Mitgliedschaft; Offline-Persistence aktiv und Sync nach Reconnect verifiziert.
- Wochenplan Mo-So, Recipe pro Tag, Plan speichert/synchronisiert korrekt.
- Einkaufsliste ist Snapshot, aggregiert korrekt, haveIt/checked funktioniert offline und nach Sync.
- Export als Text und CSV funktioniert; REWE-Suche pro Item oeffnet korrekt.
- Screenshot-Import (OCR) parst nur Zutaten, Review-Flow zum Korrigieren vorhanden.
- UX: Basis-Flows Planung, Einkauf, Import sind navigierbar, ohne Dead-Ends.
- QA: Smoke-Tests fuer Plan/Einkauf/Import dokumentiert und vom Debugger-Review bestaetigt.

## Phase-Ende Tests (Pflicht)
- Jede Phase endet mit einem kurzen Test-Block (Fail = Phase nicht abgeschlossen).
- Testergebnisse werden in einem separaten Dokument festgehalten inkl. Empfehlungen.
- Naechste Phase startet erst, wenn Empfehlungen besprochen und bestaetigt sind.
- Phase 1: Build+Run (Debug) erfolgreich, App startet auf Device.
- Phase 2: CRUD-Smoketests fuer Household, Recipe, PlanEntry (Lunch/Dinner), ShoppingList Snapshot.
- Phase 3: Aggregation verifiziert (korrekte Summen), Offline-Toggle (Flugmodus) getestet, Sync nach Reconnect ok.
- Phase 4: Export (Text/CSV) und REWE-Deep-Link getestet.
- Phase 5: OCR-Import tested (Cookidoo Screenshot), Review-Flow getestet, keine Zubereitungstexte gespeichert.
