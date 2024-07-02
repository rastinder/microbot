package net.runelite.client.plugins.microbot.quest;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.questhelper.QuestHelperPlugin;
import net.runelite.client.plugins.questhelper.questhelpers.QuestHelper;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft + "MQuester",
        description = "Microbot quest plugin",
        tags = {"quest", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class MQuestPlugin extends Plugin {
    @Inject
    private MQuestConfig config;
    @Provides
    MQuestConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MQuestConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private MQuestOverlay exampleOverlay;

    @Inject
    MQuestScript questScript;

    @Inject
    private PluginManager pluginManager;

    private QuestHelperPlugin questHelper;


    @Override
    protected void startUp() throws AWTException {
        questHelper = (QuestHelperPlugin) pluginManager.getPlugins().stream()
                .filter(p -> p instanceof QuestHelperPlugin)
                .findFirst()
                .orElse(null);

        if (questHelper == null) {
            log.error("QuestHelperPlugin not found. Make sure it's enabled.");
        }
        if (overlayManager != null) {
            overlayManager.add(exampleOverlay);
        }
        questScript.run(config, questHelper);
    }

    protected void shutDown() {
        questScript.shutdown();
        overlayManager.remove(exampleOverlay);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        questScript.onChatMessage(chatMessage);
    }
}
