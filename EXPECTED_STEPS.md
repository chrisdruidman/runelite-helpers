# Steps to Create a Java Agent for RuneLite Background Automation

## 1. Set Up Your Java Agent Project

-   Create a new Java project in the `osrs-helper-agent` folder (separate from `runelite`).
-   Add a bytecode manipulation library dependency (e.g., ByteBuddy or ASM) to your project.

## 2. Analyze the RuneLite Source Code

-   Use the local `runelite` source to identify classes and methods handling input, game logic, and events.
-   Focus on `runelite-client/src/main/java/net/runelite/client/input` and related packages for input handling.
-   Explore other relevant packages for game logic and event handling.

## 3. Implement the Java Agent

-   Write a class with a `premain` method using the Instrumentation API.
-   Use your bytecode library to hook or modify the identified RuneLite methods at load time.

## 4. Inject Automation Logic

-   Inject code to simulate actions (e.g., clicks, interactions) by calling or modifying RuneLite’s internal methods.
-   Ensure your logic does not require window focus or real mouse/keyboard input.

## 5. Build and Package the Agent

-   Package your agent as a JAR with the correct manifest (`Premain-Class` attribute).

## 6. Launch RuneLite with the Agent

-   Start RuneLite using the `-javaagent:/path/to/youragent.jar` JVM argument.

## 7. Test and Iterate

-   Test your automation, refine your hooks, and update as needed when RuneLite updates.

---

**Note:**

-   Keep your agent project separate from the `runelite` folder.
-   Use the local source for easier debugging and method identification.
