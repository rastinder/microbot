package net.runelite.client.plugins.microbot.firstTimeChecks;

import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.settings.Rs2Settings;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;


public class firstTimecheckScript extends Script {
    public static double version = 1.0;
    public static String randmills = randommil();
    public int attackStyleIs = 1;
    public int enemylevels = 0;
    public int progress = 0;;
    public String enemy = null;
    WorldPoint enemylocation = new WorldPoint(3246, 3238, 0);
    WorldPoint deathlocation = new WorldPoint(3176, 5726, 0);
    WorldPoint deathlocation1 = new WorldPoint(3222, 3218, 0);

    public boolean run(firstTimecheckConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();
                if (progress == 0) {
                    advJon();
                    progress++;
                }
                if (progress == 1) {
                    tutortalk();
                    progress++;
                }
                if (progress == 2) {
                    randomTalk();
                    progress++;
                }
                if (progress == 3) {
                    bank();
                    progress++;
                }
                if (progress == 4) {
                    //killOrSettings();
                    warnings();
                    progress++;
                }
                if (progress == 5) {
                    //rasMasterScriptScript masterControl = new rasMasterScriptScript();
                    rasMasterScriptScript.startPlugin("sos");
                    do{sleep(2000);}
                    while (rasMasterScriptScript.isPlugEnabled("sos"));
                    progress++;
                }
                if (progress == 6) {
                    shutdown();
                }


                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        stopPlugin("ras firsttime check");
        super.shutdown();
    }

    public static void warnings() {
        Rs2Tab.switchToSettingsTab();
        sleep(800);
        Rs2Widget.clickWidget(Rs2Widget.findWidget("ALL Settings").getText());
        sleep(800);
        Rs2Widget.clickWidget(Rs2Widget.findWidget("Warnings").getText());
        sleep(800);
        String alcho = Rs2Widget.getWidget(134,18).getDynamicChildren()[117].getText();
        System.out.println(alcho);
        //Rectangle widgtrs = Rs2Widget.getWidget(134,14).getDynamicChildren()[5].getBounds();
        Point widgtr = Rs2Widget.findWidget("Teleports").getCanvasLocation();
        Rs2Widget.clickWidget("Disable teleport");
        boolean again = false;
        for (int j = 0; j < 5; j++) {
            Microbot.getMouse().scrollDown(widgtr);
            sleep(310);
            Microbot.getMouse().scrollDown(widgtr);
            sleep(310);
            if (Rs2Widget.hasWidget("Disable tablet") && !again){
                Rs2Widget.clickWidget("Disable tablet");
                again = true;
            }
        //do{
            Microbot.getMouse().scrollDown(widgtr);
            sleep(310);
            if (Rs2Widget.getWidget(134,18).getDynamicChildren()[110] != null) {
                Widget alch = Rs2Widget.getWidget(134,18).getDynamicChildren()[110];
                System.out.print("visible");
                if (!alch.getText().contains(randmills)) {
                    Microbot.click(alch.getBounds());
                    sleep(510);
                    if (Rs2Widget.isWidgetVisible(162,37)) {
                        Rs2Keyboard.typeString(randmills);
                        sleep(110);
                        Rs2Keyboard.keyPress(KeyEvent.VK_ENTER);
                    }
                }
            }else
                System.out.print("not visible");
       // }while (!Rs2Widget.hasWidget("Alchemy spells warning"));
        Widget[] widgts = Rs2Widget.getWidget(134, 18).getDynamicChildren();
        for (Widget widgt : widgts) {
            System.out.println("Total time for loop " + widgt.getSpriteId());
            if (widgt.getSpriteId() == 2848 && !Rs2Widget.getWidget(8781834).getBounds().intersects(widgt.getBounds()) ) {
                sleep(300);
                Microbot.getMouse().click(widgt.getBounds());
                sleep(150);
            }
        }}
        sleep(200);
        Rs2Widget.clickWidget(Rs2Widget.findWidget("Interface").getText());
        sleep(400);
        widgtr = Rs2Widget.findWidget("General").getCanvasLocation();
        Microbot.getMouse().scrollDown(widgtr);
        sleep(400);
        if (Rs2Widget.getWidget(134,18).getDynamicChildren()[36].getSpriteId() == 2847)
            Microbot.getMouse().click(Rs2Widget.getWidget(134,18).getDynamicChildren()[36].getBounds());
        sleep(400);
        Rs2Keyboard.keyPress(KeyEvent.VK_ESCAPE);
    }

