package net.runelite.client.plugins.microbot.rascleanleaf;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "ras cleanleaf",
        description = "Microbot example plugin",
        tags = {"example", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class rascleanleafPlugin extends Plugin {
    @Inject
    private rascleanleafConfig config;
    @Provides
    rascleanleafConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(rascleanleafConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private rascleanleafOverlay rascleanleafOverlay;

    @Inject
    rascleanleafScript rascleanleafScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(rascleanleafOverlay);
        }
        rascleanleafScript.run(config);
    }

    protected void shutDown() {
        rascleanleafScript.shutdown();
        overlayManager.remove(rascleanleafOverlay);
    }
}
