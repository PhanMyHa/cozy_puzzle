# Cozy Puzzle — Product Requirements Document (PRD)

## 1. Product Overview

**Product name:** Cozy Puzzle  
**Platform:** Android  
**Primary technology:** Kotlin  
**Target:** Portfolio / Junior Kotlin Android project

Cozy Puzzle is a cute, cozy puzzle game that combines several puzzle gameplay modes with a persistent personal garden, collectible pets, decoration, achievements, and player statistics.

The product should feel like a small complete game rather than a technical puzzle demo.

### Product vision

> Solve cute puzzles, collect rewards, grow your garden, hatch pets, and build your own cozy little world.

---

## 2. Product Goals

### Primary goals

1. Build a polished Android application suitable for a Kotlin Junior portfolio.
2. Demonstrate strong Android fundamentals and practical Kotlin architecture.
3. Create a reusable puzzle engine shared by multiple game modes.
4. Demonstrate custom drawing, touch/gesture handling, animation, and state management.
5. Provide offline-first persistence for player progress.
6. Create a visually cohesive and cute/cozy game experience.

### Technical goals

The project should demonstrate:

- Kotlin
- MVVM / Clean Architecture
- State-driven UI
- Custom View / Canvas
- Touch and gesture handling
- Animation
- Room
- DataStore
- Coroutines
- Flow / StateFlow
- Dependency Injection
- Puzzle generation
- Pathfinding / A* where appropriate
- Undo / Redo
- Local persistence
- Deterministic daily puzzle generation

---

## 3. Target Users

### Primary user

Android developers, recruiters, and interviewers evaluating a Junior Kotlin candidate.

### Secondary user

Casual players who enjoy:

- Cute games
- Cozy aesthetics
- Short puzzle sessions
- Collecting pets
- Decorating virtual spaces

### Expected session length

Most puzzle sessions should be approximately:

- 1–5 minutes for quick puzzles
- 5–15 minutes for harder levels
- Longer optional sessions for garden customization

---

# 4. Information Architecture

The application has four main bottom-navigation tabs:

1. **Home**
2. **Garden**
3. **Shop**
4. **Profile**

```text
Cozy Puzzle
│
├── Home
│   ├── Daily Puzzle
│   ├── Basic
│   ├── Find My Cat
│   ├── Cozy Garden
│   └── Frog Pond
│
├── Garden
│   ├── View Garden
│   ├── Edit Garden
│   ├── Place Decoration
│   └── Manage Pets
│
├── Shop
│   ├── Flowers
│   ├── Trees
│   ├── Furniture
│   ├── Buildings
│   ├── Pets
│   └── Themes
│
└── Profile
    ├── Avatar
    ├── Name
    ├── Statistics
    ├── Results
    ├── Achievements
    └── Settings
```

---

# 5. Home

## 5.1 Purpose

Home is the main game entry point.

It should immediately communicate:

- What can I play?
- What is today's challenge?
- What is my current progress?
- Which game modes are available?

## 5.2 Home layout

Suggested structure:

```text
Good evening, [Name] 🌸

        Daily Puzzle
        🧩 4 × 4
        Best: 32.4s

          [ PLAY ]

Game Modes

┌──────────────┐ ┌──────────────┐
│ 🖼️           │ │ 🐱           │
│ BASIC        │ │ FIND MY CAT  │
└──────────────┘ └──────────────┘

┌──────────────┐ ┌──────────────┐
│ 🌸           │ │ 🐸           │
│ COZY GARDEN  │ │ FROG POND    │
└──────────────┘ └──────────────┘
```

## 5.3 Daily Puzzle

A daily puzzle is generated deterministically using the current date as a seed.

Example:

```kotlin
val seed = LocalDate.now().toEpochDay()
```

Requirements:

- One daily puzzle per day.
- Puzzle can be replayed.
- Best result is stored locally.
- Daily streak is tracked.
- The same seed should generate the same puzzle configuration.

Future enhancement:

- Firebase leaderboard
- Global daily ranking

---

# 6. Game Mode 1 — Basic

## 5.4 Remote Content Configuration

The **Basic** game mode should load its puzzle/content configuration from **Firebase Remote Config** instead of hard-coding the complete configuration inside the application.

Firebase Remote Config can control:

- Available Basic puzzle categories
- Built-in image IDs / asset references
- Difficulty configuration
- Board sizes
- Number of available puzzles
- Puzzle rotation/swap rules
- Hint availability
- Reward configuration
- Feature flags
- Seasonal or temporary content

