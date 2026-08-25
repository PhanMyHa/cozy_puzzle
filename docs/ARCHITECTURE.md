# Cozy Puzzle — Architecture & Codebase Structure

## 1. Purpose

This document defines the technical architecture, package structure, file responsibilities, naming conventions, and implementation boundaries for the Cozy Puzzle Android application.

This document answers:

> **HOW should the application be implemented?**

The product requirements document answers:

> **WHAT should the application do?**

OpenCode must read both documents before implementing features:

```text
docs/
├── PRODUCT_REQUIREMENTS.md
└── ARCHITECTURE.md
```

---

# 2. Architecture Principles

The project should follow:

- Kotlin-first development
- MVVM
- Clean Architecture principles
- Feature-oriented presentation structure
- Repository pattern
- Single Responsibility Principle
- State-driven UI
- Offline-first persistence
- Dependency Injection with Hilt
- Coroutines + Flow
- Room for persistent player data
- DataStore for lightweight preferences
- Firebase Remote Config for remote game configuration
- Custom View / Canvas for puzzle and garden rendering

## Core rule

UI must not directly access:

- Room DAO
- Firebase
- DataStore
- Repository implementation
- Business logic
- Puzzle engine internals

The preferred dependency flow is:

```text
Fragment / View
      ↓
ViewModel
      ↓
UseCase
      ↓
Repository Interface
      ↓
Repository Implementation
      ↓
Data Source
 ┌────┴────┐
 ↓         ↓
Room    Firebase
```

---

# 3. High-Level Project Structure

```text
com.yourpackage.cozypuzzle
│
├── CozyPuzzleApplication.kt
│
├── di/
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   └── FirebaseModule.kt
│
├── core/
│   ├── common/
│   │   ├── Constants.kt
│   │   ├── AppResult.kt
│   │   ├── Extensions.kt
│   │   ├── DateUtils.kt
│   │   └── DispatcherProvider.kt
│   │
│   ├── ui/
│   │   ├── BaseFragment.kt
│   │   ├── BaseViewModel.kt
│   │   └── UiState.kt
│   │
│   ├── puzzle/
│   │   ├── engine/
│   │   │   ├── PuzzleEngine.kt
│   │   │   ├── PuzzleValidator.kt
│   │   │   ├── PuzzleGenerator.kt
│   │   │   └── PuzzleSolver.kt
│   │   │
│   │   ├── model/
│   │   │   ├── Puzzle.kt
│   │   │   ├── PuzzlePiece.kt
│   │   │   ├── PuzzleConfig.kt
│   │   │   ├── PuzzleAction.kt
│   │   │   ├── PuzzleMode.kt
│   │   │   └── Difficulty.kt
│   │   │
│   │   └── history/
│   │       └── PuzzleHistory.kt
│   │
│   └── customview/
│       ├── PuzzleBoardView.kt
│       └── GardenCanvasView.kt
│
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   └── CozyPuzzleDatabase.kt
│   │   │
│   │   ├── dao/
│   │   │   ├── PlayerDao.kt
│   │   │   ├── PuzzleSessionDao.kt
│   │   │   ├── PuzzleResultDao.kt
│   │   │   ├── GardenItemDao.kt
│   │   │   ├── PetDao.kt
│   │   │   ├── InventoryDao.kt
│   │   │   └── AchievementDao.kt
│   │   │
│   │   └── entity/
│   │       ├── PlayerEntity.kt
│   │       ├── PuzzleSessionEntity.kt
│   │       ├── PuzzleResultEntity.kt
│   │       ├── GardenItemEntity.kt
│   │       ├── PetEntity.kt
│   │       ├── InventoryItemEntity.kt
│   │       └── AchievementEntity.kt
│   │
│   ├── remote/
│   │   └── firebase/
│   │       ├── FirebaseRemoteConfigDataSource.kt
│   │       └── RemoteConfigKeys.kt
│   │
│   ├── preferences/
│   │   └── AppPreferences.kt
│   │
│   └── repository/
│       ├── PuzzleRepositoryImpl.kt
│       ├── PlayerRepositoryImpl.kt
│       ├── GardenRepositoryImpl.kt
│       ├── PetRepositoryImpl.kt
│       └── ConfigRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── Player.kt
│   │   ├── Pet.kt
│   │   ├── GardenItem.kt
│   │   ├── ShopItem.kt
│   │   └── Achievement.kt
│   │
│   ├── repository/
│   │   ├── PuzzleRepository.kt
│   │   ├── PlayerRepository.kt
│   │   ├── GardenRepository.kt
│   │   ├── PetRepository.kt
│   │   └── ConfigRepository.kt
│   │
│   └── usecase/
│       ├── puzzle/
│       ├── garden/
│       ├── pet/
│       ├── shop/
│       └── profile/
│
└── presentation/
    ├── main/
    │   ├── MainActivity.kt
    │   └── MainViewModel.kt
    │
    ├── home/
    │   ├── HomeFragment.kt
    │   ├── HomeViewModel.kt
    │   ├── HomeUiState.kt
    │   ├── HomeEvent.kt
    │   └── adapter/
    │       └── GameModeAdapter.kt
    │
    ├── puzzle/
    │   ├── PuzzleFragment.kt
    │   ├── PuzzleViewModel.kt
    │   ├── PuzzleUiState.kt
    │   ├── PuzzleEvent.kt
    │   └── adapter/
    │
    ├── garden/
    │   ├── GardenFragment.kt
    │   ├── GardenViewModel.kt
    │   ├── GardenUiState.kt
    │   ├── GardenEvent.kt
    │   └── adapter/
    │
    ├── shop/
    │   ├── ShopFragment.kt
    │   ├── ShopViewModel.kt
    │   ├── ShopUiState.kt
    │   ├── ShopEvent.kt
    │   └── adapter/
    │
    └── profile/
        ├── ProfileFragment.kt
        ├── ProfileViewModel.kt
        ├── ProfileUiState.kt
        ├── ProfileEvent.kt
        └── adapter/
```

