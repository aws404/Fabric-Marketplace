package io.github.aws404.api.events;

import io.github.aws404.market.orders.MarketListing;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class MarketListingEvents {
    public static final Event<ListEvent> LIST = EventFactory.createArrayBacked(ListEvent.class, (callbacks) -> (market) -> {
        for (ListEvent callback : callbacks) {
            callback.onMarketEvent(market);
        }
    });

    public static final Event<ListEvent> CANCEL = EventFactory.createArrayBacked(ListEvent.class, (callbacks) -> (market) -> {
        for (ListEvent callback : callbacks) {
            callback.onMarketEvent(market);
        }
    });

    public static final Event<ClaimEvent> CLAIM = EventFactory.createArrayBacked(ClaimEvent.class, (callbacks) -> (listing, slot) -> {
        for (ClaimEvent callback : callbacks) {
            callback.onClaim(listing, slot);
        }
    });

    @FunctionalInterface
    public interface ListEvent {
        void onMarketEvent(MarketListing instance);
    }

    @FunctionalInterface
    public interface ClaimEvent {
        void onClaim(MarketListing instance, int slot);
    }
}
