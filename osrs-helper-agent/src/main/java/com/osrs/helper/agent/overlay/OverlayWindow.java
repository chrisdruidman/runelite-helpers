package com.osrs.helper.agent.overlay;

/**
 * Java Swing overlay window for module control.
 * <b>IMPORTANT:</b> This overlay is the only component that uses injected hooks/ASM. All other agent logic must use the minimal API exposed by patch files only.
 * Do NOT reference or depend on any code from runelite/ directly. This overlay is part of the hybrid patch-based approach.
 */

import com.osrs.helper.agent.helpermodules.AgentModule;
import com.osrs.helper.agent.helpermodules.agility.AgilityModule;
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
        if (module instanceof AgilityModule) {
            AgilityModule agilityModule = (AgilityModule) module;
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JLabel("Select Rooftop Course:"));
            JComboBox<String> courseSelector = new JComboBox<>(agilityModule.getCourseNames().toArray(new String[0]));
            String selected = agilityModule.getSelectedCourse() != null ? agilityModule.getSelectedCourse().getName() : null;
            courseSelector.setSelectedItem(selected);
            courseSelector.addActionListener(e -> {
                String selectedCourse = (String) courseSelector.getSelectedItem();
                agilityModule.setSelectedCourse(selectedCourse);
            });
            panel.add(courseSelector);
            // --- Agility automation options ---
            panel.add(new JLabel("Max Laps (0 = infinite):"));
            JSpinner lapSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 1000, 1));
            panel.add(lapSpinner);
            JButton startButton = new JButton("Start Automation");
            JButton stopButton = new JButton("Stop Automation");
            startButton.addActionListener(e -> {
                int maxLaps = (int) lapSpinner.getValue();
                agilityModule.setMaxLaps(maxLaps == 0 ? -1 : maxLaps);
                agilityModule.startAutomation();
            });
            stopButton.addActionListener(e -> agilityModule.stopAutomation());
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            buttonPanel.add(startButton);
            buttonPanel.add(stopButton);
            panel.add(buttonPanel);
            configPanel.add(panel, BorderLayout.CENTER);
        } else {
            // Fallback: no options available
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
