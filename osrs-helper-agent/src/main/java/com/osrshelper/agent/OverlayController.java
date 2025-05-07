package com.osrshelper.agent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal always-on-top overlay for toggling modules on/off at runtime.
 */
public class OverlayController {
    private final JFrame frame;
    private final Map<String, Boolean> moduleStates = new ConcurrentHashMap<>();
    private final Map<String, Module> modules;

    public OverlayController(Map<String, Module> modules) {
        this.modules = modules;
        for (String name : modules.keySet()) {
            moduleStates.put(name, true); // Default: enabled
        }
        frame = new JFrame("OSRS Helper Modules");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        frame.setUndecorated(true);
        frame.setSize(200, modules.size() * 40 + 20);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.setBackground(new Color(0,0,0,0));
        frame.add(buildPanel(), BorderLayout.CENTER);
    }

    private JPanel buildPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(modules.size(), 2, 5, 5));
        for (String name : modules.keySet()) {
            JLabel label = new JLabel(name);
            JToggleButton toggle = new JToggleButton("ON");
            toggle.setSelected(true);
            toggle.addActionListener((ActionEvent e) -> {
                boolean enabled = toggle.isSelected();
                moduleStates.put(name, enabled);
                toggle.setText(enabled ? "ON" : "OFF");
            });
            panel.add(label);
            panel.add(toggle);
        }
        return panel;
    }

    public void show() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    public boolean isModuleEnabled(String name) {
        return moduleStates.getOrDefault(name, false);
    }
}
