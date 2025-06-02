# OSRS Helper Agent: Host Application Concrete Plan

## Technology Stack Decision

-   **Primary Host Stack:**
    -   **Frontend/UI:** Wails (HTML/CSS/JS/TS, system webview, Go backend)
    -   **Backend:** Go
    -   **Scripting Engine:** Lua (embedded via `gopher-lua` in Go)
    -   **Hot Reload:** fsnotify (Go) to watch and reload Lua scripts on change
-   **Backup Option:**
    -   **Frontend/UI:** Electron (HTML/CSS/JS/TS, Chromium)
    -   **Backend:** Node.js
    -   **Scripting Engine:** JavaScript (native) or Lua (via node-lua or similar)

**Rationale:**

-   Wails + Go + gopher-lua provides a modern, cross-platform, low-resource desktop app with a powerful, embeddable scripting engine.
-   Lua is lightweight, fast, and easy to hot-reload, and integrates well with Go using the `gopher-lua` library.
-   Wails allows you to use your JS/TS skills for the UI, while keeping the backend efficient, maintainable, and easy to deploy.
-   fsnotify enables robust hot-reloading of Lua scripts for rapid development and testing.
-   Electron/Node.js is kept as a backup for rapid prototyping or if Go/Wails integration becomes a blocker.

---

## 1. Host Application (Wails + Go + gopher-lua)

-   **Role:**

    -   Launches RuneLite as a subprocess.
    -   Runs all agent logic (e.g., agility scripts) as Lua scripts, managed by the Go backend.
    -   Provides a modern UI (Wails) for script management, hot-reload, logging, and control.
    -   Handles all automation logic, including script execution and decision-making.

-   **Responsibilities:**

    -   Start/stop RuneLite subprocess.
    -   Manage and hot-reload Lua agent scripts/modules (e.g., agility, anti-AFK) via the Go backend using fsnotify.
    -   Communicate with RuneLite via a minimal, private IPC API (e.g., local socket or named pipe).
    -   Send commands (e.g., menu actions, synthetic mouse events) to RuneLite.
    -   Receive events/data (e.g., game state, player position, obstacle info) from RuneLite.
    -   Provide a UI for starting/stopping scripts, viewing logs, and reloading scripts.

-   **Lua Integration:**
    -   Use the `gopher-lua` library to embed Lua in Go.
    -   Expose Go functions to Lua for sending commands and receiving events.
    -   Hot-reload scripts by reloading Lua files and re-initializing state using fsnotify.
    -   Scripts interact with the backend only, never directly with RuneLite.

## 2. RuneLite Client (Subprocess)

-   **Role:**

    -   Remains as close to upstream as possible.
    -   Only patch: expose a minimal API for agent communication (IPC bridge).
    -   No agent logic or scripting engine inside RuneLite.

-   **Responsibilities:**
    -   Listen for commands from the host (e.g., "click here", "invoke menu action").
    -   Execute those commands internally (e.g., via MouseManager, MenuAction, or other safe APIs).
    -   Send events/data back to the host (e.g., "game tick", "player moved", "obstacle detected").
    -   Never move the system mouse—only simulate input internally for background operation.

## 3. Minimal API Surface (Patched into RuneLite)

-   **Design:**
    -   IPC bridge (local socket or named pipe) for communication.
    -   Expose only the necessary agent API:
        -   Synthetic mouse/menu actions (background-friendly).
        -   Game state queries (player position, inventory, etc.).
        -   Event notifications (game tick, object spawn, etc.).
    -   All communication is local and private.

## 4. Example: Agility Module/Script Flow

1. Script logic (in Lua, managed by Go backend) decides to click the next obstacle.
2. Go backend sends a "click" or "menu action" command to RuneLite via IPC.
3. RuneLite executes the action internally (no system mouse movement).
4. RuneLite sends back events (e.g., "player moved", "obstacle completed").
5. Lua script reacts and decides the next action.

## 5. Hot-Reload and Modularity

-   Scripts/modules are stored and managed by the host app (Lua scripts in Go backend), not inside RuneLite.
-   Hot-reload is as simple as reloading the Lua script in the backend—no RuneLite restart or patching required for logic changes. fsnotify is used to watch for changes and trigger reloads automatically.
-   All code remains modular, extensible, and private.

## 6. Advantages

-   Modern, cross-platform UI (Wails) with low resource usage.
-   Lua scripting is lightweight, fast, and easy to hot-reload (with fsnotify).
-   Maximum safety, privacy, and maintainability.
-   Rapid development and testing (short feedback loop).
-   Minimal patch surface in RuneLite, making updates easy.
-   Backup option (Electron/Node.js) available if needed.

## 7. Next Steps

1. Patch RuneLite to add the IPC bridge and minimal agent API.
2. Scaffold the Wails host application (Go backend, JS/TS frontend, Lua scripting with gopher-lua, hot-reload with fsnotify).
3. Implement a simple end-to-end example (e.g., Lua script triggers a command, receives an event from RuneLite).
4. Expand the API and script capabilities as needed (e.g., for agility, anti-AFK, etc.).
5. If Go/Wails/gopher-lua integration is blocked, fall back to Electron/Node.js stack.
