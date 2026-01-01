# AGENTS.md

## Projekt
- Name: was_essen_wir
- Ziel: Essensplanungs-App fuer zwei Personen (Android/Web), Fokus auf Einkaufsliste, Planung, Screenshot-Import.
- Stack: Android (Kotlin, Jetpack Compose), MVVM (ViewModel + StateFlow), Firebase Auth + Firestore (Offline an).

## Arbeitsweise (kurz)
- Vor jedem Arbeitspaket kurze, nummerierte Fragen stellen und erst nach Bestaetigung starten.
- Kleine, klare Commits pro Phase.
- Keine Secrets ins Repo; Firebase Keys nur ueber .env/.local files.

## Rollen (Agent-Setup)
- orchestrator: Scope klaeren, Phasenplan steuern, Reviews koordinieren.
- android-policy-expert: Abgleich mit Play Store Richtlinien (Datenschutz, Permissions, Billing).
- firebase-specialist: Auth, Firestore-Struktur, Security Rules, Offline Sync.
- qa-lead: Testplan, Smoke Tests, Offline-Checks.
- ux-director: Minimal-UX fuer Planung/Einkauf/Import.

## Guardrails
- Keine Analytics/Ads im MVP.
- Firestore: Zugriff nur fuer Household-Mitglieder.
- Datenmodell strikt nach MVP-Spezifikation.
- Screenshot-Import: nur OCR + Zutaten, keine Zubereitungstexte.
