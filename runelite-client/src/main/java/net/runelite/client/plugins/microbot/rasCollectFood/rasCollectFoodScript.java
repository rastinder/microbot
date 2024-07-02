package net.runelite.client.plugins.microbot.rasCollectFood;

import net.runelite.api.GameState;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;


public class rasCollectFoodScript extends Script {
    public static double version = 1.0;
    private WorldPoint lumsalmon = new WorldPoint(1,1,0);
    private WorldPoint edgeville = new WorldPoint(1,1,0);
    private WorldPoint cabage = new WorldPoint(1,1,0);
    private WorldPoint cookedmeat = new WorldPoint(1,1,0);


    public boolean run(rasCollectFoodConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                int world = Login.getRandomWorld(Rs2Player.isMember(), null);
                boolean isHopped = Microbot.hopToWorld(world);
                if (!isHopped) return;
                boolean result = sleepUntil(() -> Rs2Widget.findWidget("Switch World") != null);
                if (result) {
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleepUntil(() -> Microbot.getClient().getGameState() == GameState.HOPPING);
                    sleepUntil(() -> Microbot.getClient().getGameState() == GameState.LOGGED_IN);
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
}
