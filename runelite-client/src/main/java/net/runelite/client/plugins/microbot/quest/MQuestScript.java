package net.runelite.client.plugins.microbot.quest;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.geHandler.geHandlerScript;
import net.runelite.client.plugins.microbot.shortestpath.ShortestPathPlugin;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grandexchange.GrandExchangeSlots;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.reflection.Rs2Reflection;
import net.runelite.client.plugins.microbot.util.shop.Rs2Shop;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.questhelper.QuestHelperPlugin;
import net.runelite.client.plugins.questhelper.questinfo.QuestHelperQuest;
import net.runelite.client.plugins.questhelper.requirements.Requirement;
import net.runelite.client.plugins.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.questhelper.steps.*;
import net.runelite.client.plugins.questhelper.steps.widget.WidgetHighlight;
import net.runelite.http.api.item.ItemPrice;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.allz.AllzScript.sleepTillAnimationStop;
import static net.runelite.client.plugins.microbot.rasCollectBones.rasCollectBonesScript.waitAndPressContinue;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject.getGameObjects;
import static net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory.getSelectedItemId;
import static net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory.items;
import static net.runelite.client.plugins.microbot.util.player.Rs2Player.getQuestState;

public class MQuestScript extends Script {
    public static double version = 0.2;
    private Quest getActiveQuest() {
        try {
            return Microbot.getClientThread().runOnClientThread(() -> {
                for (Quest quest : Quest.values()) {
                    if (quest.getState(Microbot.getClient()) == QuestState.IN_PROGRESS) {
                        return quest;
                    }
                }
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static double version = 0.3;

    public static boolean isWithinXDistanceOfPlayer(WorldPoint targetPoint, int distance) {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        int playerX = playerLocation.getX();
        int targetX = targetPoint.getX();

        // Calculate the absolute difference in X coordinates between the player and the target point
        int xDifference = Math.abs(playerX - targetX);

        // Check if the difference is less than or equal to the specified distance
        return xDifference <= distance;
    }
    public static List<ItemRequirement> itemRequirements = new ArrayList<>();

    public static List<ItemRequirement> itemsMissing = new ArrayList<>();
    public static List<ItemRequirement> grandExchangeItems = new ArrayList<>();

    boolean unreachableTarget = false;
    int unreachableTargetCheckDist = 1;

    private MQuestConfig config;
    private static ArrayList<NPC> npcsHandled = new ArrayList<>();
    private static ArrayList<TileObject> objectsHandeled = new ArrayList<>();

    QuestStep dialogueStartedStep = null;

    public boolean run(MQuestConfig config) {
        this.config = config;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (Rs2Widget.getWidget(10027025)!=null){ // close quest completed box
                    sleep(1000);
                    Rs2Widget.clickWidget(10027025);
                    sleep(2000);

                }
                if (Rs2Widget.getWidget(38993958)!=null){ // close quest completed box
                    sleep(1000);
                    Rs2Widget.clickWidget(38993958);
                    sleep(2000);

                }
                if (Rs2Widget.hasWidget("That's the last of them.") && getActiveQuest().getName()=="Sheep Shearer"){
                    waitAndPressContinue();

                }
                if (Rs2Widget.hasWidget("I guess I'd better pay you then.") && getActiveQuest().getName()=="Sheep Shearer"){
                    waitAndPressContinue();


                }
                if (getQuestHelperPlugin().getSelectedQuest() == null) return;

                if (Rs2Player.isAnimating())
                    Rs2Player.waitForAnimation();

                QuestStep questStep = getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep();

                if (Rs2Dialogue.isInDialogue() && dialogueStartedStep == null)
                    dialogueStartedStep = questStep;

                if (questStep != null && Rs2Widget.isWidgetVisible(ComponentID.DIALOG_OPTION_OPTIONS)){
                    var dialogOptions = Rs2Widget.getWidget(ComponentID.DIALOG_OPTION_OPTIONS);
                    var dialogChoices = dialogOptions.getDynamicChildren();

                    for (var choice : questStep.getChoices().getChoices()){
                        if (choice.getExpectedPreviousLine() != null)
                            continue; // TODO

                        if (choice.getExcludedStrings() != null && choice.getExcludedStrings().stream().anyMatch(Rs2Widget::hasWidget))
                            continue;

                        for (var dialogChoice : dialogChoices){
                            if (dialogChoice.getText().endsWith(choice.getChoice())){
                                Rs2Keyboard.keyPress(dialogChoice.getOnKeyListener()[7].toString().charAt(0));
                                return;
                            }
                        }
                    }
                }

                if (questStep != null && !questStep.getWidgetsToHighlight().isEmpty()){
                    var widgetHighlight = questStep.getWidgetsToHighlight().stream()
                            .filter(x -> x instanceof WidgetHighlight)
                            .map(x -> (WidgetHighlight)x)
                            .filter(x -> Rs2Widget.isWidgetVisible(x.getGroupId(), x.getChildId()))
                            .findFirst().orElse(null);

                    if (widgetHighlight != null){
                        var widget = Rs2Widget.getWidget(widgetHighlight.getGroupId(), widgetHighlight.getChildId());
                        if (widget != null){
                            if (widgetHighlight.getChildChildId() != -1){
                                var childWidget = widget.getChildren()[widgetHighlight.getChildChildId()];
                                if (childWidget != null) {
                                    Rs2Widget.clickWidget(childWidget.getId());
                                    return;
                                }
                            } else {
                                Rs2Widget.clickWidget(widget.getId());
                                return;
                            }
                        }
                    }
                }

                if (getQuestHelperPlugin().getSelectedQuest() != null && !Microbot.getClientThread().runOnClientThread(() -> getQuestHelperPlugin().getSelectedQuest().isCompleted())) {
                    Widget widget = Rs2Widget.findWidget("Start ");
                    if (Rs2Widget.isWidgetVisible(ComponentID.DIALOG_OPTION_OPTIONS) && getQuestHelperPlugin().getSelectedQuest().getQuest().getId() != Quest.COOKS_ASSISTANT.getId() || (widget != null &&
                            Microbot.getClientThread().runOnClientThread(() -> widget.getParent().getId()) != 10616888) && !Rs2Bank.isOpen()) {
                        Rs2Keyboard.keyPress('1');
                        Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                        return;
                    }

                    if (Rs2Dialogue.isInDialogue() && dialogueStartedStep == questStep) {
                        // Stop walker if in dialogue
                        Rs2Walker.setTarget(null);
                        Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                        return;
                    } else {
                        dialogueStartedStep = null;
                    }

                    boolean isInCutscene = Microbot.getVarbitValue(4606) > 0;
                    if (isInCutscene) {
                        if (ShortestPathPlugin.getMarker() != null)
                            ShortestPathPlugin.exit();
                        return;
                    }

                    if (questStep instanceof DetailedQuestStep && handleRequirements((DetailedQuestStep) questStep)){
                        sleep(500, 1000);
                        return;
                    }

                    /**
                     * This portion is needed when using item on another item in your inventory.
                     * If we do not prioritize this, the script will think we are missing items
                     */
                    if (questStep instanceof DetailedQuestStep && !(questStep instanceof NpcStep || questStep instanceof ObjectStep || questStep instanceof DigStep)) {
                        boolean result = applyDetailedQuestStep((DetailedQuestStep) getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep());
                        if (result) {
                            sleepUntil(() -> Rs2Player.isInteracting() || Rs2Player.isMoving() || Rs2Player.isAnimating() || Rs2Dialogue.isInDialogue(), 500);
                            sleepUntil(() -> !Rs2Player.isInteracting() && !Rs2Player.isMoving() && !Rs2Player.isAnimating());
                            return;
                        }
                    }

                    if (getQuestHelperPlugin().getSelectedQuest().getCurrentStep() instanceof ConditionalStep) {
                        QuestStep conditionalStep = getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep();
                        applyStep(conditionalStep);
                    } else if (getQuestHelperPlugin().getSelectedQuest().getCurrentStep() instanceof NpcStep) {
                        applyNpcStep((NpcStep) getQuestHelperPlugin().getSelectedQuest().getCurrentStep());
                    } else if (getQuestHelperPlugin().getSelectedQuest().getCurrentStep() instanceof ObjectStep){
                        applyObjectStep((ObjectStep) getQuestHelperPlugin().getSelectedQuest().getCurrentStep());
                    } else if (getQuestHelperPlugin().getSelectedQuest().getCurrentStep() instanceof DigStep){
                        applyDigStep((DigStep) getQuestHelperPlugin().getSelectedQuest().getCurrentStep());
                    } else if (getQuestHelperPlugin().getSelectedQuest().getCurrentStep() instanceof PuzzleStep){
                        applyPuzzleStep((PuzzleStep) getQuestHelperPlugin().getSelectedQuest().getCurrentStep());
                    }

                    sleepUntil(() -> Rs2Player.isInteracting() || Rs2Player.isMoving() || Rs2Player.isAnimating() || Rs2Dialogue.isInDialogue(), 500);
                    sleepUntil(() -> !Rs2Player.isInteracting() && !Rs2Player.isMoving() && !Rs2Player.isAnimating());
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }, 0, Random.random(400, 1000), TimeUnit.MILLISECONDS);
        return true;
    }
    private int getDistanceToPlayer(WorldPoint itemLocation) {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        return playerLocation.distanceTo(itemLocation);
    }
    public static boolean collectFishes() {
        List<Integer> itemList = Arrays.asList(329, 331, 333, 335);
        Rs2Walker.walkTo(new WorldPoint(3105, 3432, 0), 6);
        sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3105, 3432, 0)) < 8, 15000);
        for (int k = 0; k < 3; k++) {
            while (!Rs2Inventory.isFull()) {
                for (int item : itemList) {
                    if (Rs2GroundItem.loot(item)) {
                        sleep(50, 80);
                        break;
                    }
                }
            }
        }
        return true;
    }
    public static boolean isBuySlotAvailable() {
        Pair<GrandExchangeSlots, Integer> availableSlotInfo = Rs2GrandExchange.getAvailableSlot();
        // Check if the left part of the pair is not null, indicating an available slot
        return availableSlotInfo.getLeft() != null;
    }
    private void buyBead(String bead) {
        int attempt = 0;
        while (!Rs2Inventory.contains(bead)) {
            Pair<GrandExchangeSlots, Integer> slot = Rs2GrandExchange.getAvailableSlot();
            if (slot.getLeft() != null) {
                int offerPrice = 100; // Example price, replace with actual logic to get price
                Rs2GrandExchange.buyItemAbove5Percent(bead, 1);
                long startTime = System.currentTimeMillis();
                while (true) {
                    if (Rs2GrandExchange.hasBoughtOffer()) {
                        Rs2GrandExchange.collectToInventory();
                        break;
                    }
                    if ((System.currentTimeMillis() - startTime) > 60000) { // 60 seconds check
                        break;
                    }
                    sleep(1000); // Check every second
                }
                if (!Rs2GrandExchange.hasBoughtOffer()) {
                    // Assuming cancelOffer method exists for a specific bead

                    attempt++;
                    offerPrice += (offerPrice * 5 / 100) * attempt;
                    Rs2GrandExchange.buyItem(bead, offerPrice, 1);
                }
            } else {
                // Wait until a slot is available
                sleep(5000);
            }
        }
    }
    public static long ItemQuantity(int id) {
        Rs2Item rs2Item = Rs2Inventory.get(id);
        if (rs2Item != null) {
            if (rs2Item.isStackable()) {
                return rs2Item.quantity;
            } else {
                return items().stream().filter(x -> x.id == id).count();
            }
        }
        else { return 0;}
    }
    public boolean dyesItemsCollected=false;
    public boolean dyesAreReady=false;
    public static void goBuyAndReturn(int[] amounts,int highBuyPercent, String... itemNames) {
        WorldPoint savedLocation = Rs2Player.getWorldLocation();
        WorldPoint geLocation = new WorldPoint(3164, 3485, 0); // Coordinates for GE
        while(Rs2Player.getWorldLocation().distanceTo(geLocation) > 10) {
            Rs2Walker.walkTo(geLocation);
            Rs2Player.waitForWalking();
        }
        Rs2Bank.openBank();
        long coinsInBank = (long) Rs2Bank.count("Coins",true);
        Rs2Bank.closeBank();
        long coins = ItemQuantity(995) + coinsInBank;
        for (int i = 0; i < itemNames.length; i++) {
            if (amounts[i] == 0) {
                continue; // Skip this item if the purchase amount is 0
            }
            int finalI = i;

            int itemId = (int) Microbot.getClientThread().runOnClientThread(() -> {
                List<ItemPrice> items = Microbot.getItemManager().search(itemNames[finalI]);
                return items.stream()
                        .filter(item -> itemNames[finalI].equals(item.getName()))
                        .findFirst()
                        .map(ItemPrice::getId)
                        .orElseGet(() -> {
                            System.err.println("Item not found: " + itemNames[finalI]);
                            return -1; // Default value indicating item not found
                        });
            });
            int pricePerItem = (int) Microbot.getClientThread().runOnClientThread(() ->
                    Microbot.getItemManager().getItemPrice(itemId));

            int increasedPrice = (int) Math.ceil(pricePerItem + (pricePerItem * highBuyPercent / 100.0));

            if (coins >= (long) pricePerItem * amounts[i]) {
                Rs2GrandExchange.buyItem(itemNames[i], increasedPrice, amounts[i]);
                sleepUntilTrue(() -> Rs2GrandExchange.hasBoughtOffer(), 400,12000);
                Rs2GrandExchange.collectToInventory();
            } else {
                Microbot.log("Insufficient coins to buy " + amounts[i] + " of " + itemNames[i]);
            }
        }
        while(Rs2Player.getWorldLocation().distanceTo(savedLocation) > 5) {
            Rs2Walker.walkTo(savedLocation,1);
            Rs2Player.waitForWalking();
        }
    }
    boolean handleGeForPrinceAliRescue = false;
    boolean handlePrinceAliRescueQuestEnd = false;
    boolean killedgarden = false;
    private boolean handleRequirements(DetailedQuestStep questStep) {
        var requirements = questStep.getRequirements();
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "PIRATES_TREASURE"))&&("Dig in the middle of the cross in Falador Park, and kill the Gardener (level 4) who appears. Once killed, dig again.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) && !(Rs2Player.getWorldLocation().distanceTo(new WorldPoint(2999, 3383, 0)) > 1) && !killedgarden){

            while (Rs2Player.isMoving() || Rs2Player.isAnimating() || Rs2Player.isInteracting()) {
                sleep(5000);
            }
            killedgarden=true;
        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "PIRATES_TREASURE"))&&("Please open Pirate Treasure's Quest Journal to sync the current quest state.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))){
            NewMenuEntry menuEntry = new NewMenuEntry(
                    "Read journal:",                 // Option
                    "<col=ff9040>Pirate's Treasure</col>", // Target
                    2,                              // Identifier
                    MenuAction.CC_OP,               // Type (use MenuAction enum)
                    16,                             // Param0
                    26148871,                       // Param1
                    false                           // ForceLeftClick
            );
            menuEntry.setItemId(-1); // ItemId (set to -1 as provided)

            // Use Microbot's method to send the menu entry
            Microbot.doInvoke(menuEntry, new Rectangle(1, 1, Microbot.getClient().getCanvasWidth(), Microbot.getClient().getCanvasHeight()));
            sleep(3000);

        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "PRINCE_ALI_RESCUE"))&&("Return to Hassan in the Al Kharid Palace to complete the quest.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) && !handlePrinceAliRescueQuestEnd){
           // interactWithObjectAtLocation(new WorldPoint(3123, 3243, 0), "open");
            Rs2GameObject.getWallObjects(2881, new WorldPoint(3123, 3243, 0)).forEach(Rs2GameObject::interact);
            sleep(1000);
            Rs2Walker.walkTo(new WorldPoint(3299, 3162, 0), 0);
            while (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3299, 3162, 0)) > 1) {
                sleep(2000);
                Rs2Walker.setTarget(new WorldPoint(3299, 3162, 0));
                Rs2Walker.walkTo(new WorldPoint(3299, 3162, 0), 0);
            }
            handlePrinceAliRescueQuestEnd=true;
        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "PRINCE_ALI_RESCUE"))&&("Use the key on the prison door. If Lady Keli respawned you'll need to tie her up again.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) && !Rs2Inventory.contains("Bronze Key")){
            Rs2Walker.walkTo(new WorldPoint(3111, 3261, 0), 0);
            while (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3111, 3261, 0)) > 1) {
                sleep(2000);
                Rs2Walker.setTarget(new WorldPoint(3111, 3261, 0));
                Rs2Walker.walkTo(new WorldPoint(3111, 3261, 0), 0);
            }
            while (!Rs2Inventory.contains("Bronze Key")) {
                Rs2Npc.interact("Leela","Talk-to");
                sleep(1000);
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
//                sleep(1000);
//                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
//                sleep(1000);
//                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
//                sleep(1000);
//                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
//                sleep(1000);
//                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
//                sleep(1000);
//                while(Rs2Player.isAnimating() || Rs2Player.isInteracting() || Rs2Player.isMoving()){
//                    sleep(1000);
//                }
            }

        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "PRINCE_ALI_RESCUE"))&&("Talk to Hassan in the Al Kharid Palace.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))&& (!Rs2Inventory.isEmpty()) && !handleGeForPrinceAliRescue){
            Rs2GrandExchange.walkToGrandExchange();
            sleep(2000);
            final WorldPoint GRAND_EXCHANGE_LOCATION = new WorldPoint(3164, 3485, 0); // Replace with the central tile of Grand Exchange
            while (Rs2Player.getWorldLocation().distanceTo(GRAND_EXCHANGE_LOCATION) > 1) {
                sleep(2000);
                Rs2Walker.setTarget(GRAND_EXCHANGE_LOCATION);
                Rs2Walker.walkTo(GRAND_EXCHANGE_LOCATION, 0);
            }
            Rs2Bank.openBank();
            sleep(2000);
            while (!Rs2Bank.isOpen()) {
                sleep(1000);
                Rs2Bank.openBank();
            }
            Rs2Bank.depositAll();
            sleep(2000);
            while (!Rs2Inventory.isEmpty()) {
                Rs2Bank.depositAll();
                sleep(1000);
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
                    Rs2Widget.clickWidgetFast(Rs2Widget.findWidget("Confirm: Prayer"));
                    sleep(1000);
                    break;
                }
            }
            Rs2Bank.withdrawX(995, 3000);
            sleep(2000);
            while ((int)ItemQuantity(995)<3000) {
                sleep(3000);
                if (Rs2Bank.isOpen()) {
                    Rs2Bank.withdrawX(995, 3000);
                    sleep(2000);
                }else {
                    Rs2Bank.openBank();
                    sleep(2000);
                    Rs2Bank.withdrawX(995, 3000);
                }
            }
