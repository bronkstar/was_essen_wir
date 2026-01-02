# was_essen_wir

Essensplanungs-App fuer zwei Personen mit Wochenplan, Einkaufsliste und
Screenshot-Import fuer Zutaten (Cookidoo). Fokus: schnell, offline-faehig,
einfach zu teilen.

## Ziele (MVP)
- Rezepte + Zutaten pflegen
- Woche planen (Rezept pro Tag)
- Einkaufsliste automatisch erzeugen (aggregiert, skalierbar)
- Einkaufsliste abhaken + exportieren/teilen
- Screenshot-Import (Zutatenliste) mit Review

## Tech-Stack
- Android: Kotlin + Jetpack Compose
- Architektur: MVVM (ViewModel + StateFlow)
- Backend/Sync: Firebase Auth + Cloud Firestore (Offline an)
- Keine Analytics/Ads im MVP

## Quickstart (lokal)
1) Android Studio installieren
2) Repo oeffnen: `/home/bjoern/was_essen_wir`
3) Gradle Sync abwarten
   - Hinweis: falls der Gradle Wrapper fehlt, einmal "Sync Project with Gradle Files" ausfuehren.
4) Emulator oder Device verbinden
5) App aus Android Studio starten

## Firebase Setup (MVP)
1) Firebase Projekt anlegen
2) Android App registrieren (Package Name festlegen)
3) `google-services.json` herunterladen
4) Datei nach `app/google-services.json` legen (nicht committen)
5) Firebase Auth aktivieren (Google, Email/Password)
6) Firestore aktivieren + Offline Persistence an (Client)

## Konfiguration
- `.env.example` zeigt Platzhalter. Lokale Werte in `.env` oder
  `.env.local` ablegen (nicht committen).

## Struktur (geplant)
- `app/` Android App
- `docs/konzepte_fuer_architekturen/` Sprint-Konzept + Entscheidungen

## Entwicklung
- UI: Jetpack Compose
- State: StateFlow
- Repo-Layer fuer Firestore Zugriffe

## Tests (spaeter)
- Unit-Tests fuer Aggregation (Einkaufsliste)
- UI-Smoketests fuer Plan/Einkauf/Import

## Hinweise
- Keine Secrets ins Repo.
- Datenschutz/Play-Store Richtlinien vor Release pruefen.
