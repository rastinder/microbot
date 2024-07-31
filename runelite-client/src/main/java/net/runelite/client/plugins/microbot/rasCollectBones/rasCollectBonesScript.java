package net.runelite.client.plugins.microbot.rasCollectBones;

import net.runelite.api.AnimationID;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2Cannon;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;


public class rasCollectBonesScript extends Script {
    public static double version = 1.0;
    String Bones = "Big bones,Bones";
    List<String> BonesList = Arrays.asList(Bones.split(","));
    String foodSource = "Cow,Giant rat,Chicken,Bear,Cave Bug,Cave Crawler,Cow Calf,Crawling Hand,Dwarf,Farmer,Flesh Crawler,Frog,Giant,Giant Spider,Goblin,Guard,Hill Giant,Ice Giant,Jogre,Kalphite Worker,Lesser Demon,Lizardman,Minotaur,Monk,Moss Giant,Ogre,Pyrefiend,Rock Crab,Rockslug,Rogue,Sand Crab,Sea Snake Young,Shadow Warrior,Skeleton,Spider,Suqah,Troll,Wolf,Yeti,Yak";
    List<String> foodSourceList = Arrays.asList(foodSource.split(","));
    public int centerX = 0;
    public int centerY = 0;
    public WorldArea center = new WorldArea(1, 1, 1, 1, 0);
    public static long afktimer = System.currentTimeMillis();


