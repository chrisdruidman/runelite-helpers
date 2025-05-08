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

    public OverlayWindow(List<AgentModule> modules, ModuleToggleListener toggleListener) {
        this.modules = modules;
        this.toggleListener = toggleListener;
        setTitle("OSRS Helper Agent Overlay");
        setSize(350, 200);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        modulePanel = new JPanel();
        modulePanel.setLayout(new BoxLayout(modulePanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(modulePanel), BorderLayout.CENTER);

        populateModuleControls();
    }

    private void populateModuleControls() {
        modulePanel.removeAll();
        for (AgentModule module : modules) {
            JCheckBox checkBox = new JCheckBox(module.getName(), module.isEnabled());
            checkBox.addItemListener(e -> {
                boolean shouldEnable = checkBox.isSelected();
                if (toggleListener != null) {
                    toggleListener.onModuleToggled(module, shouldEnable);
                }
                // Update checkbox state to reflect actual module state
                checkBox.setSelected(module.isEnabled());
            });
            modulePanel.add(checkBox);
        }
        modulePanel.revalidate();
        modulePanel.repaint();
    }

    public void showOverlay() {
        setVisible(true);
    }

    public void hideOverlay() {
        setVisible(false);
    }
}
