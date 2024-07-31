package net.runelite.client.plugins.microbot.rasCombine;

import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.dashboard.DashboardPlugin;
import net.runelite.client.plugins.microbot.dashboard.DashboardWebSocket;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class rasCombineScript extends Script {
    public static double version = 1.0;
    boolean hasItem = false;

    public boolean run(rasCombineConfig config) {
        Microbot.enableAutoRunOn = false;
        //String item1 = config.item1();
        //String item2 = config.item2();
        //String item3 = config.item3();

        AtomicReference<String> item12 = new AtomicReference<>("");

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {

            if (!super.run()) return;
            try {
                if (!Rs2Inventory.hasItem(config.item1()) || !Rs2Inventory.hasItem(config.item2())) {
                    if (!Rs2Bank.isOpen()) {
                        Rs2Bank.openBank();
                        sleepUntil(() -> Rs2Bank.isOpen());
                        sleep(350, 450);
                    }
                    if (Rs2Inventory.hasItem(config.item1()) || Rs2Inventory.hasItem(config.item2())) {
                        Rs2Bank.depositAllExcept(config.item1(), config.item2());
                    }
                    else if (!Rs2Inventory.isEmpty()){
                        Rs2Bank.depositAll();
                    }
                    if (Rs2Bank.hasItem(config.item1()) && Rs2Bank.hasItem(config.item2())) {
                        sleep(350, 450);
                        Rs2Bank.withdrawX(true, config.item1(), config.item1Count());
                        sleep(350, 450);
                        Rs2Bank.withdrawX(true, config.item2(), config.item2Count());
                        sleep(350, 450);
                    }
                    sleep(350, 450);
                    hasItem = !config.item3().isEmpty() && Rs2Bank.hasItem(config.item3());
                    if ((!Rs2Inventory.hasItem(config.item1()) || !Rs2Inventory.hasItem(config.item2())) && !hasItem) {
                        Plugin p = DashboardWebSocket.findPlugin("ras combine");
                        Microbot.getPluginManager().stopPlugin(p);
                        shutdown();
                    }
                    sleep(350, 450);
                    Rs2Bank.closeBank();
                    sleepUntil(() -> !Rs2Bank.isOpen());
                    sleep(850, 1250);
                }
                if (Rs2Inventory.hasItem(config.item1()) && Rs2Inventory.hasItem(config.item2())) {
                    int item1Count = Rs2Inventory.count(config.item1());
                    int item2Count = Rs2Inventory.count(config.item2());
                    item12.set(processInventory(config.item1(), config.item2(), item1Count, item2Count, config.spacepress()));

                }
                if (!config.item3().trim().isEmpty() && !item12.get().isEmpty() && hasItem){
                    Rs2Bank.openBank();
                    sleepUntil(() -> Rs2Bank.isOpen());
                    if (Rs2Bank.hasItem(config.item3())) {
                        Rs2Bank.withdrawAll(config.item3());
                        sleepUntil(() -> Rs2Inventory.hasItem(config.item3()));
                        if (Rs2Inventory.hasItem(config.item3()) && Rs2Inventory.hasItem(item12.get())) {
                            Rs2Bank.closeBank();
                            sleepUntil(() -> !Rs2Bank.isOpen());
                            sleep(850, 1250);
                            int item1Count = Rs2Inventory.count(item12.get());
                            int item2Count = Rs2Inventory.count(config.item3());
                            processInventory(item12.get(), config.item3(), item1Count, item2Count, config.spacepress());
                        }
                        else if (Rs2Bank.hasItem(item12.get()) && Rs2Bank.hasItem(config.item3())) {
                            Rs2Bank.depositAll();
                            Rs2Bank.withdrawX(item12.get(), config.item2Count());
                            Rs2Bank.withdrawX(config.item3(), config.item3Count());
                            Rs2Bank.closeBank();
                            sleepUntil(() -> !Rs2Bank.isOpen());
                            sleep(850, 1250);
                            processInventory(item12.get(), config.item3(), Rs2Inventory.count(item12.get()), Rs2Inventory.count(config.item3()), config.spacepress());

                        }
                    }
                    else hasItem = false;
                }
                else {
                    System.out.println("item 3 not configured");
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
    public String processInventory(String item1, String item2, int item1Count, int item2Count,Boolean spaceBar) {
        Widget inventoryWidget = Rs2Widget.getWidget(9764864);
        if (inventoryWidget != null) {
            Widget[] inventory = inventoryWidget.getDynamicChildren();

            if (inventory == null || inventory.length == 0) {
                System.out.println("Inventory is empty or not loaded properly.");
                return null;
            }
            if (inventory.length < 28) {
                System.out.println("Inventory does not have enough slots.");
                return null;
            }
            if (spaceBar) {
                Rs2Inventory.combine(item1, item2);
                sleepUntil(()-> Rs2Widget.hasWidget("Choose a"));
                sleep(400,600);
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                checkAnimation();
            } else if (item1Count == 1 || item2Count == 1) {
                int slot = 0;
                if (item1Count  == 1) {
                    slot = Rs2Inventory.slot(item1);
                } else {
                    slot = Rs2Inventory.slot(item2);
                }
                for (int i = 1; i <= 27; i++) {
                    if (i != slot) {
                        Microbot.getMouse().click(inventory[slot].getBounds());
                        sleep(Math.min(600, Math.max(180, (int) ((i) * 4.285714))), Math.min(600, Math.max(250, (int) ((i) * 21.428571))));

                        Microbot.getMouse().click(inventory[i].getBounds());
                        sleep(Math.min(600, Math.max(180, (int) ((i) * 4.285714))), Math.min(600, Math.max(250, (int) ((i) * 21.428571))));
                    }
                }
            } else {
                int greaterCount = Math.max(item1Count, item2Count);
                int lesserCount = Math.min(item1Count, item2Count);
                boolean item1IsGreater = item1Count >= item2Count;
                int totalCount = item1Count + item2Count;

                for (int i = 0; i < greaterCount; i++) {
                    int with = totalCount - 1 - i;

                    if (i < lesserCount) {
                        System.out.println("Slot " + i + " with " + with);
                        if (item1IsGreater) {
                            Microbot.getMouse().click(inventory[i].getBounds());
                            sleep(Math.min(600, Math.max(120, (int) ((28 - i) * 4.285714))), Math.min(500, Math.max(150, (int) (((28 - i) * 21.428571)))));
                            Microbot.getMouse().click(inventory[item1Count + i].getBounds());
                        } else {
                            Microbot.getMouse().click(inventory[item1Count + i].getBounds());
                            sleep(Math.min(600, Math.max(120, (int) ((28 - i) * 4.285714))), Math.min(500, Math.max(150, (int) (((28 - i) * 21.428571)))));
                            Microbot.getMouse().click(inventory[i].getBounds());
                        }
                        sleep(Math.min(600, Math.max(120, (int) ((28 - i) * 4.285714))), Math.min(500, Math.max(150, (int) (((28 - i) * 21.428571)))));
                    } else {
                        System.out.println("Slot " + i);
                        if (item1IsGreater) {
                            Microbot.getMouse().click(inventory[i].getBounds());
                        } else {
                            Microbot.getMouse().click(inventory[item1Count + i].getBounds());
                        }
                        sleep(Math.min(600, Math.max(120, (int) ((28 - i) * 4.285714))), Math.min(500, Math.max(150, (int) (((28 - i) * 21.428571)))));
                    }
                }
            }
            sleep(850, 1250);
            return Rs2Inventory.getNameForSlot(0);
        } else {
            System.out.println("Inventory widget not found.");
            return null;
        }
    }
    public void checkAnimation() {
        long startTime = 0;
        long nonAnimatingStartTime = 0;
        boolean wasAnimating = false;

        while (true) {
            //if (Rs2Player.isAnimating()) {
            if (sleepUntil(Rs2Inventory::waitForInventoryChanges)) {
                wasAnimating = true;
                startTime = System.currentTimeMillis();
                nonAnimatingStartTime = 0; // Reset non-animating timer
            } else {
                if (wasAnimating) {
                    long timePassed = System.currentTimeMillis() - startTime;
                    if (timePassed > 1000) {
                        return;
                    }
                }

                // Check if non-animating period exceeds 3000 ms
                if (nonAnimatingStartTime == 0) {
                    nonAnimatingStartTime = System.currentTimeMillis();
                } else {
                    long nonAnimatingTimePassed = System.currentTimeMillis() - nonAnimatingStartTime;
                    if (nonAnimatingTimePassed > 3000) {
                        return;
                    }
                }
            }

            try {
                Thread.sleep(50); // Small sleep to prevent busy-waiting
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
