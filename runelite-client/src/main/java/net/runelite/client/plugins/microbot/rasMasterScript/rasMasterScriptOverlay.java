package net.runelite.client.plugins.microbot.rasMasterScript;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript.*;

public class rasMasterScriptOverlay extends OverlayPanel {
    @Inject
    rasMasterScriptOverlay(rasMasterScriptPlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            String formattedCoins = totalCoins >= 1_000_000_000 ? totalCoins / 1_000_000_000 + "b" : totalCoins >= 1_000_000 ? totalCoins / 1_000_000 + "m" : totalCoins >= 1_000 ? totalCoins / 1_000 + "k" : String.valueOf(totalCoins);
            panelComponent.setPreferredSize(new Dimension(200, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Master Script" + rasMasterScriptScript.version)
                    .color(Color.GREEN)
                    .build());
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("money " + formattedCoins)
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(formattedTime)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("run "+ currentPluginName)
                    .build());


        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
