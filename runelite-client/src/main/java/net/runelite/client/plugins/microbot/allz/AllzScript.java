package net.runelite.client.plugins.microbot.allz;

import lombok.SneakyThrows;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.geHandler.geHandlerScript;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcManager;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcStats;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2MiniMap;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.questhelper.QuestHelperPlugin;
import net.runelite.client.plugins.questhelper.questhelpers.QuestHelper;
import net.runelite.client.plugins.questhelper.questinfo.QuestHelperQuest;
import net.runelite.client.plugins.questhelper.managers.QuestManager;
import net.runelite.client.plugins.questhelper.questhelpers.QuestDetails;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;
import  net.runelite.client.plugins.skillcalculator.skills.MagicAction.*;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static net.runelite.client.plugins.microbot.quest.MQuestScript.ItemQuantity;
import static net.runelite.client.plugins.microbot.quest.MQuestScript.goBuyAndReturn;
import static net.runelite.client.plugins.microbot.quest.MQuestScript.isWithinXDistanceOfPlayer;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject.*;
import static net.runelite.client.plugins.microbot.util.reflection.Rs2Reflection.getObjectByName;

public class AllzScript extends Script {
    public static double version = 1.0;
    public static final List<QuestHelperQuest> F2P_QUESTS = Arrays.asList(
            QuestHelperQuest.COOKS_ASSISTANT,
            QuestHelperQuest.DORICS_QUEST,
            QuestHelperQuest.ERNEST_THE_CHICKEN,
            QuestHelperQuest.GOBLIN_DIPLOMACY,
            QuestHelperQuest.IMP_CATCHER,
            QuestHelperQuest.PIRATES_TREASURE,
            QuestHelperQuest.PRINCE_ALI_RESCUE,
            QuestHelperQuest.THE_RESTLESS_GHOST,
            QuestHelperQuest.ROMEO__JULIET,
            QuestHelperQuest.RUNE_MYSTERIES,
            QuestHelperQuest.SHEEP_SHEARER,
            QuestHelperQuest.WITCHS_POTION,
            QuestHelperQuest.X_MARKS_THE_SPOT,
            QuestHelperQuest.BELOW_ICE_MOUNTAIN
    );

    private final PluginManager pluginManager;

    @Inject
    public AllzScript(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }
    public int getQuestPoints() {
        return Microbot.getClientThread().runOnClientThread(() -> {
            return Microbot.getClient().getVarpValue(VarPlayer.QUEST_POINTS);
        });
    }
    public boolean run(AllzConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn() || !super.run()) return;

                // Enable QuestHelper plugin on client thread
                pluginssss("QuestHelperPlugin", true);
                sleep(2000);
                // Get QuestHelperPlugin and QuestManager
                QuestHelperPlugin questHelperPlugin = getQuestHelperPlugin();
                if (questHelperPlugin == null) {
                    System.out.println("QuestHelperPlugin not found.");
                    return;
                }
                QuestManager questManager = questHelperPlugin.getQuestManager();
                if (questManager == null) {
                    System.out.println("QuestManager not found.");
                    return;
                }

                // Select and start a random quest
                QuestHelperQuest selectedQuest = getRandomQuest();
                if (selectedQuest == null) {
                    System.out.println("No available F2P quests to start.");
                    pluginssss("AllzPlugin", false);
                    sleep(1000);
                    pluginssss("MQuestPlugin", false);
                    sleep(1000);
                    return;
                }
                System.out.println("Attempting to start quest: " + selectedQuest.getName());
                callQuestFunction(selectedQuest.getName(), questManager);
                System.out.println("Quest function called successfully.");

                // Wait for the quest to be selected by QuestHelper
                int attempts = 0;
                while (questHelperPlugin.getSelectedQuest() == null && attempts < 4) {
                    sleep(1000);
                    attempts++;
                }
                pluginssss("AllzPlugin", false);
                // Check if quest was successfully selected
                if (questHelperPlugin.getSelectedQuest() == null) {
                    System.out.println("No quest was selected after multiple attempts.");
                    return;
                }

