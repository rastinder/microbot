package net.runelite.client.plugins.microbot.rasshopping;

import net.runelite.api.coords.WorldPoint;

public enum ShopItems {
    ITEM_1("Item Name 1", "10", 5, new WorldPoint(3035, 9845, 0), "Drogo dwarf", 20, 500, true, 100), // mine
    ITEM_3("Item Name 1", "10", 5, new WorldPoint(3202, 3433, 0), "Shopkeeper Name 1", 20, 500, true, 100), // magic location
    foodShop("Pot of flour,Raw beef,Raw chicken,Cabbage,Banana,Redberries,Bread,Chocolate bar,Cheese,Tomato,Potato", "5", 1, new WorldPoint(3013, 3206, 0), "Wydin", 20, 500, false, 5), //
    fishShop("Raw sardine", "50", 1, new WorldPoint(3014, 3225, 0), "Gerrant", 20, 50000, false, 280); //
    // Add more items as needed

    private final String itemName;
    private final String buyQuantity;
    private final int minQuantity;
    private final WorldPoint shopLocation;
    private final String shopkeeperName;
    private final int nearestBankDistance; // in tiles
    private final int dailyVolume; // items that can be sold daily
    private final boolean stackable; // whether the item is stackable
    private final int shopPrice; // hardcoded shop price

    ShopItems(String itemName, String buyQuantity, int minQuantity, WorldPoint shopLocation, String shopkeeperName, int nearestBankDistance, int dailyVolume, boolean stackable, int shopPrice) {
        this.itemName = itemName;
        this.buyQuantity = buyQuantity;
        this.minQuantity = minQuantity;
        this.shopLocation = shopLocation;
        this.shopkeeperName = shopkeeperName;
        this.nearestBankDistance = nearestBankDistance;
        this.dailyVolume = dailyVolume;
        this.stackable = stackable;
        this.shopPrice = shopPrice;
    }

    public String getItemName() {
        return itemName;
    }

    public String getBuyQuantity() {
        return buyQuantity;
    }

    public int getMinQuantity() {
        return minQuantity;
    }

    public WorldPoint getShopLocation() {
        return shopLocation;
    }

    public String getShopkeeperName() {
        return shopkeeperName;
    }

    public int getNearestBankDistance() {
        return nearestBankDistance;
    }

    public int getDailyVolume() {
        return dailyVolume;
    }

    public boolean isStackable() {
        return stackable;
    }

    public int getShopPrice() {
        return shopPrice;
    }

    // Add a method to calculate the time to bank
    public double getTimeToBank() {
        // Assuming 10 tiles take 7 seconds
        return (nearestBankDistance / 10.0) * 7.0;
    }
}