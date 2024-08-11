package net.runelite.client.plugins.microbot.geHandler;

import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grandexchange.GrandExchangeSlots;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.http.api.item.ItemPrice;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;


public class geHandlerScript extends Script {
    public static double version = 1.0;
    @Inject
    private static ItemManager itemManager;

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
    public static boolean goBuyAndReturn(int[] amounts, int highBuyPercent, String... itemNames) {
        return goBuyAndReturn(amounts,highBuyPercent,false,itemNames);
    }
    public static boolean goBuyAndReturn(int[] amounts, int highBuyPercent,boolean collectInBank, String... itemNames) {
        boolean yesbought = false;
        WorldPoint savedLocation = Rs2Player.getWorldLocation();
        WorldPoint geLocation = new WorldPoint(3164, 3485, 0); // Coordinates for GE
        while (Rs2Player.getWorldLocation().distanceTo(geLocation) > 5) {
            Rs2Walker.walkTo(geLocation);
            Rs2Player.waitForWalking();
        }
        Rs2Bank.openBank();
        long coinsInBank = (long) Rs2Bank.count("Coins", true);
        Rs2Bank.closeBank();
        long coins = Rs2Inventory.ItemQuantity(995) + coinsInBank;
        Rs2GrandExchange.openExchange();
        for (int i = 0; i < itemNames.length; i++) {
            if (amounts[i] == 0) {
                continue; // Skip this item if the purchase amount is 0
            }
            int pricePerItem = priceChecker(itemNames[i])[0];
            if (collectInBank)
                Rs2GrandExchange.collectToBank();
            else
                Rs2GrandExchange.collectToInventory();
            while (!Rs2GrandExchange.hasBoughtOffer()) { // break if buylimit exceed
                pricePerItem = (int) Math.ceil(pricePerItem + (pricePerItem * highBuyPercent / 100.0));
                Microbot.log("isAllSlotsEmpty " + Rs2GrandExchange.isAllSlotsEmpty());
                if (Rs2GrandExchange.isAllSlotsEmpty())
                    abortAllActiveOffers();

                if (coins >= (long) pricePerItem * amounts[i]) {
                    Rs2GrandExchange.buyItem(itemNames[i], pricePerItem, amounts[i]);
                    sleepUntilTrue(() -> Rs2GrandExchange.hasBoughtOffer(), 500, 12000);
                    Widget[] collectButton = Rs2Widget.getWidget(465,6).getDynamicChildren();
                    if (!collectButton[1].isSelfHidden()) {
                        if (collectInBank)
                            Rs2GrandExchange.collectToBank();
                        else
                            Rs2GrandExchange.collectToInventory();
                        yesbought = true;
                        break;
                    }
                } else {
                    Microbot.log("Insufficient coins to buy " + amounts[i] + " of " + itemNames[i]);
                    //break;
                    Rs2GrandExchange.closeExchange();
                    return yesbought;
                }
            }
        }
        Rs2GrandExchange.closeExchange();
        while (Rs2Player.getWorldLocation().distanceTo(savedLocation) > 5) {
            Rs2Walker.walkTo(savedLocation, 1);
            Rs2Player.waitForWalking();
        }
        return yesbought;
    }

