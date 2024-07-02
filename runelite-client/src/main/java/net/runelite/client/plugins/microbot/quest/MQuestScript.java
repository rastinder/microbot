package net.runelite.client.plugins.microbot.quest;

import net.runelite.api.NPC;
import net.runelite.api.Quest;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.questhelper.QuestHelperPlugin;
import net.runelite.client.plugins.questhelper.questhelpers.QuestHelper;
import net.runelite.client.plugins.questhelper.requirements.Requirement;
import net.runelite.client.plugins.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.questhelper.steps.*;
import net.runelite.client.plugins.questhelper.managers.QuestManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MQuestScript extends Script {
    public static double version = 0.2;
    public static List<ItemRequirement> itemRequirements = new ArrayList<>();
    public static List<ItemRequirement> itemsMissing = new ArrayList<>();
    public static List<ItemRequirement> grandExchangeItems = new ArrayList<>();
    private MQuestConfig config;
    private QuestHelperPlugin questHelper;


    public boolean run(MQuestConfig config,QuestHelperPlugin questHelper) {
        this.config = config;
        this.questHelper = questHelper;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                String name = questHelper.getSelectedQuest().getQuest().getName();
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (questHelper.getSelectedQuest() != null && !Microbot.getClientThread().runOnClientThread(() -> questHelper.getSelectedQuest().isCompleted())) {
//                    Widget widget = Rs2Widget.findWidget("Start ");
//                    if (Rs2Widget.hasWidget("select an option") && QuestHelperPlugin.getSelectedQuest().getQuest().getId() != Quest.COOKS_ASSISTANT.getId() || (widget != null &&
//                            Microbot.getClientThread().runOnClientThread(() -> widget.getParent().getId()) != 10616888)) {
//                        Rs2Keyboard.keyPress('1');
//                        Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
//                        return;
//                    }
                    sleep(410,680);
                    long lastCheck = System.currentTimeMillis();
                    while (System.currentTimeMillis() - lastCheck >= 500) {
                        if (Rs2Widget.hasWidget("Click here to continue")) {
                            sleep(50,150);
                            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                            lastCheck = System.currentTimeMillis();
                        }
                        sleep(50);
                    }
                    if (Rs2Widget.getWidget(219, 1) != null && Rs2Widget.findWidget("[") != null) {
                        // Widget[] choices = Rs2Widget.getWidget(219,1).getDynamicChildren();
                        System.out.println(Rs2Widget.findWidget("[").getText());
                        Rs2Widget.clickWidget(Rs2Widget.findWidget("[").getText());
                        return;
                    } else if (Rs2Widget.hasWidget("Start")) {
                        Rs2Widget.clickWidget(Rs2Widget.findWidget("Yes").getText());
                        return;
                    } else { // strong hold of secority
                        if (Rs2Widget.hasWidget("Report")){

                        }

                    }

                    if (Rs2Player.isInteracting() || Rs2Player.isAnimating()) {
                        return;
                    }

                    boolean isInCutscene = Microbot.getVarbitValue(4606) > 0;
                    if (isInCutscene) {
                        return;
                    }

                    if (questHelper.getSelectedQuest().getQuest().getId() == Quest.THE_RESTLESS_GHOST.getId()) {
                        if (Rs2Inventory.hasItem("ghostspeak amulet")) {
                            Rs2Inventory.wear("ghostspeak amulet");
                        }
                    }

                    if (questHelper.getSelectedQuest().getQuest().getId() == Quest.RUNE_MYSTERIES.getId()) {
                        NPC aubury = Rs2Npc.getNpc("Aubury");
                        if (Rs2Inventory.hasItem("research package") && aubury != null) {
                            Rs2Npc.interact(aubury, "Talk-to");
                        }
                    }

                    if (questHelper.getSelectedQuest().getQuest().getId() == Quest.COOKS_ASSISTANT.getId()) {
                        NPC aubury = Rs2Npc.getNpc("Aubury");
                        if (Rs2Inventory.hasItem("research package") && aubury != null) {
                            Rs2Npc.interact(aubury, "Talk-to");
                        }
                    }
                    QuestStep questStep = questHelper.getSelectedQuest().getCurrentStep().getActiveStep();
                    if (questStep instanceof DetailedQuestStep && !(questStep instanceof NpcStep || questStep instanceof ObjectStep)) {
                        boolean result = applyDetailedQuestStep((DetailedQuestStep) questHelper.getSelectedQuest().getCurrentStep().getActiveStep());
                        if (result) {
                            return;
                        }
                    }

                    if (questHelper.getSelectedQuest().getCurrentStep() instanceof ConditionalStep) {
                        QuestStep conditionalStep = questHelper.getSelectedQuest().getCurrentStep().getActiveStep();
                        applyStep(conditionalStep);
                    } else if (questHelper.getSelectedQuest().getCurrentStep() instanceof NpcStep) {
                        applyNpcStep((NpcStep) questHelper.getSelectedQuest().getCurrentStep());
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, Random.random(400, 1000), TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        reset();
    }

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
        } else if (step instanceof DetailedQuestStep) {
            return applyDetailedQuestStep((DetailedQuestStep) step);
        }
        return true;
    }

    public boolean applyNpcStep(NpcStep step) {
        NPC npc = Rs2Npc.getNpc(step.npcID);
        if (npc != null && Rs2Camera.isTileOnScreen(npc.getLocalLocation()) && Rs2Npc.hasLineOfSight(npc)) {
            Rs2Npc.interact(step.npcID, "Talk-to");
        } else if (npc != null && !Rs2Camera.isTileOnScreen(npc.getLocalLocation())) {
            Rs2Walker.walkTo(npc.getWorldLocation());
        } else if (npc != null && !Rs2Npc.hasLineOfSight(npc)) {
            Rs2Walker.walkTo(npc.getWorldLocation());
        } else {
            if (step.getWorldPoint().distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation()) > 3) {
                Rs2Walker.walkTo(step.getWorldPoint());
                return false;
            }
        }
        return true;
    }


    public boolean applyObjectStep(ObjectStep step) {
        if (Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo2D(step.getWorldPoint()) > 5) {
            Rs2Walker.walkTo(step.getWorldPoint());
            return false;
        }
        boolean success = Rs2GameObject.interact(step.objectID, true);
        if (!success) {
            for (int objectId : step.alternateObjectIDs) {
                success = Rs2GameObject.interact(objectId, true);
                if (success) break;
            }
        }
//        if (!success) {
//            if (step.getWorldPoint().distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation()) > 3) {
//                if (config.enableHybridWalking()) {
//                    Rs2Walker.walkTo(step.getWorldPoint(), config.useNearest());
//                } else {
//                    Rs2Walker.walkTo(step.getWorldPoint(), true);
//                }
//                return false;
//            }
//        }
        return true;
    }

    private boolean applyDetailedQuestStep(DetailedQuestStep conditionalStep) {
        if (conditionalStep instanceof NpcStep) return false;
        for (Rs2Item item : Rs2Inventory.items()) {
            for (Requirement requirement : conditionalStep.getRequirements()) {
                if (requirement instanceof ItemRequirement) {
                    ItemRequirement itemRequirement = (ItemRequirement) requirement;

                    if (itemRequirement.getAllIds().contains(item.id)) {
                        if (itemRequirement.shouldHighlightInInventory(Microbot.getClient())) {
                            Rs2Inventory.use(item.id);
                        }
                    }

                    if (!itemRequirement.getAllIds().contains(item.id) && conditionalStep.getWorldPoint() != null) {
                        if (Rs2Walker.canReach(conditionalStep.getWorldPoint())) {
                            Rs2GroundItem.loot(itemRequirement.getId());
                        } else {
                            Rs2Walker.walkTo(conditionalStep.getWorldPoint());
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
