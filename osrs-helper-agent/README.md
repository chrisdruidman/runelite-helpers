# OSRS RuneLite Agent Helper

This project is a Java agent for automating and instrumenting the RuneLite client using ByteBuddy. It enables advanced automation (e.g., agility course automation) by injecting hooks and logic into RuneLite internals at runtime.

## Features

-   Human-like mouse movement and click simulation
-   Agility rooftop course automation (Canifis supported)
-   Mark of Grace detection and collection
-   Robust fall detection and recovery
-   In-game overlay for toggling automation and status display

## Requirements

-   Java 11 or higher
-   Maven
-   RuneLite client (built from source or official launcher)

## Building the Agent

```
cd osrs-helper-agent
mvn clean package
```

This will produce a JAR in `osrs-helper-agent/target/osrs-helper-agent-*.jar`.

## Running RuneLite with the Agent

Use the provided script or run manually:

```
java -javaagent:%localappdata%\Runelite\osrs-helper-agent\target\osrs-helper-agent-1.0-SNAPSHOT.jar -jar %localappdata%\Runelite\RuneLite.jar
```

Replace the JAR names if needed. `%localappdata%` is an environment variable that expands to your local app data folder (e.g., `C:\Users\YourName\AppData\Local`).

## Provided Script

Use `run-with-agent.bat` (Windows) to launch RuneLite with the agent attached. The script is preconfigured to use `%localappdata%\Runelite` as the base path.

## Toggling Automation

-   Use the in-game overlay button to enable/disable automation at any time.

## Customization

-   All agent code is in `osrs-helper-agent/src/main/java/com/osrs/agent/`.
-   To target other agility courses or add features, update the agent logic and obstacle IDs as needed.

## Disclaimer

This project is for educational and research purposes only. Use at your own risk.