---

# 4. Application Layer

## `CozyPuzzleApplication.kt`

Responsibilities:

- Application entry point.
- Hilt application configuration.
- Application-wide initialization if required.

Do not put:

- Gameplay logic.
- Database queries.
- Firebase business logic.
- Player state.

---

# 5. Dependency Injection

## `di/AppModule.kt`

Provides general application dependencies.

Examples:

- DispatcherProvider
- Shared utilities
- General services

## `di/DatabaseModule.kt`

Provides:

- Room database
- DAO instances

## `di/FirebaseModule.kt`

Provides:

- Firebase Remote Config
- Firebase-related dependencies

Firebase initialization should not be scattered across Fragments or ViewModels.

---

# 6. Core Common

## `core/common/Constants.kt`

Contains truly global constants.

Examples:

```kotlin
const val DEFAULT_GRID_SIZE = 4
const val MAX_HINTS = 3
```

Do not put feature-specific constants here.

---

## `core/common/AppResult.kt`

Represents generic operation results.

Example:

```kotlin
sealed interface AppResult<out T> {

    data class Success<T>(
        val data: T
    ) : AppResult<T>

    data class Error(
        val throwable: Throwable
    ) : AppResult<Nothing>
}
```

---

## `core/common/Extensions.kt`

Contains small reusable Kotlin/Android extension functions.

Examples:

```kotlin
fun View.visible()
fun View.gone()
fun Long.formatDuration()
```

Do not create a generic `Utils.kt`.

---

## `core/common/DateUtils.kt`

Contains date/time-related utilities.

Examples:

- Daily puzzle date key
- Date formatting
- Streak date comparison

---

## `core/common/DispatcherProvider.kt`

Abstracts Coroutine dispatchers to make business logic testable.

Example:

```kotlin
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}
```

---

# 7. Core UI

## `core/ui/BaseFragment.kt`

Only shared Fragment functionality.

Examples:

- View binding helpers
- Common lifecycle helpers
- Common navigation helpers

Do not put feature-specific logic here.

---

## `core/ui/BaseViewModel.kt`

Optional base ViewModel for shared behavior.

Do not create a giant base ViewModel containing unrelated application logic.

---

## `core/ui/UiState.kt`

Contains generic UI state abstractions if needed.

