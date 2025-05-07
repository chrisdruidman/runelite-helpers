package com.osrshelper.agent;

// Minimal always-on-top overlay for toggling modules on/off at runtime.
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;
import java.awt.*;

public class OverlayController {
    private final Map<String, Boolean> moduleStates = new ConcurrentHashMap<>();
    private final Map<String, HelperModule> modules;
    private final JFrame frame;

    public OverlayController(Map<String, HelperModule> modules) {
        this.modules = modules;
        for (String name : modules.keySet()) {
            moduleStates.put(name, true); // Default: enabled
        }
        frame = new JFrame("OSRS Helper Modules");
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(200, modules.size() * 40 + 20);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(modules.size(), 2, 5, 5));
        for (String name : modules.keySet()) {
            JCheckBox checkBox = new JCheckBox(name, true);
            checkBox.addActionListener(e -> {
                boolean enabled = checkBox.isSelected();
                moduleStates.put(name, enabled);
            });
            panel.add(new JLabel(name));
            panel.add(checkBox);
        }
        frame.add(panel);
    }

    public void show() {
        frame.setVisible(true);
    }

    public boolean isModuleEnabled(String name) {
        return moduleStates.getOrDefault(name, false);
    }
}
