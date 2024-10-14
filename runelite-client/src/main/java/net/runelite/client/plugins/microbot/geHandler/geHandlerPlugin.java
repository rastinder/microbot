package net.runelite.client.plugins.microbot.geHandler;
import java.util.logging.Logger;

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
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.client.plugins.microbot.geHandler.geHandlerScript.boughtQuantity;

@PluginDescriptor(
        name = PluginDescriptor.Default + "geHandler",
        description = "Microbot example plugin",
        tags = {"example", "microbot"},
        enabledByDefault = false
)
public class geHandlerPlugin extends Plugin {
    private static final Logger logger = Logger.getLogger(geHandlerPlugin.class.getName());

    @Inject
    private geHandlerConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private geHandlerOverlay geHandlerOverlay;

    @Inject
    private geHandlerScript geHandlerScript;

    @Provides
    geHandlerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(geHandlerConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
        //if (overlayManager != null) {
        //    overlayManager.add(geHandlerOverlay);
       // }
        geHandlerScript.run(config);
    }

    protected void shutDown() {
        geHandlerScript.shutdown();
        overlayManager.remove(geHandlerOverlay);
    }

    int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick) {
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
            return;
        }
        System.out.println(String.format("GE offer updated: state: %s, slot: %d, item: %d, qty: %d",
                offer.getState(), slot, offer.getItemId(), offer.getQuantitySold()));
        if (offer.getState().toString().contains("BOUGHT") ||offer.getState().toString().contains("BUYING")){
            boughtQuantity = (int) offer.getQuantitySold();
        }

    }
}