//            Rs2GrandExchange.openExchange();
//            sleep(2000);
//            while (!Rs2GrandExchange.isOpen()) {
//                sleep(1000);
//                Rs2GrandExchange.openExchange();
//            }
            String[] itemNames = {
                    "Soft clay","Soft clay",  "Ball of wool","Ball of wool","Ball of wool", "Yellow dye", "Redberries",
                    "Ashes", "Bucket of water", "Pot of flour", "Bronze bar",
                    "Pink skirt", "Beer","Beer","Beer", "Rope"
            };
            int[] quantities = {1,1,1,1,1, 1, 1, 1, 1, 1, 1, 1, 1,1,1, 1};
            int highBuyPercent = 50;
            sleep(1000);
            goBuyAndReturn(quantities, highBuyPercent, itemNames);
            sleep(1000);
            Rs2GrandExchange.closeExchange();

            handleGeForPrinceAliRescue=true;
//            Rs2GrandExchange.buyItem("Soft clay", (int)ItemQuantity(995), 1);
//            while (!Rs2GrandExchange.hasBoughtOffer()) {
//                sleep(1000);
//            }
//            Rs2GrandExchange.collectToInventory();
//            Rs2GrandExchange.buyItem("Ball of wool", (int)ItemQuantity(995), 3);
//            //sleep(1000);
//            while (!Rs2GrandExchange.hasBoughtOffer()) {
//                sleep(1000);
//            }
//            Rs2GrandExchange.collectToInventory();
//            Rs2GrandExchange.buyItem("Yellow dye", (int)ItemQuantity(995), 1);
//            while (!Rs2GrandExchange.hasBoughtOffer()) {
//                sleep(1000);
//            }
//            Rs2GrandExchange.collectToInventory();
//            Rs2GrandExchange.buyItem("Redberries", (int)ItemQuantity(995), 1);
//            while (!Rs2GrandExchange.hasBoughtOffer()) {
//                sleep(1000);
//            }
//            Rs2GrandExchange.collectToInventory();
//            Rs2GrandExchange.buyItem("Ashes", (int)ItemQuantity(995), 1);
//            while (!Rs2GrandExchange.hasBoughtOffer()) {
//                sleep(1000);
//            }
//            Rs2GrandExchange.collectToInventory();
//            Rs2GrandExchange.buyItem("Bucket of water", (int)ItemQuantity(995), 1);
//            while (!Rs2GrandExchange.hasBoughtOffer()) {
//                sleep(1000);
//            }
//            Rs2GrandExchange.collectToInventory();
//            Rs2GrandExchange.buyItem("Pot of flour", (int)ItemQuantity(995), 1);
//            while (!Rs2GrandExchange.hasBoughtOffer()) {
//                sleep(1000);
//            }
//            Rs2GrandExchange.collectToInventory();
//            Rs2GrandExchange.buyItem("Bronze bar", (int)ItemQuantity(995), 1);
//            while (!Rs2GrandExchange.hasBoughtOffer()) {
//                sleep(1000);
//            }
//            Rs2GrandExchange.collectToInventory();
//            Rs2GrandExchange.buyItem("Pink skirt", (int)ItemQuantity(995), 1);
//            while (!Rs2GrandExchange.hasBoughtOffer()) {
//                sleep(1000);
//            }
//            Rs2GrandExchange.collectToInventory();
//            Rs2GrandExchange.buyItem("Beer", (int)ItemQuantity(995), 3);
//            while (!Rs2GrandExchange.hasBoughtOffer()) {
//                sleep(1000);
//            }
//            Rs2GrandExchange.collectToInventory();
//            Rs2GrandExchange.buyItem("Rope", (int)ItemQuantity(995), 1);
//            while (!Rs2GrandExchange.hasBoughtOffer()) {
//                sleep(1000);
//            }

        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "DORICS_QUEST"))&&("Bring Doric north of Falador all the required items. You can mine them all in the Dwarven Mines, or buy them from the Grand Exchange.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))&& (!Rs2Inventory.contains("Clay") || !Rs2Inventory.contains("Copper Ore") || !Rs2Inventory.contains("Iron Ore"))){
            boolean haspickAxe = Rs2Inventory.get("pickaxe", false) != null;
            if (!haspickAxe){
                Rs2Bank.walkToBank();
                while (!Rs2Bank.isNearBank(2)) {
                    sleep(2000);
                    Rs2Bank.walkToBank();
                }
                Rs2Bank.openBank();
                sleep(2000);
                while (!Rs2Bank.isOpen()) {
                    sleep(3000);
                    Rs2Bank.openBank();
                }
                boolean  hasAxe1 = Rs2Inventory.get("pickaxe", false) != null;
                while (!hasAxe1) {
                    Rs2Bank.withdrawItem("pickaxe");
                    sleep(3000);
                    hasAxe1 = Rs2Inventory.get("pickaxe", false) != null;
                }
            }
            try {
                if (!(Rs2Inventory.count("Clay") >= 6)) {
                    Rs2Walker.walkTo(new WorldPoint(3179, 3369, 0), 0);
                    while (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3179, 3369, 0)) > 1) {
                        sleep(2000);
                        Rs2Walker.setTarget(new WorldPoint(3179, 3369, 0));
                        Rs2Walker.walkTo(new WorldPoint(3179, 3369, 0), 0);
                    }
                    while (!(Rs2Inventory.count("Clay")>6)) {
                        Rs2GameObject.interact("Clay rocks");
                        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                        while (Rs2Player.isAnimating() || Rs2Player.isInteracting() || Rs2Player.isMoving()) {
                            sleep(1000);
                        }
                    }
                }
            } catch (Exception e) {
                if (!(Rs2Inventory.count("Clay") >= 6)) {
                    Rs2Walker.walkTo(new WorldPoint(3179, 3369, 0), 0);
                    while (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3179, 3369, 0)) > 1) {
                        sleep(2000);
                        Rs2Walker.setTarget(new WorldPoint(3179, 3369, 0));
                        Rs2Walker.walkTo(new WorldPoint(3179, 3369, 0), 0);
                    }
                    while (!(Rs2Inventory.count("Clay")>6)) {
                        Rs2GameObject.interact("Clay rocks");
                        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                        while (Rs2Player.isAnimating() || Rs2Player.isInteracting() || Rs2Player.isMoving()) {
                            sleep(1000);
                        }
                    }
                }
            }

            try {
                if (!(Rs2Inventory.count("Copper ore") >= 4)) {
                    Rs2Walker.walkTo(new WorldPoint(3283, 3366, 0), 0);
                    while (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3283, 3366, 0)) > 1) {
                        sleep(2000);
                        Rs2Walker.setTarget(new WorldPoint(3283, 3366, 0));
                        Rs2Walker.walkTo(new WorldPoint(3283, 3366, 0), 0);
                    }
                    while (!(Rs2Inventory.count("Copper ore")>4)) {
                        Rs2GameObject.interact("Copper rocks");
                        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                        while (Rs2Player.isAnimating() || Rs2Player.isInteracting() || Rs2Player.isMoving()) {
                            sleep(1000);
                        }
                    }
                }
            } catch (Exception e) {
                if (!(Rs2Inventory.count("Copper ore") >= 4)) {
                    Rs2Walker.walkTo(new WorldPoint(3283, 3366, 0), 0);
                    while (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3283, 3366, 0)) > 1) {
                        sleep(2000);
                        Rs2Walker.setTarget(new WorldPoint(3283, 3366, 0));
                        Rs2Walker.walkTo(new WorldPoint(3283, 3366, 0), 0);
                    }
                    while (!(Rs2Inventory.count("Copper ore")>4)) {
                        Rs2GameObject.interact("Copper rocks");
                        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                        while (Rs2Player.isAnimating() || Rs2Player.isInteracting() || Rs2Player.isMoving()) {
                            sleep(1000);
                        }
                    }
                }
            }

            if(Rs2Player.getRealSkillLevel(Skill.MINING) < 15){
                Rs2Walker.walkTo(new WorldPoint(3283, 3366, 0), 0);
                while (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3283, 3366, 0)) > 1) {
                    sleep(2000);
                    Rs2Walker.setTarget(new WorldPoint(3283, 3366, 0));
                    Rs2Walker.walkTo(new WorldPoint(3283, 3366, 0), 0);
                }
                while(Rs2Player.getRealSkillLevel(Skill.MINING) < 15){
                    if (Rs2Inventory.isFull()){
                        Rs2Inventory.dropAll("Copper ore");
                    }
                    Rs2GameObject.interact("Copper rocks");
                    sleep(2000);
                    while(Rs2Player.isAnimating() || Rs2Player.isInteracting() || Rs2Player.isMoving() || Rs2Player.isWalking()){
                        sleep(1000);
                    }
                }

            }
            Rs2Inventory.dropAll("Copper ore");
            sleep(2000);
            while(Rs2Inventory.count("Copper ore")<4){
                Rs2GroundItem.pickup("Copper ore",2);
                sleep(1000);
           //     Rs2Inventory.waitForInventoryChanges();
                while(Rs2Player.isAnimating() || Rs2Player.isInteracting() || Rs2Player.isMoving() || Rs2Player.isWalking()){
                    sleep(1000);
                }
            }

            sleep(2000);
            try {
                if (!(Rs2Inventory.count("Iron ore") >= 4)) {
                    Rs2Walker.walkTo(new WorldPoint(3283, 3366, 0), 0);
                    while (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3283, 3366, 0)) > 1) {
                        sleep(2000);
                        Rs2Walker.setTarget(new WorldPoint(3283, 3366, 0));
                        Rs2Walker.walkTo(new WorldPoint(3283, 3366, 0), 0);
                    }
                    while (!(Rs2Inventory.count("Iron ore")>4)) {
                        Rs2GameObject.interact("Iron rocks");
                        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                        while (Rs2Player.isAnimating() || Rs2Player.isInteracting() || Rs2Player.isMoving()) {
                            sleep(1000);
                        }
                    }
                }
            } catch (Exception e) {
                if (!(Rs2Inventory.count("Iron ore") >= 4)) {
                    Rs2Walker.walkTo(new WorldPoint(3283, 3366, 0), 0);
                    while (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3283, 3366, 0)) > 1) {
                        sleep(2000);
                        Rs2Walker.setTarget(new WorldPoint(3283, 3366, 0));
                        Rs2Walker.walkTo(new WorldPoint(3283, 3366, 0), 0);
                    }
                    while (!(Rs2Inventory.count("Iron ore")>4)) {
                        Rs2GameObject.interact("Iron rocks");
                        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                        while (Rs2Player.isAnimating() || Rs2Player.isInteracting() || Rs2Player.isMoving()) {
                            sleep(1000);
                        }
                    }
                }
            }


        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "PIRATES_TREASURE"))&&("Talk to Redbeard Frank in Port Sarim.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))&& !Rs2Inventory.contains("Spade")){
        Rs2Walker.walkTo(new WorldPoint(2982,3369,0),0);
        while (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(2982,3369,0))>1){
            sleep(2000);
            Rs2Walker.setTarget(new WorldPoint(2982,3369,0));
            Rs2Walker.walkTo(new WorldPoint(2982,3369,0),0);
        }
        while(!Rs2Inventory.contains("Spade")){
            Rs2GroundItem.interact("Spade","Take");
            sleep(2000);
            while(Rs2Player.isAnimating() || Rs2Player.isInteracting() || Rs2Player.isMoving()){
                sleep(1000);
            }
        }
        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "IMP_CATCHER"))&&("Collect one of each bead. You can kill imps for these beads, or buy them on the Grand Exchange.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))){
            if(Rs2Inventory.contains("Red bead") && Rs2Inventory.contains("Yellow bead") && Rs2Inventory.contains("Black bead") && Rs2Inventory.contains("White bead")){
                return true;
            }
            Rs2GrandExchange.walkToGrandExchange();
            sleepTillAnimationStop();
            final WorldPoint GRAND_EXCHANGE_LOCATION = new WorldPoint(3164, 3485, 0); // Replace with the central tile of Grand Exchange
            while (Rs2Player.getWorldLocation().distanceTo(GRAND_EXCHANGE_LOCATION) > 10) {
                Rs2Walker.walkTo(GRAND_EXCHANGE_LOCATION, 8);
                sleepTillAnimationStop();
            }
            long coinsInBank = (long) Rs2Bank.count("Coins",true);
            if (ItemQuantity(995)<15000) {
                Rs2Bank.walkToBank();
                while (!Rs2Bank.isNearBank(10)) {
                    Rs2Bank.walkToBank();
                    sleepTillAnimationStop();
                }
                Rs2Bank.openBank();
                sleep(500);
                Rs2Bank.withdrawX(995, 15000-(int)ItemQuantity(995));
                Rs2Bank.closeBank();

            }

            while (!Rs2GrandExchange.isOpen()) {
                Rs2GrandExchange.openExchange();
                sleep(2000);
            }
            geHandlerScript.goBuyAndReturn(new int[]{1,1,1,1},10, "Red bead","Yellow bead","Black bead","White bead");

        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "WITCHS_POTION"))&&("Bring the ingredients to Hetty.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))&& Rs2Inventory.contains("Rat's tail") && !Rs2Inventory.contains("Burnt meat") && !Rs2Inventory.contains("Eye of newt") && !Rs2Inventory.contains("Onion")){
            sleep(2000);
            Rs2Walker.walkTo(new WorldPoint(2950,3250,0),10);
            sleepTillAnimationStop();
            WorldPoint targetTile = new WorldPoint(2950, 3250, 0);
            while (Rs2Player.getWorldLocation().distanceTo(targetTile) > 6) {
                Rs2Walker.setTarget(targetTile);
                Rs2Walker.walkTo(targetTile, 5);
                sleepTillAnimationStop();
            }
            while(!Rs2Inventory.contains("Onion")){
                Rs2GameObject.interact("onion","Pick");
                sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
            }

        }

        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "WITCHS_POTION"))&&("Bring the ingredients to Hetty.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))&& Rs2Inventory.contains("Rat's tail") && !Rs2Inventory.contains("Burnt meat") && !Rs2Inventory.contains("Eye of newt") && Rs2Inventory.contains("Onion")){
            sleep(2000);
            Rs2Walker.walkTo(new WorldPoint(3014,3257,0),10);
            sleepTillAnimationStop();
            WorldPoint targetTile = new WorldPoint(3014, 3257, 0);
            WorldPoint playerLocation = Rs2Player.getWorldLocation();
            while (Rs2Player.getWorldLocation().distanceTo(targetTile) > 10) {
                Rs2Walker.setTarget(targetTile);
                Rs2Walker.walkTo(targetTile, 8);
                sleepTillAnimationStop();
            }

            //sleep(2000);
            while(!Rs2Inventory.contains("Eye of newt")){
                Rs2Npc.interact("Betty","Trade");
                sleepUntil(Rs2Shop::isOpen, 5000);
                Rs2Shop.buyItem("Eye of newt","1");
                sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                /*
                NewMenuEntry menuEntry = new NewMenuEntry(
                        "Buy 1",                   // Option
                        "<col=ff9040>Eye of newt</col>", // Target
                        2,                         // Identifier
                        MenuAction.CC_OP,          // Type (use MenuAction enum)
                        15,                        // Param0
                        19660816,                  // Param1
                        false                      // ForceLeftClick
                );
                menuEntry.setItemId(221); // ItemId

                // Use Microbot's method to send the menu entry
                Microbot.doInvoke(menuEntry, new Rectangle(1, 1, Microbot.getClient().getCanvasWidth(), Microbot.getClient().getCanvasHeight()));
                sleep(2000,3000);

                 */
            }

        }