Suggested flow:

```text
Firebase Remote Config
        ↓
Fetch & Activate
        ↓
FirebaseRemoteConfigDataSource
        ↓
GameConfigRepository
        ↓
Puzzle Generator
        ↓
Basic Puzzle
```

The app must provide sensible local defaults so Basic mode remains usable when:

- The device is offline.
- Firebase is unavailable.
- Remote Config fetch fails.
- Configuration is invalid or incomplete.

Example configuration:

```json
{
  "basic_puzzle_config": {
    "enabled": true,
    "available_sizes": [3, 4, 5, 6],
    "default_size": 4,
    "categories": [
      "animals",
      "nature",
      "food",
      "space"
    ],
    "allow_rotate": true,
    "allow_swap": true,
    "allow_drag": true,
    "max_hints": 3
  }
}
```

Remote Config is responsible for **content and feature configuration**, not player progress. Player-specific data such as completed levels, garden state, inventory, pets, achievements, and statistics should remain persisted locally using Room/DataStore.

### Remote Config requirements

- Fetch configuration at application startup or when appropriate.
- Use a reasonable minimum fetch interval in production.
- Cache the last successful configuration.
- Fall back to bundled defaults when remote configuration is unavailable.
- Validate configuration before applying it.
- Avoid making gameplay dependent on a successful network request.
- Keep Firebase-specific implementation behind a repository/data-source abstraction.

Suggested structure:

```text
data
├── remote
│   └── firebase
│       └── FirebaseRemoteConfigDataSource
└── repository

domain
└── config
    └── GameConfigRepository

presentation
└── home
    └── BasicGame
```

This adds a real-world Android architecture use case: **remote feature/content configuration with an offline fallback**.

## 6.1 Concept

Classic image puzzle.

The player chooses an image and solves it by manipulating puzzle pieces.

## 6.2 Image sources

Initial version:

- Built-in local images
- Gallery
- Camera (optional phase 2)

## 6.3 Difficulty

Supported board sizes:

- 3 × 3
- 4 × 4
- 5 × 5
- 6 × 6

## 6.4 Core interactions

- Tap piece
- Rotate piece
- Swap pieces
- Drag piece
- Undo
- Redo
- Hint

## 6.5 Puzzle completion

A puzzle is completed when every piece satisfies its target position and orientation.

Completion should trigger:

- Success animation
- Score calculation
- Reward calculation
- Result screen

---

# 7. Game Mode 2 — Find My Cat

## 7.1 Concept

The player helps a cute cat find its way home.

The board contains path tiles and obstacles.

Example:

```text
🐱 → 🪵 → 🌷 → 🏠
```

The player rotates or swaps tiles to create a valid route.

## 7.2 Gameplay

Player must:

1. Manipulate tiles.
2. Create a valid path.
3. Press or trigger "Walk".
4. Cat follows the calculated path.
5. Reach the house to complete the level.

## 7.3 Obstacles

Possible obstacles:

- Trees
- Rocks
- Water
- Bushes
- Fences

## 7.4 Technical highlight

Use a pathfinding algorithm such as A* to calculate the cat's route.

Possible abstraction:

```text
PathFinder
├── AStarPathFinder
└── PathResult
```

This mode should demonstrate algorithmic problem solving in addition to Android UI development.

---

# 8. Game Mode 3 — Cozy Garden

## 8.1 Concept

A puzzle mode where completed levels reward the player with seeds, coins, and experience.

The player uses rewards to grow the garden.

Example progression:

```text
🌱 → 🌿 → 🌷 → 🌸
```

## 8.2 Rewards

Completing puzzles can provide:

- Coins
- Seeds
- Pet EXP
- Decoration items
- Mystery eggs

## 8.3 Garden progression

The garden becomes visually richer as the player progresses.

Possible unlocks:

- Flowers
- Trees
- Rocks
- Paths
- Furniture
- Small houses
- Decorative objects
- Pets

---

# 9. Game Mode 4 — Frog Pond

## 9.1 Concept

The player helps a frog cross a pond using lily pads.

```text
🐸 → 🪷 → 🪷 → 🪷 → 🏡
```

The player rotates and swaps lily-pad tiles to construct a valid route.

## 9.2 Gameplay

- Rotate lily pads.
- Swap tiles.
- Create a valid path.
- Frog jumps along the path.
- Reaching the destination completes the level.

