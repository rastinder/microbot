package net.runelite.client.plugins.microbot.rasCombine;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "ras Combine",
        description = "Microbot example plugin",
        tags = {"example", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class rasCombinePlugin extends Plugin {
    @Inject
    private rasCombineConfig config;
    @Provides
    rasCombineConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(rasCombineConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private rasCombineOverlay rasCombineOverlay;

    @Inject
    rasCombineScript rasCombineScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(rasCombineOverlay);
        }
        rasCombineScript.run(config);
    }

    protected void shutDown() {
        rasCombineScript.shutdown();
        overlayManager.remove(rasCombineOverlay);
    }
}
