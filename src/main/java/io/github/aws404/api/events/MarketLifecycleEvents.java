package io.github.aws404.api.events;

import io.github.aws404.market.MarketInstance;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class MarketLifecycleEvents {
    public static final Event<MarketEvent> MARKET_STARTED = EventFactory.createArrayBacked(MarketEvent.class, (callbacks) -> (market) -> {
        for (MarketEvent callback : callbacks) {
            callback.onMarketEvent(market);
        }
    });

    public static final Event<MarketEvent> MARKET_STOPPED = EventFactory.createArrayBacked(MarketEvent.class, (callbacks) -> (market) -> {
        for (MarketEvent callback : callbacks) {
            callback.onMarketEvent(market);
        }
    });

    public static final Event<MarketEvent> MARKET_TICK = EventFactory.createArrayBacked(MarketEvent.class, (callbacks) -> (market) -> {
        for (MarketEvent callback : callbacks) {
            callback.onMarketEvent(market);
        }
    });

    @FunctionalInterface
    public interface MarketEvent {
        void onMarketEvent(MarketInstance instance);
    }
}
