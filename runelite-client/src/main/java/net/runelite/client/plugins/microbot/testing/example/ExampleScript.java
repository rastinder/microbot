package net.runelite.client.plugins.microbot.testing.example;

import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.rasCollectFood.rasCollectFoodConfig;
import net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript.homeTeleport;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginManager;

import javax.inject.Inject;


public class ExampleScript extends Script {
    public static double version = 1.0;
    @Inject
    private  ConfigManager configManager;

    @Inject
    public ExampleScript(PluginManager pluginManager, ConfigManager configManager) {
        this.configManager = configManager;
    }

    public boolean run(ExampleConfig config1) {
        Microbot.enableAutoRunOn = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                Rs2Tab.switchToQuestTab();
                Rs2Widget.clickWidget(629,3);
                String accountName = Rs2Widget.getWidget(712,1).getText();
                Rs2Widget.clickWidget(String.valueOf(Rs2Widget.getWidget(612,2).getDynamicChildren()[100]));
                Rs2Widget.clickWidget("again");
                String timeplayed = Rs2Widget.getWidget(712,1).getText();


            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
        return true;
    }
    public void setPluginConfig(String pluginName, String configKey, String configValue) {
        //if (!Microbot.isLoggedIn()) return;
        //if (!super.run()) return;
        //long startTime = System.currentTimeMillis();
        //rasCollectFoodConfig config = configManager.getConfig(rasCollectFoodConfig.class);
        //String alch = Rs2Widget.getWidget(134,18).getDynamicChildren()[117].getText();
        //System.out.println(alch);
        homeTeleport();
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
