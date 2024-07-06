package net.runelite.client.plugins.microbot.ras_highalc.ras;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class Ras_highalcOverlay extends OverlayPanel {
    private final Ras_highalcScript script;
    @Inject
    Ras_highalcOverlay(Ras_highalcPlugin plugin, Ras_highalcScript script)
    {
        super(plugin);
        this.script = script;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(400, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("RAS money making " + Ras_highalcScript.version)
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Ras_highalcScript.opitem)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder().build());

            long lastActiveTime = script.getInactivityTimer();
            String lastActiveTimeFormatted = getElapsedTime(lastActiveTime);

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(lastActiveTimeFormatted)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Microbot.status)
                    .build());

        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
    public String getElapsedTime(long lastActiveTime) {
        try {
            if (lastActiveTime == 0) {
                throw new IllegalStateException("lastActiveTime is 0");
            }

            long currentTime = System.currentTimeMillis();
            long elapsedMillis = currentTime - lastActiveTime;

            long seconds = (elapsedMillis / 1000) % 60;
            long minutes = (elapsedMillis / (1000 * 60)) % 60;

            return String.format("%02d:%02d", minutes, seconds);
        } catch (Exception e) {
            return "00:00";
        }
    }
}