                // Print the selected quest name
                QuestHelper selectedQuestHelper = questHelperPlugin.getSelectedQuest();
                String questName = selectedQuestHelper.getQuest().getName();
                System.out.println("Selected Quest: " + questName);

                // Wait for the quest to complete
                while (!getQuestHelperPlugin().getSelectedQuest().isCompleted()) {
                    System.out.println("Waiting for quest to complete...");
                    sleep(1000);
                }
                System.out.println(questName + " quest completed!");

                // Disable QuestHelper plugin
                pluginssss("QuestHelperPlugin", false);

            } catch (Exception ex) {
                System.out.println("Error in scheduled task: " + ex.getMessage());
                ex.printStackTrace();
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    public void stoppPlugin(String pluginName) {
        try {
            Microbot.getPluginManager().setPluginEnabled(getPluginByName(pluginName), false);
            sleep(1000);
            Microbot.getPluginManager().stopPlugin(getPluginByName(pluginName));
            sleep(1000);
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

    private QuestHelperQuest getRandomQuest() {
        List<QuestHelperQuest> availableQuests = new ArrayList<>();
        for (QuestHelperQuest quest : F2P_QUESTS) {
            if (!checkQuestCompletion(quest) && checkQuestRequirements(quest)) {
                availableQuests.add(quest);
            }
        }
        if (availableQuests.isEmpty()) {
            return null;
        }
        Random random = new Random();
        int index = random.nextInt(availableQuests.size());
        return availableQuests.get(index);
    }
    private boolean checkQuestRequirements(QuestHelperQuest quest) {
        switch (quest) {
            case BELOW_ICE_MOUNTAIN:
                return getQuestPoints() >= 16;
            // Add other quest requirements here
            default:
                return true;
        }
    }
    public static List<String> getF2PQuests() {
        List<String> f2pQuests = new ArrayList<>();
        for (QuestHelperQuest quest : QuestHelperQuest.values()) {
            if (quest.getQuestType() == QuestDetails.Type.F2P) {
                f2pQuests.add(quest.getName());
            }
        }
        return f2pQuests;
    }

    private static final Map<String, Consumer<QuestManager>> questHandlers = new HashMap<>();

    {
        questHandlers.put("Cook's Assistant", this::handleCooksAssistant);
        questHandlers.put("Doric's Quest", this::handleDoricsQuest);
        questHandlers.put("Ernest the Chicken", this::handleErnestTheChicken);
        questHandlers.put("Goblin Diplomacy", this::handleGoblinDiplomacy);
        questHandlers.put("Imp Catcher", this::handleImpCatcher);
        questHandlers.put("Pirate's Treasure", this::handlePiratesTreasure);
        questHandlers.put("Prince Ali Rescue", this::handlePrinceAliRescue);
        questHandlers.put("The Restless Ghost", this::handleRestlessGhost);
        questHandlers.put("Romeo & Juliet", this::handleRomeoJuliet);
        questHandlers.put("Rune Mysteries", this::handleXRuneMysteries);
        questHandlers.put("Sheep Shearer", this::handleSheepShearer);
        questHandlers.put("Witch's Potion", this::handleWitchsPotion);
        questHandlers.put("X Marks the Spot", this::handleXMarksTheSpot);
        questHandlers.put("Below Ice Mountain", this::handleBelowIceMountain);
    }

    private static void callQuestFunction(String quest, QuestManager questManager) {
        Consumer<QuestManager> handler = questHandlers.get(quest);
        if (handler != null) {
            handler.accept(questManager);
        } else {
            System.out.println("Unknown quest: " + quest);
        }
    }
    private void pluginssss(String name, boolean dothis) {
        try {
            pluginManager.controlPlugin(name, dothis);
        } catch (Exception e) {
            System.out.println("hihi");
        }
    }

    private void gotoBankAndDepositall() {
        while(!Rs2Bank.isNearBank(6)) {
            Rs2Bank.walkToBank();
            sleepUntilTrue(() -> Rs2Bank.isNearBank(10), 100, 150000);
        }
        while(!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            sleepUntilTrue(() -> Rs2Bank.isOpen(), 100, 5000);
        }
        if (!Rs2Inventory.isEmpty()) {
            Rs2Bank.depositAll();
            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
        }
        while (!Rs2Inventory.isEmpty()) {
            if (Rs2Inventory.contains("Antique lamp")){
                Rs2Bank.closeBank();
                sleep(1000);
                while (Rs2Bank.isOpen()) {
                    sleep(1000);
                    Rs2Bank.closeBank();
                }
                Rs2Inventory.interact("Antique lamp","Rub");
                sleep(1000);
                Widget sdv= Rs2Widget.getWidget(240,8).getDynamicChildren()[9];
                Microbot.click(sdv.getBounds());
                sleep(1000);
                Rs2Widget.clickWidget("Confirm: Prayer");
                sleep(2000);
                break;
            }
        }
    }
    private void checkObjectType(int objectId) {
        Tile[][][] tiles = Microbot.getClient().getScene().getTiles();
        for (int z = 0; z < tiles.length; z++) {
            for (int x = 0; x < tiles[z].length; x++) {
                for (int y = 0; y < tiles[z][x].length; y++) {
                    Tile tile = tiles[z][x][y];
                    if (tile != null) {
                        for (TileObject tileObject : tile.getGameObjects()) {
                            if (tileObject != null && tileObject.getId() == objectId) {
                                if (tileObject instanceof GameObject) {
                                    System.out.println("Object is of type GameObject");
                                } else if (tileObject instanceof GroundObject) {
                                    System.out.println("Object is of type GroundObject");
                                } else if (tileObject instanceof WallObject) {
                                    System.out.println("Object is of type WallObject");
                                } else {
                                    System.out.println("Object is of unknown type");
                                }
                                return;
                            }
                        }
                        if (tile.getGroundObject() != null && tile.getGroundObject().getId() == objectId) {
                            System.out.println("Object is of type GroundObject");
                            return;
                        }
                        if (tile.getWallObject() != null && tile.getWallObject().getId() == objectId) {
                            System.out.println("Object is of type WallObject");
                            return;
                        }
                        if (tile.getDecorativeObject() != null && tile.getDecorativeObject().getId() == objectId) {
                            System.out.println("Object is of type DecorativeObject");
                            return;
                        }
                    }
                }
            }
        }
        System.out.println("Object not found");
    }

    public WorldPoint[] getStructuralPillarLocations() {
        List<TileObject> allObjects = Rs2GameObject.getAll();
        List<WorldPoint> locations = new ArrayList<>();
        for (TileObject object : allObjects) {
            ObjectComposition objComp = Rs2GameObject.getObjectComposition(object.getId());
            if (objComp != null && objComp.getName().equalsIgnoreCase("Structural pillar")) {
                locations.add(object.getWorldLocation());
            }
        }
        return locations.toArray(new WorldPoint[0]);
    }


    private void handleBelowIceMountain (QuestManager questManager) {
        gotoBankAndDepositall();
        Rs2Bank.withdrawX(995,1000);
        sleep(1000);
        boolean haspickAxe = Rs2Inventory.get("pickaxe", false) != null;
        if (!haspickAxe){
            Rs2Bank.walkToBank();
            while (!Rs2Bank.isNearBank(8)) {
                Rs2Walker.setTarget(Rs2Bank.getNearestBank().getWorldPoint());
                Rs2Walker.walkTo(Rs2Bank.getNearestBank().getWorldPoint(), 5);
                boolean result = sleepUntilTrue(Rs2Player::isAnimating, 100, 5000);
                if (!result) return;
                sleepUntilTrue(() -> !Rs2Player.isAnimating(),100,280000);
            }
            Rs2Bank.openBank();
            boolean  hasAxe1 = Rs2Inventory.get("pickaxe", false) != null;
            while (!hasAxe1) {
                Rs2Bank.withdrawItem("pickaxe");
                sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                hasAxe1 = Rs2Inventory.get("pickaxe", false) != null;
            }
            Rs2Bank.closeBank();
        }
        String[] itemNames = {
                "Beer","Beer",  "Knife","Cooked meat","Bread"
        };
        int[] quantities = {1,1,1,1,1};
        geHandlerScript.goBuyAndReturn(quantities, 5, itemNames);

        if(Rs2Player.getRealSkillLevel(Skill.MINING) < 10){
            Rs2Walker.walkTo(new WorldPoint(3283, 3366, 0), 0);
            while (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3283, 3366, 0)) > 10) {
                Rs2Walker.setTarget(new WorldPoint(3283, 3366, 0));
                Rs2Walker.walkTo(new WorldPoint(3283, 3366, 0), 5);
                sleepTillAnimationStop();
            }
            while(Rs2Player.getRealSkillLevel(Skill.MINING) < 10){
                if (Rs2Inventory.isFull()){
                    Rs2Inventory.dropAll("Copper ore");
                }
                Rs2GameObject.interact("Copper rocks");
                sleepTillAnimationStop();
                //while(Rs2Player.isAnimating() || Rs2Player.isInteracting() || Rs2Player.isMoving() || Rs2Player.isWalking()){
               //     sleep(1000);
                //}
            }

        }
        Rs2Inventory.dropAll("Copper ore");
        System.out.println("Handling Below Ice Mountain quest...");
        QuestHelperQuest BELOW_ICE_MOUNTAIN = QuestHelperQuest.BELOW_ICE_MOUNTAIN;
        QuestHelper questHelper = BELOW_ICE_MOUNTAIN.getQuestHelper();
        Microbot.getClientThread().invokeLater(() -> questManager.startUpQuest(questHelper, true));
        pluginssss("MQuestPlugin", true);
        // Add logic for handling Below Ice Mountain quest here
        while (!checkQuestCompletion(QuestHelperQuest.BELOW_ICE_MOUNTAIN)) {
            System.out.println("Waiting for quest to complete...");
            if (Rs2Inventory.contains("Bronze pickaxe")){
                Rs2Inventory.interact("Bronze pickaxe","Wield");
                sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
            }
            if(isWithinXDistanceOfPlayer(new WorldPoint(2951, 5771, 0),10)){
                Rs2Tab.switchToCombatOptionsTab();
                sleep(500);
                Rs2Combat.setAutoRetaliate(false);
                sleep(1000);
                while(Rs2Npc.getNpc(10654)!=null && !Rs2Npc.getNpc(10654).isDead()){
                    WorldPoint[] points = getStructuralPillarLocations();
                    for (WorldPoint point : points) {
                        System.out.println("Mining at 1: " + Rs2GameObject.interact(point, "Mine"));
                        Rs2GameObject.interact(point, "Mine");
                        int currentXP = Microbot.getClient().getSkillExperience(Skill.MINING);
                        while (currentXP == Microbot.getClient().getSkillExperience(Skill.MINING)) {
                            Rs2Player.eatAt(60);
                            System.out.println("Mining at 2: " + Rs2GameObject.interact(point, "Mine"));
                            Rs2GameObject.interact(point, "Mine");
                            sleep(200);
                            while (Rs2Player.isAnimating() || Rs2Player.isInteracting()){
                                sleep(100);
                            }

                        }
                    }
                }
            }
        }
        Rs2Tab.switchToCombatOptionsTab();
        sleep(500);
        Rs2Combat.setAutoRetaliate(true);
        sleep(1000);
        pluginssss("MQuestPlugin", false);
        sleep(1000);
        shutdown();
        System.out.println("Below Ice Mountain quest completed!");
    }
    private void handleCooksAssistant(QuestManager questManager) {
        System.out.println("Handling Cook's Assistant quest...");
        gotoBankAndDepositall();
        QuestHelperQuest COOKS_ASSISTANT = QuestHelperQuest.COOKS_ASSISTANT;
        QuestHelper questHelper = COOKS_ASSISTANT.getQuestHelper();
        Microbot.getClientThread().invokeLater(() -> questManager.startUpQuest(questHelper, true));
        sleep(2000);
        // Add logic for handling Cook's Assistant quest here
        pluginssss("MQuestPlugin", true);
        sleep(1000);
        while (!checkQuestCompletion(QuestHelperQuest.COOKS_ASSISTANT)) {
            System.out.println("Waiting for quest to complete...");
            sleep(1000);
        }
        pluginssss("MQuestPlugin", false);
        sleep(1000);
        shutdown();
        System.out.println("Cook's Assistant quest completed!");
    }



    private void handleDoricsQuest(QuestManager questManager) {
        System.out.println("Handling Doric's Quest quest...");
        gotoBankAndDepositall();
        QuestHelperQuest DORICS_QUEST = QuestHelperQuest.DORICS_QUEST;
        QuestHelper questHelper = DORICS_QUEST.getQuestHelper();
        Microbot.getClientThread().invokeLater(() -> questManager.startUpQuest(questHelper, true));
        sleep(2000);
        pluginssss("MQuestPlugin", true);
        // Add logic for handling Doric's Quest here
        while (!checkQuestCompletion(QuestHelperQuest.DORICS_QUEST)) {
            System.out.println("Waiting for quest to complete...");
            sleep(1000);
        }
        pluginssss("MQuestPlugin", false);
        sleep(1000);
        shutdown();
        System.out.println("Doric's Quest quest completed!");
    }



    private void handleErnestTheChicken(QuestManager questManager) {
        System.out.println("Handling Ernest the Chicken quest...");
        gotoBankAndDepositall();
        QuestHelperQuest ERNEST_THE_CHICKEN = QuestHelperQuest.ERNEST_THE_CHICKEN;
        QuestHelper questHelper = ERNEST_THE_CHICKEN.getQuestHelper();
        Microbot.getClientThread().invokeLater(() -> questManager.startUpQuest(questHelper, true));
        sleep(2000);
        pluginssss("MQuestPlugin", true);
        // Add logic for handling Ernest the Chicken quest here
        while (!checkQuestCompletion(QuestHelperQuest.ERNEST_THE_CHICKEN)) {
            System.out.println("Waiting for quest to complete...");
            sleep(1000);
        }
        pluginssss("MQuestPlugin", false);
        sleep(1000);
        shutdown();
        System.out.println("Ernest the Chicken quest completed!");
    }

    private void handleGoblinDiplomacy(QuestManager questManager) {
        gotoBankAndDepositall();
        //  Rs2Bank.withdrawX(995, 15000-(int)ItemQuantity(995));
        Rs2Bank.withdrawX(995,500);
        sleep(1000);
        while (Rs2Bank.isOpen()) {
            sleep(1000);
            Rs2Bank.closeBank();
        }
        System.out.println("Handling Goblin Diplomacy quest...");
        QuestHelperQuest GOBLIN_DIPLOMACY = QuestHelperQuest.GOBLIN_DIPLOMACY;
        QuestHelper questHelper = GOBLIN_DIPLOMACY.getQuestHelper();
        Microbot.getClientThread().invokeLater(() -> questManager.startUpQuest(questHelper, true));
        sleep(2000);
        pluginssss("MQuestPlugin", true);
        // Add logic for handling Goblin Diplomacy quest here
        while (!checkQuestCompletion(QuestHelperQuest.GOBLIN_DIPLOMACY)) {
            System.out.println("Waiting for quest to complete...");
            sleep(1000);
        }
        pluginssss("MQuestPlugin", false);
        sleep(1000);
        shutdown();
        System.out.println("Goblin Diplomacy quest completed!");
    }

    private void handleImpCatcher(QuestManager questManager) {
        gotoBankAndDepositall();
        Rs2Bank.withdrawX(995, 15000-(int)ItemQuantity(995));
        //    Rs2Bank.withdrawX(995,500);
        sleep(1000);
        while (Rs2Bank.isOpen()) {
            sleep(1000);
            Rs2Bank.closeBank();
        }
        System.out.println("Handling Imp Catcher quest...");
        QuestHelperQuest IMP_CATCHER = QuestHelperQuest.IMP_CATCHER;
        QuestHelper questHelper = IMP_CATCHER.getQuestHelper();
        Microbot.getClientThread().invokeLater(() -> questManager.startUpQuest(questHelper, true));
        sleep(2000);
        pluginssss("MQuestPlugin", true);
        // Add logic for handling Imp Catcher quest here
        while (!checkQuestCompletion(QuestHelperQuest.IMP_CATCHER)) {
            System.out.println("Waiting for quest to complete...");
            sleep(1000);
        }
        if(checkQuestCompletion(QuestHelperQuest.IMP_CATCHER)){
            Rs2Tab.switchToMagicTab();
            sleep(1000);
            Rs2Widget.clickWidget("<col=00ff00>Lumbridge Home Teleport</col>");
            sleep(4000);
            while(Rs2Player.isAnimating() || Rs2Player.isInteracting() || Rs2Player.isMoving()){
                sleep(1000);
            }
            pluginssss("MQuestPlugin", false);
            sleep(1000);
            shutdown();
            pluginssss("AllzPlugin", false);
            sleep(1000);

            return;
        }
        pluginssss("MQuestPlugin", false);
        sleep(1000);
        shutdown();
        System.out.println("Imp Catcher quest completed!");
    }



    private void handlePiratesTreasure(QuestManager questManager) {
        gotoBankAndDepositall();
        Rs2Bank.withdrawX(995,500);
        sleep(1000);
        while (Rs2Bank.isOpen()) {
            sleep(1000);
            Rs2Bank.closeBank();
        }
        System.out.println("Handling Pirate's Treasure quest...");
        QuestHelperQuest PIRATES_TREASURE = QuestHelperQuest.PIRATES_TREASURE;
        QuestHelper questHelper = PIRATES_TREASURE.getQuestHelper();
        Microbot.getClientThread().invokeLater(() -> questManager.startUpQuest(questHelper, true));
        sleep(2000);
        pluginssss("MQuestPlugin", true);
        // Add logic for handling Pirate's Treasure quest here
        while (!checkQuestCompletion(QuestHelperQuest.PIRATES_TREASURE)) {
            System.out.println("Waiting for quest to complete...");
            sleep(1000);
        }
        pluginssss("MQuestPlugin", false);
        sleep(1000);
        shutdown();
        System.out.println("Pirate's Treasure quest completed!");
    }

    private void handlePrinceAliRescue(QuestManager questManager) {
        gotoBankAndDepositall();
        Rs2Bank.withdrawX(995,3500);
        sleep(1000);
        while (Rs2Bank.isOpen()) {
            sleep(1000);
            Rs2Bank.closeBank();
        }
        System.out.println("Handling Prince Ali Rescue quest...");
        QuestHelperQuest PRINCE_ALI_RESCUE = QuestHelperQuest.PRINCE_ALI_RESCUE;
        QuestHelper questHelper = PRINCE_ALI_RESCUE.getQuestHelper();
        Microbot.getClientThread().invokeLater(() -> questManager.startUpQuest(questHelper, true));
        sleep(2000);
        pluginssss("MQuestPlugin", true);
        // Add logic for handling Prince Ali Rescue quest here
        while (!checkQuestCompletion(QuestHelperQuest.PRINCE_ALI_RESCUE)) {
            System.out.println("Waiting for quest to complete...");
            sleep(1000);
        }
        pluginssss("MQuestPlugin", false);
        sleep(1000);
        shutdown();
        System.out.println("Prince Ali Rescue quest completed!");
    }

    private void handleRestlessGhost(QuestManager questManager) {
        gotoBankAndDepositall();

        System.out.println("Handling Restless Ghost quest...");
        QuestHelperQuest RESTLESS_GHOST = QuestHelperQuest.THE_RESTLESS_GHOST;
        QuestHelper questHelper = RESTLESS_GHOST.getQuestHelper();
        Microbot.getClientThread().invokeLater(() -> questManager.startUpQuest(questHelper, true));
        sleep(2000);
        pluginssss("MQuestPlugin", true);
        // Add logic for handling Restless Ghost quest here
        while (!checkQuestCompletion(QuestHelperQuest.THE_RESTLESS_GHOST)) {
            System.out.println("Waiting for quest to complete...");
            sleep(1000);
        }
        pluginssss("MQuestPlugin", false);
        sleep(1000);
        shutdown();
        System.out.println("Restless Ghost quest completed!");
    }

    private void handleRomeoJuliet(QuestManager questManager) {
        gotoBankAndDepositall();
        System.out.println("Handling Romeo & Juliet quest...");
        QuestHelperQuest ROMEO__JULIET = QuestHelperQuest.ROMEO__JULIET;
        QuestHelper questHelper = ROMEO__JULIET.getQuestHelper();
        Microbot.getClientThread().invokeLater(() -> questManager.startUpQuest(questHelper, true));
        sleep(2000);
        pluginssss("MQuestPlugin", true);
        // Add logic for handling Romeo & Juliet quest here
        while (!checkQuestCompletion(QuestHelperQuest.ROMEO__JULIET)) {
            System.out.println("Waiting for quest to complete...");
            sleep(1000);
        }
        pluginssss("MQuestPlugin", false);
        sleep(1000);
        shutdown();
        System.out.println("Romeo & Juliet quest completed!");
    }

    public boolean reachtoduek = false;
    private void handleXRuneMysteries(QuestManager questManager) {
        gotoBankAndDepositall();
        if(checkQuestCompletion(QuestHelperQuest.RUNE_MYSTERIES)){
            return;
        }
        System.out.println("Handling Rune Mysteries quest...");
        QuestHelperQuest RUNE_MYSTERIES = QuestHelperQuest.RUNE_MYSTERIES;
        QuestHelper questHelper = RUNE_MYSTERIES.getQuestHelper();
        Microbot.getClientThread().invokeLater(() -> questManager.startUpQuest(questHelper, true));
        sleep(2000);
        pluginssss("MQuestPlugin", true);
        sleep(2000);
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "RUNE_MYSTERIES")) && ("Talk to Duke Horacio on the first floor of Lumbridge castle.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))  && !reachtoduek){
            Rs2Walker.walkTo(new WorldPoint(3198,3212,0),6);
            while (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3198,3212,0))>6){
                Rs2Walker.setTarget(new WorldPoint(3198,3212,0));
                Rs2Walker.walkTo(new WorldPoint(3198,3212,0),5);
                sleepTillAnimationStop();
            }
            reachtoduek=true;
        }
        while(!checkQuestCompletion(QuestHelperQuest.RUNE_MYSTERIES)) {
            sleep(3000);
        }
        if(checkQuestCompletion(QuestHelperQuest.RUNE_MYSTERIES)){
            Rs2Tab.switchToMagicTab();
            sleep(1000);
            Rs2Widget.clickWidget("<col=00ff00>Lumbridge Home Teleport</col>");
            sleep(4000);
            while(Rs2Player.isAnimating() || Rs2Player.isInteracting() || Rs2Player.isMoving()){
                sleep(1000);
            }
            pluginssss("MQuestPlugin", false);
            sleep(1000);
            shutdown();
            pluginssss("AllzPlugin", false);
            sleep(1000);

            return;
        }

    }

    private void handleSheepShearer(QuestManager questManager) {
        gotoBankAndDepositall();
        System.out.println("Handling Sheep Shearer quest...");
        QuestHelperQuest SHEEP_SHEARER = QuestHelperQuest.SHEEP_SHEARER;
        QuestHelper questHelper = SHEEP_SHEARER.getQuestHelper();
        Microbot.getClientThread().invokeLater(() -> questManager.startUpQuest(questHelper, true));
        sleep(2000);
        pluginssss("MQuestPlugin", true);
        // Add logic for handling Sheep Shearer quest here
        while (!checkQuestCompletion(QuestHelperQuest.SHEEP_SHEARER)) {
            System.out.println("Waiting for quest to complete...");
            sleep(1000);
        }
        pluginssss("MQuestPlugin", false);
        sleep(1000);
        System.out.println("Sheep Shearer quest completed!");
        shutdown();
    }

    private void handleWitchsPotion(QuestManager questManager) {
        gotoBankAndDepositall();
        Rs2Bank.withdrawX(995,500);
        sleep(1000);
        while (Rs2Bank.isOpen()) {
            Rs2Bank.closeBank();
            sleep(1000);
        }
        System.out.println("Handling Witch's Potion quest...");
        QuestHelperQuest WITCHS_POTION = QuestHelperQuest.WITCHS_POTION;
        QuestHelper questHelper = WITCHS_POTION.getQuestHelper();
        Microbot.getClientThread().invokeLater(() -> questManager.startUpQuest(questHelper, true));
        sleep(2000);
        pluginssss("MQuestPlugin", true);
        // Add logic for handling Witch's Potion quest here
        while (!checkQuestCompletion(QuestHelperQuest.WITCHS_POTION)) {
            System.out.println("Waiting for quest to complete...");
            sleep(1000);
        }
        pluginssss("MQuestPlugin", false);
        sleep(1000);
        System.out.println("Witch's Potion the Spot quest completed!");
        shutdown();
    }

    private void handleXMarksTheSpot(QuestManager questManager) {
        gotoBankAndDepositall();
        System.out.println("Handling X Marks the Spot quest...");
        QuestHelperQuest X_MARKS_THE_SPOT = QuestHelperQuest.X_MARKS_THE_SPOT;
        QuestHelper questHelper = X_MARKS_THE_SPOT.getQuestHelper();
        Microbot.getClientThread().invokeLater(() -> questManager.startUpQuest(questHelper, true));
        sleep(2000);
        pluginssss("MQuestPlugin", true);
        sleep(2000);
        while(!Rs2Inventory.contains("Spade")) {
            Rs2Bank.walkToBank();
            sleepTillAnimationStop();
            while (!Rs2Bank.isNearBank(6)) {
                Rs2Bank.walkToBank();
                sleepTillAnimationStop();
            }
            pluginssss("MQuestPlugin", false);
            Rs2Bank.openBank();
            sleep(1000);
            boolean  hasAxe1 = Rs2Inventory.get("Spade", false) != null;
            while (!hasAxe1) {
                Rs2Bank.withdrawX("Spade", 1);
                sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                hasAxe1 = Rs2Inventory.get("Spade", false) != null;
            }
            if (Rs2Inventory.contains("Spade")){
                pluginssss("MQuestPlugin", true);
                sleep(1000);
                break;
            }
            while (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(2982,3369,0))>10){
                Rs2Walker.setTarget(new WorldPoint(2982,3369,0));
                Rs2Walker.walkTo(new WorldPoint(2982,3369,0),5);
                sleepTillAnimationStop();
            }
            Rs2Walker.walkFastCanvas(new WorldPoint(2982,3369,0));
            sleepTillAnimationStop();
            while(!Rs2Inventory.contains("Spade")){
                Rs2GroundItem.interact("Spade","Take");
                sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                while(Rs2Player.isAnimating() || Rs2Player.isInteracting() || Rs2Player.isMoving()){
                    sleep(1000);
                }
            }
            pluginssss("MQuestPlugin", true);
            sleep(1000);
        }
        while (!checkQuestCompletion(QuestHelperQuest.X_MARKS_THE_SPOT)) {
            System.out.println("Waiting for quest to complete...");
            sleep(1000);
        }
        pluginssss("MQuestPlugin", false);
        sleep(1000);
        System.out.println("X Marks the Spot quest completed!");
        shutdown();
    }

    public boolean checkQuestCompletion(QuestHelperQuest quest) {
        return Microbot.getClientThread().runOnClientThread(() -> {
            QuestHelper questHelper = quest.getQuestHelper();
            return questHelper.isCompleted();
        });
    }

    protected QuestHelperPlugin getQuestHelperPlugin() {
        return (QuestHelperPlugin) Microbot.getPluginManager().getPlugins().stream()
                .filter(x -> x instanceof QuestHelperPlugin)
                .findFirst()
                .orElse(null);
    }
    public static void  sleepTillAnimationStop() {
        boolean result = sleepUntilTrue(Rs2Player::isAnimating, 100, 5000);
        if (!result) return;
        sleepUntilTrue(() -> !Rs2Player.isAnimating(), 100, 280000);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}