//1342 cutted //1276 uncut
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "WITCHS_POTION"))&&("Bring the ingredients to Hetty.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))&& Rs2Inventory.contains("Rat's tail") && !Rs2Inventory.contains("Burnt meat") && Rs2Inventory.contains("Eye of newt") && Rs2Inventory.contains("Onion")){
            while (!Rs2Inventory.contains("Burnt meat")){
            if (!Rs2Inventory.contains(1511)){
            Rs2Walker.walkTo(new WorldPoint(2928,3298,0),0);
            sleep(2000);
            WorldPoint targetTile = new WorldPoint(2928, 3298, 0);
            WorldPoint playerLocation = Rs2Player.getWorldLocation();
            while (Rs2Player.getWorldLocation().distanceTo(targetTile) > 10) {
                Rs2Walker.setTarget(targetTile);
                Rs2Walker.walkTo(targetTile, 8);
                sleepTillAnimationStop();
            }
            Rs2Walker.walkFastCanvas(targetTile);
            sleepTillAnimationStop();
            Rs2GameObject.interact(1276,"Chop down");
            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
            while( !Rs2Inventory.contains(1511)){
                Rs2GameObject.interact(1276,"Chop down");
                sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
            }
            }
            WorldPoint targetTile = new WorldPoint(2920, 3288, 0);
            WorldPoint playerLocation = Rs2Player.getWorldLocation();
            while (Rs2Player.getWorldLocation().distanceTo(targetTile) > 10) {
                sleep(3000);
                Rs2Walker.setTarget(targetTile);
                Rs2Walker.walkTo(targetTile, 6);
            }

            while(!Rs2Inventory.contains("Raw beef")){
                Rs2Npc.interact("cow","Attack");
                while(Rs2Player.isAnimating() || Rs2Player.isInteracting() || Rs2Player.isMoving()){
                    sleep(1000);
                }
                sleep(2000);
                Rs2GroundItem.interact("raw beef", "Take");
                sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
            }
                 Rs2Inventory.use(1511);
                sleep(2000);
                Rs2Inventory.use("Tinderbox");
                sleep(2000);
                while(Rs2Player.isMoving() || Rs2Player.isAnimating() || Rs2Player.isInteracting()){
                    sleep(1000);
                }
            while(Rs2Inventory.contains("Raw beef")){
                Rs2Inventory.use("Raw beef");
                sleep(1000);
                Rs2GameObject.interact("Fire","Use");
                sleep(2000);
//                if (Rs2Widget.hasWidget("<col=ffffff>All</col>")){
//                    Rs2Widget.clickWidget(Rs2Widget.getWidget(270,12).getX(),Rs2Widget.getWidget(270,12)[0].getY());
//                    sleep(5000);
//                }
                while(Rs2Player.isMoving() || Rs2Player.isAnimating() || Rs2Player.isInteracting()){
                    sleep(1000);
                }
                if (Rs2Inventory.contains("Burnt meat")){
                    break;
                }
                 Rs2Inventory.use("Cooked meat");
                sleep(1000);
                Rs2GameObject.interact("Fire","Use");
                sleep(2000);
                while(Rs2Player.isMoving() || Rs2Player.isAnimating() || Rs2Player.isInteracting()){
                    sleep(1000);
                }
            }
            if (Rs2Inventory.contains("Burnt meat") && Rs2Inventory.contains("Eye of newt") && Rs2Inventory.contains("Onion") && Rs2Inventory.contains("Rat's tail")){
                break;
            }

        }
        }

        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "WITCHS_POTION"))&&("Talk to Hetty in Rimmington.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))){
            boolean hasAxe = Rs2Inventory.get("axe", false) != null;
            if (!hasAxe || !Rs2Inventory.contains("Tinderbox") || !Rs2Inventory.contains("Coins")){
                Rs2Bank.walkToBank();
                while (!Rs2Bank.isNearBank(6)) {
                    sleep(2000);
                    Rs2Bank.walkToBank();
                }
                Rs2Bank.openBank();
                sleep(2000);
                while (!Rs2Bank.isOpen()) {
                    sleep(3000);
                    Rs2Bank.openBank();
                }
                boolean  hasAxe1 = Rs2Inventory.get("axe", false) != null;
                while (!hasAxe1) {
                    Rs2Bank.withdrawItem("axe");
                    sleep(3000);
                    hasAxe1 = Rs2Inventory.get("axe", false) != null;
                }
                while (!Rs2Inventory.contains("Tinderbox")) {
                    Rs2Bank.withdrawItem("Tinderbox");
                    sleep(3000);
                }
                while (!Rs2Inventory.contains("Coins")) {
                    int[] possibleAmounts = {1000, 1500, 2000, 2500};
                    int randomAmount = possibleAmounts[new java.util.Random().nextInt(possibleAmounts.length)];
                    Rs2Bank.withdrawX("Coins", randomAmount);
                    sleep(3000);
                }
                Rs2Bank.closeBank();
            }
        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "SHEEP_SHEARER"))&&("Shear 20 sheep in the nearby field.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) &&!Rs2Inventory.hasItemAmount("Wool",20)){
            if (Rs2Inventory.contains("Shears")){
             //   Rs2Walker.walkTo(Rs2Bank.getNearestBank().getWorldPoint(),0);
                Rs2Bank.walkToBankAndUseBank();
                while (!Rs2Bank.isNearBank(2)){
                    sleep(1000);
                }
                Rs2Bank.openBank();
                sleep(2000);
                while (!Rs2Bank.isOpen()){
                    sleep(2000);
                    Rs2Bank.openBank();
                }
                try {
                if( Rs2Widget.getWidget(664,29).getChild(0)!=null){
                    Microbot.click(Rs2Widget.getWidget(664,29).getChild(0).getBounds());
                    sleep(2000);
                }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Rs2Bank.depositAllExcept("Shears","Antique lamp");
                sleep(2000);
                Rs2Bank.closeBank();
                Rs2Walker.walkTo(new WorldPoint(3200,3271,0),0);
                sleep(2000);

                while(!(Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3200,3271,0))>8)){
                    sleep(2000);
                    Rs2Walker.setTarget(new WorldPoint(3200,3271,0));
                    sleep(1000);
                    Rs2Walker.walkTo(new WorldPoint(3200,3271,0),0);
                }
                int[] SheepIDtoShear = {2693, 2699,2786 , 2694};
                while(!Rs2Inventory.hasItemAmount("Wool",20)){
                    if(!Rs2Player.isMoving() && !Rs2Player.isAnimating() && !Rs2Player.isInteracting()){
                        java.util.Random random = new java.util.Random();
                        int randomIndex = random.nextInt(SheepIDtoShear.length);
                        int selectedNumber = SheepIDtoShear[randomIndex];
                        Rs2Npc.interact(selectedNumber,"Shear");
                        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                    }
                }

            }
        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "COOKS_ASSISTANT"))&&("Purchase a bucket from the Lumbridge General Store.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) && (!Rs2Inventory.contains("egg") || !Rs2Inventory.contains("bucket"))){
            sleep(6000);
            Rs2Npc.interact(4626,"Talk-to");
            sleepUntilTrue(Rs2Dialogue::isInDialogue,100,6000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(1200,2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(1200,2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(1200,2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(1200,2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(1200,2000);
            if (QuestState.FINISHED.equals(getQuestState(Quest.COOKS_ASSISTANT))){
                return true;
            }
            if (!Rs2Inventory.contains("egg") || !Rs2Inventory.contains("bucket")){

                Rs2Walker.walkTo(new WorldPoint(3238,3295,0),5);

                sleepTillAnimationStop();
                WorldPoint targetTile = new WorldPoint(3238, 3295, 0);
                WorldPoint playerLocation = Rs2Player.getWorldLocation();

                // Get reachable tiles within 2 tiles distance from the player
                HashMap<WorldPoint, Integer> reachableTiles = Rs2Tile.getReachableTilesFromTile(playerLocation, 1);

                // Check if the target tile is within the reachable tiles
                while (!reachableTiles.containsKey(targetTile)) {
                    // Perform the desired actions
                    WorldPoint playerLocation1 = Rs2Player.getWorldLocation();
                    // Get reachable tiles within 2 tiles distance from the player
                    reachableTiles = Rs2Tile.getReachableTilesFromTile(playerLocation1, 2);
                    Rs2Walker.walkFastCanvas(new WorldPoint(3238,3295,0),false);
                    sleepTillAnimationStop();
                }
                Rs2Walker.walkTo(new WorldPoint(3225,3293,0),10);
                sleepTillAnimationStop();
                WorldPoint targetTile1 = new WorldPoint(3225, 3293, 0);
                WorldPoint playerLocation1 = Rs2Player.getWorldLocation();
                HashMap<WorldPoint, Integer> reachableTiles2 = Rs2Tile.getReachableTilesFromTile(playerLocation, 1);
                while (!reachableTiles2.containsKey(targetTile1)) {
                    // Perform the desired actions
                    WorldPoint playerLocation2 = Rs2Player.getWorldLocation();
                    // Get reachable tiles within 2 tiles distance from the player
                    reachableTiles2 = Rs2Tile.getReachableTilesFromTile(playerLocation2, 1);
                    Rs2Walker.walkFastCanvas(new WorldPoint(3225,3293,0),false);
                    sleepTillAnimationStop();
                }
                while(!Rs2GroundItem.exists("bucket",3)){
                    sleep(1000);
                }
                Rs2GroundItem.interact("bucket","Take");
                sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                while(!Rs2GroundItem.exists("egg", 5)){
                    sleep(1000);
                }
                while(Rs2GroundItem.exists("egg",5)){
                    int searchRadius = 5;

                    // Get all ground items within the specified distance
                    RS2Item[] groundItems = Microbot.getClientThread().runOnClientThread(() ->
                            Rs2GroundItem.getAll(searchRadius)
                    );

                    // Find the nearest egg ground item
                    Optional<RS2Item> nearestEgg = Arrays.stream(groundItems)
                            .filter(item -> item.getItem().getName().equalsIgnoreCase("egg")) // Corrected to use getItem().getName()
                            .min(Comparator.comparingInt(item -> getDistanceToPlayer(item.getTile().getWorldLocation()))); // Find the nearest egg
                    Rs2Walker.walkTo(nearestEgg.get().getTile().getWorldLocation(),0);
                 //   sleep(5000);
                    while (Rs2Player.getWorldLocation().distanceTo(nearestEgg.get().getTile().getWorldLocation())>1){
                        Rs2Walker.walkFastCanvas(nearestEgg.get().getTile().getWorldLocation(),true);
                        sleepTillAnimationStop();
                    }
                    Rs2GroundItem.interact("egg","Take");
                    sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                    if (Rs2Inventory.contains("egg")){
                        break;
                    }
                }
            }


        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "COOKS_ASSISTANT"))&&("Purchase a pot from the Lumbridge General Store.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) && (Rs2Inventory.contains("bucket") && Rs2Inventory.contains("egg"))){
            Rs2Npc.interact(4626,"Talk-to");
            sleep(1200,2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(1200,2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(1200,2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(1200,2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(1200,2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(1200,2000);
            Rs2Walker.walkTo(Rs2GameObject.findObjectById(8689).getWorldLocation(),5);
            sleepTillAnimationStop();
            WorldPoint cowMilk = Rs2GameObject.findObjectById(8689).getWorldLocation();
        //while (Rs2Player.getWorldLocation().distanceTo(Rs2GameObject.findObjectById(8689).getWorldLocation())>1){
        // sleep(1000);
        // Rs2Walker.walkTo(Rs2GameObject.findObjectById(8689).getWorldLocation(),1);
       // }
        Rs2GameObject.interact(8689,"Milk");
        sleepTillAnimationStop();
        while (!Rs2Inventory.contains("Bucket of milk")){
            Rs2GameObject.interact(8689,"Milk");
          //  sleepUntilTrue(()->Rs2Dialogue.isInDialogue(),100,15000);
            while(Rs2Player.isAnimating() || Rs2Player.isInteracting() || Rs2Player.isMoving()){
                sleep(600,1500);
            }
        }
        WorldPoint targetTile = new WorldPoint(3211, 3213, 0);
        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        while (Rs2Player.getWorldLocation().distanceTo(targetTile) > 3) {
            Rs2Walker.walkTo(targetTile, 0);
            sleepTillAnimationStop();
        if (Rs2Player.getWorldLocation()== new WorldPoint(3200,3218,0)){
            Rs2Walker.walkFastCanvas(new WorldPoint(3202,3216,0),true);
            sleepTillAnimationStop();
        }
        }
        sleep(3000);
            while (!Rs2GroundItem.exists(1931,5)){
                sleep(1000);
            }
            while(!Rs2Inventory.contains(1931)){
                Rs2GroundItem.interact("pot","Take");
                sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
            }

            Rs2Walker.walkTo(new WorldPoint(3161,3295,0),10);
            sleepTillAnimationStop();
            WorldPoint targetTile4 = new WorldPoint(3161, 3295, 0);
           // WorldPoint playerLocation4 = Rs2Player.getWorldLocation();

            while (Rs2Player.getWorldLocation().distanceTo(targetTile4) > 1) {
                Rs2Walker.walkFastCanvas(targetTile4, true);
                sleepTillAnimationStop();
            }
            Rs2GameObject.interact(15506,"Pick");
            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 300, 5000);


        }
    if ((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "GOBLIN_DIPLOMACY"))&& ("You need three goblin mails, which you can find around the Goblin Village. The first is up the ladder in a crate in the south of the village.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))&& !Rs2Inventory.contains("goblin mail")){
        if (Rs2Inventory.contains("yellow dye") && Rs2Inventory.contains("orange dye")){
            sleep(2000,3000);
            Rs2Walker.walkTo(new WorldPoint(2956,3495,0),2);
            sleep(2000,3000);
        }
    if(!(Rs2Inventory.count("Onion") >= 2) && !Rs2Inventory.contains("yellow dye") &&!Rs2Inventory.contains("orange dye")){
        sleep(2000);
    Rs2Walker.walkTo(new WorldPoint(3190,3267,0),10);
        sleepTillAnimationStop();
        Rs2Walker.walkFastCanvas(new WorldPoint(3190,3267,0),true);
        sleepTillAnimationStop();
  //  Rs2Tile.getWalkableTilesAroundPlayer(1).stream().filter(x -> x.distanceTo(new WorldPoint(3190, 3267, 0)) == 1).findFirst().ifPresent(Rs2Walker::walkTo);
        WorldPoint targetTile = new WorldPoint(3190, 3267, 0);
        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        // Get reachable tiles within 2 tiles distance from the player
        HashMap<WorldPoint, Integer> reachableTiles = Rs2Tile.getReachableTilesFromTile(playerLocation, 2);

        // Check if the target tile is within the reachable tiles
        if (reachableTiles.containsKey(targetTile)) {
            // Perform the desired actions

            Rs2GameObject.interact("onion","Pick");
            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
            Rs2GameObject.interact("onion","Pick");
            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
        }

        }
        if((Rs2Inventory.count("Onion") >= 2) &&!(Rs2Inventory.hasItemAmount("woad leaf",2,true)) &&!Rs2Inventory.contains("orange dye")){
            Rs2Walker.walkTo(new WorldPoint(3025,3378,0),0);
            sleepTillAnimationStop();
            //Rs2Walker.walkFastCanvas(new WorldPoint(3025,3378,0),true);
            //sleepTillAnimationStop();
            WorldPoint targetTile = new WorldPoint(3025, 3378, 0);
            WorldPoint playerLocation = Rs2Player.getWorldLocation();

            // Get reachable tiles within 2 tiles distance from the player
            HashMap<WorldPoint, Integer> reachableTiles = Rs2Tile.getReachableTilesFromTile(playerLocation, 2);

            // Check if the target tile is within the reachable tiles
            if (reachableTiles.containsKey(targetTile)) {
                // Perform the desired actions
                Rs2Npc.interact(5422,"Talk");
                sleepTillAnimationStop();
            }

        }
        if((Rs2Inventory.count("Onion") >= 2) &&(Rs2Inventory.hasItemAmount("woad leaf",2,true)) &&!(Rs2Inventory.hasItemAmount("redberries",3,false))  && !Rs2Inventory.contains("red dye")&&!Rs2Inventory.contains("orange dye")){

            Rs2Walker.walkTo(new WorldPoint(3273,3375,0),10);
            sleepTillAnimationStop();
            Rs2Walker.walkFastCanvas(new WorldPoint(3273,3375,0),true);
            sleepTillAnimationStop();
            WorldPoint targetTile = new WorldPoint(3273, 3375, 0);
            WorldPoint playerLocation = Rs2Player.getWorldLocation();

            // Get reachable tiles within 2 tiles distance from the player

            HashMap<WorldPoint, Integer> reachableTiles = Rs2Tile.getReachableTilesFromTile(playerLocation, 1);

            // Check if the target tile is within the reachable tiles
            if (reachableTiles.containsKey(targetTile)) {
                while (Rs2GameObject.findObjectByIdAndDistance(23628,1)==null){
                    sleep(1000);
                }

                // Perform the desired actions
                Rs2GameObject.interact("redberry","Pick");
                sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                Rs2GameObject.interact("redberry","Pick");
                sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                if (((Rs2Inventory.count("Onion") >= 2) &&(Rs2Inventory.hasItemAmount("woad leaf",2,true)) && ((Rs2Inventory.hasItemAmount("redberries",3,false))))){
                    dyesItemsCollected = true;
                }
            }

        }
        if (((Rs2Inventory.count("Onion") >= 2) &&(Rs2Inventory.hasItemAmount("woad leaf",2,true)) && ((Rs2Inventory.hasItemAmount("redberries",3,false))))){
            dyesItemsCollected = true;
        }
        if(dyesItemsCollected && !( Rs2Inventory.contains("blue dye")  && Rs2Inventory.contains("orange dye"))){
            while(!(Rs2Inventory.contains("blue dye") && Rs2Inventory.contains("orange dye"))){
            Rs2Walker.walkTo(new WorldPoint(3087,3258,0),10);
                sleepTillAnimationStop();
                Rs2Walker.walkFastCanvas(new WorldPoint(3087,3258,0));
                sleepTillAnimationStop();
            WorldPoint targetTile = new WorldPoint(3087, 3258, 0);
            WorldPoint playerLocation = Rs2Player.getWorldLocation();

            // Get reachable tiles within 2 tiles distance from the player

            HashMap<WorldPoint, Integer> reachableTiles = Rs2Tile.getReachableTilesFromTile(playerLocation, 1);
                while (reachableTiles.containsKey(targetTile)) {
                // Perform the desired actions
                Rs2Npc.interact("aggie","Talk");
                if (dyesItemsCollected && !Rs2Inventory.contains("red dye")){
                    Rs2Npc.interact("aggie","Talk");
                    sleep(3000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(3000,3500);
                    Rs2Widget.clickWidget("What could you make for me?");
                    sleep(3000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(2000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(2000,3500);
                    Rs2Widget.clickWidget("What do you need to make red dye?");
                    sleep(2000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(2000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(2000,3500);
                    Rs2Widget.clickWidget("Okay, make me some red dye please.");
                    sleep(2000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(2000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE); //red Die is DOne here
                    sleep(2000,3500);

                }
                if (Rs2Inventory.contains("red dye") && !Rs2Inventory.contains("orange dye") && !Rs2Inventory.contains("yellow dye") && !Rs2Inventory.contains("blue dye")) {
                    Rs2Npc.interact("aggie","Talk");
                    sleep(3000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(3000,3500);
                    Rs2Widget.clickWidget("What could you make for me?");
                    sleep(3000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(2000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(2000,3500);
                    Rs2Widget.clickWidget("What do you need to make yellow dye?");
                    sleep(2000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(2000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(2000,3500);
                    Rs2Widget.clickWidget("Okay, make me some yellow dye please.");
                    sleep(2000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(2000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE); //Yellow Die is DOne here
                    sleep(2000,3500);
                }
                if (Rs2Inventory.contains("red dye") && !Rs2Inventory.contains("orange dye") && Rs2Inventory.contains("yellow dye") && !Rs2Inventory.contains("blue dye")) {
                    Rs2Npc.interact("aggie","Talk");
                    sleep(3000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(3000,3500);
                    Rs2Widget.clickWidget("What could you make for me?");
                    sleep(3000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(2000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(2000,3500);
                    Rs2Widget.clickWidget("What do you need to make blue dye?");
                    sleep(2000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(2000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(2000,3500);
                    Rs2Widget.clickWidget("Okay, make me some blue dye please.");
                    sleep(2000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(2000,3500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE); //blue Die is DOne here
                    sleep(2000,3500);
                }
                if (Rs2Inventory.contains("red dye") && Rs2Inventory.contains("blue dye") && Rs2Inventory.contains("yellow dye") && !Rs2Inventory.contains("orange dye")){
                    Rs2Inventory.use("red dye");
                    sleep(1000,1500);
                    Rs2Inventory.use("yellow dye");
                    sleep(1000,1500);
                }
                if ( Rs2Inventory.contains("blue dye")  && Rs2Inventory.contains("orange dye")){
                    break;
                }
            }
            }

        }
    if (Rs2Inventory.contains("blue dye")  && Rs2Inventory.contains("orange dye")){
        dyesAreReady=true;
    }
    if(dyesAreReady && !Rs2Inventory.contains("goblin mail")){
        sleep(2000);
        Rs2Walker.walkTo(new WorldPoint(2956,3495,0),10);
        sleepTillAnimationStop();
        Rs2Walker.walkFastCanvas(new WorldPoint(2956,3495,0));
        sleepTillAnimationStop();
        WorldPoint targetTile = new WorldPoint(2956, 3495, 0);
        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        // Get reachable tiles within 2 tiles distance from the player

        HashMap<WorldPoint, Integer> reachableTiles = Rs2Tile.getReachableTilesFromTile(playerLocation, 1);
        if (reachableTiles.containsKey(targetTile)) {
            sleep(2000);
            // Perform the desired actions
        }
    }
    }
        for (var requirement : requirements){
            if (requirement instanceof ItemRequirement){
                var itemRequirement = (ItemRequirement) requirement;

                if (itemRequirement.isEquip() && Rs2Inventory.contains(itemRequirement.getAllIds().toArray(new Integer[0]))
                    && itemRequirement.getAllIds().stream().noneMatch(Rs2Equipment::isWearing)){
                    Rs2Inventory.wear(itemRequirement.getAllIds().stream().filter(Rs2Inventory::contains).findFirst().orElse(-1));
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        reset();
    }
public boolean talktoaubury2ndtime = false;
    public static void reset() {
        itemsMissing = new ArrayList<>();
        itemRequirements = new ArrayList<>();
        grandExchangeItems = new ArrayList<>();
    }

    public boolean applyStep(QuestStep step) {
        if (step == null) return false;

        if (step instanceof ObjectStep) {
            return applyObjectStep((ObjectStep) step);
        } else if (step instanceof NpcStep) {
            return applyNpcStep((NpcStep) step);
        } else if (step instanceof WidgetStep){
            return applyWidgetStep((WidgetStep) step);
        } else if (step instanceof DigStep){
            return applyDigStep((DigStep) step);
        } else if (step instanceof PuzzleStep){
            return applyPuzzleStep((PuzzleStep) step);
        } else if (step instanceof DetailedQuestStep) {
            return applyDetailedQuestStep((DetailedQuestStep) step);
        }
        return true;
    }
public boolean talktoaubruayagain= false;
    public boolean collectcavandaberries= false;
    public boolean applyNpcStep(NpcStep step) {
        var npcs = step.getNpcs();
        var npc = npcs.stream().findFirst().orElse(null);
if (Rs2Widget.hasWidget("Congratulations, you've completed a quest: <col=081190>Rune Mysteries</col>")){
sleep(2000);
    Rs2Keyboard.keyPress(KeyEvent.VK_ESCAPE);
    sleep(1000);
    Rs2Tab.switchToPrayerTab();
    sleep(2000);
    Rs2Widget.clickWidget(218,7);
    sleep(15000);

}
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "PIRATES_TREASURE")) && ("Talk to Zambo in the Karamja Wines, Spirits and Beers bar. Buy one Karamjan rum.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) && !Rs2Inventory.contains("Karamjan rum")){
            while (Rs2Npc.getNpc("Zembo")==null){
                sleep(5000);
                Rs2Walker.walkTo(new WorldPoint(2929,3144,0),0);
                sleep(1000);
                while (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(2929,3144,0))>1){
                    sleep(1000);
                    Rs2Walker.setTarget(new WorldPoint(2929,3144,0));
                    Rs2Walker.walkTo(new WorldPoint(2929,3144,0),0);
                }
            }
            Rs2Npc.interact("Zembo","Trade");
            sleep(3000);
            while (!Rs2Inventory.contains("Karamjan rum")){
                Rs2Npc.interact("Zembo","Trade");
                sleep(3000);
                NewMenuEntry menuEntry = new NewMenuEntry(
                        "Buy 1",                          // Option
                        "<col=ff9040>Karamjan rum</col>", // Target
                        2,                                // Identifier
                        MenuAction.CC_OP,                 // Type (use MenuAction enum)
                        2,                                // Param0
                        19660816,                         // Param1
                        false                             // ForceLeftClick
                );
                menuEntry.setItemId(431); // ItemId

                // Use Microbot's method to send the menu entry
                Microbot.doInvoke(menuEntry, new Rectangle(1, 1, Microbot.getClient().getCanvasWidth(), Microbot.getClient().getCanvasHeight()));
                sleep(2000,3000);
            }
        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "WITCHS_POTION")) && ("Kill a rat in the house to the west for a rat tail.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) && !Rs2Inventory.contains("Rat's tail")){
if (!Rs2Player.isMoving() && !Rs2Player.isAnimating() && !Rs2Player.isInteracting()){
            while(!Rs2Inventory.contains("Rat's tail")){
                if (Rs2GroundItem.exists("Rat's tail",2)){
                    Rs2GroundItem.interact("Rat's tail","Take");
                    sleep(2000);
                }

                    Rs2Npc.interact("Rat","Attack");
                    sleep(2000);


            }
}

        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "COOKS_ASSISTANT")) && ("Give the Cook in Lumbridge Castle's kitchen the required items to finish the quest.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) ){
            Rs2Walker.walkTo(new WorldPoint(3210,3213,0),0);
            sleep(2000);
            WorldPoint targetTile = new WorldPoint(3210, 3213, 0);
            WorldPoint playerLocation = Rs2Player.getWorldLocation();
            while (Rs2Player.getWorldLocation().distanceTo(targetTile) > 1) {
                // sleep(3000);
                Rs2Walker.walkTo(targetTile, 0);
                if (!Rs2Player.isMoving()){
                   Rs2Walker.setTarget(targetTile);
                    sleep(1000);
                    Rs2Walker.walkTo(targetTile, 0);
                    sleep(1000);
                }
            }
            while (Rs2Player.isWalking() || Rs2Player.isMoving() || Rs2Player.isAnimating()){
                sleep(1000);
            }

            if (!Rs2Inventory.contains("egg")){
                Rs2Npc.interact(4626,"Talk-to");
                sleep(2000);
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                sleep(2000);
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                sleep(2000);
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                sleep(2000);
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                sleep(2000);
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                sleep(2000);
            }



        }
        if (step.isAllowMultipleHighlights()){
            if (npcs.stream().anyMatch(x -> !npcsHandled.contains(x)))
                npc = npcs.stream().filter(x -> !npcsHandled.contains(x)).findFirst().orElse(null);
            else
                npc = npcs.stream().min(Comparator.comparing(x -> Rs2Player.getWorldLocation().distanceTo(x.getWorldLocation()))).orElse(null);
        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "ROMEO__JULIET"))&&("Talk to Romeo in Varrock Square.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))&& !collectcavandaberries){
        Rs2Walker.walkTo(new WorldPoint(3277,3373,0),0);
            while ((Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3277, 3373, 0)))>1){
                sleep(1000);
            }
        Rs2GameObject.interact("Cadava Bush","Pick-from");
            sleep(3000);
            while(!Rs2Inventory.contains("Cadava berries")){
                Rs2GameObject.interact("Cadava Bush","Pick-from");
                sleep(6000);
            }
            collectcavandaberries=true;
        }
 if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "RUNE_MYSTERIES")) && ("Bring the Research Package to Aubury in south east Varrock.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) && Rs2Tile.isTileReachable(new WorldPoint(3107,3161,0)) && !TheRestLessGhostOutOfTower ){
        sleep(3000);
        if(findObjectByIdWithinDistance(new WorldPoint(3107,3162,0),3,23972)){
            Rs2GameObject.interact(23972) ; //Open entrance door to tower
        }
        sleep(2000);
        Rs2Walker.walkTo(new WorldPoint(3112, 3165, 0),0);
        sleep(2000);
        if(findObjectByIdWithinDistance(new WorldPoint(3109,3167,0),3,23972)){
            Rs2GameObject.interact(23972) ; //Open entrance door to tower
        }
        sleep(2000);
        Rs2Walker.walkTo(new WorldPoint(3113, 3171, 0),0);
        sleep(2000);
        Rs2Walker.walkTo(new WorldPoint(3251, 3393, 0),0);
        sleep(2000);
        TheRestLessGhostOutOfTower=true;
    }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "RUNE_MYSTERIES")) && ("Talk to Aubury again in south east Varrock.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))  && !talktoaubruayagain ){
            sleep(2000);
            Rs2Npc.interact(11434,"Talk-to");
            sleep(2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(2000);
            Rs2Widget.clickWidget("Anything useful in that package I gave you?");
            sleep(2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(2000);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            talktoaubruayagain=true;
            sleep(4000);
        }
        // Workaround for instances
        if (npc != null && Rs2Camera.isTileOnScreen(npc.getLocalLocation()) && (Microbot.getClient().isInInstancedRegion() || Rs2Npc.canWalkTo(npc, 10))) {
            // Stop pathing
            Rs2Walker.setTarget(null);

            if (step.getText().stream().anyMatch(x -> x.toLowerCase().contains("kill"))) {
                if (!Rs2Combat.inCombat())
                    Rs2Npc.interact(step.npcID, "Attack");

                return true;
            }

            if (step instanceof NpcEmoteStep){
                var emoteStep = (NpcEmoteStep)step;

                for (Widget emoteWidget : Rs2Widget.getWidget(ComponentID.EMOTES_EMOTE_CONTAINER).getDynamicChildren())
                {
                    if (emoteWidget.getSpriteId() == emoteStep.getEmote().getSpriteId())
                    {
                        var id = emoteWidget.getOriginalX() / 42 + ((emoteWidget.getOriginalY() - 6) / 49) * 4;

                        Microbot.doInvoke(new NewMenuEntry("Perform", emoteWidget.getText(), 1, MenuAction.CC_OP, id, ComponentID.EMOTES_EMOTE_CONTAINER, false), new Rectangle(0, 0, 1, 1));
                        Rs2Player.waitForAnimation();

                        if (Rs2Dialogue.isInDialogue())
                            return false;
                    }
                }
            }

            var itemId = step.getIconItemID();
            if (itemId != -1){
                Rs2Inventory.use(itemId);
                Rs2Npc.interact(npc);
            } else
                Rs2Npc.interact(npc, chooseCorrectNPCOption(step, npc));

            if (step.isAllowMultipleHighlights()){
                npcsHandled.add(npc);
                // Might open up a dialog
                sleepUntil(Rs2Dialogue::isInDialogue);
            }
        } else if (npc != null && !Rs2Camera.isTileOnScreen(npc.getLocalLocation())) {
            Rs2Walker.walkTo(npc.getWorldLocation(), 2);
        } else if (npc != null && (!Rs2Npc.hasLineOfSight(npc) || !Rs2Npc.canWalkTo(npc, 10))) {
            Rs2Walker.walkTo(npc.getWorldLocation(), 2);
        } else {
            if (step.getWorldPoint().distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation()) > 3) {
                Rs2Walker.walkTo(step.getWorldPoint(), 2);
                return false;
            }
        }
        return true;
    }
        private static final int WALL_OBJECT_ID = 44603;

    public void interactWithObjectAtLocation(WorldPoint targetLocation, String interactionType) {
        Client client = Microbot.getClient();
        List<GameObject> gameObjects = getGameObjects(); // Assuming this method retrieves all game objects

        Optional<GameObject> targetObject = gameObjects.stream()
                .filter(obj -> obj.getWorldLocation().equals(targetLocation))
                .findFirst();

        if (targetObject.isPresent()) {
            GameObject object = targetObject.get();
            Rs2GameObject.interact(object,"Open");
            // Assuming there's a method to interact with the object based on the interaction type
            // This part of the code will vary depending on how your game framework handles interactions
            switch (interactionType.toLowerCase()) {
                case "click":
                    Rs2GameObject.interact(object);
                    break;
                case "examine":
                    Rs2GameObject.interact(object,"open");
                    break;
                default:
                    System.out.println("Unsupported interaction type.");
                    break;
            }
        } else {
            System.out.println("No object found at the specified location.");
        }
    }
    public static List<GameObject> findObjectsWithinRadius(WorldPoint centerPoint, int distance) {
        List<GameObject> allGameObjects = getGameObjects(); // Retrieve all game objects
        List<GameObject> objectsWithinDistance = new ArrayList<>();

        for (GameObject gameObject : allGameObjects) {
            if (gameObject.getWorldLocation().distanceTo(centerPoint) <= distance) {
                objectsWithinDistance.add(gameObject);
            }
        }

        return objectsWithinDistance;
    }

    public static boolean findObjectByIdWithinDistance(WorldPoint centerPoint, int distance, int objectId) {
        List<GameObject> allGameObjects = getGameObjects(); // Retrieve all game objects

        for (GameObject gameObject : allGameObjects) {
            if (gameObject.getWorldLocation().distanceTo(centerPoint) <= distance && gameObject.getId() == objectId) {
                return true; // Found a matching object within the specified distance
            }
        }

        return false; // No matching object found within the specified distance
    }

    public boolean walkToCoffin = false;
    public boolean basementOfWizardTower = false;
    public boolean walkToCoffinfinal= false;
    public boolean walktospindle1in=false;
    public boolean basementOfWizardTower2=false;
    public boolean TheRestLessGhostOutOfTower=false;
    public boolean backtosedridorbasement= false;
    public boolean outbOfbacktosedridorbasement= false;
    public boolean talktojuliet = false;
    public boolean reachedLeverEPullLoc = false;
    public boolean applyObjectStep(ObjectStep step) {
        var object = step.getObjects().stream().findFirst().orElse(null);
        var itemId = step.getIconItemID();

        if (step.getObjects().size() > 1){
            if (step.getObjects().stream().anyMatch(x -> !objectsHandeled.contains(x)))
                object = step.getObjects().stream().filter(x -> !objectsHandeled.contains(x)).findFirst().orElse(null);
            else
                object = step.getObjects().stream().min(Comparator.comparing(x -> Rs2Player.getWorldLocation().distanceTo(x.getWorldLocation()))).orElse(null);
        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "PIRATES_TREASURE"))&&("Right-click fill the rest of the crate with bananas, then talk to Luthas.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))&& !(Rs2Inventory.count("Banana")>=10)){
        Rs2Walker.walkTo(new WorldPoint(2914,3161,0),0);
        sleep(2000);
        WorldPoint targetTile = new WorldPoint(2914, 3161, 0);
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        while (Rs2Player.getWorldLocation().distanceTo(targetTile) > 1) {
            sleep(2000);
            Rs2Walker.setTarget(targetTile);
            Rs2Walker.walkTo(targetTile, 0);
        }
        while (!(Rs2Inventory.count("Banana")>=10)){
            Rs2GameObject.interact(2073,"Pick");
            sleep(1000);
            while(Rs2Player.isMoving() || Rs2Player.isAnimating() || Rs2Player.isInteracting()){
                sleep(1000);
            }

        }

        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "PIRATES_TREASURE"))&&("Climb up the stairs in The Blue Moon Inn in Varrock.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))&& (Rs2Inventory.contains("Chest key"))){
            Rs2Walker.walkTo(new WorldPoint(3225,3390,0),0);
            sleep(2000);
            WorldPoint targetTile = new WorldPoint(3225, 3390, 0);
            WorldPoint playerLocation = Rs2Player.getWorldLocation();
            while (Rs2Player.getWorldLocation().distanceTo(targetTile) > 1) {
                sleep(2000);
                Rs2Walker.setTarget(targetTile);
                Rs2Walker.walkTo(targetTile, 0);
            }
        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "ROMEO__JULIET"))&&("Talk to Juliet in the house west of Varrock.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))&& !talktojuliet){
        Rs2Walker.walkTo(new WorldPoint(3168,3433,0),0);
        sleep(5000);
            talktojuliet=true;
        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "ERNEST_THE_CHICKEN")) && ("Use the poisoned fish food on the fountain south west of the manor.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) && Rs2Inventory.contains("Poisoned fish food") &&Rs2Tile.getWalkableTilesAroundPlayer(4).contains(new WorldPoint(3089,3335,0))){
            Rs2Inventory.use("Poisoned fish food");
        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "SHEEP_SHEARER")) && ("Climb the staircase in the Lumbridge Castle to spin the wool into balls of wool.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) && Rs2Inventory.hasItemAmount("Wool",20)){
            Rs2Walker.walkTo(new WorldPoint(3203,3212,0),0);
            sleep(2000);
            while (!(Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3203, 3212, 0)) ==0)){
                sleep(2000);
                Rs2Walker.setTarget(new WorldPoint(3203, 3212, 0));
                Rs2Walker.walkTo(new WorldPoint(3203, 3212, 0),0);
            }
        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "RUNE_MYSTERIES")) && ("Bring the research notes to Sedridor in the Wizard Tower's basement.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))  && !backtosedridorbasement ){
           // Rs2Inventory.use("Poisoned fish food");
            sleep(5000);
            Rs2Walker.walkTo(new WorldPoint(3113, 3171, 0),0);
            while ((Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3113, 3171, 0)) >1)){
                sleep(1000);
            }
            //  sleep(5000);
            if(findObjectByIdWithinDistance(new WorldPoint(3109,3167,0),3,23972)){
                Rs2GameObject.interact(23972) ; //Open entrance door to tower
            }
            sleep(5000);
            Rs2Walker.walkTo(new WorldPoint(3112, 3165, 0),0);
            while (!(Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3112, 3165, 0)) ==0)){
                sleep(2000,3000);
                // Parse a string input to an integer using Integer.parseInt method
                Rs2Walker.walkTo(new WorldPoint(3112, 3165, 0),0);
            }
            // sleep(5000);
            if(findObjectByIdWithinDistance(new WorldPoint(3107,3162,0),3,23972)){
                Rs2GameObject.interact(23972) ; //Open entrance door to tower
            }
            sleep(5000);
            Rs2Walker.walkTo(new WorldPoint(3105, 3160, 0),0);
            sleep(5000);
//            while (!(Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3105, 3159, 0)) <1)){
//                sleep(1000);
//                if(Rs2GameObject.findObjectByIdAndDistance())
//
//            }
            backtosedridorbasement=true;
        }
        if ((ShortestPathPlugin.getPathfinder() == null) && (Objects.equals(Microbot.getClient().getLocalPlayer().getWorldLocation(), new WorldPoint(3107, 9757, 0)))){
            sleep(2000);
            interactWithObjectAtLocation(new WorldPoint(3108, 9758, 0), "open");
            sleep(2000);
        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "ERNEST_THE_CHICKEN")) && ("Pull up lever B.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) ){
            sleep(2000);
            interactWithObjectAtLocation(new WorldPoint(3108, 9758, 0), "open");
            sleep(3000);
            Rs2Walker.walkTo(new WorldPoint(3118, 9752, 0));
        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "ERNEST_THE_CHICKEN")) && ("Pull down lever E.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) ){
            sleep(5000);
            interactWithObjectAtLocation(new WorldPoint(3102, 9758, 0), "open");
            sleep(5000);
            interactWithObjectAtLocation(new WorldPoint(3100, 9760, 0), "open");
            sleep(5000);
            interactWithObjectAtLocation(new WorldPoint(3097, 9763, 0), "open");
            sleep(5000);
            Rs2Walker.walkTo(new WorldPoint(3097, 9767, 0),0);
            sleep(5000);
//            interactWithObjectAtLocation(new WorldPoint(3097, 9767, 0), "Pull");
            Rs2GameObject.interact(150);
            sleep(2000);
        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "ERNEST_THE_CHICKEN")) && ("Pull down lever C.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) ){          sleep(5000);
            interactWithObjectAtLocation(new WorldPoint(3100, 9765, 0), "open");
            sleep(5000);
            interactWithObjectAtLocation(new WorldPoint(3105, 9765, 0), "open");
            sleep(5000);
            Rs2Walker.walkTo(new WorldPoint(3112, 9760, 0));
            sleep(5000);
//            interactWithObjectAtLocation(new WorldPoint(3108, 9758, 0), "open");
//            sleep(5000);
//            Rs2Walker.walkTo(new WorldPoint(3097, 9767, 0));
//            sleep(5000);
        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "ERNEST_THE_CHICKEN")) && ("Pull up lever E.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))&& !reachedLeverEPullLoc ){          sleep(5000);
            interactWithObjectAtLocation(new WorldPoint(3105, 9765, 0), "open");

            sleep(5000);
            interactWithObjectAtLocation(new WorldPoint(3100, 9765, 0), "open");
            sleep(5000);
            Rs2Walker.walkTo(new WorldPoint(3097, 9767, 0));
            sleep(5000);
            if (Rs2Tile.isTileReachable(new WorldPoint(3097, 9767, 0))){
                interactWithObjectAtLocation(new WorldPoint(3097, 9767, 0), "Pull");
                reachedLeverEPullLoc=true;
            }

        }

        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "THE_RESTLESS_GHOST")) && !walkToCoffin&& ("Open the coffin in the Lumbridge Graveyard to spawn the ghost.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) ){
            sleep(5000);
            Rs2Walker.walkTo(new WorldPoint(3152, 3166, 0));
            sleep(10000);
            Rs2Walker.walkTo(new WorldPoint(3248, 3192, 0));
            while (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3248, 3192, 0)) > 1){
                sleep(1000);
                Rs2Walker.setTarget(new WorldPoint(3248, 3192, 0));
                Rs2Walker.walkTo(new WorldPoint(3248, 3192, 0));
                walkToCoffin=true;
            }
        }
        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "THE_RESTLESS_GHOST")) && !basementOfWizardTower&& ("Enter the Wizards' Tower basement.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) ){
            sleep(5000);
            Rs2Walker.walkTo(new WorldPoint(3113, 3171, 0),0);
            while ((Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3113, 3171, 0)) >1)){
                sleep(1000);
            }
          //  sleep(5000);
            if(findObjectByIdWithinDistance(new WorldPoint(3109,3167,0),3,23972)){
                Rs2GameObject.interact(23972) ; //Open entrance door to tower
            }
            sleep(5000);
            Rs2Walker.walkTo(new WorldPoint(3112, 3165, 0),0);
            while (!(Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3112, 3165, 0)) ==0)){
                sleep(2000,3000);
                // Parse a string input to an integer using Integer.parseInt method
                Rs2Walker.walkTo(new WorldPoint(3112, 3165, 0),0);
            }
           // sleep(5000);
            if(findObjectByIdWithinDistance(new WorldPoint(3107,3162,0),3,23972)){
                Rs2GameObject.interact(23972) ; //Open entrance door to tower
            }
            sleep(5000);
            Rs2Walker.walkTo(new WorldPoint(3105, 3160, 0),0);
            sleep(5000);
