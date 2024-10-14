package net.runelite.client.plugins.microbot.rasCombatTrain;

import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.player.Rs2Player.playerDetectionTimes;


public class rasCombatTrainScript extends Script {
    public static double version = 1.0;
    long awayFromCombatTime = 0;
    long awayFromCombatTimeAnd = 1;
    long foodEatingCooldown = 0;
    long lastPrintTime = 0;
    int healthThreshold = new Random().nextInt(10) + 6;
    int location = 1;
    public static long stopTimer = 1;
    WorldPoint soswholend = new WorldPoint(2, 3, 0);
    WorldPoint soslvl1start = new WorldPoint(2, 3, 0);
    WorldPoint soslvl2start = new WorldPoint(2, 3, 0);
    WorldPoint soslvl1end = new WorldPoint(2, 3, 0);
    WorldPoint soslvl2CrawlerLocation1 = new WorldPoint(2, 3, 0);
    WorldPoint soslvl2CrawlerLocation2 = new WorldPoint(2, 3, 0);
    WorldArea soslvl2CrawlerLocation3 = new WorldArea(2038, 5186, 6, 7, 0);
    WorldPoint rope = new WorldPoint(2041, 5208, 0);


    public boolean run(rasCombatTrainConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                rasMasterScriptScript.autoShutdown("ras CombatTrain");
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (stopTimer == 1) stopTimer = rasMasterScriptScript.autoStopTimer();
                rasMasterScriptScript.hopworld();
                if (Rs2Player.isInteracting()) {
                    awayFromCombatTime = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - lastPrintTime >= 1000) {
                    System.out.println("Not in combat for " + (System.currentTimeMillis() - awayFromCombatTime) / 1000 + " seconds");
                    lastPrintTime = System.currentTimeMillis();
                }
                if (Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS) < healthThreshold) {
                    eattillhealthfull();
                    healthThreshold = new Random().nextInt(10) + 6;
                    foodEatingCooldown = System.currentTimeMillis();
                }
                long health = Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS);
                if (Rs2Inventory.getInventoryFood().size() <= 1 && Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS) < 20) {
                    getfood();
                    awayFromCombatTime = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() - awayFromCombatTime > 20000) { // away from combat
                    boolean skip = false;
                    if (System.currentTimeMillis() - awayFromCombatTime < 3600000) {
                        long sleepDuration = new Random().nextInt(116000) + 210;
                        long sleepStartTime = System.currentTimeMillis();
                        System.out.println("sleep for  " + (sleepDuration / 1000 + " seconds"));
                        while (System.currentTimeMillis() - sleepStartTime < sleepDuration) {
                            if (Rs2Player.isInteracting()) {
                                skip = true;
                                break; // Exit the sleep loop if the player starts interacting
                            }
                            sleep(100); // Check interaction status every 100 ms
                        }
                    }
                    if (!skip) {
                        if (Rs2Inventory.getInventoryFood().size() < 10)
                            getfood();
                        else
                            walkLittlebit();
                        awayFromCombatTime = System.currentTimeMillis();
                    }
                }
                if (System.currentTimeMillis() > stopTimer)shutdown();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        stopTimer = 1;
        rasMasterScriptScript.stopPlugin("ras CombatTrain");
        super.shutdown();
    }

    public void walkLittlebit() {
        eattillhealthfull();
        if (location == 0) {  //  go to 3nd crawler
            WorldPoint randomsoslvl2CrawlerLocation3 = soslvl2CrawlerLocation3.toWorldPointList().get(net.runelite.client.plugins.microbot.util.math.Random.random(0, soslvl2CrawlerLocation3.toWorldPointList().size() - 1));
            while (randomsoslvl2CrawlerLocation3.distanceTo(Rs2Player.getWorldLocation()) >3) {
                Rs2Walker.walkTo(randomsoslvl2CrawlerLocation3,1);
                waitForAnimationStop();
                try {
                    Rs2Walker.walkFastCanvas(randomsoslvl2CrawlerLocation3);
                }
                catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
            awayFromCombatTimeAnd = System.currentTimeMillis();
            location = 2;

        } else if (location == 1) { // go to fisrt crawler
            soslvl2CrawlerLocation1 = Math.random() < 0.5 ? new WorldPoint(2044, 5233, 0) : new WorldPoint(2044, 5234, 0);
            while (soslvl2CrawlerLocation1.distanceTo(Rs2Player.getWorldLocation()) >1) {
                Rs2Walker.walkTo(soslvl2CrawlerLocation1, 0);
                waitForAnimationStop();
                try {
                    Rs2Walker.walkFastCanvas(soslvl2CrawlerLocation1);
                    waitForAnimationStop();
                }
                catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
            location = 0;
            return;

        } else if (location == 2) {
            if (System.currentTimeMillis() - awayFromCombatTimeAnd >= 600000) {
                Rs2Walker.walkTo(rope, 1);
                sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(rope) < 3, 20000);
                Rs2GameObject.interact("Rope", "Climb-up");
                sleepUntil(() -> new WorldPoint(2042, 5245, 0).distanceTo(Rs2Player.getWorldLocation()) < 2, 10000);

                soslvl2CrawlerLocation1 = Math.random() < 0.5 ? new WorldPoint(2044, 5233, 0) : new WorldPoint(2044, 5234, 0);
                while (soslvl2CrawlerLocation1.distanceTo(Rs2Player.getWorldLocation()) >1) {
                    Rs2Walker.walkTo(soslvl2CrawlerLocation1, 0);
                    waitForAnimationStop();
                    try {
                        Rs2Walker.walkFastCanvas(soslvl2CrawlerLocation1);
                        waitForAnimationStop();
                    }
                    catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                }
                if (detectplayer()) {
                    if (Math.random() < 0.5) Rs2Walker.walkCanvas(new WorldPoint(2040, 5232, 0));
                    else Rs2Walker.walkCanvas(new WorldPoint(2040, 5231, 0));
                    //location = 0;
                } else {
                    return;
                }
            }
            else {
                WorldPoint randomsoslvl2CrawlerLocation3 = soslvl2CrawlerLocation3.toWorldPointList().get(net.runelite.client.plugins.microbot.util.math.Random.random(0, soslvl2CrawlerLocation3.toWorldPointList().size() - 1));
                Rs2Walker.walkCanvas(randomsoslvl2CrawlerLocation3);
            }
        }
    }

    public void getfood() {
        eattillhealthfull(99);
        if (location == 2) {
            Rs2Walker.walkTo(rope, 1);
            sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(rope) < 6, 25000);
            Rs2GameObject.interact("Rope", "Climb-up");
            sleepUntil(() -> new WorldPoint(2042, 5245, 0).distanceTo(Rs2Player.getWorldLocation()) < 2, 8000);
        } else {
            Rs2Walker.walkTo(new WorldPoint(2042, 5245, 0), 1);
            sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(new WorldPoint(2042, 5245, 0)) < 3, 15000);
            sleep(1000);
        }
        Rs2GameObject.interact("Ladder", "Climb-up");
        sleep(1000);
        Rs2GameObject.interact("Ladder", "Climb-up");
        sleep(2000);

        Rs2Walker.walkTo(new WorldPoint(3105, 3432, 0), 6);
        //List<Integer> itemList = Arrays.asList(329, 331, 333, 335);
        List<Integer>itemList = Arrays.asList(329 ,333);
        if (Rs2Player.getRealSkillLevel(Skill.COOKING) >= 15)
            itemList.add(335);
        if (Rs2Player.getRealSkillLevel(Skill.COOKING) >= 25)
            itemList.add(331);
        sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(new WorldPoint(3105, 3432, 0)) < 8, 15000);
        for (int k = 0; k < 3; k++) {
            while (!Rs2Inventory.isFull()) {
                outerLoop:
                for (int item : itemList) {
                    if (Rs2GroundItem.take(item)) {
                        sleep(50, 80);
                        break outerLoop;
                    }
                }
            }
            while (Rs2Inventory.hasItem("Raw")) {
                Rs2Inventory.use("raw");
                sleepUntil(() -> Rs2GameObject.interact("Fire", "use"));
                waitForAnimationStop();
                sleepUntil(() -> Rs2Widget.hasWidget("Choose"), 1000);
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                waitForAnimationStop();
            }
            eattillhealthfull(99);
            sleep(350);
            while (Rs2Inventory.hasItem(Rs2Inventory.get("Burnt").getId())) { // check for crash
                Rs2Inventory.dropAll(Rs2Inventory.get("Burnt").getId());
                sleepUntil(() -> !Rs2Inventory.hasItem(Rs2Inventory.get("Burnt").getId()), 10000);
            }
        }
        Rs2Walker.walkTo(new WorldPoint(3080, 3421, 0), 2);
        sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(new WorldPoint(2042, 5245, 0)) < 8, 15000);
        Rs2GameObject.interact("Entrance", "Climb-down");
        Rs2Player.waitForWalking();
        sleep(800);
        Rs2GameObject.interact("Portal", "Use");
        Rs2Player.waitForWalking();
        Rs2Walker.walkFastCanvas(new WorldPoint(1903, 5222, 0));
        Rs2Player.waitForWalking();
        Rs2GroundItem.interact("Ladder", "Climb-down", 2);
        sleep(1800);
        // handel msg
        location = new Random().nextInt(3);
        walkLittlebit();
    }

    public boolean detectplayer() {
        return IfPlayerDetected(1, 1000, 2);
    }

    public static boolean IfPlayerDetected(int amountOfPlayers, int time, int distance) {
        List<Player> players = Microbot.getClient().getPlayers();
        long currentTime = System.currentTimeMillis();

        if (distance > 0) {
            players = players.stream()
                    .filter(x -> x != null && x.getWorldLocation().distanceTo(Rs2Player.getWorldLocation()) <= distance)
                    .collect(Collectors.toList());
        }
        if (time > 0 && players.size() > amountOfPlayers) {
            // Update detection times for currently detected players
            for (Player player : players) {
                playerDetectionTimes.putIfAbsent(player, currentTime);
            }

            // Remove players who are no longer detected
            playerDetectionTimes.keySet().retainAll(players);

            // Check if any player has been detected for longer than the specified time
            for (Player player : players) {
                long detectionTime = playerDetectionTimes.getOrDefault(player, 0L);
                if (currentTime - detectionTime >= time) { // convert time to milliseconds
                    return true;
                }
            }
        } else if (players.size() >= amountOfPlayers) {
            return true;
        }
        return false;
    }
    public void waitForAnimationStop() {
        long lastAnimationStopTime = System.currentTimeMillis();
        while (true) {
            sleep(100);
            if (Rs2Player.isAnimating()) {
                lastAnimationStopTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - lastAnimationStopTime >= 3000) {
                break;
            }
        }
    }
    public void eattillhealthfull(){
        eattillhealthfull(76);
    }
    public void eattillhealthfull(int hit){
        while (Rs2Player.eatAt(hit))
            sleep(1100, 1500);
    }
    private void hopworld() {
        int world = Microbot.getClient().getWorld();
        while (world != 301 && world != 308) {
            Microbot.hopToWorld(301);
            boolean result = sleepUntil(() -> Rs2Widget.findWidget("Switch World") != null);
            if (result) {
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                sleepUntil(() -> Microbot.getClient().getGameState() == GameState.HOPPING);
                sleepUntil(() -> Microbot.getClient().getGameState() == GameState.LOGGED_IN);
            }
            if (Microbot.getClient().getGameState() == GameState.LOGIN_SCREEN){
                boolean isHopped = Microbot.hopToWorld(308);
                if (!isHopped) return;
                result = sleepUntil(() -> Rs2Widget.findWidget("Switch World") != null);
                if (result) {
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleepUntil(() -> Microbot.getClient().getGameState() == GameState.HOPPING);
                    sleepUntil(() -> Microbot.getClient().getGameState() == GameState.LOGGED_IN);
                }

            }
            world = Microbot.getClient().getWorld();
        }
        Rs2Tab.switchToInventoryTab();
    }

}
