package io.github.aws404.market.guis;

import io.github.aws404.market.MarketInstance;
import io.github.aws404.market.guis.templates.AbstractMarketScreen;
import io.github.aws404.market.orders.MarketListing;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;

public class MainMenu extends AbstractMarketScreen {
    public static final ItemStack YOUR_TRADES = createItem(Items.PLAYER_HEAD, new LiteralText("View Your Trades"), null);

    public MainMenu(ServerPlayerEntity player, int page) {
        super(player, "Marketplace", page);
    }

    @Override
    protected Button getRightButton() {
        return new Button(playerEntity -> new PersonalTrades(playerEntity, 1).open(), YOUR_TRADES);
    }

    @Override
    protected ArrayList<MarketListing> getBuyOrders() {
        return MarketInstance.MARKET_INSTANCE.getBuyOrders(MarketListing::isAvailable);
    }
}
