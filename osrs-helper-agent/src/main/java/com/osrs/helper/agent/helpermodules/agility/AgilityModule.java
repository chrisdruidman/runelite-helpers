package com.osrs.helper.agent.helpermodules.agility;

import com.osrs.helper.agent.helpermodules.AgentModule;

import java.util.Map;
import java.util.LinkedHashMap;
import javax.swing.*;

/**
 * Minimal scaffold for the Agility helper module.
 * This module will be extended to provide automation and overlays for the Agility skill.
 */
public class AgilityModule implements AgentModule {
    private boolean enabled = false;
    private final Map<String, AgilityCourse> courses = new LinkedHashMap<>();
    private AgilityCourse selectedCourse = null;
    private JPanel configPanel;

    public AgilityModule() {
        // Register available courses
        AgilityCourse canifis = new CanifisCourse();
        courses.put(canifis.getName(), canifis);
        // Future: add more courses here
    }

    @Override
    public void onEnable() {
        if (selectedCourse == null) {
            System.err.println("[AgilityModule] Cannot enable: No course selected!");
            JOptionPane.showMessageDialog(null, "Please select an agility course before enabling the module.", "Agility Module Warning", JOptionPane.WARNING_MESSAGE);
            enabled = false;
            return;
        }
        enabled = true;
        System.out.println("AgilityModule enabled for course: " + selectedCourse.getName());
    }

    @Override
    public void onDisable() {
        enabled = false;
        System.out.println("AgilityModule disabled");
    }

    @Override
    public String getName() {
        return "Agility Helper";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns a configuration panel for the overlay to display.
     */
    public JPanel getConfigPanel() {
        if (configPanel == null) {
            configPanel = new JPanel();
            configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
            configPanel.add(new JLabel("Select Rooftop Course:"));
            JComboBox<String> courseSelector = new JComboBox<>(courses.keySet().toArray(new String[0]));
            courseSelector.setSelectedItem(selectedCourse != null ? selectedCourse.getName() : null);
            courseSelector.addActionListener(e -> {
                String selected = (String) courseSelector.getSelectedItem();
                selectedCourse = courses.get(selected);
            });
            configPanel.add(courseSelector);
        }
        return configPanel;
    }

    public AgilityCourse getSelectedCourse() {
        return selectedCourse;
    }
}
