package io.github.aws404.market.guis;

import io.github.aws404.market.MarketInstance;
import io.github.aws404.market.guis.templates.AbstractMarketScreen;
import io.github.aws404.market.orders.MarketListing;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;

public class PersonalTrades extends AbstractMarketScreen {
    private static final ItemStack RETURN_TO_MARKET = createItem(Items.PAPER, new LiteralText("Return to Trade Market"), null);

    public PersonalTrades(ServerPlayerEntity player, int page) {
        super(player, player.getName().asString() + "'s Listings", page);
    }

    @Override
    protected Button getRightButton() {
        return new Button(playerEntity -> new MainMenu(player, 1).open(), RETURN_TO_MARKET);
    }

    @Override
    protected ArrayList<MarketListing> getBuyOrders() {
        return MarketInstance.MARKET_INSTANCE.getBuyOrders(player.getUuid(), MarketListing::isActiveTrade);
    }
}
