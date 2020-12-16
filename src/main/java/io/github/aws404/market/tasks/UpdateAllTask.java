package io.github.aws404.market.tasks;

import io.github.aws404.InitFabricMarketplace;
import io.github.aws404.market.MarketInstance;
import io.github.aws404.market.orders.MarketListing;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class UpdateAllTask extends MarketTask {

    public UpdateAllTask() {
        super(instance -> {
            instance.profiler.push("updateAllTask");
            LocalDateTime now = LocalDateTime.now();

            ArrayList<MarketListing> orders = instance.getBuyOrders(MarketListing::isAvailable);

            for (MarketListing order : orders) {
                LocalDateTime expire = LocalDateTime.ofEpochSecond(order.timestamp.getTime(), order.timestamp.getNanos(), InitFabricMarketplace.ZONE_OFFSET).plus(Duration.ofMinutes(InitFabricMarketplace.CONFIG.orderExpireTime));
                Duration remaining = Duration.ofSeconds(now.until(expire, ChronoUnit.SECONDS));
                if (remaining.getSeconds() < 0) {
                    MarketInstance.LOGGER.debug("The order " + order.id + " has expired!");
                    order.cancel();
                    continue;
                }

                for (MarketListing buyOrder : orders) {
                    if (order != buyOrder && buyOrder.getIdentifier().equals(order.getIdentifier()) && buyOrder.isAvailable() && order.isAvailable() && !buyOrder.getSeller().equals(order.getSeller())) {
                        if (buyOrder.getInput().canBuy(order.getOutput())) {
                            buyOrder.connectOrders(instance, order);
                        } else if (order.getInput().canBuy(buyOrder.getOutput())) {
                            order.connectOrders(instance, buyOrder);
                        }
                    }
                }
            }
            instance.profiler.pop();
        });
    }
}
