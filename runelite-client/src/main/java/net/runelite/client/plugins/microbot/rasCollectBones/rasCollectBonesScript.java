package net.runelite.client.plugins.microbot.rasCollectBones;

import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.firstTimeChecks.firstTimecheckScript;
import net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2Cannon;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.firstTimeChecks.firstTimecheckScript.switchToTab;
import static net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript.randomSleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;


public class rasCollectBonesScript extends Script {
    public static double version = 1.0;
    String Bones = "Big bones,Bones";
    List<String> BonesList = Arrays.asList(Bones.split(","));
    //String foodSource = "Cow,Giant rat,Chicken,Bear,Cave Bug,Cave Crawler,Cow Calf,Crawling Hand,Dwarf,Farmer,Flesh Crawler,Frog,Giant,Goblin,Guard,Hill Giant,Ice Giant,Jogre,Kalphite Worker,Lesser Demon,Lizardman,Minotaur,Monk,Moss Giant,Ogre,Pyrefiend,Rock Crab,Rockslug,Rogue,Sand Crab,Sea Snake Young,Shadow Warrior,Skeleton,Suqah,Troll,Wolf,Yeti,Yak";
    String foodSource = "Bear,Cow,Chicken,Giant rat";
    List<String> foodSourceList = Arrays.asList(foodSource.split(","));
    public int centerX = 0;
    public int centerY = 0;
    public WorldArea center = new WorldArea(1, 1, 1, 1, 0);
    public static long afktimer = System.currentTimeMillis();
    public static long stopTimer = 1;
    WorldPoint deathlocation = new WorldPoint(3176, 5726, 0);

