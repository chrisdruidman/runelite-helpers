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
import org.json.JSONObject;

public class RuneLiteCustomLauncher {
    private static final String BOOTSTRAP_URL = "https://static.runelite.net/bootstrap.json";
    private static final String AGENT_JAR = ".." + File.separator + "osrs-helper-agent" + File.separator + "target" + File.separator + "osrs-helper-agent-1.0-SNAPSHOT-shaded.jar";

    public static void main(String[] args) throws Exception {
        // Download and parse bootstrap.json
        System.out.println("[Launcher] Downloading bootstrap.json...");
        String bootstrapJson = readUrl(BOOTSTRAP_URL);
        JSONObject json = new JSONObject(bootstrapJson);
        // Get the first artifact for the latest client
        JSONObject clientArtifact = json.getJSONArray("artifacts").getJSONObject(0);
        String clientUrl = clientArtifact.getString("path");
        String clientHash = clientArtifact.getString("hash");
        String clientJarName = clientArtifact.getString("name");
        // Get client JVM arguments
        List<String> jvmArgs = new ArrayList<>();
        if (json.has("clientJvmArguments")) {
            for (Object arg : json.getJSONArray("clientJvmArguments")) {
                jvmArgs.add(arg.toString());
            }
        }
        // Get the launcher version (fallback to hardcoded if not present)
        String launcherVersion = "2.7.4";
        if (json.has("launcher")) {
            launcherVersion = json.getJSONObject("launcher").optString("version", launcherVersion);
        } else if (json.has("version")) {
            // Only use top-level version if it looks like a launcher version (e.g., contains a dot and is not client version)
            String v = json.getString("version");
            if (v.matches("\\d+\\.\\d+\\.\\d+")) {
                launcherVersion = v;
            }
        }
        System.out.println("[Launcher] Latest client jar: " + clientUrl);
        System.out.println("[Launcher] Expected SHA-256: " + clientHash);
        System.out.println("[Launcher] Launcher version: " + launcherVersion);

        // Download client jar if needed
        File clientJar = new File(clientJarName);
        boolean needsDownload = true;
        if (clientJar.exists()) {
            String localHash = sha256(clientJar.getAbsolutePath());
            System.out.println("[Launcher] Local client jar SHA-256: " + localHash);
            if (localHash.equalsIgnoreCase(clientHash)) {
                System.out.println("[Launcher] Local client jar is up to date.");
                needsDownload = false;
            } else {
                System.out.println("[Launcher] Local client jar hash mismatch, re-downloading...");
            }
        }
        if (needsDownload) {
            downloadFile(clientUrl, clientJarName);
            String downloadedHash = sha256(clientJar.getAbsolutePath());
            if (!downloadedHash.equalsIgnoreCase(clientHash)) {
                throw new RuntimeException("Downloaded client jar hash does not match expected SHA-256!");
            }
            System.out.println("[Launcher] Downloaded and verified client jar.");
        }

        // Add agent and launcher version JVM args
        jvmArgs.add("-Drunelite.launcher.version=" + launcherVersion);
        jvmArgs.add("-javaagent:" + AGENT_JAR);

        // Build the command
        List<String> command = new ArrayList<>();
        command.add(javaBin());
        command.addAll(jvmArgs);
        command.add("-jar");
        command.add(clientJarName);
        for (String arg : args) {
            command.add(arg);
        }
        System.out.println("[Launcher] Launching RuneLite with command:");
        for (String part : command) {
            System.out.print(part + " ");
        }
        System.out.println();
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        Process process = pb.start();
        process.waitFor();
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
