package net.runelite.client.plugins.microbot.rasCollectFood;

import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;


public class rasCollectFoodScript extends Script {
    public static double version = 1.0;
    private WorldPoint lumsalmon = new WorldPoint(1,1,0);
    private WorldPoint edgeville = new WorldPoint(3105,3432,0);
    private WorldArea cabage = new WorldArea(3057,3290,5,5,0);
    private WorldPoint cookedmeat = new WorldPoint(1,1,0);


    public boolean run(rasCollectFoodConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                int world = Microbot.getClient().getWorld();
                if (world != 301 || world != 308) {
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
                }
                int health = Microbot.getClient().getRealSkillLevel(Skill.HITPOINTS);
                if (health <= 15 ){
                    getcabage();
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
        super.shutdown();
    }
    void getcabage(){
        Rs2Walker.walkTo(cabage.toWorldPoint(),3);
        sleepUntil(()-> Rs2Player.getWorldLocation().distanceTo(cabage)< 10,50000);
        while(!Rs2Inventory.isFull()){
            Rs2GameObject.interact("Cabbage","pick");
            sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 3000);
        }

    }
    void troutedge(){}
}