Feature-specific state should remain inside its feature package.

---

# 8. Puzzle Core

The puzzle engine is one of the most important technical components.

It must be independent from Android UI as much as possible.

---

## `PuzzleEngine.kt`

Responsible for puzzle operations.

Examples:

- Rotate piece
- Swap pieces
- Move piece
- Apply action
- Generate next state

It must not:

- Access Room
- Access Firebase
- Navigate
- Show Toast
- Access Fragment
- Update UI directly

---

## `PuzzleValidator.kt`

Responsible for checking puzzle validity.

Examples:

- Is puzzle solved?
- Is board configuration valid?
- Is a path valid?
- Is a sliding puzzle solvable?

---

## `PuzzleGenerator.kt`

Responsible for generating puzzle states.

Responsibilities:

- Generate board
- Shuffle pieces
- Apply rotations
- Generate deterministic puzzle from seed
- Generate difficulty-based configuration

It must be deterministic when given the same seed and configuration.

---

## `PuzzleSolver.kt`

Abstraction for puzzle solving.

Example:

```kotlin
interface PuzzleSolver {
    suspend fun solve(puzzle: Puzzle): List<PuzzleAction>
}
```

Specific algorithms can later implement this interface.

Example:

```text
AStarPuzzleSolver
```

---

# 9. Puzzle Models

## `Puzzle.kt`

Represents a complete puzzle state.

## `PuzzlePiece.kt`

Represents one piece.

Possible fields:

```kotlin
data class PuzzlePiece(
    val id: Int,
    val targetPosition: Int,
    val currentPosition: Int,
    val rotation: Int
)
```

## `PuzzleConfig.kt`

Configuration required to generate a puzzle.

```kotlin
data class PuzzleConfig(
    val rows: Int,
    val columns: Int,
    val mode: PuzzleMode,
    val difficulty: Difficulty
)
```

## `PuzzleMode.kt`

Use an enum for the four gameplay modes.

```kotlin
enum class PuzzleMode {
    BASIC,
    FIND_MY_CAT,
    COZY_GARDEN,
    FROG_POND
}
```

## `Difficulty.kt`

```kotlin
enum class Difficulty {
    EASY,
    MEDIUM,
    HARD,
    EXPERT
}
```

## `PuzzleAction.kt`

Represents user actions for history and replay.

```kotlin
sealed interface PuzzleAction {

    data class Rotate(
        val pieceId: Int
    ) : PuzzleAction

    data class Swap(
        val firstId: Int,
        val secondId: Int
    ) : PuzzleAction

    data class Move(
        val pieceId: Int,
        val position: Int
    ) : PuzzleAction
}
```

---

# 10. Puzzle History

## `PuzzleHistory.kt`

Responsible for:

- Undo stack
- Redo stack
- Restoring previous puzzle states

Public operations:

```kotlin
fun undo()
fun redo()
fun canUndo(): Boolean
fun canRedo(): Boolean
```

Do not put history logic inside Fragment.

---

# 11. Custom Views

## `PuzzleBoardView.kt`

Responsible only for rendering and user interaction.

Responsibilities:

- Draw puzzle board
- Draw puzzle pieces
- Draw selection
- Draw highlights
- Detect touch
- Handle drag
- Render animations

The view can expose callbacks:

```kotlin
var onPieceClicked: ((Int) -> Unit)? = null
var onPieceDragged: ((Int, Float, Float) -> Unit)? = null
```

The view must not:

- Access ViewModel
- Access Repository
- Access Room
- Access Firebase
- Calculate rewards
- Navigate

---

## `GardenCanvasView.kt`

Responsible for:

- Drawing garden
- Drawing garden items
- Rendering pets
- Selecting items
- Moving items
- Rotating items
- Zoom/pan interaction

Garden business logic remains outside the View.

---

# 12. Data Layer

The data layer is responsible for external data sources.

---

# 13. Room Database

## `CozyPuzzleDatabase.kt`

Defines Room database and entities.

No business logic.

---

# 14. DAOs

DAOs only perform database operations.

## `PlayerDao.kt`

Player queries.

## `PuzzleSessionDao.kt`

Save/resume puzzle sessions.

