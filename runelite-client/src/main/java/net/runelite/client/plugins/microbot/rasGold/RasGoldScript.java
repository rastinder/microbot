package net.runelite.client.plugins.microbot.rasGold;

import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
//import net.runelite.client.plugins.microbot.ras_jwel.ras.RasjwelConfig;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;


public class RasGoldScript extends Script {
    WorldPoint furnaceLocation = new WorldPoint(3109, 3499, 0);
    WorldPoint bankLocation = new WorldPoint(3094, 3494, 0);
    public int id = 0;


    public boolean run(RasGoldConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {
                boolean isBankVisible = Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(bankLocation) <= 7;
                boolean hasRunEnergy = Microbot.getClient().getEnergy() > 4000;
                boolean hasBars = Rs2Inventory.hasItem(config.getBarName());
                boolean hasStone = !(config.stoneNeeded() && !Rs2Inventory.hasItem(config.stoneProductName()));
                boolean hasMould = !(config.mouldNeeded() && !Rs2Inventory.hasItem(config.mouldProductName()));


                if (hasRunEnergy) Rs2Player.toggleRunEnergy(true);
                if (Microbot.pauseAllScripts) return;

                if (hasBars && hasStone && hasMould && Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(furnaceLocation) > 5) {
                    Rs2Walker.walkTo(furnaceLocation);
                }

                if (hasBars && hasStone && hasMould && Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(furnaceLocation) <= 5) {
                    if (!Rs2Widget.hasWidget("What would")) {
                        Rs2GameObject.interact("Furnace", "Smelt");
                        sleepUntil(() -> Rs2Widget.hasWidget("What would"), 5000);
                    }
                    if (Rs2Widget.hasWidget("What would")) {
                        sleep(200, 400);
                        clcickWidget(config);
                        sleep(400, 800);
                        Rs2Inventory.open();
                        sleepUntil(() -> !stopedWorking(config), 60000);
                        sleep(400, 800);
                    }
                }

                if (!hasBars && !isBankVisible) {
                    Rs2Walker.walkTo(bankLocation, 2);
                }

                if (!hasBars && isBankVisible) {
                    if (Rs2Bank.isOpen()) {
                        Rs2Bank.depositAll(config.finishedProductName());
                        sleepUntil(() -> Rs2Inventory.hasItem(config.finishedProductName()));
                        sleep(200, 400);
                        if (Rs2Bank.hasItem(config.getBarName())) {
                            Rs2Bank.withdrawX(config.getBarName(), config.getBarCount());
                            sleepUntil(() -> Rs2Inventory.hasItem(config.getBarName()));
                            sleep(200, 400);
                            if (config.stoneNeeded()) {
                                Rs2Bank.withdrawX(config.stoneProductName(), config.getStoneCount());
                                sleepUntil(() -> Rs2Inventory.hasItem(config.stoneProductName()));
                                sleep(200, 400);
                            }

                            if (config.mouldNeeded()) {
                                Rs2Bank.withdrawX(true, config.mouldProductName(), 1);
                                sleepUntilOnClientThread(() -> Rs2Inventory.hasItem(config.mouldProductName()));
                                sleep(200, 400);
                            }
                        } else {
                            Microbot.getNotifier().notify("Run out of Materials");
                            shutdown();
                        }

                        Rs2Bank.closeBank();
                    } else {
                        Rs2Bank.openBank();
                    }
                }

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    private void clcickWidget(RasGoldConfig config) {
        if (config.mouldProductName().contains("ing")) {
            if (id == 0) {
                for (int i = 16; i >= 8; i--) {
                    Rs2Widget.clickWidget(446, i);
                    sleepUntil(() -> !Rs2Widget.hasWidget("What would"), 800);
                    if (!Rs2Widget.hasWidget("What would")) {
                        id = i;
                        return;
                    }
                }
            }
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            //Rs2Widget.clickWidget(446, id);
            return;

        }
        Widget[] widgets = Rs2Widget.getWidget(446, 21).getStaticChildren();

        for (Widget widget : widgets) {
            Widget[] dynamicChildren = widget.getDynamicChildren();
            if (dynamicChildren.length > 0) {
                // Click on the last dynamic child widget
                Microbot.getMouse().click(dynamicChildren[dynamicChildren.length - 1].getBounds());
                return;
            }
        }
        widgets = Rs2Widget.getWidget(446, 35).getStaticChildren();

        for (Widget widget : widgets) {
            Widget[] dynamicChildren = widget.getDynamicChildren();
            if (dynamicChildren.length > 0) {
                // Click on the last dynamic child widget
                Microbot.getMouse().click(dynamicChildren[dynamicChildren.length - 1].getBounds());
                return;
            }
        }
        widgets = Rs2Widget.getWidget(446, 46).getStaticChildren();

        for (Widget widget : widgets) {
            Widget[] dynamicChildren = widget.getDynamicChildren();
            if (dynamicChildren.length > 0) {
                // Click on the last dynamic child widget
                Microbot.getMouse().click(dynamicChildren[dynamicChildren.length - 1].getBounds());
                return;
            }
        }
        widgets = Rs2Widget.getWidget(446, 51).getStaticChildren();

        for (Widget widget : widgets) {
            String[] actions =widget.getActions();
            if (actions != null) {
                for (String action : actions) {
                    if (action != null && action.contains("Make")) {
                        Microbot.getMouse().click(widget.getBounds());
                        return;
                    }
                }
            }
        }
        //Microbot.getMouse().click(Rs2Widget.getWidget(446, 50).getBounds());

    }

    private boolean stopedWorking(RasGoldConfig config) {
        if (Rs2Widget.hasWidget("Click here")) {
            return false;
        }
        sleep(100);
        return Rs2Inventory.hasItem(config.getBarName());
    }
}
