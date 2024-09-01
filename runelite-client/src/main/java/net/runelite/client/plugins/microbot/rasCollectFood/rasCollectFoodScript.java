package net.runelite.client.plugins.microbot.rasCollectFood;

import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.geHandler.geHandlerScript;
import net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;


public class rasCollectFoodScript extends Script {
    public static double version = 1.0;
    private WorldPoint lumsalmon = new WorldPoint(1,1,0);
    private WorldPoint edgeville = new WorldPoint(3105,3432,0);
    private WorldArea cabage = new WorldArea(3057,3290,5,5,0);
    private WorldPoint cookedmeat = new WorldPoint(1,1,0);
    private Set<String> itemNames = new HashSet<>();
    //long stopTimer = random(1800000,2760000) + System.currentTimeMillis();

    public boolean run(rasCollectFoodConfig config) {
        long stopTimer = random(1800000,2760000) + System.currentTimeMillis();
        Set<String> itemsToPickSet = new HashSet<>(Arrays.asList(config.itemsToPick().toLowerCase().split(",")));



        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                rasMasterScriptScript.autoShutdown("collect food");
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();
                hopworld();

                if (Rs2Inventory.isFull()){
                    rasMasterScriptScript.bankAllAndGet(0,"ras");
                }
                if (Rs2Player.getWorldLocation().distanceTo(edgeville) > 15) {
                    Rs2Walker.walkTo(edgeville,6);
                }
                if (stopTimer < System.currentTimeMillis()) {
                    if (config.sellthem())
                        geHandlerScript.goSell(false, 5, new int[]{0},itemNames.toArray(new String[0]) );
                    shutdown();
                }
                if (Rs2Player.getWorldLocation().distanceTo(edgeville) < 15 && !Rs2Inventory.isFull() ){
                    RS2Item[] items = Rs2GroundItem.getAll(15);
                    if( items != null){
                        for (RS2Item item : items){
                            if (itemsToPickSet.stream().anyMatch(name-> item.getItem().getName().toLowerCase().contains(name)) && !Rs2Inventory.isFull()) {
                                    Rs2GroundItem.interact(item);
                                    sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                                    if(Rs2Inventory.ItemQuantity(item.getItem().getId()) > 0)
                                        itemNames.add(item.getItem().getName());
                                    break;
                                }
                        }
                    }
                }



                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                //System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    private void hopworld() {
        int world = Microbot.getClient().getWorld();
        if (world != 301 && world != 308) {
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
    }

    @Override
    public void shutdown() {
        String pluginName = "collect food";
        rasMasterScriptScript masterControl = new rasMasterScriptScript();
        masterControl.stopPlugin(pluginName);
        do{sleep(2000);}
        while (masterControl.isPlugEnabled(pluginName));
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
