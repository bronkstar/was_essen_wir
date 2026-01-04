# Cut – was_essen_wir
Datum: 2026-01-03
Branch: main
Letzter Commit: 0dbffca (Plan -> Einkauf Tab Navigation), 4a0eb0b (Einkaufsliste Screen), 21ef19b (Plan Range Persistenz)

## Ziel dieser Phase
- MVP-Flows fuer Planung und Einkaufsliste stabilisieren und bedienbar machen.

## Aktueller Stand
- Plan-Flow auf Wochenliste umgestellt: Datum von/bis, Liste erstellen, Rezepte toggeln, Tage zuweisen.
- Plan-Uebersicht ist persistent (PlanRange in Firestore) und bleibt nach Tab-Wechsel sichtbar.
- Plan-Uebersicht ist scrollbar und zeigt Mittag/Abend Fortschritt + Tageszuweisungen.
- Button „Zur Einkaufsliste“ erzeugt Liste und wechselt in den Einkauf-Tab.
- Einkaufsliste Screen: Dropdown zur Listenauswahl, Zutaten gesamt als Auszug (3 Items), Einkaufsliste darunter mit HaveIt/Checked.

## Wichtige Entscheidungen
- PlanRange wird pro Haushalt in Firestore gespeichert (Collection: planRanges, docId = householdId).
- Einkaufsliste basiert auf „Checked“-Items; HaveIt bleibt als zusaetzliche Markierung.
- Zutaten-Anzeige zeigt g/ml ggf. als kg/l (>=1000).

## Offene Punkte / TODO
- [ ] Einkaufsliste im Plan: Nutzerfeedback pruefen (UX/Feinschliff).
- [ ] Firestore Rules fuer planRanges an MVP-Policy anpassen (z.B. household membership).
- [ ] Zusammenfassung/Zutatenlogik ggf. weiter verfeinern (Filter, Sortierung).

## Bekannte Bugs / Risiken
- Gradle Build in WSL kann sporadisch an „usable wildcard IP“ scheitern (WSL restart hilft).
- Firestore Rules aktuell sehr permissiv (isSignedIn), keine household member checks.

## Nächste Schritte (konkret)
1) App testen: Plan -> Einkauf Tab Wechsel, Persistenz nach App-Neustart.
2) UX-Feedback fuer Einkaufsliste (Auszug 3 Items + komplette Liste).
3) Firestore Rules update fuer planRanges.

## Lokale Hinweise
- google-services.json liegt lokal unter app/google-services.json und ist ignoriert.
