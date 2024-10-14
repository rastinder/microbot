package net.runelite.client.plugins.microbot.testing;

import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.grandexchange.GrandExchangePlugin;
import net.runelite.client.plugins.grounditems.GroundItem;
import net.runelite.client.plugins.grounditems.GroundItemsPlugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import net.runelite.client.eventbus.Subscribe;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import javax.inject.Inject;
import javax.swing.*;

import static net.runelite.api.Varbits.DISABLE_LEVEL_UP_INTERFACE;
import static net.runelite.client.plugins.grandexchange.GrandExchangePlugin.GE_SLOTS;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;


public class testingScript extends Script {
    public static long stopTimer = 1;
    @Inject
    private  ConfigManager configManager;
    @Inject
    private  ItemManager itemManager;
    @Inject
    private Client client;
    public static int boughtQuantity = 0;



    public boolean run(testingConfig config) {
        Microbot.enableAutoRunOn = false;
        rasMasterScriptScript ras = new rasMasterScriptScript();

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (rasMasterScriptScript.autoShutdown("testing sc"))return;
                System.out.println( Microbot.getClient().getGameState());
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (stopTimer == 1)
                    stopTimer = rasMasterScriptScript.autoStopTimer();
                long startTime = System.currentTimeMillis();
                try {
                    lootCoinsOutsideArea();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
                try {
                    //System.out.println( configManager.getRSProfileConfiguration("geoffer", Integer.toString(1)));
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }


                //CODE HERE

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                //System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }
    private void hopworld() {
        int world = Microbot.getClient().getWorld();
        while (world != 301) {
            Microbot.hopToWorld(301);
            boolean result = sleepUntil(() -> Rs2Widget.findWidget("Switch World") != null);
            if (result) {
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                sleepUntil(() -> Microbot.getClient().getGameState() == GameState.HOPPING);
                sleepUntil(() -> Microbot.getClient().getGameState() == GameState.LOGGED_IN);
            }
            if (Microbot.getClient().getGameState() == GameState.LOGIN_SCREEN){
                new Login(308);
                sleepUntil(() -> Microbot.getClient().getGameState() == GameState.HOPPING);
                sleepUntil(() -> Microbot.getClient().getGameState() == GameState.LOGGED_IN);
            }
            world = Microbot.getClient().getWorld();
            sleep(500);
        }
        Rs2Tab.switchToInventoryTab();
    }

    public void varbitValue(int varbit, int value) {
        Microbot.getClientThread().runOnClientThread(() -> {
            try {
                if (Microbot.getClient().getVarbitValue(varbit) != value) {
                    client.setVarbitValue(client.getVarps(), varbit, value);
                    VarbitComposition varbitComposition = client.getVarbit(varbit);
                    client.queueChangedVarp(varbitComposition.getIndex());
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Set varbit " + varbit + " to " + value, null);
                    VarbitChanged varbitChanged = new VarbitChanged();
                    varbitChanged.setVarbitId(varbit);
                    varbitChanged.setValue(value);
                }
                else {
                    System.out.println("already setting is done");
                }
                //eventBus.post(varbitChanged); // fake event
            } catch (Exception e) {
                System.out.println(e);
            }
            return null; // or return some appropriate value
        });
    }

    public void varbitValue1(int varbit, int value) {
        Microbot.getClientThread().runOnClientThread(() -> {
            try {
                if (Microbot.getClient().getVarbitValue(varbit) != value) {
                    Microbot.getClient().setVarbit(varbit, value);
                }
            } catch (Exception e) {
                System.out.println(e);
            }
            return null; // or return some appropriate value
        });
    }
    public void lootCoinsOutsideArea() {
        //WorldArea coinArea = new WorldArea(3243, 3248, 3248 - 3243, 3248 - 3244, 0);
        WorldArea coinArea = new WorldArea(3243, 3244, 5, 4, 0);
        int range = 50;
        List<GroundItem> gitems = getGroundItems("Coins",range);
        for (GroundItem gcoin : gitems) {
            WorldPoint coinLocation = gcoin.getLocation();
            if (!coinLocation.isInArea(coinArea)) {
                if (Rs2GroundItem.interact(gcoin)) {
                    sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)) , 100, 15000);
                }
            }
            else{
                System.out.println("in the area so skip");
            }
        }
    }
    public List<GroundItem> getGroundItems(String itemName, int range) {
        return GroundItemsPlugin.getCollectedGroundItems().values().stream()
                .filter(groundItem -> groundItem.getName().equalsIgnoreCase(itemName) && groundItem.getLocation().distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation()) < range)
                .collect(Collectors.toList());
    }

    @Override
    public void shutdown() {
        stopTimer = 1;
        //eventBus.unregister(this);
        rasMasterScriptScript.stopPlugin("testing sc");
        super.shutdown();
    }
}
