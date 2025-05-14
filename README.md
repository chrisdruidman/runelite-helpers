# RuneLite Helpers (Patch-Based Modular Workflow)

This project provides a modular, patch-based automation and helper system for Old School RuneScape using the RuneLite client. All RuneLite modifications are tracked as patch files, and all automation logic is kept outside the client source for maintainability, stealth, and ease of updates.

## Features

-   Patch-based workflow: All RuneLite changes are tracked as patch files in `osrs-helper-patches/`.
-   Modular automation: All helper and automation logic is kept in separate modules/scripts, not in the client.
-   Easy updates: Update RuneLite by pulling the latest source and re-applying patches.
-   Strict privacy: This repository and all patches must remain private.
-   **Modular registry pattern:** All modules and services are registered and constructed via a central `AgentRegistry`, which provides all shared dependencies (such as the `Client` instance).
-   **UI integration:** The sidebar panel (or any UI) must not wire dependencies into modules; it should only interact with modules via their public APIs and the registry.

## Directory Structure

```
runelite-helpers/
  osrs-helper-agent/         # Automation logic (Java)
  osrs-helper-launcher/      # Custom launcher (if used)
  osrs-helper-patches/       # All patch files and changelogs
  runelite/                  # Working copy of official RuneLite source
  steps-taken/               # Markdown summaries of completed steps
  .gitignore                 # Excludes runelite/ and build artifacts
  README.md                  # This file
  LICENSE                    # MIT License
```

## Patch Workflow

1. Make changes to the `runelite/` folder as needed for your minimal API or hooks.
2. Use `git diff` or similar to generate patch files and save them in `osrs-helper-patches/`.
3. When updating RuneLite, check out or pull the latest version in the `runelite/` folder, then re-apply your patches.
4. Document each patch and any manual steps in the patch folder.

## Modular Registry & Dependency Injection Pattern

-   All modules and services must be registered and constructed via a central `AgentRegistry`.
-   The `AgentRegistry` is responsible for holding and providing all shared dependencies (such as the `Client` instance).
-   Modules and services must access shared dependencies (like `Client`) only via the registry, never by direct injection or manual wiring from the UI or other modules.
-   The sidebar panel (or any UI) must not wire dependencies into modules; it should only interact with modules via their public APIs and the registry.
-   All module and service construction must use constructor injection with the registry, ensuring a single source of truth and proper lifecycle management.
-   This pattern ensures extensibility, testability, and maintainability across all agent logic.

## Project Rules Summary

| Area             | Rule/Instruction                                                                                               |
| ---------------- | -------------------------------------------------------------------------------------------------------------- |
| Patch Management | All RuneLite changes as patch files in patch folder                                                            |
| Automation Logic | All automation logic is kept outside the client and managed via patches and external modules/scripts as needed |
| Build/Run        | Use scripts to apply patches, build, and launch                                                                |
| Documentation    | Document every patch, step, and workflow                                                                       |
| Privacy          | Repo and patches must remain private                                                                           |
| Modularity       | All code must be modular, extensible, and well-documented                                                      |
| Registry Pattern | All shared dependencies accessed via AgentRegistry, never by direct injection or UI wiring                     |

## Privacy & Security

-   This repository is private and for personal/trusted use only.
-   Never share your patches, binaries, or automation logic publicly.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
