package net.runelite.client.plugins.microbot.rasCombatTrain;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "ras CombatTrain",
        description = "Microbot example plugin",
        tags = {"example", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class rasCombatTrainPlugin extends Plugin {
    @Inject
    private rasCombatTrainConfig config;
    @Provides
    rasCombatTrainConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(rasCombatTrainConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private rasCombatTrainOverlay rasCombatTrainOverlay;

    @Inject
    rasCombatTrainScript rasCombatTrainScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(rasCombatTrainOverlay);
        }
        rasCombatTrainScript.run(config);
    }

    protected void shutDown() {
        rasCombatTrainScript.shutdown();
        overlayManager.remove(rasCombatTrainOverlay);
    }
    int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));

        if (ticks > 0) {
            ticks--;
        } else {
            ticks = 10;
        }

    }

}
