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
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.grandexchange.GrandExchangeSlots;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.math.Random;
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

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.microbot.util.math.Random.random;

public class Ras_highalcScript extends Script {
    public static double version = 1.0;
    public static String opitem = "";
    private static final WorldArea grandexchange = new WorldArea(3167, 3486, 5, 5, 0);
    private static final long UPDATE_INTERVAL = 4 * 60 * 60 * 1000; // Update interval in milliseconds (4 hours)
    private static Map<String, ProfitData> profitDataMap = new HashMap<>();
    private OSRSPriceFetcher fetcher = new OSRSPriceFetcher();
    boolean withdrawcoins  = false;
    public InactivityTimer inactivityTimer = new InactivityTimer();

    @Inject
    private ItemManager itemManager;

    @Inject
    private ClientThread clientThread;

    @Inject
    private RuneLiteConfig runeLiteConfig;

    public boolean run(Ras_highalcConfig config) {
        long stopTimer = random(1800000,2760000) + System.currentTimeMillis();
        Microbot.enableAutoRunOn = false;
        String itemListString = config.itemList();
        int naturalRunePrice = config.naturalRunePrice();
        Rs2Magic rs2Magic = new Rs2Magic();
        ConcurrentHashMap<String, Integer> remainingQuantities = new ConcurrentHashMap<>();

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            rasMasterScriptScript.autoShutdown("ras_high alc");
            if (!super.run()) return;
            try {
                if (!withdrawcoins) {
                    onetimeChecks();
                    withdrawcoins = true;
                }
                if (Rs2Inventory.hasItem("coins")) {
                    System.out.println(Rs2Inventory.ItemQuantity(995));
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
                if (config.highAlch())rs2Magic.alchInventory(config);
                if (stopTimer < System.currentTimeMillis()){
                    if (config.autoBuy())
                        Microbot.getPluginManager().setPluginValue("general", "Autobuy", false);
                    else if (!config.autoBuy() && config.highAlch() && Rs2Inventory.count() <= 2) {
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
                System.out.println("crash");
                System.out.println(ex.getMessage());
                System.out.println("crash");
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    private ProfitData getMostProfitableItem(String itemListString, int naturalRunePrice) {
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
        return null;
    }

    private void updateProfitData(String itemListString, int naturalRunePrice) {
        //itemManager.loadPrices();
        sleep(1500);
        int new_method = 0;
        long gePrice = 0;
        try {
        fetcher.fetchPriceData();
        } catch (Exception e) {
            System.out.println("Failed to fetch price data: " + e.getMessage());
            new_method =1;
        }
        List<String> itemList = Arrays.asList(itemListString.split(","));
        for (String item : itemList) {
            int colonIndex = item.indexOf(':');
            try {
                if (colonIndex != -1) {
                    String itemName = item.substring(0, colonIndex).trim();
                    int buyLimit = Integer.parseInt(item.substring(colonIndex + 1).trim());
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
            }
            catch(Exception ex){
                    System.out.println("item not found: " + item);
                }
        }
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
        String pluginName = "ras_high alc";
        rasMasterScriptScript masterControl = new rasMasterScriptScript();
        masterControl.stopPlugin(pluginName);
        do{sleep(2000);}
        while (masterControl.isPlugEnabled(pluginName));
        super.shutdown();
    }

    public boolean goto_grand_exchange(){
        WorldPoint randomBankPoint = grandexchange.toWorldPointList().get(Random.random(0, grandexchange.toWorldPointList().size() - 1));
        Rs2Walker.walkTo(randomBankPoint);
        sleepUntil(() -> grandexchange.contains(Microbot.getClient().getLocalPlayer().getWorldLocation()),30000);
        return true;
    }

    public void inventrySetup() {
        if (!Rs2Inventory.hasItem("Nature rune")){
            if(!Rs2Bank.isOpen()) {
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
            if (!Rs2Inventory.hasItem("Staff of fire")) {
                Rs2Inventory.equip("Staff of fire");
            } else {
                Rs2Bank.openBank();
                if (Rs2Bank.hasBankItem("Staff of fire")) {
                    Rs2Bank.withdrawAndEquip("Staff of fire");
                } else {
                    // no staff no alcomy or buy staff of fire or withrdraw 5 fire runes atleast after checking
                    shutdown();
                }
            }

        }
    }
    private  void buy_nature_rune(){
        long availableMoney = Rs2Inventory.ItemQuantity(995);
        if (availableMoney > 10000) {
            if(Rs2Bank.isOpen()) {Rs2Bank.closeBank();sleepUntil(()->!Rs2Bank.closeBank());}
            if(!Rs2GrandExchange.isOpen()) {Rs2GrandExchange.openExchange();sleepUntil(()->Rs2GrandExchange.isOpen());}
            buyItem("Nature rune", howManyToBuy(availableMoney), 150);
            while (!Rs2Inventory.hasItem("Nature rune")) {
                Rs2GrandExchange.collectToInventory();
                sleep(1000);
            }
        }
    }
    private  void nature_rune_check(){
        if (!Rs2Inventory.hasItem("Nature rune") || Rs2Inventory.ItemQuantity("Nature rune") < 130) {
            long availableMoney = Rs2Inventory.ItemQuantity(995);
            if (availableMoney > 10000) {
                if(Rs2Bank.isOpen()) {Rs2Bank.closeBank();sleepUntil(()->!Rs2Bank.closeBank());}
                if(!Rs2GrandExchange.isOpen()) {Rs2GrandExchange.openExchange();sleepUntil(()->Rs2GrandExchange.isOpen());}
                int result = howManyToBuy(availableMoney);
                System.out.println("Available Money: " + availableMoney + ", nature rune buying: " + result);
                buyItem("Nature rune", result, 150);
                while (!Rs2Inventory.hasItem("Nature rune") || Rs2Inventory.ItemQuantity("Nature rune") < 100) {
                    Rs2GrandExchange.collectToInventory();
                    sleep(1000);
                }
                if(Rs2GrandExchange.isOpen()) {Rs2GrandExchange.closeExchange();sleepUntil(()->!Rs2GrandExchange.isOpen());}
            }
        }
    }
    public static int howManyToBuy(long totalSum) {
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
        return Math.min(maxItems, maxAffordableItems);
    }
    private void buyItem(String itemName, int buyLimit, int gePrice) {
        Rs2GrandExchange.buyItem(itemName,gePrice,buyLimit);
        inactivityTimer.update();
    }
    private boolean isTimetobuy() {
        if (!Rs2GrandExchange.isOpen()){
            Rs2GrandExchange.openExchange();
        }
        sleep(100,250);
        System.out.println("Item brought: " + Rs2GrandExchange.hasBoughtOffer());
        Rs2GrandExchange.collectToInventory();
        Pair<GrandExchangeSlots, Integer> slot = Rs2GrandExchange.getAvailableSlot();
        System.out.println(slot.getLeft());
        if (slot.getLeft() != null){
            return true;
        }
        else {
            return false;
        }
    }
    private void onetimeChecks(){
        goto_grand_exchange();
        Rs2Bank.openBank();
        if (Rs2Bank.hasBankItem("coins")) {
            Rs2Bank.withdrawAll("Coins");
            sleepUntil(() -> Rs2Inventory.hasItem("coins"));
        }
        sleepUntil(() -> Rs2Bank.closeBank());
        inventrySetup();
    }
    public class Rs2Magic extends Script{

        public void alchInventory(Ras_highalcConfig config) {
            if (Microbot.getClient().getRealSkillLevel(Skill.MAGIC) < 55) {
                throw new IllegalStateException("Magic level is too low for High Alchemy");
            }
            for (int j = 0; j < 3; j++) {
                List<Rs2Item> allItems = Rs2Inventory.all();
                for (Rs2Item item : allItems) {
                    String itemName = item.name.toLowerCase();
                    if (!itemName.contains("coins") && !itemName.contains("nature rune")) {
                        Rs2GrandExchange.closeExchange();
                        System.out.println("Item: " + item.name);
                        sleep(250, 400);
                        int totalQuantity = (int) Rs2Inventory.ItemQuantity(item.id);
                        for (int i = 0; i < totalQuantity; i++) {
                            if (!config.highAlch()) return;
                            highAlch(item);
                        }
                        inactivityTimer.update();
                    }
                }
            }
        }

        private void highAlch(Rs2Item item) {
            if (Rs2Tab.getCurrentTab() != InterfaceTab.MAGIC) {
                Rs2Tab.switchToMagicTab();
                sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC, 5000);
            }
            sleep(150,300);
            // Find the high alchemy widget
            Widget highAlch = Rs2Widget.findWidget(MagicAction.HIGH_LEVEL_ALCHEMY.getName());

            // If high alchemy widget is not found, return
            if (highAlch.getSpriteId() != 41){
                onetimeChecks();
                return;
            }

            // Get the center point of the high alchemy widget
            Point point = new Point((int) highAlch.getBounds().getCenterX(), (int) highAlch.getBounds().getCenterY());

            Rs2Tab.getCurrentTab();
            // Click on the high alchemy widget
            Microbot.getMouse().click(point);
            sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.INVENTORY, 5000);

            if (Rs2Tab.getCurrentTab() == InterfaceTab.INVENTORY ) {
                Microbot.status = "Alching " + item.name;
                System.out.println("Alching " + item.name);
                System.out.println("type " + item.name.getClass().getName());
            }
            else {
                Rs2Tab.switchToInventoryTab();
                sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.INVENTORY, 5000);
                Microbot.status = "Alching " + item.name;
            }
            Rs2Inventory.interact(item.name, "Cast");
            sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC, 5000);
        }
    }

    public class OSRSPriceFetcher {

        private static final String API_URL = "https://prices.runescape.wiki/api/v1/osrs/latest";
        private  final HttpClient client = HttpClient.newHttpClient();
        private  JsonNode priceData = null;

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
        }

        public int getHighPriceForId(int id) {
            if (priceData == null) {
                throw new IllegalStateException("Price data has not been fetched yet.");
            }
            JsonNode itemData = priceData.path(String.valueOf(id));
            if (itemData.isMissingNode()) {
                throw new IllegalArgumentException("No data found for ID " + id);
            }
            return itemData.path("high").asInt();
        }
    }
    private void checkForInactivity(Ras_highalcConfig config) {
        System.out.println("last active time "+ inactivityTimer.getElapsedTime());
        getInactivityTimer();
        if (inactivityTimer.hasExceededInterval((long) config.waitTime() * 60 * 1000)) {
            abort();
            inactivityTimer.update();
        }
    }
    public long getInactivityTimer() {
        return inactivityTimer.lastActiveTime;
    }

    private void abort() {
        System.out.println("Aborting due to inactivity.");
        abortAllActiveOffers();
    }
    public class InactivityTimer {
        public long lastActiveTime;

        public InactivityTimer() {
            this.lastActiveTime = System.currentTimeMillis();
        }

        public void update() {

            this.lastActiveTime = System.currentTimeMillis();
        }

        public boolean hasExceededInterval(long interval) {
            long currentTime = System.currentTimeMillis();
            return (currentTime - lastActiveTime) > interval;
        }
        public String getElapsedTime() {
            try {
                if (lastActiveTime == 0) {
                    throw new IllegalStateException("lastActiveTime is 0");
                }

                long currentTime = System.currentTimeMillis();
                long elapsedMillis = currentTime - lastActiveTime;

                long seconds = (elapsedMillis / 1000) % 60;
                long minutes = (elapsedMillis / (1000 * 60)) % 60;

                return String.format("%02d:%02d", minutes, seconds);
            } catch (Exception e) {
                return "00:00";
            }
        }
    }
    public boolean abortAllActiveOffers() {
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
}