## `PuzzleResultDao.kt`

Puzzle results and best times.

## `GardenItemDao.kt`

Garden decoration positions.

## `PetDao.kt`

Pet persistence.

## `InventoryDao.kt`

Owned shop items.

## `AchievementDao.kt`

Achievement state.

DAO classes must not:

- Calculate rewards
- Validate gameplay
- Access Firebase
- Contain UI logic

---

# 15. Room Entities

Entities represent database storage.

Examples:

```text
PlayerEntity
PuzzleSessionEntity
PuzzleResultEntity
GardenItemEntity
PetEntity
InventoryItemEntity
AchievementEntity
```

Entities should not be directly exposed to the UI.

Map them to domain models.

---

# 16. Firebase Remote Config

Basic puzzle configuration must be remotely configurable.

Architecture:

```text
Firebase Remote Config
        ↓
FirebaseRemoteConfigDataSource
        ↓
ConfigRepositoryImpl
        ↓
ConfigRepository
        ↓
GetBasicPuzzleConfigUseCase
        ↓
ViewModel
```

## `FirebaseRemoteConfigDataSource.kt`

Responsible for:

- Fetching Remote Config
- Activating configuration
- Reading values
- Parsing configuration
- Handling Firebase errors

It must not update UI.

---

## `RemoteConfigKeys.kt`

Centralizes Remote Config keys.

Example:

```kotlin
object RemoteConfigKeys {
    const val BASIC_PUZZLE_CONFIG = "basic_puzzle_config"
}
```

Never scatter Firebase key strings throughout the project.

---

# 17. Remote Config Fallback

Remote Config is not the source of truth for player data.

If Firebase fails:

```text
Firebase
   ↓ failure
Cached config
   ↓ failure
Bundled default config
```

Basic mode must still work offline.

Player data remains local:

```text
Room
├── Player
├── Progress
├── Garden
├── Pets
├── Inventory
└── Results
```

---

# 18. Preferences

## `AppPreferences.kt`

Use DataStore for lightweight settings.

Examples:

- Sound enabled
- Music enabled
- Selected theme
- First launch
- Onboarding completed

Do not store large structured game data here.

---

# 19. Repository Layer

Repository interfaces belong in `domain/repository`.

Implementations belong in `data/repository`.

Example:

```text
domain/repository/ConfigRepository.kt
                 ↑
                 │
data/repository/ConfigRepositoryImpl.kt
```

Repositories decide how data is obtained.

Example:

```text
Get Basic Config
       ↓
ConfigRepository
       ↓
Remote Config
       ↓
Fallback
```

ViewModels must depend on interfaces, not implementation classes.

---

# 20. Domain Models

Domain models represent business objects.

Examples:

```text
Player
Pet
GardenItem
ShopItem
Achievement
```

They should be independent from:

- Room
- Firebase
- Android Views

Example:

```kotlin
data class Pet(
    val id: String,
    val name: String,
    val level: Int,
    val experience: Int,
    val species: PetSpecies
)
```

---

# 21. Use Cases

Each UseCase should have one responsibility.

Do not create a huge class such as:

```text
PuzzleUseCase.kt
```

containing 20 unrelated methods.

Recommended structure:

```text
domain/usecase/
│
├── puzzle/
│   ├── GeneratePuzzleUseCase.kt
│   ├── RotatePieceUseCase.kt
│   ├── SwapPiecesUseCase.kt
│   ├── CheckPuzzleCompletedUseCase.kt
│   ├── UndoPuzzleUseCase.kt
│   ├── RedoPuzzleUseCase.kt
│   ├── GetPuzzleHintUseCase.kt
│   ├── SavePuzzleSessionUseCase.kt
│   └── ResumePuzzleUseCase.kt
│
├── garden/
│   ├── GetGardenUseCase.kt
│   ├── PlaceGardenItemUseCase.kt
│   ├── MoveGardenItemUseCase.kt
│   ├── RotateGardenItemUseCase.kt
│   └── RemoveGardenItemUseCase.kt
│
├── pet/
│   ├── GetPetsUseCase.kt
│   ├── HatchEggUseCase.kt
│   └── AddPetExperienceUseCase.kt
│
├── shop/
│   ├── GetShopItemsUseCase.kt
│   └── PurchaseItemUseCase.kt
│
└── profile/
    ├── GetProfileUseCase.kt
    ├── GetStatisticsUseCase.kt
    └── GetAchievementsUseCase.kt
```

