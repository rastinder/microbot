package net.runelite.client.plugins.microbot.geHandler;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;


public class geHandlerScript extends Script {
    public static double version = 1.0;

    public boolean run(geHandlerConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                //CODE HERE

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
    public static void goBuyAndReturn(int[] amounts,int highBuyPercent, String... itemNames) {
        WorldPoint savedLocation = Rs2Player.getWorldLocation();
        WorldPoint geLocation = new WorldPoint(3164, 3485, 0); // Coordinates for GE
        while(Rs2Player.getWorldLocation().distanceTo(geLocation) > 5) {
            Rs2Walker.walkTo(geLocation);
            Rs2Player.waitForWalking();
        }
        Rs2Bank.openBank();
        long coinsInBank = (long) Rs2Bank.count("Coins",true);
        Rs2Bank.closeBank();
        long coins = Rs2Inventory.ItemQuantity(995) + coinsInBank;
        Rs2GrandExchange.openExchange();
        for (int i = 0; i < itemNames.length; i++) {
            if (amounts[i] == 0) {
                continue; // Skip this item if the purchase amount is 0
            }
            int finalI = i;
            int itemId = (int) Microbot.getClientThread().runOnClientThread(() ->
                    Microbot.getItemManager().search(itemNames[finalI]).get(0).getId());
            int pricePerItem = (int) Microbot.getClientThread().runOnClientThread(() ->
                    Microbot.getItemManager().getItemPrice(itemId));

            int increasedPrice = (int) Math.ceil(pricePerItem + (pricePerItem * highBuyPercent / 100.0));

            if (coins >= (long) pricePerItem * amounts[i]) {
                Rs2GrandExchange.buyItem(itemNames[i], increasedPrice, amounts[i]);
                sleepUntilTrue(() -> Rs2GrandExchange.hasBoughtOffer(), 100,12000);
                Rs2GrandExchange.collectToInventory();
            } else {
                Microbot.log("Insufficient coins to buy " + amounts[i] + " of " + itemNames[i]);
            }
        }
        Rs2GrandExchange.closeExchange();
        while(Rs2Player.getWorldLocation().distanceTo(savedLocation) > 5) {
            Rs2Walker.walkTo(savedLocation,1);
            Rs2Player.waitForWalking();
        }
    }
    public static void goSell(boolean returnAfterSell,int decreasePricePercent,int[] amounts, String... itemNames){
        WorldPoint savedLocation = Rs2Player.getWorldLocation();
        WorldPoint geLocation = new WorldPoint(3164, 3485, 0); // Coordinates for GE
        while(Rs2Player.getWorldLocation().distanceTo(geLocation) > 5) {
            Rs2Walker.walkTo(geLocation);
            Rs2Player.waitForWalking();
        }
        Rs2Bank.openBank();
        sleep(280,350);
        Rs2Bank.setWithdrawAsNote();
        sleep(280,350);

        for (int i = 0; i < itemNames.length; i++) {
            int finalI = i;
            if (amounts[i] <= 0) {
                Rs2Bank.withdrawAll(itemNames[finalI]);
            } else
                Rs2Bank.withdrawX(itemNames[finalI], amounts[i]);
        }
        Rs2Bank.closeBank();
        Rs2GrandExchange.openExchange();
        Rs2GrandExchange.collectToBank();

        for (int i = 0; i < itemNames.length; i++) {
            int finalI = i;
            int itemId = (int) Microbot.getClientThread().runOnClientThread(() ->
                    Microbot.getItemManager().search(itemNames[finalI]).get(0).getId());
            int pricePerItem = (int) Microbot.getClientThread().runOnClientThread(() ->
                    Microbot.getItemManager().getItemPrice(itemId));

            Rs2GrandExchange.sellItem(Rs2Inventory.get(itemNames[finalI]).getName(),Rs2Inventory.count(itemNames[finalI]),1);
            //Rs2GrandExchange.sellItem(Rs2Inventory.get(itemNames[finalI]).getName(),Rs2Inventory.count(itemNames[finalI]),pricePerItem);
            sleepUntilTrue(Rs2GrandExchange::hasSoldOffer,1000,250000);
            Rs2GrandExchange.collectToBank();
            sleep(280,350);
        }
        Rs2GrandExchange.closeExchange();
        if (returnAfterSell){
            while(Rs2Player.getWorldLocation().distanceTo(savedLocation) > 5) {
                Rs2Walker.walkTo(savedLocation,1);
                Rs2Player.waitForWalking();
            }
        }
    }
}
