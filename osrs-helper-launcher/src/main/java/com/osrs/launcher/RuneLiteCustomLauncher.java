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
    private static final String CLIENT_JAR_NAME = "runelite-client-latest.jar";
    private static final String AGENT_JAR = ".." + File.separator + "osrs-helper-agent" + File.separator + "target" + File.separator + "osrs-helper-agent-1.0-SNAPSHOT-shaded.jar";

    public static void main(String[] args) throws Exception {
        // Download and parse bootstrap.json
        System.out.println("[Launcher] Downloading bootstrap.json...");
        String bootstrapJson = readUrl(BOOTSTRAP_URL);
        JSONObject json = new JSONObject(bootstrapJson);
        JSONObject clientObj = json.getJSONObject("client");
        String clientUrl = clientObj.getString("jar");
        String clientHash = clientObj.getString("sha256");
        // Get the launcher version from bootstrap.json
        String launcherVersion = json.getJSONObject("launcher").getString("version");
        System.out.println("[Launcher] Latest client jar: " + clientUrl);
        System.out.println("[Launcher] Expected SHA-256: " + clientHash);
        System.out.println("[Launcher] Launcher version: " + launcherVersion);

        // Download client jar if needed
        File clientJar = new File(CLIENT_JAR_NAME);
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
            downloadFile(clientUrl, CLIENT_JAR_NAME);
            String downloadedHash = sha256(clientJar.getAbsolutePath());
            if (!downloadedHash.equalsIgnoreCase(clientHash)) {
                throw new RuntimeException("Downloaded client jar hash does not match expected SHA-256!");
            }
            System.out.println("[Launcher] Downloaded and verified client jar.");
        }

        // JVM arguments from the log
        List<String> jvmArgs = new ArrayList<>();
        jvmArgs.add("-XX:+DisableAttachMechanism");
        jvmArgs.add("-Xmx768m");
        jvmArgs.add("-Xss2m");
        jvmArgs.add("-XX:CompileThreshold=1500");
        jvmArgs.add("-Dsun.java2d.d3d=true");
        jvmArgs.add("-Dsun.java2d.opengl=false");
        jvmArgs.add("-Drunelite.launcher.version=" + launcherVersion);
        jvmArgs.add("-XX:ErrorFile=" + System.getenv("USERPROFILE") + "\\.runelite\\logs\\jvm_crash_pid_%p.log");
        jvmArgs.add("-javaagent:" + AGENT_JAR);

        // Build the command
        List<String> command = new ArrayList<>();
        command.add(javaBin());
        command.addAll(jvmArgs);
        command.add("-jar");
        command.add(CLIENT_JAR_NAME);
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
