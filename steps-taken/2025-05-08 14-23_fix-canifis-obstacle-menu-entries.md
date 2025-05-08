COMPLETED - 2025-05-08 14:23

Summary:

-   Identified and fixed incorrect menu text for the first Canifis agility course obstacle, updating it from "Climb | Tree" to "Climb | tall tree" to match the in-game text.
-   Updated the AgilityModule to properly wait for obstacle completion before moving to the next one by:
    -   Adding a continue mechanism to retry when a menu entry isn't found
    -   Improving obstacle completion detection by tracking animation changes, player position changes, and adding appropriate timeouts
    -   Adding enhanced logging to better track obstacle progression
-   Verified all obstacle IDs against RuneLite's source code (ObjectID.java) to confirm our course implementation matches the game
-   Restored "Jump-down" as the menu option for the final obstacle based on RuneLite's ROOFTOPS_CANIFIS_LEAPDOWN naming convention
-   Added detailed comments to the obstacle definitions linking them to RuneLite's corresponding constants
-   These changes should resolve both issues: menu entry matching failure and premature advancement to the next obstacle
