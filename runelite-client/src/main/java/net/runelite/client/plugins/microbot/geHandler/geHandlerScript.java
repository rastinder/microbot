package net.runelite.client.plugins.microbot.geHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.grandexchange.GrandExchangePlugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.ras_highalc.ras.Ras_highalcScript;
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
import javax.inject.Singleton;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.abs;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;

import java.util.logging.Logger;

public class geHandlerScript extends Script {
    private static final Logger logger = Logger.getLogger(geHandlerScript.class.getName());
    public static double version = 1.0;
    private static ItemManager itemManager;
    private static ConfigManager configManager;
    public static int boughtQuantity = 0;
    private static Map<String, Long> itemBuyLimitMap = new HashMap<>();
    private static final long BUY_LIMIT_DURATION = 4 * 60 * 60 * 1000;

    private static final String API_URL = "https://prices.runescape.wiki/api/v1/osrs/latest";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static JsonNode priceData = null;



    private static final AtomicBoolean isInitialized = new AtomicBoolean(false);
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
                //System.out.println("gehandel " + totalTime);

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
    public static boolean goBuy(int[] amounts, String... itemNames) {
        return goBuyAndReturn(amounts,false,false,10,true,itemNames);
    }
    public static boolean goBuy(int[] amounts, int highBuyPercent, String... itemNames) {
        return goBuyAndReturn(amounts,false,false,highBuyPercent,true,itemNames);
    }
    public static boolean goBuy(int[] amounts, int highBuyPercent,boolean collectInBank, String... itemNames) {
        return goBuyAndReturn(amounts,false,false,highBuyPercent,collectInBank,itemNames);
    }
    public static boolean goBuyAndReturn(int[] amounts, int highBuyPercent, String... itemNames) {
        return goBuyAndReturn(amounts,highBuyPercent,false,itemNames);
    }
    public static boolean goBuyAndReturn(int[] amounts, int highBuyPercent,boolean collectInBank, String... itemNames) {
        return goBuyAndReturn(amounts,false,true,highBuyPercent,collectInBank,itemNames);
    }
    public static boolean goBuyAndReturn(int[] amounts,boolean compromiseOnAmount,boolean returnalso, int highBuyPercent,boolean collectInBank, String... itemNames) {
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
            int remainingBuyLimit = GELimits.getRemainingBuyLimit(itemNames[i]);
            if (amounts[i] == 0 || remainingBuyLimit == 0) {
                if (remainingBuyLimit == 0)
                    return false;
                continue; // Skip this item if the purchase amount is 0
            }
            amounts[i] = Math.min(amounts[i],remainingBuyLimit);
            int pricePerItem = priceChecker(itemNames[i])[0];
            if (collectInBank)
                Rs2GrandExchange.collectToBank();
            else
                Rs2GrandExchange.collectToInventory();
            int loopCounter = 0;
            while (!Rs2GrandExchange.hasBoughtOffer()) { // break if buy limit exceeded
                if (loopCounter > 0) {
                    pricePerItem = Math.max((int) Math.ceil(pricePerItem + (pricePerItem * highBuyPercent / 100.0)), pricePerItem + 10);
                }
                System.out.println("price: " + pricePerItem + "  amount: " + amounts[i] + "  item: " + itemNames[i] + " current attempt "+ loopCounter );
                Microbot.log("isAllSlotsEmpty " + Rs2GrandExchange.isAllSlotsEmpty());

                if (!Rs2GrandExchange.isAllSlotsEmpty()) { // always returns false
                    abortAllActiveOffers();
                    sleepUntilTrue(Rs2GrandExchange::collectToBank,100,800);
                    //Rs2GrandExchange.collectToBank();
                }
                if (loopCounter == 10)
                    return yesbought;

                // If insufficient coins and compromise is allowed
                if (coins < (long) pricePerItem * amounts[i]) {
                    if (compromiseOnAmount) {
                        // Try reducing the amount until it fits
                        while (amounts[i] > 1 && coins < (long) pricePerItem * amounts[i]) {
                            amounts[i]--;
                        }
                        if (amounts[i] <= 1) {
                            Microbot.log("Cannot afford even 1 of " + itemNames[i]);
                            Rs2GrandExchange.closeExchange();
                            return yesbought;
                        }
                    } else {
                        Microbot.log("Insufficient coins to buy " + amounts[i] + " of " + itemNames[i]);
                        Rs2GrandExchange.closeExchange();
                        return yesbought;
                    }
                }

                // Proceed with buying if coins are sufficient
                if (coins >= (long) pricePerItem * amounts[i]) {
                    boughtQuantity = 0;
                    Rs2GrandExchange.buyItem(itemNames[i], pricePerItem, amounts[i]);
                    sleepUntilTrue(() -> Rs2GrandExchange.hasBoughtOffer(), 500, 12000);
                    System.out.println("boughtQuantity= " + boughtQuantity);
                    amounts[i] = amounts[i] - boughtQuantity;
                    GELimits.setRemainingBuyLimit(itemNames[i],boughtQuantity);
                    GELimits.printUpdatedBuyLimits();

                    Widget[] collectButton = Rs2Widget.getWidget(465, 6).getDynamicChildren();
                    if (!collectButton[1].isSelfHidden() && Rs2GrandExchange.hasBoughtOffer()) {
                        if (collectInBank) {
                            Rs2GrandExchange.collectToBank();
                        } else {
                            Rs2GrandExchange.collectToInventory();
                        }
                        yesbought = true;
                        break;
                    }
                }
                loopCounter++;
            }
        }
        Rs2GrandExchange.closeExchange();
        if (!returnalso) {
            while (Rs2Player.getWorldLocation().distanceTo(savedLocation) > 5) {
                Rs2Walker.walkTo(savedLocation, 1);
                Rs2Player.waitForWalking();
            }
        }
        return yesbought;
    }
    public static void goSell(boolean returnAfterSell, int decreasePricePercent, int[] amounts, String... itemNames){
        goSell( returnAfterSell,  decreasePricePercent,false,  amounts, itemNames);
    }

    public static void goSell(boolean returnAfterSell, int decreasePricePercent,boolean exactname, int[] amounts, String... itemNames) {
        Rs2Walker.setTarget(null);
        WorldPoint savedLocation = Rs2Player.getWorldLocation();
        WorldPoint geLocation = new WorldPoint(3162, 3487, 0); // Coordinates for GE
           while (true) {
               while (Rs2Player.getWorldLocation().distanceTo(geLocation) > 5) {
                   Rs2Walker.walkTo(geLocation, 3);
                   Rs2Player.waitForWalking();
               }
               if (Rs2Player.getWorldLocation().distanceTo(geLocation) <= 5) {
                   Rs2Walker.walkCanvas(new WorldPoint(random(0, 5) + 3162, 3487, 0));
                   sleep(499, 800);
                   Rs2Bank.openBank();
                   sleepUntilTrue(Rs2Bank::isOpen, 100, 3000);
                   if (Rs2Bank.isOpen())
                       break;
               }
           }
        sleep(280, 350);
        if (!Rs2Inventory.isEmpty()) { // if this cause problem then remove
            Rs2Bank.depositAll();
            sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)) , 100, 5000);
            sleep(280, 350);
        }

        Rs2Bank.setWithdrawAsNote();
        sleep(280, 350);

        for (int i = 0; i < itemNames.length; i++) {
            int finalI = i;
            if (amounts[i] == 0) {
                Rs2Bank.withdrawAll(itemNames[finalI],exactname);
            }
            else if (amounts[i] < 0) {
                Rs2Bank.withdrawAll(itemNames[finalI],exactname);
                sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)) , 100, 5000);
                Rs2Bank.depositX(itemNames[finalI],abs(amounts[i]));
            } else
                Rs2Bank.withdrawX(false,itemNames[finalI], amounts[i],exactname);
            sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)) , 100, 5000);
        }
        Rs2Bank.closeBank();
        while (!Rs2GrandExchange.isOpen()) {
            Rs2GrandExchange.openExchange();
            sleep(500);
        }
        Rs2GrandExchange.collectToBank();

        for (int i = 0; i < itemNames.length; i++) {
            try {
                if (Rs2Inventory.hasItem(itemNames[i])) {
                    int pricePerItem = priceChecker(itemNames[i])[0];
                    System.out.println("Selling item = " + itemNames[i] + " amount = " + Rs2Inventory.ItemQuantity(itemNames[i]));
                    Rs2GrandExchange.sellItem(itemNames[i], (int) Rs2Inventory.ItemQuantity(itemNames[i]), 1);
                    //Rs2GrandExchange.sellItem(Rs2Inventory.get(itemNames[finalI]).getName(),Rs2Inventory.count(itemNames[finalI]),pricePerItem);
                    sleepUntilTrue(Rs2GrandExchange::hasSoldOffer, 1000, 250000);
                    Rs2GrandExchange.collectToBank();
                    sleep(280, 350);
                }
            }
            catch (Exception e)
            {
                System.out.println("cant find item in inv that needs to sell"+e);
            }
        }
        Rs2GrandExchange.closeExchange();
        if (returnAfterSell) {
            while (Rs2Player.getWorldLocation().distanceTo(savedLocation) > 5) {
                Rs2Walker.walkTo(savedLocation, 1);
                Rs2Player.waitForWalking();
            }
        }
    }

    public static int[] priceChecker(String itemName){
        int itemId = (int) Microbot.getClientThread().runOnClientThread(() -> {
            List<ItemPrice> items = Microbot.getItemManager().search(itemName);
            return items.stream()
                    .filter(item -> itemName.equalsIgnoreCase(item.getName()))
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
        int pricePerItem = 0;
        try {
             pricePerItem = (int)
                     getHighPriceForId(itemId);
        }
        catch (Exception e) {
             pricePerItem = (int) Microbot.getClientThread().runOnClientThread(() ->
                    Microbot.getItemManager().getItemPrice(itemId));
        }
        return new int[]{pricePerItem, itemId};
    }
    public static boolean abortAllActiveOffers() {
        Microbot.status = "Aborting all active offers";
        if (!Rs2GrandExchange.isOpen()) Rs2GrandExchange.openExchange();
        boolean abortedAny = false;
        for (GrandExchangeSlots slot : GrandExchangeSlots.values()) {
            Widget offerSlot = Rs2GrandExchange.getSlot(slot);
            if (offerSlot != null && Rs2GrandExchange.isOfferScreenOpen()) {
                Widget[] children = offerSlot.getDynamicChildren();
                if (children != null && !children[22].isSelfHidden()) {
                    Rs2Widget.clickWidgetFast(offerSlot, 2, 2);
                    sleepUntil(() -> children[22].getTextColor() == 9371648, 2000);
                    abortedAny = true;
                }
            }
        }
        return abortedAny;
    }
    public static boolean buyItemsWithRatio(long coins, double[] ratio, int limit, boolean collectInBank, String... itemNames) {
        if (itemNames.length > 1) {
            Map<String, Integer> itemPrices = new HashMap<>();
            Map<String, Integer> itemsBought = new HashMap<>();
            Map<String, Integer> itemBuyLimits = new HashMap<>(); // Map to store buy limits

            // Initialize item prices and buy limits
            for (String itemName : itemNames) {
                int pricePerItem = (int) (priceChecker(itemName)[0] * 1.25); // Increase price by 25%
                itemPrices.put(itemName, pricePerItem);
                itemsBought.put(itemName, 0);
                int buyLimit = GELimits.getRemainingBuyLimit(itemName); // Get the buy limit for each item
                if (buyLimit == 0) return false; // Return false if any item's buy limit is zero
                itemBuyLimits.put(itemName, buyLimit);
            }

            double totalRatio = 0;
            for (double r : ratio) totalRatio += r;

            long totalCost = 0;
            Map<String, Integer> initialItemsToBuy = new HashMap<>();
            boolean limitHit = false;

            // Calculate maximum quantities while considering the buy limits
            for (int i = 0; i < itemNames.length; i++) {
                initialItemsToBuy.clear();
                totalCost = 0;
                int buyLimit = itemBuyLimits.get(itemNames[i]);

                // Check if the current item hits the buy limit
                for (int j = 0; j < itemNames.length; j++) {
                    String itemName = itemNames[j];
                    int pricePerItem = itemPrices.get(itemName);
                    double itemRatio = ratio[j];
                    int quantity;
                    if (i == j) {
                        quantity = Math.min(limit, buyLimit); // Use Math.min for the current item
                        limitHit = true;
                    } else {
                        quantity = (int) Math.floor(Math.min(limit, buyLimit) * (itemRatio / ratio[i]));
                    }
                    initialItemsToBuy.put(itemName, quantity);
                    totalCost += quantity * pricePerItem;
                }

                if (totalCost <= coins) {
                    break;
                }
            }

            // If no item hit the buy limit in the above loop, manually enforce the buy limit for one item
            if (!limitHit) {
                initialItemsToBuy.clear();
                String itemName = itemNames[1];
                int buyLimit = itemBuyLimits.get(itemName);
                initialItemsToBuy.put(itemName, Math.min(limit, buyLimit)); // Set item2 to limit or buyLimit
                totalCost = initialItemsToBuy.get(itemName) * itemPrices.get(itemName);

                // Calculate quantities for other items based on ratio
                for (int i = 0; i < itemNames.length; i++) {
                    if (i == 1) continue; // Skip item2 since it already hits the limit
                    itemName = itemNames[i];
                    int pricePerItem = itemPrices.get(itemName);
                    int quantity = (int) Math.floor(Math.min(limit, buyLimit) * (ratio[i] / ratio[1]));
                    initialItemsToBuy.put(itemName, quantity);
                    totalCost += quantity * pricePerItem;
                }
            }

            // Adjust the quantities proportionally to fit within the remaining coins
            if (totalCost > coins) {
                double scaleFactor = (double) coins / totalCost;
                for (String itemName : itemNames) {
                    int adjustedQuantity = (int) Math.floor(initialItemsToBuy.get(itemName) * scaleFactor);
                    initialItemsToBuy.put(itemName, adjustedQuantity);
                }
            }

            // Update itemsBought and subtract cost from coins
            for (String itemName : itemNames) {
                int itemsToBuyItem = initialItemsToBuy.get(itemName);
                itemsBought.put(itemName, itemsToBuyItem);
                coins -= itemsToBuyItem * itemPrices.get(itemName);
            }

            // Adjust quantities to ensure even values if necessary
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
        } else {
            String itemName = itemNames[0];
            int pricePerItem = (int) priceChecker(itemName)[0];
            int amount = (int) Math.min(Math.floor((double) coins / pricePerItem), limit);
            System.out.println("price: " + pricePerItem + " amount: " + amount + " item: " + itemNames);
            return goBuyAndReturn(new int[]{amount}, true, true, 10, collectInBank, itemNames);
        }
    }

    public static void fetchPriceData() throws Exception {
        System.out.println("Entering fetchPriceData() function");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(API_URL))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "Google Sheet Updater")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.body());
            priceData = rootNode.path("data");
        } else {
            throw new RuntimeException("Failed to fetch data: HTTP " + response.statusCode());
        }
        System.out.println("Exiting fetchPriceData() function");
    }

    public static int getHighPriceForId(int id) throws Exception {
        fetchPriceData();
        //System.out.println("Entering getHighPriceForId() function");
        if (priceData == null) {
            throw new IllegalStateException("Price data has not been fetched yet.");
        }
        JsonNode itemData = priceData.path(String.valueOf(id));
        if (itemData.isMissingNode()) {
            throw new IllegalArgumentException("No data found for ID " + id);
        }
        //System.out.println("Exiting getHighPriceForId() function");
        //return itemData.path("high").asInt();
        return Math.max(itemData.path("high").asInt(), itemData.path("low").asInt());
    }
}
