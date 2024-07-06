package net.runelite.client.plugins.microbot.rasMagicTrain;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;

import static net.runelite.client.plugins.microbot.util.Global.sleep;

public class RasClass {
    public static void main(String[] args) {
        // Example usage
        int totalCoins = 10000;
        int runeCost = 200;
        int ore1Cost = 100;
        int ore2Cost = 50;

        // Example with three values
        int[] result = rasFunction(totalCoins, runeCost, ore1Cost, ore2Cost, 1, 1, 2);
        System.out.println("Three values example:");
        System.out.println("Runes to buy: " + result[0]);
        System.out.println("Ore1 to buy: " + result[1]);
        System.out.println("Ore2 to buy: " + result[2]);

        // Example with two values
        result = rasFunction(totalCoins, runeCost, ore1Cost, ore2Cost, 1, 2);
        System.out.println("Two values example:");
        System.out.println("Runes to buy: " + result[0]);
        System.out.println("Ore1 to buy: " + result[1]);
        System.out.println("Ore2 to buy: " + result[2]);

        // Grand Exchange operations
        grandExchangeOperations(totalCoins, runeCost, ore1Cost, ore2Cost, 1, 1, 2);
    }

    public static int[] rasFunction(int totalCoins, int runeCost, int ore1Cost, int ore2Cost, int ratioRune, int ratioOre1, int ratioOre2) {
        int unitCost = runeCost * ratioRune + ore1Cost * ratioOre1 + ore2Cost * ratioOre2;
        int maxUnits = totalCoins / unitCost;

        int runesToBuy = maxUnits * ratioRune;
        int ore1ToBuy = maxUnits * ratioOre1;
        int ore2ToBuy = maxUnits * ratioOre2;

        return new int[]{runesToBuy, ore1ToBuy, ore2ToBuy};
    }

    public static int[] rasFunction(int totalCoins, int runeCost, int ore1Cost, int ore2Cost, int ratioRune, int ratioOre1) {
        int unitCost = runeCost * ratioRune + ore1Cost * ratioOre1 + ore2Cost;
        int maxUnits = totalCoins / unitCost;

        int runesToBuy = maxUnits * ratioRune;
        int ore1ToBuy = maxUnits * ratioOre1;
        int ore2ToBuy = maxUnits;

        return new int[]{runesToBuy, ore1ToBuy, ore2ToBuy};
    }

    public static void grandExchangeOperations(int totalCoins, int runeCost, int ore1Cost, int ore2Cost, int ratioRune, int ratioOre1, int ratioOre2) {
        int[] proportions = rasFunction(totalCoins, runeCost, ore1Cost, ore2Cost, ratioRune, ratioOre1, ratioOre2);

        int runesToBuy = proportions[0];
        int ore1ToBuy = proportions[1];
        int ore2ToBuy = proportions[2];

        // Perform the Grand Exchange operations
        executeGrandExchange("Steel bar", "Nature rune", runesToBuy);
        executeGrandExchange(null, "iron ore", ore1ToBuy);
        executeGrandExchange(null, "coal", ore2ToBuy);
    }

    public static void executeGrandExchange(String itemToSell, String itemToBuy, int quantityToBuy) {
        Rs2GrandExchange.openExchange();
        sleep(2000);
        // Sell the specified item
        if (itemToSell != null && Rs2Inventory.hasItem(itemToSell)) {
            Rs2GrandExchange.sellItemUnder5Percent(itemToSell);
            sleep(2000);
            while (!Rs2GrandExchange.hasSoldOffer() )
                sleep(5000);
        }

        // Buy the specified item
        if (itemToBuy != null) {
            //Rs2GrandExchange.buyItemAbove5Percent(itemToBuy, quantityToBuy);
            Rs2GrandExchange.buyItem(itemToBuy, Microbot.getItemManager().search(itemToBuy).get(0).getPrice(),quantityToBuy);
            sleep(2000);
            while (!Rs2GrandExchange.hasBoughtOffer())
                sleep(5000);
        }

        if (itemToBuy != null && itemToBuy.contains("Nature rune")){
            Rs2GrandExchange.collectToInventory();
        }else
            Rs2GrandExchange.collectToBank();
        sleep(2000);
        // Deposit all items except Nature runes
        Rs2Bank.depositAllExcept("Nature rune");
        sleep(2000);


        // Close and open Grand Exchange
        Rs2GrandExchange.closeExchange();
        sleep(2000);

    }
}
