# SKS Trainer ⛵

Der **SKS Trainer** ist eine moderne Android-Lern-App, die speziell zur Vorbereitung auf den Sportküstenschifferschein (SKS) entwickelt wurde. Sie bietet eine effiziente Möglichkeit, den amtlichen Fragenkatalog per Multiple-Choice oder per Spracheingabe zu trainieren.

## ✨ Features

### 📚 Lern-Modus (Flashcards)
*   Lernen des offiziellen SKS-Fragenkatalogs (Navigation, Schifffahrtsrecht, Wetterkunde, Seemannschaft I).
*   Elegantes 3D-Flashcard-System: Tippen zum Umdrehen der Kartekarte.
*   Automatische Anzeige von Bildern (Lichterführung, Seekartenausschnitte) passend zur Frage.
*   **Favoriten-System:** Markiere schwere Fragen mit einem Lesezeichen, um sie später gezielt in der Kategorie "Gemerkte Fragen" zu wiederholen.

### 🎙️ Test-Modus (Spracherkennung)
*   Simuliert eine mündliche Prüfungssituation.
*   Integrierte Google-Spracherkennung (Speech-to-Text): Sprich die Antwort einfach ein, indem du den Mikrofon-Button gedrückt hältst.
*   Direkter Vergleich: Deine eingesprochene Antwort wird der amtlichen Musterantwort gegenübergestellt.
*   Ehrliche Selbsteinschätzung (Gewusst / Nicht gewusst).

### 📊 Detaillierte Statistiken
*   Fortschrittsanzeige für die gesamte App sowie einzeln für jedes Themengebiet.
*   Anzeige der absolvierten Tests, der bearbeiteten Lernkarten und der Erfolgsquote (in Prozent).
*   Einfaches Zurücksetzen der Statistiken (global oder themenspezifisch) über den integrierten Mülleimer-Button.

### 🎨 Modernes Design
*   Komplett in **Jetpack Compose** geschrieben.
*   Maritimes Farbschema im SKS-Dunkelblau.
*   Unterstützt Light- und Dark-Mode.

## 🛠️ Technische Details
*   **Sprache:** Kotlin
*   **UI-Toolkit:** Jetpack Compose (Material Design 3)
*   **Architektur:** MVVM (Model-View-ViewModel) Ansätze
*   **Navigation:** Navigation Compose
*   **Datenspeicherung:** JSON-basierte Speicherung des Fragenkatalogs im `assets`-Ordner. Lokale Speicherung von Statistiken und Favoriten über `kotlinx.serialization` direkt auf dem Gerät.
*   **Spracherkennung:** Android `SpeechRecognizer` API

## 🚀 Installation & Build

1. Klone das Repository:
   ```bash
   git clone https://github.com/dein-benutzername/sks-trainer.git
   ```
2. Öffne das Projekt in **Android Studio**.
3. Baue das Projekt mit Gradle und starte es auf einem Emulator oder einem physischen Android-Gerät.

## 📝 Lizenz & Rechtliches
*   Die App basiert auf dem amtlichen Fragenkatalog für den Sportküstenschifferschein (SKS) der Wasserstraßen- und Schifffahrtsverwaltung des Bundes (WSV) / ELWIS.
*   Die App dient ausschließlich Übungszwecken und ersetzt keine amtlichen Lehrmaterialien.
*   Es werden **keine** Sprach- oder Nutzerdaten an Dritte gesendet. Die Spracherkennung erfolgt über die systeminternen Dienste.

---
*Entwickelt mit Jetpack Compose und viel Liebe zum Segelsport.*
