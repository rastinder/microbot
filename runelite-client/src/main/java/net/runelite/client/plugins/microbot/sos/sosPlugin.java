package net.runelite.client.plugins.microbot.sos;

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
        name = PluginDescriptor.Default + "sos",
        description = "Microbot firstTimeChecks plugin",
        tags = {"firstTimeChecks", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class sosPlugin extends Plugin {
    @Inject
    private sosConfig config;
    @Provides
    sosConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(sosConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private sosOverlay sosOverlay;

    @Inject
    sosScript sosScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(sosOverlay);
        }
        sosScript.run(config);
    }

    protected void shutDown() {
        sosScript.shutdown();
        overlayManager.remove(sosOverlay);
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
