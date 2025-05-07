COMPLETED - 2025-05-07 17:15

**Step: Update launcher to use classpath and main class for RuneLite**

-   Refactored the launcher to launch RuneLite using the -cp (classpath) option and net.runelite.client.RuneLite as the main class, instead of java -jar.
-   The classpath is now built from all JARs in the client-artifacts directory, ensuring all dependencies are included.
-   This resolves the 'no main manifest attribute' error and allows RuneLite to start with the agent injected.
-   Confirmed that RuneLite now runs, though there is a new issue to address in the next step.
