package net.runelite.client.plugins.microbot.rasshopping;

import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.geHandler.geHandlerScript;
import net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.shop.Rs2Shop;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;


public class rasshoppingScript extends Script {
    @Inject
    private Client client;

    public static long stopTimer = 1;
    private String activity = "buyFromShop";
    private WorldPoint shopLocation;
    private String shopkeeper;
    private String itemQuantity;
    private String itemName;
    private WorldPoint depositebox = new WorldPoint(3046,3235,0);

    @Inject
    public rasshoppingScript(Client client) {
        this.client = client;
        ShopItems foodItem;
        if (random(0,2)==0)
            foodItem = ShopItems.fishShop;
        else
            foodItem = ShopItems.foodShop;

        this.shopLocation = foodItem.getShopLocation();
        this.shopkeeper = foodItem.getShopkeeperName();
        this.itemQuantity = foodItem.getBuyQuantity();
        this.itemName = foodItem.getItemName();
    }


    public boolean run(rasshoppingConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (rasMasterScriptScript.autoShutdown("ras Shopping"))return;
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (stopTimer == 1)
                    stopTimer = rasMasterScriptScript.autoStopTimer();
                doThis();

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 200, TimeUnit.MILLISECONDS);
        return true;
    }

    private void doThis() {
        switch (activity) {
            case "buyFromShop":
                try {
                    if (Rs2Inventory.ItemQuantity(995) < 20) {
                        activity = "sellToGe";
                        return;
                    }
                    if (Rs2Npc.hasLineOfSight(Rs2Npc.getNpc(shopkeeper)) && Rs2Shop.openShop(shopkeeper)) {
                        List<Rs2Item> items = Rs2Shop.shopItems;
                        for (Rs2Item item : items) {
                            if (Rs2Shop.hasMinimumStock(item.getName(), 1) && itemName.contains(item.getName())) {
                                Rs2Shop.buyItem(item.getName(), itemQuantity);
                                sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)) , 100, 1000);
                                activity = "deposit";
                            }
                        }
                    } else if (Rs2Player.getWorldLocation().distanceTo(shopLocation) > 10) {
                        Rs2Walker.setTarget(null);
                        Rs2Walker.walkTo(shopLocation, 6);
                        Rs2Player.waitForAnimation();
                        return;
                    }
                } catch (Exception ex) {
                    if (Rs2Player.getWorldLocation().distanceTo(shopLocation) > 10) {
                        Rs2Walker.setTarget(null);
                        Rs2Walker.walkTo(shopLocation, 6);
                        Rs2Player.waitForAnimation();
                        return;
                    }
                }
                break;
            case "deposit":
                if (Rs2Player.getWorldLocation().distanceTo(depositebox) > 10) {
                    Rs2Walker.setTarget(null);
                    Rs2Walker.walkTo(depositebox);
                    Rs2Player.waitForAnimation();
                    return;
                }
                //if (Rs2Player.getWorldLocation().distanceTo(depositebox) <= 10 && Rs2Player.getWorldLocation().distanceTo(depositebox) > 3) {
                if ((Rs2Player.getWorldLocation().distanceTo(depositebox) <= 10) && Rs2Inventory.hasItem("Coins", true)) {
                    //if (Rs2Player.getWorldLocation().distanceTo(depositebox) <= 10) {
                    //    Rs2Walker.setTarget(null);
                    //    Rs2Walker.walkFastCanvas(depositebox);
                    //    Rs2Player.waitForWalking();
                   // }
                    while (Rs2Inventory.hasItem("Coins", true)) {
                        Rs2Inventory.drop("Coins");
                        sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)) , 100, 2000);
                    }
                    return;
                }
                if (!Rs2Inventory.hasItem("Coins", true)) {
                    if (Rs2GameObject.interact("Bank deposit box", "Deposit")) {
                        sleepUntilTrue(() -> Rs2Widget.hasWidget("The Bank of"), 100, 5000);
                        if (Rs2Widget.hasWidget("The Bank of")) {
                            if (Microbot.getVarbitValue(4430) != 4)
                                //client.setVarbit(4430,4);
                                Rs2Widget.clickWidget("All");
                            if (Rs2Inventory.hasItem("Coins", true)) {
                                System.out.println("we have coins why?");
                            }
                            Rs2Widget.clickWidget(192, 4); // deposite all
                            sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)) , 100, 2000);
                            Rs2Keyboard.keyPress(KeyEvent.VK_ESCAPE);
                            sleep(400);
                            Rs2GroundItem.take("Coins", 15);
                            sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)) , 100, 10000);
                            if(stopTimer < System.currentTimeMillis())
                                activity = "sellToGe";
                            else
                                activity = "buyFromShop";
                        }
                    }
                }
                break;
            case "sellToShop":
                break;
            case "buyFromGe":
                break;
            case "sellToGe":
                String[] itemNamesArray = Arrays.stream(itemName.split(","))
                        .filter(item -> !item.contains("Chocolate bar"))
                        .filter(item -> !item.contains("Redberries"))
                        .filter(item -> !item.contains("Pot of flour"))
                        .toArray(String[]::new);

                int[] amount = new int[itemNamesArray.length];
                geHandlerScript.goSell(false,10,true,amount,itemNamesArray);
                if(stopTimer < System.currentTimeMillis())
                    shutdown();
                else
                    rasMasterScriptScript.bankAllAndGet(5000,"Coins");
                activity ="buyFromShop";
                break;
        }
    }
    public ShopItems printFoodItemDetails() {
        // Loop through all enum values
        for (ShopItems item : ShopItems.values()) {
            // Check if the item is a food item based on itemName or any other property
            if (item == ShopItems.foodShop) { // Modify this condition to match your criteria for food items
                System.out.println("Item: " + item.getItemName());
                System.out.println("Buy Quantity: " + item.getBuyQuantity());
                System.out.println("Minimum Quantity: " + item.getMinQuantity());
                System.out.println("Location: " + item.getShopLocation());
                System.out.println("Shopkeeper: " + item.getShopkeeperName());
                System.out.println("Nearest Bank Distance: " + item.getNearestBankDistance() + " tiles");
                System.out.println("Daily Volume: " + item.getDailyVolume());
                System.out.println("Stackable: " + item.isStackable());
                System.out.println("Shop Price: " + item.getShopPrice());
                System.out.println("Time to Bank: " + item.getTimeToBank() + " seconds");
                System.out.println("-----------------------");
                return item;
            }
        }
        return null;
    }

    @Override
    public void shutdown() {
        stopTimer = 1;
        rasMasterScriptScript.stopPlugin("ras Shopping");
        super.shutdown();
    }
}