    private void bank() {
        if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3206, 3208, 0)) > 10) {
            Rs2Walker.walkTo(new WorldPoint(3206, 3208, 0), 1);
            sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3206, 3208, 0)) < 10, 20000);
        }
        if (random(0, 2) == 0) {
            Rs2GroundItem.loot("Mind rune", 10);
            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
        }
        Rs2Bank.walkToBank();
        sleepUntil(() -> Rs2Bank.isNearBank(5), 15000);
        Rs2Bank.openBank();
        sleepUntil(() -> Rs2Bank.isOpen(), 2000);
        sleep(450);
        if (Rs2Widget.isWidgetVisible(664, 29))
            Rs2Widget.clickWidget(664, 29);
        sleep(850);
        Rs2Bank.depositAll();
        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
        sleep(450);
        if (random(0,2) == 0){
            if (random(0,2) == 0){
                List<String> items = Arrays.asList("Training shield", "Training sword");
                Collections.shuffle(items);
                for (String item : items) {
                    Rs2Bank.withdrawAll(item);
                    sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                    sleep(280,350);
                }
            }
            else {
                List<String> items = Arrays.asList("Bronze sword", "Training sword");
                Collections.shuffle(items);
                for (String item : items) {
                    Rs2Bank.withdrawAll(item);
                    sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                    sleep(280,350);
                }
            }

        }
        Rs2Bank.closeBank();
    }

    private void advJon() {
        Rs2Tab.switchToInventoryTab();
        if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3232, 3234, 0)) > 10) {
            Rs2Walker.walkTo(new WorldPoint(3232, 3234, 0), 1);
            sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3232, 3234, 0)) < 2,15000);
        }
        Rs2Player.waitForAnimation(500);
        Rs2Walker.setTarget(null);
        NPC jon = Rs2Npc.getNpc(9244);
        if (jon != null) {
            Rs2Npc.interact(jon);
            sleepUntilTrue(()-> Rs2Player.isInteracting(),100,5000);
            int count = random(1, 3);
            int pressCount = 0;
            while (pressCount < 8) {
                if (Rs2Widget.hasWidget("Select an option"))
                    break;
                if (Rs2Widget.hasWidget("Click here to continue")) {
                    sleep(20, 60);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    pressCount++;
                }
            }
            pressCount = 0;
            while (pressCount < count) {
                sleep(20, 60);
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                pressCount++;
            }
        }
    }
    private void kill() {
        while(true) {
            if (randomattack()) {
                deathdialog();
                break;
            }
        }
    }
    private void killOrSettings() {
        List<Runnable> function = new ArrayList<>();
        function.add(() -> warnings());
        function.add(() -> kill());

        Collections.shuffle(function, new Random());
        for (Runnable functio : function) {
            functio.run();
        }
    }
    private void tutortalk() {
        List<Runnable> functions = new ArrayList<>();
        functions.add(() -> combattutor());
        functions.add(() -> magictutor());
        functions.add(() -> archtutor());

        Collections.shuffle(functions, new Random());
        for (Runnable function : functions) {
            function.run();
        }
    }

    private void magictutor() {
        if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3219, 3237, 0)) > 10) {
            Rs2Walker.walkTo(new WorldPoint(3219, 3237, 0), 1);
            sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3219, 3237, 0)) < 5,15000);
        }
        Rs2Player.waitForWalking();
        Rs2Walker.setTarget(null);
        NPC jon = Rs2Npc.getNpc(3218);
        if (jon != null) {
            List<Rs2Item> items = Rs2Inventory.items();
            for (Rs2Item item : items) {
                if (item.getName().contains("rune")) {
                    sleep(140, 400);
                    Rs2Inventory.drop(item.name);
                    sleep(300,400);
                }
            }
            Rs2Npc.interact(jon, "Claim");
            sleepUntilTrue(()-> Rs2Player.isInteracting(),100,5000);
            sleepUntil(() -> Rs2Widget.hasWidget("Click here to continue"), 5000);
            if (random(0, 2) == 0) {
                sleep(140, 400);
                Rs2Dialogue.clickContinue();
            }
        }
        RS2Item[] items = Rs2GroundItem.getAll(10);
        for (RS2Item item : items) {
            if (item.getItem().getName().contains("rune")) {
                Rs2GroundItem.loot(item.getItem().getId());
                sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
            }
        }
    }

    private void combattutor() {
        if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3219, 3237, 0)) > 10) {
            Rs2Walker.walkTo(new WorldPoint(3219, 3237, 0), 1);
            sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3219, 3237, 0)) < 2,15000);
        }
        Rs2Player.waitForWalking();
        Rs2Walker.setTarget(null);
        NPC jon = Rs2Npc.getNpc(3216);
        if (jon != null) {
            Rs2Npc.interact(jon);
            sleepUntilTrue(() -> Rs2Player.isInteracting(), 100, 5000);

            sleepUntil(() -> Rs2Widget.hasWidget("Click here to continue"), 5000);
            sleep(600, 800);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(1600, 1800);
            sleepUntilTrue(() -> Rs2Widget.hasWidget("sword"), 100, 5000);
            sleepUntil(() -> Rs2Widget.clickWidget(Rs2Widget.findWidget("sword").getText()));
            sleepUntil(() -> Rs2Widget.hasWidget("Click here to continue"), 5000);
            sleep(120, 280);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleepUntil(() -> Rs2Widget.hasWidget("Click here to continue"), 5000);
            sleep(120, 280);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
        }
    }

    private void archtutor() {
        if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3219, 3237, 0)) > 10) {
            Rs2Walker.walkTo(new WorldPoint(3219, 3237, 0), 1);
            sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3219, 3237, 0)) < 5,15000);
        }
        Rs2Player.waitForWalking();
        Rs2Walker.setTarget(null);
        NPC jon = Rs2Npc.getNpc(3217);
        if (jon != null) {
            Rs2Npc.interact(jon);
            sleepUntilTrue(()-> Rs2Player.isInteracting(),100,5000);

        sleepUntil(() -> Rs2Widget.hasWidget("Click here to continue"), 5000);
        sleep(600, 800);
        Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
        sleep(1600, 1800);
        sleepUntilTrue(()-> Rs2Widget.hasWidget("ammo"),100,5000);
        sleepUntil(() -> Rs2Widget.clickWidget(Rs2Widget.findWidget("ammo").getText()));
        waitAndPressContinue();
        sleepUntilTrue(()-> Rs2Widget.hasWidget("Automatically"),100,5000);
        sleepUntil(() -> Rs2Widget.clickWidget(Rs2Widget.findWidget("Automatically").getText()));
        waitAndPressContinue();
        sleepUntil(() -> Rs2Widget.hasWidget("Click here to continue"), 5000);
        waitAndPressContinue();
        if (random(0, 2) == 0) {
            sleep(140, 400);
            sleepUntilTrue(()-> Rs2Widget.hasWidget("Goodbye"),100,5000);
            sleepUntil(() -> Rs2Widget.clickWidget(Rs2Widget.findWidget("Goodbye").getText()));
            waitAndPressContinue();
        }
        }
    }

    private void randomTalk() {
        if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3223, 3246, 0)) > 10) {
            Rs2Walker.walkTo(new WorldPoint(3223, 3246, 0), random(1, 7));
            sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3223, 3246, 0)) < 5,15000);
        }
        Rs2Player.waitForWalking(500);
        Rs2Walker.setTarget(null);
        if (random(0, 2) == 0) {
            NPC jon = Rs2Npc.getNpc(3226);
            if (jon != null) {
                Rs2Npc.interact(jon);
                sleepUntil(()-> Rs2Dialogue.isInDialogue(),10000);
                if (random(0, 2) == 0) {
                    waitAndPressContinue();
                }
            }
        }
        if (random(0, 2) == 0) {
            NPC jon = Rs2Npc.getNpc(3224);
            if (jon != null) {
                Rs2Npc.interact(jon);
                sleepUntil(()-> Rs2Player.isInteracting(),10000);
                if (random(0, 2) == 0) {
                    waitAndPressContinue();
                }
            }
        }
    }

    private boolean randomattack() {
        System.out.println("attack");
        if (enemylevels < 10) {
            if (random(0, 2) == 0) {
                NPC jon = null;
                 jon = Rs2Npc.getNpc("Man");
                if (jon == null) {
                    if (random(0, 2) == 0) {
                        if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3223, 3239, 0)) > 10) {
                            Rs2Walker.walkTo(new WorldPoint(3223, 3239, 0), random(1, 7));
                            sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3223, 3239, 0)) < 10,60000);
                        }
                    } else {
                        if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3219, 3221, 0)) > 10) {
                            Rs2Walker.walkTo(new WorldPoint(3219, 3221, 0), random(1, 7));
                            sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3219, 3221, 0)) <  10,60000);
                        }

                    }
                }
                jon = Rs2Npc.getNpc("Man");
                if (jon != null && Rs2Npc.hasLineOfSight(jon)) {
                    Rs2Npc.attack(jon);
                    enemylevels = enemylevels + random(1, 3);
                    sleepUntil(()-> Rs2Player.isInteracting(),15000);
                    sleep(2000, 4000);
                    if (random(0, 2) == 0) {
                        switchToTab("inventory");
                        sleep(200, 400);
                        if (!Rs2Equipment.hasEquippedContains("Bronze sword") || !Rs2Equipment.hasEquippedContains("Training sword")) {
                            sleep(200, 400);
                            if (Rs2Inventory.hasItem("Bronze sword"))
                                Rs2Inventory.interact("Bronze sword","wield");
                            else if (Rs2Inventory.hasItem("Training sword"))
                                Rs2Inventory.interact("Training sword","wield");
                            if (Rs2Inventory.hasItem("Training shield")) {
                                sleep(400, 800);
                                Rs2Inventory.interact("Training shield", "Wear");
                            }
                        }

                    }
                    long endTime = System.currentTimeMillis() + 50000;
                    while (!jon.isDead() && System.currentTimeMillis() < endTime) {
                        sleep(400);
                        if (Rs2Player.getWorldLocation().distanceTo(deathlocation) < 10) {
                            return true;
                        }
                    }
                    sleep(1200, 1600);
                    if (random(0, 2) == 0) {
                        RS2Item[] groundItems = Rs2GroundItem.getAll(3);
                        for (RS2Item groundItem : groundItems) {
                            Rs2GroundItem.loot(groundItem.getItem().getId());
                            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                        }
                        while (Rs2Inventory.hasItem("Bones")) {
                            sleep(400, 800);
                            Rs2Inventory.interact("Bones", "Bury");
                            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);

                        }
                    }
                }
            }
            if (random(0, 2) == 0) {
                NPC jon = null;
                 jon = Rs2Npc.getNpc("Rat");
                if (jon == null) {
                    if (random(0, 2) == 0) {
                        if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3233, 3225, 0)) > 10) {
                            Rs2Walker.walkTo(new WorldPoint(3233, 3225, 0), random(1, 7));
                            sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3233, 3225, 0)) < 10,60000);
                        }
                    } else {
                        if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3219, 3221, 0)) > 10) {
                            Rs2Walker.walkTo(new WorldPoint(3219, 3221, 0), random(1, 7));
                            sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3219, 3221, 0)) < 10,60000);
                        }

                    }
                }
                Rs2Player.waitForWalking();
                jon = Rs2Npc.getNpc("Rat");
                if (jon != null && Rs2Npc.hasLineOfSight(jon)) {
                    Rs2Npc.attack(jon);
                    enemylevels = enemylevels + random(1, 3);
                    sleepUntil(()-> Rs2Player.isInteracting(),15000);
                    sleep(2000, 4000);
                    if (random(0, 2) == 0) {
                        switchToTab("inventory");
                        sleep(200, 400);
                        if (!Rs2Equipment.hasEquippedContains("Bronze sword") || !Rs2Equipment.hasEquippedContains("Training sword")) {
                            sleep(200, 400);
                            if (Rs2Inventory.hasItem("Bronze sword"))
                                Rs2Inventory.interact("Bronze sword","wield");
                            else if (Rs2Inventory.hasItem("Training sword"))
                                Rs2Inventory.interact("Training sword","wield");
                            if (Rs2Inventory.hasItem("Training shield")) {
                                sleep(400, 800);
                                Rs2Inventory.interact("Training shield", "Wear");
                            }
                        }
                        sleep(800, 1400);
                        if (random(0, 2) == 0) {
                            switchToTab("combat");
                            sleep(800, 1400);
                            if (attackStyleIs == 2) {
                                Rs2Combat.setAttackStyle(WidgetInfo.COMBAT_STYLE_ONE);
                                attackStyleIs = 1;
                            } else {
                                Rs2Combat.setAttackStyle(WidgetInfo.COMBAT_STYLE_TWO);
                                attackStyleIs = 2;
                            }
                        }

                    }
                    long endTime = System.currentTimeMillis() + 50000;
                    while (!jon.isDead() && System.currentTimeMillis() < endTime) {
                        sleep(400);
                        if (Rs2Player.getWorldLocation().distanceTo(deathlocation) < 10) {
                            return true;
                        }
                        if (Rs2Player.getWorldLocation().distanceTo(deathlocation1) < 5) {
                            return true;
                        }
                    }
                    sleep(1200, 1600);
                    if (random(0, 2) == 0) {
                        RS2Item[] groundItems = Rs2GroundItem.getAll(3);
                        for (RS2Item groundItem : groundItems) {
                            Rs2GroundItem.loot(groundItem.getItem().getId());
                            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                        }
                        while (Rs2Inventory.hasItem("Bones")) {
                            sleep(400, 800);
                            Rs2Inventory.interact("Bones", "Bury");
                            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);

                        }
                    }
                }
            }

        } else {
            if (enemy == null) {
                String[] options = {"Frog", "Goblin", "Giant rat"};
                 enemy = options[new Random().nextInt(options.length)];
                if (enemy.equals("Goblin")) {
                    enemylocation = new WorldPoint(3246, 3238, 0);
                }
                if (enemy.equals("Giant rat")) {
                    enemylocation = new WorldPoint(3196, 3202, 0);
                }
                if (enemy.equals("Frog")) {
                    enemylocation = new WorldPoint(3215, 3181, 0);
                }
            }
            NPC jon = null;
             jon = Rs2Npc.getNpc(enemy);
            if (jon == null) {
                if (Rs2Player.getWorldLocation().distanceTo(enemylocation) > 10) {
                    Rs2Walker.walkTo(enemylocation, random(1, 7));
                    sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(enemylocation) < 8,15000);
                }
            }
            jon = null;
            jon = Rs2Npc.getNpc(enemy);
            if (jon != null && Rs2Npc.hasLineOfSight(jon)) {
                Rs2Walker.setTarget(null);
                Rs2Npc.attack(jon);
                sleepUntil(()-> !Rs2Player.isInteracting(),15000);
                long endTime = System.currentTimeMillis() + 50000;
                while (!jon.isDead() && System.currentTimeMillis() < endTime) {
                    sleep(400);
                    if (Rs2Player.getWorldLocation().distanceTo(deathlocation) < 10) {
                        return true;
                    }
                    if (Rs2Player.getWorldLocation().distanceTo(deathlocation1) < 5) {
                        return true;
                    }
                }
                sleepUntil(jon::isDead, 20000);
                sleep(1200, 1600);
                if (random(0, 2) == 0) {
                    RS2Item[] groundItems = Rs2GroundItem.getAll(3);
                    for (RS2Item groundItem : groundItems) {
                        Rs2GroundItem.loot(groundItem.getItem().getId());
                        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                    }
                    while (Rs2Inventory.hasItem("Bones")) {
                        sleep(400, 800);
                        Rs2Inventory.interact("Bones", "Bury");
                        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);

                    }
                }
            }


        }
        return false;
    }

    public void switchToTab(String finalTabName) {
        Runnable[] tabSwitchers = {
                Rs2Tab::switchToInventoryTab,
                Rs2Tab::switchToSkillsTab,
                Rs2Tab::switchToEquipmentTab,
                Rs2Tab::switchToPrayerTab,
                Rs2Tab::switchToMagicTab,
                Rs2Tab::switchToFriendsTab,
                Rs2Tab::switchToAccountManagementTab,
                Rs2Tab::switchToSettingsTab,
                Rs2Tab::switchToEmotesTab,
                Rs2Tab::switchToMusicTab,
        };

        int switches = random(0, 5);
        for (int i = 0; i < switches; i++) {
            int randomTabIndex = random(0, tabSwitchers.length);
            tabSwitchers[randomTabIndex].run();
            sleep(400, 800);
        }

        switch (finalTabName.toLowerCase()) {
            case "combat":
                Rs2Tab.switchToCombatOptionsTab();
                break;
            case "inventory":
                Rs2Tab.switchToInventoryTab();
                break;
            case "skills":
                Rs2Tab.switchToSkillsTab();
                break;
            case "equipment":
                Rs2Tab.switchToEquipmentTab();
                break;
            case "prayer":
                Rs2Tab.switchToPrayerTab();
                break;
            case "magic":
                Rs2Tab.switchToMagicTab();
                break;
            case "friends":
                Rs2Tab.switchToFriendsTab();
                break;
            case "account":
                Rs2Tab.switchToAccountManagementTab();
                break;
            case "settings":
                Rs2Tab.switchToSettingsTab();
                break;
            case "emotes":
                Rs2Tab.switchToEmotesTab();
                break;
            case "music":
                Rs2Tab.switchToMusicTab();
                break;
            default:
                throw new IllegalArgumentException("Unknown tab name: " + finalTabName);
        }
    }
    public void deathdialog() {
        try {
            if (Rs2Player.getWorldLocation().distanceTo(deathlocation1) < 5) {
                return;
            }
            sleepUntilTrue(Rs2Dialogue::isInDialogue, 100, 5000);
            waitAndPressContinue();
            waitAndClickDialog("I pay");
            waitAndPressContinue();
            waitAndClickDialog("How long");
            waitAndPressContinue();
            waitAndClickDialog("I die");
            waitAndPressContinue();
            waitAndClickDialog("done here");
            waitAndPressContinue();
            Rs2GameObject.interact("Portal", "use");
            waitForAnimationStop();
            sleep(5000);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public void waitAndClickDialog(String dialog) throws Exception {
        sleepUntil(() -> Rs2Widget.hasWidget(dialog));

        if (Rs2Widget.hasWidget(dialog)) {
            Rs2Widget.clickWidget(Rs2Widget.findWidget(dialog).getText());
        } else {
            throw new Exception("Can't find dialog " + dialog);
        }
    }
    public void waitAndPressContinue() {
        int sleep = random(1300, 1600);
        long endTime = System.currentTimeMillis() + sleep;
        while (System.currentTimeMillis() < endTime) {
            if (Rs2Widget.hasWidget("Click here to continue")) {
                sleep(110, 280);
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                endTime = System.currentTimeMillis() + sleep;
            }
        }
    }
    public static String randommil(){
        Random random = new Random();

        int option = random.nextInt(2); // 0 or 1
        long number;

        if (option == 0) {
            int digit = random.nextInt(9) + 1; // 1 to 9
            number = digit * 1111111L; // Generates 1111111, 2222222, ..., 9999999
        } else {
            int firstDigit = random.nextInt(9) + 1; // 1 to 9
            int zeroCount = random.nextInt(2) + 6; // 6 to 7 zeros (for numbers between 10,000,000 to 100,000,000)
            number = firstDigit * (long) Math.pow(10, zeroCount); // Generates 1000000, 10000000, ..., 90000000
        }

        System.out.println(number);
        String numberStr = Long.toString(number);
        return  numberStr;
    }
    public void waitForAnimationStop() {
        long lastAnimationStopTime = System.currentTimeMillis();
        while (true) {
            sleep(100);
            if (Rs2Player.isAnimating()) {
                lastAnimationStopTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - lastAnimationStopTime >= 2000) {
                break;
            }
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

}

