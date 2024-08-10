package net.runelite.client.plugins.microbot.geHandler;

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
        name = PluginDescriptor.Default + "geHandler",
        description = "Microbot example plugin",
        tags = {"example", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class geHandlerPlugin extends Plugin {
    @Inject
    private geHandlerConfig config;
    @Provides
    geHandlerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(geHandlerConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private geHandlerOverlay geHandlerOverlay;

    @Inject
    geHandlerScript geHandlerScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(geHandlerOverlay);
        }
        geHandlerScript.run(config);
    }

    protected void shutDown() {
        geHandlerScript.shutdown();
        overlayManager.remove(geHandlerOverlay);
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
