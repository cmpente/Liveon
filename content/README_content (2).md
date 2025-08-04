# AltLife Content Packs & Thematic Structure

## Themed Content Packs
- All game content is organized by logical category for easy expansion and localization.
- Event packs: `events_en_[theme].json` (e.g., childhood, career, health, crime, etc.)
- Scenario packs: `scenarios_en_[theme].json` (e.g., classic, challenge)
- Achievement packs: `achievements_en_[theme].json` (e.g., life, rare)

## How To Add or Localize Content
- Add new packs by dropping more files in `/assets/` (matching the pattern)
- For new languages: `events_fr_childhood.json`, etc.

## Loader Code
- The engine will auto-load and merge all matching packs at runtime.

## Modding/Expansion
- Anyone can add new event, scenario, or achievement packs—no code changes required.
