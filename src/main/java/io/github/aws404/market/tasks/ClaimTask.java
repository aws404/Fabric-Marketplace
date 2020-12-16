package io.github.aws404.market.tasks;

import io.github.aws404.api.events.MarketListingEvents;
import io.github.aws404.market.orders.MarketListing;
import net.minecraft.server.network.ServerPlayerEntity;

public class ClaimTask extends MarketTask {
    public ClaimTask(ServerPlayerEntity playerEntity, MarketListing order, int slot) {
        super(instance -> {
            instance.profiler.push("claimTask");
            checkPermissionToModify(playerEntity, order);

            if (slot >= order.getClaimable().size()) {
                return;
            }

            order.getClaimableInSlot(slot).give(playerEntity);
            MarketListingEvents.CLAIM.invoker().onClaim(order, slot);
            order.removeClaimable(slot);
            instance.writeUpdatesToOrder(order);
            instance.profiler.pop();
        });
    }
}
