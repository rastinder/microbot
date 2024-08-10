package net.runelite.client.plugins.microbot.rasCollectBones;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class rasCollectBonesOverlay extends OverlayPanel {
    private static final Color RED_TRANSLUCENT = new Color(255, 0, 0, 50);
    private void renderPolygon(Graphics2D graphics, Polygon polygon, Color color) {
        graphics.setColor(color);
        graphics.drawPolygon(polygon);
        graphics.fillPolygon(polygon);
    }
    private rasCollectBonesScript script;
    private rasCollectBonesConfig config;
    @Inject
    rasCollectBonesOverlay(rasCollectBonesPlugin plugin, rasCollectBonesScript script,rasCollectBonesConfig config)
    {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(200, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Ras attack loot" + rasCollectBonesScript.version)
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Microbot.status)
                    .build());
        } catch(Exception ex) {
            System.out.println("overlay error "+ ex.getMessage());
        }
        return super.render(graphics);
    }
}
