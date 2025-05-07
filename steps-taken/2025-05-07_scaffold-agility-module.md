COMPLETED - 2025-05-07

## Step: Scaffold Agility Module and Course Structure

-   Decided on a modular, extensible structure for the Agility helper.
-   Created `AgilityModule` as the entry point for agility automation, with course registration and execution logic.
-   Defined the `AgilityCourse` interface for all course modules to implement.
-   Added a stub `CanifisCourse` as an example course module.
-   Documented Canifis Rooftop Agility Course obstacles and how RuneLite's AgilityPlugin uses obstacle IDs in `canifis-course-obstacles.md`.
-   Committed all changes to git with a clear message.

This structure allows for easy addition of new agility courses and keeps course logic isolated and maintainable.
