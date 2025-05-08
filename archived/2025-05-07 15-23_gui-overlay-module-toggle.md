COMPLETED - 2025-05-07 15:23

## Step: Add Minimal GUI Overlay for Runtime Module Toggling

-   Designed and implemented a minimal always-on-top Swing overlay (`OverlayController`) for toggling agent modules on/off at runtime.
-   The overlay lists all registered modules with ON/OFF toggle buttons, allowing the user to enable or disable modules while the game is running.
-   Integrated the overlay into the agent initialization, so it launches automatically and only runs modules that are enabled in the overlay.
-   This approach avoids in-game detection risks and provides user-friendly, real-time control over automation modules.
-   The system remains modular and ready for further extension (e.g., more overlay features, hotkeys, or dynamic updates).
