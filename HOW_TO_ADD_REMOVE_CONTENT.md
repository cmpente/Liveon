# Adding and Removing Content

This guide explains how to add or remove content such as achievements, events, and scenarios in the game. The game primarily uses JSON files located in the `content` directory to define its content.

## Understanding the Content Structure

The `content` directory contains JSON files that structure the game's content. Each type of content (achievements, events, scenarios) has its own JSON file(s) and a corresponding JSON schema to validate the structure.

-   `content/achievement_schema.json`: Schema for validating achievement JSON files.
-   `content/achievements_en_life.json`, `content/achievements_en_rare.json`: JSON files containing achievement definitions.
-   `content/event_schema.json`: Schema for validating event JSON files.
-   `content/events_en_career.json`, `content/events_en_childhood.json`, `content/events_en_crime.json`, `content/events_en_family.json`, `content_events_en_health.json`, `content/events_en_misc.json`, `content/events_en_relationships.json`, `content/events_en_school.json`: JSON files containing event definitions.
-   `content/scenario_schema.json`: Schema for validating scenario JSON files.
-   `content/scenarios_en_challenge.json`, `content/scenarios_en_classic.json`: JSON files containing scenario definitions.

The game loads this content using the `JsonAssetLoader` in the `app/src/main/java/com/liveongames/liveon/util/JsonAssetLoader.kt` file and processes it through asset loaders in the `data/src/main/java/com/liveongames/data/assets/` directory.

## Adding New Content

To add new content, you'll need to create or modify the relevant JSON files in the `content` directory and ensure they adhere to the corresponding JSON schema.

### 1. Identify the Content Type and File

Determine whether you are adding an achievement, event, or scenario. Find the appropriate JSON file in the `content` directory to add your new entry. You can also create a new JSON file for organization, but you'll need to ensure it's loaded by the game.

### 2. Understand the JSON Structure

Refer to the JSON schema files (`*_schema.json`) to understand the required structure and fields for the content you are adding. Each content type has specific properties (e.g., for an event, you'll need a `title`, `description`, `choices`, etc.).

### 3. Add the New Entry

Open the relevant JSON file and add a new JSON object representing your content to the existing array. Make sure your new entry follows the structure defined in the schema.

**Example: Adding a new event to `events_en_misc.json`**

Let's say the `event_schema.json` requires events to have an `id`, `title`, `description`, and a list of `choices`, where each choice has `text` and a list of `outcomes`.

```
json
[
  {
    "id": "existing_event_1",
    "title": "An Old Event",
    "description": "This is an event that was already here.",
    "choices": [
      {
        "text": "Option A",
        "outcomes": []
      }
    ]
  },
  {
    "id": "new_event_1",
    "title": "A New Discovery",
    "description": "You found something interesting!",
    "choices": [
      {
        "text": "Investigate",
        "outcomes": [
          {
            "type": "stat_change",
            "stat": "smarts",
            "amount": 5
          }
        ]
      },
      {
        "text": "Ignore it",
        "outcomes": []
      }
    ]
  }
]
```
Ensure your new entry has a unique `id` within its content type to avoid conflicts.

### 4. Validate Your JSON

It's highly recommended to validate your modified JSON file against its corresponding schema. You can use online JSON schema validators or integrate validation into your development workflow.

### 5. (Optional) Update Data Loading (if creating a new file)

If you created a new JSON file instead of modifying an existing one, you might need to update the data loading logic in the `data` module to include your new file. This typically involves modifying the asset loader class for that content type (e.g., `EventsAssetLoader`) to read your new file.

### 6. Build and Run the Game

After adding your content and validating the JSON, build and run the game to test if your new content appears as expected.

## Removing Content

To remove content, you'll simply need to locate the entry in the relevant JSON file and delete its corresponding JSON object from the array.

### 1. Identify the Content Type and File

Determine the content type (achievement, event, or scenario) you want to remove and find the JSON file containing its definition.

### 2. Locate and Remove the Entry

Open the JSON file and find the JSON object that represents the content you want to remove based on its properties (like `id`, `title`, etc.). Carefully delete the entire JSON object from the array.

**Example: Removing an event from `events_en_misc.json`**

```
json
[
  {
    "id": "existing_event_1",
    "title": "An Old Event",
    "description": "This is an event that was already here.",
    "choices": [
      {
        "text": "Option A",
        "outcomes": []
      }
    ]
  }
]
```
In this example, the entry with `"id": "new_event_1"` has been removed.

### 3. Validate Your JSON

Validate the modified JSON file against its schema to ensure you haven't introduced any syntax errors by removing the entry.

### 4. Build and Run the Game

Build and run the game to confirm that the removed content no longer appears.

## Important Considerations

-   **JSON Syntax:** Pay close attention to JSON syntax (commas, brackets, braces, quotes). A small error can prevent the entire file from being parsed correctly.
-   **Schema Adherence:** Always ensure your added or modified content strictly adheres to the defined JSON schema.
-   **Content IDs:** When adding new content, use unique and descriptive IDs.
-   **Backup:** Before making any changes to the content files, it's a good practice to create a backup of the `content` directory.
-   **Localization:** The current structure suggests English content (`_en`). If adding content for other languages, you would typically create new files (e.g., `achievements_fr_life.json`) and potentially update the loading logic to handle different languages.
-   **Code Dependencies:** Be aware that some content might be referenced or used in the game's code logic. Removing content that the code directly depends on could lead to errors. Analyze the codebase if you are unsure about removing specific entries.

By following these steps and understanding the structure of the content files, you can effectively add and remove content to customize the game.