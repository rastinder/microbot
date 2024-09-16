package net.runelite.client.plugins.microbot.testing;

import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.grandexchange.GrandExchangePlugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.rasMasterScript.rasMasterScriptScript;

import java.util.concurrent.TimeUnit;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.api.events.GrandExchangeOfferChanged;

import javax.inject.Inject;

import static net.runelite.client.plugins.grandexchange.GrandExchangePlugin.GE_SLOTS;


public class testingScript extends Script {
    public static long stopTimer = 1;
    @Inject
    private  ConfigManager configManager;
    @Inject
    private  ItemManager itemManager;
    public static int boughtQuantity = 0;



    public boolean run(testingConfig config) {
        Microbot.enableAutoRunOn = false;
        rasMasterScriptScript ras = new rasMasterScriptScript();

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (rasMasterScriptScript.autoShutdown("testing sc"))
                    return;
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (stopTimer == 1)
                    stopTimer = rasMasterScriptScript.autoStopTimer();
                long startTime = System.currentTimeMillis();
                try {
                    //System.out.println( configManager.getRSProfileConfiguration("geoffer", Integer.toString(0)));
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
                try {
                    //System.out.println( configManager.getRSProfileConfiguration("geoffer", Integer.toString(1)));
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }


                //CODE HERE

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                //System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        stopTimer = 1;
        //eventBus.unregister(this);
        rasMasterScriptScript.stopPlugin("testing sc");
        super.shutdown();
    }
}
