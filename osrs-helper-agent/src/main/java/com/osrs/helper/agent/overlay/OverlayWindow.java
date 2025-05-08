package com.osrs.helper.agent.overlay;

import com.osrs.helper.agent.helpermodules.AgentModule;
import com.osrs.helper.agent.listeners.ModuleToggleListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;

/**
 * OverlayWindow is a Java Swing overlay for controlling agent modules.
 * This window allows enabling/disabling modules via checkboxes.
 */
public class OverlayWindow extends JFrame {
    private final List<AgentModule> modules;
    private final JPanel modulePanel;
    private final ModuleToggleListener toggleListener;
    private final JPanel configPanel = new JPanel(new BorderLayout());

    public OverlayWindow(List<AgentModule> modules, ModuleToggleListener toggleListener) {
        this.modules = modules;
        this.toggleListener = toggleListener;
        setTitle("OSRS Helper Agent Overlay");
        setSize(400, 300);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        modulePanel = new JPanel();
        modulePanel.setLayout(new BoxLayout(modulePanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(modulePanel), BorderLayout.CENTER);
        add(configPanel, BorderLayout.SOUTH);

        populateModuleControls();
    }

    private void populateModuleControls() {
        modulePanel.removeAll();
        ButtonGroup group = new ButtonGroup();
        for (AgentModule module : modules) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JCheckBox checkBox = new JCheckBox(module.getName(), module.isEnabled());
            checkBox.addItemListener(e -> {
                boolean shouldEnable = checkBox.isSelected();
                if (toggleListener != null) {
                    toggleListener.onModuleToggled(module, shouldEnable);
                }
                checkBox.setSelected(module.isEnabled());
            });
            row.add(checkBox);
            JButton configButton = new JButton("Options");
            configButton.addActionListener(e -> showModuleConfig(module));
            row.add(configButton);
            modulePanel.add(row);
            group.add(checkBox);
        }
        modulePanel.revalidate();
        modulePanel.repaint();
    }

    private void showModuleConfig(AgentModule module) {
        configPanel.removeAll();
        // If the module provides a config panel, show it
        try {
            java.lang.reflect.Method m = module.getClass().getMethod("getConfigPanel");
            JPanel panel = (JPanel) m.invoke(module);
            configPanel.add(panel, BorderLayout.CENTER);
        } catch (Exception e) {
            configPanel.add(new JLabel("No options available for this module."), BorderLayout.CENTER);
        }
        configPanel.revalidate();
        configPanel.repaint();
    }

    public void showOverlay() {
        setVisible(true);
    }

    public void hideOverlay() {
        setVisible(false);
    }
}
