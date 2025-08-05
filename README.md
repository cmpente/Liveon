# liveon – Rewrite Your Fate

liveon is a life simulator for Android inspired by BitLife and similar titles.  It is built entirely in Kotlin using Jetpack Compose (Material 3), MVVM architecture and Hilt dependency injection.  liveon aims to surpass competitors by offering deep stats, generational play, a vast event pool, and a professional user experience.

## Features

* **Character creation** – Fully customize your character’s name, gender and nationality; randomize base stats and traits.
* **Generational play** – When your character dies, continue the story through heirs, inheriting assets, reputation and stats.
* **Event system** – Thousands of handcrafted events with branching outcomes, stat changes and trait gains; context‑sensitive triggers; no repetition per playthrough.
* **Stats & traits** – Tracks happiness, health, intelligence, looks, karma, stress, fame, sexuality, addiction risk, fertility and finances.  Dozens of traits affect event outcomes.
* **Health system** – Separate physical and mental health bars that are impacted by events, illness, injury and therapy.
* **Pets** – Adopt, care for and mourn pets.  Pets have their own stats and ageing.
* **Crime** – Commit crimes from petty theft to serious offences with outcomes like arrest or escape; a criminal record persists and can be cleared through gameplay.
* **Relationships** – Simulate family, romance, friends, rivals and pets, each with relationship meters that change over time.
* **Careers & education** – Multiple career paths (corporate, creative, sports, criminal, politics, etc.), education options and job mechanics like interviews, promotions and strikes.
* **Assets & economy** – Own homes, vehicles, luxury goods, stocks, crypto and businesses; a marketplace with depreciation/appreciation mechanics; debt, loans and bankruptcy.
* **Save system** – Multiple save slots with autosave and manual save.  Saves store full character snapshots and can be managed from a dedicated screen.
* **Scenario builder** – Start your life from predefined scenarios (e.g. "High School Star", "Troubled Childhood") with custom stats and traits.  Scenarios are loaded from JSON and can be extended.
* **Mature content filter** – Toggle mature events (violence, drugs, adult themes) on or off via settings.  The event pool adapts accordingly.
* **Background autosave** – A WorkManager worker periodically saves progress so players never lose their story.
* **Achievements & meta** – Over 200 achievements, leaderboards, multiple save slots and a robust life journal to review and share your story.
* **Accessibility & localization** – Material 3 design with dark/light mode; WCAG‑compliant contrast; screen‑reader support; externalized strings and JSON data for easy translation.

## Project Structure

This project is organized as a multi‑module Gradle build:

```
liveon/
├── app/        # Android application with Compose UI and Hilt modules
├── data/       # Room database, DAOs and repository implementations
├── domain/     # Pure Kotlin module containing models, repositories and use cases
├── core/       # Shared utilities (e.g. Resource sealed class)
├── json_schemas.md  # Documentation of JSON data formats
└── README.md   # This file
```

### Modules

| Module | Purpose |
|---|---|
| **app** | Hosts the Android app, Compose screens, ViewModels, navigation and dependency injection bindings. |
| **data** | Implements the persistence layer using Room and loads JSON assets. Provides repository implementations for the domain layer. |
| **domain** | Defines plain Kotlin data models (Character, Event, Asset, etc.), repository interfaces and use cases (AdvanceYear, ApplyEvent, UpdateStats, CalculateInheritance). |
| **core** | Contains shared utilities such as the `Resource` sealed class for representing loading/success/error states. |

### Architecture

liveon follows Clean Architecture principles with distinct layers:

* **Domain** – Business logic and use cases operate on plain models without Android dependencies.
* **Data** – Repositories interact with Room and JSON to persist and retrieve data, mapping between entities and domain models.
* **App/UI** – Jetpack Compose screens observe `ViewModel` state flows and trigger use cases through user actions.  Navigation is handled by the Navigation Compose library.
* **Dependency Injection** – Hilt modules provide databases, DAOs, repositories and use cases.  `MainActivity` and ViewModels are annotated for injection.

## Building & Running

1. Ensure you have Android Studio Flamingo (or later) with Kotlin 1.9 and Compose support.
2. Clone the repository and open the `liveon` directory in Android Studio.
3. Sync Gradle.  The project uses Kotlin DSL build scripts and requires the Hilt plugin.
4. Create an Android device or emulator running API 24 or higher.
5. Run the **liveon** configuration.  You can explore character creation, events, assets, careers and achievements.

> **Note:** This project intentionally omits the `local.properties` file because that file contains a machine‑specific `sdk.dir` property.  When opening the project on your machine, Android Studio will prompt you to specify the location of your Android SDK.  Either set the `ANDROID_HOME` environment variable or create a `local.properties` file in the project root with a line like `sdk.dir=C\:\\Users\\yourname\\AppData\\Local\\Android\\Sdk` (on Windows) or `sdk.dir=/home/yourname/Android/Sdk` (on Linux/macOS).

## Extending the Game

* **Adding events and achievements:** Edit the JSON files under `app/src/main/assets`.  The schemas are documented in [`json_schemas.md`](json_schemas.md).  New content can be added without changing code.
* **Localization:** Provide translated versions of the JSON files and string resources (e.g. `events_es_ES.json`, `strings_fr.xml`) and load them based on the device locale.
* **New traits and stats:** Extend the `Stats` and `Trait` data classes in the domain module and update `ApplyEventUseCase` and UI accordingly.
* **UI customization:** Modify Compose theme definitions in `liveonTheme.kt` and `Type.kt` or replace fonts with those specified in the branding guide.

## License

This project is developed by liveon Games.  All original code and assets are provided under the MIT License.  Third‑party libraries are used under their respective licenses.  See individual library documentation for details.

## Acknowledgements

Thanks to the open‑source community and life simulation games that inspired liveon.  This project builds upon established patterns while introducing unique features like generational play, an advanced economy, AI‑generated events and an open scenario builder.