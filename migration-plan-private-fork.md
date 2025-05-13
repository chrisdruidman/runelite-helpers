# Migration Plan: Patch/Agent/Launcher → Private Custom RuneLite Fork

**Date:** 2025-05-13

## Overview

This plan details how to migrate from a patch/agent/launcher hybrid approach to a private custom RuneLite fork, while preserving modularity, maintainability, and stealth. Keeping the fork private is mandatory for your project.

---

## Step-by-Step Migration Plan

### 1. Prepare Your Private Fork

-   Fork the official RuneLite repository (or clone a fresh copy into your workspace).
-   Create a new branch for your customizations (e.g., `automation-main`).
-   **Set repository visibility to private** (on GitHub, GitLab, or your chosen platform).
-   Restrict access to trusted collaborators only.

### 2. Port Minimal API Additions

-   Copy any minimal API classes you’ve added (like `DummyApi`) directly into the appropriate place in your fork’s `runelite-api` and `runelite-client`.
-   Remove patching logic and stub implementations—implement the real logic directly in the client.

### 3. Move Agent Logic In-Tree

-   Move your automation modules, overlays, and services from `osrs-helper-agent` into a new package or module inside your fork (e.g., `net.runelite.client.automation`).
-   Integrate your overlay and service registry directly with the client’s plugin/module system for easier lifecycle management.

### 4. Remove Patch/Launcher Complexity

-   Remove the custom launcher and classloader logic. Use the standard RuneLite launcher for development and testing.
-   Remove patch files and patch application scripts—they’re no longer needed.

### 5. Refactor for Modularity

-   Keep each automation module (e.g., agility, mouse input, overlays) in its own package or submodule.
-   Use interfaces and service registries to keep code extensible and maintainable.
-   Document each module’s purpose and integration points.

### 6. Automate Build & Test

-   Update the Maven build to include your new modules and ensure everything builds as a single project.
-   Add basic tests for your automation logic if possible.

### 7. Upstream Sync Strategy

-   Regularly pull changes from upstream RuneLite and merge into your fork.
-   Resolve conflicts as needed, but your modular structure should help minimize pain.
-   **Keep your fork private at all times. Never push to a public repository.**

### 8. Documentation & Versioning

-   Document your fork’s structure, custom modules, and any changes to core client logic.
-   Use clear commit messages and keep a changelog for major features and fixes.
-   Note in documentation that the fork is strictly private and for personal/trusted use only.

---

## Optional: Keep Stealth/Modularity

-   Keep automation logic in a separate package and avoid touching core client code unless necessary.
-   Never distribute your fork or binaries publicly.

---

## Example Directory Structure (After Migration)

```
runelite/
  runelite-api/
    src/main/java/net/runelite/api/
      DummyApi.java
  runelite-client/
    src/main/java/net/runelite/client/
      automation/
        AgilityModule.java
        MouseInputService.java
        OverlayManager.java
      plugins/
      ...
  ...
```

---

## Summary Table

| Step                       | Description                                     |
| -------------------------- | ----------------------------------------------- |
| Prepare Private Fork       | Fork/clone RuneLite, set to private, new branch |
| Port API Additions         | Move minimal API changes into the fork          |
| Move Agent Logic In-Tree   | Integrate automation modules directly           |
| Remove Patch/Launcher      | Delete patch files and custom launcher          |
| Refactor for Modularity    | Keep code modular and extensible                |
| Automate Build & Test      | Update Maven, add tests                         |
| Upstream Sync Strategy     | Regularly merge upstream changes, keep private  |
| Documentation & Versioning | Document structure and changes, note privacy    |

---

**Note:**

-   Keeping your fork private is mandatory for stealth and security. Never share your fork or binaries publicly.
-   If you need help with any migration step, modularization, or private repo setup, just ask!
