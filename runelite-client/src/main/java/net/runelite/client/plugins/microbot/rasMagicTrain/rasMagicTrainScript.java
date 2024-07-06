package net.runelite.client.plugins.microbot.rasMagicTrain;

import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.random;
import static java.lang.reflect.Array.get;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory.items;


public class rasMagicTrainScript extends Script {
    public static double version = 1.0;
    WorldPoint grabLocation = new WorldPoint(3191,9825,0);
    WorldPoint bankLocation = new WorldPoint(3185,3436,0);

    public boolean run(rasMagicTrainConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                superHeat();


                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
    void hopWorld(){
        int world = Login.getRandomWorld(Rs2Player.isMember(), null);
        boolean isHopped = Microbot.hopToWorld(world);
        if (!isHopped) return;
        boolean result = sleepUntil(() -> Rs2Widget.findWidget("Switch World") != null);
        if (result) {
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleepUntil(() -> Microbot.getClient().getGameState() == GameState.HOPPING,5000);
            sleepUntil(() -> Microbot.getClient().getGameState() == GameState.LOGGED_IN,5000);
            sleep(1000, 1500);
        }
    }
    public void superHeat(){
        String rune = "Nature rune";
        String ironore = "Iron ore";
        String tinore = "Coal";
        if (Rs2Inventory.hasItem(rune) && Rs2Inventory.hasItem(ironore) && Rs2Inventory.hasItem(tinore)) {
                System.out.println("Casting Superheat Item on Iron ore and Tin ore.");
                Rs2Magic.cast(MagicAction.SUPERHEAT_ITEM);
                Rs2Inventory.interact(ironore);
                sleep(2800-3200); // Wait for the casting animation to complete
        } else if (!Rs2Inventory.hasItem(ironore) && Rs2Inventory.hasItem(rune)) {
            // Walk to bank location and deposit bars
            Rs2Bank.openBank();
            sleepUntil(() -> Rs2Bank.openBank(), 5000);
            Rs2Bank.depositAllExcept("Nature rune");
            sleepUntil(() -> Rs2Inventory.fullSlotCount() == 1, 5000);

            // Withdraw iron and tin ores in a 1:2 ratio to fill 27 slots
            int ironOreToWithdraw = 9; // 1/3 of 27 slots
            int tinOreToWithdraw = 18; // 2/3 of 27 slots

            Rs2Bank.withdrawX(ironore, ironOreToWithdraw);
            sleep(500);
            Rs2Bank.withdrawX(tinore, tinOreToWithdraw);
            sleep(500);
            if (Rs2Inventory.hasItem(ironore) && Rs2Inventory.hasItem(tinore))
                Rs2Bank.closeBank();
            sleep(500);
        } else {
            Rs2Bank.openBank();
            sleepUntilTrue(Rs2Bank::openBank,100,5000);
            sleep(500);
            Rs2Bank.setWithdrawAsNote();
            sleep(500);
            Rs2Bank.withdrawAll("Steel bar");
            sleep(500);
            Rs2Bank.closeBank();
            sleep(500);
            System.out.println("Missing Nature runes, Iron ore, or Tin ore.");
            int  iron = Microbot.getItemManager().search("Iron ore").get(0).getPrice();
            int coal = Microbot.getItemManager().search("Coal").get(0).getPrice();
            RasClass.grandExchangeOperations(new Random().nextInt(50000)+300000,106,iron,coal,1,1,2);
        }

    }
    public void teleGrab(){
        if (Rs2Inventory.hasItem("law Rune") && !Rs2Inventory.isFull() ){
            System.out.println("distance  " + grabLocation.distanceTo(Rs2Player.getWorldLocation()));
            if (grabLocation.distanceTo(Rs2Player.getWorldLocation()) == 0) {
                RS2Item[] groundItems = Microbot.getClientThread().runOnClientThread(() -> Rs2GroundItem.getAll(7));

                // Shuffle the array using Fisher-Yates algorithm
                Random random = new Random();
                for (int i = groundItems.length - 1; i > 0; i--) {
                    int index = random.nextInt(i + 1);
                    RS2Item temp = groundItems[index];
                    groundItems[index] = groundItems[i];
                    groundItems[i] = temp;
                }
                for (RS2Item item : groundItems) {
                    System.out.println("found   " + item.getItem().getName());
                    if ((Rs2GroundItem.hasLineOfSight(item.getTile()) && !item.getItem().getName().contains("Coins")) || item.getItem().getName().contains("Gold ore")) {
                        System.out.println("grab   " + item.getItem().getName());
                        Rs2Magic.cast(MagicAction.TELEKINETIC_GRAB);
                        int count = Rs2Inventory.fullSlotCount();
                        sleep(350, 380);
                        Rs2GroundItem.interact(item);
                        sleep(200);
                        sleepUntil(()->Rs2Inventory.fullSlotCount() > count, 5000);
                        sleep(180, 340);
                        if (Rs2Inventory.isFull()) break;
                    }
                }
                if(!Rs2Inventory.isFull())
                    hopWorld();
            }
            else if (grabLocation.distanceTo(Rs2Player.getWorldLocation()) > 9)
                Rs2Walker.walkTo(grabLocation);
            else Rs2Walker.walkFastCanvas(grabLocation);
            Rs2Player.waitForAnimation(500);

        } else if (Rs2Inventory.isFull()) {
            Rs2Walker.walkTo(bankLocation);
            waitForAnimationStopnDoor();
            Rs2Bank.openBank();
            sleepUntil(() -> Rs2Bank.openBank(), 5000);
            Rs2Bank.depositAllExcept("Law rune");
            sleepUntil(() -> Rs2Inventory.fullSlotCount() == 1,5000);
        }
    }
    public void waitForAnimationStopnDoor() {
        long lastAnimationStopTime = System.currentTimeMillis();
        while (true) {
            sleep(100);
            if (Rs2Player.isAnimating()) {
                lastAnimationStopTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - lastAnimationStopTime >= 2000) {
                break;
            }
            if (Rs2GameObject.interact(11775)) {
                sleep(800);
                break;
            }
        }
    }
}
