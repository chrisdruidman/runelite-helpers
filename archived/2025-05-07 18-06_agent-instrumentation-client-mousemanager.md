COMPLETED - 2025-05-07 18:06

## Step: Reliable Instrumentation and Reflection for Client and MouseManager Access

-   Implemented ASM-based constructor instrumentation for both `RuneLite` and `MouseManager` classes in the agent.
-   Captured the main `RuneLite` instance and the `MouseManager` instance at runtime using static callbacks injected into their constructors.
-   Moved all agent initialization logic to run only after the `RuneLite` instance is constructed, and used a background thread to poll for the `Client` and `MouseManager` fields to avoid blocking RuneLite startup.
-   Added robust logging and up to 2 minutes of polling for both fields, ensuring the agent waits for full initialization before proceeding.
-   Confirmed that all core agent services (Client, MouseManager, GameStateProvider, MouseInputService) now initialize successfully and modules can be started reliably.
-   This step enables deep, reliable automation and interaction with RuneLite internals for future helper modules.
