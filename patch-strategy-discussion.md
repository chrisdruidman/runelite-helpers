# OSRS RuneLite Agent Project: Communication & Patch Strategy Discussion

**Date:** 2025-05-13

## Key Points from Our Approach

-   **Stealth is important, but ease of development and maintainability are top priorities.**
-   **Modular code structure** is already aiding maintainability and future extensibility.
-   **Manual patching/compilation is acceptable** (you’re comfortable with this workflow).
-   **Minimal internal plugin** is a fallback, but you prefer to avoid it due to mixed stealth reports.

---

## Recommended Approach

### 1. Continue with Patch-Based Hybrid (Current Path)

-   Use a minimal, stable API surface (as you’re doing with `DummyApi`).
-   Implement all logic in the client, patching only what’s necessary.
-   Use the custom classloader to inject patched classes at runtime.
-   Keep all automation and overlays in the agent, outside the client.
-   Document and version all patches for easy reapplication after RuneLite updates.

**Why?**

-   This approach is stealthier than a plugin, as it avoids the plugin system and keeps the API surface minimal.
-   It’s modular and maintainable, as all automation logic is outside the client.
-   Manual patching/compilation is manageable and fits your workflow.

### 2. Fallback: Minimal Internal Plugin

-   Only use this if the patch-based approach becomes unmanageable or if you need more complex communication that can’t be achieved with minimal patching.
-   Keep the plugin as minimal and internal as possible to reduce detection risk.

---

## Next Steps (Automated)

1. **Automate the patch build process:**
    - Patch the client-side `DummyApi` implementation.
    - Compile it in the context of the client.
    - Copy the resulting `.class` file to the patch directory.
2. **Test the runtime patching pipeline:**
    - Launch RuneLite with the custom classloader.
    - Verify that the patched class is loaded and menu entries are retrieved.

---

## Alternative Approaches Considered

-   **Service/Plugin Bridge (Internal Plugin):** Exposes a controlled API for the agent via a RuneLite plugin. Easier to maintain, but less stealthy.
-   **Reflection-Only (No Patch):** Agent uses reflection to access client internals. Very stealthy, but fragile to obfuscation and code changes.
-   **Hybrid: Minimal Patch + Reflection:** Minimal patch to expose a bridge method, rest via reflection. Reduces patching but still requires maintenance.
-   **IPC (Inter-Process Communication):** Agent runs as a separate process and communicates via sockets/pipes. Isolated, but more complex.

---

**Summary:**

-   Continue with the patch-based hybrid approach for now, as it best balances stealth, maintainability, and ease of development for your needs.
-   Keep the minimal internal plugin as a fallback if required.
