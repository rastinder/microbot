package net.runelite.client.plugins.microbot.rascleanleaf;

import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript.randomSleep;


public class rascleanleafScript extends Script {
    public static double version = 1.0;
    String Grimy = "Grimy";
    String clean = "";
    int[] sequence1 = {0, 1, 4, 5, 8, 9, 12, 13, 16, 17, 20, 21, 24, 25, 26, 27, 22,23, 19, 18, 15, 14, 11, 10, 7, 6, 3, 2};
    int[] sequence2 = {0, 1, 4, 5, 8, 9, 12, 13, 16, 17, 20, 21, 24, 25, 26, 27, 22,23, 18, 19, 15, 14, 11, 10, 7, 6, 2, 3};
    Random random = new Random();

    public boolean run(rascleanleafConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {
                randomSleep();
                if (!Rs2Bank.isOpen() && !Rs2Inventory.hasItem(Grimy)) {
                    Rs2Bank.openBank();
                    sleepUntil(() -> Rs2Bank.isOpen());
                    sleep(350,450);
                    if (!Rs2Inventory.isEmpty()) {
                        Rs2Bank.depositAll();
                        sleepUntil(() -> Rs2Inventory.isEmpty());
                    }
                    if (Rs2Bank.hasItem(Grimy)) {
                        Rs2Bank.withdrawAll(Grimy);
                    }
                    else {
                        shutdown();
                    }
                    sleep(150,250);
                    Rs2Bank.closeBank();
                    sleepUntil(() -> !Rs2Bank.isOpen());
                    sleep(650,850);
                }
                if (Rs2Inventory.hasItem(Grimy)) {
                    int[] selectedSequence = random.nextBoolean() ? sequence1 : sequence2;
                    List<Rs2Item> items = Rs2Inventory.all();
                    Widget inventoryWidget = Rs2Widget.getWidget(9764864);
                    Widget[] inventory = inventoryWidget.getDynamicChildren();
                    for (int i = 0; i < 28; i++) {
                        try {
                            Rs2Item item = items.get(selectedSequence[i]);
                            if (item.name.contains(Grimy)) {
                                //System.out.println(item.name);
                                    Microbot.getMouse().click(inventory[selectedSequence[i]].getBounds());
                                    sleep(60, 120);
                                    if (i == 15)
                                        sleep(25, 50);
                                if ((i + 1) % 2 == 0)
                                    sleep(50, 80);
                            }
                        } catch (Exception ex) {
                            continue;
                        }
                        if (!Rs2Inventory.hasItem(Grimy))
                            break;
                    }
                    sleepUntil(() -> !Rs2Inventory.hasItem(Grimy), 2000);
                    /*
                    if (Rs2Inventory.hasItem(Grimy)) {
                        items = Rs2Inventory.all();
                        for (int i = 0; i < items.size(); i++) {
                            Rs2Item item = items.get(i);
                            System.out.println(item.name);
                            if (item.name.contains(Grimy)) {
                                Rs2Inventory.interact(item.id, "clean");
                                if ((i + 1) % 4 == 0) {
                                    sleep(450, 850);
                                } else {
                                    sleep(250, 330);
                                }
                            }
                        }
                    }

                     */
                    //sleep(350, 450);
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
