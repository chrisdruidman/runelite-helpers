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
import net.runelite.client.input.MouseListener;
import java.awt.*;
import java.awt.event.MouseEvent;

public class AutomationOverlay extends Overlay implements MouseListener {
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
        // Ensure overlay bounds always match the drawn area for mouse event routing
        this.getBounds().setBounds(0, 0, 220, 110);
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
    public MouseEvent mouseClicked(MouseEvent event) {
        if (buttonBounds.contains(event.getPoint())) {
            AgentMain.setAutomationEnabled(!AgentMain.isAutomationEnabled());
            return null; // Consume the event so it does not fall through
        }
        return event;
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent event) {
        boolean wasHovered = buttonHovered;
        buttonHovered = buttonBounds.contains(event.getPoint());
        // If hover state changed, request overlay redraw (RuneLite will handle this)
        return event;
    }

    // Implement other MouseListener methods as no-ops
    @Override
    public MouseEvent mousePressed(MouseEvent event) {
        if (buttonBounds.contains(event.getPoint())) {
            return null; // Also consume on press for reliability
        }
        return event;
    }

    @Override public MouseEvent mouseReleased(MouseEvent event) { return event; }
    @Override public MouseEvent mouseEntered(MouseEvent event) { return event; }
    @Override public MouseEvent mouseExited(MouseEvent event) { return event; }
    @Override public MouseEvent mouseDragged(MouseEvent event) { return event; }
}
