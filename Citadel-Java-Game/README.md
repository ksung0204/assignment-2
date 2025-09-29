
# 🏰 Citadels Java Game

[![Java](https://img.shields.io/badge/Java-8-blue.svg?logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/Built%20With-Gradle-02303A.svg?logo=gradle)](https://gradle.org/)
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Coverage](https://img.shields.io/badge/Test%20Coverage-90%25%2B-brightgreen)](./build/reports/tests/)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen)](./build/libs)

> A Java-based command-line implementation of the classic **Citadels** card game.


<p align="center">
  <img src="https://github.com/user-attachments/assets/f1d2d251-a0b6-4842-81cf-bf4ebf64f47b" alt="Centered Image" width="300"/>
</p>

---

## 📜 Features

- Fully functional Citadels game for **4–7 players**, one human and the rest AI.
- All **8 character cards** with special abilities implemented.
- **District cards**, including **purple special-effect cards**, are loaded from `cards.tsv`.
- Turn-based gameplay via CLI with full command handling.
- **Save/load** game state using JSON format.
- Intelligent **AI decision-making** logic for computer players.
- Unit tested with **JUnit5** and coverage via **JaCoCo**.

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/citadels/
│   │   ├── Cards/         # Card-related classes: CharacterCard, DistrictCard
│   │   ├── Game/          # Game logic and state management
│   │   ├── Player/        # Player hierarchy (Human, Computer)
│   │   ├── Utils/         # Utilities: card loader, deck, enums
│   │   └── App.java       # Main entry point
│   └── resources/
│       └── citadels/cards.tsv   # District cards data
├── test/                  # JUnit test cases
└── build.gradle           # Gradle build file
```

---

## 🚀 Getting Started

### 📦 Requirements

- Java 8+
- Gradle 7+

### ▶️ Run the Game

```bash
# Compile and package into a JAR
gradle jar

# Run the game
java -jar build/libs/citadels.jar
```

---

## 🧪 Running Tests

```bash
# Run all unit tests
gradle test

# Generate coverage report
gradle jacocoTestReport

# Open HTML coverage report
open build/reports/jacoco/test/html/index.html
```

---

## 📖 Game Commands

| Command                | Description |
|------------------------|-------------|
| `t`                    | Process the next turn |
| `hand`                 | Show your current hand |
| `gold`                 | Show current gold |
| `build <n>`            | Build a district (from hand position `n`) |
| `action`               | Use your character's ability |
| `action purple <name>` | Use the ability of a built purple district |
| `info <name>`          | Get info on a character or district |
| `citadel [p]`          | Show built districts (your own or another player's) |
| `end`                  | End your turn |
| `save <file>`          | Save game state to JSON |
| `load <file>`          | Load game state from JSON |
| `all`                  | Show all players' status |
| `help`                 | Show help menu |
| `debug`                | Toggle debug mode for AI hands |

---

## 🏁 Game Ending & Scoring

The game ends after a player builds 8 districts. Scores are calculated based on:
- Total cost of all built districts
- 3 bonus points for having all district colors
- 4 bonus points for the first to complete a city, 2 for others
- Extra points from special purple districts

---

## 💡 Notes

- Purple district abilities are accessed via `action purple <district name>` (only if built).
- For character abilities, use `action` alone.
- The game is robust to invalid inputs and provides feedback accordingly.

---

## 👨‍💻 Author

- 🎓 Developed for learning and practice purposes

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