    public boolean run(rasCollectBonesConfig config) {
        Microbot.enableAutoRunOn = false;
        AtomicInteger action = new AtomicInteger(1);
        centerY = 0;
        firstTimecheckScript dialog = new firstTimecheckScript();
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (rasMasterScriptScript.autoShutdown("ras range bone collector"))return;
            if (!Microbot.isLoggedIn()) return;
            if (!super.run()) {return;}
            try {
                if (stopTimer == 1) stopTimer = rasMasterScriptScript.autoStopTimer();
                if (Rs2Player.getWorldLocation().distanceTo(deathlocation) < 10) {dialog.deathdialog();}
                if (shutdownTimer()) return;
                randomSleep();
                if (centerY == 0 && Microbot.getClient().getGameState() == GameState.LOGGED_IN) {
                    centerX = Rs2Player.getWorldLocation().getX();
                    centerY = Rs2Player.getWorldLocation().getY();
                    center = new WorldArea(Rs2Player.getWorldLocation(), 6, 6);
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
                    //Rs2Player.waitForAnimation();
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
                    if (!Rs2Player.eatAt(random)){
                        double treshHold = (double) (Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS) * 100) / Microbot.getClient().getRealSkillLevel(Skill.HITPOINTS);
                        if (treshHold <= random && !Rs2Player.isFullHealth() && config.cookFood() != rasCollectBonesConfig.cookON.Monk) { // added monk codition for testing
                            sleepUntilTrue(()->!Rs2Combat.inCombat(),100,15000);
                        if (Rs2Inventory.getInventoryFood().isEmpty()) {
                            if (config.cookFood() == rasCollectBonesConfig.cookON.Fire) {
                                getFireMakingstuff(config);
                                if (!Rs2Inventory.hasItem("logs")) {
                                    List<String> treesThatGiveLogs = Arrays.asList(
                                            "Tree",
                                            "tree"
                                            //"Evergreen",
                                            //"Achey Tree",
                                            //"Oak Tree",
                                            //"Willow Tree",
                                            //"Maple Tree",
                                            //"Yew Tree",
                                            //"Magic Tree"
                                    );
                                    for (String trees : treesThatGiveLogs) {
                                        GameObject tree = Rs2GameObject.findObject(trees, false, 150,false, center.toWorldPoint());
                                        if (tree != null) {
                                            while (tree.getWorldLocation().distanceTo(Rs2Player.getWorldLocation())> 6){
                                                Rs2Walker.walkTo(tree.getWorldLocation(),1);
                                                Rs2Player.waitForAnimation(500);
                                                Rs2Player.waitForAnimation(500);
                                            }
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
                                }else {
                                    huntfoodsource();
                                    if(!Rs2Inventory.hasItem("Raw")){
                                        Microbot.getPluginManager().setPluginValue("collectbones", "cook food items", rasCollectBonesConfig.cookON.Cabbage);
                                    }
                                }
                            }else if (config.cookFood() == rasCollectBonesConfig.cookON.Monk) {
                                NPC headMonk = Rs2Npc.getNpc(2577);
                                if (headMonk != null) {
                                    WorldPoint headMonkLocation = new WorldPoint(3058,3485,0);
                                    Rs2Walker.walkTo(headMonkLocation,random(2,6));
                                }
                                else{
                                        healFromMonk();
                                }
                            }else if (config.cookFood() == rasCollectBonesConfig.cookON.Cabbage) {
                                eatCabbage();

                            }
                        }
                    }
                    }
                }
                if (Microbot.getClient().getEnergy() > new Random().nextInt((4500 - 2500) + 1) + 2500) {
                    try {
                        if(!Rs2Player.isRunEnabled())
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
                if (config.toggleCombat())
                    changeAttackStyle();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    private void eatCabbage() {
        WorldPoint cabbage1 = new WorldPoint(3051,3504,0); // monk
        WorldPoint cabbage2 = new WorldPoint(2950,3257,0); //
        WorldPoint cabbage3 = new WorldPoint(3051,3291,0); // under galador
        WorldPoint cabbage4 = new WorldPoint(3228,3386,0); // varock
        WorldPoint cabbage5 = new WorldPoint(3412,3180,0); // desert
        WorldPoint playerPosition = Rs2Player.getWorldLocation();

        WorldPoint[] cabbages = {cabbage1, cabbage2, cabbage3, cabbage4, cabbage5};

        int shortestDistance = Integer.MAX_VALUE;
        WorldPoint closestCabbage = null;

        for (WorldPoint cabbage : cabbages) {
            int distance = playerPosition.distanceTo(cabbage);
            if (distance < shortestDistance) {
                shortestDistance = distance;
                closestCabbage = cabbage;
            }
        }
        Rs2Walker.walkTo(closestCabbage,5);
        WorldPoint finalClosestCabbage = closestCabbage;
        sleepUntilTrue(()->Rs2Player.getWorldLocation().distanceTo(finalClosestCabbage) < 6,200,500000);
        while (Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS) != Microbot.getClient().getRealSkillLevel(Skill.HITPOINTS))
        {
            Rs2GameObject.interact("Cabbage","Pick");
            sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)) , 100, 5000);
            Rs2Inventory.interact("cabbage","Eat");
            sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)) , 100, 5000);
        }
        Rs2Walker.walkTo(playerPosition,5);
        sleepUntilTrue(()->Rs2Player.getWorldLocation().distanceTo(playerPosition) < 6,200,500000);
    }

    private static void healFromMonk() {
        System.out.print("heal from monk");
        NPC headMonk = Rs2Npc.getNpc(2577);
        while (Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS) != Microbot.getClient().getRealSkillLevel(Skill.HITPOINTS))
        {
            if (Rs2Player.isInteracting())
                Rs2Npc.interact(headMonk, "Talk-to");
            else
                Rs2Npc.interact(2579, "Talk-to");
            Global.sleepUntilTrue(() -> Rs2Dialogue.isInDialogue(), 100, 15000);
            sleep(400, 600);
            Rs2Dialogue.clickContinue();
            Global.sleepUntilTrue(() -> Rs2Widget.hasWidget("Select an option"), 100, 15000);
            sleep(300, 500);
            Rs2Keyboard.typeString("1");
            waitAndPressContinue();
        }
    }

    private boolean shutdownTimer() {
        if (System.currentTimeMillis() > stopTimer) {
            shutdown();
            return true;
        }
        return false;
    }


    public void loot(String items, long minValue, int centerX, int centerY, int LengthX, int HeightY) {
        Set<String> itemsToPickSet = new HashSet<>(Arrays.asList(items.split(",")));

        // Perform a single scan for ground items in the area
        RS2Item[] groundItems = Microbot.getClientThread().runOnClientThread(() ->
                Rs2GroundItem.getAll(7));

        // Filter the items based on the value and itemList, allowing partial name matching
        List<RS2Item> filteredItems = Arrays.stream(groundItems)
                .parallel()  // Parallelize the stream for faster performance with large arrays
                .filter(rs2Item -> {
                    if (Rs2Inventory.isFull()) return false;

                    String itemName = rs2Item.getItem().getName().toLowerCase();  // Convert name to lowercase for case-insensitive matching
                    long totalPrice = (long) Microbot.getClientThread().runOnClientThread(() ->
                            Microbot.getItemManager().getItemPrice(rs2Item.getItem().getId()) * rs2Item.getTileItem().getQuantity());

                    // Check if item matches any in the list, either exactly or partially
                    boolean matchesItemName = itemsToPickSet.stream().anyMatch(item -> itemName.contains(item.toLowerCase()));

                    return totalPrice >= minValue || matchesItemName;
                })
                .collect(Collectors.toList());

        // Loot all items from the filtered list
        for (RS2Item rs2Item : filteredItems) {
            if (Rs2Inventory.isFull()) break;

            int itemId = rs2Item.getItem().getId();
            int originalQuantity = rs2Item.getTileItem().getQuantity();
            WorldPoint itemPosition = rs2Item.getTile().getWorldLocation();

            // Check if the item is still on the same ground tile before trying to loot
            if (isItemStillOnGround(itemId, itemPosition, originalQuantity)) {
                Rs2GroundItem.take(itemId);

                // Wait for the inventory to change or stop early if the item disappears
                boolean success = sleepUntilTrue(() ->
                        !isItemStillOnGround(itemId, itemPosition, originalQuantity) || Rs2Inventory.waitForInventoryChanges(()-> sleep(100)), 100, 7000);

                // If the item was looted or disappeared, continue with the next item
                if (!success) {
                    continue;
                }

                sleep(112, 180); // Small delay between actions
            }
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

    private boolean isItemStillOnGround(int itemId, WorldPoint position, int originalQuantity) {
        RS2Item[] groundItems = Microbot.getClientThread().runOnClientThread(() ->
                Rs2GroundItem.getAll(7));

        // Check if the item with the given ID, position, and quantity still exists on the ground
        return Arrays.stream(groundItems).anyMatch(item ->
                item.getItem().getId() == itemId &&
                        item.getTile().getWorldLocation().equals(position) &&
                        item.getTileItem().getQuantity() == originalQuantity
        );
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
            if (Objects.requireNonNull(npc.getName()).contains("Monk")){ healFromMonk();}
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
            WorldPoint randomBankPoint = center.toWorldPointList().get(random(0, center.toWorldPointList().size() - 2));
            if (Rs2Player.getWorldLocation().distanceTo(randomBankPoint) > 10){
                Rs2Walker.walkTo(randomBankPoint);
            }
            else {
                Rs2Walker.walkFastCanvas(randomBankPoint);
            }
            sleepUntilTrue(()-> Rs2Player.getWorldLocation().distanceTo(randomBankPoint) < 8,100,380000);
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
        String eatAbleItems = "Anchovy,Anchovy Pizza,Banana,Banana Pizza,Bass,Big Bass,Bread,Burning Amulet,Cake,Cheese,Chocolate Bar,Chocolate Cake,Choc-Ice,Chompy,Dark Crab,Eel,Frog Spawn Gumbo,Garlic Bread,Gnome Spice,Gnomebowl,Gnomebowl (Half Made),Gnomecrunch,Gnomecrunch (Half Made),Gnomeglass,Gnomeglory,Gnomeglory (Half Made),Gnomelemon,Gnomelemon (Half Made),Gnomewings,Gnomewings (Half Made),Golden Tench,Hard Cheese,Half A Meat Pie,Half A Redberry Pie,Half A Rabbit Pie,Half A Summer Pie,Half A Wild Pie,Half Full Cake,Herring,Hotfish,Insect Repellent,Jangerberries,Jungle Spider Kebab,Karamjan Rum,Kebbit,King Worm,Lobster,Manta Ray,Meat Pizza,Mind Bomb,Monkey Nuts,Monkfish,Onion,Papaya,Peach,Peach Pizza,Pineapple,Pineapple Pizza,Plain Pizza,Poison Meat,Poisoned Cheese,Purple Sweets,Redberry Pie,Redberry Pizza,Roe,Salmon,Sardine,Seaweed,Shrimp,Spicy Crunchies,Stew,Strawberries,Strawberry Pie,Stuffed Cabbage,Swamp Snake,Tomato,Tuna,Ugthanki,Kebab,Watermelon,Wilderness Sword 4";
        String unCooked = "Raw";
        String fire = "tinderbox,bronze axe,iron axe,steel axe,black axe,mithril axe,adamant axe,rune axe,dragon axe,crystal axe,infernal axe,third age axe,logs";
        List<String> edibleItemsList = new ArrayList<>(Arrays.asList(eatAbleItems.split(",")));
        try {
            Rs2Player.toggleRunEnergy(false);
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
        Rs2Bank.walkToBank();
        Rs2Player.waitForWalking();
        sleepUntil(()->Rs2Bank.isNearBank(6),282000);
        Rs2Bank.openBank();
        sleepUntil(()->!Rs2Bank.isOpen(),2000);
        String[] itemsToKeep = (fire + "," + unCooked + "," + eatAbleItems).split(",");
        Rs2Bank.depositAllExcept(itemsToKeep);
        sleep(200);
        int count = 0;
        for (String itemName : edibleItemsList) {
            count = (int) Rs2Inventory.ItemQuantity(itemName) + count;
            if (count > maxFoodItems) {
                Rs2Bank.depositX(itemName, count - maxFoodItems);
                sleep(200);
            } else if (count <= maxFoodItems) {
                int amountToWithdraw = maxFoodItems - count;
                Rs2Bank.withdrawX(itemName, amountToWithdraw);
                sleep(200);
                break;
            }
        }
        for (int i = 0; i < 3; i++) {
            if (count < maxFoodItems && Rs2Inventory.count() < 20) {
                Rs2Item item = Rs2Bank.findBankItem("Raw");
                if ((!item.getName().contains("Raw salmon") && !item.getName().contains("Raw trout")) ||
                        (Rs2Player.getRealSkillLevel(Skill.COOKING) >= 25 && item.getName().contains("Raw salmon")) ||
                        (Rs2Player.getRealSkillLevel(Skill.COOKING) >= 15 && item.getName().contains("Raw trout"))) {
                    Rs2Bank.withdrawX(item.getName(), (20 - Rs2Inventory.count()));
                }
            }
        }

        sleep(200);
        Rs2Bank.closeBank();
        sleepUntil(()->!Rs2Bank.isOpen(),2000);



    }

    private boolean burnLog() {
        WorldPoint fireSpot;
        while (Rs2Player.isStandingOnGameObject()) {
            fireSpot = fireSpot(1);
            Rs2Walker.walkFastCanvas(fireSpot);
        }
        if (!isFiremake()) {
            Rs2Inventory.use("tinderbox");
            sleep(random(300, 600));
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
        while (Rs2Inventory.hasItem("Raw") && Rs2GameObject.findObjectById(26185) != null) {
            Rs2Inventory.use("raw");
            sleepUntil(() -> Rs2GameObject.interact(26185, "use"));
            sleepUntil(() -> Rs2Widget.hasWidget("Choose"), 1000);
            sleep(200,300);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            waitForAnimationStop();
        }
        sleep(350);
        if (Rs2Inventory.hasItem("Burnt")) {
            Rs2Inventory.dropAll(Rs2Inventory.get("Burnt").getId());
            sleepUntil(() -> !Rs2Inventory.hasItem("Burnt"), 5000);
        }

    }

    private void getFireMakingstuff(rasCollectBonesConfig config) {
        if (!Rs2Inventory.hasItem("axe") || !Rs2Inventory.hasItem("Tinder")) {
            doBanking(center, config.maxFood(), config);
            Rs2Bank.openBank();
            sleepUntil(() -> !Rs2Bank.isOpen());
            Rs2Bank.withdrawX("Bronze axe", 1); // it can pick pickaxe, check by id instead
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
                    sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)) ,100,4000);
                    startTime = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - startTime >= 10000) {
                    break;
                }
                sleep(100); // Adding a small sleep to prevent tight loop
            }
            startTime = System.currentTimeMillis();
            while (Rs2Inventory.count("Raw") + Rs2Inventory.getInventoryFood().size() < 15 && !Rs2Inventory.isFull()) {
                outerloop:
                for (String npc : foodSourceList) {
                    if (Rs2Npc.attack(npc)) {
                        sleepUntil(() -> Rs2Combat.inCombat(), 5000);
                        sleepUntilTrue(() -> !Rs2Combat.inCombat(), 500,50000);
                        sleep(1200);
                        RS2Item[] groundItems = Microbot.getClientThread().runOnClientThread(() ->
                                Rs2GroundItem.getAll(7));

                        for (RS2Item item : groundItems) {
                            if (item.getItem().getName().toLowerCase().contains("raw")) {
                                while (Rs2GroundItem.take(item.getItem().getId()) && !Rs2Inventory.isFull()){
                                    sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)) , 100, 4000);
                                    if (Rs2Inventory.isFull())
                                        break; // Exit the loop once the item is successfully looted
                                }
                            }
                        }
                        startTime = System.currentTimeMillis();
                        break outerloop;
                    }
                }
                //startTime = System.currentTimeMillis();
                if (System.currentTimeMillis() - startTime >= 120000) {
                    break;
                }
                sleep(100);
            }
    }
    @Override
    public void shutdown() {
        stopTimer = 1;
        String pluginName = "ras range bone collector";
        //rasMasterScriptScript masterControl = new rasMasterScriptScript();
        rasMasterScriptScript.stopPlugin(pluginName);
        super.shutdown();
    }
    public static void waitAndPressContinue(){
        int sleep = random(1300,1600);
        long endTime = System.currentTimeMillis() + sleep;
        while (System.currentTimeMillis() < endTime) {
            if (Rs2Widget.hasWidget("Click here to continue")) {
                sleep(180,380);
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                if (random(0,2)==0) {
                    sleep(80,180);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                }
                endTime = System.currentTimeMillis() + sleep;
            }
            if (Rs2Widget.hasWidget("Please wait")){
                endTime = System.currentTimeMillis() + sleep;
            }
        }
    }
    public static void changeAttackStyle(){
        if (random(0, 50) == 5 ) {
            switchToTab("combat");
            sleep(800, 1400);
            int attackStyleIs = random(1, 5);
            if (attackStyleIs == 1) {
                Rs2Combat.setAttackStyle(WidgetInfo.COMBAT_STYLE_ONE);
            } else if (attackStyleIs == 2){
                Rs2Combat.setAttackStyle(WidgetInfo.COMBAT_STYLE_TWO);
            } else if (attackStyleIs == 3){
                Rs2Combat.setAttackStyle(WidgetInfo.COMBAT_STYLE_THREE);
            } else if (attackStyleIs == 4) {
                Rs2Combat.setAttackStyle(WidgetInfo.COMBAT_STYLE_FOUR);
            }
        }
    }
    public void waitForAnimationStop() {
        long lastAnimationStopTime = System.currentTimeMillis();
        while (true) {
            sleep(100);
            if (Rs2Player.isAnimating()) {
                lastAnimationStopTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - lastAnimationStopTime >= 1200) {
                break;
            }
        }
    }
}
