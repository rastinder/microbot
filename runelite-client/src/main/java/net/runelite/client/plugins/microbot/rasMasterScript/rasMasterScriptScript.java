package net.runelite.client.plugins.microbot.rasMasterScript;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.geHandler.geHandlerScript;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.rasCollectBones.rasCollectBonesConfig;
import net.runelite.client.plugins.microbot.rasCollectFood.rasCollectFoodConfig;
import net.runelite.client.plugins.microbot.rasCollectFood.rasCollectFoodScript;
import net.runelite.client.plugins.microbot.sideloading.MicrobotPluginManager;
import net.runelite.client.plugins.microbot.tutorialisland.TutorialIslandScript;
import net.runelite.client.plugins.microbot.tutorialisland.TutorialislandPlugin;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.microbot.woodcutting.enums.WoodcuttingResetOptions;
import net.runelite.client.config.ConfigManager;

import javax.swing.SwingUtilities;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;

public class rasMasterScriptScript extends Script {
    public static double version = 1.0;
    public static long totalCoins;
    private long startTime = 0;
    private long endTime = System.currentTimeMillis();
    private long totalTime = endTime - startTime;
    private static String activity = null;
    private static boolean alreadyOn = false;
    public  static String currentPluginName;
    private static String lastPluginName;
    private static long pluginStartTime = 0;
    private long lastPrintTime = 0;
    public static String formattedTime;
    public static long stopTimer = 1;
    static rasMasterScriptConfig jattConfig = null;