---

# 22. Presentation Layer

Presentation is organized by feature.

Each main feature should follow:

```text
Feature
├── Fragment
├── ViewModel
├── UiState
├── Event
└── Adapter
```

---

# 23. Main Screen

## `MainActivity.kt`

Responsible for:

- Hosting navigation
- Bottom navigation
- Main app-level UI
- Navigation between four main tabs

It should not contain feature business logic.

Tabs:

```text
Home
Garden
Shop
Profile
```

---

## `MainViewModel.kt`

Only needed if MainActivity has app-level state.

Examples:

- Global player state
- App initialization state

Do not put puzzle logic here.

---

# 24. Home Feature

```text
presentation/home/
├── HomeFragment.kt
├── HomeViewModel.kt
├── HomeUiState.kt
├── HomeEvent.kt
└── adapter/
    └── GameModeAdapter.kt
```

## `HomeFragment.kt`

Responsibilities:

- Render Home UI
- Observe HomeUiState
- Send HomeEvent
- Navigate to puzzle

Forbidden:

- Firebase
- Room
- Puzzle generation
- Reward calculation

---

## `HomeViewModel.kt`

Responsibilities:

- Receive UI events
- Call Home-related UseCases
- Expose StateFlow<HomeUiState>

Example dependency:

```kotlin
class HomeViewModel(
    private val getHomeDataUseCase: GetHomeDataUseCase
) : BaseViewModel()
```

---

## `HomeUiState.kt`

Contains only state required by Home UI.

Example:

```kotlin
data class HomeUiState(
    val playerName: String = "",
    val dailyPuzzle: DailyPuzzleUiModel? = null,
    val gameModes: List<GameModeUiModel> = emptyList(),
    val isLoading: Boolean = false
)
```

---

## `HomeEvent.kt`

Use sealed interface.

```kotlin
sealed interface HomeEvent {

    data object DailyPuzzleClicked : HomeEvent

    data class GameModeClicked(
        val mode: PuzzleMode
    ) : HomeEvent
}
```

---

# 25. Puzzle Feature

```text
presentation/puzzle/
├── PuzzleFragment.kt
├── PuzzleViewModel.kt
├── PuzzleUiState.kt
├── PuzzleEvent.kt
└── adapter/
```

## `PuzzleFragment.kt`

Responsible for:

- Binding PuzzleBoardView
- Observing PuzzleUiState
- Sending PuzzleEvent
- Showing result navigation

No puzzle rules inside Fragment.

---

## `PuzzleViewModel.kt`

Responsible for:

- Starting puzzle
- Handling puzzle events
- Calling puzzle UseCases
- Updating UiState
- Saving progress
- Triggering completion state

It should not directly access DAO or Firebase.

---

## `PuzzleUiState.kt`

Example:

```kotlin
data class PuzzleUiState(
    val puzzle: Puzzle? = null,
    val selectedPieceId: Int? = null,
    val moves: Int = 0,
    val elapsedTime: Long = 0L,
    val hintsRemaining: Int = 0,
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false
)
```

---

## `PuzzleEvent.kt`

```kotlin
sealed interface PuzzleEvent {

    data class PieceClicked(
        val pieceId: Int
    ) : PuzzleEvent

    data class RotatePiece(
        val pieceId: Int
    ) : PuzzleEvent

    data class SwapPieces(
        val firstId: Int,
        val secondId: Int
    ) : PuzzleEvent

    data object Undo : PuzzleEvent
    data object Redo : PuzzleEvent
    data object Hint : PuzzleEvent
    data object Pause : PuzzleEvent
    data object Resume : PuzzleEvent
}
```

---

# 26. Garden Feature

```text
presentation/garden/
├── GardenFragment.kt
├── GardenViewModel.kt
├── GardenUiState.kt
├── GardenEvent.kt
└── adapter/
```

## GardenFragment

