package com.osrs.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RuneLiteCustomLauncher {
    public static void main(String[] args) throws Exception {
        // Update these paths as needed
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String agentJar = ".." + File.separator + "osrs-helper-agent" + File.separator + "target" + File.separator + "osrs-helper-agent-1.0-SNAPSHOT-shaded.jar";
        String clientJar = System.getenv("LOCALAPPDATA") + "\\Runelite\\client-1.11.8-SNAPSHOT.jar";

        // JVM arguments from the log
        List<String> jvmArgs = new ArrayList<>();
        jvmArgs.add("-XX:+DisableAttachMechanism");
        jvmArgs.add("-Xmx768m");
        jvmArgs.add("-Xss2m");
        jvmArgs.add("-XX:CompileThreshold=1500");
        jvmArgs.add("-Dsun.java2d.d3d=true");
        jvmArgs.add("-Dsun.java2d.opengl=false");
        jvmArgs.add("-Drunelite.launcher.version=2.7.4");
        jvmArgs.add("-XX:ErrorFile=" + System.getenv("USERPROFILE") + "\\.runelite\\logs\\jvm_crash_pid_%p.log");
        jvmArgs.add("-javaagent:" + agentJar);

        // Build the command
        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.addAll(jvmArgs);
        command.add("-jar");
        command.add(clientJar);

        // Pass through any additional arguments
        for (String arg : args) {
            command.add(arg);
        }

        // Print the command for debugging
        System.out.println("Launching RuneLite with command:");
        for (String part : command) {
            System.out.print(part + " ");
        }
        System.out.println();

        // Start the process
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        Process process = pb.start();
        process.waitFor();
    }
}
