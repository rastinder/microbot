package net.runelite.client.plugins.microbot.rasMagicTrain;

import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.geHandler.geHandlerScript;
import net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript;
import net.runelite.client.plugins.microbot.ras_highalc.ras.Ras_highalcConfig;
import net.runelite.client.plugins.microbot.ras_highalc.ras.Ras_highalcScript;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grandexchange.GrandExchangeSlots;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.shop.Rs2Shop;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;
import net.runelite.api.Client;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.raschoclatebar.raschoclatebarScript.waitForAnimationStop;
import static net.runelite.client.plugins.microbot.util.Global.*;
import static net.runelite.client.plugins.microbot.util.math.Random.random;


public class rasMagicTrainScript extends Script {
    public static long stopTimer =1;
    public static double version = 1.0;
    public boolean bankcheck = false;
    public int Law_rune_price = 0;
    WorldPoint grabLocation = new WorldPoint(3191, 9825, 0);
    WorldPoint bankLocation = new WorldPoint(3185, 3436, 0);

    public int FIRE_STRIKE_LEVEL = 13;
    public int wind_strike = 334;
    public int fire_strike = 1428;
    public  String CURRENT_STRIKE = null;
    public boolean attackLocationFound = false;
    WorldPointFunctionMap.WorldPointEntry entry;
    public Map<String, WorldPointFunctionMap.WorldPointEntry> worldPointMap = new HashMap<>();

