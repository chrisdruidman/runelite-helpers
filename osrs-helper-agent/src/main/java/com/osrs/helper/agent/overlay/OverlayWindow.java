package com.osrs.helper.agent.overlay;

import javax.swing.*;
import java.awt.*;

/**
 * OverlayWindow is a Java Swing overlay for controlling agent modules.
 * This window will be extended to allow enabling/disabling modules and selecting options.
 */
public class OverlayWindow extends JFrame {
    public OverlayWindow() {
        setTitle("OSRS Helper Agent Overlay");
        setSize(350, 200);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel placeholder = new JLabel("Module controls will appear here.", SwingConstants.CENTER);
        add(placeholder, BorderLayout.CENTER);
    }

    public void showOverlay() {
        setVisible(true);
    }

    public void hideOverlay() {
        setVisible(false);
    }
}