//            while (!(Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3105, 3159, 0)) <1)){
//                sleep(1000);
//                if(Rs2GameObject.findObjectByIdAndDistance())
//
//            }
            basementOfWizardTower=true;
        } //

        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "SHEEP_SHEARER")) && !walktospindle1in && ("Climb the staircase in the Lumbridge Castle to spin the wool into balls of wool.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) && Rs2Inventory.isFull() ){
            sleep(3000);
            Rs2Walker.walkTo(new WorldPoint(3203, 3212, 0),0);
            while (!(Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3203, 3212, 0)) <1)){
                sleep(1000);
            }
            sleep(1000);

        }

        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "THE_RESTLESS_GHOST")) && !walkToCoffinfinal && ("Open the ghost's coffin in Lumbridge graveyard.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) ){
            sleep(3000);
            if(findObjectByIdWithinDistance(new WorldPoint(3107,3162,0),3,23972)){
                Rs2GameObject.interact(23972) ; //Open entrance door to tower
            }
            sleep(2000);
            Rs2Walker.walkTo(new WorldPoint(3112, 3165, 0),0);
            sleep(2000);
            if(findObjectByIdWithinDistance(new WorldPoint(3109,3167,0),3,23972)){
                Rs2GameObject.interact(23972) ; //Open entrance door to tower
            }
            sleep(2000);
            Rs2Walker.walkTo(new WorldPoint(3113, 3171, 0),0);
            sleep(2000);
            Rs2Walker.walkTo(new WorldPoint(3248, 3192, 0),0);
            while (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3248, 3192, 0)) > 1){
                sleep(1000);
                walkToCoffinfinal=true;
            }
        }

        if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "RUNE_MYSTERIES")) && ("Bring the Air Talisman to Sedridor in the Wizard Tower's basement.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString()))  ){
//Rs2Tile.getTilesAroundPlayer()
List <WorldPoint> Walkto =Rs2Tile.getWalkableTilesAroundPlayer(1);
            if (Walkto.contains(new WorldPoint(3206,3209,2))){
                Rs2GameObject.interact(16673);
                sleep(4000);
               // Rs2GameObject.interact(16672,"Climb-down");
            //    sleep(2000);
           //     Rs2Walker.walkTo(new WorldPoint(3113,3171,0),0);
           //     sleep(2000);
            }
            List <WorldPoint> Walkto1 =Rs2Tile.getWalkableTilesAroundPlayer(1);
        if (Walkto1.contains(new WorldPoint(3206,3209,1))){
            Rs2GameObject.interact(16673);
            sleep(2000);
            Rs2GameObject.interact(16672,"Climb-down");
            sleep(4000);
            Rs2Walker.walkTo(new WorldPoint(3113,3171,0),0);
            sleep(2000);
        }
            List <WorldPoint> Walkto2 =Rs2Tile.getWalkableTilesAroundPlayer(6);




        if(Walkto2.contains(new WorldPoint(3113, 3171, 0))&& !basementOfWizardTower2){

            while ((Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3113, 3171, 0)) >1)){
                sleep(3000);
                Rs2Walker.walkTo(new WorldPoint(3113, 3171, 0),0);
            }
            //  sleep(5000);
            if(findObjectByIdWithinDistance(new WorldPoint(3109,3167,0),3,23972)){
                Rs2GameObject.interact(23972) ; //Open entrance door to tower
            }
            sleep(5000);
            Rs2Walker.walkTo(new WorldPoint(3110, 3165, 0),0);
            while (!(Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3110, 3165, 0)) ==0)){
                sleep(2000,3000);
                    // Parse a string input to an integer using Integer.parseInt method
                    Rs2Walker.walkTo(new WorldPoint(3110, 3165, 0),0);
            }
            // sleep(5000);
            if(findObjectByIdWithinDistance(new WorldPoint(3107,3162,0),3,23972)){
                Rs2GameObject.interact(23972) ; //Open entrance door to tower
            }
            sleep(5000);
            Rs2Walker.walkTo(new WorldPoint(3105, 3160, 0),0);
            sleep(5000);
