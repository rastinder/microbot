package net.runelite.client.plugins.microbot.allz;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.plugins.PluginManager;
import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "Allz Quester",
        description = "Microbot allz plugin",
        tags = {"allz", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class AllzPlugin extends Plugin {
    @Inject
    private AllzConfig config;
    @Provides
    AllzConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AllzConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AllzOverlay allzOverlay;

    @Inject
    AllzScript allzScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(allzOverlay);
        }
        allzScript.run(config);
    }

    protected void shutDown() {
        allzScript.shutdown();
        overlayManager.remove(allzOverlay);
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
