package net.runelite.client.plugins.microbot.testing;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.grandexchange.GrandExchangePlugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;


@PluginDescriptor(
        name = PluginDescriptor.Default + "testing sc",
        description = "Microbot example plugin",
        tags = {"example", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class testingPlugin extends Plugin {
    private int[] previousQuantitiesSold = new int[GrandExchangePlugin.GE_SLOTS]; // Store previous quantities for each slot
    @Inject
    private testingConfig config;
    @Provides
    testingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(testingConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private testingOverlay testingOverlay;

    @Inject
    testingScript testingScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(testingOverlay);
        }
        testingScript.run(config);
    }

    protected void shutDown() {
        testingScript.shutdown();
        overlayManager.remove(testingOverlay);
    }
    int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));

        if (ticks > 0) {
            ticks--;
        } else {
            ticks = 10;
        }

    }
    @Subscribe
    public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged offerEvent) {
        final int slot = offerEvent.getSlot();
        final GrandExchangeOffer offer = offerEvent.getOffer();

        if (offer.getState() == GrandExchangeOfferState.EMPTY && Microbot.getClient().getGameState() != GameState.LOGGED_IN)
        {
            // Trades are cleared by the client during LOGIN_SCREEN/HOPPING/LOGGING_IN, ignore those so we don't
            // clear the offer config.
            return;
        }
        System.out.println(String.format("GE offer updated: state: %s, slot: %d, item: %d, qty: %d",
                offer.getState(), slot, offer.getItemId(), offer.getQuantitySold()));
    }

}
