COMPLETED - 2025-05-07 10:41

## Step: Modular Game State Provider for Agility Automation

-   Designed and implemented a modular GameStateProvider interface for the agent, allowing course logic to remain decoupled from RuneLite internals.
-   Added DummyGameStateProvider for testing and demonstration.
-   Refactored CanifisCourse to use GameStateProvider for all game state queries, supporting easy extensibility and future integration with RuneLite.
-   This pattern enables new courses and tasks to be added with minimal changes and supports both testing and live automation.
