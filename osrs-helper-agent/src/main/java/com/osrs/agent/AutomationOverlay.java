package com.osrs.agent;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.OverlayMouseListener;
import java.awt.*;
import java.awt.event.MouseEvent;

public class AutomationOverlay extends Overlay implements OverlayMouseListener {
    private final PanelComponent panel = new PanelComponent();
    private Rectangle buttonBounds = new Rectangle(0, 0, 180, 32);
    private static final String BUTTON_TEXT_ENABLED = "Disable Automation";
    private static final String BUTTON_TEXT_DISABLED = "Enable Automation";
    private boolean buttonHovered = false;

    public AutomationOverlay() {
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPreferredSize(new Dimension(220, 110));
        // Add overlay menu entry for toggling automation
        addMenuEntry(MenuAction.RUNELITE_OVERLAY, "Toggle Automation", "OSRS Agent Automation", (menuEntry) -> {
            AgentMain.setAutomationEnabled(!AgentMain.isAutomationEnabled());
        });
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panel.getChildren().clear();
        panel.getChildren().add(TitleComponent.builder().text("OSRS Agent Automation").build());
        panel.getChildren().add(LineComponent.builder()
            .left("Status:")
            .right(AgentMain.isAutomationEnabled() ? "Enabled" : "Disabled")
            .build());
        panel.getChildren().add(LineComponent.builder().left("").build());
        // Draw the button
        String buttonText = AgentMain.isAutomationEnabled() ? BUTTON_TEXT_ENABLED : BUTTON_TEXT_DISABLED;
        int buttonX = 20;
        int buttonY = 55;
        buttonBounds.setBounds(buttonX, buttonY, 180, 32);
        graphics.setColor(buttonHovered ? new Color(180, 200, 255) : new Color(220, 220, 220));
        graphics.fillRect(buttonBounds.x, buttonBounds.y, buttonBounds.width, buttonBounds.height);
        graphics.setColor(Color.BLACK);
        graphics.drawRect(buttonBounds.x, buttonBounds.y, buttonBounds.width, buttonBounds.height);
        FontMetrics fm = graphics.getFontMetrics();
        int textWidth = fm.stringWidth(buttonText);
        int textX = buttonBounds.x + (buttonBounds.width - textWidth) / 2;
        int textY = buttonBounds.y + (buttonBounds.height + fm.getAscent()) / 2 - 4;
        graphics.drawString(buttonText, textX, textY);
        // Draw the panel (text above button)
        panel.setPreferredSize(new Dimension(220, 110));
        panel.setPreferredLocation(new Point(0, 0));
        panel.render(graphics);
        return new Dimension(220, 110);
    }

    @Override
    public void onMouseOver() {
        // Optionally highlight the button on hover (not required for menu click)
    }

    @Override
    public boolean mouseClicked(MouseEvent event) {
        if (buttonBounds.contains(event.getPoint())) {
            AgentMain.setAutomationEnabled(!AgentMain.isAutomationEnabled());
            event.consume(); // Prevents click from passing through
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseMoved(MouseEvent event) {
        boolean wasHovered = buttonHovered;
        buttonHovered = buttonBounds.contains(event.getPoint());
        if (buttonHovered != wasHovered) {
            // Request overlay redraw
            return true;
        }
        return false;
    }

    // Optionally, you can also override mousePressed/mouseReleased if needed
}
