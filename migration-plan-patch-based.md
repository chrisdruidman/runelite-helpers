# Migration Plan: Patch/Agent/Launcher → Patch-Based Modular Workflow

**Date:** 2025-05-13

## Overview

This plan details how to evolve from a patch/agent/launcher hybrid approach to a robust, maintainable, and private patch-based workflow for RuneLite automation. This approach keeps your repository small, private, and focused on patches and automation logic, while making it easy to update to new RuneLite versions.

---

## Step-by-Step Migration Plan

### 1. Prepare Your Patch-Based Workspace

-   Clone the official RuneLite repository (do not fork, to avoid GitHub visibility issues).
-   Create a private repository for your patches, agent, launcher, and documentation (e.g., `osrs-runelite-agent-helper`).
-   Add scripts for applying patches and building the client.

### 2. Track Minimal API and Client Patches

-   For every change needed in the RuneLite API or client (e.g., new hooks, minimal APIs like `DummyApi`), create a patch file using `git diff` or `git format-patch`.
-   Store all patch files in a dedicated folder (e.g., `osrs-helper-patches/`).
-   Document each patch with a changelog or README.

### 3. Keep Automation Logic Separate

-   Keep all automation, overlays, and helper logic in your own projects (e.g., `osrs-helper-agent`, `osrs-helper-launcher`).
-   Do not add automation logic directly to the RuneLite client—only patch what is necessary for your agent to function.

### 4. Patch Application Workflow

-   When setting up a new workspace or updating RuneLite:
    1. Clone or update the official RuneLite repo.
    2. Apply your patches using a script (e.g., `git apply` or `patch`).
    3. Build RuneLite as usual.
    4. Build your agent and launcher.
-   Automate this process with a script (e.g., `apply-patches.ps1`).

### 5. Minimize and Modularize Patches

-   Only patch what is absolutely necessary for your agent to work.
-   For new features (e.g., new skills, overlays), add new patch files rather than modifying existing ones when possible.
-   Keep each patch focused and well-documented.

### 6. Upstream Sync Strategy

-   When RuneLite updates, pull the latest changes, re-apply your patches, and resolve any conflicts.
-   Update your patch files as needed and document any changes.

### 7. Documentation & Versioning

-   Document your patch workflow, build steps, and any manual intervention required.
-   Use clear commit messages and keep a changelog for major features and fixes.
-   Note in documentation that the repo is strictly private and for personal/trusted use only.

---

## Example Directory Structure (Patch-Based Workflow)

```
osrs-runelite-agent-helper/
  osrs-helper-agent/
  osrs-helper-launcher/
  osrs-helper-patches/
    minimal-dummy-api.patch
    canifis-agility-hooks.patch
    ...
  apply-patches.ps1
  README.md
  ...
runelite/
  runelite-api/
  runelite-client/
  ...
```

---

## Summary Table

| Step                        | Description                                     |
| --------------------------- | ----------------------------------------------- |
| Prepare Patch Workspace     | Clone RuneLite, create private patch repo       |
| Track Minimal Patches       | Store all changes as patch files                |
| Keep Automation Separate    | All automation logic outside client             |
| Patch Application Workflow  | Scripted patch/apply/build process              |
| Minimize/Modularize Patches | Small, focused, well-documented patches         |
| Upstream Sync Strategy      | Re-apply patches on update, resolve conflicts   |
| Documentation & Versioning  | Document workflow, keep changelog, note privacy |

---

**Note:**

-   Keeping your patch repo private is mandatory for stealth and security. Never share your patches or binaries publicly.
-   If you need help automating patch application, modularizing patches, or documenting your workflow, just ask!
