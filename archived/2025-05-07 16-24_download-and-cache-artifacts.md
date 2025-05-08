COMPLETED - 2025-05-07 16:24

## Step: Download and Cache All Artifacts from bootstrap.json

-   Implemented logic in the launcher to download all artifacts listed in bootstrap.json to `cache/client-artifacts`.
-   Each artifact is only downloaded if it is missing or if its SHA-256 hash does not match the expected value from bootstrap.json.
-   After download, each artifact is verified against its expected hash.
-   All actions and errors are logged to both the console and `launcher-output`.
-   This keeps the launcher codebase tidy and ensures all dependencies for launching the client are present and up-to-date.