    public static void goSell(boolean returnAfterSell, int decreasePricePercent, int[] amounts, String... itemNames) {
        WorldPoint savedLocation = Rs2Player.getWorldLocation();
        WorldPoint geLocation = new WorldPoint(3164, 3485, 0); // Coordinates for GE
        while (Rs2Player.getWorldLocation().distanceTo(geLocation) > 5) {
            Rs2Walker.walkTo(geLocation, 3);
            Rs2Player.waitForWalking();
        }
        Rs2Bank.openBank();
        sleep(280, 350);
        Rs2Bank.setWithdrawAsNote();
        sleep(280, 350);

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
            int pricePerItem = priceChecker(itemNames[i])[0];

            Rs2GrandExchange.sellItem(Rs2Inventory.get(itemNames[i]).getName(), Rs2Inventory.count(itemNames[i]), 1);
            //Rs2GrandExchange.sellItem(Rs2Inventory.get(itemNames[finalI]).getName(),Rs2Inventory.count(itemNames[finalI]),pricePerItem);
            sleepUntilTrue(Rs2GrandExchange::hasSoldOffer, 1000, 250000);
            Rs2GrandExchange.collectToBank();
            sleep(280, 350);
        }
        Rs2GrandExchange.closeExchange();
        if (returnAfterSell) {
            while (Rs2Player.getWorldLocation().distanceTo(savedLocation) > 5) {
                Rs2Walker.walkTo(savedLocation, 1);
                Rs2Player.waitForWalking();
            }
        }
    }

    public static int[] priceChecker(String itemName) {
        int itemId = (int) Microbot.getClientThread().runOnClientThread(() -> {
            List<ItemPrice> items = Microbot.getItemManager().search(itemName);
            return items.stream()
                    .filter(item -> itemName.equals(item.getName()))
                    .findFirst()
                    .map(ItemPrice::getId)
                    .orElseGet(() -> {
                        System.err.println("Item not found: " + itemName);
                        return -1;
                    });
        });

        if (itemId == -1) {
            return new int[]{-1, -1}; // Item not found
        }

        int pricePerItem = (int) Microbot.getClientThread().runOnClientThread(() ->
                Microbot.getItemManager().getItemPrice(itemId));

        return new int[]{pricePerItem, itemId};
    }

    public static boolean abortAllActiveOffers() {
        Microbot.status = "Aborting all active offers";
        if (!Rs2GrandExchange.isOpen()) {
            Rs2GrandExchange.openExchange();
        }

        boolean abortedAny = false;
        for (GrandExchangeSlots slot : GrandExchangeSlots.values()) {
            Widget offerSlot = Rs2GrandExchange.getSlot(slot);
            if (offerSlot != null && Rs2GrandExchange.isOfferScreenOpen()) {
                Widget[] Abort = offerSlot.getDynamicChildren();
                Rs2Widget.clickWidgetFast(offerSlot, 2,2);
                sleepUntil(() -> Abort[22].getTextColor() == 9371648, 5000); // Assuming child(2) indicates offer state
                abortedAny = true;
            }
        }
        return abortedAny;
    }
    public static boolean buyItemsWithRatio(long coins, double[] ratio, int limit,boolean collectInBank, String[] itemNames) {
        if (itemNames.length >1) {
            Map<String, Integer> itemPrices = new HashMap<>();
            Map<String, Integer> itemsBought = new HashMap<>();
            Random random = new Random();

            for (String itemName : itemNames) {
                int pricePerItem = (int) priceChecker(itemName)[0];
                itemPrices.put(itemName, pricePerItem);
                itemsBought.put(itemName, 0);
            }

            double totalRatio = 0;
            for (double r : ratio) totalRatio += r;

            long totalCost = 0;
            Map<String, Integer> initialItemsToBuy = new HashMap<>();
            boolean limitHit = false;

            // Ensure at least one item hits the limit
            for (int i = 0; i < itemNames.length; i++) {
                initialItemsToBuy.clear();
                totalCost = 0;
                for (int j = 0; j < itemNames.length; j++) {
                    String itemName = itemNames[j];
                    int pricePerItem = itemPrices.get(itemName);
                    double itemRatio = ratio[j];
                    int quantity;
                    if (i == j) {
                        quantity = limit;
                        limitHit = true;
                    } else {
                        quantity = (int) Math.floor(limit * (itemRatio / ratio[i]));
                    }
                    initialItemsToBuy.put(itemName, quantity);
                    totalCost += quantity * pricePerItem;
                }
                if (totalCost <= coins) break;
            }

            // Ensure at least one item hits the limit
            initialItemsToBuy.clear();
            initialItemsToBuy.put(itemNames[1], limit);  // Set item2 to limit
            totalCost = limit * itemPrices.get(itemNames[1]);

            // Calculate quantities for other items based on ratio
            for (int i = 0; i < itemNames.length; i++) {
                if (i == 1) continue;  // Skip item2 since it already hits the limit
                String itemName = itemNames[i];
                int pricePerItem = itemPrices.get(itemName);
                int quantity = (int) Math.floor(limit * (ratio[i] / ratio[1]));
                initialItemsToBuy.put(itemName, quantity);
                totalCost += quantity * pricePerItem;
            }

            // Adjust the quantities proportionally to fit within the remaining coins
            if (totalCost > coins) {
                double scaleFactor = (double) coins / totalCost;
                for (String itemName : itemNames) {
                    int adjustedQuantity = (int) Math.floor(initialItemsToBuy.get(itemName) * scaleFactor);
                    initialItemsToBuy.put(itemName, adjustedQuantity);
                }
            }

            for (String itemName : itemNames) {
                int itemsToBuyItem = initialItemsToBuy.get(itemName);
                itemsBought.put(itemName, itemsToBuyItem);
                coins -= itemsToBuyItem * itemPrices.get(itemName);
            }
            for (int i = 0; i < itemNames.length; i++) {
                if ((ratio[i] % 2 == 0) && (itemsBought.get(itemNames[i]) % 2 != 0)) {
                    itemsBought.put(itemNames[i], itemsBought.get(itemNames[i]) - 1);
                }
            }
            // Print final results
            for (String itemName : itemNames) {
                System.out.println(itemName + ": " + itemsBought.get(itemName) + " items bought at " + itemPrices.get(itemName) + " coins each.");
            }
            System.out.println("Coins left: " + coins);

            // Call goBuyAndReturn function
            int[] amounts = new int[itemNames.length];
            for (int i = 0; i < itemNames.length; i++) {
                amounts[i] = itemsBought.get(itemNames[i]);
            }
            return goBuyAndReturn(amounts, 5, collectInBank, itemNames);
        }
        else{
           String itemName = itemNames[0];
           int pricePerItem = (int) priceChecker(itemName)[0];
           int amount = (int) Math.min(Math.floor((double) pricePerItem /coins),limit);
           return goBuyAndReturn(new int[]{amount}, 5, collectInBank, itemNames);
        }

    }
}