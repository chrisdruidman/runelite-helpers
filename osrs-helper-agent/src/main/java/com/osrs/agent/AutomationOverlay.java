package com.osrs.agent;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.LineComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AutomationOverlay extends Overlay {
    private final PanelComponent panel = new PanelComponent();
    private boolean buttonHovered = false;
    private Rectangle buttonBounds = new Rectangle(0, 0, 120, 24);

    public AutomationOverlay() {
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        // Add mouse listener for button
        Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
            if (e instanceof MouseEvent && ((MouseEvent) e).getID() == MouseEvent.MOUSE_CLICKED) {
                MouseEvent me = (MouseEvent) e;
                if (buttonBounds.contains(me.getPoint())) {
                    AgentMain.setAutomationEnabled(!AgentMain.isAutomationEnabled());
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        System.out.println("[AutomationOverlay] render() called");
        panel.getChildren().clear();
        panel.getChildren().add(TitleComponent.builder().text("OSRS Agent Automation").build());
        panel.getChildren().add(LineComponent.builder()
            .left("Status:")
            .right(AgentMain.isAutomationEnabled() ? "Enabled" : "Disabled")
            .build());
        // Draw toggle button
        String buttonText = AgentMain.isAutomationEnabled() ? "Disable Automation" : "Enable Automation";
        panel.getChildren().add(LineComponent.builder().left(" ").right(" ").build());
        panel.getChildren().add(LineComponent.builder().left(buttonText).build());
        // Draw the panel and get its bounds
        Dimension dim = panel.render(graphics);
        // Use a fixed offset for the button position
        buttonBounds.setLocation(10, 40);
        graphics.setColor(buttonHovered ? Color.LIGHT_GRAY : Color.GRAY);
        graphics.fillRect(buttonBounds.x, buttonBounds.y, buttonBounds.width, buttonBounds.height);
        graphics.setColor(Color.BLACK);
        graphics.drawRect(buttonBounds.x, buttonBounds.y, buttonBounds.width, buttonBounds.height);
        graphics.drawString(buttonText, buttonBounds.x + 10, buttonBounds.y + 16);
        return dim;
    }
}
