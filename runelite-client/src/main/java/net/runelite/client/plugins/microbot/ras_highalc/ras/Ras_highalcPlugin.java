package net.runelite.client.plugins.microbot.ras_highalc.ras;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "ras_high alc",
        description = "Ras da plugin",
        tags = {"Ras", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class Ras_highalcPlugin extends Plugin {
    @Inject
    private Ras_highalcConfig config;
    @Provides
    Ras_highalcConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(Ras_highalcConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private Ras_highalcOverlay rasHighalcOverlay;

    @Inject
    Ras_highalcScript rasHighalcScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(rasHighalcOverlay);
        }
        rasHighalcScript.run(config);
    }

    protected void shutDown() {
        rasHighalcScript.shutdown();
        overlayManager.remove(rasHighalcOverlay);
    }

}
