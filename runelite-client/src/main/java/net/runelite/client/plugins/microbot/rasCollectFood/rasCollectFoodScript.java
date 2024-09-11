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
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.*;
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
    public  static long stopTimer =1;
    private int range = 2 ;
    //long stopTimer = random(1800000,2760000) + System.currentTimeMillis();

    public boolean run(rasCollectFoodConfig config) {
        Set<String> itemsToPickSet = new HashSet<>(Arrays.asList(config.itemsToPick().toLowerCase().split(",")));
        List<Integer> amounts = new ArrayList<>();
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (stopTimer == 1)
                    stopTimer = rasMasterScriptScript.autoStopTimer();
                rasMasterScriptScript.autoShutdown("collect food");
                hopworld();
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                if (Rs2Inventory.isFull()){
                    rasMasterScriptScript.bankAllAndGet(0,"ras");
                }
                if (Rs2Player.getWorldLocation().distanceTo(edgeville) > 15) {
                    Rs2Walker.walkTo(edgeville,6);
                }
                if (stopTimer < System.currentTimeMillis()) {
                    if (config.sellthem()) {
                        while (amounts.size() < itemNames.size()) {
                            amounts.add(0); // Fill with 0s to match the size
                        }
                        geHandlerScript.goSell(false, 5, amounts.stream().mapToInt(i -> i).toArray(), itemNames.toArray(new String[0]));
                    }
                    shutdown();
                }
                if (Rs2Player.getWorldLocation().distanceTo(edgeville) < 15 && !Rs2Inventory.isFull() ){
                    RS2Item[] items = Rs2GroundItem.getAll(range);
                    if( items != null && items.length > 0){
                        if ( Rs2Player.getWorldLocation().distanceTo(items[0].getTile().getWorldLocation()) > 3) {
                            Rs2Walker.walkFastCanvas(items[0].getTile().getWorldLocation());
                            sleepUntilTrue(() -> Rs2Player.getWorldLocation().distanceTo(items[0].getTile().getWorldLocation()) < 2, 100, 3000);
                        }
                        for (RS2Item item : items){
                            if (itemsToPickSet.stream().anyMatch(name-> item.getItem().getName().toLowerCase().contains(name)) && !Rs2Inventory.isFull()) {
                                try {
                                    if (Rs2GroundItem.interact(item)) {
                                        if (range > 2)
                                            range--;
                                        sleepUntilTrue(Rs2Inventory::waitForInventoryChanges, 100, 5000);
                                    }
                                }
                                catch (Exception e){
                                    System.out.println("item gone");
                                }
                                    if(Rs2Inventory.ItemQuantity(item.getItem().getId()) > 0) {
                                        itemNames.add(item.getItem().getName());
                                    }
                                }
                        }
                    }
                    else if (range < 8)
                        range++;
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

    @Override
    public void shutdown() {
        stopTimer = 1;
        String pluginName = "collect food";
        //rasMasterScriptScript masterControl = new rasMasterScriptScript();
        rasMasterScriptScript.stopPlugin(pluginName);
        do{sleep(2000);}
        while (rasMasterScriptScript.isPlugEnabled(pluginName));
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
