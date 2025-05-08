COMPLETED - 2025-05-07 22:54

Step Summary
Timestamp: 2025-05-07 22:54

What we have done:
Continued development of the OSRS Helper agent for RuneLite, focusing on automating the Canifis Rooftop Agility Course.
Refined the click logic in the agent to improve reliability when interacting with agility obstacles.
Implemented multiple strategies for determining the correct click location:
Attempted to use the model-based clickbox via Perspective.getClickbox.
Fallback to the object's getClickbox() if the model is unavailable.
If both are unavailable, planned to use Perspective.localToCanvas to project the object's location to canvas coordinates and click there.
Improved click accuracy by clicking the centroid of the polygon or the center of the bounding box, with a small random offset and a leftward shift.
Added logic to wait for the player's animation to change and for the next obstacle to become visible before proceeding to the next step.
What we have attempted to fix:
The main issue was that the click was sometimes missing the obstacle, especially when the model-based clickbox was unavailable (null).
We tried several strategies:
Using the model-based clickbox via Perspective.getClickbox.
Falling back to the object's getClickbox() if the model was unavailable.
Clicking the centroid of the polygon or the center of the bounding box.
Adding a small random offset and shifting the click slightly to the left.
As a final fallback, using Perspective.localToCanvas to project the object's location to canvas coordinates and clicking there.
Despite these efforts, the click sometimes still misses the obstacle, likely due to limitations in clickbox/model availability or coordinate mismatches.
Next steps:
Further investigation may be needed into how RuneLite determines interactable areas for obstacles when models/clickboxes are unavailable.
Additional debugging or alternative strategies (such as simulating menu actions) may be required for perfect reliability.
