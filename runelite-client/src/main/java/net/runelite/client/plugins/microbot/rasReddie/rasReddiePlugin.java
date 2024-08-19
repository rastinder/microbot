package net.runelite.client.plugins.microbot.rasReddie;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "0",
        description = "Ras da plugin",
        tags = {"Ras", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class rasReddiePlugin extends Plugin {
    @Inject
    private rasReddieConfig config;
    @Provides
    rasReddieConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(rasReddieConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private rasReddieOverlay rasReddieOverlay;

    @Inject
    rasReddieScript rasReddieScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(rasReddieOverlay);
        }
        rasReddieScript.run(config);
    }

    protected void shutDown() {
        rasReddieScript.shutdown();
        overlayManager.remove(rasReddieOverlay);
    }
}
