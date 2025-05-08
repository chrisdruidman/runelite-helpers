COMPLETED - 2025-05-07 15:53

## Step: Build a shaded JAR for the agent

-   Added the Maven Shade Plugin to the osrs-helper-agent/pom.xml to produce a shaded JAR with all dependencies bundled.
-   Ran `mvn clean package` and confirmed that the build succeeded.
-   Verified that `osrs-helper-agent-1.0-SNAPSHOT-shaded.jar` was created in the target directory.
-   The agent is now ready for distribution or injection as a single, self-contained JAR.
