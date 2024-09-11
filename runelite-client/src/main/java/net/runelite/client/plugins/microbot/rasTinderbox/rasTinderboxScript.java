package net.runelite.client.plugins.microbot.rasTinderbox;

import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.geHandler.geHandlerScript;
import net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;


public class rasTinderboxScript extends Script {
    public static long stopTimer = 1;
    WorldArea shopArea = new WorldArea(3088,3251,6,5,0);
    WorldPoint doorLocation = new WorldPoint(3088,3251,0);
    WorldPoint tindershelf = new WorldPoint(3291,3254,0);
    //WorldPoint bank = new WorldPoint(3088,3251,0);
    int doorClosed = 1535;

    public boolean run(rasTinderboxConfig config) {
        Microbot.enableAutoRunOn = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (rasMasterScriptScript.autoShutdown("ras Tinderbox")) return;
                if (!super.run()) return;
                if (stopTimer == 1)
                    stopTimer = rasMasterScriptScript.autoStopTimer();
                long startTime = System.currentTimeMillis();

                if (Rs2Inventory.isFull() ){
                   // if (Rs2GameObject.interact(doorLocation,"Open"))
                   //     Rs2Player.waitForAnimation();
                    Rs2Bank.walkToBank();
                    Rs2Player.waitForAnimation();
                    Rs2Bank.openBank();
                    sleepUntilTrue(Rs2Bank::isOpen,100,10000);
                    Rs2Bank.depositAll();
                    sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                    if (stopTimer < System.currentTimeMillis()){
                        geHandlerScript.goSell(false,5,new int[]{-1},"Tinderbox");
                        shutdown();
                    }
                }
                if (Rs2Inventory.isEmpty() && !shopArea.contains(Rs2Player.getWorldLocation())){
                    WorldPoint shopAreaPoint = shopArea.toWorldPointList().get(Random.random(0, shopArea.toWorldPointList().size() - 1));
                    if (!Rs2Tile.isTileReachable(tindershelf))
                        Rs2Walker.walkTo(shopAreaPoint,0);
                    else
                        Rs2Walker.walkCanvas(shopAreaPoint);
                    //if (Rs2Player.getWorldLocation().distanceTo(doorLocation) < 5 && Rs2GameObject.interact(doorClosed,"Open"))
                    //    Rs2Player.waitForAnimation();
                }
                if (shopArea.contains(Rs2Player.getWorldLocation()) && TinderboxOnGround(27) && Rs2Inventory.ItemQuantity(590) == 1) {
                    // collect from ground
                    while (!Rs2Inventory.isFull()) {
                        Rs2GroundItem.loot(590);
                        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                    }


                }else if(shopArea.contains(Rs2Player.getWorldLocation()) && !Rs2Inventory.isFull()) {
                    Rs2Tab.switchToInventoryTab();
                    // intract
                    Rs2GameObject.interact(7079,"Open");
                    Rs2Walker.setTarget(null);
                    sleepUntilTrue(Rs2Dialogue::hasContinue,100,5000);
                    if (!Rs2Widget.hasWidget("tinderboxes")){
                        Rs2Npc.interact(2108,"Talk-to");
                        sleepUntilTrue(()->Rs2Widget.hasWidget("Greetings"),100,5000);
                        while (Rs2Dialogue.isInDialogue()){
                            Rs2Dialogue.clickContinue();
                            if (Rs2Widget.hasWidget("some other time")||(Rs2Widget.hasWidget("junk,")))
                                break;
                        }
                        Rs2GameObject.interact(7079,"Open");
                        sleepUntilTrue(Rs2Dialogue::hasContinue,100,5000);
                    }
                    if (Rs2Inventory.hasItem("Tinderbox") && !TinderboxOnGround(27))
                        Rs2Inventory.dropAll();
                }


                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
               // System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    private boolean TinderboxOnGround(int requiredAmount) {
        int tinderboxCount = 0;
        Rs2GroundItem.getAll(1);
        RS2Item[]  groundItems = Rs2GroundItem.getAll(1);
        for (RS2Item item : groundItems) {
            if (item.getItem().getId() == ItemID.TINDERBOX) {
                tinderboxCount++;
            }
        }

        return tinderboxCount >= requiredAmount;
    }

    @Override
    public void shutdown() {
        stopTimer = 1;
        rasMasterScriptScript.stopPlugin("ras Tinderbox");
        super.shutdown();
    }
}
