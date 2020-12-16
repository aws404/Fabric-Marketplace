package io.github.aws404.market.guis;

import io.github.aws404.market.MarketInstance;
import io.github.aws404.market.guis.templates.AbstractMarketScreen;
import io.github.aws404.market.orders.MarketListing;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;

public class SearchScreen extends AbstractMarketScreen {
    private final String search;

    public SearchScreen(ServerPlayerEntity player, String search, int page) {
        super(player, String.format("Search '%s'", search), page);
        this.search = search;
    }

    @Override
    protected Button getRightButton() {
        return new Button((player) -> new MainMenu(player, 1).open(), createItem(Items.ACACIA_BUTTON, new LiteralText("Return to Trade Market"), null));
    }

    @Override
    protected ArrayList<MarketListing> getBuyOrders() {
        return MarketInstance.MARKET_INSTANCE.getBuyOrders((order) -> order.isAvailable() && (fuzzySearch(order.getInput().serialise().toString(), search) || fuzzySearch(order.getOutput().serialise().toString(), search)));
    }

    public static boolean fuzzySearch(String toCheck, String searchText) {
        int i = 0, j = 0;
        char[] toCheckArray = toCheck.toCharArray();
        for (char c : searchText.toCharArray()) {
            while (i < toCheck.length()) {
                if (c == toCheckArray[i]) {
                    i++;
                    j++;
                    break;
                }
                i++;
            }
        }
        return j == searchText.length();
    }
}
