package com.osrshelper.agent;

// Minimal always-on-top overlay for toggling modules on/off at runtime.
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

public class OverlayController {
    private final Map<String, Boolean> moduleStates = new ConcurrentHashMap<>();
    private final Map<String, HelperModule> modules;
    private final JFrame frame;
    private static final Logger logger = Logger.getLogger("AgentLogger");
    private JComboBox<String> courseComboBox;

    public OverlayController(Map<String, HelperModule> modules) {
        this.modules = modules;
        for (String name : modules.keySet()) {
            moduleStates.put(name, false); // Default: disabled
        }
        frame = new JFrame("OSRS Helper Modules");
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(300, modules.size() * 40 + 60);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(modules.size() + 1, 2, 5, 5));

        // Course selection dropdown (for AgilityModule)
        HelperModule agilityModule = modules.get("agility");
        if (agilityModule instanceof AgilityModule) {
            AgilityModule am = (AgilityModule) agilityModule;
            String[] courseNames = am.getCourseNames();
            courseComboBox = new JComboBox<>(courseNames);
            courseComboBox.setSelectedIndex(0);
            courseComboBox.addActionListener(e -> {
                String selected = (String) courseComboBox.getSelectedItem();
                if (courseSelectionListener != null) {
                    courseSelectionListener.onCourseSelected(selected);
                }
            });
            panel.add(new JLabel("Agility Course:"));
            panel.add(courseComboBox);
        }

        for (String name : modules.keySet()) {
            JCheckBox checkBox = new JCheckBox(name, false); // Default: unchecked
            checkBox.addActionListener(e -> {
                boolean enabled = checkBox.isSelected();
                moduleStates.put(name, enabled);
                if (enabled) {
                    logger.info("Enabling module: " + name);
                    Agent.runModule(name);
                } else {
                    logger.info("Disabling module: " + name);
                    // Future: add stop logic if needed
                }
            });
            panel.add(new JLabel(name));
            panel.add(checkBox);
        }
        frame.add(panel);
    }

    // Listener interface for course selection
    public interface CourseSelectionListener {
        void onCourseSelected(String courseName);
    }

    private CourseSelectionListener courseSelectionListener;

    public void setCourseSelectionListener(CourseSelectionListener listener) {
        this.courseSelectionListener = listener;
        // Fire initial selection if possible
        if (courseComboBox != null && courseComboBox.getItemCount() > 0) {
            String selected = (String) courseComboBox.getSelectedItem();
            if (selected != null) {
                courseSelectionListener.onCourseSelected(selected);
            }
        }
    }

    public void show() {
        frame.setVisible(true);
    }

    public boolean isModuleEnabled(String name) {
        return moduleStates.getOrDefault(name, false);
    }
}
