# RuneLite Helpers (Patch-Based Modular Workflow)

This project provides a modular, patch-based automation and helper system for Old School RuneScape using the RuneLite client. All RuneLite modifications are tracked as patch files, and all automation logic is kept outside the client source for maintainability, stealth, and ease of updates.

## Features

-   Patch-based workflow: All RuneLite changes are tracked as patch files in `osrs-helper-patches/`.
-   Modular automation: All helper and automation logic is kept in separate modules/scripts, not in the client.
-   Easy updates: Update RuneLite by pulling the latest source and re-applying patches.
-   Strict privacy: This repository and all patches must remain private.

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

## Privacy & Security

-   This repository is private and for personal/trusted use only.
-   Never share your patches, binaries, or automation logic publicly.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
