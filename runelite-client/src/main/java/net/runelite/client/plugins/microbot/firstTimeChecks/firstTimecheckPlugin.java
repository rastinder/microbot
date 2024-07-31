package net.runelite.client.plugins.microbot.firstTimeChecks;

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
        name = PluginDescriptor.Default + "ras firsttime check",
        description = "Microbot firstTimeChecks plugin",
        tags = {"firstTimeChecks", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class firstTimecheckPlugin extends Plugin {
    @Inject
    private firstTimecheckConfig config;
    @Provides
    firstTimecheckConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(firstTimecheckConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private firstTimecheckOverlay firstTimecheckOverlay;

    @Inject
    firstTimecheckScript firstTimecheckScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(firstTimecheckOverlay);
        }
        firstTimecheckScript.run(config);
    }

    protected void shutDown() {
        firstTimecheckScript.shutdown();
        overlayManager.remove(firstTimecheckOverlay);
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
