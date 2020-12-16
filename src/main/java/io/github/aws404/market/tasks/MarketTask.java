package io.github.aws404.market.tasks;

import io.github.aws404.market.MarketInstance;
import io.github.aws404.market.orders.MarketListing;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class MarketTask implements Consumer<MarketInstance> {

    private final Consumer<MarketInstance> marketTask;

    public MarketTask(Consumer<MarketInstance> runnable) {
        this.marketTask = runnable;
    }

    public MarketTask(Runnable runnable) {
        this.marketTask = instance1 -> runnable.run();
    }

    @Override
    public void accept(MarketInstance instance) {
        marketTask.accept(instance);
    }

    protected static void checkPermissionToModify(ServerPlayerEntity playerEntity, MarketListing order) {
        order.refresh();
        if (playerEntity.getUuid() != order.getSeller().getId() && !playerEntity.hasPermissionLevel(2)) {
            playerEntity.sendMessage(new LiteralText("You do not have permission to modify that order!").formatted(Formatting.RED), false);
            throw new RuntimeException("Player tried to modify an order without permission");
        }
    }

}
