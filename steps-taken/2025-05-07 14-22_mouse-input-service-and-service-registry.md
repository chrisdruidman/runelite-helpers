COMPLETED - 2025-05-07 14:22

## Step: Implement Mouse Input Service, GameObject Click, and Service Registry

-   Reviewed RuneLite's mouse event handling and identified MouseManager as the injection point for simulating mouse input.
-   Used reflection in the agent to obtain the MouseManager instance and injected it into MouseInputService.
-   Implemented clickAt in MouseInputService to simulate mouse clicks at screen coordinates using MouseManager.
-   Implemented clickGameObject in MouseInputService to find a game object by ID, convert its world position to screen coordinates, and click it.
-   Extended GameStateProvider and RealGameStateProvider to support retrieving a game object's world position by ID.
-   Created a ServiceRegistry class for managing and retrieving shared service instances.
-   Integrated ServiceRegistry into agent initialization, registering all core services.
-   Refactored modules and services (AgilityModule, MouseInputService, etc.) to retrieve dependencies from the ServiceRegistry for improved modularity and maintainability.