//            while (!(Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3105, 3159, 0)) <1)){
//                sleep(1000);
//                if(Rs2GameObject.findObjectByIdAndDistance())
//
//            }
            basementOfWizardTower2=true;
        }
        }


        if (object != null && unreachableTarget){
            var tileObjects = Rs2GameObject.getTileObjects().stream().filter(x -> x instanceof WallObject).collect(Collectors.toList());

            for (var tile : Rs2Tile.getWalkableTilesAroundTile(object.getWorldLocation(), unreachableTargetCheckDist)){
                if (tileObjects.stream().noneMatch(x -> x.getWorldLocation().equals(tile))){
                    if (!Rs2Walker.walkTo(tile) && ShortestPathPlugin.getPathfinder() == null)
                        return false;

                    sleepUntil(() -> ShortestPathPlugin.getPathfinder() == null || ShortestPathPlugin.getPathfinder().isDone());
                    if (ShortestPathPlugin.getPathfinder() == null || ShortestPathPlugin.getPathfinder().isDone()){
                        unreachableTarget = false;
                        unreachableTargetCheckDist = 1;
                    }
                    return false;
                }
            }

            unreachableTargetCheckDist++;
            return false;
        }

        /**
         * TODO: rework this block of code to handle walking closer to an object before interacting with it
         */
        if (step.getWorldPoint() != null && Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo2D(step.getWorldPoint()) > 1
                && !Rs2GameObject.canWalkTo(object, 10)) {
            WorldPoint targetTile = null;
            WorldPoint stepLocation = object == null ? step.getWorldPoint() : object.getWorldLocation();
            int radius = 0;
            while (targetTile == null) {
                if (mainScheduledFuture.isCancelled())
                    break;
                radius++;
                TileObject finalObject = object;
                targetTile = Rs2Tile.getWalkableTilesAroundTile(stepLocation, radius)
                        .stream().filter(x -> Rs2GameObject.hasLineOfSight(x, finalObject))
                        .sorted(Comparator.comparing(x -> x.distanceTo(Rs2Player.getWorldLocation()))).findFirst().orElse(null);

                if (radius > 10 && targetTile == null)
                    targetTile = stepLocation;
            }

            Rs2Walker.walkTo(targetTile, 1);

            if (ShortestPathPlugin.getPathfinder() != null){
                var path = ShortestPathPlugin.getPathfinder().getPath();
                if (path.get(path.size() - 1).distanceTo(step.getWorldPoint()) <= 1)
                    return false;
            } else
                return false;
        }

        if (Rs2GameObject.hasLineOfSight(object) || object != null && (Rs2Camera.isTileOnScreen(object) || object.getCanvasLocation() != null)){
            // Stop pathing
            Rs2Walker.setTarget(null);

            if (itemId == -1)
                Rs2GameObject.interact(object, chooseCorrectObjectOption(step, object));
            else{
                Rs2Inventory.use(itemId);
                Rs2GameObject.interact(object);
            }

            sleepUntil(() -> Rs2Player.isWalking() || Rs2Player.isAnimating());
            sleep(100);
            sleepUntil(() -> !Rs2Player.isWalking() && !Rs2Player.isAnimating());
            objectsHandeled.add(object);
        }

        return true;
    }

    private boolean applyDigStep(DigStep step){
        if (!Rs2Walker.walkTo(step.getWorldPoint()))
            return false;
        else if (!Rs2Player.getWorldLocation().equals(step.getWorldPoint()))
            Rs2Walker.walkFastCanvas(step.getWorldPoint());
        else {
            Rs2Inventory.interact(ItemID.SPADE, "Dig");
            return true;
        }

        return false;
    }

    private boolean applyPuzzleStep(PuzzleStep step){
        if (!step.getHighlightedButtons().isEmpty()){
            var widgetDetails = step.getHighlightedButtons().stream().filter(x -> Rs2Widget.isWidgetVisible(x.groupID, x.childID)).findFirst().orElse(null);
            if (widgetDetails != null){
                Rs2Widget.clickWidget(widgetDetails.groupID, widgetDetails.childID);
                return true;
            }
        }

        return false;
    }

    private String chooseCorrectObjectOption(QuestStep step, TileObject object){
        ObjectComposition objComp = Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getObjectDefinition(object.getId()));

        if (objComp == null)
            return "";

        String[] actions;
        if (objComp.getImpostorIds() != null) {
            actions = objComp.getImpostor().getActions();
        } else {
            actions = objComp.getActions();
        }

        for (var action : actions){
            if (action != null && step.getText().stream().anyMatch(x -> x.toLowerCase().contains(action.toLowerCase())))
                return action;
        }

        return "";
    }

    private String chooseCorrectNPCOption(QuestStep step, NPC npc){
        var npcComp = Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getNpcDefinition(npc.getId()));

        if (npcComp == null)
            return "Talk-to";

        for (var action : npcComp.getActions()){
            if (action != null && step.getText().stream().anyMatch(x -> x.toLowerCase().contains(action.toLowerCase())))
                return action;
        }

        return "Talk-to";
    }

    private String chooseCorrectItemOption(QuestStep step, int itemId){
        for (var action : Rs2Inventory.get(itemId).getInventoryActions()){
            if (action != null && step.getText().stream().anyMatch(x -> x.toLowerCase().contains(action.toLowerCase())))
                return action;
        }

        return "use";
    }

    private boolean applyDetailedQuestStep(DetailedQuestStep conditionalStep) {
        if (conditionalStep instanceof NpcStep) return false;

        if (conditionalStep.getIconItemID() != -1
                && conditionalStep.getWorldPoint() != null
                && !conditionalStep.getWorldPoint().toWorldArea().hasLineOfSightTo(Microbot.getClient().getTopLevelWorldView(), Rs2Player.getWorldLocation())) {
            if (Rs2Tile.areSurroundingTilesWalkable(conditionalStep.getWorldPoint(), 1, 1)) {
                WorldPoint nearestUnreachableWalkableTile = Rs2Tile.getNearestWalkableTileWithLineOfSight(conditionalStep.getWorldPoint());
                if (nearestUnreachableWalkableTile != null) {
                    return Rs2Walker.walkTo(nearestUnreachableWalkableTile, 0);
                }
            }
        }

        boolean usingItems = false;
        for (Requirement requirement : conditionalStep.getRequirements()) {
            if (requirement instanceof ItemRequirement) {
                ItemRequirement itemRequirement = (ItemRequirement) requirement;

                if (itemRequirement.shouldHighlightInInventory(Microbot.getClient())
                    && Rs2Inventory.contains(itemRequirement.getAllIds().toArray(new Integer[0]))) {
                    var itemId = itemRequirement.getAllIds().stream().filter(Rs2Inventory::contains).findFirst().orElse(-1);
                    Rs2Inventory.interact(itemId, chooseCorrectItemOption(conditionalStep, itemId));
                    sleep(100, 200);
                    usingItems = true;
                    continue;
                }
                if((Objects.equals(getQuestHelperPlugin().getSelectedQuest().getQuest().name(), "ERNEST_THE_CHICKEN")) && ("Pick up the oil can in the west room.".equals(getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep().getText().get(0).toString())) ){          sleep(5000);
                    interactWithObjectAtLocation(new WorldPoint(3100, 9765, 0), "open");
                    // interactWithObjectAtLocation(new WorldPoint(3105, 9765, 0), "open");

                    sleep(5000);
                    interactWithObjectAtLocation(new WorldPoint(3102, 9763, 0), "open");
                    sleep(5000);
                    interactWithObjectAtLocation(new WorldPoint(3102, 9758, 0), "open");
                    sleep(5000);
                    interactWithObjectAtLocation(new WorldPoint(3100, 9755, 0), "open");
                    sleep(5000);
                    Rs2Walker.walkTo(new WorldPoint(3093, 9755, 0));
                    sleep(5000);
                    Rs2GroundItem.interact("Oil can", "Take");
                    sleep(5000);
                    interactWithObjectAtLocation(new WorldPoint(3100, 9755, 0), "open");
                    sleep(5000);
                }
                if (!Rs2Inventory.contains(itemRequirement.getAllIds().toArray(new Integer[0])) && conditionalStep.getWorldPoint() != null) {
                    if (Rs2Walker.canReach(conditionalStep.getWorldPoint()) &&
                            (conditionalStep.getWorldPoint().distanceTo(Rs2Player.getWorldLocation()) < 2)
                            || conditionalStep.getWorldPoint().toWorldArea().hasLineOfSightTo(Microbot.getClient().getTopLevelWorldView(), Microbot.getClient().getLocalPlayer().getWorldLocation().toWorldArea())
                            && Rs2Camera.isTileOnScreen(LocalPoint.fromWorld(Microbot.getClient().getTopLevelWorldView(), conditionalStep.getWorldPoint()))) {
                        Rs2GroundItem.loot(itemRequirement.getId());
                    } else {
                        Rs2Walker.walkTo(conditionalStep.getWorldPoint(), 0);
                    }
                    return true;
                } else if (!Rs2Inventory.contains(itemRequirement.getAllIds().toArray(new Integer[0]))){
                    Rs2GroundItem.loot(itemRequirement.getId());
                    return true;
                }
            }
        }

        if (!usingItems && conditionalStep.getWorldPoint() != null && !Rs2Walker.walkTo(conditionalStep.getWorldPoint()))
            return true;

        if (conditionalStep.getIconItemID() != -1 && conditionalStep.getWorldPoint() != null
        && conditionalStep.getWorldPoint().toWorldArea().hasLineOfSightTo(Microbot.getClient().getTopLevelWorldView(), Rs2Player.getWorldLocation())) {
            if (conditionalStep.getQuestHelper().getQuest() == QuestHelperQuest.ZOGRE_FLESH_EATERS) {
                if (conditionalStep.getIconItemID() == 4836) { // strange potion
                    Rs2GroundItem.interact(ItemID.CUP_OF_TEA_4838, "", 20);
                }
            }
        }

        return usingItems;
    }

    private boolean applyWidgetStep(WidgetStep step) {
        var widgetDetails = step.getWidgetDetails().get(0);
        var widget = Microbot.getClient().getWidget(widgetDetails.groupID, widgetDetails.childID);

        if (widgetDetails.childChildID != -1){
            var tmpWidget = widget.getChild(widgetDetails.childChildID);

            if (tmpWidget != null)
                widget = tmpWidget;
        }

        return Rs2Widget.clickWidget(widget.getId());
    }

    protected QuestHelperPlugin getQuestHelperPlugin() {
        return (QuestHelperPlugin)Microbot.getPluginManager().getPlugins().stream().filter(x -> x instanceof QuestHelperPlugin).findFirst().orElse(null);
    }

    public void onChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getMessage().equalsIgnoreCase("I can't reach that!"))
            unreachableTarget = true;
    }
}