## 9.3 Failure state

If no valid path exists:

```text
🐸 → 💦

Oops! Try again 🥺
```

The player can use:

- Undo
- Hint
- Retry

---

# 10. Pet System — Hatch the Egg

Hatch the Egg is not a separate Home game mode.

It is a meta-progression system shared across the game.

## 10.1 Egg progression

```text
🥚
 ↓
🥚✨
 ↓
🐣
 ↓
🐥
 ↓
🐤
```

## 10.2 How players obtain eggs

Possible sources:

- Puzzle completion
- Daily puzzle
- Achievements
- Shop
- Special events

## 10.3 Pet collection

Possible initial pets:

- Cat
- Bunny
- Bear
- Frog
- Chick

Each pet can have:

- Name
- Species
- Level
- EXP
- Mood
- Favorite item
- Garden position

## 10.4 Pet progression

Puzzle rewards provide Pet EXP.

```text
Puzzle completed
       ↓
    Pet EXP
       ↓
   Pet Level Up
       ↓
   New animation
   / cosmetic
```

---

# 11. Garden Tab

## 11.1 Purpose

The Garden is the player's personal customizable space.

It is persistent across sessions.

## 11.2 Garden features

The player can:

- View garden
- Enter edit mode
- Place decorations
- Move decorations
- Rotate decorations
- Remove decorations
- Place pets
- Move pets
- Zoom
- Pan
- Snap objects to grid

## 11.3 Garden edit mode

Suggested UI:

```text
┌─────────────────────────┐
│                         │
│      🌳    🌷           │
│          🐱             │
│   🏡          🌸        │
│                         │
├─────────────────────────┤
│ 🌷  🌳  🪨  🏡  🪑      │
└─────────────────────────┘
```

## 11.4 Technical requirement

Garden placement should use a coordinate-based model rather than hard-coded views.

Example:

```kotlin
data class GardenItem(
    val id: String,
    val itemType: String,
    val x: Float,
    val y: Float,
    val rotation: Float,
    val scale: Float
)
```

This makes garden state easy to persist and restore.

---

# 12. Shop Tab

## 12.1 Purpose

The Shop allows players to spend in-game currency on cosmetic content.

No pay-to-win mechanics are required.

## 12.2 Categories

- Flowers
- Trees
- Buildings
- Furniture
- Decorations
- Pets
- Themes

## 12.3 Rarity

Possible rarity levels:

- Common
- Rare
- Epic
- Legendary

Example:

```text
🌷 Common
🌸 Rare
🌈 Epic
🌳✨ Legendary
```

## 12.4 Currency

Initial version:

- Coins
- Optional Gems

Coins should primarily be earned through gameplay.

---

# 13. Profile Tab

## 13.1 Profile information

Display:

- Avatar
- Player name
- Player level
- Total coins
- Total puzzles completed

## 13.2 Statistics

Example:

```text
Games Played       127
Completed           98
Win Rate            77%

Current Streak       7
```

## 13.3 Per-mode results

```text
Basic              54
Find My Cat        28
Cozy Garden        31
Frog Pond          14
```

## 13.4 Best times

```text
Basic
4 × 4      21.4 sec

Find My Cat
Level 12   38.2 sec

Frog Pond
Level 08   41.8 sec
```

## 13.5 Achievements

Initial achievements:

- First Puzzle
- First Perfect Game
- Speed Runner
- No Hint
- 5 Day Streak
- 7 Day Streak
- Garden Master
- Pet Collector
- Puzzle Master

---

# 14. Puzzle Engine

The puzzle engine is the core technical component of the application.

The game modes should reuse common puzzle functionality instead of implementing independent puzzle systems.

## 14.1 Architecture

```text
                 Puzzle Engine
                       │
        ┌──────────────┼──────────────┐
        ↓              ↓              ↓
   Basic Mode     Find My Cat     Frog Pond
        │              │              │
 Image Piece       Path Tile      Lily Pad
        │              │              │
        └──────────────┼──────────────┘
                       ↓
                Common Engine
```

## 14.2 Common functionality

- Board management
- Piece state
- Position validation
- Rotation
- Swapping
- Dragging
- Hit testing
- Undo
- Redo
- Hint
- Timer
- Move counter
- Completion detection
- Save / Resume

## 14.3 Suggested models

