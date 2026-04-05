# Space Shooter

A classic arcade-style space shooter game written in Java using Swing.

## Features

- Player ship that moves left/right and fires upward bullets
- 55 enemies (5 rows × 11 columns) in three types, each with unique artwork
- Formation movement – enemies sweep side-to-side and drop down when they hit an edge
- Enemy fire: random enemies shoot downward bullets
- Score tracking with high-score persistence per session
- Three distinct enemy types worth 10, 20, and 30 points
- 3 lives with brief invincibility on hit
- Speed increases as enemies are eliminated (classic "last alien" feel)
- Multi-level progression – clear all enemies to advance
- Scrolling star-field background
- Pause support

## Controls

| Key            | Action      |
|----------------|-------------|
| ← / A          | Move left   |
| → / D          | Move right  |
| Space          | Fire        |
| P              | Pause       |
| Enter          | Start / Next level |
| Esc            | Return to menu |

## Requirements

- Java 8 or later (JDK)

## Build & Run

**Using Make:**

```bash
make run
```

**Manually:**

```bash
mkdir -p out
javac -d out src/spaceshooter/*.java
java -cp out spaceshooter.SpaceShooter
```

## Project Structure

```
src/
└── spaceshooter/
    ├── SpaceShooter.java   # Entry point – creates the JFrame window
    ├── GamePanel.java      # Game loop, rendering, collision detection
    ├── Player.java         # Player ship entity
    ├── Enemy.java          # Enemy (alien) entity
    └── Bullet.java         # Projectile entity
```
