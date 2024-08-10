package net.runelite.client.plugins.microbot.raschoclatebar;

import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grandexchange.Rs2GrandExchange;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.concurrent.TimeUnit;


public class raschoclatebarScript extends Script {
    public static double version = 1.0;
    private static String status = "firstrun";
    private static String item1 = "firstrun";
    private static String item2 = "firstrun";
    private static String item12 = "firstrun";
    private static String item3 = "firstrun";
    private static String item4 = "firstrun";
    private static String item34 = "firstrun";
    private static String item1234 = "firstrun";
    private static boolean space = true;
    private static int startbuy = 0; // 0 false, 1 partial, 2 true;
    private static final WorldArea grandexchange = new WorldArea(3167, 3486, 5, 5, 0);
    int ratio1 = 1; // item1 ratio
    int ratio2 = 2; // item2 ratio




    public boolean run(raschoclatebarConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                switch (status){
                    case "firstrun": firstun();break;
                    case "buy": buy();break;
                    case "combineHandle": combine();break;
                    case "sell": sell();break;
                }

            } catch (Exception ex) {
                System.out.println("crash " + ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
    private  static void firstun(){
        if(Rs2Inventory.hasItem(item1) && Rs2Inventory.hasItem(item2))
            return;
        Rs2Bank.walkToBankAndUseBank();
        if(Rs2Bank.hasBankItem(item1)){
            if(Rs2Bank.hasBankItem(item12)){
                status = "sell";
                return;
            }
            if(Rs2Bank.hasBankItem(item2)){
                status = "combineHandle";
                return;
            }
        }
        if(!Rs2Bank.hasBankItem(item2) || !Rs2Bank.hasBankItem(item1)){
            status = "buy";
            startbuy = 2;
            return;
        }
    }
    private void buy(){
        String buyfrom = "GE"; // GE/shop
        if (buyfrom.contains("GE") && startbuy == 2) {
            Rs2Walker.walkTo(grandexchange.toWorldPoint());
            waitForAnimationStop();

            Rs2Bank.depositAll();
            sleepUntil(Rs2Inventory::isEmpty, 5000);
            //Rs2Bank.setWithdrawAsNote();
            //Rs2Bank.withdrawAll(item1);
            int totalCoins = Rs2Bank.count("Coins");
            int totalItem1 = Rs2Bank.count(item1);
            int item1Cost = Rs2Bank.findBankItem(item1).getPrice();
            int totalItem2 = Rs2Bank.count(item2);
            int item2Cost = Rs2Bank.findBankItem(item2).getPrice();

            int userBuyLimit = 500; // Example user-defined buy limit

            int maxItem1 = Math.min((totalCoins / item1Cost), userBuyLimit - totalItem1);
            int maxItem2 = Math.min((totalCoins / item2Cost), userBuyLimit - totalItem2);

            int item1ToBuy = Math.min(maxItem1, totalItem1 + (totalItem2 / ratio2));
            int item2ToBuy = Math.min(maxItem2, (totalItem1 / ratio1) + totalItem2);

            item1ToBuy = Math.min(item1ToBuy, userBuyLimit - totalItem1);
            item2ToBuy = Math.min(item2ToBuy, userBuyLimit - totalItem2);

            if(item1ToBuy > 1)
             buyItem(item1, item1ToBuy, item1Cost);
            if(item2ToBuy > 1)
                buyItem(item2, item2ToBuy, item2Cost);
            startbuy = 1;
        }else
        if(buyfrom.contains("GE") && startbuy ==1 ) {
            if (Rs2GrandExchange.hasBoughtOffer()) {
                Rs2GrandExchange.collectToBank();
            }
        }
        if(buyfrom.contains("GE") && startbuy !=0 ) {
            if (Rs2GrandExchange.isAllSlotsEmpty())
                startbuy = 0;
        }
    }
    private  static void combine(){

    }
    private  static void sell(){}
    private void buyItem(String itemName, int buyLimit, int gePrice) {
        Rs2GrandExchange.buyItem(itemName,gePrice,buyLimit);
        //inactivityTimer.update();
    }
    public static void waitForAnimationStop() {
        long lastAnimationStopTime = System.currentTimeMillis();
        while (true) {
            if (Rs2Player.isAnimating()) {
                lastAnimationStopTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - lastAnimationStopTime >= 500) {
                break;
            }
        }
    }
}
