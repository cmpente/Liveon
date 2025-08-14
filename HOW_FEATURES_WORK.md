# How Game Features Work

This document delves into the implementation of key game features within the Liveon project, focusing on the interaction between the `domain` and `app` modules to handle game logic, data flow, and user interactions.

## Project Structure and Module Interaction

The project follows a modular architecture, primarily separating concerns into `app`, `domain`, and `data` modules.

*   **`app` module:** This module contains the user interface (UI) built with Jetpack Compose, Android-specific components (like Activities and the Application class), and ViewModels that manage UI state and interact with the `domain` layer.
*   **`domain` module:** This module is the core of the game's business logic. It defines the game's models (e.g., `Character`, `Event`, `Crime`), repositories interfaces, and use cases. Use cases encapsulate specific actions or features in the game and orchestrate the flow of data between the UI and the data layer. This module is independent of Android.
*   **`data` module:** This module handles data sources, such as JSON assets and potentially a Room database (indicated by `LiveonDatabase` and various DAO files). It provides the concrete implementations of the repository interfaces defined in the `domain` module.

The `app` module depends on the `domain` module, and the `domain` module depends on the `data` module. This dependency flow ensures that the business logic in `domain` is not coupled to the UI or specific data source implementations.

## Key Feature Implementations

### Events

Events are a central mechanic in the game, presenting the player with choices that affect their character's stats and progression.

1.  **Data Loading:** Event data is loaded from JSON assets within the `data` module using `EventsAssetLoader` and `RawAssetReader`. This data is then mapped to `Event` models in the `domain` module by `EventMapper`.
2.  **Availability:** The `EventRepository` (implemented in `data` and defined as an interface in `domain`) provides methods to access event data. The `GetAvailableEventsUseCase` in the `domain` module likely determines which events are relevant to the player's current age and circumstances. `GetYearlyEventsUseCase` and `GetRandomEventsUseCase` are also used to retrieve specific types of events.
3.  **UI Presentation:** In the `app` module, the `EventViewModel` interacts with the event use cases to fetch available events. The `EventsScreen.kt` composable displays these events to the user, typically with descriptions and choices.
4.  **User Interaction and Logic:** When the player selects a choice for an event, the UI triggers a corresponding action in the `EventViewModel`. This ViewModel then calls the `ApplyChoiceOutcomesUseCase` or `ApplyEventUseCase` in the `domain` module. These use cases contain the logic to process the chosen outcome, updating the character's stats (via `UpdateStatsUseCase` and `UpdateCharacterUseCase`) and potentially triggering other game effects.
5.  **Game State Update:** The updated character and game state are likely persisted through the `GameRepository` and `SaveRepository` (again, interfaces in `domain`, implementations in `data`). The UI observes changes in the `GameUIState` managed by the `GameViewModel` to reflect the new state.
6.  **Event Tracking:** `MarkEventAsShownUseCase` and `RemoveEventUseCase` suggest that the game tracks which events have been presented to the player to avoid repetition or manage event sequences.

### Crime

The crime feature allows characters to engage in criminal activities with potential consequences.

1.  **Data and Logic:** Crime data is likely defined in the `domain` module's `Crime`, `CrimeType`, `CrimeOutcome`, and `CrimeRecord` models. The `CrimeRepository` (implemented in `data`) provides access to crime information.
2.  **Availability and Outcomes:** The `GetCrimesUseCase` and `GetCrimeStatsUseCase` in `domain` provide information about available crimes and the character's criminal history. The outcomes of committing a crime are handled by the `RecordCrimeUseCase`. This use case would determine success or failure based on character stats and potentially other factors, then apply the consequences defined in `CrimeOutcome` (e.g., gaining money, losing reputation, getting arrested).
3.  **UI Interaction:** The `CrimeViewModel` in the `app` module interacts with the crime use cases. The `CrimeScreen.kt` composable would likely present available crime options to the player.
4.  **Applying Consequences:** When a player chooses to commit a crime, the UI triggers the `RecordCrimeUseCase`. This use case updates the `CrimeRecord` for the character and modifies their stats accordingly using `UpdateStatsUseCase` and `UpdateCharacterUseCase`.
5.  **Clearing Record:** The `ClearCriminalRecordUseCase` suggests a mechanism for players to reduce or eliminate their character's criminal history, potentially through actions like serving time or paying fines.

### Education

The education feature allows characters to pursue various educational paths to improve their skills and unlock career opportunities.

1.  **Data Loading and Structure:** Education data, including available courses and actions, is loaded from assets (likely JSON) using `EducationAssetLoader` in the `data` module. This data is mapped to models like `Education`, `EducationCourse`, and `EducationOption` in the `domain` module. The `EducationPathMap` composable in the `app` module suggests a visual representation of educational paths.
2.  **Enrollment and Progression:** The game likely tracks the character's current education state through entities in the database (e.g., `EducationEntity`, `TermStateEntity`, `EducationActionStateEntity`) managed by `EducationDao` and `EducationActionStateDao`. Use cases would handle enrolling in courses, completing terms, and taking actions related to education.
3.  **Actions and Mini-Games:** `EducationActionDef` and models like `DialogChoice` suggest interactive elements within the education system. The presence of `MemoryMatchMiniGame`, `QuickQuizMiniGame`, and `TimingTapMiniGame` composables indicates that education might involve mini-games to determine success or outcomes.
4.  **Outcomes:** Completing educational milestones or actions would likely affect character stats (`UpdateStatsUseCase`), unlock achievements (`EducationAchievement`), and potentially open up new career options. The `GpaInfoDialog` and `GpaInfoSheet` suggest tracking and displaying academic performance.
5.  **UI Interaction:** The `EducationViewModel` in the `app` module manages the UI state related to education. `EducationSheet.kt` and `EducationPopup.kt` likely present educational options and information to the player.

### Character Progression

Character progression is a core aspect of the game, involving the evolution of a character's stats, traits, and life events over time.

1.  **Character Model:** The `Character` model in the `domain` module encapsulates the character's attributes, including `CharacterStats`, `Traits`, and potentially relationships (`Relationship`).
2.  **Stat Updates:** Character stats are primarily modified by the outcomes of events, crimes, education, and other life actions. The `UpdateStatsUseCase` is a central use case for applying these changes.
3.  **Aging and Life Events:** The `AdvanceYearUseCase` is likely responsible for progressing the game by one year. This use case would trigger yearly events (`GetYearlyEventsUseCase`), potentially introduce new opportunities or challenges, and update the character's age and related attributes.
4.  **Life Log:** The `LifeLogEntry` model suggests a mechanism for recording significant life events, providing a history of the character's journey.
5.  **Persistence:** Character data is persisted using the `CharacterRepository` (backed by `CharacterDao`) and the `SaveRepository`. `SaveGameUseCase` and `LoadGameUseCase` handle saving and loading the entire game state, including character progression.

## Data Flow Summary

In summary, user interactions in the `app` module trigger calls to ViewModels. ViewModels then execute relevant use cases in the `domain` module. These use cases encapsulate the game's logic and interact with repository interfaces defined in `domain`. The `data` module provides the concrete implementations of these repositories, accessing and modifying data from assets or the database. Changes in the game state are then reflected back to the UI through observed data streams or state management in the ViewModels, providing a clear unidirectional data flow in key parts of the architecture.