Responsible for:

- Display garden
- Connect GardenCanvasView
- Observe state
- Send edit events

## GardenViewModel

Responsible for:

- Loading garden
- Moving items
- Rotating items
- Placing items
- Removing items
- Saving garden state

## GardenUiState

Contains:

```kotlin
data class GardenUiState(
    val items: List<GardenItem> = emptyList(),
    val selectedItemId: String? = null,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false
)
```

## GardenEvent

```kotlin
sealed interface GardenEvent {

    data object ToggleEditMode : GardenEvent

    data class SelectItem(
        val id: String
    ) : GardenEvent

    data class MoveItem(
        val id: String,
        val x: Float,
        val y: Float
    ) : GardenEvent

    data class RotateItem(
        val id: String,
        val rotation: Float
    ) : GardenEvent

    data class RemoveItem(
        val id: String
    ) : GardenEvent
}
```

---

# 27. Shop Feature

```text
presentation/shop/
├── ShopFragment.kt
├── ShopViewModel.kt
├── ShopUiState.kt
├── ShopEvent.kt
└── adapter/
```

Responsibilities:

- Display items
- Filter categories
- Show price
- Handle purchase
- Update inventory

Purchase logic must be handled by a UseCase.

The Fragment must never directly modify coins.

---

# 28. Profile Feature

```text
presentation/profile/
├── ProfileFragment.kt
├── ProfileViewModel.kt
├── ProfileUiState.kt
├── ProfileEvent.kt
└── adapter/
```

Profile should display:

- Avatar
- Name
- Level
- Statistics
- Results
- Achievements
- Best times

Profile should be read-oriented.

Changes such as avatar/name updates should use dedicated UseCases.

---

# 29. Sealed Classes / Sealed Interfaces

Use sealed types for finite event/result/state variants.

Recommended:

```text
*Event.kt
PuzzleAction.kt
AppResult.kt
NavigationEvent.kt (if needed)
```

Do not create sealed classes for everything.

Use `enum class` for simple fixed categories:

```text
PuzzleMode
Difficulty
PetSpecies
ItemRarity
```

Use `data class` for structured state/data.

---

# 30. Companion Object Rules

Use `companion object` only for class-local constants or static-like helper behavior.

Allowed:

```kotlin
class PuzzleBoardView : View {

    companion object {
        private const val DEFAULT_PADDING = 16f
    }
}
```

Avoid:

```text
❌ Global mutable state
❌ Repository singleton
❌ Player singleton
❌ Firebase instance
❌ Database instance
```

Hilt should manage dependencies.

---

# 31. Utils Rules

Never create:

```text
Utils.kt
Helper.kt
CommonHelper.kt
EverythingUtils.kt
```

Instead, create focused files:

```text
Extensions.kt
DateUtils.kt
ImageUtils.kt
NumberUtils.kt
```

A utility should be:

- Small
- Stateless
- Reusable
- Independent from feature business logic

If logic belongs to a domain, it should be a UseCase or domain service instead of a utility.

---

# 32. Adapters

RecyclerView adapters should be responsible for:

- Binding item data
- Handling item click callbacks

Adapters must not:

- Call Room
- Call Firebase
- Modify player coins
- Execute business logic
- Navigate directly

Example:

```kotlin
class GameModeAdapter(
    private val onClick: (PuzzleMode) -> Unit
)
```

---

# 33. Navigation

Navigation should be controlled from the presentation layer.

Recommended:

```text
MainActivity
    ↓
NavHost
    ↓
Fragments
```

Fragments can request navigation after observing ViewModel state/events.

Business logic must not know about Android navigation classes.

---

# 34. UI State Rules

Use `StateFlow` for screen state.

Example:

```kotlin
private val _uiState = MutableStateFlow(PuzzleUiState())
val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()
```

The UI observes:

```text
StateFlow
   ↓
Fragment
   ↓
Render
```

The Fragment should not manually maintain duplicated business state.

---

# 35. One-Way Data Flow

The preferred flow is:

```text
User Action
     ↓
Event
     ↓
ViewModel
     ↓
UseCase
     ↓
Repository
     ↓
New State
     ↓
StateFlow
     ↓
UI
```

