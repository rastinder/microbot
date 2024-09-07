package net.runelite.client.plugins.microbot.ras_highalc.ras;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.geHandler.geHandlerScript;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.grandexchange.GrandExchangeSlots;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;

public class Ras_highalcScript extends Script {
    public static double version = 1.0;
    public static String opitem = "";
    private static final WorldArea grandexchange = new WorldArea(3167, 3486, 5, 5, 0);
    private static final long UPDATE_INTERVAL = 4 * 60 * 60 * 1000; // Update interval in milliseconds (4 hours)
    private static Map<String, ProfitData> profitDataMap = new HashMap<>();
    private OSRSPriceFetcher fetcher = new OSRSPriceFetcher();
    boolean withdrawcoins = false;
    public InactivityTimer inactivityTimer = new InactivityTimer();
    public static String inactiveTime;
    int buylimit = 3000;
    String itemListString = "";

    @Inject
    private ItemManager itemManager;

    @Inject
    private ClientThread clientThread;

    @Inject
    private RuneLiteConfig runeLiteConfig;

    public boolean run(Ras_highalcConfig config) {
        System.out.println("Entering run() function");
        Microbot.enableAutoRunOn = false;
        int naturalRunePrice = config.naturalRunePrice();
        Rs2Magic rs2Magic = new Rs2Magic();
        ConcurrentHashMap<String, Integer> remainingQuantities = new ConcurrentHashMap<>();
        long stopTimer = random(1800000,2760000) + System.currentTimeMillis();

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            rasMasterScriptScript.autoShutdown("ras_high alc");
            System.out.println("Entering mainScheduledFuture runnable");
            if (!super.run()) return;
            try {
                inactiveTime = inactivityTimer.getElapsedTime();
                if (!withdrawcoins) {
                    itemListString = itemListString();
                    onetimeChecks();
                    withdrawcoins = true;
                }
                if (Rs2Inventory.hasItem("coins")) {
                    System.out.println("coins " + Rs2Inventory.ItemQuantity(995));
                } else {
                    System.out.println("no coins in inventory");
                    return;
                }
                while (isTimetobuy() && config.autoBuy()) {
                    nature_rune_check();
                    ProfitData mostProfitableItem = getMostProfitableItem(itemListString, naturalRunePrice);
                    if (mostProfitableItem != null) {
                        String itemName = mostProfitableItem.getItemName();
                        int buyLimit = mostProfitableItem.getBuyLimit();
                        long availableMoney = Rs2Inventory.ItemQuantity(995);
                        int quantityToBuy = remainingQuantities.getOrDefault(itemName, buyLimit);
                        long totalCost = mostProfitableItem.getGePrice() * quantityToBuy;
                        opitem = String.format("name: %s profit: %d", itemName, mostProfitableItem.getProfit());

                        if (totalCost <= availableMoney) {
                            buyItem(itemName, quantityToBuy, (int) mostProfitableItem.getGePrice());
                            remainingQuantities.remove(itemName);
                            mostProfitableItem.setLastAccess(System.currentTimeMillis());
                            mostProfitableItem.setPartiallyBought(false);
                        } else if (availableMoney >= mostProfitableItem.getGePrice()) {
                            int maxAffordableQuantity = (int) (availableMoney / mostProfitableItem.getGePrice());
                            buyItem(itemName, maxAffordableQuantity, (int) mostProfitableItem.getGePrice());
                            remainingQuantities.put(itemName, quantityToBuy - maxAffordableQuantity);
                            mostProfitableItem.setPartiallyBought(true);
                        } else {
                            break;
                        }
                    } else {
                        System.out.println("No profitable item found at the moment.");
                    }
                }
                if (config.autoBuy()) checkForInactivity(config);
                if (config.highAlch()) rs2Magic.alchInventory(config);
                if (stopTimer < System.currentTimeMillis()){
                    System.out.println("wrap up started");
                    if (config.autoBuy())
                        Microbot.getPluginManager().setPluginValue("highalc", "Autobuy", true); // supposed to be false testing
                    else if (!config.autoBuy() && config.highAlch() && Rs2Inventory.count() <= 2) {
                        System.out.println("stopping high alch");
                        Rs2Bank.openBank();
                        sleep(350);
                        Rs2Bank.depositAll();
                        sleep(350);
                        Rs2Bank.closeBank();
                        shutdown();
                    }
                }
                sleep(800);
            } catch (Exception ex) {
                System.out.println("crash " + ex.getMessage());
            }
            System.out.println("Exiting mainScheduledFuture runnable");
        }, 0, 1000, TimeUnit.MILLISECONDS);
        System.out.println("Exiting run() function");
        return true;
    }

    private ProfitData getMostProfitableItem(String itemListString, int naturalRunePrice) {
        System.out.println("Entering getMostProfitableItem() function");
        long currentTime = System.currentTimeMillis();

        updateProfitData(itemListString, naturalRunePrice);

        List<Map.Entry<String, ProfitData>> sortedProfitData = new ArrayList<>(profitDataMap.entrySet());
        sortedProfitData.sort((entry1, entry2) -> Long.compare(entry2.getValue().getProfit(), entry1.getValue().getProfit()));

        for (Map.Entry<String, ProfitData> entry : sortedProfitData) {
            ProfitData data = entry.getValue();
            if (data.isPartiallyBought() || currentTime - data.getLastAccess() >= UPDATE_INTERVAL) {
                System.out.println("4 hour passed UPDATE_INTERVAL or partial buy");
                return data;
            }
        }
        System.out.println("Exiting getMostProfitableItem() function");
        return null;
    }

    private void updateProfitData(String itemListString, int naturalRunePrice) {
        System.out.println("Entering updateProfitData() function");
        itemManager.loadPrices();
        sleep(1500);
        int new_method = 0;
        long gePrice = 0;
        try {
            fetcher.fetchPriceData();
        } catch (Exception e) {
            System.out.println("Failed to fetch price data: " + e.getMessage());
            new_method = 1;
        }
        List<String> itemList = Arrays.asList(itemListString.split(","));
        for (String item : itemList) {
            int colonIndex = item.indexOf(':');
            try {
                if (colonIndex != -1) {
                    String itemName = item.substring(0, colonIndex).trim();
                    int buyLimit = Math.min(Integer.parseInt(item.substring(colonIndex + 1).trim()), buylimit);
                    int itemId = (int) Microbot.getClientThread().runOnClientThread(() ->
                            Microbot.getItemManager().search(itemName).get(0).getId());
                    if (new_method == 1) {
                        gePrice = (long) Microbot.getClientThread().runOnClientThread(() ->
                                Microbot.getItemManager().getItemPrice(itemId));
                    } else {
                        gePrice = (long) fetcher.getHighPriceForId(itemId);
                    }
                    gePrice = gePrice + 101;//  gePrice++;
                    long alchPrice = (long) Microbot.getClientThread().runOnClientThread(() ->
                            Microbot.getItemManager().getItemComposition(itemId).getHaPrice());
                    long profit = alchPrice - gePrice - naturalRunePrice;
                    ProfitData profitData = profitDataMap.get(itemName);
                    if (profitData != null) {
                        profitData.setItemId(itemId);
                        profitData.setBuyLimit(buyLimit);
                        profitData.setGePrice(gePrice);
                        profitData.setAlchPrice(alchPrice);
                        profitData.setProfit(profit);
                    } else {
                        profitDataMap.put(itemName, new ProfitData(itemName, itemId, buyLimit, gePrice, alchPrice, profit));
                    }
                } else {
                    System.out.println("Invalid item format: " + item);
                }
            } catch (Exception ex) {
                System.out.println("item not found: " + item);
            }
        }
        System.out.println("Exiting updateProfitData() function");
    }

    static class ProfitData {
        private String itemName;
        private int itemId;
        private int buyLimit;
        private long gePrice;
        private long alchPrice;
        private long profit;
        private long lastAccess;
        private boolean partiallyBought;

        public ProfitData(String itemName, int itemId, int buyLimit, long gePrice, long alchPrice, long profit) {
            this.itemName = itemName;
            this.itemId = itemId;
            this.buyLimit = buyLimit;
            this.gePrice = gePrice;
            this.alchPrice = alchPrice;
            this.profit = profit;
            this.lastAccess = 0;
            this.partiallyBought = false;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public int getItemId() {
            return itemId;
        }

        public void setItemId(int itemId) {
            this.itemId = itemId;
        }

        public int getBuyLimit() {
            return buyLimit;
        }

        public void setBuyLimit(int buyLimit) {
            this.buyLimit = buyLimit;
        }

        public long getGePrice() {
            return gePrice;
        }

        public void setGePrice(long gePrice) {
            this.gePrice = gePrice;
        }

        public long getAlchPrice() {
            return alchPrice;
        }

        public void setAlchPrice(long alchPrice) {
            this.alchPrice = alchPrice;
        }

        public long getProfit() {
            return profit;
        }

        public void setProfit(long profit) {
            this.profit = profit;
        }

        public long getLastAccess() {
            return lastAccess;
        }

        public void setLastAccess(long lastAccess) {
            this.lastAccess = lastAccess;
        }

        public boolean isPartiallyBought() {
            return partiallyBought;
        }

        public void setPartiallyBought(boolean partiallyBought) {
            this.partiallyBought = partiallyBought;
        }
    }

    @Override
    public void shutdown() {
        System.out.println("Entering shutdown() function");
        String pluginName = "ras_high alc";
        //rasMasterScriptScript masterControl = new rasMasterScriptScript();
        rasMasterScriptScript.stopPlugin(pluginName);
        do{sleep(2000);}
        while (rasMasterScriptScript.isPlugEnabled(pluginName));
        super.shutdown();
        System.out.println("Exiting shutdown() function");
    }

    public boolean goto_grand_exchange() {
        System.out.println("Entering goto_grand_exchange() function");
        WorldPoint randomBankPoint = grandexchange.toWorldPointList().get(Random.random(0, grandexchange.toWorldPointList().size() - 1));
        Rs2Walker.walkTo(randomBankPoint);
        sleepUntil(() -> grandexchange.contains(Microbot.getClient().getLocalPlayer().getWorldLocation()), 30000);
        System.out.println("Exiting goto_grand_exchange() function");
        return true;
    }

    public void inventrySetup() {
        System.out.println("Entering inventrySetup() function");
        if (!Rs2Inventory.hasItem("Nature rune")) {
            if (!Rs2Bank.isOpen()) {
                Rs2Bank.openBank();
                sleepUntil(() -> Rs2Bank.isOpen());
            }
            if (Rs2Bank.hasBankItem("Nature rune")) {
                Rs2Bank.withdrawAll("Nature rune");
                sleepUntil(() -> Rs2Inventory.hasItem("Nature rune"));
            } else {
                //buy_nature_rune();
                nature_rune_check();
            }
        }
        if (!Rs2Equipment.hasEquippedContains("Staff of fire")) {
            if (Rs2Inventory.hasItem("Staff of fire")) {
                Rs2Inventory.equip("Staff of fire");
            } else {
                Rs2Bank.openBank();
                if (Rs2Bank.hasBankItem("Staff of fire")) {
                    Rs2Bank.withdrawAndEquip("Staff of fire");
                } else {
                    System.out.println("shutdown");
                    // no staff no alcomy or buy staff of fire or withrdraw 5 fire runes atleast after checking
                    shutdown();
                }
            }

        }
        System.out.println("Exiting inventrySetup() function");
    }

    private void buy_nature_rune() {
        System.out.println("Entering buy_nature_rune() function");
        long availableMoney = Rs2Inventory.ItemQuantity(995);
        if (availableMoney > 10000) {
            if (Rs2Bank.isOpen()) {
                Rs2Bank.closeBank();
                sleepUntil(() -> !Rs2Bank.closeBank());
            }
            if (!Rs2GrandExchange.isOpen()) {
                Rs2GrandExchange.openExchange();
                sleepUntil(() -> Rs2GrandExchange.isOpen());
            }
            buyItem("Nature rune", howManyToBuy(availableMoney), 150);
            while (!Rs2Inventory.hasItem("Nature rune")) {
                Rs2GrandExchange.collectToInventory();
                sleep(1000);
            }
        }
        System.out.println("Exiting buy_nature_rune() function");
    }

    private void nature_rune_check() {
        System.out.println("Entering nature_rune_check() function");
        if (!Rs2Inventory.hasItem("Nature rune") || Rs2Inventory.ItemQuantity("Nature rune") < 130) {
            long availableMoney = Rs2Inventory.ItemQuantity(995);
            if (availableMoney > 10000) {
                if (Rs2Bank.isOpen()) {
                    Rs2Bank.closeBank();
                    sleepUntil(() -> !Rs2Bank.closeBank());
                }
                if (!Rs2GrandExchange.isOpen()) {
                    Rs2GrandExchange.openExchange();
                    sleepUntil(() -> Rs2GrandExchange.isOpen(), 5000);
                }
                int result = howManyToBuy(availableMoney);
                System.out.println("Available Money: " + availableMoney + ", nature rune buying: " + result);
                sleep(100);
                Rs2GrandExchange.collectToInventory();
                if (Rs2GrandExchange.getAvailableSlot().getLeft() != null) {
                    buyItem("Nature rune", result, 150);
                    while (!Rs2Inventory.hasItem("Nature rune") || Rs2Inventory.ItemQuantity("Nature rune") < 100) {
                        Rs2GrandExchange.collectToInventory();
                        sleep(1000);
                    }
                    if (Rs2GrandExchange.isOpen()) {
                        Rs2GrandExchange.closeExchange();
                        sleepUntil(() -> !Rs2GrandExchange.isOpen());
                    }
                }
            }
        }
        System.out.println("Exiting nature_rune_check() function");
    }

    public static int howManyToBuy(long totalSum) {
        System.out.println("Entering howManyToBuy() function");
        int productCost = 150;
        int minItems = 100;
        int maxItems = 10000;
        double percentageToUse = 0.1; // 1%

        // Calculate the maximum amount we can spend
        long maxSpend = (long) (totalSum * percentageToUse);

        // Calculate the number of products we can afford
        int maxAffordableItems = (int) (maxSpend / productCost);

        // If we can afford fewer than the minimum required items, buy as many as we can afford
        if (maxAffordableItems < minItems) {
            if (totalSum < productCost) {
                return 0; // Can't afford any item
            } else {
                return (int) (totalSum / productCost); // Buy as many as we can afford
            }
        }

        // Otherwise, buy the minimum required items or the maximum we can afford, whichever is less
        System.out.println("Exiting howManyToBuy() function");
        return Math.min(maxItems, maxAffordableItems);
    }

    private void buyItem(String itemName, int buyLimit, int gePrice) {
        System.out.println("Entering buyItem() function");
        Rs2GrandExchange.buyItem(itemName, gePrice, buyLimit);
        inactivityTimer.update();
        System.out.println("Exiting buyItem() function");
    }

    private boolean isTimetobuy() {
        System.out.println("Entering isTimetobuy() function");
        if (!Rs2GrandExchange.isOpen()) {
            Rs2GrandExchange.openExchange();
        }
        sleep(100, 250);
        int totalCount = 0;
        List<Rs2Item> inItems = Rs2Inventory.all();
        for (int i = 0; i < inItems.size(); i++) {
            Rs2Item item = inItems.get(i);
            if (item != null) {
                if (!item.getName().equals("Coins") && !item.getName().equals("Nature rune")) {
                    System.out.println("Found the item: " + item.getName() + "count " + Rs2Inventory.ItemQuantity(item.getId()));
                    totalCount = totalCount + (int) Rs2Inventory.ItemQuantity(item.getId());
                }
            }
        }
        System.out.println("total inv count " + totalCount+ " with "+ inItems.size() + " items");
        System.out.println("vacant slot: " + Rs2GrandExchange.hasBoughtOffer());
        Rs2GrandExchange.collectToInventory();
        Pair<GrandExchangeSlots, Integer> slot = Rs2GrandExchange.getAvailableSlot();
        //System.out.println(slot.getLeft());
        if (slot.getLeft() != null && totalCount < 1000 && Rs2Inventory.count() < 16) { // inItems.size() < 25 t on sure inv doesnt go full
            System.out.println("Exiting isTimetobuy() function with true");
            return true;
        } else {
            if (totalCount >= 1500) // to protect that inv doesnt get full // inItems.size() < 25
                inactivityTimer.update();
            System.out.println("Exiting isTimetobuy() function with false");
            return false;
        }
    }

    private void onetimeChecks() {
        System.out.println("Entering onetimeChecks() function");
        goto_grand_exchange();
        Rs2Bank.openBank();
        sleepUntilTrue(Rs2Bank::openBank,1000,52000);
        if (Rs2Bank.hasBankItem("coins")) {
            Rs2Bank.withdrawAll("Coins");
            sleepUntil(() -> Rs2Inventory.hasItem("coins"));
        }
        sleepUntil(() -> Rs2Bank.closeBank());
        inventrySetup();
        System.out.println("Exiting onetimeChecks() function");
    }

    public class Rs2Magic extends Script {
        public void alchInventory(Ras_highalcConfig config) {
            System.out.println("Entering alchInventory() function");
            if (Microbot.getClient().getRealSkillLevel(Skill.MAGIC) < 55) {
                throw new IllegalStateException("Magic level is too low for High Alchemy");
            }
            for (int j = 0; j < 1; j++) {
                List<Rs2Item> allItems = Rs2Inventory.all();
                for (Rs2Item item : allItems) {
                    String itemName = item.name.toLowerCase();
                    if (!itemName.contains("coins") && !itemName.contains("nature rune")) {
                        Rs2GrandExchange.closeExchange();
                        sleep(250, 400);
                        int totalQuantity = (int) Rs2Inventory.ItemQuantity(item.id);
                        System.out.println("Item: " + item.name + " times " + totalQuantity);
                        for (int i = 0; i < totalQuantity; i++) {
                            System.out.println("i < totalQuantity: " +i+" < "+totalQuantity);
                            if (!config.highAlch()) {
                                System.out.println("Returning due to config.highAlch() being false");
                                return;
                            }
                            if (!super.run()) {
                                System.out.println("Returning due to super.run() being false");
                                //return; testing
                            }
                            highAlch(item);
                            System.out.println("successful bought " + Rs2GrandExchange.hasBoughtOffer());
                            if (Rs2GrandExchange.hasBoughtOffer() && Rs2Inventory.count() < 16) {
                                System.out.println("Returning due to Rs2GrandExchange.hasBoughtOffer() being true and inventory count < 16");
                                return;
                            }
                            if (config.autoBuy()) {
                                if (checkForInactivity(config) && Rs2Inventory.count() < 25) {
                                    System.out.println("Returning due to inactivity && inventory count < 25");
                                    return;
                                }
                            }
                            if (Rs2Inventory.ItemQuantity("Nature rune") < 100) {
                                System.out.println("Returning as nature rune count < 100");
                                return;
                            }
                            inactiveTime = inactivityTimer.getElapsedTime();
                            randomSleep();
                        }
                        //inactivityTimer.update();
                    }
                }
            }
            System.out.println("Exiting alchInventory() function");
        }


        private void highAlch(Rs2Item item) {
            System.out.println("Entering highAlch() function");
            if (Rs2Tab.getCurrentTab() != InterfaceTab.MAGIC) {
                Rs2Tab.switchToMagicTab();
                sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC, 5000);
            }
            sleep(150, 300);
            net.runelite.client.plugins.microbot.util.magic.Rs2Magic.alch(item);
            // Find the high alchemy widget
            Widget highAlch = Rs2Widget.findWidget(MagicAction.HIGH_LEVEL_ALCHEMY.getName());
            sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC, 5000);
            System.out.println("Exiting highAlch() function");
            return;
            /*
            // If high alchemy widget is not found, return
            if (highAlch.getSpriteId() != 41) {
                onetimeChecks();
                return;
            }

            // Get the center point of the high alchemy widget
            Point point = new Point((int) highAlch.getBounds().getCenterX(), (int) highAlch.getBounds().getCenterY());

            Rs2Tab.getCurrentTab();
            // Click on the high alchemy widget
            Microbot.getMouse().click(point);
            sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.INVENTORY, 5000);

            if (Rs2Tab.getCurrentTab() == InterfaceTab.INVENTORY) {
                Microbot.status = "Alching " + item.name;
                //System.out.println("Alching " + item.name);
                //System.out.println("type " + item.name.getClass().getName());
            } else {
                Rs2Tab.switchToInventoryTab();
                sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.INVENTORY, 5000);
                Microbot.status = "Alching " + item.name;
            }
            Rs2Inventory.interact(item.name, "Cast");
            sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC, 5000);
            System.out.println("Exiting highAlch() function");

             */
        }
    }

    public class OSRSPriceFetcher {
        private static final String API_URL = "https://prices.runescape.wiki/api/v1/osrs/latest";
        private final HttpClient client = HttpClient.newHttpClient();
        private JsonNode priceData = null;

        public void main(String[] args) {
            try {
                fetchPriceData();
                int id = 6;  // Example ID
                int highPrice = getHighPriceForId(id);
                System.out.println("High price for ID " + id + ": " + highPrice);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void fetchPriceData() throws Exception {
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

        public int getHighPriceForId(int id) {
            //System.out.println("Entering getHighPriceForId() function");
            if (priceData == null) {
                throw new IllegalStateException("Price data has not been fetched yet.");
            }
            JsonNode itemData = priceData.path(String.valueOf(id));
            if (itemData.isMissingNode()) {
                throw new IllegalArgumentException("No data found for ID " + id);
            }
            //System.out.println("Exiting getHighPriceForId() function");
            return itemData.path("high").asInt();
        }
    }

    private boolean checkForInactivity(Ras_highalcConfig config) {
        //System.out.println("Entering checkForInactivity() function");
        System.out.println("last active time " + inactivityTimer.getElapsedTime());
        System.out.println("getAvailableSlot " + Rs2GrandExchange.getAvailableSlot().getRight());
        getInactivityTimer();
        if (inactivityTimer.hasExceededInterval((long) config.waitTime() * 60 * 1000) && Rs2GrandExchange.getAvailableSlot().getRight() == 0) {
            abort();
            inactivityTimer.update();
            //System.out.println("Exiting checkForInactivity() function");
            return true;
        }
        return false;
    }

    public long getInactivityTimer() {
        //System.out.println("Entering getInactivityTimer() function");
        //System.out.println("Exiting getInactivityTimer() function");
        return inactivityTimer.lastActiveTime;
    }

    private void abort() {
        System.out.println("Entering abort() function");
        System.out.println("Aborting due to inactivity.");
        geHandlerScript.abortAllActiveOffers();
        System.out.println("Exiting abort() function");
    }

    public class InactivityTimer {
        public long lastActiveTime;

        public InactivityTimer() {
            this.lastActiveTime = System.currentTimeMillis();
        }

        public void update() {
            //System.out.println("Entering update() function");
            this.lastActiveTime = System.currentTimeMillis();
            //System.out.println("Exiting update() function");
        }

        public boolean hasExceededInterval(long interval) {
            System.out.println("Entering hasExceededInterval() function");
            long currentTime = System.currentTimeMillis();
            System.out.println("Exiting hasExceededInterval() function");
            return (currentTime - lastActiveTime) > interval;
        }

        public String getElapsedTime() {
            System.out.println("Entering getElapsedTime() function");
            try {
                if (lastActiveTime == 0) {
                    throw new IllegalStateException("lastActiveTime is 0");
                }

                long currentTime = System.currentTimeMillis();
                long elapsedMillis = currentTime - lastActiveTime;

                long seconds = (elapsedMillis / 1000) % 60;
                long minutes = (elapsedMillis / (1000 * 60)) % 60;

                System.out.println("Exiting getElapsedTime() function");
                return String.format("%02d:%02d", minutes, seconds);
            } catch (Exception e) {
                System.out.println("Exiting getElapsedTime() function");
                return "00:00";
            }
        }
    }

    public String itemListString() {
        System.out.println("Entering itemListString() function");
        if (Rs2Player.isMember()) {
            System.out.println("Exiting itemListString() function");
            return "Dragon med helm:8,Torn prayer scroll:5,Black d'hide body:70,Mystic lava staff:8,Dragon 2h sword:8,Amulet of the damned (full):8,Red d'hide body:70,Blue d'hide body:125,Dragon scimitar:70,Rune kiteshield:70,Rune sword:70,Rune battleaxe:70,Dragon dagger:70,Rune med helm:70,Mystic robe top:125,Runite limbs:4000,Rune mace:70,Lava battlestaff:8,Rune full helm:70,Adamant platelegs:125,Black d'hide vambraces:70,Rune pickaxe:40,Red spiky vambraces:125,Mystic robe bottom:125,Rune 2h sword:70,Adamant platebody:125,Rune platelegs:70,Green d'hide body:125,Rune sq shield:70,Splitbark gauntlets:70,Dragon mace:70,Rune axe:40,Rune chainbody:70,Rune plateskirt:70,Mystic earth staff:4000,Blue d'hide vambraces:125,Rune longsword:70,Ancient crystal:250,Adamant kiteshield:125,Combat bracelet:4000,Rune dagger:70,Snakeskin shield:125,Onyx bolts (e):4000,Mystic water staff:4000,Earth battlestaff:4000,Fire battlestaff:4000,Black longsword:125,Air battlestaff:4000,Amulet of defence:125,Steel platebody:125,Rune scimitar:70,Adamant sword:125,Water battlestaff:4000,Adamant pickaxe:40,Adamant sq shield:125,Blue d'hide shield:125,Mithril pickaxe:40,Combat bracelet(4):4000,Mystic air staff:4000,Mithril platebody:125,Mithril chainbody:125,Steel 2h sword:125,Mithril kiteshield:125,Adamant chainbody:125,Adamant axe:40,Adamant battleaxe:125,Green d'hide chaps:125,Adamant spear:125,Dragon longsword:70,Rune boots:70,Steel pickaxe:40,Diamond bracelet:4000,Phoenix necklace:4000,Steel battleaxe:125,Adamant 2h sword:125,Onyx dragon bolts (e):4000,Dragonstone bolts (e):4000,Diamond necklace:4000,Sapphire bracelet:4000,Redwood shield:125,Ring of life:4000,Mithril sword:125,Mithril longsword:125,Mithril 2h sword:125,Mithril full helm:125,Adamant mace:125,Dragon javelin heads:4000,Magic longbow:4000,Bracelet of clay:4000,Steel kiteshield:125,Studded chaps:125,Adamant med helm:125,Mithril mace:125,Black full helm:125,Rune javelin heads:4000,Adamant dagger:125,Amethyst javelin heads:4000,Inoculation bracelet:4000,Black sq shield:125,Diamond ring:4000,Yew shield:125,Ruby bracelet:4000,Ruby necklace:4000,Mithril axe:40,Atlatl dart:4000,Emerald necklace:4000,Iron platebody:125,Steel platelegs:125,Emerald bracelet:4000,Diamond amulet (u):4000,Yew longbow:4000,Abyssal bracelet(5):4000,Mithril battleaxe:125,Blue d'hide chaps:125,Ruby ring:4000,Steel sq shield:125,Mithril sq shield:125,Maple shield:125,Rune hasta:70,Sapphire necklace:4000,Steel longsword:125,Proselyte hauberk:70";
        } else {
            System.out.println("Exiting itemListString() function");
            return "Rune axe:40,Rune sword:70,Rune platelegs:70,Mithril kiteshield:125,Rune chainbody:70,Rune full helm:70,Rune pickaxe:40,Rune sq shield:70,Rune kiteshield:70,Rune platebody:70,Steel 2h sword:125,Rune 2h sword:70,Rune battleaxe:70,Adamant platebody:125,Rune med helm:70,Rune longsword:70,Rune warhammer:70,Rune dagger:70,Adamant 2h sword:125,Adamant pickaxe:40,Rune mace:70,Adamant platelegs:125,Rune plateskirt:70,Adamant full helm:125,Black longsword:125,Adamant sword:125,Mithril pickaxe:40,Mithril chainbody:125,Mithril plateskirt:125,Mithril mace:125,Iron platebody:125,Steel platebody:125,Mithril warhammer:125,Rune scimitar:70,Adamant dagger:125,Adamant sq shield:125,Mithril med helm:125,Adamant mace:125,Diamond necklace:18000,Mithril sq shield:125,Diamond amulet (u):10000,Steel platelegs:125,Green d'hide body:125,Steel warhammer:125,Mithril platebody:125,Adamant battleaxe:125,Black battleaxe:125,Steel kiteshield:125,Ring of forging:10000,Ruby necklace:18000,Emerald necklace:18000,Diamond ring:10000,Adamant plateskirt:125,Green d'hide chaps:125,Mithril axe:40,Castle wars bracelet(3):10000,Black 2h sword:125,Mithril battleaxe:125,Green d'hide vambraces:125,Emerald amulet (u):10000,Ruby amulet (u):10000,Sapphire necklace:18000,Steel longsword:125,Steel battleaxe:125,Mithril scimitar:125,Black sword:125,Adamant kiteshield:125,Steel plateskirt:125,Steel scimitar:125,Iron kiteshield:125,Ruby ring:10000,Sapphire ring:10000,Sapphire amulet (u):10000,Black full helm:125,Emerald ring:10000,Studded chaps:125";
        }
    }
    public void randomSleep(){
        if (random(1, 60) == 1) {
            System.out.println("sleep max 3sec");
            sleep(2000,3000);
        } else if (random(1,300) == 1) {
            System.out.println("sleep max 30sec");
            sleep(20000,30000);

        } else if (random(1,3000) == 1) {
            System.out.println("sleep max 10min");
            sleep(280000, 600000);
        }

    }
}