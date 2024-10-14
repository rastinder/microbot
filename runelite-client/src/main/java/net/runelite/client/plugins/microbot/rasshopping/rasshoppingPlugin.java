package net.runelite.client.plugins.microbot.rasshopping;

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
        name = PluginDescriptor.Default + "ras Shopping",
        description = "Microbot example plugin",
        tags = {"example", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class rasshoppingPlugin extends Plugin {
    @Inject
    private rasshoppingConfig config;
    @Provides
    rasshoppingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(rasshoppingConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private rasshoppingOverlay rasshoppingOverlay;

    @Inject
    rasshoppingScript rasshoppingScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(rasshoppingOverlay);
        }
        rasshoppingScript.run(config);
    }

    protected void shutDown() {
        rasshoppingScript.shutdown();
        overlayManager.remove(rasshoppingOverlay);
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
