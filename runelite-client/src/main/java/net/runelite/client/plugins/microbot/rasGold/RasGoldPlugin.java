package net.runelite.client.plugins.microbot.rasGold;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.mouse.VirtualMouse;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "Ras gold Maker",
        description = "Ras da plugin",
        tags = {"Ras", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class RasGoldPlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private RasGoldConfig config;
    @Inject
    private ClientThread clientThread;
    @Inject
    Notifier notifier;
    @Provides
    RasGoldConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(RasGoldConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private RasGoldOverlay rasGoldOverlay;

    @Inject
    RasGoldScript rasGoldScript;


    @Override
    protected void startUp() throws AWTException {
        Microbot.pauseAllScripts = false;
        Microbot.setClient(client);
        Microbot.setClientThread(clientThread);
        Microbot.setNotifier(notifier);
        Microbot.setMouse(new VirtualMouse());
        if (overlayManager != null) {
            overlayManager.add(rasGoldOverlay);
        }
        rasGoldScript.run(config);
    }

    protected void shutDown() {
        rasGoldScript.shutdown();
        overlayManager.remove(rasGoldOverlay);
    }
}