    public boolean run(rasCollectBonesConfig config) {
        Microbot.enableAutoRunOn = false;
        AtomicInteger action = new AtomicInteger(1);
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) {
                return;
            }
            try {
                if (centerY == 0 && Microbot.getClient().getGameState() == GameState.LOGGED_IN) {
                    centerX = Rs2Player.getWorldLocation().getX();
                    centerY = Rs2Player.getWorldLocation().getY();
                    center = new WorldArea(Rs2Player.getWorldLocation(), 3, 3);
                }
                if (!config.toggleCombat() || !config.toggleLootItems()) {
                    if (config.toggleLootItems())
                        action.set(2);
                    if (config.toggleCombat())
                        action.set(1);
                }
                if (!Rs2Combat.inCombat() && action.get() == 1 && config.toggleCombat()) {
                    attack(config.enemyName(), config.attackType()); //......................................attack
                    if (config.toggleLootItems())
                        action.set(2);
                    Rs2Player.waitForAnimation();
                }
                if (action.get() == 2 && !Rs2Combat.inCombat() && config.toggleLootItems()) {
                    if (config.toggleCombat()) {
                        action.set(1);
                        sleep(1000, 1200);
                    }
                    loot(config.LootItems(), config.minValues(), centerX, centerY, config.LengthX(), config.HeightY()); //....loot
                }
                walkTowardsCenter(center, false, config);
                if (config.togglecanonRefil()) {
                    if (Rs2Cannon.repair())
                        return;
                    Rs2Cannon.refill();
                }
                if (config.eatfood()) {
                    int random = new Random().nextInt(41) + 20;
                    if (!Rs2Player.eatAt(random)) {
                        if (Rs2Inventory.getInventoryFood().isEmpty()) {
                            if (config.cookFood() == rasCollectBonesConfig.cookON.Fire) {
                                getFireMakingstuff(config);
                                if (!Rs2Inventory.hasItem("logs")) {
                                    List<String> treesThatGiveLogs = Arrays.asList(
                                            "Tree",
                                            "Evergreen",
                                            "Achey Tree",
                                            "Oak Tree",
                                            "Willow Tree",
                                            "Maple Tree",
                                            "Yew Tree",
                                            "Magic Tree"
                                    );
                                    for (String trees : treesThatGiveLogs) {
                                        GameObject tree = Rs2GameObject.findObject(trees, true, 50,false, center.toWorldPoint());
                                        if (tree != null) {
                                            Rs2GameObject.interact(tree.getId(), "cut");
                                            sleepUntil(() -> Rs2Inventory.hasItem("log"), 10000);
                                            break;
                                        }
                                    }
                                }
                                if(Rs2Inventory.hasItem("Raw")) {
                                    // if standing tile has nothing on it
                                    burnLog();
                                    cookfood();
                                }else huntfoodsource();
                            }
                        }
                    }
                }
                if (Microbot.getClient().getEnergy() > new Random().nextInt((4500 - 2500) + 1) + 2500) {
                    try {
                        Rs2Player.toggleRunEnergy(true);
                    }
                    catch(Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                if (config.bankItems() && Rs2Inventory.isFull()) {
                    doBanking(center, config.maxFood(), config);
                    walkTowardsCenter(center, true, config, true);
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }


    public void loot(String items, long minValue, int centerX, int centerY, int LengthX, int HeightY) {
        List<String> itemList = new ArrayList<>(Arrays.asList(items.split(",")));

        // Perform a single scan for ground items in the area
        RS2Item[] groundItems = Microbot.getClientThread().runOnClientThread(() ->
                Rs2GroundItem.getAll(7));

        // Filter the items based on the value and itemList
        List<RS2Item> filteredItems = new ArrayList<>();
        for (RS2Item rs2Item : groundItems) {
            if (Rs2Inventory.isFull()) break;

            long totalPrice = (long) Microbot.getClientThread().runOnClientThread(() ->
                    Microbot.getItemManager().getItemPrice(rs2Item.getItem().getId()) * rs2Item.getTileItem().getQuantity());

            if (totalPrice >= minValue && !itemList.contains(rs2Item.getItem().getName())) {
                itemList.add(rs2Item.getItem().getName());
                filteredItems.add(rs2Item);
            }
        }

        // Loot all items from the filtered list
        for (RS2Item rs2Item : filteredItems) {
            if (Rs2Inventory.isFull()) break;
            Rs2GroundItem.loot(rs2Item.getItem().getId());
            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
            sleep(112, 180);
        }

        // Process burying bones
        for (String Bones : BonesList) {
            while (Rs2Inventory.count(Bones) >= 1) {
                int BoneCount = Rs2Inventory.count(Bones);
                Rs2Inventory.interact(Bones, "Bury");
                sleepUntil(() -> BoneCount > Rs2Inventory.count(Bones), 2000);
            }

        }
    }

    public static boolean attack(String npcName, rasCollectBonesConfig.attackStyle attackType) {
        String[] enemyList = npcName.split(",");
        for (String enemy : enemyList) {
            NPC npc = Rs2Npc.getNpc(enemy);
            if (npc == null) continue;
            if (Rs2Combat.inCombat()) continue;
            if (attackType == rasCollectBonesConfig.attackStyle.Melee && !Rs2Npc.hasLineOfSight(npc)) continue;
            if (npc.isInteracting() && npc.getInteracting() != Microbot.getClient().getLocalPlayer() && !Rs2Player.isInMulti()) // npc array bnao 2nd npc vekho
                continue;
            afktimer = System.currentTimeMillis();
            return Rs2Npc.interact(npc, "attack");
        }
        return false;
    }

    private static void walkTowardsCenter(WorldArea center, boolean run, rasCollectBonesConfig config, boolean forceMove) {
        if (!center.contains(Microbot.getClient().getLocalPlayer().getWorldLocation()) && (config.walktoCenter() || forceMove) && !Rs2Inventory.isFull()) {
            if (Rs2Player.isAnimating() || Rs2Player.isInteracting())
                return;
            try {
                Rs2Player.toggleRunEnergy(run);
            }
            catch(Exception e) {
                System.out.println(e.getMessage());
            }
            WorldPoint randomBankPoint = center.toWorldPointList().get(net.runelite.client.plugins.microbot.util.math.Random.random(0, center.toWorldPointList().size() - 2));
            try {
                Rs2Walker.walkFastCanvas(randomBankPoint);
            } catch (Exception ex){
            Rs2Walker.walkTo(randomBankPoint);
        }
            Rs2Player.waitForWalking(60000);
        }
        //Microbot.getPluginManager().setPluginValue(rasCollectBonesConfig,,true);
    }
    private static void walkTowardsCenter(WorldArea center, boolean run, rasCollectBonesConfig config) {
        walkTowardsCenter(center, run, config, false);
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    private void doBanking(WorldArea center, int maxFoodItems, rasCollectBonesConfig config) {
        String eatAbleItems = "Anchovy,Anchovy Pizza,Banana,Banana Pizza,Bass,Big Bass,Bread,Burning Amulet,Cake,Cheese,Chocolate Bar,Chocolate Cake,Choc-Ice,Chompy,Dark Crab,Eel,Frog Spawn Gumbo,Garlic Bread,Gnome Spice,Gnomebowl,Gnomebowl (Half Made),Gnomecrunch,Gnomecrunch (Half Made),Gnomeglass,Gnomeglory,Gnomeglory (Half Made),Gnomelemon,Gnomelemon (Half Made),Gnomewings,Gnomewings (Half Made),Golden Tench,Hard Cheese,Half A Meat Pie,Half A Redberry Pie,Half A Rabbit Pie,Half A Summer Pie,Half A Wild Pie,Half Full Cake,Herring,Hotfish,Insect Repellent,Jangerberries,Jungle Spider Kebab,Karamjan Rum,Kebbit,King Worm,Lobster,Manta Ray,Meat Pizza,Mind Bomb,Monkey Nuts,Monkfish,Onion,Papaya,Peach,Peach Pizza,Pineapple,Pineapple Pizza,Plain Pizza,Poison Meat,Poisoned Cheese,Purple Sweets,Redberry Pie,Redberry Pizza,Roe,Salmon,Sardine,Seaweed,Shrimp,Spicy Crunchies,Stew,Strawberries,Strawberry Pie,Stuffed Cabbage,Swamp Snake,Tomato,Tuna,Ugthanki,Kebab,Watermelon,Wheat,Wilderness Sword 4";
        String unCooked = "raw";
        String fire = "tinderbox,pickaxe,log";
        List<String> edibleItemsList = new ArrayList<>(Arrays.asList(eatAbleItems.split(",")));
        try {
            Rs2Player.toggleRunEnergy(false);
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
        Rs2Bank.walkToBank();
        Rs2Player.waitForWalking();
        Rs2Bank.openBank();
        sleepUntil(()->!Rs2Bank.isOpen(),2000);
        Rs2Bank.depositAllExcept(fire + "," + unCooked + "," + eatAbleItems);
        sleep(200);
        for (String itemName : edibleItemsList) {
            int count = (int) Rs2Inventory.ItemQuantity(itemName);
            if (count > maxFoodItems) {
                Rs2Bank.depositX(itemName, count - maxFoodItems);
                sleep(200);
            } else if (count < maxFoodItems) {
                int amountToWithdraw = maxFoodItems - count;
                Rs2Bank.withdrawX(itemName, amountToWithdraw);
                sleep(200);
                break;
            }
        }
        sleep(200);
        Rs2Bank.closeBank();
        sleepUntil(()->Rs2Bank.isOpen(),2000);



    }

    private boolean burnLog() {
        WorldPoint fireSpot;
        while (Rs2Player.isStandingOnGameObject()) {
            fireSpot = fireSpot(1);
            Rs2Walker.walkFastCanvas(fireSpot);
        }
        if (!isFiremake()) {
            Rs2Inventory.use("tinderbox");
            sleep(net.runelite.client.plugins.microbot.util.math.Random.random(300, 600));
            Rs2Inventory.use("log");
            Rs2Player.waitForAnimation();
        }
        sleepUntil(() -> !isFiremake() && !Rs2Player.isStandingOnGameObject() && !Rs2Player.isStandingOnGroundItem(), 3500);
        return true;
    }

    private WorldPoint fireSpot(int distance) {
        List<WorldPoint> worldPoints = Rs2Tile.getWalkableTilesAroundPlayer(distance);

        for (WorldPoint walkablePoint : worldPoints) {
            if (Rs2GameObject.getGameObject(walkablePoint) == null) {
                return walkablePoint;
            }
        }

        fireSpot(distance + 1);

        return null;
    }

    private boolean isFiremake() {
        return Rs2Player.getAnimation() == AnimationID.FIREMAKING;
    }

    private void cookfood() {
        while (Rs2Inventory.hasItem("Raw")) {
            Rs2Inventory.use("raw");
            sleepUntil(() -> Rs2GameObject.interact("Fire", "use"));
            Rs2Player.waitForAnimation(1000);
            sleepUntil(() -> Rs2Widget.hasWidget("Choose"), 1000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            Rs2Player.waitForAnimation(1000);
        }
        sleep(350);
        Rs2Inventory.dropAll(Rs2Inventory.get("Burnt").getId());
        sleepUntil(()-> !Rs2Inventory.hasItem(Rs2Inventory.get("Burnt").getId()),5000);

    }

    private void getFireMakingstuff(rasCollectBonesConfig config) {
        if (!Rs2Inventory.hasItem("axe") && !Rs2Inventory.hasItem("Tinder")) {
            doBanking(center, config.maxFood(), config);
            Rs2Bank.openBank();
            sleepUntil(() -> !Rs2Bank.isOpen());
            Rs2Bank.withdrawX("axe", 1);
            sleepUntil(() -> Rs2Inventory.hasItem("axe"));
            Rs2Bank.withdrawX("Tinder", 1);
            sleepUntil(() -> Rs2Inventory.hasItem("Tinder"));
            boolean havelog = Rs2Bank.hasBankItem("log");
            if (havelog) {
                Rs2Bank.withdrawX("log", 3);
                sleepUntil(() -> Rs2Inventory.hasItem("log"));
            }
            Rs2Bank.closeBank();
            sleepUntil(() -> Rs2Bank.isOpen());
            walkTowardsCenter(center, true, config, true);
        }
    }
    private void huntfoodsource() {
        int totalfood = 0;
            long startTime = System.currentTimeMillis();
            while (Rs2Inventory.count("Raw") + Rs2Inventory.getInventoryFood().size() < 15) {

                if (Rs2GroundItem.loot("Raw", 15)) {
                    sleepUntilTrue(Rs2Inventory::waitForInventoryChanges,100,4000);
                    startTime = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - startTime >= 10000) {
                    break;
                }
                sleep(100); // Adding a small sleep to prevent tight loop
            }
            startTime = System.currentTimeMillis();
            while (Rs2Inventory.count("Raw") + Rs2Inventory.getInventoryFood().size() < 15) {
                outerloop:
                for (String npc : foodSourceList) {
                    if (Rs2Npc.attack(npc)) {
                        sleepUntil(() -> Rs2Combat.inCombat(), 50000);
                        sleep(1200);
                        Rs2GroundItem.loot("Raw", 8);
                        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 4000);
                        startTime = System.currentTimeMillis();
                        break outerloop;
                    }
                }
                startTime = System.currentTimeMillis();
                if (System.currentTimeMillis() - startTime >= 120000) {
                    break;
                }
                sleep(100);
            }
    }
}