```kotlin
data class PuzzleConfig(
    val rows: Int,
    val columns: Int,
    val mode: PuzzleMode,
    val difficulty: Difficulty
)

data class PuzzlePiece(
    val id: Int,
    val targetPosition: Int,
    val currentPosition: Int,
    val rotation: Int
)
```

---

# 15. Puzzle State

The UI should be driven by a single state model.

```kotlin
data class PuzzleUiState(
    val board: List<PuzzlePiece>,
    val selectedPiece: Int?,
    val moves: Int,
    val elapsedTime: Long,
    val isCompleted: Boolean,
    val hintsRemaining: Int
)
```

Events can be modeled as:

```kotlin
sealed interface PuzzleEvent {
    data class PieceClicked(val id: Int) : PuzzleEvent
    data class Rotate(val id: Int) : PuzzleEvent
    data class Swap(val firstId: Int, val secondId: Int) : PuzzleEvent
    data object Hint : PuzzleEvent
    data object Undo : PuzzleEvent
    data object Redo : PuzzleEvent
}
```

---

# 16. Custom Canvas

The puzzle board should preferably use a custom Android View / Canvas for the core board.

Responsibilities:

- Draw puzzle pieces
- Draw grid
- Draw selection
- Draw highlights
- Handle touch coordinates
- Detect piece hit
- Handle drag
- Render rotation
- Render snapping
- Render completion animation

This provides a strong technical showcase for the portfolio.

---

# 17. Animation

Important animations:

## Puzzle

- Piece rotation
- Piece movement
- Piece snapping
- Selection scale
- Hint highlight

## Cat

- Walk
- Idle
- Happy
- Failed

## Frog

- Jump
- Splash
- Success

## Garden

- Plant growth
- Flower bloom
- Pet idle
- Pet interaction

## Result

- Confetti
- Reward popup
- Star animation

Animations should be lightweight and performant.

---

# 18. Hint System

Hints should have multiple levels.

### Level 1

Highlight an incorrect piece.

### Level 2

Show the correct position.

### Level 3

Automatically perform one correct move.

Hints may consume a limited number of hint points or reduce the final score.

---

# 19. Undo / Redo

The puzzle engine should maintain history.

```text
Current State
     │
     ├── Undo Stack
     │
     └── Redo Stack
```

Operations:

```kotlin
fun undo()
fun redo()
```

Every meaningful puzzle action should create a restorable state.

---

# 20. Save / Resume

Players should be able to leave a puzzle and continue later.

Persist:

- Puzzle ID
- Puzzle mode
- Board state
- Piece positions
- Piece rotations
- Elapsed time
- Move count
- Difficulty
- Creation time

Room should be used for persistent game sessions.

---

# 21. Local Persistence

Recommended:

### Room

Use Room for:

- Puzzle sessions
- Results
- Garden items
- Inventory
- Pets
- Achievements
- Statistics

### DataStore

Use DataStore for:

- Settings
- Sound enabled
- Music enabled
- Theme
- First-launch state

---

# 22. Data Model

High-level model:

```text
Player
│
├── Profile
│   ├── name
│   └── avatar
│
├── Currency
│   ├── coins
│   └── gems
│
├── Pets
│
├── Garden
│
├── Inventory
│
├── Statistics
│
├── Achievements
│
└── Puzzle Sessions
```

Suggested entities:

```text
PlayerEntity
PetEntity
GardenItemEntity
InventoryItemEntity
PuzzleSessionEntity
PuzzleResultEntity
AchievementEntity
```

---

# 23. Reward System

A completed puzzle produces rewards based on:

- Difficulty
- Completion time
- Number of moves
- Hint usage
- Perfect completion

Example:

```text
Easy
+20 coins

Medium
+40 coins

Hard
+70 coins

Perfect
+20 bonus
```

Additional rewards:

- Seeds
- Eggs
- Pet EXP
- Decoration unlocks

---

# 24. Achievement System

Achievements should be condition-based.

Example:

```kotlin
sealed interface AchievementCondition {
    data object FirstWin : AchievementCondition
    data object SpeedRunner : AchievementCondition
    data class WinStreak(val count: Int) : AchievementCondition
    data class GamesCompleted(val count: Int) : AchievementCondition
}
```

The achievement system should be extensible so new achievements can be added without changing core gameplay.

---

# 25. Difficulty System

Difficulty can be based on:

- Board size
- Number of pieces
- Number of rotations
- Number of swaps
- Obstacles
- Timer
- Special pieces

Suggested progression:

```text
Easy
3 × 3
Simple manipulation

Medium
4 × 4
Rotate + Swap

Hard
5 × 5
Mixed mechanics

Expert
6 × 6
Complex mechanics + timer
```

---

# 26. Solver

The project should include a reusable solver where appropriate.

For sliding/path-based puzzles:

```text
PuzzleSolver
├── AStarSolver
└── SolverResult
```

A* can use Manhattan distance or another domain-specific heuristic.

Example:

```text
Current State
      ↓
A* Search
      ↓
Best Path
      ↓
Hint / Auto Solve / Replay
```

This feature is optional for the MVP but highly recommended for the portfolio version.

---

# 27. Daily Puzzle

The daily puzzle should be generated from a deterministic seed.

```kotlin
val seed = LocalDate.now().toEpochDay()
```

Requirements:

- Same day → same puzzle configuration.
- Store player's best result.
- Track daily streak.
- Reset automatically on the next day.

Possible future feature:

```text
Daily Puzzle
     ↓
Firebase
     ↓
Global Leaderboard
```

---

# 28. Replay System

A future/high-value feature.

Instead of only storing the final board, record actions:

```kotlin
sealed class PuzzleAction {
    data class Rotate(val pieceId: Int) : PuzzleAction()
    data class Swap(val firstId: Int, val secondId: Int) : PuzzleAction()
    data class Move(val pieceId: Int) : PuzzleAction()
}
```

The replay system can reproduce the entire solution.

Example:

```text
00:02 Rotate piece 4
00:05 Swap 3 ↔ 7
00:08 Rotate piece 1
00:11 Move piece 5
```

---

# 29. Developer Mode

Optional developer/debug screen.

Features:

- Show piece IDs
- Show target positions
- Show collision areas
- Show current board state
- Show FPS
- Generate random puzzle
- Auto solve
- Reset progress

This is useful during development and can demonstrate engineering discipline during a technical interview.

---

# 30. Non-Functional Requirements

## Performance

- Puzzle interactions should feel responsive.
- Avoid unnecessary object allocation during Canvas drawing.
- Animations should remain smooth.
- Large images should be resized before rendering.
- Garden should support a reasonable number of objects without noticeable lag.

## Offline-first

Core gameplay should work without an internet connection.

Required offline functionality:

- Puzzle gameplay
- Garden
- Shop inventory
- Profile
- Statistics
- Achievements
- Save / Resume

Internet-dependent functionality can be added later.

## Persistence

Player progress must survive:

- App restart
- Activity recreation
- Process recreation where persisted state is required

---

# 31. Recommended Android Stack

```text
Language
Kotlin

Architecture
MVVM + Clean Architecture

UI
XML or Jetpack Compose

Core Rendering
Custom View / Canvas

State
StateFlow / Flow

Async
Coroutines

Database
Room

Preferences
DataStore

Dependency Injection
Hilt

Image Loading
Coil / Glide

Testing
JUnit
MockK
AndroidX Test

Optional
Firebase
```

If the primary objective is demonstrating existing Android Canvas experience, XML + Custom View is a strong choice for the puzzle board. Other screens can still use standard Android UI components.

---

# 32. Suggested Project Structure

```text
com.example.cozypuzzle

├── core
│   ├── puzzle
│   │   ├── engine
│   │   ├── model
│   │   ├── generator
│   │   ├── solver
│   │   └── history
│   ├── ui
│   ├── animation
│   └── common
│
├── data
│   ├── local
│   │   ├── dao
│   │   ├── entity
│   │   └── database
│   ├── repository
│   └── preferences
│
├── domain
│   ├── puzzle
│   ├── garden
│   ├── pet
│   ├── shop
│   └── profile
│
└── presentation
    ├── home
    ├── puzzle
    ├── garden
    ├── shop
    └── profile
```

---

# 33. MVP Scope

The first release should NOT implement every feature.

## Phase 1 — Core

- [ ] Four-tab navigation
- [ ] Home
- [ ] Basic puzzle
- [ ] Firebase Remote Config for Basic puzzle/content configuration
- [ ] Local fallback configuration
- [ ] Rotate
- [ ] Swap
- [ ] Drag
- [ ] Timer
- [ ] Move counter
- [ ] Win detection
- [ ] Result screen

## Phase 2 — Puzzle Engine

- [ ] Reusable puzzle engine
- [ ] Puzzle generator
- [ ] Difficulty
- [ ] Undo
- [ ] Redo
- [ ] Hint
- [ ] Save / Resume

