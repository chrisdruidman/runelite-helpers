COMPLETED - 2025-05-08 16:27

Agent: GitHub Copilot (Mr. Gippity)

## Step: Decouple overlay module toggling with listener and registry integration

-   Created ModuleToggleListener and OverlayModuleToggleListener in the listeners package for modular, extensible event handling.
-   Updated OverlayWindow to use a callback (listener) for module toggling, decoupling UI from module logic.
-   Registered OverlayModuleToggleListener in AgentRegistry and updated OsrsHelperAgent to use the instance from the registry.
-   Ensured all code changes strictly follow the modular and extensible structure required for the agent.

This step ensures the overlay and module management are fully decoupled and extensible, following all project requirements.
