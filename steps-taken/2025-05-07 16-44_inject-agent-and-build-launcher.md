COMPLETED - 2025-05-07 16:44

**Step: Inject agent and build launcher**

-   Updated the osrs-helper-launcher to inject the agent JAR into the RuneLite client using the -javaagent argument.
-   Ensured the launcher locates both the RuneLite client JAR and the agent shaded JAR.
-   Added error handling and logging for missing JARs.
-   Updated both agent and launcher Maven configurations to use Java 11 for compatibility with RuneLite.
-   Added Gson as a dependency to the launcher for JSON parsing.
-   Successfully built both the agent and launcher projects with Java 11.
-   Confirmed the launcher is ready to inject the agent and launch RuneLite.