    public boolean run(rasMasterScriptConfig config) {
        Microbot.enableAutoRunOn = false;
        try{
            jattConfig = config;
        }
        catch (Exception e){
        System.out.println("problem in config mater Script");
        }
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                jattConfig = config;
                logF2p();
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;

                startTime = System.currentTimeMillis();
                if (activity == null) {
                    //firstTimeCheck();
                    findActivity();
                }
                if (activity.equals("running")) {
                    monitorRunningPlugin();
                    printTimePlugin();
                } else {
                    executeActivity();
                }

                //endTime = System.currentTimeMillis();
                //totalTime = endTime - startTime;
                //System.out.println("Total time for loop " + totalTime);
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

    private void monitorRunningPlugin() {
        if (!isPlugEnabled(currentPluginName)) {
            stopAllPlugins(); // just a precaution may not be needed;
            alreadyOn = false;
            activity = "selectRandom";
            getTotalCoins(); // update coins
        }
    }

    private void findActivity() {
        logF2p();
        if (activity == null) tutIsland();
        if (activity == null) activity = "selectRandom"; //selectRandomActivity();
    }

    private void selectRandomActivity() {
        List<String> activities = Arrays.asList("moneymaking", "skill", "randomtimewaste", "quest", "combat", "stackedActivity");
        Collections.shuffle(activities);
        activity = activities.get(0);

    }

    private void executeActivity() {
        System.out.println("activity to start: "+ activity );
        switch (activity) {
            case "tutIsland":
                System.out.println("Starting tutIsland activity"); // Debug line
                tutIsland();
                System.out.println("Finished tutIsland activity"); // Debug line
                break;
            case "firsttimecheck":
                System.out.println("Starting firstTimeCheck activity"); // Debug line
                firstTimeCheck();
                System.out.println("Finished firstTimeCheck activity"); // Debug line
                break;
            case "moneymaking":
                System.out.println("Starting moneymaking activity"); // Debug line
                moneymaking();
                System.out.println("Finished moneymaking activity"); // Debug line
                break;
            case "skill":
                System.out.println("Starting skilling activity"); // Debug line
                skilling();
                System.out.println("Finished skilling activity"); // Debug line
                break;
            case "randomtimewaste":
                System.out.println("Starting randomTimeWaste activity"); // Debug line
                randomTimeWaste();
                System.out.println("Finished randomTimeWaste activity"); // Debug line
                break;
            case "quest":
                System.out.println("Starting questing activity"); // Debug line
                questing();
                System.out.println("Finished questing activity"); // Debug line
                break;
            case "combat":
                System.out.println("Starting combatTrain activity"); // Debug line
                combatTrain();
                System.out.println("Finished combatTrain activity"); // Debug line
                break;
            case "stackedActivity":
                System.out.println("Starting stackedActivity activity"); // Debug line
                stackedActivity();
                System.out.println("Finished stackedActivity activity"); // Debug line
                break;
            case "selectRandom":
                System.out.println("Starting selectRandomActivity activity"); // Debug line
                selectRandomActivity();
                System.out.println("Finished selectRandomActivity activity"); // Debug line
                break;
            case "mostProfitableMoneyMaking":
                System.out.println("Starting mostProfitableMoneyMaking activity"); // Debug line
                mostProfitableMoneyMaking();
                System.out.println("Finished mostProfitableMoneyMaking activity"); // Debug line
                break;
            case "login":
                System.out.println("Starting logF2p activity"); // Debug line
                logF2p();
                System.out.println("Finished logF2p activity"); // Debug line
                break;
            default:
                System.out.println("Invalid activity: " + activity); // Debug line
                break;

        }
    }

    private void mostProfitableMoneyMaking() {
        // calculate most proftable method and execute it
        activity = "selectRandom";

    }

    private void tutIsland() {
        boolean status = checkTutIslandStatus();
        if (status && !isPlugEnabled("TutorialIsland")) {
            startPlugin("TutorialIsland");
            sleep(5000);
            while (isPlugEnabled("TutorialIsland")) {
                sleep(1000);
                try {
                    if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3236, 3220, 0)) < 20)
                        stopPlugin("TutorialIsland");
                } catch (Exception e) {
                    System.out.println("cant find location");
                }
            }
            startPlugin("ras firsttime check");
            sleep(5000);
            while (isPlugEnabled("ras firsttime check")) {
                sleep(1000);
            }
            activity = "moneymaking";
        }
    }

    private static boolean checkTutIslandStatus() {
        int varbitValue = Microbot.getVarbitPlayerValue(281);
        return Rs2Widget.getWidget(558, 1) != null || Rs2Widget.getWidget(679, 1) != null || varbitValue < 10 ||
                (varbitValue >= 10 && varbitValue < 120) || (varbitValue >= 120 && varbitValue < 200) ||
                (varbitValue >= 200 && varbitValue <= 250) || (varbitValue >= 260 && varbitValue <= 360) ||
                (varbitValue > 360 && varbitValue < 510) || (varbitValue >= 510 && varbitValue < 540) ||
                (varbitValue >= 540 && varbitValue < 610) || (varbitValue >= 610 && varbitValue < 1000);
    }

    private static void firstTimeCheck() {
        managePlugin(true, "firsttimecheck", "ras firsttime check", null);
        new Thread(() -> {
            while (isPlugEnabled("ras firsttime check")) {
                try {
                    Thread.sleep(1000); // Check every second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            activity = "selectRandom";
        }).start();
    }

    public String moneymaking() {
        String pluginName = null;
        List<String> trainSkill = Arrays.asList("Prayer");
        if (random(0, 1) == 0)
            trainSkill = Arrays.asList("chocolate dust","ras HardLeather", "Ras Jewellery Maker", "ras_high alc", "pie shell", "Reddie", "Collecting ruby rings", "uncooked apple pies", "pastry dough", "Collecting fish food", "Smelting steel bars", "Cuttinglogs", "CollectingGroundItems","Collecting tinderboxes", "Wheat");
        Collections.shuffle(trainSkill);
        for (String skill : trainSkill) {
            System.out.println("initiate moneymaking: "+ skill );
            switch (skill) {
                case "ras HardLeather":
                    if (!Objects.equals(lastPluginName, currentPluginName) || Objects.equals(currentPluginName,null))
                       pluginName = HardLeather();
                    break;
                case "Ras Jewellery Maker":
                    pluginName = RasJewelleryMaker();
                    break;
                case "ras_high alc":
                    pluginName = RasHighCal();
                    break;
                case "pie shell":
                    pluginName = PieShell();
                    break;
                case "Reddie":
                    pluginName = RedDie();
                    break;
                case "Collecting ruby rings":
                    pluginName = CollectingRubyRings();
                    break;
                case "uncooked apple pies":
                    pluginName = UncookedApplePies();
                    break;
                case "pastry dough":
                    pluginName = PastryDough();
                    break;
                case "Collecting fish":
                    pluginName = CollectingFish();
                    break;
                case "Smelting steel bars":
                    pluginName = Smeltingsteelbars();
                    break;
                case "Cuttinglogs":
                    pluginName = Cuttinglogs();
                    break;
                case "CollectingGroundItems":
                    pluginName = CollectingGroundItems();
                    break;
                case "chocolate dust":
                    pluginName = chocolateDust();
                    break;

                case "Collecting fish food":
                    pluginName = Collectingfishfood();
                    break;
                case "Collecting tinderboxes":
                    pluginName = CollectingTinderboxes();
                    break;
                case "Wheat":
                    pluginName = Wheat();
                    break;
                    /*
                case "":
                    pluginName = ();
                    break;

                     */
                default:
                    System.out.println(skill + " not implemented");
                    break;
            }
            // Check if the plugin is still enabled before proceeding to the next skill
            if (pluginName != null && isPlugEnabled(pluginName)) {
                System.out.println("money making: "+ skill );
                return pluginName;
            }
        }
        return null;
    }



    private void skilling() {
        String pluginName = null;
        List<String> trainSkill = Arrays.asList("Prayer");
        if (random(0, 2) == 0)
            trainSkill = Arrays.asList("FishCookPrayer", "FishCook", "MiningSmithing", "WoodcuttingFiremaking", "RunecraftingMining", "MagicRunecrafting", "PrayerCombat", "MiningCrafting", "FishingCookingWoodcuttingFiremaking", "SmithingCrafting", "RunecraftingMagicCombat", "MiningSmithingCrafting", "MagicSmith", "CraftingWoodcutting");
        else
            trainSkill = Arrays.asList("Strength", "Range", "Prayer", "Magic", "Runecraft", "Crafting", "Mining", "Smithing", "Fishing", "Cooking", "Firemaking", "Woodcutting");
        Collections.shuffle(trainSkill);
        for (String skill : trainSkill) {
            System.out.println("initiate skilling: "+ skill );
            switch (skill) {
                case "Prayer":
                    pluginName = trainPrayer();
                    break;
                case "FishCookPrayer":
                    pluginName = trainFishCookPrayer();
                    break;
                case "Magic":
                    pluginName = trainMagic();
                    break;
                case "WoodcuttingFiremaking":
                    pluginName =trainWoodcuttingFiremaking();
                    break;
                case "Fishing":
                    pluginName = trainFishing();
                    break;
                    /*
                case "FishCook":
                    pluginName =trainFishCook();
                    break;
                case "MiningSmithing":
                    pluginName =trainMiningSmithing();
                    break;

                case "RunecraftingMining":
                    pluginName =trainRunecraftingMining();
                    break;
                case "MagicRunecrafting":
                    pluginName =trainMagicRunecrafting();
                    break;
                case "PrayerCombat":
                    pluginName =trainPrayerCombat();
                    break;
                case "MiningCrafting":
                    pluginName =trainMiningCrafting();
                    break;
                case "FishingCookingWoodcuttingFiremaking":
                    pluginName =trainFishingCookingWoodcuttingFiremaking();
                    break;
                case "SmithingCrafting":
                    pluginName =trainSmithingCrafting();
                    break;
                case "RunecraftingMagicCombat":
                    pluginName =trainRunecraftingMagicCombat();
                    break;
                case "MiningSmithingCrafting":
                    pluginName =trainMiningSmithingCrafting();
                    break;
                case "MagicSmith":
                    pluginName =trainMagicSmith();
                    break;
                case "CraftingWoodcutting":
                    pluginName =trainCraftingWoodcutting();
                    break;
                case "Strength":
                    trainStrength();
                    break;
                case "Range":
                    trainRange();
                    break;
                case "Runecraft":
                    trainRunecraft();
                    break;
                case "Crafting":
                    trainCrafting();
                    break;
                case "Mining":
                    trainMining();
                    break;
                case "Smithing":
                    trainSmithing();
                    break;
                case "Cooking":
                    trainCooking();
                    break;
                case "Firemaking":
                    trainFiremaking();
                    break;
                case "Woodcutting":
                    trainWoodcutting();
                    break;

                     */
                default:
                    System.out.println(skill + " not implemented");
                    break;
            }
            if (pluginName != null && isPlugEnabled(pluginName)) {
                System.out.print("skill: "+ skill );
                return;
            }
        }
    }
    private String Wheat() {
        if (jattConfig.wheat()){
        bankAllAndGet("jatt");
        startPlugin("Wheat");
            //long sleepTime = random(1800000, 2760000), startTime = System.currentTimeMillis();
            long sleepTime = autoStopTimer(), startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < sleepTime && isPlugEnabled("Wheat")) {
                sleep(2000);
            }
            System.out.println("stopping " + currentPluginName);
            stopPlugin("Wheat");
            while (isPlugEnabled("Wheat")) sleep(2000);

        //bankAllAndGet("jatt");
        geHandlerScript.goSell(false,5,new int[]{0},"Grain");
        }
        return null;
    }
    public String CollectingTinderboxes(){
        if (totalCoins < 50000 && jattConfig.tinderbox()) {
            bankAllAndGet("nothing");
            return startPlugin("ras Tinderbox");
        }
        return null;
    }
    private String Collectingfishfood() {
        if (totalCoins < 100000 && jattConfig.fishfood()) {
            bankAllAndGet("jatt");
            Microbot.getPluginManager().setPluginValue("firstTimeChecks", "sellthem", true);
            Microbot.getPluginManager().setPluginValue("firstTimeChecks", "itemsToPick", "Raw,Trout,Salmon");;
            return startPlugin("collect food");
        }
        return null;
    }

    private String trainWoodcuttingFiremaking() {
        if (jattConfig.Woodcutting()) {
            bankAllAndGet("Bronze axe", "Tinderbox");
            if (!Rs2Inventory.hasItem("Bronze axe") && totalCoins > 500) {
                geHandlerScript.goBuy(new int[]{1}, 5, false, "Bronze axe");
                if (!Rs2Inventory.hasItem("Tinderbox") && totalCoins > 500)
                    geHandlerScript.goBuy(new int[]{1}, 5, false, "Tinderbox");
            } else
                return null;
            WorldPoint tree1 = Math.random() < 0.5 ? new WorldPoint(3015, 3171, 0) : new WorldPoint(3162, 3265, 0);

            while (tree1.distanceTo(Rs2Player.getWorldLocation()) < 10) {
                Rs2Walker.walkTo(tree1);
                sleepUntilTrue(() -> tree1.distanceTo(Rs2Player.getWorldLocation()) < 10, 100, 30000);
            }
            if (Rs2Inventory.hasItem("Tinderbox"))
                Microbot.getPluginManager().setPluginValue("Woodcutting", "ItemAction", WoodcuttingResetOptions.FIREMAKE);
            else
                Microbot.getPluginManager().setPluginValue("Woodcutting", "ItemAction", WoodcuttingResetOptions.BANK);
            startPlugin("Auto Woodcutting");
            sleep(random(1800000, 2760000));
            stopPlugin("Auto Woodcutting");
            do {
                sleep(2000);
                System.out.println("stopping" + currentPluginName);
            }
            while (isPlugEnabled("Wheat"));
        }
        return null;
    }

    private String chocolateDust() {
        if (jattConfig.chocolateDust()) {
            bankAllAndGet("Knife");
            if (!Rs2Inventory.hasItem("Knife")) {
                if (totalCoins > 200)
                    geHandlerScript.goBuyAndReturn(new int[]{1}, 10, true, "Knife");
                else
                    return null;
            }
            if (!Rs2Inventory.hasItem("Knife")) {
                Rs2Tab.switchToInventoryTab();
                Microbot.getPluginManager().setPluginValue("Combine", "item 1", "Knife"); // this item should always be lowest count or same count as item2
                Microbot.getPluginManager().setPluginValue("Combine", "item 1 count", 1);
                Microbot.getPluginManager().setPluginValue("Combine", "item 2", "Chocolate bar");
                Microbot.getPluginManager().setPluginValue("Combine", "item 2 count", 27);
                Microbot.getPluginManager().setPluginValue("Combine", "item 3", "");
                Microbot.getPluginManager().setPluginValue("Combine", "press space", false);
                Microbot.getPluginManager().setPluginValue("Combine", "buyMissingItems", true);
                return startPlugin("ras Combine");
            } else
                return null;
        }
        return null;
    }

    private String PieShell() {
        if (totalCoins > 1000 && jattConfig.pieShell()) {
            Rs2Tab.switchToInventoryTab();
            Microbot.getPluginManager().setPluginValue("Combine", "item 1", "Pie dish"); // this item should always be lowest count or same count as item2
            Microbot.getPluginManager().setPluginValue("Combine", "item 1 count", 14);
            Microbot.getPluginManager().setPluginValue("Combine", "item 2", "Pastry dough");
            Microbot.getPluginManager().setPluginValue("Combine", "item 2 count", 14);
            Microbot.getPluginManager().setPluginValue("Combine", "item 3", "");
            Microbot.getPluginManager().setPluginValue("Combine", "press space", true);
            Microbot.getPluginManager().setPluginValue("Combine", "buyMissingItems", true);
            return startPlugin("ras Combine");
        }
        else
            return null;
    }

    private String RedDie() {
        if (totalCoins > 100000 ) {
            bankAllAndGet(random(3, 11) * 1000, "Coins");
            return startPlugin("Ras Red die");
        }
        System.out.println("no money");
        return null;
    }

    private String CollectingRubyRings() {
        return null;
    }

    private String PastryDough() {
        if (totalCoins > 500 && jattConfig.pastryDough()) {
            Microbot.getPluginManager().setPluginValue("Combine", "item 1", "Jug of water"); // this item should always be lowest count or same count as item2
            Microbot.getPluginManager().setPluginValue("Combine", "item 1 count", 9);
            Microbot.getPluginManager().setPluginValue("Combine", "item 2", "Pot of flour");
            Microbot.getPluginManager().setPluginValue("Combine", "item 2 count", 9);
            Microbot.getPluginManager().setPluginValue("Combine", "item 3", "");
            Microbot.getPluginManager().setPluginValue("Combine", "press space", true);
            Microbot.getPluginManager().setPluginValue("Combine", "buyMissingItems", true);
            return startPlugin("ras Combine");
        }
        return null;
    }

    private String CollectingFish() {
        return null;
    }

    private String Smeltingsteelbars() {
        return null;
    }

    private String UncookedApplePies() {
        if (totalCoins > 1000000000000000000L) { // testing
        Microbot.getPluginManager().setPluginValue("Combine", "item 1", "Pie dish"); // this item should always be lowest count or same count as item2
        Microbot.getPluginManager().setPluginValue("Combine", "item 1 count", 14);
        Microbot.getPluginManager().setPluginValue("Combine", "item 2", "Pastry dough");
        Microbot.getPluginManager().setPluginValue("Combine", "item 2 count", 14);
        Microbot.getPluginManager().setPluginValue("Combine", "item 3", "Cooking apple");
        Microbot.getPluginManager().setPluginValue("Combine", "item 3 count", 14);
        Microbot.getPluginManager().setPluginValue("Combine", "press space", true);
        Microbot.getPluginManager().setPluginValue("Combine", "buyMissingItems", true);
        return startPlugin("ras Combine");
    }
        return null;
    }

    private String Cuttinglogs() {
        return null;
    }

    private String CollectingGroundItems() {
        return null;
    }

    private String RasHighCal() {
        if (Rs2Player.getRealSkillLevel(Skill.MAGIC) > 54 && jattConfig.highCal()) {
            Microbot.getPluginManager().setPluginValue("highalc", "Autobuy", true);
            return startPlugin("ras_high alc");
        }
        return null;
    }

    private String RasJewelleryMaker() {
        return null;
    }

    private String HardLeather() {
        if (jattConfig.HardLeather()) {
            bankAllAndGet(3000, "Coins");
            Microbot.getPluginManager().setPluginValue("menuentryswapper", "swapTan", true);
            return startPlugin("ras HardLeather");
        }
        return null;
    }



    private String trainMagic() {
        if (!jattConfig.trainmagic())
            return null;
         if (totalCoins == 0) {
            if (getTotalCoins() > 200000) {
                if (Rs2Player.getRealSkillLevel(Skill.MAGIC) > 54) {
                    Microbot.getPluginManager().setPluginValue("highalc", "Autobuy", true);
                    return startPlugin("ras_high alc");
                } else
                    return startPlugin("ras Magic Train");
            }
        }else if (getTotalCoins() > 200000) {
             if (Rs2Player.getRealSkillLevel(Skill.MAGIC) > 54) {
                 Microbot.getPluginManager().setPluginValue("highalc", "Autobuy", true);
                 return startPlugin("ras_high alc");
             } else
                 return startPlugin("ras Magic Train");
        }
        return null;
    }

    private String trainFishCookPrayer() {
        activity = "selectRandom";
        return activity;
    }

    private String trainPrayer() {
        // add location hop world etc bank items etc etc.
        Microbot.getPluginManager().setPluginValue("Combat", "Combat", false);
        Microbot.getPluginManager().setPluginValue("Loot", "Loot items", true);
        Microbot.getPluginManager().setPluginValue("Loot", "items to loot", "Bones,Big");
        //return  startPlugin("ras range bone collector");

        //activity = "selectRandom";
        return null;
    }

    private String trainFishing() {
        System.out.println("train fush? "+ jattConfig.fishing());
        if (!jattConfig.fishing())
            return null;
        if (totalCoins > 20) {
            bankAllAndGet(10, "Coins");
            bankAllAndGet(1, "net");
            WorldPoint fish = new WorldPoint(3266, 3149, 0);
            Rs2Walker.walkTo(fish);
            sleepUntilTrue(() -> fish.distanceTo(Rs2Player.getWorldLocation()) < 10, 100, 280000);
            Microbot.getPluginManager().setPluginValue("Fishing", "UseBank", true); // also not working
            startPlugin("Auto Fishing");
            sleep((int)autoStopTimer());
            while (isPlugEnabled("Auto Fishing")) {
                stopPlugin("Auto Fishing");
                sleep(2000);
            }
            homeTeleport();
            return null;
            //bankAllAndGet(10, "Coins");
        }
        return null;
    }

    private void combatTrain() {
        if (!jattConfig.combattrain())
            activity = "selectrandom";
        else {
            if (Microbot.getClient().getRealSkillLevel(Skill.STRENGTH) > 39 || Microbot.getClient().getRealSkillLevel(Skill.ATTACK) > 39 || Microbot.getClient().getRealSkillLevel(Skill.DEFENCE) > 19)
                startPlugin("ras CombatTrain");
            else {
                Microbot.getPluginManager().setPluginValue("collectbones", "cook food items", rasCollectBonesConfig.cookON.Cabbage); // default healing method
                Microbot.getPluginManager().setPluginValue("collectbones", "Combat", true);
                Microbot.getPluginManager().setPluginValue("collectbones", "Loot items", false);
                Microbot.getPluginManager().setPluginValue("collectbones", "items to loot", "Bones,Big");
                int chooseLocation = random(0, 5);
                String jattDiTalwar = "";
                int attackLevel = Microbot.getClient().getRealSkillLevel(Skill.ATTACK);
                if (attackLevel >= 40) {
                    jattDiTalwar = "Adamant scimitar";
                } else if (attackLevel >= 30) {
                    jattDiTalwar = "Mithril scimitar";
                } else if (attackLevel >= 20) {
                    jattDiTalwar = "Steel scimitar";
                } else if (attackLevel >= 10) {
                    jattDiTalwar = "Iron scimitar";
                } else {
                    jattDiTalwar = "Bronze scimitar";
                }
                WorldPoint enemyLocation = new WorldPoint(3017, 3290, 0);
                if (Microbot.getClient().getRealSkillLevel(Skill.STRENGTH) > 20) {
                    Microbot.getPluginManager().setPluginValue("collectbones", "cook food items", rasCollectBonesConfig.cookON.Fire);
                    //Microbot.getPluginManager().setPluginValue("Combat", "enemyToAttack", "Giant frog,Big frog,Giant rat,Chicken");
                    Microbot.getPluginManager().setPluginValue("collectbones", "Loot items", true);
                    sleep(10);
                    chooseLocation = random(0, 2);
                    if (chooseLocation == 0) {
                        enemyLocation = new WorldPoint(3200, 3172, 0); //Giant frogs
                    }
                    if (chooseLocation == 1) {
                        enemyLocation = new WorldPoint(3199, 3191, 0); //Giant frogs
                    }

                } else if (Microbot.getClient().getRealSkillLevel(Skill.STRENGTH) > 10) {
                    Microbot.getPluginManager().setPluginValue("collectbones", "enemyToAttack", "Cow,Cow calf");
                    chooseLocation = random(0, 6);
                    if (chooseLocation == 0) {
                        enemyLocation = new WorldPoint(3031, 3304, 0); //flador cow
                    } else if (chooseLocation == 1) {
                        enemyLocation = new WorldPoint(2924, 3285, 0); // crafting guild cow 1
                    } else if (chooseLocation == 2) {
                        Microbot.getPluginManager().setPluginValue("collectbones", "enemyToAttack", "Rat,Giant rat");
                        enemyLocation = new WorldPoint(3197, 3202, 0); // Giant rat lumbridge
                    } else if (chooseLocation == 3) {
                        enemyLocation = new WorldPoint(2935, 3274, 0);  // crafting guild cow 2
                    } else if (chooseLocation == 4) {
                        enemyLocation = new WorldPoint(3203, 3292, 0);  // nearmil cow 1
                    } else if (chooseLocation == 5) {
                        enemyLocation = new WorldPoint(3191, 3312, 0);  // nearmil cow 2
                    }
                } else if (Microbot.getClient().getRealSkillLevel(Skill.STRENGTH) >= 1) {
                    chooseLocation = random(0, 4);
                    Microbot.getPluginManager().setPluginValue("collectbones", "enemyToAttack", "chicken");
                    if (chooseLocation == 0) {
                        enemyLocation = new WorldPoint(3232, 3296, 0); //chicken normal
                    }
                    if (chooseLocation == 1) {
                        enemyLocation = new WorldPoint(3177, 3296, 0); //chicken near mill
                    }
                    if (chooseLocation == 2) {
                        enemyLocation = new WorldPoint(3052, 3491, 0); // monk
                        Microbot.getPluginManager().setPluginValue("collectbones", "enemyToAttack", "Monk");
                        Microbot.getPluginManager().setPluginValue("collectbones", "cook food items", rasCollectBonesConfig.cookON.Monk);
                    }
                    if (chooseLocation == 3) {
                        enemyLocation = new WorldPoint(3017, 3290, 0); // chicken near falador
                    }
                }
                if (Rs2Equipment.get(EquipmentInventorySlot.WEAPON) == null || !Rs2Equipment.get(EquipmentInventorySlot.WEAPON).name.contains(jattDiTalwar)) {
                    System.out.println(Rs2Equipment.get(EquipmentInventorySlot.WEAPON).name);
                    bankAllAndGet(jattDiTalwar);
                    if (Rs2Inventory.hasItem(jattDiTalwar)) {
                        Rs2Inventory.interact(jattDiTalwar, "Wield");
                        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                    } else {
                        if (totalCoins > (geHandlerScript.priceChecker(jattDiTalwar)[0] * 2L)) {
                            geHandlerScript.goBuy(new int[]{1}, 5, jattDiTalwar);
                            Rs2Inventory.interact(jattDiTalwar, "Wield");
                        } else {
                            bankAllAndGet("scimitar");
                            Rs2Bank.openBank();
                            if (Rs2Equipment.get(EquipmentInventorySlot.WEAPON) == null || !Rs2Equipment.get(EquipmentInventorySlot.WEAPON).name.contains("scimitar")) {
                                System.out.println(Rs2Equipment.get(EquipmentInventorySlot.WEAPON).name);
                                if (!Rs2Equipment.get(EquipmentInventorySlot.WEAPON).name.contains("scimitar")) {
                                    if (Rs2Inventory.hasItem("scimitar", false)) {
                                        Rs2Inventory.interact(jattDiTalwar, "Wield");
                                        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                                    }
                                }
                            } else if (Rs2Bank.hasBankItem("scimitar", false) && !Rs2Equipment.get(EquipmentInventorySlot.WEAPON).name.contains("scimitar")) {
                                Rs2Bank.withdrawX(true, "scimitar", 1, false);
                                sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                                Rs2Inventory.interact(Rs2Inventory.get("scimitar").getName(), "Wield");
                            } else if (!Rs2Equipment.get(EquipmentInventorySlot.WEAPON).name.contains("scimitar")) {
                                if (Rs2Bank.hasBankItem("sword", false)) {
                                    Rs2Bank.withdrawX(true, "sword", 1, false);
                                    sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                                    Rs2Inventory.interact(Rs2Inventory.get("sword").getName(), "Wield");
                                } else if (Rs2Bank.hasBankItem("dagger", false)) {
                                    Rs2Bank.withdrawX(true, "dagger", 1, false);
                                    sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                                    Rs2Inventory.interact(Rs2Inventory.get("dagger").getName(), "Wield");
                                }

                            }

                        }
                    }
                }
                Rs2Walker.setTarget(enemyLocation);
                Rs2Walker.walkTo(enemyLocation, random(0, 3));
                WorldPoint finalEnemyLocation = enemyLocation;
                sleepUntilTrue(() -> Rs2Player.getWorldLocation().distanceTo(finalEnemyLocation) < 5, 500, 380000);
                Rs2Player.waitForAnimation();
                startPlugin("ras range bone collector");
            }
        }
    }

    private void questing() {
        System.out.println("Questing activity is  not implemented .");
       // if( jattconfig.questAllz && totalCoins > 20000 && (!Objects.equals(lastPluginName, currentPluginName) || Objects.equals(currentPluginName,null)))
       //     startPlugin("Allz Quester");
      //  else
            activity = "selectRandom";
    }

    private void randomTimeWaste() {
        System.out.println("Random time wasting activity not implemented yet.");
        activity = "selectRandom";
    }

    private void stackedActivity() {
        System.out.println("Stacked activity running implemented yet.");
        List<Integer> amounts = new ArrayList<>();
        List<String> itemNamesToPass = new ArrayList<>();
        bankAllAndGet("jatt");
        Rs2Bank.openBank();
        Map<String, Integer> itemAmountMap = new HashMap<>() {{
            put("Raw salmon", 0);
            put("Pie shell", 0);
            put("Uncooked apple pie", 0);
            put("Raw trout", 0);
            //put("Tinderbox", -1);
            put("Grain", 0);
            put("Hard leather", 0);
            put("Pot", 0);
            put("Jug", 0);
            put("Bucket", 0);
            put("Bowl", 0);
        }};
        if(Rs2Bank.hasBankItem("Tinderbox",2)){
            itemNamesToPass.add("Tinderbox");
            amounts.add(0);
        }
        itemAmountMap.forEach((item, amount) -> {
            if (Rs2Bank.hasItem(item, true)) {
                itemNamesToPass.add(item);
                amounts.add(amount);
            }
        });
        if(!itemNamesToPass.isEmpty())
            geHandlerScript.goSell(false, 5, amounts.stream().mapToInt(i -> i).toArray(), itemNamesToPass.toArray(new String[0]));
        activity = "selectRandom";
    }

    private void logF2p() {
        if (Microbot.getClient().getGameState() == GameState.LOGIN_SCREEN) {
            stopAllPlugins();
            sleep(5000); // so that other plugins can disable themselfs
            new Login(Login.getRandomWorld(Rs2Player.isMember()));
            sleep(5000);
            while (!Microbot.isLoggedIn() || !Rs2Widget.hasWidget("CLICK HERE TO PLAY")) sleep(2000);
            if (Rs2Widget.hasWidget("CLICK HERE TO PLAY")){
                Rs2Widget.clickWidget("CLICK HERE TO PLAY");
            }
            sleep(6000);
            Microbot.getMouse().scrollDown(new Point(800, 800));
            sleep(1000);
            Microbot.getClient().setCameraPitchTarget(460);
            if (totalCoins == 0) {
                if (isinALKhrid())
                    homeTeleport();
                if (getTotalCoins() < 100000) {
                    activity = "moneymaking";
                }
            }
        }
    }

    public static boolean isinALKhrid() {
        if (Rs2Player.getWorldLocation().getY()  < 3228 && Rs2Player.getWorldLocation().getX() >3267){
            return true;
        }
        return false;
    }

    private void printTimePlugin() {
        if (System.currentTimeMillis() - lastPrintTime >= 5000) {
            try {
                Duration elapsed = Duration.ofMillis(System.currentTimeMillis() - pluginStartTime);
                formattedTime = String.format("%02d:%02d:%02d",
                        elapsed.toHours(),
                        elapsed.toMinutesPart(),
                        elapsed.toSecondsPart());
                //System.out.println("Time passed: " + formattedTime);
                lastPrintTime = System.currentTimeMillis();
            } catch (Exception e) {
                System.out.println("Error in time calculation: " + e.getMessage());
            }
        }
    }

    private static void managePlugin(Boolean status, String currentActivity, String pluginName, String nextActivity) {
        if (isPlugEnabled(pluginName)) {
            if (currentActivity.equals("tutIsland") && !checkTutIslandStatus()) {
                stopPlugin(pluginName);
                alreadyOn = false;
                activity = nextActivity != null ? nextActivity : "selectRandom";
                if (activity.equals("firsttimecheck")) {
                    firstTimeCheck();
                }
            }
        } else if (status) {
            startPlugin(pluginName);
            alreadyOn = true;
            activity = currentActivity;
        }
    }

    public static boolean isPlugEnabled(String pluginName) {
        try {
            return Microbot.getPluginManager().isPluginEnabled(getPluginByName(pluginName));
        } catch (Exception x) {
            return false;
        }
    }

    public static String startPlugin(String pluginName) {
        System.out.println("Starting startPlugin"); // Debug line
        try {
            Microbot.getPluginManager().setPluginEnabled(getPluginByName(pluginName), true);
            sleep(100);
            Microbot.getPluginManager().startPlugins();
            if (!(currentPluginName == null))
                lastPluginName = currentPluginName;
            currentPluginName = pluginName;
            activity = "running";
            pluginStartTime = System.currentTimeMillis();
            System.out.println("started plugin: " + pluginName);
            return pluginName;
        } catch (Exception e) {
            System.out.println("Failed to start plugin: " + e.getMessage());
            return null;
        }
    }

    public static void stopPlugin(String pluginName) {
        try {
            Microbot.getPluginManager().setPluginEnabled(getPluginByName(pluginName), false);
            sleep(500);
            SwingUtilities.invokeLater(() -> {
                try {
                    Microbot.getPluginManager().stopPlugin(getPluginByName(pluginName));
                    System.out.println("stopped plugin: " + pluginName);
                } catch (PluginInstantiationException e) {
                    System.out.println("error stopPlugin"); // Debug line
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            System.out.println("Failed to stop plugin: " + e.getMessage());
        }
    }

    public static Plugin getPluginByName(String pluginName) {
        for (Plugin plugin : Microbot.getPluginManager().getPlugins()) {
            PluginDescriptor descriptor = plugin.getClass().getAnnotation(PluginDescriptor.class);
            if (descriptor != null && descriptor.name().contains(pluginName)) {
                //System.out.println("plugin enabled: " + descriptor.name()); // uncomment for testing
                return plugin;
            }
        }
        return null;
    }
    public void bankAllAndGet(String... items) {
        bankAllAndGet(1,items);
    }
    public static void bankAllAndGet(int amount, String... items) {
        while (!Rs2Bank.isOpen()) {
            if (!Rs2Bank.isNearBank(10)) {
                Rs2Bank.walkToBank();
                sleepUntilTrue(() -> Rs2Bank.isNearBank(10), 100, 550000);
            }else {
                Rs2Bank.openBank();
                sleepUntilTrue(() -> Rs2Bank.isOpen(), 100, 5000);
            }
        }
        if (!Rs2Inventory.isEmpty()) {
            Rs2Bank.depositAll();
            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
        }
        for (String item : items) {
            Rs2Bank.withdrawX(item, amount);
            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
        }
        long coinsInBank = (long) Rs2Bank.count("Coins", true);
        Rs2Bank.closeBank();
        totalCoins = Rs2Inventory.ItemQuantity(995) + coinsInBank;
    }

    public static long getTotalCoins() {
        while (!Rs2Bank.isOpen()) {
        WorldPoint location = Rs2Player.getWorldLocation();
            if (!Rs2Bank.isNearBank(6)) {
                Rs2Bank.walkToBank();
                sleepUntilTrue(() -> Rs2Bank.isNearBank(6), 100, 280000);
            }
                Rs2Bank.openBank();
                sleep(1500);
        }
        if (Rs2Widget.isWidgetVisible(664, 29)) {
            firstTimeCheck();
            //Rs2Widget.clickWidget(664, 29);
            //sleep(500);
        }
        long coinsInBank = (long) Rs2Bank.count("Coins", true);
        Rs2Bank.closeBank();
        totalCoins = Rs2Inventory.ItemQuantity(995) + coinsInBank;
        /*
        if (Rs2Player.getWorldLocation().distanceTo(location) > 6) {
            Rs2Walker.walkTo(location, 2);
            sleepUntilTrue(() -> Rs2Player.getWorldLocation().distanceTo(location) < 6, 100, 280000);
        }

         */
        return totalCoins;
    }
    public static boolean autoShutdown(String pluginName){
        if (Microbot.getClient().getGameState() == GameState.LOGIN_SCREEN) {
            System.out.println("stoping plugin "+ pluginName );
            stopPlugin(pluginName);
            System.out.println("successfully stoped "+ pluginName );
            return true;
        }
        return false;
    }
    public static void homeTeleport() {
        if(!isHomeTeleportOnCooldown()) {
            Rs2Walker.setTarget(null);
            Rs2Tab.switchToMagicTab();
            sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC);
            Rs2Walker.setTarget(null);
            WorldPoint oldlocation = Rs2Player.getWorldLocation();
            sleep(500);
            Rs2Widget.clickWidget("Lumbridge Home");
            sleepUntilTrue(() -> Rs2Player.getWorldLocation().distanceTo(oldlocation) > 10, 500, 20000);
            Rs2Tab.switchToInventoryTab();
            //Rs2Player.waitForAnimation();
        }
        else {
            WorldPoint outside = new WorldPoint(3274,3331,0);
            //WorldPoint outside1 = new WorldPoint(3247,3313,0);
            while (Rs2Player.getWorldLocation().distanceTo(outside) > 10) {
                Rs2Walker.setTarget(outside);
                Rs2Walker.walkTo(outside);
                sleepUntilTrue(() -> Rs2Player.getWorldLocation().distanceTo(outside) < 15, 500, 120000);
            }
        }

    }
    public static Instant getLastHomeTeleportUsage() {
        Instant lastTeleportUsage = Instant.ofEpochSecond((long) Microbot.getClient().getVarpValue(VarPlayer.LAST_HOME_TELEPORT) * 60L);
        System.out.println("Last Home Teleport Usage (Epoch Seconds): " + lastTeleportUsage.getEpochSecond());
        return lastTeleportUsage;
    }

    public static boolean isHomeTeleportOnCooldown() {
        Instant lastUsage = getLastHomeTeleportUsage();
        Instant cooldownEnd = lastUsage.plus(30L, ChronoUnit.MINUTES);
        Instant currentTime = Instant.now();

        System.out.println("Last Usage: " + lastUsage);
        System.out.println("Cooldown End: " + cooldownEnd);
        System.out.println("Current Time: " + currentTime);

        boolean onCooldown = cooldownEnd.isAfter(currentTime);
        System.out.println("Is Home Teleport on Cooldown? " + onCooldown);

        return onCooldown;
    }
    /*
    public static Instant getLastHomeTeleportUsage() {
        return Instant.ofEpochSecond((long) Microbot.getClient().getVarpValue(VarPlayer.LAST_HOME_TELEPORT) * 60L);
    }

    public static boolean isHomeTeleportOnCooldown() {
        return !getLastHomeTeleportUsage().plus(30L, ChronoUnit.MINUTES).isAfter(Instant.now());
    }

     */
    public static void stopAllPlugins() {
        List<String> pluginNames = Arrays.asList(
                "ras Tinderbox",
                "collect food",
                "ras Combine",
                "Ras Red die",
                "ras_high alc",
                "ras Magic Train",
                "ras HardLeather",
                "ras range bone collector",
                "Wheat",
                "Auto Woodcutting",
                "Auto Fishing",
                "Allz Quester"
        );

        for (String pluginName : pluginNames) {
            if (isPlugEnabled(pluginName))
                stopPlugin(pluginName);
        }
    }
    public static long autoStopTimer(){
        long MrTime = 1;
        //if(jattConfig.plugintimer().equals(TimerOptions.MIN_5))
            MrTime = random(300000, 300002) + System.currentTimeMillis(); // 5 mins
        //else
        //    MrTime =  random(1800000,2760000) + System.currentTimeMillis(); // 30-46 mins

        long durationInMillis = MrTime - System.currentTimeMillis();

        long hours = (durationInMillis / (1000 * 60 * 60)) % 24;
        long minutes = (durationInMillis / (1000 * 60)) % 60;
        long seconds = (durationInMillis / 1000) % 60;

        System.out.println("Plug in will run for " + hours + " hrs " + minutes + " mins " + seconds + " secs");
        return MrTime;
    }
}