Example:

```text
User taps Rotate
        ↓
PuzzleEvent.RotatePiece
        ↓
PuzzleViewModel
        ↓
RotatePieceUseCase
        ↓
PuzzleEngine
        ↓
PuzzleUiState updated
        ↓
PuzzleBoardView redraws
```

---

# 36. Basic Mode + Firebase Remote Config

Basic mode is special because its configuration comes from Firebase Remote Config.

Flow:

```text
Firebase Remote Config
        ↓
FirebaseRemoteConfigDataSource
        ↓
ConfigRepositoryImpl
        ↓
ConfigRepository
        ↓
GetBasicPuzzleConfigUseCase
        ↓
PuzzleViewModel
        ↓
PuzzleGenerator
        ↓
Basic Puzzle
```

Remote configuration can define:

```json
{
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
```

Requirements:

- Firebase errors must not break Basic mode.
- Bundled defaults must exist.
- Last successful configuration may be cached.
- Remote Config values must be validated.
- Firebase implementation must remain inside the data layer.

---

# 37. Player Data Ownership

Player-specific data belongs to local persistence.

```text
Room
├── Player
├── Puzzle Sessions
├── Puzzle Results
├── Garden
├── Pets
├── Inventory
└── Achievements
```

DataStore:

```text
DataStore
├── Sound
├── Music
├── Theme
└── Onboarding
```

Firebase Remote Config:

```text
Remote Config
├── Game configuration
├── Feature flags
├── Basic puzzle configuration
├── Reward configuration
└── Seasonal configuration
```

Do not store player progress in Remote Config.

---

# 38. Empty File / Skeleton Requirement

When initially creating the project structure, OpenCode should create all planned files with minimal compilable skeletons.

Do not implement full business logic during the structure phase.

Example:

```kotlin
class HomeViewModel : ViewModel()
```

Example:

```kotlin
class HomeFragment : Fragment()
```

Example:

```kotlin
data class HomeUiState(
    val isLoading: Boolean = false
)
```

The purpose of this phase is to verify:

- Package structure
- Dependencies
- Navigation
- DI
- Room setup
- Firebase setup
- Compilation

---

# 39. Implementation Phases

## Phase 0 — Project Skeleton

Create:

- Project
- Packages
- Empty classes
- Navigation
- Hilt
- Room foundation
- DataStore foundation
- Firebase Remote Config foundation

No gameplay.

Acceptance:

```text
./gradlew assembleDebug
```

must succeed.

---

## Phase 1 — Main Navigation

Implement:

- MainActivity
- Bottom navigation
- Home
- Garden
- Shop
- Profile

All screens may initially contain placeholder UI.

---

## Phase 2 — Puzzle Engine

Implement:

- Puzzle models
- PuzzleEngine
- PuzzleValidator
- PuzzleGenerator
- PuzzleHistory
- Rotate
- Swap
- Drag
- Undo
- Redo
- Completion detection

No garden/pet/shop yet.

---

## Phase 3 — Basic Mode

Implement:

- Firebase Remote Config
- Basic puzzle configuration
- Local fallback
- Image puzzle
- Difficulty
- Timer
- Hint
- Result screen

---

## Phase 4 — Find My Cat

Implement:

- Path tiles
- Obstacles
- Path validation
- A* pathfinding
- Cat movement animation

---

## Phase 5 — Frog Pond

Implement:

- Lily pad tiles
- Path validation
- Frog movement
- Water failure state
- Success animation

---

## Phase 6 — Cozy Garden

Implement:

- Garden Canvas
- Place item
- Move item
- Rotate item
- Remove item
- Save garden to Room

---

## Phase 7 — Pet System

Implement:

- Egg
- Hatch
- Pet collection
- Pet EXP
- Pet level
- Pet placement in garden

---

## Phase 8 — Shop

Implement:

- Categories
- Item list
- Currency
- Purchase
- Inventory

---

## Phase 9 — Profile

Implement:

- Avatar
- Name
- Statistics
- Results
- Best times
- Achievements

---

## Phase 10 — Polish

Implement:

- Animations
- Sound
- Music
- Confetti
- Empty states
- Error states
- Loading states
- Performance optimization

