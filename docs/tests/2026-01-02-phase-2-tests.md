# Phase 2 Test Report (2026-01-02)

## Scope
- Phase 2: Datenmodell + CRUD + Firestore-Anbindung
- Automatisiert: Unit-Tests via Gradle

## Environment
- WSL
- Java: /home/bjoern/android-studio/jbr (OpenJDK 21)
- Gradle: 8.14 (Wrapper)
- Android Gradle Plugin: 8.5.2
- compileSdk/targetSdk: 36

## Automated Tests
Command:
```
GRADLE_USER_HOME=/home/bjoern/was_essen_wir/.gradle \
JAVA_HOME=/home/bjoern/android-studio/jbr \
PATH=/home/bjoern/android-studio/jbr/bin:$PATH \
./gradlew test
```

Result: PASS

Warnings:
- AGP 8.5.2 ist nur bis compileSdk 34 getestet (compileSdk 36 gesetzt).
- Kotlin Opt-In Warnungen in `app/src/main/java/com/wasessenwir/app/ui/AppViewModel.kt` (ExperimentalCoroutinesApi).

## Manual Smoke Tests
- Nicht ausgefuehrt (UI-Tests in App noch offen).

## Empfehlungen
1) AGP upgraden oder `android.suppressUnsupportedCompileSdk=36` setzen.
2) `@OptIn(ExperimentalCoroutinesApi::class)` an den betreffenden Flow-Usage setzen oder alternative API verwenden.
3) Phase-2-Smoke-Tests in der App manuell durchfuehren (Household -> aktiv, Recipe CRUD, PlanEntry Lunch/Dinner, ShoppingList Snapshot).
