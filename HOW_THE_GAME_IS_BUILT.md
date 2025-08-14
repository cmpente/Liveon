## Project Analysis: Live On Game

This project, "Live On," is a life simulation game built for Android using Kotlin and leveraging Jetpack Compose for its UI. The project exhibits a modular structure, separating concerns into distinct modules for better organization, maintainability, and scalability.

### Project Structure

The project follows a typical Android multi-module setup:

*   **`/` (Root)**: Contains project-level configuration files such as `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, and the `gradlew` wrapper scripts.
*   **`app/`**: This is the main application module. It contains the Android manifest, resources, and the primary activities/composables that compose the user interface. It depends on the `core`, `data`, and `domain` modules to access game logic and data.
*   **`core/`**: This module likely holds common utilities, helper classes, and potentially shared interfaces or base classes used across other modules. The presence of `repository` subdirectories suggests it might contain repository interfaces or common repository implementations.
*   **`data/`**: This module is responsible for data handling. It contains data sources (e.g., `EventDataSource.kt`, `GameDataSource.kt`), database implementations using Room (`LiveonDatabase.kt`, various `dao/` and `entity/` files), asset loading logic (`assets/` subdirectories and loaders), and repository implementations that interact with these data sources. It maps data models from the data layer to domain models.
*   **`domain/`**: This module encapsulates the core business logic and rules of the game. It defines the game's models (`model/` subdirectories), repositories (`repository/` subdirectories which likely contain interfaces implemented in the `data` module), and use cases (`usecase/` subdirectories) that represent specific actions or operations within the game. This module should not have any Android-specific dependencies.
*   **`content/`**: This directory appears to contain game content defined in JSON files, such as achievements (`achievements_en_life.json`, `achievements_en_rare.json`), events (`events_en_career.json`, etc.), and scenarios (`scenarios_en_challenge.json`, etc.). These files are likely processed and loaded by the `data` module's asset loaders.
*   **`docs/`**: Contains documentation files.
*   **`gradle/`**: Contains Gradle wrapper and dependency version information (`libs.versions.toml`).

### Technologies Used

*   **Kotlin:** The primary programming language used throughout the project.
*   **Jetpack Compose:** Used for building the declarative user interface in the `app` module. This is evident from the presence of Kotlin files in `app/src/main/java/com/liveongames/liveon/ui/` and files like `ActivityMain.xml` (though a Compose project primarily uses Kotlin for UI).
*   **Gradle:** The build automation tool used to manage dependencies, compile code, and build the application. The use of `build.gradle.kts` indicates Kotlin DSL for Gradle.
*   **Room Persistence Library:** Based on the files in `data/src/main/java/com/liveongames/data/db/`, Room is used for local database persistence.
*   **Dependency Injection (likely Hilt):** The `di/` packages in the `app`, `data`, and `domain` modules suggest the use of a dependency injection framework like Hilt (built on top of Dagger) for managing dependencies between different components.
*   **JSON:** Used for storing game content data in the `content/` directory.
*   **Navigation Component (for fragments initially, potentially transitioning to Compose Navigation):** While `fragment_game.xml`, `fragment_home.xml`, and `fragment_new_game.xml` suggest an initial reliance on Fragments, the increasing use of Jetpack Compose indicates a likely transition or coexistence with Compose Navigation.

### Module Interaction

The project follows a unidirectional dependency flow:

*   **`app`** depends on **`core`**, **`data`**, and **`domain`**.
*   **`data`** depends on **`core`** and **`domain`** (specifically, the domain models and repository interfaces).
*   **`domain`** depends only on **`core`**.

This structure promotes a clean separation of concerns:

*   The **`domain`** module defines the "what" of the game – the rules and logic, independent of how data is stored or presented.
*   The **`data`** module defines the "how" of data – how it's fetched, stored, and managed. It implements the repository interfaces defined in the `domain` module.
*   The **`app`** module defines the "how" of the UI – how the game is presented to the user. It interacts with the use cases defined in the `domain` module to perform actions and retrieve data, without needing to know the underlying data implementation details.

Dependency injection (likely Hilt) plays a crucial role in connecting these modules. Modules define their dependencies, and the DI framework provides the concrete implementations at runtime, primarily within the `app` module where the components are instantiated. For example, the `app` module's ViewModels (e.g., `GameViewModel.kt`) would inject use cases from the `domain` module, and the `data` module would provide the concrete repository implementations that the use cases depend on.

The content files in the `content/` directory are loaded and parsed by the asset loaders within the `data` module, which then populate the database or provide data directly to the data repositories.

This modular architecture makes the project more maintainable, testable (each module can be tested independently), and allows for easier feature development and potential future expansion to other platforms by reusing the `core`, `data`, and `domain` modules. The shift to Jetpack Compose further enhances the modern Android development approach for the UI.