---

# 40. OpenCode Instructions

OpenCode must follow these rules.

## Rule 1 — Read documentation first

Before implementing anything:

```text
Read:
docs/PRODUCT_REQUIREMENTS.md
docs/ARCHITECTURE.md
```

Understand both before changing code.

---

## Rule 2 — Do not implement everything at once

Follow the implementation phases in this document.

Only implement the requested phase unless explicitly instructed otherwise.

---

## Rule 3 — Respect file responsibility

Do not move logic into a file just because it is convenient.

Examples:

```text
❌ Firebase call inside Fragment
❌ Room query inside ViewModel
❌ Business logic inside Adapter
❌ Puzzle logic inside Custom View
❌ Navigation inside UseCase
❌ Reward calculation inside Fragment
```

---

## Rule 4 — Avoid giant classes

Do not create:

```text
❌ MainUtils.kt
❌ PuzzleManager.kt with all features
❌ AppViewModel.kt with all state
❌ GodRepository.kt
❌ GodUseCase.kt
```

Prefer small classes with one responsibility.

---

## Rule 5 — Do not create unnecessary abstractions

Use abstractions where they provide real value.

Avoid creating interfaces for every single class without a reason.

Good candidates:

- Repository interfaces
- PuzzleSolver
- DispatcherProvider
- Data sources where testing/alternative implementations matter

---

## Rule 6 — Keep business logic testable

Core puzzle logic should not depend on Android classes.

This should be possible:

```text
PuzzleEngine
     ↓
JUnit test
```

without requiring an Android device.

---

## Rule 7 — Do not break existing features

Before changing shared code:

1. Check all usages.
2. Make the smallest safe change.
3. Run tests/build.
4. Report affected files.

---

## Rule 8 — Compile after structural changes

At minimum:

```bash
./gradlew assembleDebug
```

For relevant logic:

```bash
./gradlew test
```

---

# 41. Testing Structure

Tests should mirror important business logic.

Suggested:

```text
app/src/test/
└── com/yourpackage/cozypuzzle/
    ├── puzzle/
    │   ├── PuzzleEngineTest.kt
    │   ├── PuzzleValidatorTest.kt
    │   ├── PuzzleGeneratorTest.kt
    │   └── PuzzleSolverTest.kt
    │
    ├── garden/
    └── pet/
```

High-priority tests:

- Puzzle completion detection
- Rotation
- Swap
- Undo / Redo
- Puzzle generation
- Puzzle solvability
- A* pathfinding
- Reward calculation
- Pet EXP
- Garden item placement

---

# 42. Definition of Done for a Feature

A feature is not considered complete merely because the UI works.

A feature should have:

- UI
- ViewModel
- UiState
- Events where needed
- UseCase where business logic exists
- Repository interaction where data is required
- Proper persistence where needed
- Error handling
- Loading state where appropriate
- Empty state where appropriate
- Unit tests for important business logic
- No architecture violations

---

# 43. Final Architecture

```text
                         Presentation
                              │
          ┌───────────────────┼───────────────────┐
          ↓                   ↓                   ↓
      Fragment             ViewModel            View
          │                   │                   │
          └──────────────┬────┘                   │
                         ↓                        │
                      UiState                     │
                         ↑                        │
                         │                        │
                      UseCase                     │
                         ↓                        │
                   Repository Interface           │
                         ↓                        │
                Repository Implementation         │
                    ┌────┴────┐                   │
                    ↓         ↓                   │
                  Room     Firebase               │
                                                 │
                    Custom Canvas ←───────────────┘
                         │
                         ↓
                  User interaction
                         │
                         ↓
                       Event
                         │
                         └────────→ ViewModel
```

---

# 44. Final Rule

The project should remain easy to understand for a Junior Android developer.

Prefer:

```text
Small class
Small responsibility
Clear data flow
Testable business logic
```

over:

```text
Clever abstraction
Deep inheritance
Huge utility classes
God objects
Hidden side effects
```

The goal is not to create the most complicated architecture possible.

The goal is to create a **clean, maintainable, understandable Android application that demonstrates strong Kotlin engineering skills**.