    public boolean run(rasMagicTrainConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                rasMasterScriptScript.autoShutdown("ras magic train");
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (stopTimer == 1)
                    stopTimer = random(1800000,2760000) + System.currentTimeMillis();
                long startTime = System.currentTimeMillis();
                System.out.println("magic level " + Rs2Player.getRealSkillLevel(Skill.MAGIC));
                if(!Rs2Player.isInteracting()) {
                    if (Rs2Player.getRealSkillLevel(Skill.MAGIC) > 54) {
                        highAlch();
                    } else if ((Rs2Player.getRealSkillLevel(Skill.MAGIC)) > 42 && Rs2Player.getRealSkillLevel(Skill.SMITHING) > 29) {
                        superHeat();
                    } else if (Rs2Player.getRealSkillLevel(Skill.MAGIC) > 32 && Law_rune_price < 140) {
                        if (Law_rune_price == 0){
                            Law_rune_price = (int) Microbot.getClientThread().runOnClientThread(() ->
                                    Microbot.getItemManager().getItemPrice(563));
                        }
                        if (Law_rune_price < 140) {
                            if (!Rs2Inventory.hasItem("Law rune")) {
                                sellalreadygrabbediyemsandbuylawrunes();
                            }
                            if (!Rs2Equipment.hasEquippedContains("Staff of air"))
                                fetchFromBank(MagicAction.TELEKINETIC_GRAB);
                            teleGrab();
                        }
                    } else if (Rs2Player.getRealSkillLevel(Skill.MAGIC) > 0) {
                        if (!bankcheck){
                            bankcheck = true;
                            bankCheck();
                        }
                        attack();
                    }
                }
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);
                if (stopTimer < System.currentTimeMillis()){
                    //shutdown();
                }

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        String pluginName = "ras magic train";
        rasMasterScriptScript masterControl = new rasMasterScriptScript();
        rasMasterScriptScript.stopPlugin(pluginName);
        do{sleep(2000);}
        while (masterControl.isPlugEnabled(pluginName));
        super.shutdown();
    }
    public static boolean hasOnlyRunesOrStaff() {
        List<Rs2Item> items = Rs2Inventory.items();

        for (Rs2Item item : items) {
            String itemName = item.getName().toLowerCase();
            if (!itemName.contains("rune") && !itemName.contains("staff")) {
                return false; // If any item is not a rune or staff, return false
            }
        }
        return true; // All items are either runes or a staff
    }
    public void bankCheck(){
        if (hasOnlyRunesOrStaff())
            return;
        if (!Rs2Inventory.isEmpty()){
            Rs2Bank.walkToBank();
            waitForAnimationStop();
            sleepUntilTrue(()-> Rs2Bank.isNearBank(8),100,120000);
            Rs2Bank.openBank();
            Rs2Bank.depositAll();
            sleepUntilTrue(() -> !Rs2Inventory.isEmpty(), 100, 2000);

        }
    }
    public boolean autoCast(String spellname){
        try {
            Rs2Tab.switchToCombatOptionsTab();
            sleep(800);
            Rs2Widget.clickWidget(593, 26);
            sleep(800);
            Widget[] spell = Rs2Widget.getWidget(201, 1).getDynamicChildren();
            sleep(800);
            if (spellname.contains("Wind")) {
                Microbot.click(spell[1].getBounds());
                return true;
            } else {
                Microbot.click(spell[4].getBounds());
                return true;
            }
        }catch (Exception e){
            System.out.println("problem in autoCast");
            Microbot.showMessage("problem in autoCast");
            return false;
        }
    }
    public void hopWorld() {
        int world = Login.getRandomWorld(Rs2Player.isMember(), null);
        boolean isHopped = Microbot.hopToWorld(world);
        if (!isHopped) return;
        boolean result = sleepUntil(() -> Rs2Widget.findWidget("Switch World") != null);
        if (result) {
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleepUntil(() -> Microbot.getClient().getGameState() == GameState.HOPPING, 5000);
            sleepUntil(() -> Microbot.getClient().getGameState() == GameState.LOGGED_IN, 5000);
            sleep(1000, 1500);
        }
    }

    public void superHeat() {
        String rune = "Nature rune";
        String ironore = "Iron ore";
        String tinore = "Coal";
        if (Rs2Inventory.hasItem(rune) && Rs2Inventory.hasItem(ironore) && Rs2Inventory.hasItem(tinore)) {
            System.out.println("Casting Superheat Item on Iron ore and Tin ore.");
            Rs2Magic.cast(MagicAction.SUPERHEAT_ITEM);
            Rs2Inventory.interact(ironore);
            sleep(2800 - 3200); // Wait for the casting animation to complete
        } else if (!Rs2Inventory.hasItem(ironore) && Rs2Inventory.hasItem(rune)) {
            // Walk to bank location and deposit bars
            Rs2Bank.openBank();
            sleepUntil(() -> Rs2Bank.openBank(), 5000);
            Rs2Bank.depositAllExcept("Nature rune");
            sleepUntil(() -> Rs2Inventory.fullSlotCount() == 1, 5000);

            // Withdraw iron and tin ores in a 1:2 ratio to fill 27 slots
            int ironOreToWithdraw = 9; // 1/3 of 27 slots
            int tinOreToWithdraw = 18; // 2/3 of 27 slots

            Rs2Bank.withdrawX(ironore, ironOreToWithdraw);
            sleep(500);
            Rs2Bank.withdrawX(tinore, tinOreToWithdraw);
            sleep(500);
            if (Rs2Inventory.hasItem(ironore) && Rs2Inventory.hasItem(tinore))
                Rs2Bank.closeBank();
            sleep(500);
        } else {
            Rs2Bank.openBank();
            sleepUntilTrue(Rs2Bank::openBank, 100, 5000);
            sleep(500);
            Rs2Bank.setWithdrawAsNote();
            sleep(500);
            Rs2Bank.withdrawAll("Steel bar");
            sleep(500);
            Rs2Bank.closeBank();
            sleep(500);
            System.out.println("Missing Nature runes, Iron ore, or Tin ore.");
            int iron = Microbot.getItemManager().search("Iron ore").get(0).getPrice();
            int coal = Microbot.getItemManager().search("Coal").get(0).getPrice();
            RasClass.grandExchangeOperations(new Random().nextInt(50000) + 300000, 106, iron, coal, 1, 1, 2);
        }

    }

    public void teleGrab() {
        if (Rs2Inventory.hasItem("law Rune") && !Rs2Inventory.isFull()) {
            System.out.println("distance  " + grabLocation.distanceTo(Rs2Player.getWorldLocation()));
            if (grabLocation.distanceTo(Rs2Player.getWorldLocation()) == 0) {
                RS2Item[] groundItems = Microbot.getClientThread().runOnClientThread(() -> Rs2GroundItem.getAll(7));

                // Shuffle the array using Fisher-Yates algorithm
                Random random = new Random();
                for (int i = groundItems.length - 1; i > 0; i--) {
                    int index = random.nextInt(i + 1);
                    RS2Item temp = groundItems[index];
                    groundItems[index] = groundItems[i];
                    groundItems[i] = temp;
                }
                for (RS2Item item : groundItems) {
                    int priceItem = (int) Microbot.getClientThread().runOnClientThread(() ->
                            Microbot.getItemManager().getItemPrice(item.getItem().getId()));
                    if (priceItem < Law_rune_price)
                        continue;
                    System.out.println("found   " + item.getItem().getName());
                    if ((Rs2GroundItem.hasLineOfSight(item.getTile()) && !item.getItem().getName().contains("Coins")) || item.getItem().getName().contains("Gold ore")) {
                        System.out.println("grab   " + item.getItem().getName());
                        Rs2Magic.cast(MagicAction.TELEKINETIC_GRAB);
                        int count = Rs2Inventory.fullSlotCount();
                        sleep(350, 380);
                        Rs2GroundItem.interact(item);
                        //sleep(200);
                        sleepUntil(() -> Rs2Inventory.fullSlotCount() > count, 5000);
                        sleep(180, 340);
                        if (Rs2Inventory.isFull()) break;
                    }
                }
                if (!Rs2Inventory.isFull())
                    hopWorld();
            } else if (grabLocation.distanceTo(Rs2Player.getWorldLocation()) > 9)
                Rs2Walker.walkTo(grabLocation);
            else Rs2Walker.walkFastCanvas(grabLocation);
            Rs2Player.waitForAnimation(500);

        } else if (Rs2Inventory.isFull()) {
            Rs2Walker.walkTo(bankLocation);
            waitForAnimationStopnDoor();
            Rs2Bank.openBank();
            sleepUntil(() -> Rs2Bank.openBank(), 5000);
            Rs2Bank.depositAllExcept("Law rune");
            sleepUntil(() -> Rs2Inventory.fullSlotCount() == 1, 5000);
        }
    }
    public boolean sellalreadygrabbediyemsandbuylawrunes(){
        WorldPoint geLocation = new WorldPoint(3164, 3485, 0); // Coordinates for GE
        while(Rs2Player.getWorldLocation().distanceTo(geLocation) > 10) {
            Rs2Walker.walkTo(geLocation);
            Rs2Player.waitForWalking();
        }
        List<String> ITEMS_TO_SELL = Arrays.asList("Gold bar", "Gold ore", "Gold necklace");
        if (Rs2Bank.openBank()) {
            Rs2Bank.depositAll();
            sleepUntilTrue(Rs2Inventory::isEmpty,100,1000);
            Rs2Bank.setWithdrawAsNote();
            for (String item : ITEMS_TO_SELL) {
                sleep(120,280);
                Rs2Bank.withdrawAll(item); // Withdraw all items as noted
            }
            Rs2Bank.closeBank();
            if (!Rs2Inventory.isEmpty()) {
                for (String item : ITEMS_TO_SELL) {
                    int itemId = (int) Microbot.getClientThread().runOnClientThread(() ->
                            Microbot.getItemManager().search(item).get(0).getId());
                    int sellPrice = (int) Microbot.getClientThread().runOnClientThread(() ->
                            Microbot.getItemManager().getItemPrice(itemId));

                    Rs2GrandExchange.sellItem(item, (int) Rs2Inventory.ItemQuantity(itemId), sellPrice); // Sell all items
                    retrySell(item, sellPrice);
                }
            }
            geHandlerScript.goBuyAndReturn(new int[]{100},15, "law rune");
            return true;
        }
        return false;
    }
    public boolean retrySell(String item, int sellPrice){
        while (true){
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 120000) { // Wait for 120 seconds
            if (Rs2GrandExchange.hasSoldOffer()) break;
            sleep(1000);
        }
        if (!Rs2GrandExchange.hasSoldOffer()) {
            abortAllActiveOffers();
            Rs2GrandExchange.collectToInventory();
            Rs2GrandExchange.sellItem(item, (int)(sellPrice * 0.95), 0); // Retry with decreased price
        }
        else {
            Rs2GrandExchange.collectToInventory();
            return true;
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
    public void waitForAnimationStopnDoor() {
        long lastAnimationStopTime = System.currentTimeMillis();
        while (true) {
            sleep(100);
            if (Rs2Player.isAnimating()) {
                lastAnimationStopTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - lastAnimationStopTime >= 2000) {
                break;
            }
            if (Rs2GameObject.interact(11775)) {
                sleep(800);
                break;
            }
        }
    }
    public void waitForAnimationStop() {
        long lastAnimationStopTime = System.currentTimeMillis();
        while (true) {
            sleep(100);
            if (Rs2Player.isAnimating() || Rs2Player.isWalking()) {
                lastAnimationStopTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - lastAnimationStopTime >= 3000) {
                break;
            }
            //if (Rs2Widget.hasWidget("Select an option"))
            //    break;
        }
    }

    public void highAlch() {
        Microbot.getPluginManager().setPluginValue("general", "Autobuy", true);
        Ras_highalcConfig confi = new Ras_highalcConfig() {
            @Override
            public boolean autoBuy() {
                return Ras_highalcConfig.super.autoBuy();
            }

            @Override
            public boolean naturalRuneCheck() {
                return Ras_highalcConfig.super.naturalRuneCheck();
            }

            @Override
            public boolean highAlch() {
                return Ras_highalcConfig.super.highAlch();
            }

            @Override
            public int waitTime() {
                return Ras_highalcConfig.super.waitTime();
            }

            @Override
            public int naturalRunePrice() {
                return Ras_highalcConfig.super.naturalRunePrice();
            }

            @Override
            public String itemList() {
                return Ras_highalcConfig.super.itemList();
            }
        };
        Ras_highalcScript alchscript = new Ras_highalcScript();
        alchscript.run(confi);

        //confi.run(confi);
    }


    public boolean attack() {
        int currentMagicLevel = Microbot.getClient().getRealSkillLevel(Skill.MAGIC);
        MagicAction spellToCast = currentMagicLevel >= FIRE_STRIKE_LEVEL ? MagicAction.FIRE_STRIKE : MagicAction.WIND_STRIKE;

        if (!checkEquipmentAndRunes(spellToCast)) {
            if (!fetchFromBank(spellToCast)) {
                if (!buyFromShop(spellToCast)) {
                    if (!buyFromGe(spellToCast)) {
                        moneyMaking(); // more like get free
                        return false;
                    }
                }
            }
        }

        if (CURRENT_STRIKE == null || !CURRENT_STRIKE.equals(spellToCast.getName())) {
            if (autoCast(spellToCast.getName()))
                CURRENT_STRIKE = spellToCast.getName();
        }
        if (!attackLocationFound) {
            attackLocationFound = true;
            // Add entries to the map with their associated functions
            worldPointMap.put("demonjailed", new WorldPointFunctionMap.WorldPointEntry(new WorldPoint(3110, 3159, 2), WorldPointFunctionMap::interrogateDemon, spellToCast));
            worldPointMap.put("knights", new WorldPointFunctionMap.WorldPointEntry(new WorldPoint(3017, 3509, 0), WorldPointFunctionMap::killKnights, spellToCast));
            worldPointMap.put("Zamorak", new WorldPointFunctionMap.WorldPointEntry(new WorldPoint(2943, 3517, 0), WorldPointFunctionMap::worshipZamorak, spellToCast));
            worldPointMap.put("wildernessLineSkelton", new WorldPointFunctionMap.WorldPointEntry(new WorldPoint(3116, 3520, 0), WorldPointFunctionMap::guardSkeleton, spellToCast));
            worldPointMap.put("varrockSewer", new WorldPointFunctionMap.WorldPointEntry(new WorldPoint(3228, 9900, 0), WorldPointFunctionMap::exploreSewer, spellToCast));
            worldPointMap.put("Zamorakjailed", new WorldPointFunctionMap.WorldPointEntry(new WorldPoint(3214, 3476, 0), WorldPointFunctionMap::freeZamorak, spellToCast));
            worldPointMap.put("faladorCow", new WorldPointFunctionMap.WorldPointEntry(new WorldPoint(3033, 3314, 0), WorldPointFunctionMap::milkCow, spellToCast));
            worldPointMap.put("guildCow", new WorldPointFunctionMap.WorldPointEntry(new WorldPoint(2922, 3292, 0), WorldPointFunctionMap::protectCow, spellToCast));
            worldPointMap.put("CowNearMill", new WorldPointFunctionMap.WorldPointEntry(new WorldPoint(3201, 3302, 0), WorldPointFunctionMap::feedCow, spellToCast));

            // Select a random entry
            String[] keys = worldPointMap.keySet().toArray(new String[0]);
            Random random = new Random();
            String randomKey = keys[random.nextInt(keys.length)];
            entry = worldPointMap.get(randomKey);
            System.out.println("Selected: " + randomKey);
        }
        try {
            if (entry != null) {
                entry.getAction().execute(entry.getWorldPoint(), spellToCast);
                //waitForAnimationStop();
            }
        }
        catch (Exception x)
        {
            System.out.println("problem in function: " +x);
            //attackLocationFound = false; // comment out for testing otherwise leave it
        }
        return true;
    }

    private boolean checkEquipmentAndRunes(@NotNull MagicAction spell) {
        boolean hasStaff = false;
        boolean hasRunes = false;

        switch (spell) {
            case FIRE_STRIKE:
                if (Rs2Inventory.hasItem(ItemID.STAFF_OF_FIRE)) {
                    Rs2Inventory.interact(ItemID.STAFF_OF_FIRE, "Wield");
                }
                sleep(110, 280);
                hasStaff = Rs2Equipment.hasEquipped(ItemID.STAFF_OF_FIRE);
                hasRunes = Rs2Inventory.hasItemAmount(ItemID.AIR_RUNE, 2) && Rs2Inventory.hasItemAmount(ItemID.MIND_RUNE, 1);
                break;
            case WIND_STRIKE:
                if (Rs2Inventory.hasItem(ItemID.STAFF_OF_AIR)) {
                    Rs2Inventory.interact(ItemID.STAFF_OF_AIR, "Wield");
                }
                sleep(110, 280);
                hasStaff = Rs2Equipment.hasEquipped(ItemID.STAFF_OF_AIR);
                hasRunes = Rs2Inventory.hasItemAmount(ItemID.MIND_RUNE, 1);
                break;
            case TELEKINETIC_GRAB:
                if (Rs2Inventory.hasItem(ItemID.STAFF_OF_AIR)) {
                    Rs2Inventory.interact(ItemID.STAFF_OF_AIR, "Wield");
                }
                sleep(110, 280);
                hasStaff = Rs2Equipment.hasEquipped(ItemID.STAFF_OF_AIR);
                hasRunes = Rs2Inventory.hasItemAmount(ItemID.LAW_RUNE, 1);
                break;
            default:
                return false;

        }

        return hasStaff && hasRunes;
    }

    private boolean fetchFromBank(MagicAction spell) {
        Rs2Bank.walkToBank();
        boolean Wield = false;
        sleepUntilTrue(()->Rs2Bank.isNearBank(11),100,120000);
        if(!Rs2Bank.isNearBank(11))
            return false;
        Rs2Bank.openBank();
            sleep(300,600);
            String staffName = "";
            int primaryRuneItemId = 0, secondaryRuneItemId = 0;
            int primaryRuneQuantity = 100, secondaryRuneQuantity = 0;

            switch (spell) {
                case WIND_STRIKE:
                    staffName = "Staff of air";
                    primaryRuneItemId = ItemID.MIND_RUNE;
                    break;
                case FIRE_STRIKE:
                    staffName = "Staff of fire";
                    primaryRuneItemId = ItemID.MIND_RUNE;
                    secondaryRuneItemId = ItemID.AIR_RUNE;
                    secondaryRuneQuantity = 100;
                    break;
                case TELEKINETIC_GRAB:
                    staffName = "Staff of air";
                    primaryRuneItemId = ItemID.LAW_RUNE;
                    break;
            }

            if (staffName.isEmpty() || primaryRuneItemId == 0) return false; // Ensure valid spellId

            if (!Rs2Equipment.hasEquippedContains(staffName)) {
                if (Rs2Bank.hasItem(staffName)) {
                    Rs2Bank.withdrawX(staffName, 1);
                    sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                    sleep(110, 280);
                    if (Rs2Inventory.hasItem(staffName)) {
                        Wield = true;
                    }
                } else if (Rs2Bank.count("Coins", true) > 1500) {
                    Rs2Bank.withdrawX(995, 1500);
                } else {
                    shutdown(); // or get some from magic tutor
                }
            }

            //if (!Rs2Inventory.contains(primaryRuneItemId, primaryRuneQuantity)) {
                Rs2Bank.withdrawAll(primaryRuneItemId);
            //}

            if (secondaryRuneItemId != 0 ){ //&& !Rs2Inventory.contains(secondaryRuneItemId, secondaryRuneQuantity)) {
                Rs2Bank.withdrawAll(secondaryRuneItemId);
            }
            if (Wield)
                Rs2Inventory.interact(staffName, "Wield");
            Rs2Bank.closeBank();
            return checkEquipmentAndRunes(spell);
    }

    private boolean buyFromShop(MagicAction spell) {
        String staffName = null;
        switch (spell) {
            case FIRE_STRIKE:
                staffName = "Staff of fire";
                break;
            case WIND_STRIKE:
                staffName = "Staff of air";
                break;
        }
        if(Rs2Equipment.hasEquippedContains(staffName))
            return checkEquipmentAndRunes(spell);
        WorldPoint staffshop= new WorldPoint(3204,3432,0);
        while (Rs2Player.getWorldLocation().distanceTo(staffshop) > 2) {
            Rs2Walker.walkTo(staffshop, 1);
            waitForAnimationStop();
        }
        Rs2Shop.openShop("Zaff");
        sleepUntil(()-> Rs2Widget.hasWidget("Staff of fire"),4000);
        sleep(110,280);
        if (Rs2Shop.isOpen()) { // check if quantity is two if not then hop world
            String finalStaffName = staffName;
            while (!Rs2Shop.hasMinimumStock(staffName,2)) {
                Rs2Shop.closeShop();
                hopWorld();
                Rs2Shop.openShop("Zaff");
                sleepUntil(() -> Rs2Widget.hasWidget(finalStaffName), 4000);
            }
            Rs2Shop.buyItem(staffName,"1");
            sleepUntil(()->Rs2Inventory.hasItem(finalStaffName), 5000);
            Rs2Shop.closeShop();
            return checkEquipmentAndRunes(spell);
        }
        return false;
    }
    private boolean buyFromGe(MagicAction spell) {
        int currentLevel = Microbot.getClient().getRealSkillLevel(Skill.MAGIC);
        int targetLevel1 = 13;
        int targetLevel2 = 33;
        if (Law_rune_price > 140)
            targetLevel2 =43;
        if (currentLevel > 42 && Rs2Player.getRealSkillLevel(Skill.SMITHING) < 29)
            targetLevel2 =55;

        // Calculate the runes needed for each spell
        int[] runesNeeded;
        if (spell == MagicAction.FIRE_STRIKE) {
            runesNeeded = calculateRunesNeeded(currentLevel, targetLevel1, targetLevel2, spell);
        } else if (spell == MagicAction.WIND_STRIKE) {
            runesNeeded = calculateRunesNeeded(currentLevel, targetLevel1, targetLevel2, spell);
        } else {
            return false; // Unsupported spell
        }

        String[] itemsToBuy;
        if (spell == MagicAction.FIRE_STRIKE) {
            itemsToBuy = new String[]{"Mind rune", "Air rune"};
        } else if (spell == MagicAction.WIND_STRIKE) {
            itemsToBuy = new String[]{"Mind rune"};
        } else {
            return false; // Unsupported spell
        }

        return geHandlerScript.goBuyAndReturn(runesNeeded,5, itemsToBuy);
        //return true;
    }

    private int[] calculateRunesNeeded(int currentLevel, int targetLevel1, int targetLevel2, MagicAction spell) {
        // Calculate the number of casts needed to reach the target levels
        int castsToTarget1 = calculateCastsNeeded(currentLevel, targetLevel1, spell);
        int castsToTarget2 = calculateCastsNeeded(currentLevel, targetLevel2, spell);

        // Choose the target level based on the current level
        int targetLevel;
        if (currentLevel < targetLevel1) {
            targetLevel = targetLevel1;
        } else {
            targetLevel = targetLevel2;
        }
        int casts = calculateCastsNeeded(currentLevel, targetLevel, spell);

        // Calculate runes needed based on the spell and number of casts
        int mindRunes = 0, airRunes = 0, lawRunes = 0, natureRunes = 0, fireRunes = 0;
        if (spell == MagicAction.FIRE_STRIKE) {
            mindRunes = casts;
            airRunes = casts * 2; // Assuming each cast requires 2 air runes
        } else if (spell == MagicAction.WIND_STRIKE) {
            mindRunes = casts;
        } else if (spell == MagicAction.TELEKINETIC_GRAB) {
            lawRunes = casts;
        } else if (spell == MagicAction.SUPERHEAT_ITEM) {
            natureRunes = casts;
        }
        // Check current runes and subtract from needed runes
        int currentMindRunes = (int) Rs2Inventory.ItemQuantity("Mind rune");
        int currentAirRunes = (int) Rs2Inventory.ItemQuantity("Air rune");
        int currentLawRunes = (int) Rs2Inventory.ItemQuantity("Law rune");
        int currentNatureRunes = (int) Rs2Inventory.ItemQuantity("Nature rune");
        int currentFireRunes = (int) Rs2Inventory.ItemQuantity("Fire rune");

        mindRunes = Math.max(0, mindRunes - currentMindRunes);
        airRunes = Math.max(0, airRunes - currentAirRunes);
        lawRunes = Math.max(0, lawRunes - currentLawRunes);
        natureRunes = Math.max(0, natureRunes - currentNatureRunes);
        fireRunes = Math.max(0, fireRunes - currentFireRunes);

        return new int[]{mindRunes, airRunes, lawRunes, natureRunes, fireRunes};
    }

    private int calculateCastsNeeded(int currentLevel, int targetLevel, MagicAction spell) {
        double experiencePerCast = getExperiencePerCast(spell);
        int currentExp = getExperienceForLevel(currentLevel);
        int targetExp = getExperienceForLevel(targetLevel);
        int expNeeded = targetExp - currentExp;

        return (int) Math.ceil(expNeeded / experiencePerCast);
    }

    private double getExperiencePerCast(MagicAction spell) {
        switch (spell) {
            case FIRE_STRIKE:
                return 11.5;
            case WIND_STRIKE:
                return 5.5;
            case TELEKINETIC_GRAB:
                return 43.0;
            case SUPERHEAT_ITEM:
                return 53.0;
            default:
                throw new IllegalArgumentException("Unsupported spell: " + spell);
        }
    }

    // This method should use the experience table to determine the experience required for a specific level
    private int getExperienceForLevel(int level) {
        int[] experienceTable = {
                0, 83, 174, 276, 388, 512, 650, 801, 969, 1154,
                1358, 1584, 1833, 2107, 2411, 2746, 3115, 3523, 3973, 4470,
                5018, 5624, 6291, 7028, 7842, 8740, 9730, 10824, 12031, 13363,
                14833, 16456, 18247, 20224, 22406, 24815, 27473, 30408, 33648, 37224,
                41171, 45529, 50339, 55649, 61512, 67983, 75127, 83014, 91721, 101333,
                111945, 123660, 136594, 150872, 166636, 184040, 203254, 224466, 247886, 273742,
                302288, 333804, 368599, 407015, 449428, 496254, 547953, 605032, 668051, 737627,
                814445, 899257, 992895, 1096278, 1210421, 1336443, 1475581, 1629200, 1798808, 1986068,
                2192818, 2421087, 2673114, 2951373, 3258594, 3597792, 3972294, 4385776, 4842295, 5346332,
                5902831, 6517253, 7195629, 7944614, 8771558, 9684577, 10692629, 11805606, 13034431
        };
        return experienceTable[level - 1];
    }

    private void moneyMaking() {
        rasMasterScriptScript master = new rasMasterScriptScript();
        long starttime = System.currentTimeMillis();
        String somemethod = master.moneymaking();
        do {sleep(2000);}
        while (master.isPlugEnabled(somemethod));

        // Implement money-making strategy here
        Microbot.showMessage("No money or runes. Initiating money-making strategy.");
        //shutdown();
    }
}
     /*
    public void equipHighMagicAttackItems() {
        ItemContainer bank = Client.getItemContainer(InventoryID.BANK);
        if (bank == null) {
            return;
        }

        for (Item bankItem : bank.getItems()) {
            if (bankItem == null) {
                continue;
            }

            int bankItemId = bankItem.getId();
            String itemName = ItemManager.getItemComposition(bankItemId).getName().toLowerCase();

            // Skip staff items
            if (itemName.contains("staff")) {
                continue;
            }

            int bankItemMagicAttack = getMagicAttackBonus(bankItemId);

            EquipmentInventorySlot slot = getEquipSlot(bankItemId);
            if (slot == null) {
                continue;
            }

            Rs2Item equippedItem = Rs2Equipment.get(slot);
            if (equippedItem == null || bankItemMagicAttack > getMagicAttackBonus(equippedItem.getId())) {
                Rs2Bank.withdrawAndEquip(bankItemId);
            }
        }
    }

    private int getMagicAttackBonus(int itemId) {
        Rs2Bank.bankItems
        // Assuming getItemStats returns the item stats including magic attack bonus
        return ItemManager.getItemStats(itemId, false).getBonuses()[EquipmentInventorySlot.WEAPON.getSlotIdx()]; // Example index
    }

    private EquipmentInventorySlot getEquipSlot(int itemId) {
        // Assuming itemManager.getItemComposition(itemId).getEquipSlot() returns the equipment slot of the item
        return EquipmentInventorySlot.values()[ItemManager.getItemComposition(itemId).getEquipSlot()];
    }

  */

