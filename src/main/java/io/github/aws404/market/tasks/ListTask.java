package io.github.aws404.market.tasks;

import io.github.aws404.api.events.MarketListingEvents;
import io.github.aws404.market.MarketInstance;
import io.github.aws404.market.orders.MarketListing;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ListTask extends MarketTask {
    public ListTask(ServerPlayerEntity playerEntity, MarketListing newOrder) {
        super(instance -> {
            instance.profiler.push("listTask");
            checkPermissionToModify(playerEntity, newOrder);

            if (!newOrder.hasTotalOutput(playerEntity)) {
                playerEntity.sendMessage(new LiteralText("There was an error buying that item. You do not have the required input."), false);
                return;
            }

            MarketInstance.LOGGER.info("Listing new order from {}. Input: {}, Output: {}", newOrder.getSeller().getId(), newOrder.getInput().serialise(), newOrder.getOutput().serialise());

            try {
                Statement stmt = instance.getDatabase().createStatement();
                String sql = String.format("INSERT INTO buy_requests (SELLER,TYPE,CLAIMABLE,SERIALISED) VALUES ('%s','%s','%s','%s')",
                        newOrder.getSeller().getId(),
                        newOrder.getIdentifier(),
                        newOrder.serialiseClaimable(),
                        newOrder.serialiseOrder());
                stmt.execute(sql);
                ResultSet set = stmt.executeQuery("SELECT last_insert_rowid() AS ID FROM buy_requests;");
                newOrder.id = set.getInt("ID");
                MarketListingEvents.LIST.invoker().onMarketEvent(newOrder);
                newOrder.refresh();
                set.close();
                stmt.close();

                newOrder.takeTotalOutput(playerEntity);
            } catch (SQLException throwables) {
                MarketInstance.LOGGER.error("There was a database error listing an order from {}. Input: {}, Output: {}", newOrder.getSeller().getId(), newOrder.getInput().serialise(), newOrder.getOutput().serialise());
                playerEntity.sendMessage(new LiteralText("There was an error listing on the market. The database threw an error."), false);
                throwables.printStackTrace();
                return;
            }

            instance.send(new UpdateAllTask());
            instance.profiler.pop();
        });
    }

}
