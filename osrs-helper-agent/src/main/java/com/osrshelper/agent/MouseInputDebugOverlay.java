package com.osrshelper.agent;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import java.awt.*;

public class MouseInputDebugOverlay extends Overlay {
    private final MouseInputService mouseInputService;
    private final Client client;

    public MouseInputDebugOverlay(MouseInputService mouseInputService, Client client) {
        super();
        this.mouseInputService = mouseInputService;
        this.client = client;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        Shape clickbox = mouseInputService.getLastClickbox();
        if (clickbox != null) {
            graphics.setColor(new Color(255, 0, 0, 120));
            graphics.draw(clickbox);
            graphics.setColor(new Color(255, 0, 0, 40));
            graphics.fill(clickbox);
        }
        return null;
    }
}
