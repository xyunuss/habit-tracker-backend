# Habit Tracker - Backend

**Thema:** Habit Tracker  
**Team:** Yunus Schultze (598122) - Einzelarbeit  
**Modul:** Webtechnologien, HTW Berlin

## 📋 Projektbeschreibung

Das Backend des Habit Trackers ist eine REST API, die mit Spring Boot entwickelt wurde. Sie ermöglicht das Verwalten von Gewohnheiten (Habits) und deren täglichen Einträgen (Entries).

## 🛠️ Tech Stack

- **Framework:** Spring Boot 3.5
- **Sprache:** Java 21
- **Datenbank:** PostgreSQL (Production), H2 (Tests)
- **Build Tool:** Gradle
- **Deployment:** Render.com

## 🚀 Schnellstart

### Voraussetzungen
- Java 21
- Gradle (oder Gradle Wrapper verwenden)
- PostgreSQL (für lokale Entwicklung) oder Umgebungsvariablen setzen

### Installation & Start

```bash
# Repository klonen
git clone https://github.com/xyunuss/habit-tracker-backend.git
cd habit-tracker-backend

# Umgebungsvariablen setzen (oder .env Datei erstellen)
export DB_URL=jdbc:postgresql://localhost:5432/habit_tracker
export DB_USER=your_username
export DB_PASSWORD=your_password

# Anwendung starten
./gradlew bootRun
```

Die API ist dann unter `http://localhost:8080` erreichbar.

### Tests ausführen

```bash
./gradlew test
```

## 📡 API Endpunkte

### Habits

| Methode | Endpunkt | Beschreibung |
|---------|----------|--------------|
| GET | `/api/habits` | Alle Habits abrufen |
| GET | `/api/habits/{id}` | Einzelnen Habit abrufen |
| POST | `/api/habits` | Neuen Habit erstellen |
| PUT | `/api/habits/{id}` | Habit aktualisieren |
| DELETE | `/api/habits/{id}` | Habit löschen |

### Habit Entries

| Methode | Endpunkt | Beschreibung |
|---------|----------|--------------|
| GET | `/api/entries/habit/{habitId}` | Entries für einen Habit |
| GET | `/api/entries/range` | Entries in Zeitraum (Query: startDate, endDate) |
| GET | `/api/entries/date/{date}` | Entries für ein Datum |
| POST | `/api/entries/toggle` | Entry togglen (check/uncheck) |
| POST | `/api/entries` | Entry setzen |
| DELETE | `/api/entries/{id}` | Entry löschen |

### Beispiel: Habit erstellen

```bash
curl -X POST http://localhost:8080/api/habits \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sport",
    "color": "blue",
    "type": "DAILY",
    "icon": "💪"
  }'
```

### Beispiel: Habit abhaken

```bash
curl -X POST http://localhost:8080/api/entries/toggle \
  -H "Content-Type: application/json" \
  -d '{
    "habitId": 1,
    "date": "2024-01-15"
  }'
```

## 📁 Projektstruktur

```
src/main/java/htw/webtech/habit_tracker/
├── HabitTrackerApplication.java    # Main Application + CORS Config
├── HabitController.java            # REST Controller für Habits
├── HabitEntryController.java       # REST Controller für Entries
├── model/
│   ├── Habit.java                  # Entity: Gewohnheit
│   └── HabitEntry.java             # Entity: Tageseintrag
└── repository/
    ├── HabitRepository.java        # JPA Repository für Habits
    └── HabitEntryRepository.java   # JPA Repository für Entries
```

## 🗄️ Datenmodell

### Habit
| Feld | Typ | Beschreibung |
|------|-----|--------------|
| id | Long | Primärschlüssel |
| name | String | Name der Gewohnheit (required) |
| description | String | Kurze Beschreibung (optional) |
| color | String | Farbe (blue, green, purple, etc.) |
| icon | String | Emoji Icon (optional) |
| type | Enum | DAILY oder WEEKLY |
| targetPerWeek | Integer | Ziel pro Woche (für WEEKLY) |
| createdAt | LocalDate | Erstellungsdatum |

### HabitEntry
| Feld | Typ | Beschreibung |
|------|-----|--------------|
| id | Long | Primärschlüssel |
| habit | Habit | Zugehöriger Habit (FK) |
| date | LocalDate | Datum des Eintrags |
| completed | boolean | Abgehakt ja/nein |

## 🔒 Sicherheit

- Credentials werden über Umgebungsvariablen verwaltet
- CORS ist für das Frontend konfiguriert
- Keine sensiblen Daten im Repository

## 🧪 Tests

Das Projekt enthält:
- **Controller-Tests:** Testen der REST API mit MockMvc
- **Repository-Tests:** Testen der Datenbankoperationen
- **Integration-Tests:** End-to-End Tests mit H2 In-Memory DB

```bash
# Alle Tests ausführen
./gradlew test

# Test-Report anzeigen
open build/reports/tests/test/index.html
```

## 🌐 Deployment

Das Backend ist auf Render.com deployed:
- **URL:** https://habit-tracker-backend-v21g.onrender.com
- **Auto-Deploy:** Bei Push auf main Branch

## 📝 Lizenz

Dieses Projekt wurde im Rahmen des Moduls Webtechnologien an der HTW Berlin erstellt.