## Phase 3 — Cute Game Modes

- [ ] Find My Cat
- [ ] Frog Pond
- [ ] Cozy Garden puzzle mode
- [ ] Cat movement
- [ ] Frog movement
- [ ] Path validation

## Phase 4 — Meta Game

- [ ] Garden tab
- [ ] Garden editor
- [ ] Decorations
- [ ] Coins
- [ ] Inventory
- [ ] Pet system
- [ ] Hatch Egg

## Phase 5 — Profile

- [ ] Avatar
- [ ] Name
- [ ] Statistics
- [ ] Per-mode results
- [ ] Achievements
- [ ] Best times

## Phase 6 — Polish

- [ ] Animations
- [ ] Confetti
- [ ] Sound effects
- [ ] Music
- [ ] Dark theme
- [ ] Empty states
- [ ] Error handling
- [ ] Performance optimization

## Phase 7 — Portfolio Extras

- [ ] A* solver
- [ ] Replay
- [ ] Daily Puzzle
- [ ] Developer mode
- [ ] Automated tests
- [ ] Firebase leaderboard

---

# 34. Out of Scope for Initial Version

To avoid excessive scope, the first version should not require:

- Real-money purchases
- Multiplayer
- Real-time PvP
- Complex backend
- User-generated online content
- Social feed
- Chat
- Large cloud image catalog
- Complex AI models

These can be considered future enhancements.

---

# 35. Success Criteria

The project is considered portfolio-ready when:

1. A new user can open the app and start a puzzle within a few taps.
2. All four Home modes have distinct gameplay identities.
3. Puzzle interaction feels smooth.
4. Puzzle state is correctly restored after leaving the app.
5. Garden changes persist after restarting the app.
6. Rewards correctly update player inventory.
7. Pets can be hatched and displayed in the garden.
8. Profile statistics reflect actual gameplay.
9. The app works without an internet connection.
10. Core puzzle logic is separated from UI.
11. At least one non-trivial algorithmic feature is implemented.
12. The project contains meaningful unit tests.

---

# 36. Portfolio / CV Value

The project should be presented as more than a puzzle game.

Key technical highlights:

- Reusable puzzle engine
- Custom Android Canvas rendering
- Touch and gesture processing
- State-driven architecture
- Kotlin Coroutines + Flow
- Room persistence
- DataStore
- Undo/Redo state history
- Deterministic puzzle generation
- A* pathfinding
- Animation system
- Offline-first architecture
- Persistent player progression
- Garden placement system
- Pet progression system

Example CV description:

> **Cozy Puzzle — Android / Kotlin**  
> Built a cozy puzzle game featuring reusable puzzle mechanics, custom Canvas rendering, gesture-based interactions, multiple puzzle modes, A* pathfinding, persistent garden/pet progression, Room-based offline storage, achievements, daily challenges, and animated game states.

---

# 37. Product Principles

The app should follow five principles:

### 1. Cute

Characters, animations, icons, and feedback should feel friendly.

### 2. Cozy

The experience should be relaxing rather than stressful.

### 3. Simple

A user should understand the gameplay without reading a long tutorial.

### 4. Rewarding

Every completed puzzle should contribute to long-term progression.

### 5. Technically solid

The cute UI should sit on top of clean, reusable, testable Kotlin architecture.

---

# 38. Final Product Vision

```text
                         COZY PUZZLE 🌸
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
       🧩                    🌳                    🛍️
      HOME                 GARDEN                 SHOP
        │                     │                     │
   ┌────┼────┐               │                Decorations
   │    │    │               │                Pets
   │    │    │               │                Themes
   🖼️   🐱   🌸  🐸          │
 Basic Cat Garden Frog       │
   │    │    │               │
   └────┴────┴──────┐        │
                    ↓        ↓
                 Rewards → Garden
                    │
                    ↓
                   🥚
                    ↓
              Hatch the Egg
                    ↓
             🐱 🐰 🐻 🐥
                    │
                    ↓
               Personal Garden
                    │
                    ↓
              👤 Profile / Results
```

**Core product loop:**

```text
Play Puzzle
    ↓
Complete Level
    ↓
Earn Coins / Seeds / EXP / Eggs
    ↓
Unlock Pets & Decorations
    ↓
Customize Garden
    ↓
Grow Collection
    ↓
Play More Puzzles
```

This loop is the foundation of the product and should remain simple throughout development.
