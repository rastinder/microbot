package net.runelite.client.plugins.microbot.rasCollectBones;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "ras range bone collector",
        description = "Microbot example plugin",
        tags = {"example", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class rasCollectBonesPlugin extends Plugin {
    @Inject
    private rasCollectBonesConfig config;
    @Provides
    rasCollectBonesConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(rasCollectBonesConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private rasCollectBonesOverlay rasCollectBonesOverlay;

    @Inject
    rasCollectBonesScript rasCollectBonesScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(rasCollectBonesOverlay);
        }
        rasCollectBonesScript.run(config);
    }

    protected void shutDown() {
        rasCollectBonesScript.shutdown();
        overlayManager.remove(rasCollectBonesOverlay);
    }
}
