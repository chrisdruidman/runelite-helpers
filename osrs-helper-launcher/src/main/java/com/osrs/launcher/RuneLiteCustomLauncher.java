package com.osrs.launcher;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import org.json.JSONObject;

public class RuneLiteCustomLauncher {
    private static final String BOOTSTRAP_URL = "https://static.runelite.net/bootstrap.json";
    private static final String AGENT_JAR = ".." + File.separator + "osrs-helper-agent" + File.separator + "target" + File.separator + "osrs-helper-agent-1.0-SNAPSHOT-shaded.jar";

    public static void main(String[] args) throws Exception {
        // Download and parse bootstrap.json
        System.out.println("[Launcher] Downloading bootstrap.json...");
        String bootstrapJson = readUrl(BOOTSTRAP_URL);
        JSONObject json = new JSONObject(bootstrapJson);

        // Download all artifacts and build classpath
        List<String> classpathJars = new ArrayList<>();
        for (Object artifactObj : json.getJSONArray("artifacts")) {
            JSONObject artifact = (JSONObject) artifactObj;
            String jarUrl = artifact.getString("path");
            String jarHash = artifact.getString("hash");
            String jarName = artifact.getString("name");
            File jarFile = new File(jarName);
            boolean needsDownload = true;
            if (jarFile.exists()) {
                String localHash = sha256(jarFile.getAbsolutePath());
                if (localHash.equalsIgnoreCase(jarHash)) {
                    needsDownload = false;
                } else {
                    System.out.println("[Launcher] Hash mismatch for " + jarName + ", re-downloading...");
                }
            }
            if (needsDownload) {
                downloadFile(jarUrl, jarName);
                String downloadedHash = sha256(jarFile.getAbsolutePath());
                if (!downloadedHash.equalsIgnoreCase(jarHash)) {
                    throw new RuntimeException("Downloaded " + jarName + " hash does not match expected SHA-256!");
                }
                System.out.println("[Launcher] Downloaded and verified " + jarName);
            }
            classpathJars.add(jarName);
        }

        // Get client JVM arguments
        List<String> jvmArgs = new ArrayList<>();
        if (json.has("clientJvmArguments")) {
            for (Object arg : json.getJSONArray("clientJvmArguments")) {
                jvmArgs.add(arg.toString());
            }
        }
        // Remove unsupported JVM args for Java 11+
        jvmArgs.removeIf(arg -> arg.equals("-Xincgc") || arg.equals("-XX:+UseConcMarkSweepGC") || arg.equals("-XX:+UseParNewGC"));

        // Get the launcher version (fallback to hardcoded if not present)
        String launcherVersion = "2.7.4";
        System.out.println("[Launcher] Launcher version: " + launcherVersion);

        // Add agent and launcher version JVM args
        jvmArgs.add("-Drunelite.launcher.version=" + launcherVersion);
        jvmArgs.add("-javaagent:" + AGENT_JAR);
        // Add --add-opens for Guice reflection access (Java 9+)
        jvmArgs.add("--add-opens=ALL-UNNAMED/com.google.inject.internal=ALL-UNNAMED");
        jvmArgs.add("--add-opens=ALL-UNNAMED/com.google.inject=ALL-UNNAMED");
        jvmArgs.add("--add-opens=ALL-UNNAMED/com.google.inject.spi=ALL-UNNAMED");
        jvmArgs.add("--add-opens=java.base/java.lang=ALL-UNNAMED");
        jvmArgs.add("--add-opens=java.base/java.lang.reflect=ALL-UNNAMED");
        jvmArgs.add("--add-opens=java.base/java.util=ALL-UNNAMED");
        jvmArgs.add("--add-opens=java.base/sun.reflect=ALL-UNNAMED");

        // Build the classpath (Windows uses ';' as separator)
        String classpath = classpathJars.stream().collect(Collectors.joining(";"));

        // Build the command
        List<String> command = new ArrayList<>();
        command.add(javaBin());
        command.addAll(jvmArgs);
        command.add("-cp");
        command.add(classpath);
        command.add("net.runelite.client.RuneLite");
        for (String arg : args) {
            command.add(arg);
        }
        System.out.println("[Launcher] Launching RuneLite with command:");
        for (String part : command) {
            System.out.print(part + " ");
        }
        System.out.println();
        // Start the process and print exit code
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();
        System.out.println("[Launcher] RuneLite client process exited with code: " + exitCode);
    }

    private static String javaBin() {
        return System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    }

    private static String readUrl(String urlString) throws Exception {
        try (InputStream in = new URL(urlString).openStream(); Scanner s = new Scanner(in).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    private static void downloadFile(String urlString, String dest) throws Exception {
        System.out.println("[Launcher] Downloading client jar from: " + urlString);
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestProperty("User-Agent", "RuneLite");
        try (InputStream in = conn.getInputStream(); FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }

    private static String sha256(String filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = Files.newInputStream(Paths.get(filePath))) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = fis.read(buffer)) > 0) {
                digest.update(buffer, 0, n);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : digest.digest()) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
