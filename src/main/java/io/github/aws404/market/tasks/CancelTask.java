package io.github.aws404.market.tasks;

import io.github.aws404.api.events.MarketListingEvents;
import io.github.aws404.market.orders.MarketListing;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class CancelTask extends MarketTask {
    public CancelTask(ServerPlayerEntity playerEntity, MarketListing order) {
        super(instance -> {
            instance.profiler.push("cancelTask");
            checkPermissionToModify(playerEntity, order);

            if (!order.isAvailable()) {
                playerEntity.sendMessage(new LiteralText("There was an error cancelling that item. It has already been purchased."), false);
                return;
            }

            order.cancel();
            MarketListingEvents.CANCEL.invoker().onMarketEvent(order);
            instance.writeUpdatesToOrder(order);

            playerEntity.sendMessage(new LiteralText("Your order has been cancelled."), false);
            instance.profiler.pop();
        });
    }
}
