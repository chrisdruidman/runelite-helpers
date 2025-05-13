package com.osrshelper.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;
import java.nio.charset.StandardCharsets;
import com.google.gson.*;

public class Main {
    private static final String LOG_FILE = "osrs-helper-launcher/launcher-output";
    private static final String BOOTSTRAP_URL = "https://static.runelite.net/bootstrap.json";
    private static final String CACHE_DIR = "osrs-helper-launcher/cache";
    private static final String CACHE_FILE = CACHE_DIR + "/bootstrap.json";
    private static final String ARTIFACTS_DIR = CACHE_DIR + "/client-artifacts";
    private static final Duration CACHE_MAX_AGE = Duration.ofHours(1);
    private static Logger logger;

    public static void main(String[] args) {
        setupLogging();
        logger.info("OSRS Helper Launcher started.");
        try {
            String bootstrapPath = getBootstrapJson();
            logger.info("Using bootstrap.json at: " + bootstrapPath);
            BootstrapInfo info = parseBootstrapJson(bootstrapPath);
            if (info == null) {
                logger.severe("Failed to parse bootstrap.json");
                return;
            }
            logger.info("RuneLite version: " + info.version);
            BootstrapInfo.Artifact clientArtifact = info.getClientArtifact();
            if (clientArtifact != null) {
                logger.info("Latest client jar: " + clientArtifact.name + " (" + clientArtifact.path + ")");
            } else {
                logger.warning("Could not find client jar artifact in bootstrap.json");
            }
            logger.info("Client JVM args: " + info.clientJvmArguments);
            logger.info("Launcher JVM args: " + info.launcherArguments);
            // Download all artifacts if needed
            downloadArtifacts(info.artifacts);

            // === OVERWRITE DOWNLOADED runelite-api JAR WITH PATCHED JAR ===
            // Find the runelite-api jar in ARTIFACTS_DIR
            try {
                Path patchedApiJar = Path.of("..", "runelite", "runelite-api", "target", "runelite-api-1.11.8-SNAPSHOT.jar").normalize();
                if (Files.exists(patchedApiJar)) {
                    Files.list(Path.of(ARTIFACTS_DIR))
                        .filter(p -> p.getFileName().toString().startsWith("runelite-api-") && p.getFileName().toString().endsWith(".jar"))
                        .forEach(apiJar -> {
                            try {
                                logger.info("Overwriting " + apiJar + " with patched runelite-api jar: " + patchedApiJar);
                                Files.copy(patchedApiJar, apiJar, StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                logger.severe("Failed to overwrite " + apiJar + " with patched runelite-api jar: " + e.getMessage());
                            }
                        });
                } else {
                    logger.warning("Patched runelite-api jar not found at: " + patchedApiJar);
                }
            } catch (IOException e) {
                logger.severe("Error searching for runelite-api jar in artifacts dir: " + e.getMessage());
            }

            // === AGENT INJECTION AND CLIENT LAUNCH ===
            BootstrapInfo.Artifact clientArtifact2 = info.getClientArtifact();
            if (clientArtifact2 == null) {
                logger.severe("No RuneLite client JAR found in artifacts. Aborting launch.");
                return;
            }
            Path clientJar = Path.of(ARTIFACTS_DIR, clientArtifact2.name);
            if (!Files.exists(clientJar)) {
                logger.severe("RuneLite client JAR not found at: " + clientJar);
                return;
            }
            // Locate agent JAR (relative to launcher project root)
            Path agentJar = Path.of("..", "osrs-helper-agent", "target", "osrs-helper-agent-1.0-SNAPSHOT-shaded.jar").normalize();
            if (!Files.exists(agentJar)) {
                logger.severe("Agent JAR not found at: " + agentJar + ". Please build the agent project first.");
                return;
            }
            // Filter problematic JVM args
            List<String> filteredJvmArgs = new ArrayList<>();
            for (String arg : info.clientJvmArguments) {
                if (arg.equals("-Xincgc") ||
                    arg.equals("-XX:+UseConcMarkSweepGC") ||
                    arg.equals("-XX:+UseParNewGC") ||
                    arg.equals("-XX:+DisableAttachMechanism")) {
                    logger.info("Stripping problematic JVM arg: " + arg);
                    continue;
                }
                filteredJvmArgs.add(arg);
            }
            // Build classpath from all JARs in ARTIFACTS_DIR
            StringBuilder classpath = new StringBuilder();
            try {
                Files.list(Path.of(ARTIFACTS_DIR))
                    .filter(p -> p.toString().endsWith(".jar"))
                    .forEach(p -> {
                        if (classpath.length() > 0) classpath.append(";");
                        classpath.append(p.toAbsolutePath());
                    });
            } catch (IOException e) {
                logger.severe("Failed to build classpath: " + e.getMessage());
                return;
            }
            // Build command
            List<String> cmd = new ArrayList<>();
            cmd.add("java");
            cmd.add("-cp");
            cmd.add(classpath.toString());
            cmd.add("-javaagent:" + agentJar.toAbsolutePath());
            cmd.addAll(filteredJvmArgs);
            cmd.add("net.runelite.client.RuneLite");
            logger.info("Launching RuneLite with agent injected (using -cp)...");
            logger.info("Command: " + String.join(" ", cmd));
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.inheritIO(); // Show output in current console
            try {
                Process proc = pb.start();
                int exitCode = proc.waitFor();
                logger.info("RuneLite process exited with code: " + exitCode);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to launch RuneLite with agent", e);
            }
            // ...future extensibility here...
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get or parse bootstrap.json", e);
        }
    }

    /**
     * Downloads and caches the bootstrap.json file. Returns the path to the cached file.
     */
    private static String getBootstrapJson() throws IOException {
        Path cacheDir = Path.of(CACHE_DIR);
        Path cacheFile = Path.of(CACHE_FILE);
        try {
            Files.createDirectories(cacheDir);
        } catch (FileAlreadyExistsException ignored) {}
        boolean shouldDownload = true;
        if (Files.exists(cacheFile)) {
            Instant lastModified = Files.getLastModifiedTime(cacheFile).toInstant();
            Instant now = Instant.now();
            if (Duration.between(lastModified, now).compareTo(CACHE_MAX_AGE) < 0) {
                logger.info("Using cached bootstrap.json (age: " + Duration.between(lastModified, now).toMinutes() + " min)");
                shouldDownload = false;
            } else {
                logger.info("Cached bootstrap.json is older than 1 hour, refreshing...");
            }
        }
        if (shouldDownload) {
            logger.info("Downloading bootstrap.json from " + BOOTSTRAP_URL);
            HttpURLConnection conn = (HttpURLConnection) new URL(BOOTSTRAP_URL).openConnection();
            conn.setRequestProperty("User-Agent", "osrs-helper-launcher");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            try (InputStream in = conn.getInputStream(); OutputStream out = Files.newOutputStream(cacheFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                in.transferTo(out);
            }
            logger.info("Downloaded and cached bootstrap.json");
        }
        return cacheFile.toAbsolutePath().toString();
    }

    private static void setupLogging() {
        logger = Logger.getLogger("OSRSHelperLauncher");
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);

        // Clear log file at start
        try {
            Files.write(Path.of(LOG_FILE), new byte[0], StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to clear log file: " + e.getMessage());
        }

        // Console handler
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);

        // File handler
        try {
            FileHandler fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            System.err.println("Failed to set up file logging: " + e.getMessage());
        }
    }

    private static BootstrapInfo parseBootstrapJson(String path) {
        try {
            String json = Files.readString(Path.of(path), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            BootstrapInfo info = new BootstrapInfo();
            // Parse artifacts
            JsonArray artifactsArr = obj.getAsJsonArray("artifacts");
            if (artifactsArr != null) {
                for (JsonElement el : artifactsArr) {
                    JsonObject art = el.getAsJsonObject();
                    BootstrapInfo.Artifact a = new BootstrapInfo.Artifact();
                    a.name = art.get("name").getAsString();
                    a.path = art.get("path").getAsString();
                    a.hash = art.has("hash") ? art.get("hash").getAsString() : null;
                    a.size = art.has("size") ? art.get("size").getAsLong() : 0;
                    info.artifacts.add(a);
                }
            }
            // Parse JVM args
            JsonArray clientJvmArgs = obj.getAsJsonArray("clientJvmArguments");
            if (clientJvmArgs != null) {
                for (JsonElement el : clientJvmArgs) {
                    info.clientJvmArguments.add(el.getAsString());
                }
            }
            JsonArray launcherArgs = obj.getAsJsonArray("launcherArguments");
            if (launcherArgs != null) {
                for (JsonElement el : launcherArgs) {
                    info.launcherArguments.add(el.getAsString());
                }
            }
            // Version
            info.version = obj.has("version") ? obj.get("version").getAsString() : null;
            return info;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to parse bootstrap.json", e);
            return null;
        }
    }

    /**
     * Download all artifacts to ARTIFACTS_DIR, only if missing or hash mismatch.
     */
    private static void downloadArtifacts(List<BootstrapInfo.Artifact> artifacts) {
        Path artifactsDir = Path.of(ARTIFACTS_DIR);
        try {
            Files.createDirectories(artifactsDir);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create artifacts directory", e);
            return;
        }
        for (BootstrapInfo.Artifact artifact : artifacts) {
            Path artifactPath = artifactsDir.resolve(artifact.name);
            boolean needsDownload = true;
            if (Files.exists(artifactPath)) {
                String fileHash = getFileSha256(artifactPath);
                if (fileHash != null && fileHash.equalsIgnoreCase(artifact.hash)) {
                    logger.info("Artifact up-to-date: " + artifact.name);
                    needsDownload = false;
                } else {
                    logger.info("Artifact hash mismatch or unreadable: " + artifact.name + ", redownloading...");
                }
            }
            if (needsDownload) {
                try {
                    logger.info("Downloading artifact: " + artifact.name + " from " + artifact.path);
                    HttpURLConnection conn = (HttpURLConnection) new URL(artifact.path).openConnection();
                    conn.setRequestProperty("User-Agent", "osrs-helper-launcher");
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    try (InputStream in = conn.getInputStream(); OutputStream out = Files.newOutputStream(artifactPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                        in.transferTo(out);
                    }
                    String downloadedHash = getFileSha256(artifactPath);
                    if (downloadedHash != null && downloadedHash.equalsIgnoreCase(artifact.hash)) {
                        logger.info("Downloaded and verified: " + artifact.name);
                    } else {
                        logger.severe("Hash mismatch after download for " + artifact.name + ". Expected: " + artifact.hash + ", got: " + downloadedHash);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to download artifact: " + artifact.name, e);
                }
            }
        }
    }

    /**
     * Compute SHA-256 hash of a file. Returns hex string or null on error.
     */
    private static String getFileSha256(Path file) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            try (InputStream fis = Files.newInputStream(file)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = fis.read(buf)) > 0) {
                    digest.update(buf, 0, n);
                }
            }
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to compute hash for file: " + file, e);
            return null;
        }
    }

    static class BootstrapInfo {
        public static class Artifact {
            public String name;
            public String path;
            public String hash;
            public long size;
        }
        public List<Artifact> artifacts = new ArrayList<>();
        public List<String> clientJvmArguments = new ArrayList<>();
        public List<String> launcherArguments = new ArrayList<>();
        public String version;

        public Artifact getClientArtifact() {
            for (Artifact a : artifacts) {
                if (a.name != null && a.name.startsWith("client-") && a.name.endsWith(".jar")) {
                    return a;
                }
            }
            return null;
        }
    }
}
