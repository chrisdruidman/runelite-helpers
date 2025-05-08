COMPLETED - 2025-05-08 14:50

Summary:

-   Fixed a critical issue with the direct game object interaction in MenuActionServiceImpl that was causing the agility module to fail
-   Identified that the `invokeMenuAction` method we were trying to use doesn't exist in the RuneLite Client API
-   Found the correct method signature by examining RuneLite's source code: `menuAction(int p0, int p1, MenuAction action, int id, int itemId, String option, String target)`
-   Updated the code to use the proper method and parameters for object interaction
-   Added the missing registration of AgilityModule in the ServiceRegistry to ensure the module can be accessed by the MenuActionService
-   Improved error logging in the getCurrentCourse method to provide better diagnostics
-   Updated parameter handling for game object interactions to use the appropriate MenuAction type (GAME_OBJECT_FIRST_OPTION)
-   Enhanced debug logging to show all parameters used when invoking menu actions
-   These changes should allow the agent to properly interact with all obstacles in the Canifis agility course using direct game object interaction rather than relying on menu entries
