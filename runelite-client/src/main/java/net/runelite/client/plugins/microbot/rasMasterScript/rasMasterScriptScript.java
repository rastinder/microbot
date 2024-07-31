package net.runelite.client.plugins.microbot.rasMasterScript;

import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.tutorialisland.TutorialIslandScript;
import net.runelite.client.plugins.microbot.tutorialisland.TutorialislandPlugin;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;

public class rasMasterScriptScript extends Script {
    public static double version = 1.0;
    private long startTime = 0;
    private long endTime = System.currentTimeMillis();
    private long totalTime = endTime - startTime;
    private static String activity = null;
    private static boolean alreadyOn = false;
    private String currentPluginName;
    private long pluginStartTime = 0;
    private long lastPrintTime = 0;

    public boolean run(rasMasterScriptConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                logF2p();
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;

                startTime = System.currentTimeMillis();

                if (activity == null) {
                    findActivity();
                }
                if (activity.equals("running")) {
                    monitorRunningPlugin();
                    printtimeplugin();
                } else {
                    executeActivity();
                }

                endTime = System.currentTimeMillis();
                totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);
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
            alreadyOn = false;
            activity = "selectRandom";
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
        switch (activity) {
            case "tutIsland":
                tutIsland();
                break;
            case "firsttimecheck":
                firstTimeCheck();
                break;
            case "moneymaking":
                moneymaking();
                break;
            case "skill":
                skilling();
                break;
            case "randomtimewaste":
                randomTimeWaste();
                break;
            case "quest":
                questing();
                break;
            case "combat":
                combatTrain();
                break;
            case "stackedActivity":
                stackedActivity();
                break;
            case "selectRandom":
                selectRandomActivity();
                break;
            case "mostProfitableMoneyMaking":
                mostProfitableMoneyMaking();
                break;
            case "login":
                logF2p();
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
                try{
                    if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3236,3220,0)) < 20)
                        stopPlugin("TutorialIsland");
                }
                catch (Exception e){
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

    private boolean checkTutIslandStatus() {
        int varbitValue = Microbot.getVarbitPlayerValue(281);
        return Rs2Widget.getWidget(558, 1) != null || Rs2Widget.getWidget(679, 1) != null || varbitValue < 10 ||
                (varbitValue >= 10 && varbitValue < 120) || (varbitValue >= 120 && varbitValue < 200) ||
                (varbitValue >= 200 && varbitValue <= 250) || (varbitValue >= 260 && varbitValue <= 360) ||
                (varbitValue > 360 && varbitValue < 510) || (varbitValue >= 510 && varbitValue < 540) ||
                (varbitValue >= 540 && varbitValue < 610) || (varbitValue >= 610 && varbitValue < 1000);
    }

    private void firstTimeCheck() {
        managePlugin(true,"firsttimecheck", "ras firsttime check", null);
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

    private void moneymaking() {
        String pluginName = null;
        List<String> trainSkill = Arrays.asList("Prayer");
        if (random(0, 1) == 0)
            trainSkill = Arrays.asList("ras HardLeather", "Ras Jewellery Maker", "ras_high alc", "pie shell", "Reddie", "Collecting ruby rings", "uncooked apple pies", "pastry dough", "Collecting fish food", "Smelting steel bars", "Cuttinglogs", "CollectingGroundItems");
        else
            trainSkill = Arrays.asList("Strength", "Range", "Prayer", "Magic", "Runecraft", "Crafting", "Mining", "Smithing", "Fishing", "Cooking", "Firemaking", "Woodcutting");
        Collections.shuffle(trainSkill);
        for (String skill : trainSkill) {
            switch (skill) {
                case "ras HardLeather":
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
                    /*
                case "":
                    pluginName = ();
                    break;
                case "":
                    pluginName = ();
                    break;
                case "":
                    pluginName = ();
                    break;

                     */
            }
            // Check if the plugin is still enabled before proceeding to the next skill
            if (pluginName != null && alreadyOn && isPlugEnabled(pluginName)) {
                return;
            }
            //activity = "selectRandom";
        }
    }

    private String PieShell() {
        return null;
    }

    private String RedDie() {
        return null;
    }

    private String CollectingRubyRings() {
        return null;
    }

    private String PastryDough() {
        return null;
    }

    private String CollectingFish() {
        return null;
    }

    private String Smeltingsteelbars() {
        return null;
    }

    private String UncookedApplePies() {
        return null;
    }

    private String Cuttinglogs() {
        return null;
    }

    private String CollectingGroundItems() {
        return null;
    }

    private String RasHighCal() {
        return null;
    }

    private String RasJewelleryMaker() {
        return null;
    }

    private String HardLeather() {
        return  startPlugin("ras HardLeather");
    }

    private void skilling() {
        String pluginName = null;
        List<String> trainSkill = Arrays.asList("Prayer");
        if (random(0,2) == 0)
            trainSkill = Arrays.asList("FishCookPrayer", "FishCook", "MiningSmithing", "WoodcuttingFiremaking", "RunecraftingMining", "MagicRunecrafting", "PrayerCombat", "MiningCrafting", "FishingCookingWoodcuttingFiremaking", "SmithingCrafting","RunecraftingMagicCombat","MiningSmithingCrafting","MagicSmith","CraftingWoodcutting");
        else
            trainSkill = Arrays.asList("Strength","Range","Prayer", "Magic", "Runecraft", "Crafting", "Mining", "Smithing", "Fishing", "Cooking", "Firemaking", "Woodcutting");
        Collections.shuffle(trainSkill);
        for (String skill : trainSkill) {
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
                    /*
                case "FishCook":
                    pluginName =trainFishCook();
                    break;
                case "MiningSmithing":
                    pluginName =trainMiningSmithing();
                    break;
                case "WoodcuttingFiremaking":
                    pluginName =trainWoodcuttingFiremaking();
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
                case "Fishing":
                    pluginName = trainFishing();
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
            // Check if the plugin is still enabled before proceeding to the next skill
            if (pluginName != null && alreadyOn && isPlugEnabled(pluginName)) {
                return;
            }
        }
    }

    private String trainMagic() {
        return startPlugin("rasMagicTrain");
    }

    private String trainFishCookPrayer() {
        activity = "selectRandom";
        return activity;
    }

    private String trainPrayer() {
        activity = "selectRandom";
        return activity;
    }

    private String trainFishing() {
        bankAllAndGet("net");
        Microbot.getPluginManager().setPluginValue("general","UseBank", true);
        return startPlugin("Auto fishing");
    }

    private void combatTrain() {
        if (Microbot.getClient().getRealSkillLevel(Skill.STRENGTH) > 39 || Microbot.getClient().getRealSkillLevel(Skill.ATTACK) > 39 || Microbot.getClient().getRealSkillLevel(Skill.DEFENCE) > 19 )
            startPlugin("ras CombatTrain");
        else
            startPlugin("ras range bone collectoer");
        System.out.println("Combat training activity not implemented yet.");
        activity = "selectRandom";
    }

    private void questing() {
        System.out.println("Questing activity not implemented yet.");
        activity = "selectRandom";
    }

    private void randomTimeWaste() {
        System.out.println("Random time wasting activity not implemented yet.");
        activity = "selectRandom";
    }

    private void stackedActivity() {
        System.out.println("Stacked activity not implemented yet.");
        activity = "selectRandom";
    }

    private void logF2p() {
        if (Microbot.getClient().getGameState() == GameState.LOGIN_SCREEN) {
            new Login(Login.getRandomWorld(false));
            sleep(5000);
        }
    }

    private void printtimeplugin(){
        if (System.currentTimeMillis()- lastPrintTime >= 5000) {
            long elapsedTime = System.currentTimeMillis() - pluginStartTime;
            LocalTime timeElapsed = LocalTime.ofSecondOfDay(elapsedTime / 1000);
            String formattedTime = timeElapsed.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            System.out.println("Time passed: " + formattedTime);
            lastPrintTime = System.currentTimeMillis();
        }
    }
    private void managePlugin(Boolean status,String currentActivity, String pluginName, String nextActivity) {
        if (isPlugEnabled(pluginName)) {
            if (currentActivity.equals("tutIsland") && !checkTutIslandStatus()) {
                stopPlugin(pluginName);
                alreadyOn = false;
                activity = nextActivity != null ? nextActivity : "selectRandom";
                if (activity.equals("firsttimecheck")) {
                    firstTimeCheck();
                }
            }
        } else if (status){
            startPlugin(pluginName);
            alreadyOn = true;
            activity = currentActivity;
        }
    }
    public boolean isPlugEnabled(String pluginName){
        try {
            return  Microbot.getPluginManager().isPluginEnabled(getPluginByName(pluginName));
        }
        catch (Exception x){
            return false;
        }
    }
    public String startPlugin(String pluginName) {
        try {
            Microbot.getPluginManager().setPluginEnabled(getPluginByName(pluginName),true);
            sleep(100);
            Microbot.getPluginManager().startPlugins();
            currentPluginName = pluginName;
            activity = "running";
            pluginStartTime = System.currentTimeMillis();
            return pluginName;
        } catch (Exception e) {
            System.out.println("Failed to start plugin: " + e.getMessage());
            return null;
        }
    }

    public void stopPlugin(String pluginName) {
        try {
            Microbot.getPluginManager().setPluginEnabled(getPluginByName(pluginName),false);
            sleep(100);
            Microbot.getPluginManager().startPlugins();

        } catch (Exception e) {
            System.out.println("Failed to stop plugin: " + e.getMessage());
        }
    }
    public Plugin getPluginByName(String pluginName) {
        for (Plugin plugin : Microbot.getPluginManager().getPlugins()) {
            PluginDescriptor descriptor = plugin.getClass().getAnnotation(PluginDescriptor.class);
            if (descriptor != null && descriptor.name().contains(pluginName)) {
                System.out.println("plugin enabled: " + descriptor.name());
                return plugin;
            }
        }
        return null;
    }
    public void bankAllAndGet(String... items){
        Rs2Bank.walkToBank();
        sleepUntilTrue(()->Rs2Bank.isNearBank(10), 100, 50000);
        Rs2Bank.openBank();
        if(!Rs2Inventory.isEmpty()) {
            Rs2Bank.depositAll();
            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
        }
        for (String item :items){
            Rs2Bank.withdrawX(item,1);
            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
        }
    }
}

