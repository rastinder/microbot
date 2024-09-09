package net.runelite.client.plugins.microbot.sos;

import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;
import net.runelite.http.api.worlds.WorldType;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Comparator;

import static net.runelite.client.plugins.microbot.util.math.Random.random;


public class sosScript extends Script {
    public static double version = 1.0;
    String ansewr = "follow the advice,fake,Virus,Secure my device,Authenticator,banker,Recovery,me,Don't give them,never reused,Through account settings,report,famous person,No.,No,with my email,Me.,Read the text,never buy,Nobody.,Only on,";
    List<String> ansewrs = Arrays.asList(ansewr.split(","));
    boolean runInisiated = false;
    boolean depositeEverythingBeforeStart = false;
    WorldPoint getfishPoint = new WorldPoint(3105, 3432, 0);
    int level = 0;
    boolean onenable = true;


    public boolean run(sosConfig config) {
        Microbot.enableAutoRunOn = false;
        long startTime = System.currentTimeMillis();
        long stopTimer = random(1800000,2760000) + System.currentTimeMillis();
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                rasMasterScriptScript.autoShutdown("sos");
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (onenable) {
                    onenable = false;
                    changeWorld();
                    Microbot.getPluginManager().setPluginValue("shortestpath", "useTeleportationPortals", false);
                }
                if (runInisiated) {
                    Rs2Player.eatAt(80);
                    if (Rs2Dialogue.isInDialogue() || Rs2Widget.hasWidget("Warning") || Rs2Widget.hasWidget("boots you")){ if (solveansers()) return;} else {
                        if (Rs2Inventory.ItemQuantity(995) == 2000 && Rs2Player.getWorldLocation().distanceTo(new WorldPoint(2042, 5245, 0)) < 2)
                            level = 2;
                        else if (Rs2Inventory.ItemQuantity(995) == 5000  && Rs2Player.getWorldLocation().distanceTo(new WorldPoint(2123, 5252, 0)) < 2)
                            level = 3;
                        else if (Rs2Inventory.ItemQuantity(995) == 10000 && Rs2Player.getWorldLocation().distanceTo(new WorldPoint(2358, 5215, 0)) < 2)
                            level = 4;
                        if (level == 0) {
                            Rs2Walker.walkTo(new WorldPoint(1859, 5243, 0), 1);
                            waitForAnimationStop();
                            if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(1859, 5243, 0)) < 5) level = 1;
                        } else if (level == 1) {
                            if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(1908, 5222, 0)) > 10) {
                                Rs2Walker.walkTo(new WorldPoint(1908, 5222, 0), 1);
                                waitForAnimationStop();
                                return;
                            } else if (!Rs2Inventory.hasItem(995) && !Rs2Inventory.isFull() && Rs2GameObject.interact(20656)  ) {
                                waitForAnimationStop();
                                return;
                            } else if (Rs2Inventory.hasItem(995)){ //&& Rs2Player.getWorldLocation().distanceTo(new WorldPoint(1902, 5222, 0)) < 2) {
                                if (Rs2GameObject.interact(20785))
                                    waitForAnimationStop();
                                return;
                            } else if (Rs2Inventory.isFull() && !Rs2Inventory.hasItem(995))
                                Rs2Player.eatAt(100);
                        } else if (level == 2) {
                            if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(2021, 5218, 0)) > 10) {
                                Rs2Walker.walkTo(new WorldPoint(2021, 5218, 0), 1);
                                waitForAnimationStop();
                                return;
                            } else if (Rs2Inventory.ItemQuantity(995) != 5000 && Rs2GameObject.interact(19000) ) {
                                waitForAnimationStop();
                                return;
                            } else if (Rs2Inventory.ItemQuantity(995) == 5000){ // && Rs2Player.getWorldLocation().distanceTo(new WorldPoint(1904, 5218, 0)) < 2) {
                                if (Rs2GameObject.interact(19004))
                                    waitForAnimationStop();
                                return;
                            }
                        } else if (level == 3) {
                            if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(2145, 5282, 0)) > 10) {
                                Rs2Walker.walkTo(new WorldPoint(2145, 5282, 0), 1);
                                waitForAnimationStop();
                                return;
                            } else if (Rs2Inventory.ItemQuantity(995) != 10000 && Rs2GameObject.interact(23709)) {
                                waitForAnimationStop();
                                return;
                            } else if (Rs2Inventory.ItemQuantity(995) == 10000 && Rs2Player.getWorldLocation().distanceTo(new WorldPoint(1902, 5222, 0)) > 2) {
                                if (Rs2GameObject.interact(23706))
                                    waitForAnimationStop();
                                return;
                            }
                        } else if (level == 4) {
                            if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(2343, 5214, 0)) > 10) {
                                Rs2Player.toggleRunEnergy(true);
                                Rs2Walker.walkTo(new WorldPoint(2343, 5214, 0), 1);
                                waitForAnimationStop();
                                return;
                            } else if (!Rs2Inventory.hasItem("boot") && !Rs2Widget.hasWidget("Please wait")&& !Rs2Dialogue.hasContinue() && Rs2GameObject.interact(23731)) {
                                if (Rs2Inventory.isFull())
                                    Rs2Player.eatAt(100);
                                waitForAnimationStop();
                                return;
                            } else if (Rs2Inventory.hasItem("boot") ) {
                                Microbot.getPluginManager().setPluginValue("shortestpath", "useTeleportationPortals", true);
                                Rs2Tab.switchToMagicTab();
                                sleepUntil(()->Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC);
                                sleep(280,500);
                                Rs2Widget.clickWidget("Lumbridge Home");
                                Rs2Player.waitForAnimation(2000);
                                shutdown();
                            }
                        }
                    }
                }
                else{
                    if (!Rs2Inventory.isEmpty() && !depositeEverythingBeforeStart && Rs2Player.getWorldLocation().distanceTo(getfishPoint) > 15 && !Rs2Player.isAnimating()){
                        List<String> itemNames = Rs2Inventory.items().stream()
                                .map(Rs2Item::getName)
                                .collect(Collectors.toList());
                        Rs2Bank.walkToBank();
                        waitForAnimationStop();
                        Rs2Bank.openBank();
                        sleepUntil(() -> Rs2Bank.isOpen(), 5000);
                        Rs2Bank.depositAll();
                        //Rs2Bank.bankItemsAndWalkBackToOriginalPosition(itemNames,getfishPoint);
                        if (Rs2Inventory.isEmpty())
                            depositeEverythingBeforeStart = true;
                    } else if (!depositeEverythingBeforeStart && Rs2Player.getWorldLocation().distanceTo(getfishPoint) < 15) {
                        depositeEverythingBeforeStart = true;
                    }
                    else if (!Rs2Inventory.isFull()){
                        if (Rs2Player.getWorldLocation().distanceTo(getfishPoint) < 8){
                            if (Rs2GroundItem.exists(333,10)){
                                Rs2GroundItem.loot(333);
                                waitForAnimationStop();
                            } else if (Rs2GroundItem.exists(329,10)) {
                                Rs2GroundItem.loot(329);
                                waitForAnimationStop();
                            }
                        }
                        else{
                            Rs2Walker.walkTo(getfishPoint);
                            waitForAnimationStop();
                        }
                    } else if (Rs2Inventory.isFull() || (Rs2Inventory.size() > 20 && System.currentTimeMillis() - startTime > 720000)){
                        runInisiated = true;
                    }
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        rasMasterScriptScript.stopPlugin("sos");
        super.shutdown();
    }

    void enterHole(){
        WorldPoint holeLocation = new WorldPoint(3082,3422,0);
        Rs2Walker.walkTo(holeLocation,2);
        Rs2Player.waitForAnimation(1000);
        Rs2GameObject.interact("Entrance","Climb-down");
        Rs2Player.waitForAnimation(1000);


    }

    boolean solveansers() {
        if (Rs2Widget.hasWidget("Click here to continue")) {
            sleep(50, 150);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleep(50);
            sleepUntil(()->!Rs2Widget.hasWidget("Please"),500);
            return true;
        } else if (Rs2Widget.getWidget(219, 1) != null && Rs2Widget.findWidget("[") != null) {
            // Widget[] choices = Rs2Widget.getWidget(219,1).getDynamicChildren();
            sleep(50, 150);
            System.out.println(Rs2Widget.findWidget("[").getText());
            Rs2Widget.clickWidget(Rs2Widget.findWidget("[").getText());
            return true;
        } else if (Rs2Widget.hasWidget("Warning")) {
            sleep(50, 150);
            Rs2Widget.clickWidget(Rs2Widget.findWidget("Yes").getText());
            return true;
        } else if (Rs2Widget.hasWidget("Report")) {
            sleep(50, 150);
            Rs2Widget.clickWidget(Rs2Widget.findWidget("Report").getText());
            return true;
        } else if (Rs2Widget.hasWidget("Choose")) {
            //sleep(150, 15000);
            int actionss = random(14,17);
            Rs2Widget.clickWidget(270,actionss);
            return true;
        }
        else{
            for (String ans : ansewrs){
                if (clciksolveansers(ans))
                    return true;
            }
        }
    return false;
    }
    boolean clciksolveansers(String ans){
        if (Rs2Widget.hasWidget(ans)) {
            sleep(50, 150);
            Rs2Widget.clickWidget(Rs2Widget.findWidget(ans).getText());
            return true;
        }
        return false;
    }
    public void waitForAnimationStop() {
        long lastAnimationStopTime = System.currentTimeMillis();
        while (true) {
            Rs2Player.eatAt(80);
            sleep(10);
            if (Rs2Player.isAnimating()) {
                lastAnimationStopTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - lastAnimationStopTime >= 2000) {
                break;
            }
            if (Rs2Widget.hasWidget("Select an option"))
                break;
        }
    }
    public void changeWorld(){
        int world = getWorldWithMostPlayers(Rs2Player.isMember());
        int cworld = Microbot.getClient().getWorld();
        while (world != cworld) {
            boolean isHopped = Microbot.hopToWorld(world);
            if (!isHopped) return;
            isHopped = Microbot.hopToWorld(world);
            if (!isHopped) return;
            boolean result = sleepUntil(() -> Rs2Widget.findWidget("Switch World") != null);
            if (result) {
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                sleepUntil(() -> Microbot.getClient().getGameState() == GameState.HOPPING);
                sleepUntil(() -> Microbot.getClient().getGameState() == GameState.LOGGED_IN);
            }
            cworld = Microbot.getClient().getWorld();
        }
    }

    public static int getWorldWithMostPlayers(boolean isMembers) {
        WorldResult worldResult = Microbot.getWorldService().getWorlds();

        if (worldResult != null) {
            List<World> worlds = worldResult.getWorlds();

            List<World> filteredWorlds = worlds
                    .stream()
                    .filter(x ->
                            (!x.getTypes().contains(WorldType.PVP) &&
                                    !x.getTypes().contains(WorldType.HIGH_RISK) &&
                                    !x.getTypes().contains(WorldType.BOUNTY) &&
                                    !x.getTypes().contains(WorldType.SKILL_TOTAL) &&
                                    !x.getTypes().contains(WorldType.LAST_MAN_STANDING) &&
                                    !x.getTypes().contains(WorldType.QUEST_SPEEDRUNNING) &&
                                    !x.getTypes().contains(WorldType.BETA_WORLD) &&
                                    !x.getTypes().contains(WorldType.DEADMAN) &&
                                    !x.getTypes().contains(WorldType.PVP_ARENA) &&
                                    !x.getTypes().contains(WorldType.TOURNAMENT) &&
                                    !x.getTypes().contains(WorldType.FRESH_START_WORLD)) &&
                                    x.getPlayers() < 999 &&
                                    x.getPlayers() >= 0)
                    .collect(Collectors.toList());

            if (!isMembers) {
                filteredWorlds = filteredWorlds
                        .stream()
                        .filter(x -> !x.getTypes().contains(WorldType.MEMBERS))
                        .collect(Collectors.toList());
            } else {
                filteredWorlds = filteredWorlds
                        .stream()
                        .filter(x -> x.getTypes().contains(WorldType.MEMBERS))
                        .collect(Collectors.toList());
            }

            World world = filteredWorlds
                    .stream()
                    .max(Comparator.comparingInt(World::getPlayers))
                    .orElse(null);

            if (world != null) {
                return world.getId();
            }
        }

        return isMembers ? 360 : 383;
    }
}
