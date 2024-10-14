package net.runelite.client.plugins.microbot.rasCombine;

import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.dashboard.DashboardPlugin;
import net.runelite.client.plugins.microbot.dashboard.DashboardWebSocket;
import net.runelite.client.plugins.microbot.geHandler.geHandlerScript;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript.randomSleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;

public class rasCombineScript extends Script {
    public static double version = 1.0;
    public static long stopTimer = 1;
    boolean hasItem = false;

    public boolean run(rasCombineConfig config) {
        Microbot.enableAutoRunOn = false;

        AtomicReference<String> item12 = new AtomicReference<>("");

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            rasMasterScriptScript.autoShutdown("ras Combine");
            if (!Microbot.isLoggedIn()) return;
            if (config.item1().trim().isEmpty()) // this is temporary
                shutdown();
            if (stopTimer == 1)
                stopTimer = rasMasterScriptScript.autoStopTimer();
            try {
                randomSleep();
                if (!Rs2Inventory.hasItem(config.item1()) || !Rs2Inventory.hasItem(config.item2())) {
                    if (!Rs2Bank.isNearBank(5) && !config.itemFromGE()){
                        Rs2Bank.walkToBank();
                        sleepUntilTrue(()-> Rs2Bank.isNearBank(5),200,278000);
                    } else if (!Rs2Bank.isNearBank(5) && !config.itemFromGE()) {
                        Rs2GrandExchange.walkToGrandExchange();
                        sleepUntilTrue(()-> !Rs2GrandExchange.walkToGrandExchange(),600,278000);
                    }
                    if (!Rs2Bank.isOpen()) {
                        Rs2Bank.openBank();
                        sleepUntil(() -> Rs2Bank.isOpen());
                        sleep(350, 450);
                    }
                    if (Rs2Inventory.hasItem(config.item1()) || Rs2Inventory.hasItem(config.item2())) {
                        Rs2Bank.depositAllExcept(config.item1(), config.item2());
                        sleep(350, 450);
                    }
                    else if (!Rs2Inventory.isEmpty()){
                        Rs2Bank.depositAll();
                        sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)) , 100, 5000);
                    }
                    if (Rs2Bank.hasItem(config.item1()) && Rs2Bank.hasItem(config.item2())) {
                        sleep(350, 450);
                        Rs2Bank.withdrawX(true, config.item1(), config.item1Count());
                        sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)) , 100, 5000);
                        sleep(350, 450);
                        Rs2Bank.withdrawX(true, config.item2(), config.item2Count());
                        sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)) , 100, 5000);
                        sleep(350, 450);
                    }
                    else{
                        //if (Rs2Inventory.ItemQuantity(config.item1()) == config.item1Count()) {
                            if (Rs2Bank.hasItem(config.item2())) {
                                Rs2Bank.withdrawX(true, config.item2(), config.item2Count());
                                sleepUntilTrue(() -> Rs2Inventory.hasItem(config.item2()), 100, 1000); // may not work if already present in less quantity
                            }
                        //}
                        //else if (Rs2Inventory.ItemQuantity(config.item2()) == config.item2Count()) { // remove else if problem testingOverlay
                            if (Rs2Bank.hasItem(config.item1())) {
                                Rs2Bank.withdrawX(true, config.item1(), config.item1Count());
                                sleepUntilTrue(() -> Rs2Inventory.hasItem(config.item1()), 100, 1000); // may not work if already present in less quantity
                            }
                       // }
                    }
                    sleep(350, 450);
                    hasItem = !config.item3().isEmpty() && Rs2Bank.hasItem(config.item3());
                    if (((!Rs2Inventory.hasItem(config.item1()) || !Rs2Inventory.hasItem(config.item2())) && !hasItem) && !config.itemFromGE()) {
                        shutdown();return;
                    } else if (((!Rs2Inventory.hasItem(config.item1()) || !Rs2Inventory.hasItem(config.item2())) && !hasItem) && config.itemFromGE()) {
                        if (!String.valueOf(item12).isEmpty()) {
                            geHandlerScript.goSell(false, 5, new int[]{0}, String.valueOf(item12));
                            Rs2Bank.openBank();
                            sleepUntil(() -> Rs2Bank.isOpen());
                        }
                        if (stopTimer < System.currentTimeMillis()) {
                            sellLeftOvers();
                            shutdown();return;
                        }
                        long coinsInBank = (long) Rs2Bank.count("Coins", true);
                        sleep(350, 450);
                        long item1Count = Rs2Bank.count(config.item1()) + Rs2Inventory.ItemQuantity(config.item1());
                        long item2Count = Rs2Bank.count(config.item2()) + Rs2Inventory.ItemQuantity(config.item2());
                        Rs2Bank.closeBank();
                        sleepUntil(() -> !Rs2Bank.isOpen());
                        long coins = Rs2Inventory.ItemQuantity(995) + coinsInBank;
                        sleep(850, 1250);
                        if ((item1Count == 0 ^ item2Count == 0) && config.item1Count() != 1) {
                            int count = (int) (item1Count == 0 ? item2Count : item1Count);
                            String itemName = item1Count == 0 ? config.item1() : config.item2();
                            if (!geHandlerScript.goBuyAndReturn(new int[] {count},true,false,10,true,itemName))
                                shutdown();return;
                        } else {
                            if (stopTimer < System.currentTimeMillis()) {
                                sellLeftOvers();
                                shutdown();return;
                            }else
                            if (config.item1Count() != 1) { //== 14
                                if (!geHandlerScript.buyItemsWithRatio(coins, new double[]{1, 1}, config.itemMaxLimit(), true, config.item1(), config.item2())) { // break if buylimit exceed
                                    // todo , sell spare stuff
                                    shutdown();return;
                                }
                            }else
                            if (config.item1Count() == 1) {
                                if (!geHandlerScript.buyItemsWithRatio(coins, new double[]{1}, config.itemMaxLimit(), true, new String[]{config.item2()})) { // break if buylimit exceed
                                    // todo , sell spare stuff
                                    shutdown();return;
                                }
                            }
                        }
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

    private void sellLeftOvers() {
        List<Integer> amounts = new ArrayList<>();
        List<String> itemNamesToPass = new ArrayList<>();
        Map<String, Integer> itemAmountMap = new HashMap<>() {{
            put("Pot", 0);
            put("Jug", 0);
            put("Bucket", 0);
            put("Bowl", 0);
        }};

        itemAmountMap.forEach((item, amount) -> {
            if (Rs2Bank.hasItem(item, true)) {
                itemNamesToPass.add(item);
                amounts.add(amount);
            }
        });
        geHandlerScript.goSell(false, 5, amounts.stream().mapToInt(i -> i).toArray(), itemNamesToPass.toArray(new String[0]));
    }

    @Override
    public void shutdown() {
        stopTimer = 1;
        rasMasterScriptScript.stopPlugin("ras Combine");
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
            if (Rs2Tab.getCurrentTab() != InterfaceTab.INVENTORY) {
                System.out.println("Inventory not showin.");
                Rs2Tab.switchToInventoryTab();
                sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.INVENTORY);
            }
            if (spaceBar) {
                if (random(0,2)==0) {
                    Rs2Inventory.combineClosest(item1, item2);
                }
                else if (random(0,2)==0){
                    List<Rs2Item> items = Rs2Inventory.items();
                    int lastItem1Slot = -1, firstItem2Slot = -1;
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).getName().toLowerCase().contains(item1.toLowerCase())) lastItem1Slot = i;
                        if (firstItem2Slot == -1 && items.get(i).getName().toLowerCase().contains(item2.toLowerCase()))
                            firstItem2Slot = i;
                    }
                    Microbot.getMouse().click(inventory[lastItem1Slot].getBounds());
                    Microbot.getMouse().click(inventory[firstItem2Slot].getBounds());
                }
                else
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
                        if(inventory[slot].getBounds().x > 0) {
                            Microbot.getMouse().click(inventory[slot].getBounds());
                            sleep(Math.min(600, Math.max(180, (int) ((i) * 4.285714))), Math.min(600, Math.max(250, (int) ((i) * 21.428571))));

                            if (inventory[i].getBounds().x > 0) {
                                Microbot.getMouse().click(inventory[i].getBounds());
                                sleep(Math.min(600, Math.max(180, (int) ((i) * 4.285714))), Math.min(600, Math.max(250, (int) ((i) * 21.428571))));
                            }
                        }
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
            for (int i =0 ; i < Rs2Inventory.count();i++){
                System.out.println("Checking inventory slot " + i + ": " + Rs2Inventory.getNameForSlot(i));
                if (!Objects.equals(Rs2Inventory.getNameForSlot(i).toLowerCase(), item2.toLowerCase())
                        && !Objects.equals(Rs2Inventory.getNameForSlot(i).toLowerCase(), item1.toLowerCase())
                        && !Rs2Inventory.getNameForSlot(i).toLowerCase().contains("jug")
                        && !Rs2Inventory.getNameForSlot(i).toLowerCase().contains("bucket")
                        && !Rs2Inventory.getNameForSlot(i).contains("Pot")
                        && !Rs2Inventory.getNameForSlot(i).toLowerCase().contains("bowl"))
                    return Rs2Inventory.getNameForSlot(i);
            }
            System.out.println(Rs2Inventory.getNameForSlot(0) + "is not final product.");
            return null;
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
            if (sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)),100,500 )) {
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
