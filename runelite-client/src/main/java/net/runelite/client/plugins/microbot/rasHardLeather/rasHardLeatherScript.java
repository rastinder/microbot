package net.runelite.client.plugins.microbot.rasHardLeather;

import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.Config;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.geHandler.geHandlerScript;
import net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;


public class rasHardLeatherScript extends Script {
    public static double version = 1.0;
    public static long stopTimer = 1;
    public WorldArea collectCowhide = new WorldArea(3254, 3289, 10, 10, 0);
    public WorldArea collectCoins = new WorldArea(3251, 3235, 10, 10, 0);
    public WorldArea leatherShop = new WorldArea(3275, 3191, 1, 1, 0);
    public long lastLootTime = 0;
    boolean allCoinsInInv = false;
    int totalHardLeather = 0;
    boolean onlyloot = false;
    int coins = 995;
    int minCoins = 101;

    public boolean run(rasHardLeatherConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                rasMasterScriptScript.autoShutdown("ras HardLeather");
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (stopTimer == 1)
                    stopTimer = rasMasterScriptScript.autoStopTimer();
                hopworld();
                onlyloot = config.loot();
                long endTime = System.currentTimeMillis();
                if (Rs2Inventory.hasItem("Hard leather")) {
                    Rs2Bank.walkToBank();
                    sleepUntilTrue(() -> Rs2Bank.isNearBank(9),100, 10000);
                    Rs2Bank.openBank();
                    sleepUntilTrue(() -> Rs2Bank.isOpen(),100, 10000);
                    sleep(100, 200);
                    Rs2Bank.depositAllExcept(coins);
                    sleep(100, 200);
                    if (Rs2Inventory.ItemQuantity(coins) < minCoins) {
                        Rs2Bank.withdrawX(coins, 10000);
                        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 500);
                    }
                    if (Rs2Bank.hasBankItem(coins, minCoins)) {
                        allCoinsInInv = true;
                    } else {
                        allCoinsInInv = false;
                    }
                    if (Rs2Bank.hasBankItem("Hard leather", 500))
                        sleep(500); // gotograndexchange and sell all
                    if (stopTimer < System.currentTimeMillis()){
                        System.out.println("shutting down");
                        shutdownviaTime();return;
                    }
                    sleepUntil(() -> Rs2Bank.closeBank(), 1000);
                }
                if ( Rs2Inventory.isFull() && Rs2Inventory.hasItemAmount("Cowhide", 1)) {
                    if (Rs2Inventory.ItemQuantity(coins) < minCoins && allCoinsInInv) {
                        if(!rasMasterScriptScript.isinALKhrid() ||  Rs2Inventory.ItemQuantity(coins) > 10) {
                            lootfromGoblins();
                        }else {
                            rasMasterScriptScript.homeTeleport();
                            lootfromGoblins();
                        }
                    } else if (Rs2Inventory.ItemQuantity(coins) < minCoins && !allCoinsInInv) {
                        Rs2Bank.walkToBank();
                        Rs2Player.waitForWalking();
                        sleepUntil(() -> Rs2Bank.openBank(), 10000);
                        if (Rs2Bank.isOpen()) {
                            sleep(100, 200);
                            Rs2Bank.withdrawX(coins, 10000);
                            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                            if (Rs2Bank.hasBankItem(coins, minCoins)) {
                                allCoinsInInv = true;
                            } else {
                                allCoinsInInv = false;
                            }
                            sleepUntil(() -> Rs2Bank.closeBank(), 1000);
                            sleep(500, 800);
                        }
                    }
                    Rs2Walker.walkTo(leatherShop.toWorldPoint());
                    Rs2Player.waitForWalking();
                    while (Rs2Player.getWorldLocation().distanceTo(leatherShop) > 3) {
                        sleep(1500, 2000);
                        Rs2Walker.walkTo(leatherShop.toWorldPoint(), 1);
                    }
                    //sleepUntil(()->Rs2Player.getWorldLocation().distanceTo (leatherShop) < 11,50000);
                    NPC ellis = Rs2Npc.getNpc("Ellis");
                    if (ellis != null && !Rs2Widget.hasWidget("What hides")) {
                        Rs2Npc.interact(ellis, "Trade");
                        Rs2Walker.setTarget(null);
                        sleepUntil(() -> Rs2Widget.hasWidget("What hides"), 5000);
                        if (Rs2Widget.hasWidget("What hides")) {
                            sleep(100, 200);
                            Widget tanAll = Rs2Widget.getWidget(324, 93);
                            Microbot.getMouse().click(Rs2Widget.getWidget(324, 93).getBounds());
                            //sleep(300, 800);
                            System.out.println("go to bank");
                            sleepUntil(() -> Rs2Bank.walkToBank(), 10000);
                            Rs2Player.waitForWalking();
                            Rs2Bank.openBank();
                            sleepUntil(() -> Rs2Bank.isOpen(), 10000);
                            sleep(500, 800);
                            //Rs2Bank.depositAllExcept(coins);
                            Rs2Bank.depositAll("Hard leather");
                            sleepUntil(() -> !Rs2Inventory.hasItem("Hard leather"), 3000);
                            System.out.println("deposited");
                            if (Rs2Bank.hasBankItem(coins, 1000)) {
                                if (!Rs2Inventory.hasItem(coins) || Rs2Inventory.ItemQuantity(coins) < 200L) {
                                    Rs2Bank.withdrawX(coins, 1000);
                                }
                            }
                            sleep(300, 500);
                            if (!onlyloot) {
                                Rs2Bank.withdrawAll("Cowhide");
                                sleepUntil(() -> Rs2Inventory.hasItem("Cowhide"), 3000);
                            }
                            sleepUntil(() -> Rs2Bank.closeBank(), 1000);
                        }
                    }
                }
                if (onlyloot) {
                    System.out.println("stray " + Rs2Player.getWorldLocation().distanceTo(collectCowhide));
                    if (((System.currentTimeMillis() - lastLootTime) > 10000L) && Rs2Player.getWorldLocation().distanceTo(collectCowhide) > 11 && Rs2Inventory.ItemQuantity(coins) > minCoins && !Rs2Inventory.isFull()) {
                        Rs2Walker.walkTo(collectCowhide.toWorldPoint());
                        Rs2Player.waitForWalking();
                    } else if (Rs2Inventory.ItemQuantity(coins) > minCoins && !Rs2Inventory.isFull()) {
                        if (Rs2GroundItem.loot("Cowhide", 15)) {
                            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                            if (new java.util.Random().nextInt(2) == 1 && config.randomBonePick() && Rs2Inventory.size() < 20) {
                                if (Rs2GroundItem.pickup("Bones", 1)) {
                                    sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 2000);
                                    sleep(150, 250);
                                    while (Rs2Inventory.hasItem("Bones")) {
                                        Rs2Inventory.interact("Bones", "Bury");
                                        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 10, 2000);
                                        sleep(10, 25);
                                    }
                                }
                            }
                            lastLootTime = System.currentTimeMillis();
                        }
                    }
                    else if (Rs2Inventory.ItemQuantity(coins) <= minCoins && !Rs2Inventory.isFull()){
                        if(!rasMasterScriptScript.isinALKhrid() ||  Rs2Inventory.ItemQuantity(coins) > 10) {
                            lootfromGoblins();
                        }else {
                            rasMasterScriptScript.homeTeleport();
                            lootfromGoblins();
                        }
                    }
                } else if (!Rs2Inventory.hasItem("cowhide")) {
                    if (Rs2Bank.walkToBank()) {
                        Rs2Player.waitForWalking();
                        sleepUntil(() -> Rs2Bank.openBank(), 5000);
                        sleep(500, 800);
                    }
                    if (Rs2Bank.hasBankItem(coins, 1000)) {
                        if (!Rs2Inventory.hasItem(coins) || Rs2Inventory.ItemQuantity(coins) < 200L) {
                            Rs2Bank.withdrawX(coins, 1000);
                        }
                    } else shutdownviaTime();
                    if (Rs2Bank.hasItem("Cowhide")) {
                        Rs2Bank.withdrawAll("Cowhide");
                        sleepUntil(() -> Rs2Inventory.hasItem("Cowhide"), 1000);
                        sleepUntil(() -> Rs2Bank.closeBank(), 1000);
                    } else shutdownviaTime();
                }

            } catch (
                    Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }
    public void shutdownviaTime() {
        Rs2Bank.closeBank();
        if (rasMasterScriptScript.isinALKhrid() && Rs2Inventory.ItemQuantity(995) < minCoins)
            rasMasterScriptScript.homeTeleport();
        geHandlerScript.goSell(false,5,new int[]{0},"Hard leather");
        stopTimer = 1;
        shutdown();

    }
    @Override
    public void shutdown() {
        rasMasterScriptScript.stopPlugin("ras HardLeather");
        do{sleep(2000);}
        while (rasMasterScriptScript.isPlugEnabled("ras HardLeather"));
        super.shutdown();
    }

    private void lootfromGoblins() {
        if (Rs2Inventory.ItemQuantity(coins) <= minCoins && !Rs2Inventory.isFull()) {
            hopworld();
            System.out.println("collecting 101 coins");
            long timenow = System.currentTimeMillis();
            while (Rs2Inventory.ItemQuantity(coins) <= minCoins) {
                if (System.currentTimeMillis() - timenow > 380000)
                    shutdownviaTime();
                if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3250, 3232, 0)) < 10 && Rs2GroundItem.loot(coins)) {
                        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 15000);
                } else if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3250, 3232, 0)) > 5)
                    Rs2Walker.walkTo(new WorldPoint(3250, 3232, 0), random(1, 5));
                sleep(100, 200); // cpu chill
            }
        }
    }
    private void hopworld() {
        int world = Microbot.getClient().getWorld();
        while (world != 301 && world != 308) {
            Microbot.hopToWorld(301);
            boolean result = sleepUntil(() -> Rs2Widget.findWidget("Switch World") != null);
            if (result) {
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                sleepUntil(() -> Microbot.getClient().getGameState() == GameState.HOPPING);
                sleepUntil(() -> Microbot.getClient().getGameState() == GameState.LOGGED_IN);
            }
            if (Microbot.getClient().getGameState() == GameState.LOGIN_SCREEN){
                boolean isHopped = Microbot.hopToWorld(308);
                if (!isHopped) return;
                result = sleepUntil(() -> Rs2Widget.findWidget("Switch World") != null);
                if (result) {
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleepUntil(() -> Microbot.getClient().getGameState() == GameState.HOPPING);
                    sleepUntil(() -> Microbot.getClient().getGameState() == GameState.LOGGED_IN);
                }

            }
            world = Microbot.getClient().getWorld();
        }
        sleep(500);
        Rs2Tab.switchToInventoryTab();
    }
}
