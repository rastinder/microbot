package net.runelite.client.plugins.microbot.geHandler;

import net.runelite.api.GrandExchangeOfferState;
import net.runelite.client.plugins.grandexchange.GrandExchangePlugin;
import net.runelite.client.plugins.grandexchange.SavedOffer;

import javax.inject.Inject;
import java.time.Instant;
import java.time.Duration;

public class getItemInformation {
    @Inject
    private GrandExchangePlugin grandExchangePlugin;

    public getItemInformation(GrandExchangePlugin grandExchangePlugin) {
        this.grandExchangePlugin = grandExchangePlugin;
    }

    public  void main1(){
        int itemid = 1953;
        //System.out.println(getBoughtQuantity(itemid));
        System.out.println(getResetTimer(itemid));
        //System.out.println(getBoughtQuantity(itemid));
    }

    public  int getBoughtQuantity(int itemId) {
        for (int i = 0; i < GrandExchangePlugin.GE_SLOTS; i++) {
            SavedOffer offer = grandExchangePlugin.getOffer(i);
            if (offer != null && offer.getItemId() == itemId &&
                    (offer.getState() == GrandExchangeOfferState.BOUGHT || offer.getState() == GrandExchangeOfferState.BUYING)) {
                return offer.getQuantitySold();
            }
        }
        return 0; // Item not found or not bought in any slot
    }

    public  Duration getResetTimer(int itemId) {
        Instant resetTime = grandExchangePlugin.getLimitResetTime(itemId);
        if (resetTime == null) {
            return null; // No reset timer set for this item
        }
        return Duration.between(Instant.now(), resetTime);
    }
}