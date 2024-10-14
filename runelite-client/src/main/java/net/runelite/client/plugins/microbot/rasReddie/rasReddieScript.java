package net.runelite.client.plugins.microbot.rasReddie;

import net.runelite.api.NPC;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.geHandler.geHandlerScript;
import net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript.randomSleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;


public class rasReddieScript extends Script {
    public static double version = 1.0;
    private static final WorldArea bankArea = new WorldArea(3092, 3241, 3, 6, 0);
    private static final WorldArea bankAreapoint = new WorldArea(3092, 3242, 2, 3, 0);
    private static final WorldArea shopArea = new WorldArea(3083, 3256, 6, 5, 0);
    //private static final WorldArea shopAreapoint = new WorldArea(3085, 3258, 1, 1, 0);
    private static final WorldPoint shopAreapoint = new WorldPoint(3085, 3259, 0);
    private static final WorldPoint shopGate = new WorldPoint(3088, 3258, 0);
    private static final WorldArea grandexchange = new WorldArea(3167, 3486, 2, 5, 0);
    public int coins = 995;
    public static long stopTimer = 1;

    public boolean run(rasReddieConfig config) {
        Microbot.enableAutoRunOn = true;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                rasMasterScriptScript.autoShutdown("Ras Red die");
                if (!super.run()) return;
                if (!Microbot.isLoggedIn()) return;
                if (stopTimer == 1)
                    stopTimer = rasMasterScriptScript.autoStopTimer();
                if (config.MakeDyefun()) {
                    randomSleep();
                    if (Rs2Inventory.count("Redberries") > 2 &&  Rs2Inventory.ItemQuantity(coins) > 44) {
                        // we have Redberries and money
                        boolean sleepfirst = true;
                        while (Rs2Inventory.count("Redberries") > 2) {
                            System.out.println("inventry has items");
                            intractagatha(sleepfirst);
                            sleepfirst = false;
                        }
                    } else {
                        if (!handleBank()) {
                            // no berries or money
                            if (!buy_from_grand_exchange()) {
                                //sleep(15000);
                                shutdown();
                            }
                            if (stopTimer < System.currentTimeMillis()) {
                                geHandlerScript.goBuy(new int[]{0}, "Red dye");
                                shutdown();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println("crash");
                //System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        stopTimer = 1;
        rasMasterScriptScript.stopPlugin("Ras Red die");
        super.shutdown();
    }

    public void intractagatha(boolean sleepfirst){
        WorldPoint currentPlayerWorldLocation = Microbot.getClient().getLocalPlayer().getWorldLocation();
        if (shopArea.contains(currentPlayerWorldLocation) ) {
            redDye(sleepfirst);
        }
        else {
            System.out.println("Player not in shop area " + currentPlayerWorldLocation +
                    "not in " + shopArea.toWorldPointList());
            Rs2Walker.walkTo(shopAreapoint,0);
            //Rs2Player.waitForAnimation(30000);
            sleep(100,200);
            /*
            try {
                System.out.println("door not closed");
                if (!isInHouseArea() && Rs2Player.isAnimating() ) {
                    Rs2GameObject.interact(shopGate);
                }
            } catch (Exception e) {
                System.out.println("exception when looking for a door");
            }

             */

        }

    }
    public boolean handleBank(){
        if (bankArea.contains(Microbot.getClient().getLocalPlayer().getWorldLocation())) {
            if (Rs2Bank.isOpen()) {
                Rs2Bank.depositAllExcept("Redberries","Coins");
                    if (Rs2Inventory.ItemQuantity(coins) < 135) {
                        int randomAmount = (Math.random() < 0.5) ? 1000 : 10000;
                        Rs2Bank.withdrawX("Coins", randomAmount);
                    }
                    if (Rs2Bank.hasBankItem("Redberries", 3)) {
                        if (!Rs2Inventory.isFull())
                        Rs2Bank.withdrawAll("Redberries");
                    } else {
                        return false;
                    }

            } else {
               if ( Rs2Bank.openBank(Rs2Npc.getNpc(1613))) {
                   Rs2Walker.setTarget(null);
                   sleep(350, 1800);
               }
            }
        } else {
            System.out.println("not present in bank");
            WorldPoint randomBankPoint = bankAreapoint.toWorldPointList().get(Random.random(0, bankAreapoint.toWorldPointList().size() - 1));
            Rs2Walker.walkTo(randomBankPoint,0);
        }
        return true;
    }
    public boolean buy_from_grand_exchange(){
        while(true) {
            if (grandexchange.contains(Microbot.getClient().getLocalPlayer().getWorldLocation())) {
                sleepUntil(Rs2Bank::openBank);
                if (Rs2Bank.hasItem("Red dye")) { // check if red dye is being sold , if present in bank and not selling then sell
                    //Rs2Bank.setWithdrawAsNote();
                    //Rs2Bank.withdrawAll("Red dye");
                    //sleepUntil(() -> Rs2Bank.closeBank());
                    //sleepUntil(() -> Rs2GrandExchange.openExchange());
                    //Rs2GrandExchange.sellItemUnder5Percent("Red dye");
                    geHandlerScript.goSell(false,5,new int[]{0},"Red dye");
                }
                if (areWeBuyingRedberries()) {
                    geHandlerScript.goBuyAndReturn(new int[]{200},true,false,5,true,"Redberries");
                    return true;
                    /*
                    Rs2GrandExchange.buyItemAbove5Percent("Redberries", 1000);
                    sleepUntil(() -> Rs2GrandExchange.hasBoughtOffer(), 140000);

                     */
                }
                /*
                if (Rs2GrandExchange.collectToBank()) {
                    Rs2GrandExchange.closeExchange();
                    return true;
                }
                else{
                    Rs2GrandExchange.closeExchange();
                    Rs2Bank.openBank();
                    if (Rs2Bank.count("Redberries") > 2)
                    {
                        return true;
                    }
                    else return false;

                }

                 */
            } else {
                WorldPoint randomBankPoint = grandexchange.toWorldPointList().get(Random.random(0, bankArea.toWorldPointList().size() - 1));
                Rs2Walker.walkTo(randomBankPoint,0);
                sleepUntil(() -> grandexchange.contains(Microbot.getClient().getLocalPlayer().getWorldLocation()), 180000);
            }
        }
    }
    private boolean isInHouseArea() {
        return Microbot.getClient().getLocalPlayer().getWorldLocation().isInArea(shopArea);
    }
    private boolean areWeBuyingRedberries(){
        return  true;
    }
    private void redDye(boolean sleepfirst){
        if (!Rs2Inventory.open()) {
            Rs2Inventory.getInventoryWidget();
        }
        String dyeName = "red";
        int wait = 1000;
        NPC npc = Rs2Npc.getNpc("Aggie");
        if (npc == null){
            System.out.println("no agatha found ");
            return;
        }
        if (!Rs2Widget.hasWidget("What can")) {
            if (Rs2Npc.interact(npc, "Talk-to"))
                Rs2Walker.setTarget(null);
            sleepUntil(() -> Rs2Widget.hasWidget("What can"),5000);
            if (!Rs2Widget.hasWidget("What can"))
                System.out.println("cant find dialog");
        }
        if (Rs2Widget.hasWidget("What can")){
            if (sleepfirst) sleep(700);
            sleep(200,400);
            System.out.println("found dialog");
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(300,500);
            sleepUntil(() -> Rs2Widget.clickWidget("dyes"),wait);
            sleep(60,100);
            sleepUntil(() -> Rs2Dialogue.clickContinue(),wait);
            sleep(60,100);
            sleepUntil(() -> Rs2Dialogue.clickContinue(),wait);
            sleep(300,500);
            sleepUntil(() -> Rs2Widget.clickWidget("to make "+dyeName+ " dye?"),wait);
            sleep(60,100);
            sleepUntil(() -> Rs2Dialogue.clickContinue(),wait);
            sleep(60,100);
            sleepUntil(() -> Rs2Dialogue.clickContinue(),wait);
            sleep(300,500);
            sleepUntil(() -> Rs2Widget.clickWidget("Okay"),wait);
            sleep(60,100);
            sleepUntil(() -> Rs2Dialogue.clickContinue(),wait);
            sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)),100,2000);
            sleep(300,500);
        }
    }
}
